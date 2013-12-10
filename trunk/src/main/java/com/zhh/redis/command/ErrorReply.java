package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public class ErrorReply extends  AbstractRedisReply {

	private Error error;
	private String message;
	public static final ErrorReply NOTIMPLEMENT = new ErrorReply(Error.ERR,"only get set del others don't had been implemented");
	public enum Error{
		ERR(ProtoUtil.ERROR_ERR_BULK),
		WRONGTYPE(ProtoUtil.WRONGTYPE_ERR_BULK),
		NOTIMPLEMENT(ProtoUtil.NOIMPLEMENT_ERR_BULK);
		private byte[] message;
		private Error(byte[] message){
			this.message = message;
		}
		
		public byte[] getMessage(){
		  return message;
		}
		
	}
	
	public ErrorReply(Error error,String message){
		super(Type.ERROR);
		this.error = error;
		this.message = message;
	}
	
	@Override
	public void doEncode(ChannelBuffer buffer) {
		buffer.writeBytes(this.error.getMessage());
		buffer.writeByte(' ');
		buffer.writeBytes(this.message.getBytes());
	}
	
}
