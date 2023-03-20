package cc.cc1234.common.core;

import lombok.Data;

@Data
public class PageQuery {

    /**
     * 查询的页数(默认为1)
     */
    private int page = 1;

    /**
     * 每页的条数（默认为10)
     */
    private int size = 10;

    /**
     * 是否计算总数（默认计算)
     */
    private boolean count = false;

    /**
     * 排序参数
     */
    private Sort sort = Sort.EMPTY;

    public static PageQuery of() {
        return of(1);
    }

    public static PageQuery of(int page) {
        return of(page, 10);
    }

    public static PageQuery of(int page, int size) {
        return of(page, size, false);
    }

    public static PageQuery of(int page, int size, boolean count) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setSize(size);
        query.setCount(count);
        return query;
    }

    public PageQuery orderByDesc(String field) {
        return this.orderBy(field, Sort.Direction.DESC);
    }

    public PageQuery orderByAsc(String field) {
        return this.orderBy(field, Sort.Direction.ASC);
    }

    public PageQuery orderBy(String field, Sort.Direction direction) {
        if (sort == null) {
            sort = new Sort();
        }
        sort.addOrder(field, direction);
        return this;
    }
}
