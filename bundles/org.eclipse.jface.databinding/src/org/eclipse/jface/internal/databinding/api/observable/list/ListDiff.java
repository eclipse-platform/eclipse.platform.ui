/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.api.observable.list;

/**
 * Standard implementation of IListDiff.
 * @since 1.0
 *
 */
public class ListDiff implements IListDiff {

	private IListDiffEntry[] differences;

	/**
	 * @param difference
	 */
	public ListDiff(IListDiffEntry difference) {
		this.differences = new IListDiffEntry[] { difference };
	}

	/**
	 * @param difference1
	 * @param difference2
	 */
	public ListDiff(IListDiffEntry difference1, IListDiffEntry difference2) {
		this.differences = new IListDiffEntry[] { difference1, difference2 };
	}

	/**
	 * @param differences
	 */
	public ListDiff(IListDiffEntry[] differences) {
		this.differences = differences;
	}

	public IListDiffEntry[] getDifferences() {
		return differences;
	}

}
