/*******************************************************************************
 * Copyright (C) 2014, 2023 Google Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	   Terry Parker (Google) - initial API and implementation
 *	   Marcus Eng (Google)
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.eclipse.core.runtime.Platform;

/**
 * Simple helper class for Eclipse debug tracing.
 *
 * @see <a href= "http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F"
 *      >Eclipse Wiki: FAQ How do I use the platform debug tracing facility?</a>
 */
public class Tracer {
	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault()); //$NON-NLS-1$
	private final String prefix;
	private static final PrintStream out = System.out;

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

	private String getTimestamp() {
		return timeFormatter.format(Instant.now());
	}
}
