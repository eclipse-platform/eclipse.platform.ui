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
package org.eclipse.jface.text.templates;

import org.eclipse.jface.text.BadLocationException;

/**
 * A template editor implements an action to edit a template buffer in its context.
 * 
 * @since 3.0
 */
public interface ITemplateEditor {

	/**
	 * Resolves the variables in <code>buffer</code> withing <code>context</code>
	 * and edits the template content to reflect the resolved variables.
	 * 
	 * @param buffer the template buffer
	 * @param context the template context
	 * @throws BadLocationException if the buffer cannot be successfully modified
	 */
	void edit(TemplateBuffer buffer, TemplateContext context) throws BadLocationException;

}
