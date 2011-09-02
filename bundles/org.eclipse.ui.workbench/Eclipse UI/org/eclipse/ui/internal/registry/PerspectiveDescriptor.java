/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj_eclipse@ihug.com.au) - handler registration
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * PerspectiveDescriptor.
 * <p>
 * A PerspectiveDesciptor has 3 states:
 * </p>
 * <ol>
 * <li>It <code>isPredefined()</code>, in which case it was defined from an
 * extension point.</li>
 * <li>It <code>isPredefined()</code> and <code>hasCustomFile</code>, in
 * which case the user has customized a predefined perspective.</li>
 * <li>It <code>hasCustomFile</code>, in which case the user created a new
 * perspective.</li>
 * </ol>
 * 
 */
public class PerspectiveDescriptor implements IPerspectiveDescriptor,
		IPluginContribution {

	private String id;
	private String label;
	private ImageDescriptor image;
	private IConfigurationElement element;
	private boolean hasCustomDefinition;

	public PerspectiveDescriptor(String id, String label, boolean hasCustomDefinition) {
		this.id = id;
		this.label = label;
		this.hasCustomDefinition = hasCustomDefinition;
	}

	PerspectiveDescriptor(String id, String label, IConfigurationElement element) {
		this.id = id;
		this.label = label;
		this.element = element;
	}

	public IConfigurationElement getConfigElement() {
		return element;
	}

	public IPerspectiveFactory createFactory() {
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
		return RegistryReader.getDescription(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	public String getOriginalId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (image == null) {
			image = WorkbenchImages.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
			if (element != null) {
				String icon = element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
				if (icon != null) {
					image = AbstractUIPlugin.imageDescriptorFromPlugin(
							element.getNamespaceIdentifier(), icon);
				}
			}
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getLabel()
	 */
	public String getLabel() {
		return label;
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
		return element == null ? null : element.getNamespaceIdentifier();
	}

	/**
	 * Returns <code>true</code> if this perspective has a custom definition.
	 * 
	 * @return whether this perspective has a custom definition
	 */
	public boolean hasCustomDefinition() {
		return hasCustomDefinition;
	}

	public void setHasCustomDefinition(boolean value) {
		hasCustomDefinition = value;
	}

	/**
	 * Returns <code>true</code> if this perspective is predefined by an
	 * extension.
	 * 
	 * @return boolean whether this perspective is predefined by an extension
	 */
	public boolean isPredefined() {
		return element != null;
	}

	/**
	 * Returns <code>true</code> if this perspective is a singleton.
	 * 
	 * @return whether this perspective is a singleton
	 */
	public boolean isSingleton() {
		return false;
	}
}
