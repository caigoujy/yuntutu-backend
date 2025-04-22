package com.jyniubi.picture.backend.controller;

import com.jyniubi.picture.backend.common.BaseResponse;
import com.jyniubi.picture.backend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;


@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        File file = new File("D:\123");
        if (!file.exists()){
            if (file.mkdir()){
                System.out.println();
            }else {
                System.out.println();
            }
        }
        System.out.println();
        return ResultUtils.success("ok");
    }
}
