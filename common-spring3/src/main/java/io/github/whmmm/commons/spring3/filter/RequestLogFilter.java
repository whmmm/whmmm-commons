package io.github.whmmm.commons.spring3.filter;

import cn.hutool.core.util.RandomUtil;
import io.github.whmmm.commons.requestlog.RequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nullable;
import java.io.IOException;

@Slf4j
@Component
class RequestLogFilter extends OncePerRequestFilter {

    @Configuration
    public static class RequestLogInterceptor implements HandlerInterceptor, WebMvcConfigurer {
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
            System.out.println("xx");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            RequestLogInterceptor interceptor = new RequestLogInterceptor();
            registry.addInterceptor(interceptor);
        }
    }


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
