package cc.cc1234.common.servlet.filter.cache;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContentCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof ContentCachingServletRequestWrapper)) {
            request = new ContentCachingServletRequestWrapper((HttpServletRequest) request);
        }
        if (!(response instanceof ContentCachingServletResponseWrapper)) {
            response = new ContentCachingServletResponseWrapper((HttpServletResponse) response);
        }
        chain.doFilter(request, response);
    }

}
