/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.core.runtime.CoreException;

/**
 * @version 	1.0
 * @author
 */
public interface ITextModel {
	IParagraph [] getParagraphs();
	public void parseTaggedText(String taggedText, boolean expandURLs) throws CoreException;
	public void parseRegularText(String regularText, boolean expandURLs) throws CoreException;
	public void dispose();
}