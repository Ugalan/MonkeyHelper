
public enum P {
	drawing_getPivotX("drawing:getPivotX()"),
	drawing_getPivotY("drawing:getPivotY()"),
	text_mText("text:mText"),
	mID("mID");
	
	public final String value;
	private P(String value){
		this.value = value;
	}
	
	public String value(){
		return this.value;
	}
}