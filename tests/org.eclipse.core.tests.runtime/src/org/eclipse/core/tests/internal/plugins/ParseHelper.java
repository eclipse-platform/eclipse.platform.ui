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
package org.eclipse.core.tests.internal.plugins;

import java.net.*;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.runtime.IStatus;
public class ParseHelper extends PluginVersionTest {
/**
 * ParseHelper constructor comment.
 */
public ParseHelper() {
	super();
}
/**
 * ParseHelper constructor comment.
 * @param name java.lang.String
 */
public ParseHelper(String name) {
	super(name);
}
/* doParsing
 * This method will parse a series of XML files.  The input array should be
 * an array of string buffers where each string buffer is considered a complete 
 * XML file.  The returning array will have a corresponding plugin descriptor
 * for each of the XML files in the input array
 */
static PluginRegistry doParsing(InternalFactory factory, URL[] pluginPath, boolean doResolve) {
	PluginRegistry registry = (PluginRegistry) RegistryLoader.parseRegistry(pluginPath, factory, false);
	if (doResolve) {
		IStatus resolveStatus = registry.resolve(true, true);
		factory.getStatus().merge(resolveStatus);
	}
	registry.markReadOnly();
	registry.startup(null);
	return registry;
}
}
