/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

import org.eclipse.jface.text.contentassist.IContentAssistant;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.IContentAssistant} to
 * allow to install a content assistant on the given
 * {@linkplain org.eclipse.jface.contentassist.IContentAssistSubjectControl content assist subject control}.
 *
 * @since 3.0
 */
public interface ISubjectControlContentAssistant extends IContentAssistant {

	/**
	 * Installs content assist support on the given subject.
	 *
	 * @param contentAssistSubjectControl the one who requests content assist
	 */
	void install(IContentAssistSubjectControl contentAssistSubjectControl);
}
