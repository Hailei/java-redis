package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public class BulkReply extends AbstractRedisReply {

	private byte[] value;
	
	public BulkReply(byte[] value){
		super(Type.BULK);
		this.value = value;
	}

	public void doEncode(ChannelBuffer buffer) {
		
		buffer.writeBytes(ProtoUtil.convertIntToByteArray(value.length));
		writeCRLF(buffer);
		buffer.writeBytes(value);
	}
	
	
}
