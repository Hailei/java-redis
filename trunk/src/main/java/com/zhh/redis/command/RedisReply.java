package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public interface RedisReply {

	public ChannelBuffer encode();
}
