package org.eclipse.core.tests.internal.plugin.a;

import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.plugins.*;

public class ExtensionIndirectReferenceToPlugin extends ConfigurableExtension {

public Object run(Object o) {	
	super.run(o);
	Plugin p = Platform.getPlugin("plugin.c");
	return p;
}
}