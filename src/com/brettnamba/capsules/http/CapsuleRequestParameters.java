package com.brettnamba.capsules.http;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple object representing the collection of different request parameters that can be used when
 * requesting Capsules from the web service
 */
public class CapsuleRequestParameters implements Parcelable {

    /**
     * The sort order parameter value
     */
    private int mSort;

    /**
     * The filtering parameter value
     */
    private int mFilter;

    /**
     * The current page of results parameter value
     */
    private int mPage;

    /**
     * The search term parameter value
     */
    private String mSearch;

    /**
     * Empty constructor
     */
    public CapsuleRequestParameters() {
    }

    /**
     * Instantiates a CapsuleRequestParameters given a Parcel
     *
     * @param in The Parcel used to instantiate the CapsuleRequestParameters
     */
    private CapsuleRequestParameters(Parcel in) {
        this.mSort = in.readInt();
        this.mFilter = in.readInt();
        this.mPage = in.readInt();
        this.mSearch = in.readString();
    }

    /**
     * Sets the sort parameter value
     *
     * @param sort Sort value
     * @return Returns self to allow for chaining
     */
    public CapsuleRequestParameters setSort(int sort) {
        this.mSort = sort;
        return this;
    }

    /**
     * Sets the filter parameter value
     *
     * @param filter Filter value
     * @return Returns self to allow for chaining
     */
    public CapsuleRequestParameters setFilter(int filter) {
        this.mFilter = filter;
        return this;
    }

    /**
     * Sets the page parameter value
     *
     * @param page Page value
     * @return Returns self to allow for chaining
     */
    public CapsuleRequestParameters setPage(int page) {
        this.mPage = page;
        return this;
    }

    /**
     * Sets the search parameter value
     *
     * @param search Search value
     * @return Returns self to allow for chaining
     */
    public CapsuleRequestParameters setSearch(String search) {
        this.mSearch = search;
        return this;
    }

    /**
     * Gets the current sort parameter value
     *
     * @return The sort parameter value
     */
    public int getSort() {
        return this.mSort;
    }

    /**
     * Gets the current filter parameter value
     *
     * @return The filter parameter value
     */
    public int getFilter() {
        return this.mFilter;
    }

    /**
     * Gets the current page parameter value
     *
     * @return The page parameter value
     */
    public int getPage() {
        return this.mPage;
    }

    /**
     * Gets the current search parameter value
     *
     * @return The search parameter value
     */
    public String getSearch() {
        return this.mSearch;
    }

    /**
     * Increments the page count
     */
    public void incrementPage() {
        this.mPage++;
    }

    /**
     * Gets the request parameters as a collection
     *
     * @return The collection of request parameters
     */
    public List<Pair<String, String>> getAsCollection() {
        List<Pair<String, String>> collection = new ArrayList<Pair<String, String>>();

        // Sort
        if (this.mSort > 0) {
            collection.add(new Pair<String, String>(RequestContract.Field.SORT,
                    String.valueOf(this.mSort)));
        }
        // Filter
        if (this.mFilter > 0) {
            collection.add(new Pair<String, String>(RequestContract.Field.FILTER,
                    String.valueOf(this.mFilter)));
        }
        // Page
        if (this.mPage > 0) {
            collection.add(new Pair<String, String>(RequestContract.Field.PAGE,
                    String.valueOf(this.mPage)));
        }
        // Search terms
        if (this.mSearch != null && !this.mSearch.isEmpty()) {
            this.mSearch = this.mSearch.trim();
            collection.add(new Pair<String, String>(RequestContract.Field.SEARCH,
                    String.valueOf(this.mSearch)));
        }

        return collection;
    }

    /**
     * Describes the contents for the Parcelable
     *
     * @return The hash code
     */
    @Override
    public int describeContents() {
        return this.hashCode();
    }

    /**
     * Given a parcel, writes instance data to it
     *
     * @param dest  The parcel to write the data to
     * @param flags Flags for writing data
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSort);
        dest.writeInt(this.mFilter);
        dest.writeInt(this.mPage);
        dest.writeString(this.mSearch);
    }

    /**
     * Class that rebuilds a copy of the CapsuleRequestParameters Parcelable.
     */
    public static final Parcelable.Creator<CapsuleRequestParameters> CREATOR =
            new Parcelable.Creator<CapsuleRequestParameters>() {

                /**
                 * Instantiates a CapsuleRequestParameters from a Parcel
                 *
                 * @param source Parcel to use in instantiation
                 * @return The new CapsuleRequestParameters
                 */
                @Override
                public CapsuleRequestParameters createFromParcel(Parcel source) {
                    return new CapsuleRequestParameters(source);
                }

                /**
                 * Creates a CapsuleRequestParameters array of the specified size
                 *
                 * @param size The size of the array to create
                 * @return The new array
                 */
                @Override
                public CapsuleRequestParameters[] newArray(int size) {
                    return new CapsuleRequestParameters[size];
                }

            };

}
