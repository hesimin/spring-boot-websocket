package com.github.hesimin.springbootwebsocket.controller;

import com.github.hesimin.springbootwebsocket.socket.WebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("push")
    public String push(String msg) {
        WebSocketServer.sendInfo(msg);
        return "ok";
    }
}
