/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.core.internal.runtime.ResourceTranslator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined contents of a bundle model
 * in a extensions manifest.
 */
public class Namespace extends NestedRegistryModelObject {
	// null if this does not correspond to a fragment
	private String hostId;
	private IExtensionPoint[] extensionPoints;
	private IExtension[] extensions;
	private transient ResourceBundle resourceBundle;
	private boolean missingResourceBundle = false;
	private Bundle bundle; //Introduced to fix #46308

	public String getUniqueIdentifier() {
		return getName();
	}

	public void setUniqueIdentifier(String value) {
		setName(value);
	}

	public void setExtensions(IExtension[] value) {
		extensions = value;
	}

	public IExtension getExtension(String id) {
		if (id == null)
			return null;
		IExtension[] list = getExtensions();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (id.equals(list[i].getSimpleIdentifier()))
				return list[i];
		}
		return null;
	}

	public IExtension[] getExtensions() {
		return extensions == null ? new IExtension[0] : extensions;
	}

	public void setExtensionPoints(IExtensionPoint[] value) {
		extensionPoints = value;
	}

	public IExtensionPoint getExtensionPoint(String xpt) {
		if (xpt == null)
			return null;
		IExtensionPoint[] list = getExtensionPoints();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (xpt.equals(list[i].getSimpleIdentifier()))
				return list[i];
		}
		return null;
	}

	public IExtensionPoint[] getExtensionPoints() {
		return extensionPoints == null ? new IExtensionPoint[0] : extensionPoints;
	}

	public void setHostIdentifier(String value) {
		hostId = value;
	}

	public String getHostIdentifier() {
		return hostId;
	}

	public boolean isFragment() {
		return hostId != null;
	}

	public String toString() {
		return "Namespace: " + getName(); //$NON-NLS-1$
	}

	public long getId() {
		// returns an invalid id, but avoids NPE
		return bundle == null ? -1 : bundle.getBundleId();
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle value) {
		bundle = value;
	}

	public String getResourceString(String value) {
		if (resourceBundle != null)
			return ResourceTranslator.getResourceString(null, value, resourceBundle);

		if (missingResourceBundle)
			return value;

		if (resourceBundle == null) {
			try {
				resourceBundle = ResourceTranslator.getResourceBundle(bundle);
			} catch (MissingResourceException e) {
				resourceBundle = null;
			}
		}

		if (resourceBundle == null) {
			missingResourceBundle = true;
			return value;
		}

		return ResourceTranslator.getResourceString(null, value, resourceBundle);
	}
}