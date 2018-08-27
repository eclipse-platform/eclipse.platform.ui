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

import org.eclipse.ltk.ui.refactoring.IStatusContextViewer;

public class StatusContextViewerDescriptor extends AbstractDescriptor {

	private static final String EXT_ID= "statusContextViewers"; //$NON-NLS-1$

	private static DescriptorManager fgDescriptions= new DescriptorManager(EXT_ID, "context") { //$NON-NLS-1$
		@Override
		protected AbstractDescriptor createDescriptor(IConfigurationElement element) {
			return new StatusContextViewerDescriptor(element);
		}
	};

	public static StatusContextViewerDescriptor get(Object element) throws CoreException {
		return (StatusContextViewerDescriptor)fgDescriptions.getDescriptor(element);
	}

	public StatusContextViewerDescriptor(IConfigurationElement element) {
		super(element);
	}

	public IStatusContextViewer createViewer() throws CoreException {
		return (IStatusContextViewer)fConfigurationElement.createExecutableExtension(CLASS);
	}
}
