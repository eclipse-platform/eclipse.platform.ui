/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.plugin.a;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

public class ExtensionIndirectReferenceToPlugin extends ConfigurableExtension {

public Object run(Object o) {	
	super.run(o);
	Plugin p = Platform.getPlugin("plugin.c");
	return p;
}
}