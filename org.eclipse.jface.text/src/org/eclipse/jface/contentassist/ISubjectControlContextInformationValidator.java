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
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * A context information validator is used to determine if
 * a displayed context information is still valid or should
 * be dismissed. The interface can be implemented by clients.
 * <p>
 * A control context information validator can be installed
 * on the given content assist subject control instead of a text viewer.
 * </p>
 * 
 * @see org.eclipse.jface.text.contentassist.IContextInformationValidator
 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter
 * @since 3.0
 */
public interface ISubjectControlContextInformationValidator extends IContextInformationValidator {

	/**
	 * Installs this validator for the given context information.
	 * 
	 * @param info the context information which this validator should check
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param documentPosition the document position for which the information
	 *           has been computed
	 */
	void install(IContextInformation info, IContentAssistSubjectControl contentAssistSubjectControl, int documentPosition);
}
