/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;

/**
 * Help context ids for the text editor. <p>
 * This interface contains constants only; it is not intended to be implemented.
 */
public interface ITextEditorHelpContextIds extends IAbstractTextEditorHelpContextIds {
	
	/**
	 * Id for the text editor preference page.
	 * Default value: <code>"text_editor_preference_page_context"</code>.
	 */
	public static final String TEXT_EDITOR_PREFERENCE_PAGE= PREFIX + "text_editor_preference_page_context"; //$NON-NLS-1$

	/**
	 * Id for the text editor.
	 * Default value: <code>"text_editor_context"</code>.
	 */
	public static final String TEXT_EDITOR= PREFIX + "text_editor_context"; //$NON-NLS-1$
}