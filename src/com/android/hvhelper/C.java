package com.android.hvhelper;

public enum C {
	android_widget_FrameLayout("android.widget.FrameLayout");
	
	public final String value;
	private C(String value){
		this.value = value;
	}
	
	public String value(){
		return this.value;
	}
}
