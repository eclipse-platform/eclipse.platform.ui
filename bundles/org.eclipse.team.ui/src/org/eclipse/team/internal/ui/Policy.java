package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.internal.ui.target.InfiniteSubProgressMonitor;

/**
 * Policy implements NLS convenience methods for the plugin and
 * makes progress monitor policy decisions
 */
public class Policy {
	// The resource bundle to get strings from
	protected static ResourceBundle bundle = null;

	/**
	 * Creates a NLS catalog for the given locale.
	 * 
	 * @param bundleName  the name of the bundle
	 */
	public static void localize(String bundleName) {
		bundle = ResourceBundle.getBundle(bundleName);
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 * 
	 * @param id  the id to look up
	 * @param binding  the string to bind to the result
	 * @return the bound string
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 * 
	 * @param id  the id to look up
	 * @param binding1  the first string to bind to the result
	 * @param binding2  the second string to bind to the result
	 * @return the bound string
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}
	
	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 * 
	 * @param key  the id to look up
	 * @return the string with the given key
	 */
	public static String bind(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Gets a string from the resource bundle and binds it with the given arguments. If the key is 
	 * not found, return the key.
	 * 
	 * @param key  the id to look up
	 * @param args  the strings to bind to the result
	 * @return the bound string
	 */
	public static String bind(String key, Object[] args) {
		try {
			return MessageFormat.format(bind(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Checks if the progress monitor is canceled.
	 * 
	 * @param monitor  the onitor to check for cancellation
	 * @throws OperationCanceledException if the monitor is canceled
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
	/**
	 * Returns a monitor for the given monitor
	 * 
	 * @param monitor  the monitor to return a monitor for
	 * @return a monitor for the given monitor
	 */
	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return monitor;
	}	
	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}
	public static IProgressMonitor subInfiniteMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new InfiniteSubProgressMonitor(monitor, ticks);
	}
}