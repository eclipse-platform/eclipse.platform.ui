/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.contentassist;

import org.eclipse.jface.text.contentassist.IContentAssistant;


/**
 * Extension interface for <code>IContentAssistant</code> which allows
 * to install a content assistant on the given content assist subject
 * control instead of a text viewer.
 * 
 * @see org.eclipse.jface.text.contentassist.IContentAssistant
 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl
 * @since 3.0
 */
public interface ISubjectControlContentAssistant extends IContentAssistant {
	
	/**
	 * Installs content assist support on the given subject.
	 * 
	 * @param contentAssistSubject the one who requests content assist
	 * @throws UnsupportedOperationException if the content assist does not support this method
	 */
	void install(IContentAssistSubjectControl contentAssistSubject);
}
