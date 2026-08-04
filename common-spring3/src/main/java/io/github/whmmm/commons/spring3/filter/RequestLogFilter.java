package io.github.whmmm.commons.spring3.filter;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


import jakarta.servlet.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Configuration
public class RequestLogFilter extends OncePerRequestFilter {


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

            this.doFilter(request, response, chain);
        } finally {
            RequestLogUtil.removeTraceId();
        }
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RepeatReadRequestWrapperFilter repeatReadRequestWrapperFilter() {
        RepeatReadRequestWrapperFilter filter = new RepeatReadRequestWrapperFilter();
        return filter;
    }
}
