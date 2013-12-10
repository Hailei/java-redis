package com.youku.ad.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public class StatusReply extends AbstractRedisReply {

	public static final StatusReply OK = new StatusReply("OK");
	private String message;
	
	public StatusReply(String message){
		super(Type.STATUS);
		this.message = message;
	}

	public void doEncode(ChannelBuffer buffer) {
		buffer.writeBytes(message.getBytes());
	}
	
	public static void main(String[] args){
		
		StatusReply reply = new StatusReply("OK");
		System.out.println(reply instanceof RedisReply);
	}
	
	
}
