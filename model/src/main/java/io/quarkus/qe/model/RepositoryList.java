package io.quarkus.qe.model;

import java.util.List;

public class RepositoryList {
    private List<Repository> list;
    private long totalCount;

    public List<Repository> getList() {
        return list;
    }

    public void setList(List<Repository> list) {
        this.list = list;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
