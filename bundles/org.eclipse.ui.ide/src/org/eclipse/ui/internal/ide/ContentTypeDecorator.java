/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.model.WorkbenchFile;

/**
 * Lightweight decorator for more specific file icons.
 * 
 * @since 3.4
 * 
 */
public class ContentTypeDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof IFile == false) {
			return;
		}
		IFile file = (IFile) element;
		IContentDescription contentDescription = null;
		try {
			Job.getJobManager().beginRule(file, null);
			contentDescription = file.getContentDescription();
		} catch (CoreException e) {
			// We already have some kind of icon for this file so it's ok to not
			// find a better icon.
		} finally {
			Job.getJobManager().endRule(file);
		}
		if (contentDescription != null) {
			IContentType contentType = contentDescription.getContentType();
			if (contentType != null) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench.isClosing()) {
					return;
				}
				ImageDescriptor image = workbench
						.getEditorRegistry().getImageDescriptor(file.getName(),
								contentType);
				if (image != null) {
					// also add the image descriptor as a session property so that it will be
					// picked up by the workbench label provider upon the next update.
					try {
						file.setSessionProperty(WorkbenchFile.IMAGE_CACHE_KEY, image);
					} catch (CoreException e) {
						// ignore - not being able to cache the image is not fatal
					}
					decoration.addOverlay(image);
				}
			}
		}

	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
