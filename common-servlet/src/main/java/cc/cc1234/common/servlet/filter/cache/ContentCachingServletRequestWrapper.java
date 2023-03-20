package cc.cc1234.common.servlet.filter.cache;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ContentCachingServletRequestWrapper extends HttpServletRequestWrapper {

    private ByteArrayOutputStream contentCachingOutputStream;

    public ContentCachingServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        contentCachingOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), contentCachingOutputStream);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedServletInputStream(contentCachingOutputStream.toByteArray());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentCachingOutputStream.toByteArray());
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    static class CachedServletInputStream extends ServletInputStream {

        private ByteArrayInputStream inputStream;

        private boolean finished = false;

        public CachedServletInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public boolean isReady() {
            return inputStream.available() > 0;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            int b = inputStream.read();
            if (b == -1) {
                finished = true;
            }
            return b;
        }
    }
}
