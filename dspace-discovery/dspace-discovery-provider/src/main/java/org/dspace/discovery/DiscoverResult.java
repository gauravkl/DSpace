/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import org.dspace.content.DSpaceObject;

import java.util.*;

/**
 * This class represents the result that the discovery search impl returns
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class DiscoverResult {

    private long totalSearchResults;
    private int start;
    private List<DSpaceObject> dspaceObjects;
    private Map<String, List<FacetResult>> facetResults;
    /** A map that contains all the documents sougth after, the key is a string representation of the DSpace object */
    private Map<String, List<SearchDocument>> searchDocuments;
    private int maxResults = -1;


    public DiscoverResult() {
        dspaceObjects = new ArrayList<DSpaceObject>();
        facetResults = new LinkedHashMap<String, List<FacetResult>>();
        searchDocuments = new LinkedHashMap<String, List<SearchDocument>>();
    }


    public void addDSpaceObject(DSpaceObject dso){
        this.dspaceObjects.add(dso);
    }

    public List<DSpaceObject> getDspaceObjects() {
        return dspaceObjects;
    }

    public long getTotalSearchResults() {
        return totalSearchResults;
    }

    public void setTotalSearchResults(long totalSearchResults) {
        this.totalSearchResults = totalSearchResults;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void addFacetResult(String facetField, FacetResult ...facetResults){
        List<FacetResult> facetValues = this.facetResults.get(facetField);
        if(facetValues == null)
        {
            facetValues = new ArrayList<FacetResult>();
        }
        facetValues.addAll(Arrays.asList(facetResults));
        this.facetResults.put(facetField, facetValues);
    }

    public Map<String, List<FacetResult>> getFacetResults() {
        return facetResults;
    }

    public List<FacetResult> getFacetResult(String facet){
        return facetResults.get(facet) == null ? new ArrayList<FacetResult>() : facetResults.get(facet);
    }

    public static final class FacetResult{
//        TODO: rename storedValue to filterValue
        private String asFilterQuery;
        private String displayedValue;
        private long count;

        public FacetResult(String asFilterQuery, String displayedValue, long count) {
            this.asFilterQuery = asFilterQuery;
            this.displayedValue = displayedValue;
            this.count = count;
        }

        public String getAsFilterQuery() {
            return asFilterQuery;
        }

        public String getDisplayedValue() {
            return displayedValue;
        }

        public long getCount() {
            return count;
        }
    }

    public void addSearchDocument(DSpaceObject dso, SearchDocument searchDocument){
        String dsoString = SearchDocument.getDspaceObjectStringRepresentation(dso);
        List<SearchDocument> docs = searchDocuments.get(dsoString);
        if(docs == null){
            docs = new ArrayList<SearchDocument>();
        }
        docs.add(searchDocument);
        searchDocuments.put(dsoString, docs);
    }

    /**
     * Returns all the sought after search document values 
     * @param dso the dspace object we want our search documents for
     * @return the search documents list
     */
    public List<SearchDocument> getSearchDocument(DSpaceObject dso){
        String dsoString = SearchDocument.getDspaceObjectStringRepresentation(dso);
        List<SearchDocument> result = searchDocuments.get(dsoString);
        if(result == null){
            return new ArrayList<SearchDocument>();
        }else{
            return result;
        }
    }

    /**
     * This class contains values from the fields searched for in DiscoveryQuery.java
     */
    public static final class SearchDocument{
        private Map<String, List<String>> searchFields;

        public SearchDocument() {
            this.searchFields = new LinkedHashMap<String, List<String>>();
        }

        public void addSearchField(String field, String ...values){
            List<String>searchFieldValues = searchFields.get(field);
            if(searchFieldValues == null){
                searchFieldValues = new ArrayList<String>();
            }
            searchFieldValues.addAll(Arrays.asList(values));
            searchFields.put(field, searchFieldValues);
        }

        public Map<String, List<String>> getSearchFields() {
            return searchFields;
        }

        public List<String> getSearchFieldValues(String field){
            if(searchFields.get(field) == null)
                return new ArrayList<String>();
            else
                return searchFields.get(field);
        }

        public static String getDspaceObjectStringRepresentation(DSpaceObject dso){
            return dso.getType() + ":" + dso.getID();
        }
    }
}
