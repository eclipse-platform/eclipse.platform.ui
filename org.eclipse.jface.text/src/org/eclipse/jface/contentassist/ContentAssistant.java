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

import org.eclipse.jface.text.Assert;


/**
 * The standard implementation of the <code>IContentAssistant</code> interface.
 * Usually, clients instantiate this class and configure it before using it.
 */
public class ContentAssistant extends org.eclipse.jface.text.contentassist.ContentAssistant implements IControlContentAssistant {
	
	/*
	 * @see IContentAssistantExtension#install(IContentAssistSubject)
	 */
	public void install(IContentAssistSubject contentAssistSubject) {
		Assert.isNotNull(contentAssistSubject);
		super.install(contentAssistSubject);
	}
}
