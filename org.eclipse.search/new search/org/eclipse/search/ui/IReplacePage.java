/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

/**
 * An extension interface to <code>ISearchPage</code>. If clients implement
 * <code>IReplacePage</code> in addition to <code>ISearchPage</code>, a 
 * "Replace" button will be shown in the search dialog.
 *
 * @since 3.0
 */
public interface IReplacePage {

	/**
	 * Performs the replace action for this page.
	 * The search dialog calls this method when the Replace
	 * button is pressed.
	 *
	 * @return <code>true</code> if the dialog can be closed after execution
	 */
	public boolean performReplace();
	
}
