package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestResult;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.EclipseTestHarnessApplication;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.Workbench;

/**
 * A test harness with UI and logging support.
 */
public class EclipseUITestHarnessApplication extends EclipseTestHarnessApplication {
	protected boolean ignoreFirst;
	protected int repeatCount;
	protected LoggingTestResult logResult;
	
	/**
	 * Application entry point.
	 */
	public Object run(Object userArgs) throws Exception {
		PrintStream logStream = System.err;
		String logFilename = null;
		repeatCount = 1;
		ignoreFirst = false;
		if (userArgs instanceof String[]) {
			// parse args, no error handling
			String[] args = (String[]) userArgs;
			List argsList = new ArrayList(args.length);
			for (int i = 0; i < args.length; ++i) {
				if ("-repeat".equals(args[i])) {
					repeatCount = Integer.parseInt(args[++i]);
				} else if ("-ignorefirst".equals(args[i])) {
					ignoreFirst = true;
				} else if ("-nolog".equals(args[i])) {
					logStream = null;
				} else if ("-log".equals(args[i])) {
					logFilename = args[++i];
				} else {
					argsList.add(args[i]);
				}
			}
			userArgs = argsList.toArray(new String[argsList.size()]);
		}
		// setup logging
		if (logFilename != null) {
			File file = new File(logFilename);
			logStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFilename)));
		}
		logResult = new LoggingTestResult(logStream);
		try {
			logResult.startLog(System.currentTimeMillis(), getSDKBuildId());
			return launchWorkbench(userArgs);
		} finally {
			logResult.endLog();
			if (logFilename != null) logStream.close();
		}
	}
	
	/**
	 * Launches the Workbench UI.
	 */
	protected Object launchWorkbench(final Object userArgs) throws Exception {
		final Exception[] exception = new Exception[1];
		Workbench workbench = new Workbench() {
			/*** this code should be kept in sync with Workbench.runEventLoop() ***/
			protected void runEventLoop() {
				// Dispatch all events.
				Display display = Display.getCurrent();
				while (true) {
					try {
						if (!display.readAndDispatch())
							break;
					} catch (Throwable e) {
						break;
					}
				}
		
				// Run our hook.
				try {
					workbenchHook(this);
				} catch (Exception e) {
					exception[0] = e;
				}
				
				// Close the workbench.
				close();		
			}
		};
		Object result = workbench.run(userArgs);
		if (exception[0] != null) throw exception[0];
		return result;
	}

	/**
	 * Callback from Workbench if it launched successfully.
	 */
	protected Object workbenchHook(Workbench workbench) throws Exception {
		// run the underlying non-ui test launcher to locate and start the test cases
		return super.run(workbench.getCommandLineArgs());
	}
	
	/**
	 * Runs the specified test suite.  Called from the non-ui test launcher.
	 */
	protected void run(Test suite) {
		junit.textui.TestRunner runner = new junit.textui.TestRunner() {
			public TestResult createTestResult() {
				return logResult;
			}
		};
		for (int i = 0; i < repeatCount; ++i) {
			logResult.setLogging(! (i == 0 && ignoreFirst));
			TestResult result = runner.doRun(suite, false);

			// quit if an error occurred
			if (! result.wasSuccessful()) {
				System.out.println("Aborted due to error after " + (i + 1) + " repetition(s).");
				break;
			}
		}
	}
	
	/**
	 * Gets the SDK build id
	 */
	public static String getSDKBuildId() {	
		try {
			URL url = Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.sdk").getInstallURL();
			url = new URL(url, "platform.ini");
			InputStream is = url.openStream();
			try {
				Properties sdkProperties = new Properties();
				sdkProperties.load(is);
				String buildId = sdkProperties.getProperty("buildID");
				if (buildId != null) return buildId;
			} finally {
				is.close();
			}
		} catch (Exception e) {
		}
		return "unknown";
	}
}
