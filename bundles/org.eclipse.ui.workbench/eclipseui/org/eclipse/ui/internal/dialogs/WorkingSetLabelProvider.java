/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetLabelProvider extends LabelProvider {
	private final ResourceManager images;

	/**
	 * Create a new instance of the receiver.
	 */
	public WorkingSetLabelProvider() {
		images = new LocalResourceManager(JFaceResources.getResources());
	}

	@Override
	public void dispose() {
		images.dispose();

		super.dispose();
	}

	@Override
	public Image getImage(Object object) {
		Assert.isTrue(object instanceof IWorkingSet);
		IWorkingSet workingSet = (IWorkingSet) object;
		ImageDescriptor imageDescriptor = workingSet.getImageDescriptor();

		return imageDescriptor == null ? null : images.get(imageDescriptor);
	}

	@Override
	public String getText(Object object) {
		Assert.isTrue(object instanceof IWorkingSet);
		IWorkingSet workingSet = (IWorkingSet) object;
		return workingSet.getLabel();
	}
}
