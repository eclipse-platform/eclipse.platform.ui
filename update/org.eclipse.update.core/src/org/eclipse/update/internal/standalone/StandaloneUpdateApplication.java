/*
 * Created on May 28, 2003
 */
package org.eclipse.update.internal.standalone;

import org.eclipse.core.boot.*;

/**
 * StandaloneUpdateApplication
 */
public class StandaloneUpdateApplication implements IPlatformRunnable {

	public final static Integer EXIT_ERROR = new Integer(1);

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
			boolean result = cmd.run();
			if (result)
				return IPlatformRunnable.EXIT_OK;
			else
				return EXIT_ERROR;
		}
		return EXIT_ERROR;
	}
}
