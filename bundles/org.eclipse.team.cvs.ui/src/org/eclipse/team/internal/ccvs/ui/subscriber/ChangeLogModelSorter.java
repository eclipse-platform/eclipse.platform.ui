/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorter for the change log model provider. 
 * 
 * @since 3.0
 */
public class ChangeLogModelSorter extends ViewerSorter {
	
	private final int CHANGE_LOG = 0;
	private final int FILE = 1;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	public int category(Object element) {
		if(element instanceof ChangeLogDiffNode) {
			return CHANGE_LOG;
		} else if(element instanceof ChangeLogModelProvider.FullPathSyncInfoElement) {
			return FILE;
		}
		return super.category(element);
	}
}
