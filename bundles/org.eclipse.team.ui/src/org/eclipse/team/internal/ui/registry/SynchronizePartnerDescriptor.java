/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.registry;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class SynchronizePartnerDescriptor {
	private QualifiedName id;
	private ImageDescriptor imageDescriptor;
	public  static final String ATT_QUALIFIED_NAME = "qualified_name"; //$NON-NLS-1$
	public  static final String ATT_LOCAL_NAME = "local_name"; //$NON-NLS-1$
	public  static final String ATT_NAME = "name"; //$NON-NLS-1$
	public  static final String ATT_ICON = "icon"; //$NON-NLS-1$
	public  static final String ATT_CLASS = "class"; //$NON-NLS-1$
	private static final String ATT_TYPE = "type"; //$NON-NLS-1$
	
	private static final String TYPE_STATIC = "static";
	
	private String label;
	private String className;
	private String type;
	private IConfigurationElement configElement;
	private String description;

	/**
	 * Create a new ViewDescriptor for an extension.
	 */
	public SynchronizePartnerDescriptor(IConfigurationElement e, String desc) throws CoreException {
		configElement = e;
		description = desc;
		loadFromExtension();
	}
	/**
	 * Return an instance of the declared view.
	 */
	public IViewPart createView() throws CoreException {
		Object obj = WorkbenchPlugin.createExtension(configElement, ATT_CLASS);
		return (IViewPart) obj;
	}

	public IConfigurationElement getConfigurationElement() {
		return configElement;
	}

	/**
	 * Returns this view's description. This is the value of its <code>"description"</code>
	 * attribute.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	public QualifiedName getId() {
		return id;
	}

	public String getType() {
		return type;
	}
	
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor != null)
			return imageDescriptor;
		String iconName = configElement.getAttribute(ATT_ICON);
		if (iconName == null)
			return null;
		imageDescriptor = WorkbenchImages.getImageDescriptorFromExtension(configElement.getDeclaringExtension(), iconName);
		return imageDescriptor;
	}

	public String getLabel() {
		return label;
	}

	public boolean isStatic() {
		if(type == null) return true;
		return type.equals(TYPE_STATIC);
	}
	
	/**
	 * load a view descriptor from the registry.
	 */
	private void loadFromExtension() throws CoreException {
		String qualified_name = configElement.getAttribute(ATT_QUALIFIED_NAME);
		String local_name = configElement.getAttribute(ATT_LOCAL_NAME);
		label = configElement.getAttribute(ATT_NAME);
		className = configElement.getAttribute(ATT_CLASS);
		type = configElement.getAttribute(ATT_TYPE);

		// Sanity check.
		if ((label == null) || (className == null) || (local_name == null)) {
			throw new CoreException(new Status(IStatus.ERROR, configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), 0, "Invalid extension (missing label or class name): " + id, //$NON-NLS-1$
					null));
		}
		
		id = new QualifiedName(qualified_name, local_name);
	}

	/**
	 * Returns a string representation of this descriptor. For debugging
	 * purposes only.
	 */
	public String toString() {
		return "Synchronize Participant(" + getId() + ")"; //$NON-NLS-2$//$NON-NLS-1$
	}
}