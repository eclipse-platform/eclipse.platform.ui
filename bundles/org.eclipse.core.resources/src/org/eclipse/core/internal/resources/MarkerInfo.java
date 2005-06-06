/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.eclipse.core.internal.utils.*;

public class MarkerInfo implements IMarkerSetElement, Cloneable, IStringPoolParticipant {

	//
	protected static final long UNDEFINED_ID = -1;

	/** The store of attributes for this marker. */
	protected Map attributes = null;

	/** The creation time for this marker. */
	protected long creationTime = 0;

	/** Marker identifier. */
	protected long id = UNDEFINED_ID;

	/** The type of this marker. */
	protected String type = null;

	/**
	 * Returns whether the given object is a valid attribute value.
	 */
	protected static void checkValidAttribute(Object value) {
		boolean isString = value instanceof String;
		Assert.isTrue(value == null || isString|| value instanceof Integer || value instanceof Boolean);
		
		if (!isString)
			return;
		//we cannot write attributes whose UTF encoding exceeds 65535 bytes.
		String valueString = (String)value;
		//optimized test based on maximum 3 bytes per character
		if (valueString.length() < 21000)
			return;
		byte[] bytes;
		try {
			bytes = valueString.getBytes(("UTF-8"));//$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			//cannot validate further
			return;
		}
		if (bytes.length > 65535) { 
			String msg = "Marker property value is too long: " + valueString.substring(0, 10000); //$NON-NLS-1$
			Assert.isTrue(false, msg);
		}
	}

	public MarkerInfo() {
		super();
	}

	/**
	 * See Object#clone.
	 */
	public Object clone() {
		try {
			MarkerInfo copy = (MarkerInfo) super.clone();
			//copy the attribute table contents
			copy.attributes = getAttributes(true);
			return copy;
		} catch (CloneNotSupportedException e) {
			//cannot happen because this class implements Cloneable
			return null;
		}
	}

	public Object getAttribute(String attributeName) {
		return attributes == null ? null : attributes.get(attributeName);
	}

	public Map getAttributes() {
		return getAttributes(true);
	}

	public Map getAttributes(boolean makeCopy) {
		if (attributes == null)
			return null;
		return makeCopy ? new MarkerAttributeMap(attributes) : attributes;
	}

	public Object[] getAttributes(String[] attributeNames) {
		Object[] result = new Object[attributeNames.length];
		for (int i = 0; i < attributeNames.length; i++)
			result[i] = getAttribute(attributeNames[i]);
		return result;
	}

	public long getCreationTime() {
		return creationTime;
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
		attributes = (MarkerAttributeMap) map;
	}

	public void setAttribute(String attributeName, Object value) {
		checkValidAttribute(value);
		if (attributes == null) {
			if (value == null)
				return;
			attributes = new MarkerAttributeMap();
			attributes.put(attributeName, value);
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

	public void setAttributes(Map map) {
		if (map == null)
			attributes = null;
		else
			attributes = new MarkerAttributeMap(map);
	}

	public void setAttributes(String[] attributeNames, Object[] values) {
		Assert.isTrue(attributeNames.length == values.length);
		for (int i = 0; i < attributeNames.length; i++)
			setAttribute(attributeNames[i], values[i]);
	}

	public void setCreationTime(long value) {
		creationTime = value;
	}

	public void setId(long value) {
		id = value;
	}

	public void setType(String value) {
		type = value;
	}

	/* (non-Javadoc
	 * Method declared on IStringPoolParticipant
	 */
	public void shareStrings(StringPool set) {
		type = set.add(type);
		Map map = attributes;
		if (map instanceof IStringPoolParticipant)
			((IStringPoolParticipant) map).shareStrings(set);
	}
}
