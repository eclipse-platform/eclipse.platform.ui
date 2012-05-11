/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.Assert;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * The adaptor factory to test scenario described in the bug 200068: adapting to 
 * a class not reachable be the default class loader.
 * 
 * This is a test code so almost all sanity checks are omitted (it is working on a known
 * hard-coded set of extensions and extension points). Also, for simplicity no trackers
 * or caching is done.
 */
public class TestAdapterFactoryLoader extends Assert implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		try {
			Class[] targets = getAdapterList();
			return targets[0].newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			fail("Unable to load target class");
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("Unable to load target class");
			return null;
		}
	}

	public Class[] getAdapterList() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.core.tests.runtime.factoryLoaderTest");
		IExtension[] extensions = extPoint.getExtensions();
		if (extensions.length == 0)
			return new Class[0];
		IExtension extension = extensions[0];
		IConfigurationElement[] confElements = extension.getConfigurationElements();
		String className = confElements[0].getAttribute("name");
		IContributor contributor = extension.getContributor();
		Bundle extensionBundle = ContributorFactoryOSGi.resolve(contributor);
		try {
			return new Class[] {extensionBundle.loadClass(className)};
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unable to load class " + className);
			return null;
		}
	}
}
