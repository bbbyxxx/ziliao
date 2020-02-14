package com.xust.netty.codec2;

import com.xust.netty.codec.StudentPOJO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: Victor
 * @create: 2020-02-11 16:41
 * 自定义一个Handler需要继承Netty规定的HandlerAdapter才能称为一个Handler
 **/

public class NettyServerHandler extends SimpleChannelInboundHandler<MyDataInfo.MyMessage> {



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyDataInfo.MyMessage msg) throws Exception {
         MyDataInfo.MyMessage.DataType dataType = msg.getDataType();
         if (dataType == MyDataInfo.MyMessage.DataType.StudentType) {
             System.out.println(msg.getStudent().toString());
             System.out.println("student");
         }else if (dataType == MyDataInfo.MyMessage.DataType.WorkerType) {
             System.out.println(msg.getWorker().toString());
             System.out.println("worker");
         }else {
             System.out.println("传输类型不正确");
         }
    }
}
