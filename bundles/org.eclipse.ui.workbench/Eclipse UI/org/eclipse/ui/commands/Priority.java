/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see HandlerSubmission
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.handlers.IHandlerService#activateHandler(String,
 *      IHandler, Expression)
 * @deprecated This concept is now captured in the <code>ISources</code>
 *             integer constants.
 * 
 */
public final class Priority implements Comparable {

	/**
	 * An instance representing 'legacy' priority.
	 */
	public final static Priority LEGACY = new Priority(ISources.LEGACY_LEGACY);

	/**
	 * An instance representing 'low' priority.
	 */
	public final static Priority LOW = new Priority(ISources.LEGACY_LOW);

	/**
	 * An instance representing 'medium' priority.
	 */
	public final static Priority MEDIUM = new Priority(ISources.LEGACY_MEDIUM);

	/**
	 * The string representation of this priority. This is computed once
	 * (lazily). Before it is computed, this value is <code>null</code>.
	 */
	private transient String string = null;

	/**
	 * The priority value for this instance. A lesser integer is considered to
	 * have a higher priority.
	 */
	private int value;

	/**
	 * Constructs a new instance of <code>Priority</code> using a value. This
	 * constructor should only be used internally. Priority instances should be
	 * retrieved from the static members defined above.
	 * 
	 * @param value
	 *            The priority value; a lesser integer is consider to have a
	 *            higher priority value.
	 */
	private Priority(int value) {
		this.value = value;
	}

	/**
	 * @see Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object object) {
		Priority castedObject = (Priority) object;
		int compareTo = Util.compare(value, castedObject.value);
		return compareTo;
	}

	/**
	 * The value for this priority. The lesser the value, the higher priority
	 * this represents.
	 * 
	 * @return The integer priority value.
	 */
	int getValue() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("[value="); //$NON-NLS-1$
			stringBuffer.append(value);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
