/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

public class ChangePreviewViewerDescriptor extends AbstractDescriptor {

	private static final String EXT_ID= "changePreviewViewers"; //$NON-NLS-1$

	private static DescriptorManager fgDescriptions= new DescriptorManager(EXT_ID, "change") { //$NON-NLS-1$
		@Override
		protected AbstractDescriptor createDescriptor(IConfigurationElement element) {
			return new ChangePreviewViewerDescriptor(element);
		}
	};

	public static ChangePreviewViewerDescriptor get(Object element) throws CoreException {
		return (ChangePreviewViewerDescriptor)fgDescriptions.getDescriptor(element);
	}

	public ChangePreviewViewerDescriptor(IConfigurationElement element) {
		super(element);
	}

	public IChangePreviewViewer createViewer() throws CoreException {
		return (IChangePreviewViewer)fConfigurationElement.createExecutableExtension(CLASS);
	}
}
