package com.xust.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Victor
 * @create: 2020-02-12 21:51
 **/

public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    //全局的时间执行器
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
		//会将所有的channel遍历
		channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "加入聊天 " + sdf.format(new Date()) + "\n");
        channelGroup.add(channel);
    }

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx ) throws Exception {
		Channel channel = ctx.channel();
		channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "离开聊天 " + sdf.format(new Date()) + "\n");
		
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress() + "上线了");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress() + "下线了");
	}

	@Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		Channel channel = ctx.channel();
		channelGroup.forEach( ch -> {
			if(channel != ch) {
				//转发消息
				ch.writeAndFlush("[客户]" + channel.remoteAddress() + " 发送了消息" + msg + "\n");
			}else {
				ch.writeAndFlush("[自己]发送了消息" + msg + "\n");
			}
		});
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	ctx.close();
	}

}
