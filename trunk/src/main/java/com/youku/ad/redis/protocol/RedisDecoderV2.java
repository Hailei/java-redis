package com.youku.ad.redis.protocol;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import com.youku.ad.redis.command.RequestCommand;
import com.youku.ad.redis.command.ShutDownCommand;


public class RedisDecoderV2 extends ReplayingDecoder<RedisDecoderV2.State> {

	public static final char DOLLAR_BYTE = '$';
    public static final char ASTERISK_BYTE = '*';
    public static final char CR_BYTE = '\r';
    public static final char LF_BYTE = '\n';
    private RequestCommand requestCommand;
    
    public  RedisDecoderV2(){
    	super(State.READ_SKIP);
    }
	protected enum State {
		READ_SKIP,//健壮性考虑，如果第一个字符不是*则skip直到遇到*
		READ_INIT, 
		READ_ARGC, 
		READ_ARG,
		READ_END
	}
	
	private static void skipChar(ChannelBuffer buffer){
		
		for(;;){
		    char ch = (char)buffer.readByte();
		    if(ch == ASTERISK_BYTE){
		    	buffer.readerIndex(buffer.readerIndex() - 1);
		    	break;
		    }
		}
	}
	
	private static int readInt(ChannelBuffer buffer){
		StringBuilder sb = new StringBuilder();
		char ch = (char)buffer.readByte();
		while(ch != CR_BYTE){//TODO 或许需要做一些 判断例如长度判断防止死循环
			sb.append(ch);
			ch = (char)buffer.readByte();
		}
		buffer.readByte();//TODO  是否需要增加对LF的判断
		int result = Integer.parseInt(sb.toString());//TODO  转型安全
		return result;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer, State state) throws Exception {
		switch (state) {
		case READ_SKIP:{
			try{
				skipChar(buffer);
				checkpoint(State.READ_INIT);
			}finally{
				checkpoint();
			}
		}
		case READ_INIT: {
               char ch = (char)buffer.readByte();
               if(ch == ASTERISK_BYTE){
            	   ch = (char)buffer.readByte();
            	   if(ch == CR_BYTE){//此处为shutdown
                        buffer.readByte();
                        checkpoint(State.READ_SKIP);
                        return new ShutDownCommand();
            	   }else{
            		   buffer.readerIndex(buffer.readerIndex() - 1);//重新设回原来的index
            		   requestCommand = new RequestCommand();
            		   checkpoint(State.READ_ARGC);
            	   }
               }
		}
		case READ_ARGC:{
			requestCommand.setArgCount(readInt(buffer));
			checkpoint(State.READ_ARG);
		}
		case READ_ARG:{
			List<byte[]> args = new ArrayList<byte[]>(requestCommand.getArgCount());
			while(args.size() < requestCommand.getArgCount()){
				buffer.readByte();//TODO  首先读出$ 这里可以做协议的校验
				int length = readInt(buffer);
				byte[] argByte = new byte[length];
				buffer.readBytes(argByte);
				buffer.skipBytes(2);//skip \r\n
				args.add(argByte);
			}
			requestCommand.setArgs(args);
			checkpoint(State.READ_END);
		}
		case READ_END:{
			RequestCommand rCommand = this.requestCommand;
			this.requestCommand = null;
			checkpoint(State.READ_INIT);
			return rCommand;
		}
		default:
			throw new Error("can't  reach there!");
		}
	}
}
