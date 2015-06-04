package com.brettnamba.capsules.provider;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleDiscovery;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds Capsule-related ContentProviderOperations
 *
 * @author Brett
 */
public class CapsuleOperations {

    /**
     * ContentResolver
     */
    private ContentResolver mResolver;

    /**
     * Will hold any ContentProviderOperations that are built
     */
    private ArrayList<ContentProviderOperation> mOperations;

    /**
     * A rough limit to how many ContentProviderOperations to apply at once
     */
    public static final int BATCH_SIZE = 50;

    /**
     * Constructor
     *
     * @param resolver The ContentResolver
     */
    public CapsuleOperations(ContentResolver resolver) {
        this.mResolver = resolver;
        this.mOperations = new ArrayList<ContentProviderOperation>();
    }

    /**
     * Determines the index of the last ContentProviderOperation added
     *
     * @return The index of the last ContentProviderOperation added to the collection
     */
    public int getLastOperationIndex() {
        return this.mOperations.size() - 1;
    }

    /**
     * Executes the stored ContentProviderOperations
     *
     * @return The results of the operation batch
     */
    public ContentProviderResult[] applyBatch() {
        ContentProviderResult[] results;
        try {
            // Apply the operations as a batch
            results = this.mResolver.applyBatch(CapsuleContract.AUTHORITY, this.mOperations);
            // If there were results returned, clear out the operations
            if (results != null) {
                this.mOperations.clear();
            }
        } catch (RemoteException | OperationApplicationException e) {
            results = null;
        }
        return results;
    }

    /**
     * Checks the size of the internal size of the collection of ContentProviderOperations
     * and applies them if it is over the recommend batch size
     */
    public void checkAndApply() {
        if (this.mOperations.size() > CapsuleOperations.BATCH_SIZE) {
            this.applyBatch();
        }
    }

