/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers;


import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * Factory for text file buffer annotation models. Used by the text file buffer
 * manager to create the annotation model for a new text file buffer.
 * <p>
 * This interface is the expected interface for extensions provided for the
 * <code>"org.eclipse.core.filebuffers.annotationModelCreation"</code>
 * extension point.
 *
 * @since 3.0
 */
public interface IAnnotationModelFactory {

	/**
	 * Creates and returns a new annotation model.
	 *
	 * @param location the normalized location
	 * @return a new annotation model
	 */
	IAnnotationModel createAnnotationModel(IPath location);
}
