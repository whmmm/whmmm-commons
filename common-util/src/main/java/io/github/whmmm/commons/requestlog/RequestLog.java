package io.github.whmmm.commons.requestlog;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * <p><b> ----------------------- </b></p>
 * <p><b> author: whmmm           </b></p>
 * <p><b> date  : 2023/2/13 12:48 </b></p>
 *
 * @author whmmm
 */
@Data
@Slf4j
public final class RequestLog {

    public static String REQUEST_TRACE_ID = "x-request-trace-Id";


    private static final String lineSeparator = System.lineSeparator();

    /**
     * 请求开始时间
     * <p><b> author: whmmm </b></p>
     * <p><b> date  : 2024-11-08 11:27 </b></p>
     */
    @Nullable
    private Date startAt = new Date();

    /**
     * 请求结束时间
     * <p><b> author: whmmm </b></p>
     * <p><b> date  : 2024-11-08 11:27 </b></p>
     */
    @Nullable
    private Date endAt;

    private String requestId;

    private String url;

    /**
     * GET | POST
     */
    private String type;

    /**
     * {@code} PostForm 或者 get 时 的参数
     */
    private String param;

    /**
     * {@code post json} 时的参数
     * <br/>
     * 中绑定的对象, <br/>
     * <span color=red>这里的值不一定等于前端传递的值. </span>
     * <br/>
     */
    @Nullable
    private Object body;
    /**
     * 是否限制打印日志, 默认为 false
     */
    private boolean logLimitUsable = false;
    /**
     * 限制打印日志的长度
     */
    private int maxBodyLen = 6000;

    @Nullable
    private Map<String, Object> headers;

    /**
     * 响应值
     */
    @Nullable
    private String result;

    public String dumpToLogStr(StringBuilder sb) {
        sb.append(lineSeparator);
        sb.append("=================================================================")
                .append(lineSeparator);
        sb.append("### ").append(REQUEST_TRACE_ID + ":").append(requestId);
        sb.append(" -- http log --").append(lineSeparator);
        sb.append(type).append("  ").append(url);
        // 请求头
        sb.append(lineSeparator);

        Map<String, Object> tempHeaders = this.getHeaders();
        if (tempHeaders == null) {
            tempHeaders = Collections.emptyMap();
        }

        for (Map.Entry<String, Object> entry : tempHeaders.entrySet()) {
            Object v = entry.getValue();
            if (v == null) {
                v = "";
            }
            sb.append(entry.getKey()).append(": ").append(v);
            sb.append(lineSeparator);
        }

        if (body == null) {
            sb.append(lineSeparator);
            sb.append(param);
        } else {
            sb.append(lineSeparator);
            String str = null;
            if (body instanceof String) {
                str = body.toString();
            } else {
                str = JSONUtil.toJsonStr(body);
            }

            if (this.logLimitUsable && str.length() >= maxBodyLen) {
                // 最多打印 800 个字符, 太多会影响性能！！
                str = str.substring(0, maxBodyLen) +
                        "......(超出限制:" + maxBodyLen + ")";
            }
            sb.append(str);
        }
        sb.append(lineSeparator);
        if (result != null) {
            sb.append(lineSeparator);
            if (this.logLimitUsable && result.length() >= maxBodyLen) {
                // 最多打印 800 个字符, 太多会影响性能！！
                result = result.substring(0, maxBodyLen) +
                        "......(超出限制:" + maxBodyLen + ")";
            }
            sb.append("响应的结果为: ").append(result);
            sb.append(lineSeparator);
        }
        sb.append("=================================================================");

        return sb.toString();
    }


    public String dumpToLogStr() {
        return this.dumpToLogStr(new StringBuilder());
    }
}
