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
package org.eclipse.ui.forms.parts;

/**
 * Objects that implement this interface represent hyperlinks created from
 * hyperlink (a) tag in markup.
 */
public interface IRichTextHyperlink {
	/**
	 * Returns the value of <samp>href</samp> attribute as found in the
	 * loaded text. This attribute is used to reference action that will be
	 * executed when the link is activated.
	 * 
	 * @return value of the href attribute
	 * @see RichTextHyperlinkAction
	 */
	String getHref();
	/**
	 * Returns the value of <samp>arg</samp> attribute as found in the loaded
	 * text. This attribute is used to pass additional information to the
	 * action that will be executed when the link is activated. This allow one
	 * hyperlink action to handle multiple individual hyperlinks of the same
	 * type (e.g. RichTextHTTPAction).
	 * 
	 * @return value of the href attribute
	 * @see RichTextHyperlinkAction
	 */
	String getArg();
}
