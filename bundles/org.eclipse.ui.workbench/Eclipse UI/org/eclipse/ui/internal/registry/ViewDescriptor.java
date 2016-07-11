/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.IViewDescriptor;

public class ViewDescriptor implements IViewDescriptor, IPluginContribution {

	private MApplication application;
	private MPartDescriptor descriptor;
	private IConfigurationElement element;
	private String[] categoryPath;
	private ImageDescriptor imageDescriptor;

	public ViewDescriptor(MApplication application, MPartDescriptor descriptor,
			IConfigurationElement element) {
		this.application = application;
		this.descriptor = descriptor;
		this.element = element;

		String category = descriptor.getCategory();
		if (category != null) {
			categoryPath = category.split("/"); //$NON-NLS-1$
		}
	}

	@Override
	public IViewPart createView() throws CoreException {
		if (element == null) {
			throw new CoreException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					"Unable to create an e4 view of id " + descriptor.getElementId())); //$NON-NLS-1$
		}
		return (IViewPart) element.createExecutableExtension("class"); //$NON-NLS-1$
	}

	@Override
	public String[] getCategoryPath() {
		return categoryPath;
	}

	@Override
	public String getDescription() {
		return element == null ? "" : RegistryReader.getDescription(element); //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return descriptor.getElementId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor == null) {
			String iconURI = descriptor.getIconURI();
			if (iconURI == null) {
				// If the icon attribute was omitted, use the default one
				IWorkbench workbench = (IWorkbench) application.getContext().get(
						IWorkbench.class.getName());
				imageDescriptor = workbench.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_DEF_VIEW);
			} else {
				ISWTResourceUtilities utility = (ISWTResourceUtilities) application.getContext()
						.get(IResourceUtilities.class.getName());
				imageDescriptor = utility.imageDescriptorFromURI(URI.createURI(iconURI));
			}
		}
		return imageDescriptor;
	}

	@Override
	public String getLabel() {
		return LocalizationHelper.getLocalized(descriptor.getLabel(), descriptor,
				application.getContext());
	}

	@Override
	public float getFastViewWidthRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getAllowMultiple() {
		return descriptor.isAllowMultiple();
	}

	@Override
	public boolean isRestorable() {
		if (element == null) {
			return false;
		}

		String string = element.getAttribute(IWorkbenchRegistryConstants.ATT_RESTORABLE);
		return string == null ? true : Boolean.parseBoolean(string);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter != null && adapter.equals(IConfigurationElement.class)) {
			return adapter.cast(getConfigurationElement());
		}
		return null;
	}

	public IConfigurationElement getConfigurationElement() {
		return element;
	}

	@Override
	public String getLocalId() {
		return getId();
	}

	@Override
	public String getPluginId() {
		return getConfigurationElement().getNamespaceIdentifier();
	}

}
