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
package org.eclipse.core.internal.registry;

import java.lang.reflect.Method;
import org.eclipse.core.internal.runtime.CompatibilityHelper;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined extension in a 
 * plug-in manifest.  
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class Extension extends RegistryModelObject implements IExtension {

	// DTD properties (included in plug-in manifest)
	private String extensionPoint;
	private String id;
	private IConfigurationElement[] elements;
	// this extension's elements data offset in the registry cache
	private int subElementsCacheOffset;
	// is this extension already fully loaded?
	private boolean fullyLoaded = true;

	/**
	 * Two Extensions are equal if they have the same Id
	 * and target the same extension point.
	 */
	public boolean equals(Object object) {
		if (object instanceof Extension) {
			Extension em = (Extension) object;
			return (id == em.id) && (extensionPoint == em.extensionPoint);
		}
		return false;
	}

	/**
	 * Returns the extension point with which this extension is associated.
	 *
	 * @return the extension point with which this extension is associated
	 *  or <code>null</code>
	 */
	public String getExtensionPointIdentifier() {
		return extensionPoint;
	}
	/**
	 * Returns the simple identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * This identifier is specified in the plug-in manifest as a non-empty
	 * string containing no period characters (<code>'.'</code>) and 
	 * must be unique within the defining plug-in.
	 *
	 * @return the simple identifier of the extension (e.g. <code>"main"</code>)
	 *  or <code>null</code>
	 */
	public String getSimpleIdentifier() {
		return id;
	}
	public String getUniqueIdentifier() {
		return id == null ? null : this.getParentIdentifier() + "." + id; //$NON-NLS-1$
	}
	public String getParentIdentifier() {
		BundleModel parent = (BundleModel) this.getParent();
		return parent.isFragment() ? parent.getHostIdentifier() : parent.getName();
	}
	public IConfigurationElement[] getConfigurationElements() {
		// synchronization is needed to avoid two threads trying to load the same 
		// extension at the same time (see bug 36659) 
		synchronized (this) {
			// maybe it was lazily loaded
			if (!fullyLoaded) {
				fullyLoaded = true;
				RegistryCacheReader reader = ((ExtensionRegistry) getRegistry()).getCacheReader();
				if (reader != null)
					elements = reader.loadConfigurationElements(this, subElementsCacheOffset);
			} else {
				if (elements == null)
					return new IConfigurationElement[0];
			}
		}
		return elements;
	}

	/**
	 * Sets this model object and all of its descendents to be read-only.
	 * Subclasses may extend this implementation.
	 *
	 * @see #isReadOnly
	 */
	public void markReadOnly() {
		super.markReadOnly();
		if (elements != null)
			for (int i = 0; i < elements.length; i++)
				 ((ConfigurationElement) elements[i]).markReadOnly();
	}
	/**
	 * Set the extension point with which this extension is associated.
	 * This object must not be read-only.
	 *
	 * @return the extension point with which this extension is associated.  
	 *		May be <code>null</code>.
	 */
	public void setExtensionPointIdentifier(String value) {
		assertIsWriteable();
		extensionPoint = value;
	}
	/**
	 * Sets the simple identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * This identifier is specified in the plug-in manifest as a non-empty
	 * string containing no period characters (<code>'.'</code>) and 
	 * must be unique within the defining plug-in.
	 * This object must not be read-only.
	 *
	 * @param value the simple identifier of the extension (e.g. <code>"main"</code>).
	 *		May be <code>null</code>.
	 */
	public void setSimpleIdentifier(String value) {
		assertIsWriteable();
		id = value;
	}
	/**
	 * Sets the configuration element children of this extension.
	 * This object must not be read-only.
	 *
	 * @param value the configuration elements in this extension.  
	 *		May be <code>null</code>.
	 */
	public void setSubElements(IConfigurationElement[] value) {
		assertIsWriteable();
		elements = value;
	}
	public String getLabel() {
		String s = getName();
		if (s == null)
			return ""; //$NON-NLS-1$
		return ((BundleModel) getParent()).getResourceString(s);
	}
	public String toString() {
		return getUniqueIdentifier() + " -> " + getExtensionPointIdentifier(); //$NON-NLS-1$
	}

	void setSubElementsCacheOffset(int value) {
		subElementsCacheOffset = value;
	}
	public boolean isFullyLoaded() {
		return fullyLoaded;
	}
	public void setFullyLoaded(boolean value) {
		fullyLoaded = value;
	}
	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return CompatibilityHelper.getPluginDescriptor(((BundleModel) getParent()).getName());
	}
	public String getExtensionPointUniqueIdentifier() {
		return getExtensionPointIdentifier();
	}
}
