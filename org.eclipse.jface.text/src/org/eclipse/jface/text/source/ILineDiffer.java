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
package org.eclipse.jface.text.source;


/**
 * Protocol that allows direct access to line information. Usually, implementations will also 
 * implement <code>IAnnotationModel</code>, which only allows <code>Iterator</code> based access
 * to annotations.
 * 
 * @since 3.0
 */
public interface ILineDiffer {
	
	/** 
	 * The id that instances of this interface are stored under in a document's annotation model.
	 * @see IAnnotationModelExtension
	 */
	final static String ID= "diff"; //$NON-NLS-1$
	
	/**
	 * Determines the line state for line <code>line</code> in the targeted document.
	 * 
	 * @param line
	 * @return the line information object for <code>line</code>.
	 */
	ILineDiffInfo getLineInfo(int line);
}
