package com.tcray.rayrpc.controller;

import com.tcray.rayrpc.api.UserService;
import com.tcray.rayrpc.core.annotation.EnableReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lirui
 */
@RestController
public class ConsumerController {

    @EnableReference
    private UserService userService;

    @RequestMapping("/hello")
    public String sayHello(String name) {
        return userService.sayHello(name);
    }

}
