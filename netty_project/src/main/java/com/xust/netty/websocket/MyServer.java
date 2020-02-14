package com.xust.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * @description
 * @author: Victor
 * @create: 2020-02-13 17:32
 **/
public class MyServer {
    public static void main(String[] args) {
    EventLoopGroup boosGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try{
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.ERROR))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //基于http 协议，所以需要http的编码和编码器
                        pipeline.addLast(new HttpServerCodec());
                        //是以块方式写，添加ChunkedWriteHandler处理器
                        pipeline.addLast(new ChunkedWriteHandler());
                        /**
                         * http数据在传输过程中是分段的，HttpObjectAggregator可以将多个段聚合
                         */
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        /**
                         * 对应websocket，是以帧的形式传递,表示请求的uri ws://localhost:8888/hello
                         * WebSocketServerProtocolHandler是将http协议升级为websocket协议，保持长连接
                         *
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                        pipeline.addLast(new MyTextWebSocketFrameHandler());

                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
        channelFuture.channel().closeFuture().sync();
    }catch (Exception e) {
        e.printStackTrace();
    }finally {
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
}
