package cc.cc1234.common.feign.encoder;

import cc.cc1234.common.core.PageQuery;
import cc.cc1234.common.core.Sort;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PageQueryEncoder implements Encoder {

    private final Encoder delegate;

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object instanceof PageQuery) {
            applyPageQuery((PageQuery) object, template);
        } else if (object instanceof Sort) {
            applySort((Sort) object, template);
        } else {
            delegate.encode(object, bodyType, template);
        }
    }

    private void applyPageQuery(PageQuery pageQuery, RequestTemplate template) {
        template.query("page", String.valueOf(pageQuery.getPage()));
        template.query("size", String.valueOf(pageQuery.getSize()));
        template.query("count", String.valueOf(pageQuery.isCount()));
        if (pageQuery.getSort() != null) {
            applySort(pageQuery.getSort(), template);
        }
    }

    private void applySort(Sort sort, RequestTemplate template) {
        if (sort == null || sort.getOrders() == null) {
            return;
        }

        List<String> values = new ArrayList<>();
        for (Sort.Order orderBy : sort.getOrders()) {
            values.add(orderBy.getField() + "%2C" + orderBy.getDirection());
        }
        if (!values.isEmpty()) {
            template.query("sort", values);
        }
    }
}
