/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.views.IViewDescriptor;

public class ViewDescriptor implements IViewDescriptor {

	private IConfigurationElement element;

	public ViewDescriptor(IConfigurationElement element) {
		this.element = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#createView()
	 */
	public IViewPart createView() throws CoreException {
		return (IViewPart) element.createExecutableExtension("class"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getCategoryPath()
	 */
	public String[] getCategoryPath() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getId()
	 */
	public String getId() {
		return element.getAttribute("id"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getLabel()
	 */
	public String getLabel() {
		return element.getAttribute("name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getFastViewWidthRatio()
	 */
	public float getFastViewWidthRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#getAllowMultiple()
	 */
	public boolean getAllowMultiple() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewDescriptor#isRestorable()
	 */
	public boolean isRestorable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
