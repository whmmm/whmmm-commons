package io.github.whmmm.springboottest.controller;


import cn.hutool.json.JSONUtil;
import io.github.whmmm.springboottest.domain.dto.TestForm;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/test-get")
    public String testGet(TestForm form) {
        return "test-get:\n" + JSONUtil.toJsonPrettyStr(form);
    }

    @PostMapping("/test-post")
    public String testPost(TestForm form) {
        return "test-post:\n" + JSONUtil.toJsonPrettyStr(form);
    }

    @PostMapping("/test-post-json")
    public String testPostJson(@RequestBody TestForm form) {
        return "test-post-json:\n" + JSONUtil.toJsonPrettyStr(form);
    }
}
