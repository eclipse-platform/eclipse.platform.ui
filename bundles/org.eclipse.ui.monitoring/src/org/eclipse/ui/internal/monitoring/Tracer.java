/*******************************************************************************
 * Copyright (C) 2014, Google Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Terry Parker (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.core.runtime.Platform;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Simple helper class for Eclipse debug tracing.
 *
 * @see <a href= "http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F"
 *      >Eclipse Wiki: FAQ How do I use the platform debug tracing facility?</a>
 */
public class Tracer {
	private static final Calendar localChronology = Calendar.getInstance();
	private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$
	private final String prefix;
	private final PrintStream out = System.out;

	private static String getTimestamp() {
		return timeFormatter.format(new Date(localChronology.getTimeInMillis()));
	}

	/**
	 * Returns {@code true} if the debug option is set, but only if the platform is running in debug
	 * mode (e.g., not in a unit test.)
	 */
	public static boolean isTracingEnabled(String debugOption) {
		return Platform.isRunning() && Boolean.parseBoolean(Platform.getDebugOption(debugOption));
	}

	/**
	 * Returns a tracer object if the given debug option is enabled, or {@code null} if it is not.
	 */
	public static Tracer create(String prefix, String debugOption) {
		if (isTracingEnabled(debugOption)) {
			return new Tracer(prefix);
		}
		return null;
	}

	/**
	 * Creates a tracer object that assists in debug tracing.
	 *
	 * @param prefix a string to be prefixed to every trace line (may be {@code null})
	 */
	protected Tracer(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Prints out the given Object.
	 */
	public void trace(Object o) {
		out.printf("%s %s: %s\n", getTimestamp(), prefix, o); //$NON-NLS-1$
	}

	/**
	 * Prints out the given message and object.
	 */
	public void trace(String msg, Object... params) {
		trace(String.format(msg, params));
	}

	/**
	 * Prints the stack trace of a given {@code Throwable} object
	 *
	 * @param t a {@code Throwable} that cannot be null.
	 */
	public void traceStackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		t.printStackTrace(printer);
		printer.flush();
		trace(writer.toString());
	}
}
