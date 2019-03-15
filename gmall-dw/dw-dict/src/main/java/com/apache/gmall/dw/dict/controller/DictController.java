package com.apache.gmall.dw.dict.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
public class DictController {
     @GetMapping("dict")
    public String dict(HttpServletResponse response) {
         //从数据库中读取当前维护的自定义分词
         String dict = "大帅哥\n流浪地球";
response.addHeader("Last-Modified",new Date().toString());
         return dict;
     }
}
