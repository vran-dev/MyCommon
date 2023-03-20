package cc.cc1234.common.servlet.filter.cache;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ContentCachingServletResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream contentCachingOutputStream;

    public ContentCachingServletResponseWrapper(HttpServletResponse response) {
        super(response);
        contentCachingOutputStream = new ByteArrayOutputStream(2048);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new CachedServletOutputStream(super.getOutputStream(), contentCachingOutputStream);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getOutputStream());
    }

    public byte[] getContentAsBytes() {
        return contentCachingOutputStream.toByteArray();
    }

    static class CachedServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream origin;

        private final ByteArrayOutputStream cacheOutputStream;

        public CachedServletOutputStream(ServletOutputStream origin,
                                         ByteArrayOutputStream cacheOutputStream) {
            this.origin = origin;
            this.cacheOutputStream = cacheOutputStream;
        }

        @Override
        public boolean isReady() {
            return origin.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            origin.setWriteListener(listener);
        }

        @Override
        public void write(int b) throws IOException {
            origin.write(b);
            cacheOutputStream.write(b);
        }
    }
}
