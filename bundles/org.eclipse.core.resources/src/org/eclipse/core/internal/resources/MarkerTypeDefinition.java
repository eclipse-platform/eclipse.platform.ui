/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import java.util.HashSet;
import java.util.Set;
//
public class MarkerTypeDefinition {
	protected String type;
	protected Set superTypes;
	protected Set attributeNames;
	protected boolean persistent = false;
	protected String name;
public MarkerTypeDefinition(IExtension ext) {
	super();
	this.type = ext.getUniqueIdentifier();
	this.name = ext.getLabel();
	IConfigurationElement[] elements = ext.getConfigurationElements();
	Set types = null;
	Set attributes = null;
	for (int i = 0; i < elements.length; i++) {
		IConfigurationElement element = elements[i];

		// supertype
		if (element.getName().equalsIgnoreCase("super")) { //$NON-NLS-1$
			String type = element.getAttribute("type"); //$NON-NLS-1$
			if (type != null) {
				if (types == null)
					types = new HashSet(3);
				types.add(type);
			}
		}

		// attribute name
		if (element.getName().equalsIgnoreCase("attribute")) { //$NON-NLS-1$
			String name = element.getAttribute("name"); //$NON-NLS-1$
			if (name != null) {
				if (attributes == null)
					attributes = new HashSet(3);
				attributes.add(name);
			}
		}

		// persistent
		if (elements[i].getName().equalsIgnoreCase("persistent")) { //$NON-NLS-1$
			String bool = element.getAttribute("value"); //$NON-NLS-1$
			if (bool != null)
				this.persistent = new Boolean(bool).booleanValue();
		}
		// XXX: legacy code for support of <transient> tag. remove later.
		if (elements[i].getName().equalsIgnoreCase("transient")) { //$NON-NLS-1$
			String bool = element.getAttribute("value"); //$NON-NLS-1$
			if (bool != null)
				this.persistent = !new Boolean(bool).booleanValue();
		}
	}

	//
	this.superTypes = types;
	this.attributeNames = attributes;
}
public Set getAttributeNames() {
	return attributeNames;
}
public String getName() {
	return name;
}
public Set getSuperTypes() {
	return superTypes;
}
public String getType() {
	return type;
}
public boolean persistent() {
	return persistent;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(40);
	buffer.append(this.getClass().getName());
	buffer.append("\n\ttype=" + type); //$NON-NLS-1$
	buffer.append("\n\tname=" + name); //$NON-NLS-1$
	buffer.append("\n\tsupertypes=" + superTypes); //$NON-NLS-1$
	buffer.append("\n\tattributenames=" + attributeNames); //$NON-NLS-1$
	buffer.append("\n\tpersistent=" + persistent); //$NON-NLS-1$
	return buffer.toString();
}
}
