package com.tcray.rayrpc.domain;

import lombok.Data;

/**
 * @author lirui
 */
@Data
public class User {

    private String name ;

    private String age ;

    public User() {
    }

    public User(String name, String age) {
        this.name = name;
        this.age = age;
    }

}
