package io.github.whmmm.commons.spring2.filter;

import cn.hutool.core.util.RandomUtil;
import io.github.whmmm.commons.requestlog.RequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
class RequestLogFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String traceId = RequestLogUtil.getTraceId();
            if (traceId.isEmpty()) {
                RequestLogUtil.setTraceId(
                        String.format("t%s", RandomUtil.randomString(6))
                );
            }
            RequestLog requestLog = RequestLogUtil.buildRequestLog(request);
            log.info(requestLog.dumpToLogStr());

            this.doFilter(request, response, chain);
        } finally {
            RequestLogUtil.removeTraceId();
        }
    }
}
