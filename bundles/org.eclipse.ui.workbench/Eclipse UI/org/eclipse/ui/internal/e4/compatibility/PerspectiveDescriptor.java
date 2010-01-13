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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PerspectiveDescriptor implements IPerspectiveDescriptor {

	private String id;
	private String label;
	private ImageDescriptor image;
	private IConfigurationElement element;

	PerspectiveDescriptor(String id, String label, IConfigurationElement element) {
		this.id = id;
		this.label = label;
		this.element = element;
	}

	IPerspectiveFactory createFactory() {
		try {
			return (IPerspectiveFactory) element
					.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (image == null) {
			if (element != null) {
				String icon = element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
				if (icon != null) {
					image = AbstractUIPlugin.imageDescriptorFromPlugin(element
							.getNamespaceIdentifier(), icon);
				}
				if (image == null) {
					image = WorkbenchImages
							.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
				}
			}
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getLabel()
	 */
	public String getLabel() {
		return label;
	}

}
