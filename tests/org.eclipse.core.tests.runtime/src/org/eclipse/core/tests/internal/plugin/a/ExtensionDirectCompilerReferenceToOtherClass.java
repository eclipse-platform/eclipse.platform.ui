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

public class ExtensionDirectCompilerReferenceToOtherClass extends ConfigurableExtension {
public Object run(Object o) {	
	super.run(o);
	// make direct compiler reference to other class (not plugin class)
	Class c = org.eclipse.core.tests.internal.plugin.c.api.ApiClass.class;
	return c;
}
}