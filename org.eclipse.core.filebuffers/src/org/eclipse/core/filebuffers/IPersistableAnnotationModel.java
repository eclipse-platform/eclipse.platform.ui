/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers;


import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IDocument;



/**
 * Tagging interface for {@link org.eclipse.jface.text.source.IAnnotationModel} implementers that offer
 * state persistence.
 *
 * @since 3.0
 */
public interface IPersistableAnnotationModel {

	/**
	 * Transforms the current transient state of the annotation model into a
	 * persistent state.
	 *
	 * @param document the document the annotation model is connected to
	 * @throws CoreException in case the transformation fails
	 */
	void commit(IDocument document) throws CoreException;

	/**
	 * Changes the current transient state of the annotation model to match the
	 * last persisted state.
	 *
	 * @param document the document the annotation model is connected to
	 * @throws CoreException in case accessing the persisted state
	 */
	void revert(IDocument document) throws CoreException;

	/**
	 * Forces this annotation model to re-initialize from the persistent state.
	 * The persistent state must not be the same as the last persisted state.
	 * I.e. external modification may have caused changes to the persistent
	 * state since the last <code>commit</code> or <code>revert</code>
	 * operation.
	 *
	 * @param document the document the annotation model is connected to
	 * @throws CoreException in case accessing the persistent state fails
	 */
	void reinitialize(IDocument document) throws CoreException;
}
