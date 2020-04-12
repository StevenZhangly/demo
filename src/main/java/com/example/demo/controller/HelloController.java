package com.example.demo.controller;

import com.example.hello.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private HelloService helloService;

    @GetMapping(value="/hello")
    public String sayHello(){
        return helloService.sayHello();
    }
}
