package com.android.hvhelper;

public class NoSuchNodeException extends Exception {
	public NoSuchNodeException(String msg){
		super(msg);
	}
	
	public NoSuchNodeException(){
		super();
	}
}
