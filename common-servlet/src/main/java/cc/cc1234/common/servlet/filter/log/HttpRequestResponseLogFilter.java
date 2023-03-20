package cc.cc1234.common.servlet.filter.log;

import cc.cc1234.common.servlet.filter.cache.ContentCachingServletRequestWrapper;
import cc.cc1234.common.servlet.filter.cache.ContentCachingServletResponseWrapper;
import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class HttpRequestResponseLogFilter implements Filter {

    private final LogConfig logConfig;

    private final LoggerFacade loggerFacade;

    private final BiFunction<String, String, Boolean> antPathMatcher;

    public HttpRequestResponseLogFilter(LogConfig logConfig,
                                        LoggerFacade loggerFacade,
                                        BiFunction<String, String, Boolean> antPathMatcher) {
        this.logConfig = logConfig;
        this.loggerFacade = loggerFacade;
        this.antPathMatcher = antPathMatcher;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        String path = ((HttpServletRequest) request).getRequestURI();
        boolean ignore = logConfig.getIgnorePatterns()
            .stream()
            .anyMatch(pattern -> antPathMatcher.apply(pattern, path));
        if (ignore) {
            chain.doFilter(request, response);
        } else {
            if (logConfig.isRequestLogEnabled()) {
                requestLog(request);
            }
            chain.doFilter(request, response);
            if (logConfig.isResponseLogEnabled()) {
                responseLog(request, response);
            }
        }
    }

    private void requestLog(ServletRequest request) throws IOException {
        if (request instanceof ContentCachingServletRequestWrapper) {
            ContentCachingServletRequestWrapper requestWrapper = (ContentCachingServletRequestWrapper) request;
            RequestLog requestLog = RequestLog.builder()
                .prefix(logConfig.getRequestLogPrefix())
                .suffix(logConfig.getRequestLogSuffix())
                .method(requestWrapper.getMethod())
                .url(buildUrl(requestWrapper))
                .body(buildBody(requestWrapper))
                .headers(buildRequestHeaders(requestWrapper))
                .build();
            loggerFacade.log(requestLog);
        }
    }

    private void responseLog(ServletRequest request, ServletResponse response) throws IOException {
        if (response instanceof ContentCachingServletResponseWrapper) {
            ContentCachingServletResponseWrapper responseWrapper = (ContentCachingServletResponseWrapper) response;
            String content = IOUtils.toString(responseWrapper.getContentAsBytes(), request.getCharacterEncoding());
            ResponseLog responseLog = ResponseLog.builder()
                .prefix(logConfig.getResponseLogPrefix())
                .suffix(logConfig.getResponseLogSuffix())
                .status(responseWrapper.getStatus())
                .url(buildUrl((HttpServletRequest) request))
                .body(content)
                .headers(buildResponseHeaders(response))
                .build();
            loggerFacade.log(responseLog);
        }
    }

    private String buildUrl(HttpServletRequest req) {
        if (req.getQueryString() != null) {
            return req.getRequestURL()
                .append("?")
                .append(req.getQueryString())
                .toString();
        } else {
            return req.getRequestURL().toString();
        }
    }

    private String buildBody(HttpServletRequest request) throws IOException {
        String canonicalName = request.getClass().getCanonicalName();
        // TODO add extension to build body
        if (canonicalName.contains("MultipartHttpServletRequest")) {
            return request.getParameterMap()
                .entrySet()
                .stream()
                .map(entry -> {
                    String value = String.join(",", entry.getValue());
                    return entry.getKey() + "=" + value;
                })
                .collect(Collectors.joining("\n"));
        } else {
            String characterEncoding = request.getCharacterEncoding();
            ServletInputStream stream = request.getInputStream();
            return IOUtils.toString(stream, characterEncoding);
        }
    }

    private List<String> buildRequestHeaders(HttpServletRequest request) {
        List<String> result = new ArrayList<>();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement().toLowerCase();
            if (logConfig.getRequestHeaders().contains(name)) {
                result.add(name + ": " + request.getHeader(name));
            }
        }
        return result;
    }

    private List<String> buildResponseHeaders(ServletResponse response) {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            List<String> matchedResponseHeaders = new ArrayList<>();
            List<String> configuredHeaders = logConfig.getResponseHeaders();
            Collection<String> responseHeaderNames = servletResponse.getHeaderNames();
            if (configuredHeaders.contains("content-type")) {
                matchedResponseHeaders.add("content-type: " + response.getContentType());
            }
            List<String> subMatchedHeaders = responseHeaderNames.stream()
                .filter(header -> configuredHeaders.contains(header.toLowerCase()))
                .map(header -> header + ": " + servletResponse.getHeader(header))
                .collect(Collectors.toList());
            matchedResponseHeaders.addAll(subMatchedHeaders);
            return matchedResponseHeaders;
        } else {
            return Collections.emptyList();
        }
    }

    public interface LoggerFacade {

        void log(RequestLog log);

        void log(ResponseLog log);

    }

}
