package com.github.hesimin.springbootwebsocket.socket;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.LongAdder;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * <p>
 *     js测试工具： http://jsbin.com/wikifeqiza/edit?js,console
 * </p>
 *
 * var ws = new WebSocket("ws://localhost:8080/websocket");
 * ws.onopen = function(evt) {
 * console.log("Connection open ...");
 * ws.send("Hello WebSockets!");
 * };
 * ws.onmessage = function(evt) {
 * console.log( "Received Message: " + evt.data);
 * //ws.close();
 * };
 * ws.onclose = function(evt) {
 * console.log("Connection closed.");
 * };
 *
 * <p>
 * <p>
 * 另外可以使用更高级协议：STOMP
 * <p>
 * {@link WebSocketMessageBrokerConfigurer}
 * and @MessageMapping 、 @SendTo
 */
//@ServerEndpoint("/websocket/{token}")
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    //静态变量，用来记录当前在线连接数
    private static LongAdder onlineCount = new LongAdder();
    // 可以换成 Map 以用户id做 key（通过建立连接时的token进行转换），就可以针对指定用户推送消息
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        log.info("有新连接加入！当前在线人数为" + getOnlineCount());
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("websocket IO异常");
        }
    }
    //	//连接打开时执行
    //	@OnOpen
    //	public void onOpen(@PathParam("token") String token, Session session) {
    //		System.out.println("Connected ... " + session.getId());
    //	}

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息:" + message);

        //群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }


    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) {
        log.info(message);
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static int getOnlineCount() {
        return onlineCount.intValue();
    }

    public static void addOnlineCount() {
        WebSocketServer.onlineCount.increment();
    }

    public static void subOnlineCount() {
        WebSocketServer.onlineCount.decrement();
    }
}
