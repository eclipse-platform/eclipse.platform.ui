/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.core.runtime.CoreException;

/**
 * @version 	1.0
 * @author
 */
public interface ITextModel {
	IParagraph [] getParagraphs();
	public void parseTaggedText(String taggedText) throws CoreException;
	public void parseRegularText(String regularText, boolean expandURLs) throws CoreException;
	public void dispose();
}