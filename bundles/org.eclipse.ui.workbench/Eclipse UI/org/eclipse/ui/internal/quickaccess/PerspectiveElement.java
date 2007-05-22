/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.3
 * 
 */
public class PerspectiveElement extends QuickAccessElement {

	private final IPerspectiveDescriptor descriptor;

	/* package */PerspectiveElement(IPerspectiveDescriptor descriptor, PerspectiveProvider perspectiveProvider) {
		super(perspectiveProvider);
		this.descriptor = descriptor;
	}

	public void execute() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			activePage.setPerspective(descriptor);
		}
	}

	public String getId() {
		return descriptor.getId();
	}

	public ImageDescriptor getImageDescriptor() {
		return descriptor.getImageDescriptor();
	}

	public String getLabel() {
		return descriptor.getLabel();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((descriptor == null) ? 0 : descriptor.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PerspectiveElement other = (PerspectiveElement) obj;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		return true;
	}
}
