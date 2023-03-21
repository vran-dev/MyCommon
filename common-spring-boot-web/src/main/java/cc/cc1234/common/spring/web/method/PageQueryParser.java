package cc.cc1234.common.spring.web.method;

import cc.cc1234.common.core.PageQuery;
import cc.cc1234.common.core.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;
import java.util.OptionalInt;

public class PageQueryParser {

    public static final String DEF_PAGE_NAME = "page";

    public static final String DEF_PAGE_SIZE_NAME = "size";

    public static final String DEF_COUNT_NAME = "count";

    public static final String DEF_SORT_NAME = "sort";

    private static final String DEF_SORT_DELIMITER = ",\\s*";

    public PageQuery parseQuery(NativeWebRequest webRequest) {
        PageQuery query = PageQuery.of();
        parsePage(webRequest).ifPresent(query::setPage);
        parseSize(webRequest).ifPresent(query::setSize);
        parseCount(webRequest).ifPresent(query::setCount);
        parseSort(webRequest).ifPresent(query::setSort);
        return query;
    }

    protected OptionalInt parsePage(NativeWebRequest webRequest) {
        String page = webRequest.getParameter(DEF_PAGE_NAME);
        if (page != null) {
            return parseInt(page);
        }

        // compatible with old version
        page = webRequest.getParameter("pageNumber");
        if (page != null) {
            return parseInt(page);
        }

        return OptionalInt.empty();
    }

    protected OptionalInt parseSize(NativeWebRequest webRequest) {
        String page = webRequest.getParameter(DEF_PAGE_SIZE_NAME);
        if (page != null) {
            return parseInt(page);
        }

        // compatible with old version
        page = webRequest.getParameter("pageSize");
        if (page != null) {
            return parseInt(page);
        }

        return OptionalInt.empty();
    }

    protected Optional<Boolean> parseCount(NativeWebRequest webRequest) {
        String count = webRequest.getParameter(DEF_COUNT_NAME);
        if (count != null) {
            return parseBoolean(count);
        }
        return Optional.empty();
    }

    protected Optional<Sort> parseSort(NativeWebRequest webRequest) {
        String[] sortParameters = webRequest.getParameterValues(DEF_SORT_NAME);
        if (sortParameters == null) {
            return Optional.empty();
        }

        Sort sort = new Sort();
        for (String sortItem : sortParameters) {
            String[] sortItemParts = sortItem.split(DEF_SORT_DELIMITER);
            if (sortItemParts.length == 1) {
                if (StringUtils.hasText(sortItemParts[0])) {
                    // e.g. sort=id
                    sort.addOrder(sortItemParts[0], Sort.Direction.ASC);
                }
            } else if (sortItemParts.length == 2) {
                // e.g. sort=id,desc
                String fieldName;
                String direction;
                if (StringUtils.hasText(sortItemParts[0])) {
                    fieldName = sortItemParts[0];
                    if (StringUtils.hasText(sortItemParts[1])) {
                        direction = sortItemParts[1];
                    } else {
                        direction = Sort.Direction.ASC.name();
                    }
                    sort.addOrder(fieldName, Sort.Direction.of(direction.toUpperCase()));
                }
            }
        }
        return Optional.of(sort);
    }

    private OptionalInt parseInt(String value) {
        if (value != null) {
            try {
                return OptionalInt.of(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                return OptionalInt.empty();
            }
        }
        return OptionalInt.empty();
    }

    private Optional<Boolean> parseBoolean(String value) {
        if (value != null) {
            return Optional.of(Boolean.parseBoolean(value));
        }
        return Optional.empty();
    }
}
