package org.eclipse.help.standalone;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ResourceBundle;


/**
 * Eclipse launcher
 */
class Eclipse {
	private static final String HELP_APPLICATION = "org.eclipse.help.helpApplication";
	private Class bootLoader;
	private Object platformRunnable;
	private Method runMethod;
	private String pluginsDir;
	private String tempDir;

	/**
	 * Constructor
	 */
	public Eclipse(String pluginsDir, String tempDir) 
	{
		if (pluginsDir == null)
			return;
		this.pluginsDir = pluginsDir.replace('\\', '/');
		if (tempDir == null)
			tempDir = System.getProperty("java.io.tmpdir") +  File.separator + "help_system";
		this.tempDir = tempDir;
		init();
	}

	/**
	 * Servlet destroy method shuts down Eclipse.
	 */
	public void shutdown() {
		try {
			Class bootLoader = getBootLoader();
			bootLoader.getMethod("shutdown", new Class[] {
			}).invoke(bootLoader, new Object[] {
			});
		} catch (Exception e) {
			System.out.println("problem shutting down");
		}

	}

	/**
	 * Displays help for specified help resource
	 */
	public Boolean displayHelp(String href) throws Exception {
		
		//System.out.println("displayHelp");
		Object[] params = new Object[1];
		params[0] = new Object[] { "displayHelp", href};
		Object retObj = runMethod.invoke(platformRunnable, params);
		if (retObj != null && retObj instanceof Boolean)
			return (Boolean) retObj;
		else
			return new Boolean(false);

	}
	
	/**
	 * Displays context sensitive help
	 */
	public Boolean displayContext(String contextId, int x, int y) throws Exception {
		
		//System.out.println("displayContext " + contextId+ " ("+x+","+y+")");
		Object[] params = new Object[1];
		//params[0] = new Object[] { "displayContext", contextId, new Integer(x), new Integer(y)};
		String queryString = "contextId="+ URLEncoder.encode(contextId)+"&tab=links";
		params[0] = new Object[] {"displayHelpResource", queryString };
		Object retObj = runMethod.invoke(platformRunnable, params);
		if (retObj != null && retObj instanceof Boolean)
			return (Boolean) retObj;
		else
			return new Boolean(false);

	}
	
	/**
	 * Displays context sensitive help (as infopop).
	 * Note: For now this is not supported, so we still open the full help view.
	 */
	public Boolean displayContextInfopop(String contextId, int x, int y) throws Exception {
		
		//System.out.println("displayContext " + contextId+ " ("+x+","+y+")");
		Object[] params = new Object[1];
		//params[0] = new Object[] { "displayContext", contextId, new Integer(x), new Integer(y)};
		String queryString =  "contextId="+ URLEncoder.encode(contextId)+"&tab=links";
		params[0] = new Object[] {"displayHelpResource", queryString};
		Object retObj = runMethod.invoke(platformRunnable, params);
		if (retObj != null && retObj instanceof Boolean)
			return (Boolean) retObj;
		else
			return new Boolean(false);

	}
	
	
	/**
	 * returns a platform <code>BootLoader</code> which can be used to start
	 * up and run the platform.
	 */
	private Class getBootLoader() throws Exception {
		if (bootLoader == null) {
			
			URL bootUrl = new URL("file", null, pluginsDir + "/org.eclipse.core.boot/boot.jar");
				//System.out.println("URL for bootloader:"+bootUrl);
			bootLoader =
				new URLClassLoader(new URL[] { bootUrl }, null).loadClass(
					"org.eclipse.core.boot.BootLoader");
		}
		return bootLoader;
	}

	private synchronized void init(){
		System.out.println("init eclipse");
		try {
			if (platformRunnable == null) {
				//System.out.println("getting boot loader");
				bootLoader = getBootLoader();
				//System.out.println("got bootloader: " + bootLoader);
				Method mStartup =
					bootLoader.getMethod(
						"startup",
						new Class[] { URL.class, String.class, String[].class });

				//System.out.println("starting eclipse");
				mStartup.invoke(bootLoader, new Object[] { null, tempDir, new String[] {
					}
				});

				Method mGetRunnable =
					bootLoader.getMethod("getRunnable", new Class[] { String.class });

				//System.out.println("get platform runnable");
				platformRunnable =
					mGetRunnable.invoke(bootLoader, new Object[] { HELP_APPLICATION });

				runMethod =
					platformRunnable.getClass().getMethod("run", new Class[] { Object.class });
			}

		} catch (Throwable e) {
			System.out.println("Problem occured initializing Eclipse");
		}
	}

}