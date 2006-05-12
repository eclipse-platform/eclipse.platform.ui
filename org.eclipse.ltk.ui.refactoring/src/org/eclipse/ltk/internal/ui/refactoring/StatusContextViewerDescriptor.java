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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.ltk.ui.refactoring.IStatusContextViewer;

public class StatusContextViewerDescriptor extends AbstractDescriptor {
	
	private static final String EXT_ID= "statusContextViewers"; //$NON-NLS-1$
	
	private static DescriptorManager fgDescriptions= new DescriptorManager(EXT_ID, "context") { //$NON-NLS-1$
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
