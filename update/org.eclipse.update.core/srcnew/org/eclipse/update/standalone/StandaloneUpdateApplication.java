/*
 * Created on May 28, 2003
 */
package org.eclipse.update.standalone;

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
			for (int i = 0; i < params.length - 1; i++) {
				if (isValidParam(params[i]))
					options.put(params[i], params[i + 1]);
			}

			Script script =
				new Script(
					(String) options.get("-installFeature"),
					(String) options.get("-version"),
					(String) options.get("-from"),
					(String) options.get("-to"));
			script.run();
		}
		return null;
	}

	private boolean isValidParam(String param) {
		return param.equals("-installFeature")
			|| param.equals("-version")
			|| param.equals("-to")
			|| param.equals("-from");
	}
}