    public void buildCapsuleInsert(Capsule capsule, boolean withYield) {
        // Build the INSERT operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(CapsuleContract.Capsules.CONTENT_URI);
        builder.withValues(Capsules.buildContentValues(capsule));
        builder.withYieldAllowed(withYield);
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildCapsuleUpdate(Capsule capsule, boolean withYield) {
        // URI for updating a Capsule
        Uri uri = ContentUris.withAppendedId(CapsuleContract.Capsules.CONTENT_URI, capsule.getId());
        // Build the UPDATE operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(uri);
        builder.withValues(Capsules.buildContentValues(capsule));
        builder.withYieldAllowed(withYield);
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildCapsuleDelete(Capsule capsule, boolean withYield) {
        // Build the Capsule URI
        Uri uri = ContentUris.withAppendedId(CapsuleContract.Capsules.CONTENT_URI, capsule.getId());

        // Build the DELETE operation
        this.mOperations.add(ContentProviderOperation.newDelete(uri)
                .withYieldAllowed(withYield)
                .build());
        // Get the capsule ID
        String capsuleId = String.valueOf(capsule.getId());
        // Build the Discovery DELETE operation
        this.mOperations.add(ContentProviderOperation.newDelete(CapsuleContract.Discoveries.CONTENT_URI)
                .withSelection(CapsuleContract.Discoveries.CAPSULE_ID + " = ?", new String[]{capsuleId})
                .withYieldAllowed(false)
                .build());
        // Build the Ownership DELETE operation
        this.mOperations.add(ContentProviderOperation.newDelete(CapsuleContract.Ownerships.CONTENT_URI)
                .withSelection(CapsuleContract.Ownerships.CAPSULE_ID + " = ?", new String[]{capsuleId})
                .withYieldAllowed(false)
                .build());
    }

    public void buildCapsuleCleanup(long keepId, List<Long> duplicateIds) {
        // Convert the extra IDs into selection arguments
        String[] selectionArgs = CapsuleOperations.convertIdsToArguments(duplicateIds);
        String preparedStatementParams = CapsuleOperations.buildPreparedStatementParameters(duplicateIds.size());
        // UPDATE operation to change all Discoveries referencing the extra IDs to instead reference the keep ID
        String discoverySelection = CapsuleContract.Discoveries.CAPSULE_ID
                + " IN (" + preparedStatementParams + ")";
        this.mOperations.add(ContentProviderOperation.newUpdate(CapsuleContract.Discoveries.CONTENT_URI)
                .withSelection(discoverySelection, selectionArgs)
                .withValue(CapsuleContract.Discoveries.CAPSULE_ID, keepId)
                .withYieldAllowed(true)
                .build());
        // UPDATE operation to change all Ownerships referencing the extra IDs to instead reference the keep ID
        String ownershipSelection = CapsuleContract.Ownerships.CAPSULE_ID
                + " IN (" + preparedStatementParams + ")";
        this.mOperations.add(ContentProviderOperation.newUpdate(CapsuleContract.Ownerships.CONTENT_URI)
                .withSelection(ownershipSelection, selectionArgs)
                .withValue(CapsuleContract.Ownerships.CAPSULE_ID, keepId)
                .withYieldAllowed(false)
                .build());
        // DELETE the duplicate Capsules
        String capsuleSelection = CapsuleContract.Capsules._ID
                + " IN (" + preparedStatementParams + ")";
        this.mOperations.add(ContentProviderOperation.newDelete(CapsuleContract.Capsules.CONTENT_URI)
                .withSelection(capsuleSelection, selectionArgs)
                .withYieldAllowed(false)
                .build());
    }

    public void buildDiscoveryInsert(CapsuleDiscovery discovery, boolean withYield, CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction, CapsuleContract.Discoveries.CONTENT_URI);
        // Build the INSERT operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
        builder.withValues(Discoveries.buildContentValues(discovery));
        builder.withYieldAllowed(withYield);
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildDiscoveryInsert(CapsuleDiscovery discovery, boolean withYield,
                                     int capsuleIdBackRefIndex, CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction, CapsuleContract.Discoveries.CONTENT_URI);
        // Build the INSERT operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
        builder.withValues(Discoveries.buildContentValues(discovery));
        builder.withYieldAllowed(withYield);
        // Add the back value reference index for the Capsule ID
        if (capsuleIdBackRefIndex >= 0) {
            builder.withValueBackReference(CapsuleContract.Discoveries.CAPSULE_ID, capsuleIdBackRefIndex);
        }
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildDiscoveryUpdate(CapsuleDiscovery discovery, boolean withYield,
                                     CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction,
                ContentUris.withAppendedId(CapsuleContract.Discoveries.CONTENT_URI, discovery.getDiscoveryId()));
        // Build the UPDATE operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(uri);
        builder.withValues(Discoveries.buildContentValues(discovery));
        builder.withYieldAllowed(withYield);
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildDiscoveryUpdate(CapsuleDiscovery discovery, boolean withYield,
                                     int capsuleIdBackRefIndex, CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction,
                ContentUris.withAppendedId(CapsuleContract.Discoveries.CONTENT_URI, discovery.getDiscoveryId()));
        // Build the UPDATE operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(uri);
        builder.withValues(Discoveries.buildContentValues(discovery));
        builder.withYieldAllowed(withYield);
        // Add the back value reference for the Capsule ID
        if (capsuleIdBackRefIndex >= 0) {
            builder.withValueBackReference(CapsuleContract.Discoveries.CAPSULE_ID, capsuleIdBackRefIndex);
        }
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildDiscoveryCleanup(List<Long> duplicateIds) {
        String selection = CapsuleContract.Discoveries._ID
                + " IN (" + CapsuleOperations.buildPreparedStatementParameters(duplicateIds.size()) + ")";
        String[] selectionArgs = CapsuleOperations.convertIdsToArguments(duplicateIds);
        this.mOperations.add(ContentProviderOperation.newDelete(CapsuleContract.Discoveries.CONTENT_URI)
                .withSelection(selection, selectionArgs)
                .withYieldAllowed(true)
                .build());
    }

    public void buildDiscoverySave(Capsule capsule, CapsuleContract.SyncStateAction syncAction) {
        // Make sure there is a sync ID
        if (capsule.getSyncId() <= 0) {
            throw new InvalidParameterException("The Capsule does not have a sync ID");
        }
        // Determine the best IDs for the Capsule
        CapsuleOperations.Capsules.determineBestId(this.mResolver, this, capsule);
        CapsuleOperations.Discoveries.determineBestId(this.mResolver, this, (CapsuleDiscovery) capsule);
        // Build INSERT/UPDATE operations depending on if the Capsule and Discovery already exist
        if (capsule.getId() > 0) {
            // Capsule UPDATE
            this.buildCapsuleUpdate(capsule, /* withYield */ true);
            // Check if the Discovery exists
            if (((CapsuleDiscovery) capsule).getDiscoveryId() > 0) {
                // Discovery UPDATE
                this.buildDiscoveryUpdate((CapsuleDiscovery) capsule, /* withYield */ false, syncAction);
            } else {
                // Discovery INSERT
                this.buildDiscoveryInsert((CapsuleDiscovery) capsule, /* withYield */ false, syncAction);
            }
        } else {
            // Capsule INSERT
            this.buildCapsuleInsert(capsule, /* withYield */ true);
            // Check if the Discovery exists
            if (((CapsuleDiscovery) capsule).getDiscoveryId() > 0) {
                // Discovery UPDATE
                this.buildDiscoveryUpdate((CapsuleDiscovery) capsule, /* withYield */ false,
                        this.getLastOperationIndex(), syncAction);
            } else {
                // Discovery INSERT
                this.buildDiscoveryInsert((CapsuleDiscovery) capsule, /* withYield */ false,
                        this.getLastOperationIndex(), syncAction);
            }
        }
    }

    public void buildOwnershipInsert(CapsuleOwnership ownership, boolean withYield,
                                     CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction, CapsuleContract.Ownerships.CONTENT_URI);
        // Build the INSERT operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
        builder.withValues(Ownerships.buildContentValues(ownership));
        builder.withYieldAllowed(withYield);
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildOwnershipInsert(CapsuleOwnership ownership, boolean withYield,
                                     int capsuleIdBackRefIndex, CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction, CapsuleContract.Ownerships.CONTENT_URI);
        // Build the INSERT operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
        builder.withValues(Ownerships.buildContentValues(ownership));
        builder.withYieldAllowed(withYield);
        // Add the back value reference index for the Capsule ID
        if (capsuleIdBackRefIndex >= 0) {
            builder.withValueBackReference(CapsuleContract.Ownerships.CAPSULE_ID, capsuleIdBackRefIndex);
        }
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildOwnershipUpdate(CapsuleOwnership ownership, boolean withYield,
                                     CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction,
                ContentUris.withAppendedId(CapsuleContract.Ownerships.CONTENT_URI, ownership.getOwnershipId()));
        // Build the UPDATE operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(uri);
        builder.withValues(Ownerships.buildContentValues(ownership));
        builder.withYieldAllowed(withYield);
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildOwnershipUpdate(CapsuleOwnership ownership, boolean withYield,
                                     int capsuleIdBackRefIndex, CapsuleContract.SyncStateAction syncAction) {
        // Build the URI
        Uri uri = CapsuleOperations.appendDirtyQueryParam(syncAction,
                ContentUris.withAppendedId(CapsuleContract.Ownerships.CONTENT_URI, ownership.getOwnershipId()));
        // Build the UPDATE operation
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(uri);
        builder.withValues(Ownerships.buildContentValues(ownership));
        builder.withYieldAllowed(withYield);
        // Add the back value reference index for the Capsule ID
        if (capsuleIdBackRefIndex >= 0) {
            builder.withValueBackReference(CapsuleContract.Ownerships.CAPSULE_ID, capsuleIdBackRefIndex);
        }
        // Add it to the collection
        this.mOperations.add(builder.build());
    }

    public void buildOwnershipCleanup(List<Long> duplicateIds) {
        String selection = CapsuleContract.Ownerships._ID
                + " IN (" + CapsuleOperations.buildPreparedStatementParameters(duplicateIds.size()) + ")";
        String[] selectionArgs = CapsuleOperations.convertIdsToArguments(duplicateIds);
        this.mOperations.add(ContentProviderOperation.newDelete(CapsuleContract.Ownerships.CONTENT_URI)
                .withSelection(selection, selectionArgs)
                .withYieldAllowed(true)
                .build());
    }

    public void buildOwnershipSave(Capsule capsule, CapsuleContract.SyncStateAction syncAction) {
        // Determine the best IDs for the Capsule
        CapsuleOperations.Capsules.determineBestId(this.mResolver, this, capsule);
        CapsuleOperations.Ownerships.determineBestId(this.mResolver, this, (CapsuleOwnership) capsule);
        // Build the INSERT/UPDATE operations depending on if the Capsule and Ownership exist
        if (capsule.getId() > 0) {
            // Capsule UPDATE
            this.buildCapsuleUpdate(capsule, /* withYield */ true);
            // Check if the Ownership exists
            if (((CapsuleOwnership) capsule).getOwnershipId() > 0) {
                // Ownership UPDATE
                this.buildOwnershipUpdate((CapsuleOwnership) capsule, /* withYield */ false, syncAction);
            } else {
                // Ownership INSERT
                this.buildOwnershipInsert((CapsuleOwnership) capsule, /* withYield */ false, syncAction);
            }
        } else {
            // Capsule INSERT
            this.buildCapsuleInsert(capsule, /* withYield */ true);
            // Check if the Ownership exists
            if (((CapsuleOwnership) capsule).getOwnershipId() > 0) {
                // Ownership UPDATE
                this.buildOwnershipUpdate((CapsuleOwnership) capsule, /* withYield */ false,
                        this.getLastOperationIndex(), syncAction);
            } else {
                // Ownership INSERT
                this.buildOwnershipInsert((CapsuleOwnership) capsule, /* withYield */ false,
                        this.getLastOperationIndex(), syncAction);
            }
        }
    }

    public static Uri appendDirtyQueryParam(CapsuleContract.SyncStateAction syncAction, Uri uri) {
        if (syncAction.equals(CapsuleContract.SyncStateAction.DIRTY)) {
            uri = uri.buildUpon()
                    .appendQueryParameter(CapsuleContract.Query.Parameters.SET_DIRTY, CapsuleContract.Query.Values.TRUE)
                    .build();
        } else if (syncAction.equals(CapsuleContract.SyncStateAction.CLEAN)) {
            uri = uri.buildUpon()
                    .appendQueryParameter(CapsuleContract.Query.Parameters.SET_DIRTY, CapsuleContract.Query.Values.FALSE)
                    .build();
        }
        return uri;
    }

    public static String[] convertIdsToArguments(List<Long> ids) {
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            args[i] = String.valueOf(ids.get(i));
        }
        return args;
    }

    public static String buildPreparedStatementParameters(int count) {
        StringBuilder builder = new StringBuilder(count * 2 - 1); // One question mark and comma per parameter except for the last one
        builder.append("?");
        for (int i = 1; i < count; i++) {
            builder.append(",?");
        }
        return builder.toString();
    }

    public static class Capsules {

        public static long determineBestId(ContentResolver resolver, CapsuleOperations operations, Capsule capsule) {
            // Query
            Cursor c = resolver.query(
                    CapsuleContract.Capsules.CONTENT_URI,
                    new String[]{CapsuleContract.Capsules._ID},
                    CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules.SYNC_ID + " = ?"
                            + " AND " + CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules.SYNC_ID + " != ?",
                    new String[]{String.valueOf(capsule.getSyncId()), "0"},
                    null
            );
            // Check if there is a row
            long id;
            if (c.getCount() < 1) {
                // There is no existing row
                id = 0;
            } else if (c.getCount() > 1) {
                // Get the first matching row to keep
                c.moveToFirst();
                id = c.getLong(c.getColumnIndex(CapsuleContract.Capsules._ID));
                // Add any extra IDs to a collection
                List<Long> duplicateIds = new ArrayList<Long>();
                while (c.moveToNext()) {
                    duplicateIds.add(c.getLong(c.getColumnIndex(CapsuleContract.Capsules._ID)));
                }
                // Build operations to remove the extra IDs
                operations.buildCapsuleCleanup(id, duplicateIds);
            } else {
                // There is only one row, so move to it and get the ID
                c.moveToFirst();
                id = c.getLong(c.getColumnIndex(CapsuleContract.Capsules._ID));
            }
            // Close the cursor
            c.close();

            // If a better ID was found (non-zero), reassign it
            if (id > 0) {
                capsule.setId(id);
            }

            return id;
        }

        public static ContentValues buildContentValues(Capsule capsule) {
            ContentValues values = new ContentValues();
            values.put(CapsuleContract.Capsules.SYNC_ID, capsule.getSyncId());
            values.put(CapsuleContract.Capsules.NAME, capsule.getName());
            values.put(CapsuleContract.Capsules.LATITUDE, capsule.getLatitude());
            values.put(CapsuleContract.Capsules.LONGITUDE, capsule.getLongitude());
            return values;
        }

    }

    public static class Discoveries {

        public static boolean save(ContentResolver resolver, Capsule capsule,
                                   CapsuleContract.SyncStateAction syncAction) {
            // Build the ContentProviderOperations for the save
            CapsuleOperations operations = new CapsuleOperations(resolver);
            operations.buildDiscoverySave(capsule, syncAction);

            // Apply the batch operation
            ContentProviderResult[] results = operations.applyBatch();

            // Make sure the rows were properly updated
            boolean success = true;

            // Check the first result (Capsule operation)
            if (results[0].uri != null) {
                // The Capsule operation was an INSERT, so parse the ID from the URI
                capsule.setId(ContentUris.parseId(results[0].uri));
            } else {
                // The Capsule operation was an UPDATE, so make sure a row was updated
                if (results[0].count < 1) {
                    success = false;
                }
            }

            // Check the second result (Discovery operation)
            if (results[1].uri != null) {
                // The Discovery operation was an INSERT, so parse the ID from the URI
                ((CapsuleDiscovery) capsule).setDiscoveryId(ContentUris.parseId(results[1].uri));
            } else {
                // The Discovery operation was an UPDATE, so make sure a row was updated
                if (results[1].count < 1) {
                    success = false;
                }
            }

            return success;
        }

        public static long determineBestId(ContentResolver resolver, CapsuleOperations operations,
                                           CapsuleDiscovery capsule) {
            // Build the query URI
            Uri uri = CapsuleContract.Discoveries.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CapsuleContract.Query.Parameters.INNER_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                    .build();
            // Query
            Cursor c = resolver.query(
                    uri,
                    CapsuleContract.Discoveries.CAPSULE_JOIN_PROJECTION,
                    CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules.SYNC_ID + " = ?"
                            + " AND " + CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules.SYNC_ID + " != ?",
                    new String[]{String.valueOf(capsule.getSyncId()), "0"},
                    null
            );
            // Check if there is a row
            long id;
            if (c.getCount() < 1) {
                // There is no existing row
                id = 0;
            } else if (c.getCount() > 1) {
                // There is more than one row, so get the first matching one
                c.moveToFirst();
                id = c.getLong(c.getColumnIndex(CapsuleContract.Discoveries.DISCOVERY_ID_ALIAS));
                // Add any extra IDs to a collection
                List<Long> duplicateIds = new ArrayList<Long>();
                while (c.moveToNext()) {
                    duplicateIds.add(c.getLong(c.getColumnIndex(CapsuleContract.Discoveries.DISCOVERY_ID_ALIAS)));
                }
                // Build operations to remove the extra IDs
                operations.buildDiscoveryCleanup(duplicateIds);
            } else {
                // There is only one row, so get the ID
                c.moveToFirst();
                id = c.getLong(c.getColumnIndex(CapsuleContract.Discoveries.DISCOVERY_ID_ALIAS));
            }
            // Close the cursor
            c.close();

            // If a better ID was found (non-zero), re-assign it
            if (id > 0) {
                capsule.setDiscoveryId(id);
            }

            return id;
        }

        public static ContentValues buildContentValues(CapsuleDiscovery capsule) {
            ContentValues values = new ContentValues();
            values.put(CapsuleContract.Discoveries.ACCOUNT_NAME, capsule.getAccountName());
            values.put(CapsuleContract.Discoveries.CAPSULE_ID, capsule.getId());
            values.put(CapsuleContract.Discoveries.ETAG, capsule.getEtag());
            values.put(CapsuleContract.Discoveries.RATING, capsule.getRating());
            values.put(CapsuleContract.Discoveries.FAVORITE, capsule.getFavorite());
            return values;
        }

    }

    public static class Ownerships {

        public static boolean save(ContentResolver resolver, Capsule capsule,
                                   CapsuleContract.SyncStateAction syncAction) {
            // Build the ContentProviderOperations for the save
            CapsuleOperations operations = new CapsuleOperations(resolver);
            operations.buildOwnershipSave(capsule, syncAction);

            // Apply the batch operation
            ContentProviderResult[] results = operations.applyBatch();

            // Make sure the rows were properly updated
            boolean success = true;

            // Check the first result (Capsule operation)
            if (results[0].uri != null) {
                // The Capsule operation was an INSERT, so parse the ID from the URI
                capsule.setId(ContentUris.parseId(results[0].uri));
            } else {
                // The Capsule operation was an UPDATE, so make sure a row was updated
                if (results[0].count < 1) {
                    success = false;
                }
            }

            // Check the second result (Ownership operation)
            if (results[1].uri != null) {
                // The Ownership operation was an INSERT, so parse the ID from the URI
                ((CapsuleOwnership) capsule).setOwnershipId(ContentUris.parseId(results[1].uri));
            } else {
                // The Ownership operation was an UPDATE, so make sure a row was updated
                if (results[1].count < 1) {
                    success = false;
                }
            }

            return success;
        }

        public static ArrayList<CapsuleOwnership> get(ContentResolver resolver, Account account,
                                                      CapsuleContract.SyncStateAction syncAction) {
            // Build the query URI
            Uri uri = CapsuleContract.Ownerships.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CapsuleContract.Query.Parameters.INNER_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                    .build();
            // Build the selection string
            String selection = CapsuleContract.Ownerships.TABLE_NAME + "." + CapsuleContract.Ownerships.ACCOUNT_NAME
                    + " = ?";
            if (syncAction.equals(CapsuleContract.SyncStateAction.DIRTY)) {
                selection += " AND " + CapsuleContract.Ownerships.DIRTY + " = 1";
            } else if (syncAction.equals(CapsuleContract.SyncStateAction.CLEAN)) {
                selection += " AND " + CapsuleContract.Ownerships.DIRTY + " = 0";
            }
            // Query
            Cursor c = resolver.query(
                    uri,
                    CapsuleContract.Ownerships.CAPSULE_JOIN_PROJECTION,
                    selection,
                    new String[]{account.name},
                    null
            );
            // Get the results from the cursor
            ArrayList<CapsuleOwnership> capsules = new ArrayList<CapsuleOwnership>();
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    capsules.add(new CapsuleOwnership(c));
                }
            }
            // Close the cursor
            c.close();

            return capsules;
        }

