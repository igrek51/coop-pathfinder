package igrek.mgr.robopath;

import igrek.mgr.robopath.logger.Logs;

public class MainApplication {
	
	private String[] args;
	
	public MainApplication(String[] args) {
		this.args = args;
	}
	
	public void run() {
		try {
			Logs.debug("Starting application...");
			
			MainSample.main(args);
			
			//			new CommandLine().readContinuously();
			
		} catch (Throwable e) {
			Logs.error(e);
		}
	}
}
