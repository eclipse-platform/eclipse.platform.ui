package org.eclipse.core.tests.internal.plugin.b;

import org.eclipse.core.runtime.*;

public class PluginClass extends Plugin {

	public static Plugin plugin = null;
	public int startupCount = 0;
	

public PluginClass(IPluginDescriptor descriptor) {
	super(descriptor);
	plugin = this;
}

public void startup() throws CoreException {
	startupCount++;
}
}