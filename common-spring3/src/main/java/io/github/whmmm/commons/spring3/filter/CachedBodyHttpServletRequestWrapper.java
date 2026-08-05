package io.github.whmmm.commons.spring3.filter;


import cn.hutool.core.io.IoUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;


import java.io.*;
import java.nio.charset.StandardCharsets;

class CachedBodyHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public CachedBodyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        // 读取请求体并缓存
        InputStream inputStream = request.getInputStream();
        this.body = IoUtil.readBytes(inputStream);  // 缓存请求体
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 返回缓存的请求体的 BufferedReader
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.body)));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 返回缓存的请求体的 InputStream
        return new CachedServletInputStream(this.body);
    }

    public String getBodyAsString() {
        // 将缓存的请求体转换为字符串
        return new String(this.body, StandardCharsets.UTF_8);
    }


    // 将缓存字节数组包装成 ServletInputStream
    private static class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream byteArrayInputStream;

        public CachedServletInputStream(byte[] body) {
            this.byteArrayInputStream = new ByteArrayInputStream(body);
        }

        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }


        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Async read is not supported");
        }
    }
}