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
package org.eclipse.jface.text.contentassist;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.IContextInformation} with 
 * the ability to freely position the context information.
 * 
 * @since 2.0
 */
public interface IContextInformationExtension {

	/**
	 * Returns the start offset of the range for which this context information is valid.
	 * 
	 * @return the start offset of the range for which this context information is valid
	 */
	int getContextInformationPosition();
}
