package com.xust.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author: Victor
 * @create: 2020-02-11 16:41
 * 自定义一个Handler需要继承Netty规定的HandlerAdapter才能称为一个Handler
 **/

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     *
     * @param ctx 上下文信息
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {


        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 5);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello -a2",CharsetUtil.UTF_8));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 10);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello -a3",CharsetUtil.UTF_8));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 5);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello -a4", CharsetUtil.UTF_8));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 5, TimeUnit.SECONDS);



//        System.out.println("server ctx = " + ctx) ;
//        ByteBuf buf = (ByteBuf)msg;
//        System.out.println("客户端发送信息是: " +  buf.toString(CharsetUtil.UTF_8));
//        System.out.println("客户端地址: " + ctx.channel().remoteAddress());
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
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello -a1", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
