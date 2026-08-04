package io.github.whmmm.commons.spring.filter;

import cn.hutool.core.util.RandomUtil;
import io.github.whmmm.commons.servlet.RepeatReadRequestWrapperFilter;
import io.github.whmmm.commons.servlet.RequestLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
