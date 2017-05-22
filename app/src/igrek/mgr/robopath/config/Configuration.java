package igrek.mgr.robopath.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import igrek.mgr.robopath.logger.Logs;

public class Configuration {
	
	private final String CONFIG_FILE = "config.properties";
	
	private int port = 4000;
	
	public Configuration() {
		loadConfig();
	}
	
	public void loadConfig() {
		Logs.debug("loading configuration from file " + CONFIG_FILE + "...");
		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(CONFIG_FILE);
			prop.load(input);
			
			String portStr = prop.getProperty("port");
			try {
				port = Integer.parseInt(portStr);
			} catch (NumberFormatException e) {
				Logs.error("invalid port number format");
			}
			
		} catch (IOException ex) {
			Logs.error(ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getPort() {
		return port;
	}
	
}