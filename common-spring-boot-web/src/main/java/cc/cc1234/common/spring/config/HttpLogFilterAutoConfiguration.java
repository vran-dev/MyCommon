package cc.cc1234.common.spring.config;

import cc.cc1234.common.servlet.filter.cache.ContentCachingFilter;
import cc.cc1234.common.servlet.filter.log.HttpRequestResponseLogFilter;
import cc.cc1234.common.servlet.filter.log.LogConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.support.MultipartFilter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static cc.cc1234.common.servlet.filter.log.HttpRequestResponseLogFilter.LoggerFacade;

@Configuration
@ConditionalOnClass(HttpRequestResponseLogFilter.class)
@EnableConfigurationProperties(HttpLogFilterAutoConfiguration.HttpRequestResponseLogConfig.class)
public class HttpLogFilterAutoConfiguration {

    @Data
    @ConfigurationProperties(prefix = HttpRequestResponseLogConfig.PREFIX)
    public static class HttpRequestResponseLogConfig {

        public static final String PREFIX = "common.spring.web.log";

        private String requestLogPrefix = "\n--------------------- request start ---------------------------->";

        private String requestLogSuffix = "<----------------------------------------------------------------";

        private String responseLogPrefix = "\n--------------------- request end ---------------------------->";

        private String responseLogSuffix = "<----------------------------------------------------------------";

        private List<String> ignorePatterns = Collections.emptyList();

        private List<String> requestHeaders = Collections.singletonList("Content-Type");

        private List<String> responseHeaders = Collections.emptyList();

        private boolean responseLogEnabled = true;

        private boolean requestLogEnabled = true;

        public LogConfig toLogConfig() {
            LogConfig config = new LogConfig();
            config.setRequestLogPrefix(requestLogPrefix);
            config.setRequestLogSuffix(requestLogSuffix);
            config.setResponseLogPrefix(responseLogPrefix);
            config.setResponseLogSuffix(responseLogSuffix);
            config.setIgnorePatterns(ignorePatterns);
            config.setRequestHeaders(requestHeaders);
            config.setResponseHeaders(responseHeaders);
            config.setResponseLogEnabled(responseLogEnabled);
            config.setRequestLogEnabled(requestLogEnabled);
            return config;
        }
    }

    @Bean
    FilterRegistrationBean<MultipartFilter> multipartFilterRegistrationBean(Optional<MultipartFilter> multipartFilter) {
        MultipartFilter filter = multipartFilter.orElseGet(MultipartFilter::new);
        FilterRegistrationBean<MultipartFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName(MultipartFilter.class.getSimpleName());
        bean.setOrder(1);
        return bean;
    }

    @Bean
    FilterRegistrationBean<ContentCachingFilter> contentCachingFilterRegistrationBean() {
        FilterRegistrationBean<ContentCachingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ContentCachingFilter());
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName(ContentCachingFilter.class.getSimpleName());
        bean.setOrder(2);
        return bean;
    }

    @Bean
    FilterRegistrationBean<HttpRequestResponseLogFilter> httpRequestResponseLogFilterRegistrationBean(
        @Autowired(required = false) LoggerFacade loggerFacade,
        HttpRequestResponseLogConfig httpRequestResponseLogConfig) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        FilterRegistrationBean<HttpRequestResponseLogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HttpRequestResponseLogFilter(
            httpRequestResponseLogConfig.toLogConfig(),
            loggerFacade,
            antPathMatcher::match));
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName(HttpRequestResponseLogFilter.class.getSimpleName());
        bean.setOrder(2);
        return bean;
    }
}
