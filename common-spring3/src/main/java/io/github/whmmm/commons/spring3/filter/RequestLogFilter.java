package io.github.whmmm.commons.spring3.filter;

import cn.hutool.core.util.RandomUtil;
import io.github.whmmm.commons.requestlog.RequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Nullable;
import java.io.IOException;

@Slf4j
@Component
class RequestLogFilter extends OncePerRequestFilter {

    @RestControllerAdvice
    public static class RequestLogRestAdvice implements ResponseBodyAdvice<Object> {
        @Override
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            return true;
        }

        @Nullable
        @Override
        public Object beforeBodyWrite(@Nullable Object body,
                                      MethodParameter returnType,
                                      MediaType selectedContentType,
                                      Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                      ServerHttpRequest request, ServerHttpResponse response) {
            if (body instanceof String) {
                RequestLog requestLog = RequestLogUtil.REQUEST_LOG.get();
                if (requestLog != null) {
                    requestLog.setResult((String) body);
                }
            }
            return body;
        }
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            RequestLogUtil.initTraceIdIfAbsent();
            RequestLog requestLog = RequestLogUtil.buildRequestLog(request);
            log.info(requestLog.dumpToLogStr());

            this.doFilter(request, response, chain);
        } finally {
            RequestLogUtil.dumpResultAndClear();
        }
    }
}
