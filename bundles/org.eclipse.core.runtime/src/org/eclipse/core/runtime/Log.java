/*******************************************************************************
 * Copyright (c) 2016 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos <sxenos@gmail.com> (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Contains utility functions for logging.
 *
 * @since 3.13
 */
public final class Log {
	/**
	 * Writes an error message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param message
	 *            message string to be logged.
	 * @param exception
	 *            exception to log or null if none.
	 */
	public static void error(Object context, String message, Throwable exception) {
		log(IStatus.ERROR, context, IStatus.OK, message, exception);
	}

	/**
	 * Writes an error message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param message
	 *            message string to be logged.
	 */
	public static void error(Object context, String message) {
		log(IStatus.ERROR, context, IStatus.OK, message, null);
	}

	/**
	 * Writes a warning message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param message
	 *            message string to be logged.
	 * @param exception
	 *            exception to log or null if none.
	 */
	public static void warning(Object context, String message, Throwable exception) {
		log(IStatus.WARNING, context, IStatus.OK, message, exception);
	}

	/**
	 * Writes a warning message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param message
	 *            message string to be logged.
	 */
	public static void warning(Object context, String message) {
		log(IStatus.WARNING, context, IStatus.OK, message, null);
	}

	/**
	 * Writes an info message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param message
	 *            message string to be logged.
	 * @param exception
	 *            exception to log or null if none.
	 */
	public static void info(Object context, String message, Throwable exception) {
		log(IStatus.INFO, context, IStatus.OK, message, exception);
	}

	/**
	 * Writes an info message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param message
	 *            message string to be logged.
	 */
	public static void info(Object context, String message) {
		log(IStatus.INFO, context, IStatus.OK, message, null);
	}

	/**
	 * Writes a status message to the log.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @param toLog
	 *            status message to be logged.
	 */
	public static void log(Object context, IStatus toLog) {
		getLog(context).log(toLog);
	}

	/**
	 * Returns the {@link ILog} associated with the given context object.
	 *
	 * @param context
	 *            a {@link Class} or object that was loaded from the plugin that
	 *            generated the message or a {@link String} containing the
	 *            bundle's symbolic name.
	 * @return the {@link ILog} associated with the given context object.
	 */
	public static ILog getLog(Object context) {
		Bundle bundle = getBundleForContext(context);
		return Platform.getLog(bundle);
	}

	private static void log(int severity, Object context, int code, String message, Throwable exception) {
		Bundle bundle = getBundleForContext(context);
		String symbolicName = bundle.getSymbolicName();
		Platform.getLog(bundle).log(new Status(severity, symbolicName, code, message, exception));
	}

	/**
	 * Returns the bundle for the given object if the given object didn't come
	 * from a plugin bundle. If the given context is a String, it is used as the
	 * symbolic name of the bundle. If the given context is a Class, the bundle
	 * that contains that class is returned. Otherwise, the class of the given
	 * object is used.
	 */
	private static Bundle getBundleForContext(Object context) {
		Bundle bundle;
		if (context instanceof String) {
			bundle = Platform.getBundle((String) context);
			if (bundle == null) {
				throw new IllegalArgumentException(
						"Bundle not found while attempting to write to log. Invalid bundle id = " + context); //$NON-NLS-1$
			}
			return bundle;
		}
		Class classFromBundle;
		if (context instanceof Class) {
			classFromBundle = (Class) context;
		} else {
			classFromBundle = context.getClass();
		}
		bundle = FrameworkUtil.getBundle(classFromBundle);
		if (bundle == null) {
			throw new IllegalArgumentException("Context must be a Class or Object that was loaded from an Eclipse" //$NON-NLS-1$
					+ " plugin bundle. Context was " + context); //$NON-NLS-1$
		}
		return bundle;
	}
}
