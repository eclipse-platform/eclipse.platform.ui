/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
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