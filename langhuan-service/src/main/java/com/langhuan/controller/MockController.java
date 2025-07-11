package com.langhuan.controller;


import com.langhuan.common.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockController {

    @RequestMapping("/mock")
    public Result mock(
            @RequestBody Object data
    ) {
        return Result.success(data);
    }
}
