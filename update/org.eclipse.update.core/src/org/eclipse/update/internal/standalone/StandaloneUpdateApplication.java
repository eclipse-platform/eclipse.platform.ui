/*
 * Created on May 28, 2003
 */
package org.eclipse.update.internal.standalone;

import java.util.*;

import org.eclipse.core.boot.*;

/**
 * StandaloneUpdateApplication
 */
public class StandaloneUpdateApplication implements IPlatformRunnable {

	private HashMap options = new HashMap();

	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		if (args == null)
			return null;
		if (args instanceof String[]) {
			String[] params = (String[]) args;
			CmdLineArgs cmdLineArgs = new CmdLineArgs(params);
			ScriptedCommand cmd = cmdLineArgs.getCommand();
			cmd.run();
		}
		return null;
	}
}
