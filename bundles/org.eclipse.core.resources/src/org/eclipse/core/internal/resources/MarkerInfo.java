package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.internal.utils.Assert;
import java.util.*;

public class MarkerInfo implements IMarkerSetElement {

	//
	protected static final long UNDEFINED_ID = -1;

	/** Marker identifier. */
	protected long id = UNDEFINED_ID;

	/** The type of this marker. */
	protected String type = null;

	/** The store of attributes for this marker. */
	protected HashMap attributes = null;
public Object getAttribute(String attributeName) {
	return attributes == null ? null : attributes.get(attributeName);
}
public Map getAttributes() {
	return getAttributes(true);
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
	return makeCopy ? (Map) attributes.clone() : attributes;
}
public long getId() {
	return id;
}
public String getType() {
	return type;
}
public void internalSetAttributes(Map map) {
	attributes = (HashMap) map;
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
			attributes = new HashMap(1);
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
		attributes = new HashMap(map);
}
public void setId(long value) {
	id = value;
}
public void setType(String value) {
	type = value;
}
}
