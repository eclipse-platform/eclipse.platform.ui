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
package org.eclipse.core.filebuffers;


import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IDocument;



/**
 * Interface implemented by <code>IAnnotationModel</code> implementers that offer
 * persistable state.
 * 
 * @since 3.0
 */
public interface IPersistableAnnotationModel {

	/**
	 * @param document
	 */
	void commit(IDocument document) throws CoreException;

	/**
	 * @param document
	 */
	void revert(IDocument document) throws CoreException;

	/**
	 * @param document
	 */
	void reinitialize(IDocument document) throws CoreException;
}
