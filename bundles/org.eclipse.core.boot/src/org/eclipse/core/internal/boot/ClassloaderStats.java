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
package org.eclipse.core.internal.boot;

import java.io.*;
import java.util.*;
import org.eclipse.core.boot.BootLoader;

/**
 * Contains information about the classes and the bundles loaded by a given classloader.
 * Typically there is one classloader per plugin so at levels above boot, this equates
 * to information about classes and bundles in a plugin.
 */
public class ClassloaderStats {
	private String id;
	private long loadingTime; // time spent loading classes
	private int failureCount = 0; // number of classes requested but that we fail to provide
	private Map classes = new HashMap(20); // classes loaded by the plugin	(key: class name, value: ClassStats) 
	private ArrayList bundles = new ArrayList(2); // bundles loaded

	private boolean keepTraces = false; // indicate whether or not the traces of classes loaded are kept 
	// filters to indicate which classes we want to keep the traces
	private static ArrayList packageFilters = new ArrayList(4); // filters on a package basis
	private static Set pluginFilters = new HashSet(5); // filters on a plugin basis

	private static Stack classStack = new Stack(); // represents the classes that are currently being loaded 
	private static Map loaders = new HashMap(20); // a dictionary of the classloaderStats (key: pluginId, value: ClassloaderStats)
	public static File traceFile;

	static {
		if (DelegatingURLClassLoader.TRACE_CLASSES || DelegatingURLClassLoader.TRACE_PLUGINS)
			initializeTraceOptions();
	}

	private static void initializeTraceOptions() {
		// create the trace file
		String filename = DelegatingURLClassLoader.TRACE_FILENAME;
		traceFile = new File(filename);
		traceFile.delete();

		//load the filters
		if (!DelegatingURLClassLoader.TRACE_CLASSES)
			return;
		filename = DelegatingURLClassLoader.TRACE_FILTERS;
		if (filename.length() == 0)
			return;
		try {
			File filterFile = new File(filename);
			if (!filterFile.isAbsolute())
				filterFile = new File(InternalBootLoader.getBootDir() + filename);
			System.out.print("Runtime tracing elements defined in: " + filterFile.getAbsolutePath() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
			InputStream input = new FileInputStream(filterFile);
			System.out.println("  Loaded."); //$NON-NLS-1$
			Properties filters = new Properties() {
				public Object put(Object key, Object value) {
					addFilters((String) key, (String) value);
					return null;
				}
			};
			try {
				filters.load(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			System.out.println("  No trace filters loaded."); //$NON-NLS-1$
		}
	}

	private static void addFilters(String key, String value) {
		String[] filters = DelegatingURLClassLoader.getArrayFromList(value);
		if ("plugins".equals(key)) //$NON-NLS-1$
			pluginFilters.addAll(Arrays.asList(filters));
		if ("packages".equals(key)) //$NON-NLS-1$
			packageFilters.addAll(Arrays.asList(filters));
	}

	public static void startLoadingClass(String id, String className) {
		// must be called from a synchronized location to protect against concurrent updates
		findLoader(id).startLoadClass(className);
	}

	// get and create if does not exist
	private static ClassloaderStats findLoader(String id) {
		ClassloaderStats result = (ClassloaderStats) loaders.get(id);
		if (result == null) {
			result = new ClassloaderStats(id);
			loaders.put(id, result);
		}
		return result;
	}

	public static Stack getClassStack() {
		return classStack;
	}

	public static ClassloaderStats[] getLoaders() {
		return (ClassloaderStats[]) loaders.values().toArray(new ClassloaderStats[loaders.size()]);
	}

	public static void endLoadingClass(String id, String className, boolean success) {
		// must be called from a synchronized location to protect against concurrent updates
		findLoader(id).endLoadClass(className, success);
	}

	public static void loadedBundle(String id, BundleStats info) {
		findLoader(id).loadedBundle(info);
	}

	public static ClassloaderStats getLoader(String id) {
		return (ClassloaderStats) loaders.get(id);
	}

	public ClassloaderStats(String id) {
		this.id = id;
		keepTraces = pluginFilters.contains(id);
	}

	public void addBaseClasses(String[] baseClasses) {
		if (!id.equals(BootLoader.PI_BOOT))
			return;
		for (int i = 0; i < baseClasses.length; i++) {
			String name = baseClasses[i];
			if (classes.get(name) == null) {
				ClassStats value = new ClassStats(name, this);
				value.toBaseClass();
				classes.put(name, value);
			}
		}
	}

	private void loadedBundle(BundleStats bundle) {
		bundles.add(bundle);
	}

	public ArrayList getBundles() {
		return bundles;
	}

	private void startLoadClass(String name) {
		classStack.push(findClass(name));
	}

	// internal method that return the existing classStats or creates one
	private ClassStats findClass(String name) {
		ClassStats result = (ClassStats) classes.get(name);
		return result == null ? new ClassStats(name, this) : result;
	}

	private void endLoadClass(String name, boolean success) {
		ClassStats current = (ClassStats) classStack.pop();
		if (!success) {
			failureCount++;
			return;
		}
		if (current.getLoadOrder() >= 0)
			return;

		classes.put(name, current);
		current.setLoadOrder(classes.size());
		current.loadingDone();
		traceLoad(name, current);

		// is there something on the load stack.  if so, link them together...
		if (classStack.size() != 0) {
			// get the time spent loading cli and subtract its load time from the class that requires loading
			ClassStats previous = ((ClassStats) classStack.peek());
			previous.addTimeLoadingOthers(current.getTimeLoading());
			current.setLoadedBy(previous);
			previous.loaded(current);
		} else {
			loadingTime = loadingTime + current.getTimeLoading();
		}
	}

	private void traceLoad(String name, ClassStats target) {
		// Stack trace code
		if (!keepTraces) {
			boolean found = false;
			for (int i = 0; !found && i < packageFilters.size(); i++)
				if (name.startsWith((String) packageFilters.get(i)))
					found = true;
			if (!found)
				return;
		}

		// Write the stack trace. The position in the file are set to the corresponding classStat object
		try {
			target.setTraceStart(traceFile.length());
			PrintWriter output = new PrintWriter(new FileOutputStream(traceFile.getAbsolutePath(), true));
			try {
				output.println("Loading class: " + name); //$NON-NLS-1$
				output.println("Class loading stack:"); //$NON-NLS-1$
				output.println("\t" + name); //$NON-NLS-1$
				for (int i = classStack.size() - 1; i >= 0; i--)
					output.println("\t" + ((ClassStats) classStack.get(i)).getClassName()); //$NON-NLS-1$
				output.println("Stack trace:"); //$NON-NLS-1$
				new Throwable().printStackTrace(output);
			} finally {
				output.close();
			}
			target.setTraceEnd(traceFile.length());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int getClassLoadCount() {
		return classes.size();
	}

	public long getClassLoadTime() {
		return loadingTime;
	}

	public ClassStats[] getClasses() {
		return (ClassStats[]) classes.values().toArray(new ClassStats[classes.size()]);
	}

	public String getId() {
		return id;
	}
}
