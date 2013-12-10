package com.youku.ad.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import com.youku.ad.redis.protocol.RedisDecoderV2;

public abstract class AbstractRedisReply implements RedisReply {

	private  Type type;
	
	public AbstractRedisReply(){
		
	}
	public AbstractRedisReply(Type type){
		this.type = type;
	}
	public void writeCRLF(ChannelBuffer buffer){
		buffer.writeByte(RedisDecoderV2.CR_BYTE);
		buffer.writeByte(RedisDecoderV2.LF_BYTE);
	}
	
	public enum Type{
		ERROR((byte)'-'),
		STATUS((byte)'+'),
		BULK((byte)'$');
		private byte code;
		private Type(byte code){
			this.code = code;
		}
		public byte getCode(){
			return code;
		}
	}
	
	public void writeStart(ChannelBuffer buffer){
		buffer.writeByte(type.getCode());
	}
	
public ChannelBuffer encode() {
		
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		writeStart(buffer);
	    doEncode(buffer);
		writeCRLF(buffer);
		return buffer;
	}
	
	public abstract void  doEncode(ChannelBuffer buffer);
}
