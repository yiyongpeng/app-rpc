package app.rpc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import app.rpc.remote.DefaultObjectConnector;
import app.rpc.remote.Exportor;
import app.rpc.remote.spring.SpringBeansExportor;

public class Startup {
	private static DefaultObjectConnector conector;

	public static DefaultObjectConnector getConector() {
		return conector;
	}

	public static void main(String[] args) throws IOException {
		String conf = System.getProperty("config", "./config/");

		// log4j configure
		String log4jFile = System.getProperty("log4j",conf+"log4j.properties");
		if (new File(log4jFile).exists()) {
			PropertyConfigurator.configureAndWatch(log4jFile);
		}else{
			System.err.println("Not found file : "+log4jFile);
		}

		// default sets
		String host = "0.0.0.0";
		int port = 9000;
		String beansLocation = conf+"beans.xml";
		String exportLocation = conf+"export.properties";

		// rewrite default value
		if (args.length >= 1) {
			int index = args[0].indexOf(":");
			if (index != -1) {
				host = args[0].substring(0, index);
				port = Integer.parseInt(args[0].substring(index + 1));
			} else {
				port = Integer.parseInt(args[0]);
			}
		}
		if (args.length >= 2)
			beansLocation = args[1];
		if (args.length >= 3)
			exportLocation = args[2];

		// startup server
		conector = new DefaultObjectConnector(host, port);
		if(new File(beansLocation).exists()){
			Exportor exportor = new SpringBeansExportor(beansLocation, exportLocation);
			conector.setExportor(exportor);
		}
		conector.start();

		// write port to FS
		String portPath = System.getProperty("write.port");
		if (portPath != null) {
			FileWriter out = null;
			try {
				out = new FileWriter(portPath);
				out.write(String.valueOf(port));
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				if(conector.isRuning())
				conector.stop();
			}
		});
	}

}
