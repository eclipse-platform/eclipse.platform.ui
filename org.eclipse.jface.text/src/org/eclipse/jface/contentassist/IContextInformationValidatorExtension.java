/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.contentassist;

import org.eclipse.jface.text.contentassist.IContextInformation;

/**
 * Extends <code>IContentAssit</code> with the concept of a content assist
 * subject which provides the context for the content assistant.
 * 
 * @since 3.0
 */
public interface IContextInformationValidatorExtension {

	/**
	 * Installs this validator for the given context information.
	 * 
	 * @param info the context information which this validator should check
	 * @param contentAssistSubject the content assist subject
	 * @param documentPosition the document position for which the information
	 *           has been computed
	 */
	void install(IContextInformation info, IContentAssistSubject contentAssistSubject, int documentPosition);
}
