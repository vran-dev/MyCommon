package cc.cc1234.common.spring.web.method;

import cc.cc1234.common.core.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class PageQueryMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final PageQueryParser pageQueryParser;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PageQuery.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        return pageQueryParser.parseQuery(webRequest);
    }
}
