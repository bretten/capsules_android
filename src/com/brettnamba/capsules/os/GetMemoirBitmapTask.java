package com.brettnamba.capsules.os;

import android.graphics.Bitmap;

/**
 * Background thread task to get a Memoir's image file from the server and convert it to a
 * Bitmap object.
 */
public class GetMemoirBitmapTask extends AsyncListenerTask<Long, Void, Bitmap> {

    /**
     * Listener that will handle the callbacks
     */
    private GetMemoirBitmapTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener Listener that will handle the callbacks
     */
    public GetMemoirBitmapTask(GetMemoirBitmapTaskListener listener) {
        this.setListener(listener);
    }

    /**
     * Sets the listener for the AsyncTask
     *
     * @param listener The listener that handles the callbacks
     */
    @Override
    public void setListener(TaskListener listener) {
        try {
            this.mListener = (GetMemoirBitmapTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    listener.toString() + " does not implement GetMemoirBitmapTaskListener");
        }
    }

    /**
     * Removes the listener that handles the callbacks
     */
    @Override
    public void removeListener() {
        this.mListener = null;
    }

    /**
     * Delegates the background work to the listener
     *
     * @param params The server-side ID of the Memoir
     * @return The constructed Bitmap
     */
    @Override
    protected Bitmap doInBackground(Long... params) {
        if (this.mListener != null && params[0] != null) {
            return this.mListener.duringGetMemoirBitmap(params[0]);
        }
        return null;
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreGetMemoirBitmap();
        }
    }

    /**
     * Delegates the process to the listener
     *
     * @param bitmap The Bitmap constructed using the image data from the server
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (this.mListener != null && bitmap != null) {
            this.mListener.onPostGetMemoirBitmap(bitmap);
        }
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onGetMemoirBitmapCancelled();
        }
    }

}
