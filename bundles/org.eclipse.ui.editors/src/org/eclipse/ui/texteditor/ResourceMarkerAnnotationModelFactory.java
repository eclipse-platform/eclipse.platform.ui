/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

	@Override
	public IAnnotationModel createAnnotationModel(IPath location) {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
		if (file != null)
			return new ResourceMarkerAnnotationModel(file);
		return new AnnotationModel();
	}
}
