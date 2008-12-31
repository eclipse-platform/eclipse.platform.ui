/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

/**
 * Thrown to indicate that an attempt to create or modify a {@link Range} failed because it would
 * have resulted in an illegal range. A range is illegal if its length is &lt;= 0 or if its start
 * line is &lt; 0.
 *
 * @since 3.2
 */
public final class LineIndexOutOfBoundsException extends IndexOutOfBoundsException {
	private static final long serialVersionUID= 1L;

	/**
	 * Constructs an <code>LineIndexOutOfBoundsException</code> with no detail message.
	 */
	public LineIndexOutOfBoundsException() {
		super();
	}

    /**
	 * Constructs an <code>LineIndexOutOfBoundsException</code> with the specified detail message.
	 *
	 * @param s the detail message.
	 */
	public LineIndexOutOfBoundsException(String s) {
		super(s);
	}

	/**
	 * Constructs a new <code>LineIndexOutOfBoundsException</code>
	 * object with an argument indicating the illegal index.
	 *
	 * @param index the illegal index.
	 */
	public LineIndexOutOfBoundsException(int index) {
		super("Line index out of range: " + index); //$NON-NLS-1$
	}
}
