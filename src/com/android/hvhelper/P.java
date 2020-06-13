package com.android.hvhelper;

public enum P {
	// P.text_mText.toString() == "text_mText"
	// P.text_mText.value == "text:mText"
	drawing_getPivotX("drawing:getPivotX()"),
	drawing_getPivotY("drawing:getPivotY()"),
	text_mText("text:mText"),
	mText("mText"),
	mID("mID");
	
	public final String value;
	private P(String value){
		this.value = value;
	}
	
	public String value(){
		return this.value;
	}
}
