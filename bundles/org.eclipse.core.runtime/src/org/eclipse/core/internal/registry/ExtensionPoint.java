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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.core.internal.runtime.CompatibilityHelper;
import org.eclipse.core.runtime.*;

/**
 * An object which represents the user-defined extension point in a 
 * plug-in manifest. 
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class ExtensionPoint extends NestedRegistryModelObject implements IExtensionPoint {

	// DTD properties (included in plug-in manifest)
	private String id = null;
	// transient properties (not included in plug-in manifest)
	private IExtension[] extensions = null; // configured extensions
	private String schemaReference;

	public IExtension[] getExtensions() {
		IExtension[] tmpExtensions = extensions;
		if (tmpExtensions == null)
			return new IExtension[0];
		return tmpExtensions;
	}

	public String getSchemaReference() {
		return schemaReference == null ? "" : schemaReference.replace(File.separatorChar, '/'); //$NON-NLS-1$		
	}

	public String getSchema() {
		return schemaReference;
	}

	public String getSimpleIdentifier() {
		return id;
	}

	public IExtension getExtension(String id) {
		if (id == null)
			return null;
		IExtension[] tmpExtensions = extensions;
		if (tmpExtensions == null)
			return null;
		for (int i = 0; i < tmpExtensions.length; i++)
			if (id.equals(tmpExtensions[i].getUniqueIdentifier()))
				return tmpExtensions[i];
		return null;
	}

	public String getUniqueIdentifier() {
		return getNamespace() + "." + getSimpleIdentifier(); //$NON-NLS-1$
	}

	public void setExtensions(IExtension[] value) {
		extensions = value;
	}

	public void setSchema(String value) {
		schemaReference = value;
	}

	public void setSimpleIdentifier(String value) {
		id = value;
	}

	public String getNamespace() {
		Namespace parent = (Namespace) getParent();
		return parent.isFragment() ? parent.getHostIdentifier() : parent.getName();
	}

	public String getLabel() {
		String s = getName();
		if (s == null)
			return ""; //$NON-NLS-1$
		String localized = ((Namespace) getParent()).getResourceString(s);
		if (localized != s)
			setLocalizedName(localized);
		return localized;
	}

	public String toString() {
		return getUniqueIdentifier();
	}

	public IConfigurationElement[] getConfigurationElements() {
		IExtension[] tmpExtensions = extensions;		
		if (tmpExtensions == null || tmpExtensions.length == 0)
			return new IConfigurationElement[0];
		Collection result = new ArrayList();
		for (int i = 0; i < tmpExtensions.length; i++) {
			IConfigurationElement[] toAdd = tmpExtensions[i].getConfigurationElements();
			for (int j = 0; j < toAdd.length; j++)
				result.add(toAdd[j]);
		}
		return (IConfigurationElement[]) result.toArray(new IConfigurationElement[result.size()]);
	}

	/**
	 * @deprecated
	 */
	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return CompatibilityHelper.getPluginDescriptor(getNamespace());
	}
}