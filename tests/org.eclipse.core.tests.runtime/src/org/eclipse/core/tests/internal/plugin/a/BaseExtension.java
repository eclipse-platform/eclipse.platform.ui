package org.eclipse.core.tests.internal.plugin.a;

import org.eclipse.core.boot.*;

public class BaseExtension implements IPlatformRunnable {

	public int runCount = 0;
public BaseExtension() {
	super();
}

public Object run(Object o) {	
	runCount++;
	return null;
}
}