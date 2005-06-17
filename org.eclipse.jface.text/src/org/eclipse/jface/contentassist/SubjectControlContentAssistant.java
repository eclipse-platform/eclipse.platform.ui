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

import org.eclipse.jface.text.Assert;


/**
 * The standard implementation of the {@link org.eclipse.jface.contentassist.ISubjectControlContentAssistant} interface.
 * Usually, clients instantiate this class and configure it before using it.
 *
 * @since 3.0
 */
public class SubjectControlContentAssistant extends org.eclipse.jface.text.contentassist.ContentAssistant implements ISubjectControlContentAssistant {

	/*
	 * @see ISubjectControlContentAssistant#install(IContentAssistSubjectControl)
	 */
	public void install(IContentAssistSubjectControl contentAssistSubjectControl) {
		Assert.isNotNull(contentAssistSubjectControl);
		super.install(contentAssistSubjectControl);
	}
}
