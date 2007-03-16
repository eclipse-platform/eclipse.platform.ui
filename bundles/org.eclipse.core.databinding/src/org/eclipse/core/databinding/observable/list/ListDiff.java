/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.databinding.observable.list;


/**
 * Object describing a diff between two lists.
 * 
 * @since 1.0
 */
public abstract class ListDiff {

	/**
	 * Returns a list of ListDiffEntry
	 * 
	 * @return a list of ListDiffEntry
	 */
	public abstract ListDiffEntry[] getDifferences();
	
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
