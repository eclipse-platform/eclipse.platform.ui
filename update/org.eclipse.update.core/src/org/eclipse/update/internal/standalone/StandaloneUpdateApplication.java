/*
 * Created on May 28, 2003
 */
package org.eclipse.update.internal.standalone;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;

/**
 * StandaloneUpdateApplication
 */
public class StandaloneUpdateApplication implements IPlatformRunnable {

	public final static Integer EXIT_ERROR = new Integer(1);
	private static boolean loggedException = false;

	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		if (args == null)
			return EXIT_ERROR;
		if (args instanceof String[]) {
			String[] params = (String[]) args;
			CmdLineArgs cmdLineArgs = new CmdLineArgs(params);
			ScriptedCommand cmd = cmdLineArgs.getCommand();
			if (cmd == null)
				return EXIT_ERROR;
			loggedException = false;
			boolean result = cmd.run();
			if (loggedException) {
				String log = Platform.getLogFileLocation().toOSString();
				System.out.println(
					"Errors(s) occurred during command execution.  Please check "
						+ log
						+ " log file for details.");
			}
			if (result) {
				System.out.println("Command completed successfully.");
				return IPlatformRunnable.EXIT_OK;
			} else {
				System.out.println("Command failed.");
				return EXIT_ERROR;
			}
		}
		return EXIT_ERROR;
	}
	public static void exceptionLogged() {
		loggedException = true;
	}

}
