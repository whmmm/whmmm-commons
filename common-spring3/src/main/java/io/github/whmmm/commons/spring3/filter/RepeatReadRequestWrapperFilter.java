package io.github.whmmm.commons.spring3.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 重复读取请求数据过滤器
 * <p><b> author: whmmm </b></p>
 * <p><b> date  : 2024-11-07 9:10 </b></p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RepeatReadRequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String contentType = request.getContentType();
        // 仅处理 application/json 的请求
        if (contentType == null ||
            !contentType.toLowerCase().contains("application/json")) {
            chain.doFilter(request, response);
            return;
        }


        HttpServletRequest wrappedRequest = new CachedBodyHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
