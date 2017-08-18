package igrek.robopath.utils.console;

import java.util.Scanner;

import igrek.robopath.utils.logger.Logs;

public class CommandLine {
	
	private boolean exit = false;
	
	public CommandLine() {
		Logs.info("Type \"help\" to list available commands.");
	}
	
	public void readContinuously() {
		while (!exit) {
			String cmd = readLine();
			execute(cmd);
		}
	}
	
	public String readLine() {
		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine();
	}
	
	public void execute(String cmd) {
		if (cmd.length() == 0)
			return;
		
		if (cmd.equals("exit")) {
			exit = true;
		} else if (cmd.equals("help")) {
			printHelp();
		} else {
			Logs.warn("unknown command: " + cmd);
		}
	}
	
	private void printHelp() {
		Logs.info("Available commands:");
		Logs.info("exit");
	}
	
}