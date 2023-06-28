/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @deprecated As of 3.2, replaced by Platform UI's field assist support
 */
@Deprecated
public interface ISubjectControlContentAssistant extends IContentAssistant {

	/**
	 * Installs content assist support on the given subject.
	 *
	 * @param contentAssistSubjectControl the one who requests content assist
	 */
	void install(IContentAssistSubjectControl contentAssistSubjectControl);
}
