package com.xust.netty.codec;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author: Victor
 * @create: 2020-02-11 16:41
 * 自定义一个Handler需要继承Netty规定的HandlerAdapter才能称为一个Handler
 **/

public class NettyServerHandler extends SimpleChannelInboundHandler<StudentPOJO.Student> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StudentPOJO.Student msg) throws Exception {
        System.out.println(msg.toString() + "Read0");
    }

    /**
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Thread: " + Thread.currentThread().getName());
        //需要对数据进行编码
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
