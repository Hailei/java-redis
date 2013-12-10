package com.youku.ad.redis.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author Hailei
 *
 */
public class RequestCommand implements RedisCommand {

	private int argCount;
	//private List<String> args;
	private List<byte[]> args;
	
	public RequestCommand(){
		
	}

	public List<byte[]> getArgs() {
		return args;
	}

	public void setArgs(List<byte[]> args) {
		this.args = args;
	}

	public int getArgCount() {
		return argCount;
	}
	
	public void init(int argCount){
		//TODO 边缘判断
		setArgCount(argCount);
		this.args = new ArrayList<byte[]>(argCount);
	}

	public void setArgCount(int argCount) {
		this.argCount = argCount;
	}
	
	public boolean needMoreArg(){
		return this.args.size() < this.argCount;
	}
	
	public void addArg(byte[] arg){
		args.add(arg);
	}

	@Override
	public String toString() {
		return "RequestCommand [argCount=" + argCount + ", args=" + args + "]";
	}
	
}
