/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;

/**
 * @since 3.0
 */
/* package */ class LinkedRangeDifference extends RangeDifference {

	static final int INSERT= 0;
	static final int DELETE= 1;

	LinkedRangeDifference fNext;

	/**
	 * Creates a LinkedRangeDifference an initializes it to the error state
	 */
	LinkedRangeDifference() {
		super(ERROR);
		fNext= null;
	}

	/**
	 * Constructs and links a LinkeRangeDifference to another LinkedRangeDifference
	 *
	 * @param next the next difference
	 * @param operation the operation code. Either {@link #INSERT} or {@link #DELETE}
	 */
	LinkedRangeDifference(LinkedRangeDifference next, int operation) {
		super(operation);
		fNext= next;
	}

	/**
	 * Returns the next difference.
	 *
	 * @return the next difference
	 */
	LinkedRangeDifference getNext() {
		return fNext;
	}

	/**
	 * Returns whether this difference represents a delete operation.
	 *
	 * @return <code>true</code> if this difference represents a delete operation
	 */
	boolean isDelete() {
		return kind() == DELETE;
	}

	/**
	 * Returns whether this difference represents an insert operation.
	 *
	 * @return <code>true</code> if this difference represents an insert operation
	 */
	boolean isInsert() {
		return kind() == INSERT;
	}

	/**
	 * Sets the next difference of this <code>LinkedRangeDifference</code>
	 *
	 * @param next the next difference
	 */
	void setNext(LinkedRangeDifference next) {
		fNext= next;
	}
}
