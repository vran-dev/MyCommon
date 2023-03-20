package cc.cc1234.common.servlet.filter.log;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LogConfig {

    private String requestLogPrefix = "\n--------------------- request start ---------------------------->";

    private String requestLogSuffix = "<----------------------------------------------------------------";

    private String responseLogPrefix = "\n--------------------- request end ---------------------------->";

    private String responseLogSuffix = "<----------------------------------------------------------------";

    private List<String> ignorePatterns = Collections.emptyList();

    private List<String> requestHeaders = Collections.singletonList("Content-Type");

    private List<String> responseHeaders = Collections.emptyList();

    private boolean responseLogEnabled = true;

    private boolean requestLogEnabled = true;

    public List<String> getRequestHeaders() {
        return requestHeaders.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    public List<String> getResponseHeaders() {
        return responseHeaders.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}