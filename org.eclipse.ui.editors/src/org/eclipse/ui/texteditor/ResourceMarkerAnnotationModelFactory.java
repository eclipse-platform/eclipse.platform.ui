/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IAnnotationModelFactory;

import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * An annotation model factory for resource marker annotation models.
 *
 * @since 3.0
 */
public class ResourceMarkerAnnotationModelFactory implements IAnnotationModelFactory {

	/*
	 * @see org.eclipse.core.filebuffers.IAnnotationModelFactory#createAnnotationModel(org.eclipse.core.runtime.IPath)
	 */
	public IAnnotationModel createAnnotationModel(IPath location) {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
		if (file != null)
			return new ResourceMarkerAnnotationModel(file);
		return new AnnotationModel();
	}
}
