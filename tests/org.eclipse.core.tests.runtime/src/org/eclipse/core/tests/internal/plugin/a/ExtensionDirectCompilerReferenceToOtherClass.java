package org.eclipse.core.tests.internal.plugin.a;

import org.eclipse.core.runtime.*;

public class ExtensionDirectCompilerReferenceToOtherClass extends ConfigurableExtension {
public Object run(Object o) {	
	super.run(o);
	// make direct compiler reference to other class (not plugin class)
	Class c = org.eclipse.core.tests.internal.plugin.c.api.ApiClass.class;
	return c;
}
}