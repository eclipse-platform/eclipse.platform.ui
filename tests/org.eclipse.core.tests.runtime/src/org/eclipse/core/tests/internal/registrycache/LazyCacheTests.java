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

import java.io.DataInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.RegistryCacheReader;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.model.*;

public class LazyCacheTests extends SimpleCacheTests {
	public LazyCacheTests(String name) {
		super(name);
	}
	public PluginRegistryModel doCacheRead(DataInputStream input, Factory factory) {
		// Cobble together a plugin path
		Map regIndex = InternalPlatform.getRegIndex();
		URL[] pluginPath = null;
		if (regIndex != null) {
			int entrySize = regIndex.keySet().size();
			pluginPath = new URL[entrySize];
			int i = 0;
			for (Iterator list = regIndex.keySet().iterator(); list.hasNext();) {
				String fileName = (String) list.next();
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
	/**
	 * See bug 36659 - there is a thread safety issue with lazily loading the 
	 * plug-in registry. This test consists in forcing the full loading of all 
	 * extensions in all plug-ins in the registry simultaneously in two 
	 * different threads.  
	 */
	public void testThreadSafety() {
		MultiStatus problems = new MultiStatus(org.eclipse.core.runtime.Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
		InternalFactory factory = new InternalFactory(problems);

		String[] localXMLFiles = setupExtensionTest();
		// Don't resolve the registry as all the prerequisites aren't there
		PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, false);
		registry.markReadOnly();

		// try hard to reproduce the thread safety problem
		for (int i = 0; i < 50; i++) {
			Semaphore semaphore = new Semaphore();
			PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

			// the registry visitor is a runnable that will force all extensions of all plug-ins to be loaded 			
			RegistryVisitor registryVisitor = new RegistryVisitor(cachedRegistry, semaphore);

			// create and start 2 threads running the registry visitor
			Thread[] threads = new Thread[2];
			for (int j = 0; j < threads.length; j++) {
				threads[j] = new Thread(registryVisitor);
				threads[j].start();
			}				
			for (int j = 0; j < threads.length; j++);				
				
			// wait for all threads to finish so we can know the outcome	
			try {
				semaphore.down(threads.length);
			} catch (InterruptedException e) {
				fail("2." + i, e);
			}	
			Throwable result = registryVisitor.getResult();
			// no errors
			if (result == null)
				return;
			// hopefully an org.eclipse.core.internal.runtime.AssertionFailedException (internal)
			// thrown by trying to modify a read-only model object
			if (result instanceof Exception)
				fail("3." + i, (Exception) registryVisitor.getResult());			
			if (result instanceof Error)
				throw (Error) result;				
		}
	}
	/**
	 * Forces all extensions to be fully loaded.
	 */
	class RegistryVisitor implements Runnable {
		private PluginRegistryModel registry;
		private Semaphore semaphore;
		private Throwable result;
		RegistryVisitor(PluginRegistryModel registry, Semaphore semaphore) {
			this.registry = registry;
			this.semaphore = semaphore;
		}
		private Throwable getResult() {
			return result;
		}
		public synchronized void setResult(Throwable result) {
			// to avoid overwriting the original failure 
			if (this.result == null)
				this.result = result;
		}		
		public void run() {
			try {
				int rootElementsLoaded = 0;
				PluginDescriptorModel[] plugins = registry.getPlugins();
				// should never happen - just to ensure the registry is good for our test
				assertTrue("The plug-in registry has no plug-ins", plugins.length > 0);
				for (int j = 0; j < plugins.length; j++) {
					ExtensionModel[] extensions = plugins[j].getDeclaredExtensions();
					for (int k = 0; k < extensions.length; k++) {
						ConfigurationElementModel[] elements = extensions[k].getSubElements();
						if (elements == null)
							continue;
						rootElementsLoaded += elements.length;
					}
				}
				// should never happen - just to ensure the registry is good for our test 
				assertTrue("The plug-in registry has no extension elements", rootElementsLoaded > 0);
			} catch (Throwable e) {
				setResult(e);
			} finally {
				// one more thread has finished
				semaphore.up(1);
			}
		}
	}
	static class Semaphore {
		private int counter;
		public synchronized void up(int count) {
			this.counter+= count;
			this.notifyAll(); 
		}
		public synchronized void down(int count) throws InterruptedException {
			while (this.counter < count)
				wait();	
			this.counter -= count;
		}
	}
	public static Test suite() {
		return new TestSuite(LazyCacheTests.class);
	}
}
