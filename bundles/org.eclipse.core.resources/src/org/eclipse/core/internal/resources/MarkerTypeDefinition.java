package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		if (element.getName().equalsIgnoreCase("super")) {
			String type = element.getAttribute("type");
			if (type != null) {
				if (types == null)
					types = new HashSet(3);
				types.add(type);
			}
		}

		// attribute name
		if (element.getName().equalsIgnoreCase("attribute")) {
			String name = element.getAttribute("name");
			if (name != null) {
				if (attributes == null)
					attributes = new HashSet(3);
				attributes.add(name);
			}
		}

		// persistent
		if (elements[i].getName().equalsIgnoreCase("persistent")) {
			String bool = element.getAttribute("value");
			if (bool != null)
				this.persistent = new Boolean(bool).booleanValue();
		}
		// XXX: legacy code for support of <transient> tag. remove later.
		if (elements[i].getName().equalsIgnoreCase("transient")) {
			String bool = element.getAttribute("value");
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
	buffer.append("\n\ttype=" + type);
	buffer.append("\n\tname=" + name);
	buffer.append("\n\tsupertypes=" + superTypes);
	buffer.append("\n\tattributenames=" + attributeNames);
	buffer.append("\n\tpersistent=" + persistent);
	return buffer.toString();
}
}
