package it.polimi.lab4.utils;

public class Arg {
	public static final int BOOLEAN = 1;
	public static final int STRING = 2;
	public static final int LIST = 4;
	
	public String shortOpt;
	public String longOpt;
	public boolean mandatory = false;
	public int type = Arg.STRING;
	public String description;
	public OnOption onOption;
	
	public void onOption(String value) {
		if(onOption != null) {
			onOption.OnOptionAdd(value);
		}
	}

	public interface OnOption {
		void OnOptionAdd(String value);
	}
}
