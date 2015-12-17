package com.brettnamba.capsules.http;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple object representing the collection of different request parameters that can be used when
 * requesting Capsules from the web service
 */
public class CapsuleRequestParameters {

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

}
