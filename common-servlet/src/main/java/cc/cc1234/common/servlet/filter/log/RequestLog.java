package cc.cc1234.common.servlet.filter.log;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class RequestLog {

    @Builder.Default
    private String prefix = "\n--------------------- request start ---------------------------->";

    @Builder.Default
    private String suffix = "<----------------------------------------------------------------";

    private String method;

    private String url;

    private String body;

    @Singular
    private List<String> headers;

    public String toString() {
        String line = "\n";

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(prefix).append(line)
                .append(method).append(" ").append(url).append(line);
        if (!headers.isEmpty()) {
            String headersStr = String.join(line, headers);
            logBuilder.append(line).append(headersStr).append(line);
        }

        if (body != null && body.length() > 0) {
            logBuilder.append(line).append(body).append(line);
        }
        logBuilder.append(suffix);
        return logBuilder.toString();
    }
}