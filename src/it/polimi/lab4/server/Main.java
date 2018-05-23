package it.polimi.lab4.server;

import it.polimi.lab4.utils.Arg;
import it.polimi.lab4.utils.Arg.OnOption;
import it.polimi.lab4.utils.ArgParser;

public class Main {

	
	private int sPort = -1;
	private int sTimeout = -1;
	private boolean sMultiMatch = false;
	private boolean sDebug = false;
	
	public static void main(String[] args) {
		Main m = new Main();
		m.init(args);
	}

	public Main() {
	}

	private void init(String[] args) {
		ArgParser ap = new ArgParser(args);
		
		Arg debug = new Arg();
		debug.shortOpt = "d";
		debug.longOpt = "debug";
		debug.type = Arg.BOOLEAN;
		debug.description = "Enable debug";
		debug.onOption = new OnOption() {

			@Override
			public void OnOptionAdd(String value) {
				sDebug = true;
			}
		};
		ap.addArg(debug);

		Arg help = new Arg();
		help.shortOpt = "h";
		help.longOpt = "help";
		help.type = Arg.BOOLEAN;
		help.description = "Print this help";
		help.onOption = new OnOption() {

			@Override
			public void OnOptionAdd(String value) {
				ap.printHelp(0);
			}
		};
		ap.addArg(help);

		Arg port = new Arg();
		port.shortOpt = "p";
		port.longOpt = "port";
		port.mandatory = true;
		port.description = "Port to use";
		port.onOption = new OnOption() {

			@Override
			public void OnOptionAdd(String value) {
				try {
					sPort = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					System.err.println("Invalid port number");
					ap.printHelp(11);
				}
			}
		};
		ap.addArg(port);

		Arg multiMatch = new Arg();
		multiMatch.shortOpt = "m";
		multiMatch.longOpt = "multi-match";
		multiMatch.type = Arg.BOOLEAN;
		multiMatch.description = "Enable multi-match mode (default false)";
		multiMatch.onOption = new OnOption() {

			@Override
			public void OnOptionAdd(String value) {
				sMultiMatch = true;
			}
		};
		ap.addArg(multiMatch);

		
		Arg timeout = new Arg();
		timeout.shortOpt = "t";
		timeout.longOpt = "timeout";
		timeout.mandatory = true;
		timeout.description = "Timeout to start the match";
		timeout.onOption = new OnOption() {

			@Override
			public void OnOptionAdd(String value) {
				try {
					sTimeout = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					System.err.println("Invalid timeout value");
					ap.printHelp(11);
				}
			}
		};
		ap.addArg(timeout);
		
		ap.parse();
		
		SagradaServer
			.getInstance()
			.setPort(sPort)
			.setTimeout(sTimeout)
			.setDebug(sDebug)
			.setMultiMatch(sMultiMatch)
			.start();
	}

}
