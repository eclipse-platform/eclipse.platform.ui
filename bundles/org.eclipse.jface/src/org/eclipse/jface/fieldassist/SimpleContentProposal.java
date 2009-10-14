/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.fieldassist;

import org.eclipse.core.runtime.Assert;

/**
 * A default implementation of IContentProposal that allows clients to specify
 * the contents in a constructor method.
 * 
 * @since 3.6
 * 
 */
public class SimpleContentProposal implements IContentProposal {
	private static final String EMPTY = ""; //$NON-NLS-1$

	private String content = EMPTY;
	private String label = EMPTY;
	private String description = EMPTY;
	private int cursorPosition = 0;

	/**
	 * Create a simple content proposal whose label and content are the
	 * specified String.
	 * 
	 * @param content
	 *            the String representing the content. Should not be
	 *            <code>null</code>.
	 */
	public SimpleContentProposal(String content) {
		this(content, content, null);
	}

	/**
	 * Create a simple content proposal whose content and description are as
	 * specified in the parameters.
	 * 
	 * @param content
	 *            the String representing the content. Should not be
	 *            <code>null</code>. This string will also be used as the label.
	 * @param description
	 *            the String representing the description, or <code>null</code>
	 *            if there should be no description.
	 */
	public SimpleContentProposal(String content, String description) {
		this(content, content, description);
	}

	/**
	 * Create a simple content proposal whose content, label, and description
	 * are as specified in the parameters.
	 * 
	 * @param content
	 *            the String representing the content. Should not be
	 *            <code>null</code>.
	 * @param label
	 *            the String representing the label. Should not be
	 *            <code>null</code>.
	 * 
	 * @param description
	 *            the String representing the description, or <code>null</code>
	 *            if there should be no description.
	 */
	public SimpleContentProposal(String content, String label,
			String description) {
		Assert.isNotNull(content);
		Assert.isNotNull(label);
		this.content = content;
		this.label = label;
		this.description = description;
		this.cursorPosition = content.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposal#getContent()
	 */
	public String getContent() {
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposal#getCursorPosition()
	 */
	public int getCursorPosition() {
		return cursorPosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposal#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposal#getLabel()
	 */
	public String getLabel() {
		return label;
	}
}
