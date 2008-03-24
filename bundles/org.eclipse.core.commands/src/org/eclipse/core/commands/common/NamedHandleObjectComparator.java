/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.commands.common;

import java.util.Comparator;

import org.eclipse.core.internal.commands.util.Util;

/**
 * Comparator for instances of <code>NamedHandleObject</code> for display to
 * an end user. The comparison is based on the name of the instances.
 * 
 * @since 3.2
 */
public class NamedHandleObjectComparator implements Comparator {

	/**
	 * Compares to instances of NamedHandleObject based on their names. This is
	 * useful is they are display to an end user.
	 * 
	 * @param left
	 *            The first obect to compare; may be <code>null</code>.
	 * @param right
	 *            The second object to compare; may be <code>null</code>.
	 * @return <code>-1</code> if <code>left</code> is <code>null</code>
	 *         and <code>right</code> is not <code>null</code>;
	 *         <code>0</code> if they are both <code>null</code>;
	 *         <code>1</code> if <code>left</code> is not <code>null</code>
	 *         and <code>right</code> is <code>null</code>. Otherwise, the
	 *         result of <code>left.compareTo(right)</code>.
	 */
	public final int compare(final Object left, final Object right) {
		final NamedHandleObject a = (NamedHandleObject) left;
		final NamedHandleObject b = (NamedHandleObject) right;

		String aName = null;
		try {
			aName = a.getName();
		} catch (final NotDefinedException e) {
			// Leave aName as null.
		}
		String bName = null;
		try {
			bName = b.getName();
		} catch (final NotDefinedException e) {
			// Leave bName as null.
		}

		return Util.compare(aName, bName);
	}
}

