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

import java.util.Map;

import org.eclipse.core.internal.utils.Assert;

public class MarkerInfo implements IMarkerSetElement, Cloneable {

	//
	protected static final long UNDEFINED_ID = -1;

	/** Marker identifier. */
	protected long id = UNDEFINED_ID;

	/** The type of this marker. */
	protected String type = null;

	/** The store of attributes for this marker. */
	protected Map attributes = null;
public Object getAttribute(String attributeName) {
	return attributes == null ? null : attributes.get(attributeName);
}
public Map getAttributes() {
	return getAttributes(true);
}
/**
 * See Object#clone.
 */
public Object clone() {
	try {
		MarkerInfo copy = (MarkerInfo)super.clone();
		//copy the attribute table contents
		copy.setAttributes(getAttributes());
		return copy;
	} catch (CloneNotSupportedException e) {
		//cannot happen because this class implements Cloneable
		return null;
	}
}
public Object[] getAttributes(String[] attributeNames) {
	Object[] result = new Object[attributeNames.length];
	for (int i = 0; i < attributeNames.length; i++)
		result[i] = getAttribute(attributeNames[i]);
	return result;
}
public Map getAttributes(boolean makeCopy) {
	if (attributes == null)
		return null;
	return makeCopy ? new MarkerAttributeMap(attributes) : attributes;
}
public long getId() {
	return id;
}
public String getType() {
	return type;
}
public void internalSetAttributes(Map map) {
	//the cast effectively acts as an assertion to make sure
	//the right kind of map is being used
	attributes = (MarkerAttributeMap)map;
}
/**
 * Returns whether the given object is a valid attribute value.
 */
protected static boolean isValidAttributeValue(Object value) {
	return value == null || value instanceof String || value instanceof Integer || value instanceof Boolean;
}
public void setAttribute(String attributeName, Object value) {
	Assert.isTrue(isValidAttributeValue(value));
	if (attributes == null) {
		if (value == null)
			return;
		else {
			attributes = new MarkerAttributeMap();
			attributes.put(attributeName, value);
		}
	} else {
		if (value == null) {
			attributes.remove(attributeName);
			if (attributes.isEmpty())
				attributes = null;
		} else {
			attributes.put(attributeName, value);
		}
	}
}
public void setAttributes(String[] attributeNames, Object[] values) {
	Assert.isTrue(attributeNames.length == values.length);
	for (int i = 0; i < attributeNames.length; i++)
		setAttribute(attributeNames[i], values[i]);
}
public void setAttributes(Map map) {
	if (map == null)
		attributes = null;
	else
		attributes = new MarkerAttributeMap(map);
}
public void setId(long value) {
	id = value;
}
public void setType(String value) {
	type = value;
}
}
