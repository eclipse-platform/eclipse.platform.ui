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
 * Extension interface for <code>IInformationControl</code>. As it is the responsibility of
 * the implementer of <code>IInformationControl</code> to specify whether the information
 * set is the information itself or a description of the information, only the information control
 * can decide whether there is something that must be displayed.
 * 
 * @since 2.0
 */ 
public interface IInformationControlExtension {
	
	/**
	 * Returns whether this information control has contents to be displayed.
	 * @return <code>true</code> if there is contents to be displayed.
	 */
	boolean hasContents();
}
