package org.eclipse.core.tests.internal.plugin.a;

import org.eclipse.core.runtime.*;

public class ExtensionDirectCompilerReferenceToPluginClass extends ConfigurableExtension {

public Object run(Object o) {	
	super.run(o);
	// make direct compiler reference to other plugin class
	Plugin p = org.eclipse.core.tests.internal.plugin.b.PluginClass.plugin;
	return p;
}
}