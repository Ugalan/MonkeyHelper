package com.android.hvhelper;

/**
 * @author Ugalan
 * ViewNodeÀàµÄ×Ö¶Î
 */
public enum F {
	name("name");
	
	public final String value;
	private F(String value){
		this.value = value;
	}
	
	public String value(){
		return this.value;
	}
}
