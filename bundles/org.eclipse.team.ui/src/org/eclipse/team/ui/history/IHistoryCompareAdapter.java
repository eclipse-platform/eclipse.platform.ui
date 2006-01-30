/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.history;

import org.eclipse.compare.structuremergeviewer.ICompareInput;

/**
 * Used to display the contents of an IHistoryPage in a dialog
 * 
 * @since 3.2
 * @see HistoryPageSaveablePart
 * @see IHistoryPage
 */
public interface IHistoryCompareAdapter {
	/**
	 * Returns an ICompareInput for the passed in object
	 * @param object
	 * @return
	 */
	public ICompareInput getCompareInput(Object object);
	
}
