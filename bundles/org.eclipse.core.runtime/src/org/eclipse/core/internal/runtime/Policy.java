/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

public class Policy {
	private static String bundleName = "org.eclipse.core.internal.runtime.messages"; //$NON-NLS-1$
	private static ResourceBundle bundle;

	/*
	 * Returns a resource bundle, creating one if it none is available. 
	 */
	private static ResourceBundle getResourceBundle() {
		// thread safety
		ResourceBundle tmpBundle = bundle;
		if (tmpBundle != null)
			return tmpBundle;
		return bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(), new BundleClassLoader(InternalPlatform.getDefault().getBundleContext().getBundle()));
	}

	/**
	 * Forces the internal resource bundle to be recreated.
	 */
	public static void forgetResourceBundle() {
		bundle = null;
	}

	/**
	 * Lookup the message with the given ID in this catalog 
	 */
	public static String bind(String id) {
		return bind(id, (String[]) null);
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] {binding});
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] {binding1, binding2});
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string values.
	 */
	public static String bind(String id, String[] bindings) {
		if (id == null)
			return "No message available"; //$NON-NLS-1$
		String message = null;
		try {
			message = getResourceBundle().getString(id);
		} catch (MissingResourceException e) {
			// If we got an exception looking for the message, fail gracefully by just returning
			// the id we were looking for.  In most cases this is semi-informative so is not too bad.
			return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (bindings == null)
			return message;
		return MessageFormat.format(message, bindings);
	}

	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks, int style) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks, style);
	}

	/**
	 * Print a debug message to the console. 
	 * Pre-pend the message with the current date and the name of the current thread.
	 */
	public static void debug(String message) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(new Date(System.currentTimeMillis()));
		buffer.append(" - ["); //$NON-NLS-1$
		buffer.append(Thread.currentThread().getName());
		buffer.append("] "); //$NON-NLS-1$
		buffer.append(message);
		System.out.println(buffer.toString());
	}

	/**
	 * A helper classloader that prevents ResourceBundles to be cached.
	 */
	private static class BundleClassLoader extends ClassLoader {
		private Bundle base;

		public BundleClassLoader(Bundle base) {
			this.base = base;
		}

		protected Class findClass(String name) throws ClassNotFoundException {
			return base.loadClass(name);
		}

		protected URL findResource(String name) {
			return base.getResource(name);
		}
	}

}