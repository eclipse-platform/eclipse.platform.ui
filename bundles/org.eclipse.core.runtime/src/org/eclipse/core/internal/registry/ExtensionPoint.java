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

import java.io.File;

import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionPoint;

/**
 * An object which represents the user-defined extension point in a 
 * plug-in manifest. 
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class ExtensionPoint extends RegistryModelObject implements IExtensionPoint {

	// DTD properties (included in plug-in manifest)
	private String id = null;
	// transient properties (not included in plug-in manifest)
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
		return this.getParentIdentifier() + "." + getSimpleIdentifier(); //$NON-NLS-1$
	}
	public void setExtensions(IExtension[] value) {
		extensions = value;
	}
	public void setSchema(String schemaReference) {
		assertIsWriteable();
		this.schemaReference = schemaReference;
	}
	public void setSimpleIdentifier(String value) {
		assertIsWriteable();
		id = value;
	}
	public String getParentIdentifier() {
		BundleModel parent = (BundleModel) getParent();
		return parent.isFragment() ? parent.getHostIdentifier() : parent.getName();
	}
	public String getLabel() {
		String s = getName();
		if (s == null)
			return ""; //$NON-NLS-1$
		return ((BundleModel) getParent()).getResourceString(s);
	}
	public String toString() {
		return getUniqueIdentifier();
	}
}
