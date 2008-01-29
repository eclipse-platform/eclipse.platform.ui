/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.html;



/**
 * Provides input for a {@link BrowserInformationControl}.
 *
 * @since 3.4
 */
public abstract class BrowserInformationControlInput {
	
	private BrowserInformationControlInput fPrevious;
	private BrowserInformationControlInput fNext;


	/**
	 * Creates a new browser input.
	 */
	public BrowserInformationControlInput() {
	}

	/**
	 * Creates the next browser input with the given input as previous one.
	 * 
	 * @param previous the previous input or <code>null</code> if none
	 */
	public BrowserInformationControlInput(BrowserInformationControlInput previous) {
		fPrevious= previous;
		if (previous != null)
			fPrevious.fNext= this;
	}

	/**
	 * @return the HTML contents
	 */
	public abstract String getHtml();
	
	/**
	 * Returns the previous browser input.
	 * 
	 * @return the previous browser input, or <code>null</code> if 'back' is
	 *         disabled
	 */
	public BrowserInformationControlInput getPrevious() {
		return fPrevious;
	}
	
	/**
	 * Returns the next browser input.
	 * 
	 * @return the next browser input, or <code>null</code> if 'forward' is
	 *         disabled
	 */
	public BrowserInformationControlInput getNext() {
		return fNext;
	}
}
