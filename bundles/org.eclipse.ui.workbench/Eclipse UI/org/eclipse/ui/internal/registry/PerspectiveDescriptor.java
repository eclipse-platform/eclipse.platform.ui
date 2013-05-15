/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
	private IConfigurationElement configElement;
	private boolean hasCustomDefinition;
	private String pluginId;
	private String originalId; // ID of ancestor that was based on a config
								// element

	public PerspectiveDescriptor(String id, String label, PerspectiveDescriptor originalDescriptor) {
		this.id = id;
		this.label = label;
		if (originalDescriptor != null) {
			this.originalId = originalDescriptor.getOriginalId();
			this.image = originalDescriptor.getImageDescriptor();
			this.pluginId = originalDescriptor.getPluginId();
			this.hasCustomDefinition = true;
		}
	}

	PerspectiveDescriptor(String id, IConfigurationElement element) {
		this.id = id;
		this.configElement = element;
	}

	public IConfigurationElement getConfigElement() {
		return configElement;
	}

	public IPerspectiveFactory createFactory() {
		try {
			return (IPerspectiveFactory) configElement
					.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getDescription() {
		return configElement == null ? null : RegistryReader.getDescription(configElement);
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
		if (originalId == null) {
			originalId = getId();
		}

		return originalId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (image != null)
			return image;

		// Try and get an image from the IConfigElement
		if (configElement != null) {
			String icon = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
			if (icon != null) {
				image = AbstractUIPlugin.imageDescriptorFromPlugin(
						configElement.getNamespaceIdentifier(), icon);
			}
		}

		// Get a default image
		if (image == null)
			image = WorkbenchImages.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);

		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getLabel()
	 */
	public String getLabel() {
		return configElement == null ? label : configElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
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
		return configElement == null ? pluginId : configElement.getNamespaceIdentifier();
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
		return configElement != null;
	}

	/**
	 * Returns <code>true</code> if this perspective is a singleton.
	 * 
	 * @return whether this perspective is a singleton
	 */
	public boolean isSingleton() {
		return false;
	}

	public String toString() {
		return this.getClass().getName() + " {id=" + getId() + "}"; //$NON-NLS-1$//$NON-NLS-2$
	}
}
