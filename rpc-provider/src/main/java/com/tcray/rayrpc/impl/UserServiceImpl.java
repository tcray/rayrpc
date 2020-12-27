package com.tcray.rayrpc.impl;

import com.tcray.rayrpc.api.UserService;
import com.tcray.rayrpc.core.annotation.EnableProvider;
import org.springframework.stereotype.Component;

/**
 * @author lirui
 */
@Component
@EnableProvider
public class UserServiceImpl implements UserService {

    @Override
    public String sayHello(String name) {
        return "hello ," + name;
    }

}