        public static long determineBestId(ContentResolver resolver, CapsuleOperations operations,
                                           CapsuleOwnership capsule) {
            // Build the query URI
            Uri uri = CapsuleContract.Ownerships.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CapsuleContract.Query.Parameters.INNER_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                    .build();
            // Query
            Cursor c = resolver.query(
                    uri,
                    CapsuleContract.Ownerships.CAPSULE_JOIN_PROJECTION,
                    CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules.SYNC_ID + " = ?"
                            + " AND " + CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules.SYNC_ID + " != ?",
                    new String[]{String.valueOf(capsule.getSyncId()), "0"},
                    null
            );
            // Check if there is a row
            long id;
            if (c.getCount() < 1) {
                // There is no existing row
                id = 0;
            } else if (c.getCount() > 1) {
                // There is more than one row, so get the first matching one
                c.moveToFirst();
                id = c.getLong(c.getColumnIndex(CapsuleContract.Ownerships.OWNERSHIP_ID_ALIAS));
                // Add any extra IDs to a collection
                List<Long> duplicateIds = new ArrayList<Long>();
                while (c.moveToNext()) {
                    duplicateIds.add(c.getLong(c.getColumnIndex(CapsuleContract.Ownerships.OWNERSHIP_ID_ALIAS)));
                }
                // Build operations to remove the extra IDs
                operations.buildOwnershipCleanup(duplicateIds);
            } else {
                // There is only one row, so get the ID
                c.moveToFirst();
                id = c.getLong(c.getColumnIndex(CapsuleContract.Ownerships.OWNERSHIP_ID_ALIAS));
            }
            // Close the cursor
            c.close();

            // If a better ID was found (non-zero), re-assign it
            if (id > 0) {
                capsule.setOwnershipId(id);
            }

            return id;
        }

        public static ContentValues buildContentValues(CapsuleOwnership capsule) {
            ContentValues values = new ContentValues();
            values.put(CapsuleContract.Ownerships.ACCOUNT_NAME, capsule.getAccountName());
            values.put(CapsuleContract.Ownerships.CAPSULE_ID, capsule.getId());
            values.put(CapsuleContract.Ownerships.ETAG, capsule.getEtag());
            return values;
        }

    }

}
