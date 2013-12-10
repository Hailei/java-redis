package com.youku.ad.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public interface RedisReply {

	public ChannelBuffer encode();
}
