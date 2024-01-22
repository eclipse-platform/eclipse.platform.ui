/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.commands.util;

/**
 * <p>
 * A utility class for printing tracing output to the console.
 * </p>
 * <p>
 * Clients must not extend or instantiate this class.
 * </p>
 *
 * @since 3.2 Marked for deletion via Bug 143992
 *
 * @deprecated
 * @noreference This class is not intended to be referenced by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated(forRemoval = true, since = "2024-03")
public final class Tracing {

	/**
	 * The separator to place between the component and the message.
	 */
	public static final String SEPARATOR = " >>> "; //$NON-NLS-1$

	/**
	 * <p>
	 * Prints a tracing message to standard out. The message is prefixed by a
	 * component identifier and some separator. See the example below.
	 * </p>
	 *
	 * <pre>
	 *        BINDINGS &gt;&gt; There are 4 deletion markers
	 * </pre>
	 *
	 * @param component
	 *            The component for which this tracing applies; may be
	 *            <code>null</code>
	 * @param message
	 *            The message to print to standard out; may be <code>null</code>.
	 */
	public static final void printTrace(final String component,
			final String message) {
		StringBuilder buffer = new StringBuilder();
		if (component != null) {
			buffer.append(component);
		}
		if ((component != null) && (message != null)) {
			buffer.append(SEPARATOR);
		}
		if (message != null) {
			buffer.append(message);
		}
		System.out.println(buffer.toString());
	}

	/**
	 * This class is not intended to be instantiated.
	 */
	private Tracing() {
		// Do nothing.
	}
}
