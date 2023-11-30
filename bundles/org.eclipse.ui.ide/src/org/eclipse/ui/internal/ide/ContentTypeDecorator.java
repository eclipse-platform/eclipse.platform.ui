/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.model.WorkbenchFile;

/**
 * Lightweight decorator for more specific file icons.
 *
 * @since 3.4
 */
public class ContentTypeDecorator implements ILightweightLabelDecorator {

	private boolean fHasEditorAssociationOverridesComputed= false;

	private boolean fHasEditorAssociationOverrides;

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof IFile))
			return;
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench.isClosing())
			return;

		IFile file = (IFile) element;
		ImageDescriptor image = null;

		if (hasEditorAssociationOverrides()) {
			IEditorDescriptor d = IDE.getDefaultEditor(file);
			if (d != null)
				image = d.getImageDescriptor();
		} else {
			IContentDescription contentDescription= null;
			try {
				Job.getJobManager().beginRule(file, null);
				contentDescription= file.getContentDescription();
			} catch (CoreException e) {
				// We already have some kind of icon for this file so it's OK to not
				// find a better icon.
			} finally {
				Job.getJobManager().endRule(file);
			}

			if (contentDescription != null) {
				IContentType contentType = contentDescription.getContentType();
				if (contentType != null) {
					IEditorRegistry editorRegistry = workbench.getEditorRegistry();
					if (editorRegistry != null) { // on shutdown there is no registry anymore (bug 574657)
						image = editorRegistry.getImageDescriptor(file.getName(), contentType);
					}
				}
			}
		}

		// add the image descriptor as a session property so that it will be
		// picked up by the workbench label provider upon the next update.
		try {
			if (file.getSessionProperty(WorkbenchFile.IMAGE_CACHE_KEY) != image)
				file.setSessionProperty(WorkbenchFile.IMAGE_CACHE_KEY, image);
		} catch (CoreException e) {
			// ignore - not being able to cache the image is not fatal
		}
		if (image != null)
			decoration.addOverlay(image);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	private boolean hasEditorAssociationOverrides() {
		if (!fHasEditorAssociationOverridesComputed) {
			fHasEditorAssociationOverrides = EditorAssociationOverrideDescriptor.getContributedEditorAssociationOverrides().length > 0;
			fHasEditorAssociationOverridesComputed = true;
		}
		return fHasEditorAssociationOverrides;
	}

}
