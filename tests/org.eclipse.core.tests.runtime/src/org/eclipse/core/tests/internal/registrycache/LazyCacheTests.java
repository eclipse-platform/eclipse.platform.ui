/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registrycache;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.model.Factory;
import org.eclipse.core.runtime.model.PluginRegistryModel;

public class LazyCacheTests extends SimpleCacheTests {
	public LazyCacheTests(String name) {
		super(name);
	}
	public PluginRegistryModel doCacheRead(PluginRegistryModel inRegistry, DataInputStream input, Factory factory) {
		// Cobble together a plugin path
		Map regIndex = InternalPlatform.getRegIndex();
		URL[] pluginPath = null;
		if (regIndex != null) {
			int entrySize = regIndex.keySet().size();
			pluginPath = new URL[entrySize];
			int i = 0;
			for (Iterator list = regIndex.keySet().iterator(); list.hasNext();) {
				String fileName = (String)list.next();
				fileName = "file:" + fileName;
				try {
					pluginPath[i++] = new URL(fileName);
				} catch (MalformedURLException badURL) {
					assertTrue("2.1 Bad url found for " + fileName + ".", true);
				}
			}
		}
		RegistryCacheReader cacheReader = new RegistryCacheReader(factory, true);
		PluginRegistryModel newRegistry = cacheReader.readPluginRegistry(input, pluginPath, true);
		cacheReader.setLazilyLoadExtensions(false);
		return newRegistry;
	}
	public static Test suite() {
		TestSuite suite = new TestSuite("LazyCacheTests");
		suite.addTest(new LazyCacheTests("pluginTest"));
		suite.addTest(new LazyCacheTests("requiresTest"));
		suite.addTest(new LazyCacheTests("libraryTest"));
		suite.addTest(new LazyCacheTests("extensionTest"));
		suite.addTest(new LazyCacheTests("readOnlyTest"));
		suite.addTest(new LazyCacheTests("registryTest"));
		suite.addTest(new LazyCacheTests("extExtPtLinkTest"));
		suite.addTest(new LazyCacheTests("realRegistryTest"));
		suite.addTest(new LazyCacheTests("fragmentTest"));
		suite.addTest(new LazyCacheTests("fragmentPluginTest"));
		suite.addTest(new LazyCacheTests("fragmentExtensionTest"));
		suite.addTest(new LazyCacheTests("fragmentExtExtPtLinkTest"));
		suite.addTest(new LazyCacheTests("fragmentLibraryTest"));
		suite.addTest(new LazyCacheTests("fragmentReadOnlyTest"));
		suite.addTest(new LazyCacheTests("fragmentRequiresTest"));
		suite.addTest(new LazyCacheTests("fragmentRegistryTest"));
		suite.addTest(new LazyCacheTests("testRegistryEOF"));
		return suite;
	}	
}
