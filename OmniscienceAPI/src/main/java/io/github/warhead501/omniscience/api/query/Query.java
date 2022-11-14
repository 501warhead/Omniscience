package io.github.warhead501.omniscience.api.query;

import com.google.common.collect.Lists;

import java.util.List;

public class Query {

    private List<SearchCondition> searchCriteria;
    private int searchLimit = 2500;

    public Query() {
        this.searchCriteria = Lists.newArrayList();
    }

    public List<SearchCondition> getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(List<SearchCondition> searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public void addCondition(SearchCondition fieldCondition) {
        searchCriteria.add(fieldCondition);
    }

    public int getSearchLimit() {
        return searchLimit;
    }

    public void setSearchLimit(int searchLimit) {
        this.searchLimit = searchLimit;
    }
}
