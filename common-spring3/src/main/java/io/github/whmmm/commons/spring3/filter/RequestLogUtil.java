package io.github.whmmm.commons.spring3.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.url.UrlBuilder;
import io.github.whmmm.commons.requestlog.RequestLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public final class RequestLogUtil implements Serializable {
    public static String MDC_TRACE_ID = "traceId";

    public static ThreadLocal<RequestLog> REQUEST_LOG = new ThreadLocal<>();

    @Nonnull
    public static String getTraceId() {
        String traceId = MDC.get(MDC_TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                traceId = request.getHeader(RequestLog.REQUEST_TRACE_ID);
                if (traceId == null || traceId.isEmpty()) {
                    Object attribute = request.getAttribute(MDC_TRACE_ID);
                    if (attribute != null) {
                        traceId = attribute.toString();
                    }
                }
            }
        }
        return traceId == null ? "" : traceId;
    }

    public static void setTraceId(String traceId) {
        MDC.put(MDC_TRACE_ID, traceId);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            request.setAttribute(MDC_TRACE_ID, traceId);
        }
    }

    public static RequestLog buildRequestLog(HttpServletRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();


        String traceId = getTraceId();
        Map<String, Object> requestHeaders = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            requestHeaders.put(headerName, request.getHeader(headerName));
        }
        // 构建 params 参数
        UrlBuilder urlBuilder = UrlBuilder.of();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            urlBuilder.addQuery(parameterName, request.getParameter(parameterName));
        }

        byte[] bodyBytes = null;
        try {
            bodyBytes = IoUtil.readBytes(request.getInputStream());
        } catch (IOException e) {
            log.error("read request body error:" + e.getMessage(), e);
        }

        // 初始化 RequestLog
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(traceId);
        requestLog.setHeaders(requestHeaders);
        requestLog.setStartAt(new Date());
        requestLog.setUrl(request.getRequestURI());
        requestLog.setType(request.getMethod());
        requestLog.setParam(urlBuilder.getQueryStr());
        requestLog.setContentType(request.getContentType());
        if (bodyBytes != null) {
            requestLog.setBody(new String(bodyBytes, StandardCharsets.UTF_8));
        }


        REQUEST_LOG.set(requestLog);

        return requestLog;
    }

    public static void removeTraceId() {
        MDC.remove(MDC_TRACE_ID);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            request.removeAttribute(MDC_TRACE_ID);
        }
        REQUEST_LOG.remove();
    }
}
