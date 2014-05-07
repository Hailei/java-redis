/**
 * 
 */
package com.zhh.redis.server;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhh.redis.command.BulkReply;
import com.zhh.redis.command.ErrorReply;
import com.zhh.redis.command.RequestCommand;
import com.zhh.redis.command.ShutDownCommand;
import com.zhh.redis.command.StatusReply;



public class ServerHandler extends SimpleChannelHandler {
    public static enum ENGINE_NAME{
        HASHMAP,
        SYNCMAP,
        CONCURRENTMAP,
        RBTREE
    }
    private String engine;
    private Random random = new Random();
	static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
    private Map<String,byte[]>  db = new HashMap<String, byte[]>();
    private List<String>  keys = new ArrayList<String>();
	public ServerHandler(String engine){
	    this.engine = engine;
	    if(engine.equals(ENGINE_NAME.HASHMAP.name())){
	        db = new HashMap<String, byte[]>();
	    }else if(engine.equals(ENGINE_NAME.SYNCMAP.name())){
	        db = Collections.synchronizedMap(db);
	    }else if(engine.equals(ENGINE_NAME.CONCURRENTMAP.name())){
	        db = new ConcurrentHashMap<String, byte[]>();
	    }
	}
	
	public static void main(String[] args){
	    System.out.println(ENGINE_NAME.SYNCMAP.name());
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		//命令分发
		//TODO  分发的逻辑可以抽取出来以便使用多种transport
		Object command = e.getMessage();
	//	System.out.println(command);
		if(command instanceof ShutDownCommand){
			ctx.getChannel().close();
		}else{
			RequestCommand request = (RequestCommand)command;
			String type = new String(request.getArgs().get(0));
			if("get".equalsIgnoreCase(type)){
				//TODO  判断out of length
				byte[] value = db.get(new String(request.getArgs().get(1)));
				//找不到 返回-1  协议规范
				if(value == null){
					value = "-1".getBytes();
				}
				BulkReply reply = new BulkReply(value);
				ctx.getChannel().write(reply);
			}else if("set".equalsIgnoreCase(type)){
				try{
				    String key = new String(request.getArgs().get(1));
                    db.put(key, request.getArgs().get(2));
                    //此处不是线程安全的，但收集keys只为randomkey使用。
                    keys.add(key);
                    ctx.getChannel().write(StatusReply.OK);
				}catch(Exception ex){
					//TODO  管理Error Reply
					e.getChannel().write(new ErrorReply(ErrorReply.Error.ERR, ex.getMessage()));
				}
			}else if("randomkey".equalsIgnoreCase(type)){
                String key = keys.get(random.nextInt(keys.size()));
                BulkReply reply = new BulkReply(key.getBytes());
                ctx.getChannel().write(reply);
            }
			else if("info".equalsIgnoreCase(type)){
			   String info = "java redis kv stored by " + engine + "\n";
			   info = info + "key count:" + db.size() + "\n";
			   BulkReply reply = new BulkReply(info.getBytes());
               ctx.getChannel().write(reply);
			}else{
				//其他command没有实现，给用户提示ERROR
				e.getChannel().write(ErrorReply.NOTIMPLEMENT);
			}
		}
		
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		//TODO  管理channel 例如心跳 以及client list命令
		LOGGER.info(ctx.getChannel() + "had closed!");
		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Throwable cause = e.getCause();

		if (cause instanceof IOException) {
			String message = cause.getMessage();
			if (message != null && "Connection reset by peer".equals(message)) {
				LOGGER.warn("Client closed!",cause);
			} else {
				LOGGER.error("出错,关闭连接", cause);
			}
			e.getChannel().close();
		} else {
			LOGGER.error("出错", cause);
			e.getChannel().write(new ErrorReply(ErrorReply.Error.ERR, cause.getMessage()));
		}
	}

}
