package cc.cc1234.common.feign.encoder;

import cc.cc1234.common.core.PageQuery;
import cc.cc1234.common.core.Sort;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;
import feign.querymap.BeanQueryMapEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageQueryMapEncoder extends BeanQueryMapEncoder implements QueryMapEncoder {

    @Override
    public Map<String, Object> encode(Object object) throws EncodeException {
        if (supports(object)) {
            Map<String, Object> queryMap = new HashMap<>();
            if (object instanceof PageQuery) {
                applyPageQuery(queryMap, (PageQuery) object);
            } else if (object instanceof Sort) {
                applySort(queryMap, (Sort) object);
            }
            return queryMap;
        } else {
            return super.encode(object);
        }
    }

    protected boolean supports(Object object) {
        return object instanceof PageQuery || object instanceof Sort;
    }

    private void applyPageQuery(Map<String, Object> queryMap, PageQuery pageQuery) {
        queryMap.put("page", pageQuery.getPage());
        queryMap.put("size", pageQuery.getSize());
        queryMap.put("count", pageQuery.isCount());
        if (pageQuery.getSort() != null) {
            applySort(queryMap, pageQuery.getSort());
        }
    }

    private void applySort(Map<String, Object> queryMap, Sort sort) {
        List<String> sortQueries = new ArrayList<>();
        for (Sort.Order orderBy : sort.getOrders()) {
            sortQueries.add(orderBy.getField() + "%2C" + orderBy.getDirection());
        }
        if (!sortQueries.isEmpty()) {
            queryMap.put("sort", sortQueries);
        }
    }
}
