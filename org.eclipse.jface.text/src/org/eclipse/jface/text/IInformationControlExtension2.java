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
package org.eclipse.jface.text;

 
/**
 * Extension interface for <code>IInformationControl</code>. An information control
 * which implements this interface displays the given input but ignores the text
 * which can be set using <code>setInformation(String)</code>.
 * 
 * @see org.eclipse.jface.text.IInformationControl
 * @since 2.1
 */ 
public interface IInformationControlExtension2 {
	
	/**
	 * Sets the input to be presented in this information control.
	 * 
	 * @param input		the object to be used as input for this control
	 */
	void setInput(Object input);
}
