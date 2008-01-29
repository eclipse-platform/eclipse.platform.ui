/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 208858
 *******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import org.eclipse.core.internal.databinding.Util;

/**
 * Object describing a diff between two lists.
 * 
 * @since 1.0
 */
public abstract class ListDiff {

	/**
	 * Returns a ListDiffEntry array representing the differences in the list,
	 * in the order they are to be processed.
	 * 
	 * @return a ListDiffEntry array representing the differences in the list,
	 *         in the order they are to be processed.
	 */
	public abstract ListDiffEntry[] getDifferences();

	/**
	 * Traverses the {@link #getDifferences()} array, calling the appropriate
	 * method in <code>visitor</code> for each difference.
	 * <ol>
	 * <li>{@link ListDiffVisitor#handleReplace(int, Object, Object)} is called
	 * whenever a remove entry is immediately followed by an add entry which
	 * shares the same list index.
	 * <li>{@link ListDiffVisitor#handleMove(int, int, Object)} is called
	 * whenever a remove entry is immediately followed by an add entry with an
	 * equivalent element.
	 * <li>{@link ListDiffVisitor#handleRemove(int, Object)} is called whenever
	 * a remove entry does not match conditions 1 or 2.
	 * <li>{@link ListDiffVisitor#handleAdd(int, Object)} is called whenever an
	 * add entry does not match conditions in 1 or 2.
	 * </ol>
	 * 
	 * @param visitor
	 *            the visitor to receive callbacks.
	 * @see ListDiffVisitor
	 * @since 1.1
	 */
	public void accept(ListDiffVisitor visitor) {
		ListDiffEntry[] differences = getDifferences();
		for (int i = 0; i < differences.length; i++) {
			ListDiffEntry entry = differences[i];
			int position = entry.getPosition();
			Object element = entry.getElement();
			boolean addition = entry.isAddition();

			if (!addition && i + 1 < differences.length) {
				ListDiffEntry entry2 = differences[i + 1];
				if (entry2.isAddition()) {
					int position2 = entry2.getPosition();
					Object element2 = entry2.getElement();
					if (position == position2) {
						visitor.handleReplace(position, element, element2);
						i++;
						continue;
					}
					if (Util.equals(element, element2)) {
						visitor.handleMove(position, position2, element);
						i++;
						continue;
					}
				}
			}
			if (addition)
				visitor.handleAdd(position, element);
			else
				visitor.handleRemove(position, element);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ListDiffEntry[] differences = getDifferences();
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getName());
		
		if (differences == null || differences.length == 0) {
			buffer
				.append("{}"); //$NON-NLS-1$
		} else {
			buffer
				.append("{"); //$NON-NLS-1$
			
			for (int i = 0; i < differences.length; i++) {
				if (i > 0)
					buffer.append(", "); //$NON-NLS-1$
				
				buffer
					.append("difference[") //$NON-NLS-1$
					.append(i)
					.append("] [") //$NON-NLS-1$
					.append(differences[i] != null ? differences[i].toString() : "null") //$NON-NLS-1$
					.append("]"); //$NON-NLS-1$
			}
			buffer.append("}"); //$NON-NLS-1$
		}
		
		return buffer.toString();
	}
}
