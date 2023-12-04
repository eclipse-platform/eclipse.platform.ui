/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.ui.commands;

import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * An instance of this interface represents a priority for use with instances of
 * <code>HandlerSubmission</code>.
 * </p>
 * <p>
 * The order of precedence (from highest to lowest) is as follows. Submissions
 * with higher priority will be preferred over those with lower priority.
 * </p>
 * <ol>
 * <li>MEDIUM</li>
 * <li>LOW</li>
 * <li>LEGACY</li>
 * </ol>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @see HandlerSubmission
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(String,
 *      org.eclipse.core.commands.IHandler, Expression)
 * @deprecated This concept is now captured in the <code>ISources</code> integer
 *             constants. This API is scheduled for deletion, see Bug 431177 for
 *             details
 * @noreference This class is scheduled for deletion.
 */
@Deprecated
public final class Priority implements Comparable {

	/**
	 * An instance representing 'legacy' priority.
	 */
	public static final Priority LEGACY = new Priority(ISources.LEGACY_LEGACY);

	/**
	 * An instance representing 'low' priority.
	 */
	public static final Priority LOW = new Priority(ISources.LEGACY_LOW);

	/**
	 * An instance representing 'medium' priority.
	 */
	public static final Priority MEDIUM = new Priority(ISources.LEGACY_MEDIUM);

	/**
	 * The string representation of this priority. This is computed once (lazily).
	 * Before it is computed, this value is <code>null</code>.
	 */
	private transient String string = null;

	/**
	 * The priority value for this instance. A lesser integer is considered to have
	 * a higher priority.
	 */
	private int value;

	/**
	 * Constructs a new instance of <code>Priority</code> using a value. This
	 * constructor should only be used internally. Priority instances should be
	 * retrieved from the static members defined above.
	 *
	 * @param value The priority value; a lesser integer is consider to have a
	 *              higher priority value.
	 */
	private Priority(int value) {
		this.value = value;
	}

	/**
	 * @see Comparable#compareTo(java.lang.Object)
	 */
	@Override
	@Deprecated
	public int compareTo(Object object) {
		Priority castedObject = (Priority) object;
		return Util.compare(value, castedObject.value);
	}

	/**
	 * The value for this priority. The lesser the value, the higher priority this
	 * represents.
	 *
	 * @return The integer priority value.
	 */
	@Deprecated
	int getValue() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	@Deprecated
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder();
			stringBuffer.append("[value="); //$NON-NLS-1$
			stringBuffer.append(value);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
