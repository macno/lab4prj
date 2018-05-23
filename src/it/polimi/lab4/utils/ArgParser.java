package it.polimi.lab4.utils;

import java.util.ArrayList;

public class ArgParser {

	private ArrayList<String> mandatories = new ArrayList<>();
	private ArrayList<Arg> al_args = new ArrayList<>();
	private String[] sa_args;
	private OnPrintHelpListener onPrintHelp;
	
	public ArgParser(String[] args) {
		sa_args = args;
	}
	
	public void setOnPrintHelp(OnPrintHelpListener onPrintHelp) {
		this.onPrintHelp = onPrintHelp;
	}
	
	public void addArg(Arg arg) {
		al_args.add(arg);
		if(arg.mandatory) {
			mandatories.add(arg.shortOpt);
		}
	}
	
	public void parse() {
		for (int i = 0; i < sa_args.length; i++) {
			String arg = sa_args[i];
			Arg a_arg = getArg(arg.replaceAll("-", ""));
			if (a_arg == null) {
				if(onPrintHelp != null)
					onPrintHelp.onPrintHelp(10);
				else
					printHelp(10);
				return;
			}
			if (a_arg.type == Arg.BOOLEAN) {
				a_arg.onOption("true");
			} else {
				i++;
				try {
					String value = sa_args[i];
					a_arg.onOption(value);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Missing value for " + arg);
					if(onPrintHelp != null)
						onPrintHelp.onPrintHelp(11);
					else
						printHelp(11);
					return;
				}
			}
			if(a_arg.mandatory) {
				mandatories.remove(a_arg.shortOpt);
			}
		}
		if(mandatories.size() > 0) {
			for (String shortOpt : mandatories) {
				Arg missing = getArg(shortOpt);
				System.err.println("Missing required argument -" + missing.shortOpt + ", --" + missing.longOpt);				
			}
			if(onPrintHelp != null)
				onPrintHelp.onPrintHelp(12);
			else
				printHelp(12);
		}
	}

	private Arg getArg(String name) {
		for (Arg arg : al_args) {
			if (name.equals(arg.shortOpt) || name.equals(arg.longOpt)) {
				return arg;
			}
		}
		return null;
	}
	
	public interface OnPrintHelpListener {
		void onPrintHelp(int exitStatus);
	}
	
	public void printHelp(int exitStatus) {
		System.out.println("Usage:");
		for (Arg arg : al_args) {
			System.out.print("\t-" + arg.shortOpt);
			System.out.print(", --" + arg.longOpt);
			if (arg.type > Arg.BOOLEAN) {
				System.out.print(" <value>");
			}
			System.out.print("\t" + arg.description);
			System.out.println("");
		}
		System.exit(exitStatus);
	}
}
