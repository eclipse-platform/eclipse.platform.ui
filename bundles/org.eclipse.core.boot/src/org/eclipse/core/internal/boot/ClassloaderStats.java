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
package org.eclipse.core.internal.boot;

import java.io.*;
import java.util.*;
import org.eclipse.core.boot.BootLoader;

public class ClassloaderStats {
	private String id;
	private long timeSpentLoading;
	private int numberOfFailure = 0;
	private Map classesLoaded = new HashMap(20);
	private Vector bundlesLoaded = new Vector(2);
	private boolean keepTraces = false;

	private static ArrayList packageFilters = new ArrayList(4);
	private static Set pluginFilters = new HashSet(5);
	private static int numberOfClassesLoaded;
	private static Stack classesBeingLoaded = new Stack();
	private static Map infos = new HashMap();
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
			System.out.print("Runtime tracing elements defined in: " + filterFile.getAbsolutePath() + "...");
			InputStream input = new FileInputStream(filterFile);
			System.out.println("  Loaded.");
			Properties filters = new Properties() {
				public Object put(Object key, Object value) {
					addFilters((String)key, (String)value);
					return null;
				}
			};
			try {
				filters.load(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			System.out.println("  Failed to load.");
		}
	}

	private static void addFilters(String key, String value) {
		String[] filters = DelegatingURLClassLoader.getArrayFromList(value);
		if ("plugins".equals(key))
			pluginFilters.addAll(Arrays.asList(filters));
		if ("packages".equals(key))
			packageFilters.addAll(Arrays.asList(filters));
	}

	public static void startLoadingClass(String id, String className) {
		// should be called from a synchronized location to protect against concurrent updates
		get(id).startLoadClass(className);
	}

	// get and create if does not exist
	private static ClassloaderStats get(String id) {
		ClassloaderStats result = (ClassloaderStats) infos.get(id);
		if (result == null) {
			result = new ClassloaderStats(id);
			infos.put(id, result);
		}
		return result;
	}

	public static void endLoadingClass(String id, String className, boolean success) {
		// should be called from a synchronized location to protect against concurrent updates
		get(id).endLoadClass(className, success);
	}

	public static void loadedBundle(String id, BundleStats info) {
		get(id).loadedBundle(info);
	}

	public static ClassloaderStats lookup(String id) {
		return (ClassloaderStats) infos.get(id);
	}

	public ClassloaderStats(String id) {
		this.id = id;
		keepTraces = pluginFilters.contains(id);
	}

	public void addBaseClasses(String[] classes) {
		if (!id.equals(BootLoader.PI_BOOT))
			return;
		for (int i = 0; i < classes.length; i++) {
			String name = classes[i];
			if (classesLoaded.get(name) == null) {
				ClassStats value = new ClassStats(name, this);
				value.toBaseClass();
				classesLoaded.put (name, value);
			}
		}
	}

	private void loadedBundle(BundleStats info) {
		bundlesLoaded.add(info);
	}

	public Vector getBundles() {
		return bundlesLoaded;
	}

	private void startLoadClass(String name) {
		ClassStats ci = getClassInfo(name);
		classesBeingLoaded.push(ci);
	}

	// internal method that return the existing classInfo of that create one
	private ClassStats getClassInfo(String name) {
		ClassStats ci = (ClassStats) classesLoaded.get(name);
		return ci == null ? new ClassStats(name, this) : ci;
	}

	private void endLoadClass(String name, boolean success) {
		ClassStats target = (ClassStats) classesBeingLoaded.pop();
		if (!success) {
			numberOfFailure++;
			return;
		}
		if (target.getLoadingNumber() !=-1)
			return;

		target.setLoadingNumber(++numberOfClassesLoaded);
		target.loadingDone();
		classesLoaded.put (name, target);

		traceLoad(name, target);

		// is there something on the load stack.  if so, link them together...
		if (classesBeingLoaded.size() != 0) {
		// get the time spent loading cli and substract its load time from the class that requires cli loading
			ClassStats previous = ((ClassStats) classesBeingLoaded.peek());
			previous.timeSpentLoadingOthers(target.getTotalTime());
			target.setLoadedBy(previous);
			previous.loads(target);
		} else {
			timeSpentLoading = timeSpentLoading + target.getTotalTime();
		}
	}

	private void traceLoad(String name, ClassStats target) {
		// Stack trace code
		if (!keepTraces) {
			boolean found = false;
			for (int i = 0; !found && i < packageFilters.size(); i++)
				if (name.startsWith((String)packageFilters.get(i)))
					found = true;
			if (!found)
				return;
		}

		try {
			target.setBeginningPosition(traceFile.length());
			PrintWriter pw = new PrintWriter(new FileOutputStream(traceFile.getAbsolutePath(),true));
			try {
				pw.println("Loading class: " + name);
				new Throwable().printStackTrace(pw);
			} finally {
				pw.close();
			}
			target.setEndPosition(traceFile.length());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int getNumberOfClassLoaded() {
		return classesLoaded.size();
	}

	public long getClassLoadTime() {
		return timeSpentLoading;
	}

	public ClassStats[] getClassesLoaded() {
		return (ClassStats[])classesLoaded.values().toArray(new ClassStats[classesLoaded.size()]);
	}

	public String getId() {
		return id;
	}
}