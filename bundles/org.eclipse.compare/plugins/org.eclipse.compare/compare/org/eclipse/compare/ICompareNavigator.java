/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare;

/**
 * A <code>ICompareNavigator</code> is used to navigate through the individual
 * differences of a <code>CompareEditorInput</code>.
 * <p>
 * Please note: the following might change before the final release of Eclipse 3.0.
 * You can retrieve an object implementing the <code>ICompareNavigator</code> from a
 * <code>CompareEditorInput</code> by calling <code>getAdapter(ICompareNavigator)</code>
 * on the <code>CompareEditorInput</code>.
 * </p>
 * @since 3.0
 */
public interface ICompareNavigator {
	
	/**
	 * Starting from the current selection <code>selectChange</code> selects and reveals the next (previous) change.
	 * If the end (or beginning) is reached, the method returns <code>true</code>.
	 * 
	 * @param next if <code>true</code> the next change is selected, otherwise the previous change
	 * @return returns <code>true</code> if end (beginning) is reached, <code>false</code> otherwise
	 */
	public boolean selectChange(boolean next);
}
