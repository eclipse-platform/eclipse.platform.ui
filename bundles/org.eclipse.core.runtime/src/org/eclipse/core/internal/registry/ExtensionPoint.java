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
import java.util.Arrays;
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
	// TODO should these be transient variables?
	private IExtension[] extensions = null; // configured extensions
	private String schemaReference;

	public IExtension[] getExtensions() {
		if (extensions == null)
			return new IExtension[0];
		return extensions;
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
		if (extensions == null)
			return null;
		for (int i = 0; i < extensions.length; i++) {
			if (id.equals(extensions[i].getUniqueIdentifier()))
				return extensions[i];
		}
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
		BundleModel parent = (BundleModel) getParent();
		return parent.isFragment() ? parent.getHostIdentifier() : parent.getName();
	}

	public String getLabel() {
		String s = getName();
		if (s == null)
			return ""; //$NON-NLS-1$
		String localized = ((BundleModel) getParent()).getResourceString(s);
		if (localized != s)
			setLocalizedName(localized);
		return localized;
	}

	public String toString() {
		return getUniqueIdentifier();
	}

	public IConfigurationElement[] getConfigurationElements() {
		IExtension[] exts = getExtensions();
		ArrayList allConfigElts = new ArrayList();

		for (int i = 0; i < exts.length; i++)
			allConfigElts.addAll(Arrays.asList(exts[i].getConfigurationElements()));

		IConfigurationElement[] value = new IConfigurationElement[allConfigElts.size()];
		allConfigElts.toArray(value);
		return value;
	}

	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return CompatibilityHelper.getPluginDescriptor(getNamespace());
	}
}