/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined extension in a 
 * plug-in manifest.  
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class Extension extends NestedRegistryModelObject implements IExtension {

	// DTD properties (included in plug-in manifest)
	private String extensionPoint;
	private String id;
	protected Object elements;
	// this extension's elements data offset in the registry cache
	protected int subElementsCacheOffset;
	// is this extension already fully loaded?
	protected boolean fullyLoaded = true;

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
		return getNamespace();
	}

	public String getNamespace() {
		Namespace parent = (Namespace) this.getParent();
		return parent.isFragment() ? parent.getHostIdentifier() : parent.getName();
	}

	public IConfigurationElement[] getConfigurationElements() {
		// synchronization is needed to avoid two threads trying to load the same 
		// extension at the same time (see bug 36659) 
		synchronized (this) {
			// maybe it was lazily loaded
			if (!fullyLoaded) {
				fullyLoaded = true;
				RegistryCacheReader reader = getRegistry().getCacheReader();
				if (reader != null)
					elements = reader.loadConfigurationElements(this, subElementsCacheOffset);
			}
			if (elements == null)
				elements = new IConfigurationElement[0];
		}
		return (IConfigurationElement[]) elements;
	}

	/**
	 * Set the extension point with which this extension is associated.
	 *	May be <code>null</code>. 
	 */
	public void setExtensionPointIdentifier(String value) {
		extensionPoint = value;
	}

	/**
	 * Sets the simple identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * This identifier is specified in the plug-in manifest as a non-empty
	 * string containing no period characters (<code>'.'</code>) and 
	 * must be unique within the defining plug-in.
	 *
	 * @param value the simple identifier of the extension (e.g. <code>"main"</code>).
	 *		May be <code>null</code>.
	 */
	public void setSimpleIdentifier(String value) {
		id = value;
	}

	/**
	 * Sets the configuration element children of this extension.
	 *
	 * @param value the configuration elements in this extension.  
	 *		May be <code>null</code>.
	 */
	public void setSubElements(IConfigurationElement[] value) {
		elements = value;
	}

	public String getLabel() {
		String s = getName();
		if (s == null)
			return ""; //$NON-NLS-1$
		return s;
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

	/**
	 * @deprecated
	 */
	public IPluginDescriptor getDeclaringPluginDescriptor() {
		IPluginDescriptor result = CompatibilityHelper.getPluginDescriptor(((Namespace) getParent()).getName());
		if (result == null) {
			Bundle underlyingBundle = Platform.getBundle(((Namespace) getParent()).getName());
			if (underlyingBundle != null) {
				Bundle[] hosts = Platform.getHosts(underlyingBundle);
				if (hosts != null)
					result = CompatibilityHelper.getPluginDescriptor(hosts[0].getSymbolicName());
			}
		}
		if (CompatibilityHelper.DEBUG && result == null)
			Policy.debug("Could not obtain plug-in descriptor for bundle " + ((Namespace) getParent()).getName()); //$NON-NLS-1$
		return result;
	}

	public String getExtensionPointUniqueIdentifier() {
		return getExtensionPointIdentifier();
	}

	/**
	 * Optimization to replace a non-localized key with its localized value.  Avoids having
	 * to access resource bundles for further lookups.
	 */
	public void setLocalizedName(String value) {
		name = value;
		((ExtensionRegistry) InternalPlatform.getDefault().getRegistry()).setDirty(true);
	}
}