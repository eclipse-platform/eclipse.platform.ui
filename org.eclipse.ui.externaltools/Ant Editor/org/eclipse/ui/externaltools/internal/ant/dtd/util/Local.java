/*====================================================================
Copyright (c) 2002, 2003 Object Factory Inc.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Object Factory Inc. - Initial implementation
====================================================================*/
package org.eclipse.ui.externaltools.internal.ant.dtd.util;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * Localization utilities.<p>
 * 
 * Simplified localization rules.
 * <ul>
 * <li>Only one string constant per line.</li>
 * <li>Empty string constants "" ignored.</li>
 * <li>Non-UI string lines contain "//$NO".</li>
 * <li>All other strings must be wrapped in Local method
 * and must be a key in plugin.properties file.</li>
 * </ul>
 * No platform-specific code in this class.
 * @author Bob Foster
 */
public class Local {
	
	private static LinkedList fListeners;
	protected static ResourceBundle fBundle;

	/**
	 * LocalListener allows environments to
	 * extend Local to obtain a resource bundle
	 * from an arbitrary location. The listener
	 * is not called until the first time a
	 * call is made to <code>getString()</code>
	 * (directly or indirectly) after a call
	 * to <code>pushBundle()</code> or
	 * <code>popBundle()</code>.
	 */
	public static interface LocalListener {
		public ResourceBundle getBundle();
	}
	
	/**
	 * Push a LocalListener on the stack.
	 * Except for static code that pushes a default
	 * listener, code must be written to guarantee
	 * stackwise behavior, e.g.,
	 * <pre>
	 * Local.pushListener(myListener);
	 * try {
	 *   // blah
	 * }
	 * finally {
	 *   Local.popListener();
	 * }
	 * </pre>
	 */
	static public void pushListener(LocalListener listener) {
		if (fListeners == null)
			fListeners = new LinkedList();
		fListeners.add(listener);
		fBundle = null;
	}
	
	/**
	 * Pop a LocalListener from the stack.
	 */
	static public void popListener() {
		if (fListeners == null || fListeners.size() == 0)
			throw new IllegalArgumentException("Pop of empty stack");
		fListeners.removeLast();
		if (fListeners.isEmpty())
			fListeners = null;
		fBundle = null;
	}
	
	/**
	 * Return localized string for key.
	 * @param key			Lookup key.
	 * @param defaultValue	Value returned if key not found.
	 */
	static public String getString(String key, String defaultValue) {
		try {
			getBundle();			
			return fBundle.getString(key);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * Return the local resource bundle.
	 */
	static public ResourceBundle getBundle() {
		if (fBundle == null) {
			if (fListeners == null)
				throw new IllegalStateException("no resource bundle and no LocalListener");
			fBundle = ((LocalListener)fListeners.getLast()).getBundle();
			if (fBundle == null)
				throw new IllegalStateException("LocalListener returned null");
		}
		return fBundle;
	}
	
	/**
	 * Return localized string for key.
	 * @param key			Lookup key. If key not found,
	 * 						key is returned.
	 */
	static public String getString(String key) {
		return getString(key, key);
	}
	
	/**
	 * Return formatted, localized string.
	 * @param patternKey	Lookup key of format pattern. If key not found,
	 * 						key itself is used as pattern.
	 * @param r0			Object substituted for {0} in pattern.
	 */
	static public String format(String patternKey, Object r0) {
		return MessageFormat.format(getString(patternKey), new Object[] {r0});
	}
	
	/**
	 * Return formatted, localized string.
	 * @param patternKey	Lookup key of format pattern. If key not found,
	 * 						key itself is used as pattern.
	 * @param r0			Object substituted for {0} in pattern.
	 * @param r1			Object substituted for {1} in pattern.
	 */
	static public String format(String patternKey, Object r0, Object r1) {
		return MessageFormat.format(getString(patternKey), new Object[] {r0, r1});
	}
	
	/**
	 * Return formatted, localized string.
	 * Note: <strong>do not</strong> combine these methods to produce
	 * a single message. If the <code>format()</code> variant you need
	 * is not here, add one.
	 * @param patternKey	Lookup key of format pattern. If key not found,
	 * 						key itself is used as pattern.
	 * @param r0			Object substituted for {0} in pattern.
	 * @param r1			Object substituted for {1} in pattern.
	 * @param r2			Object substituted for {2} in pattern.
	 */
	static public String format(String patternKey, Object r0, Object r1, Object r2) {
		return MessageFormat.format(getString(patternKey), new Object[] {r0, r1, r2});
	}
	
	static public String format(String patternKey, int i0) {
		return format(patternKey, Integer.toString(i0));
	}
	
	static public String format(String patternKey, int i0, int i1) {
		return format(patternKey, Integer.toString(i0), Integer.toString(i1));
	}
}
