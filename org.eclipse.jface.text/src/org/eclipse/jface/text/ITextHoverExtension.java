/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * Extension to <code>ITextHover</code> for providing its own information
 * control creator.
 * 
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.ITextHover
 * @since 3.0
 */
public interface ITextHoverExtension {

	/**
	 * Returns the hover control creator of this text hover.
	 * 
	 * @return the hover control creator
	 */
	IInformationControlCreator getHoverControlCreator();
}
