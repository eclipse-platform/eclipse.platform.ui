/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.IViewDescriptor;

public class ViewDescriptor implements IViewDescriptor, IPluginContribution {

	private MPartDescriptor descriptor;
	private IConfigurationElement element;
	private String[] categoryPath;

	public ViewDescriptor(MPartDescriptor descriptor, IConfigurationElement element) {
		this.descriptor = descriptor;
		this.element = element;

		String category = descriptor.getCategory();
		if (category != null) {
			categoryPath = category.split("/"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#createView()
	 */
	public IViewPart createView() throws CoreException {
		if (element == null) {
			throw new CoreException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					"Unable to create an e4 view of id " + descriptor.getId())); //$NON-NLS-1$
		}
		return (IViewPart) element.createExecutableExtension("class"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getCategoryPath()
	 */
	public String[] getCategoryPath() {
		return categoryPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getDescription()
	 */
	public String getDescription() {
		return element == null ? "" : RegistryReader.getDescription(element); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getId()
	 */
	public String getId() {
		return descriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getLabel()
	 */
	public String getLabel() {
		return descriptor.getLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getFastViewWidthRatio()
	 */
	public float getFastViewWidthRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#getAllowMultiple()
	 */
	public boolean getAllowMultiple() {
		return descriptor.isAllowMultiple();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewDescriptor#isRestorable()
	 */
	public boolean isRestorable() {
		// TODO Auto-generated method stub
		return element == null ? false : Boolean.parseBoolean(element.getAttribute("restorable")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter != null && adapter.equals(IConfigurationElement.class)) {
			return getConfigurationElement();
		}
		return null;
	}

	public IConfigurationElement getConfigurationElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return getConfigurationElement().getNamespaceIdentifier();
	}

}
