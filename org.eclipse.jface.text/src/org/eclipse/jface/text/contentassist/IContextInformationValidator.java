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
package org.eclipse.jface.text.contentassist;



import org.eclipse.jface.text.ITextViewer;


/**
 * A context information validator is used to determine if
 * a displayed context information is still valid or should
 * be dismissed. The interface can be implemented by clients.
 * 
 * @see IContextInformationPresenter
 */
public interface IContextInformationValidator {

	/**
	 * Installs this validator for the given context information.
	 *
	 * @param info the context information which this validator should check
	 * @param viewer the text viewer on which the information is presented
	 * @param documentPosition the document position for which the information has been computed
	 */
	void install(IContextInformation info, ITextViewer viewer, int documentPosition);

	/**
	 * Returns whether the information this validator is installed on is still valid
	 * at the given document position.
	 *
	 * @param documentPosition the current position within the document
	 * @return <code>true</code> if the information also valid at the given document position
	 */
	boolean isContextInformationValid(int documentPosition);
}
