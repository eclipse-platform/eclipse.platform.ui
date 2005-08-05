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
package org.eclipse.debug.internal.core;

 
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
/**
 * The information associated with a launch configuration
 * handle.
 */
public class LaunchConfigurationInfo {

	/**
	 * This configurations attribute table.
	 * Keys are <code>String</code>s and values
	 * are one of <code>String</code>, <code>Integer</code>,
	 * or <code>Boolean</code>.
	 */
	private HashMap fAttributes;
	
	/**
	 * This launch configuration's type
	 */
	private ILaunchConfigurationType fType;
	
	/**
	 * Constructs a new empty info
	 */
	protected LaunchConfigurationInfo() {
		setAttributeTable(new HashMap(10));
	}
	
	/**
	 * Returns this configuration's attribute table.
	 * 
	 * @return attribute table
	 */
	private HashMap getAttributeTable() {
		return fAttributes;
	}

	/**
	 * Sets this configuration's attribute table.
	 * 
	 * @param table attribute table
	 */	
	private void setAttributeTable(HashMap table) {
		fAttributes = table;
	}
	
	/**
	 * Sets the attributes in this info to those in the given map.
	 * 
	 * @param map
	 */
	protected void setAttributes(Map map) {
		if (map == null) {
			setAttributeTable(new HashMap());
			return;
		}
		Set entrySet = map.entrySet();
		HashMap attributes = new HashMap(entrySet.size());
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			attributes.put(entry.getKey(), entry.getValue());
		}
		setAttributeTable(attributes);
	}
	
	/**
	 * Returns the <code>String</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @throws CoreException if the attribute with the given key exists
	 *  but is not a <code>String</code>
	 */
	protected String getStringAttribute(String key, String defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof String) {
				return (String)attr;
			} 
			throw new DebugException(
				new Status(
				 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_Attribute__0__is_not_of_type_java_lang_String__1, new String[] {key}), null 
				)
			);
		}
		return defaultValue;
	}
	
	/**
	 * Returns the <code>int</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @throws CoreException if the attribute with the given key exists
	 *  but is not an <code>int</code>
	 */
	protected int getIntAttribute(String key, int defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof Integer) {
				return ((Integer)attr).intValue();
			} 
			throw new DebugException(
				new Status(
				 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_Attribute__0__is_not_of_type_int__2, new String[] {key}), null 
				)
			);
		}
		return defaultValue;
	}
	
	/**
	 * Returns the <code>boolean</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @throws CoreException if the attribute with the given key exists
	 *  but is not a <code>boolean</code>
	 */
	protected boolean getBooleanAttribute(String key, boolean defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof Boolean) {
				return ((Boolean)attr).booleanValue();
			} 
			throw new DebugException(
				new Status(
				 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_Attribute__0__is_not_of_type_boolean__3, new String[] {key}), null 
				)
			);
		}
		return defaultValue;
	}
	
	/**
	 * Returns the <code>java.util.List</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @throws CoreException if the attribute with the given key exists
	 *  but is not a <code>java.util.List</code>
	 */
	protected List getListAttribute(String key, List defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof List) {
				return (List)attr;
			} 
			throw new DebugException(
				new Status(
				 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_Attribute__0__is_not_of_type_java_util_List__1, new String[] {key}), null 
				)
			);
		}
		return defaultValue;
	}
	
	/**
	 * Returns the <code>java.util.Map</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @throws CoreException if the attribute with the given key exists
	 *  but is not a <code>java.util.Map</code>
	 */
	protected Map getMapAttribute(String key, Map defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof Map) {
				return (Map)attr;
			} 
			throw new DebugException(
				new Status(
				 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_Attribute__0__is_not_of_type_java_util_Map__1, new String[] {key}), null 
				)
			);
		}
		return defaultValue;
	}
	
	/** 
	 * Sets this configuration's type.
	 * 
	 * @param type launch configuration type
	 */
	protected void setType(ILaunchConfigurationType type) {
		fType = type;
	}
	
	/** 
	 * Returns this configuration's type.
	 * 
	 * @return launch configuration type
	 */
	protected ILaunchConfigurationType getType() {
		return fType;
	}	
	
	
	/**
	 * Returns a copy of this info object
	 * 
	 * @return copy of this info
	 */
	protected LaunchConfigurationInfo getCopy() {
		LaunchConfigurationInfo copy = new LaunchConfigurationInfo();
		copy.setType(getType());
		copy.setAttributeTable(getAttributes());
		return copy;
	}
	
	/**
	 * Returns a copy of this info's attribute map.
	 * 
	 * @return a copy of this info's attribute map
	 */
	protected HashMap getAttributes() {
		return (HashMap)getAttributeTable().clone();
	}
	
	/**
	 * Sets the given attribute to the given value. Only
	 * working copy's should use this API.
	 * 
	 * @param key attribute key
	 * @param value attribute value
	 */
	protected void setAttribute(String key, Object value) {
		if (value == null) {
			getAttributeTable().remove(key);
		} else {
			getAttributeTable().put(key, value);
		}
	}
	
	/**
	 * Returns the content of this info as XML
	 * 
	 * @return the content of this info as XML
	 * @throws CoreException if a attribute has been set with a null key
	 * @throws IOException if an exception occurs creating the XML
	 * @throws ParserConfigurationException if an exception occurs creating the XML
	 * @throws TransformerException if an exception occurs creating the XML
	 */
	protected String getAsXML() throws CoreException, IOException, ParserConfigurationException, TransformerException {

		Document doc = LaunchManager.getDocument();
		Element configRootElement = doc.createElement("launchConfiguration"); //$NON-NLS-1$
		doc.appendChild(configRootElement);
		
		configRootElement.setAttribute("type", getType().getIdentifier()); //$NON-NLS-1$
		
		Iterator keys = getAttributeTable().keySet().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			if (key == null) {
				throw new DebugException(
					new Status(
						IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
						DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationInfo_36, null 
					)
				);
			}
			Object value = getAttributeTable().get(key);
			if (value == null) {
				continue;
			}
			Element element = null;
			String valueString = null;
			if (value instanceof String) {
				valueString = (String)value;
				element = createKeyValueElement(doc, "stringAttribute", key, valueString); //$NON-NLS-1$
			} else if (value instanceof Integer) {
				valueString = ((Integer)value).toString();
				element = createKeyValueElement(doc, "intAttribute", key, valueString); //$NON-NLS-1$
			} else if (value instanceof Boolean) {
				valueString = ((Boolean)value).toString();
				element = createKeyValueElement(doc, "booleanAttribute", key, valueString); //$NON-NLS-1$
			} else if (value instanceof List) {				
				element = createListElement(doc, "listAttribute", key, (List)value); //$NON-NLS-1$
			} else if (value instanceof Map) {				
				element = createMapElement(doc, "mapAttribute", key, (Map)value); //$NON-NLS-1$
			}			
			configRootElement.appendChild(element);
		}

		return LaunchManager.serializeDocument(doc);
	}
	
	/**
	 * Helper method that creates a 'key value' element of the specified type with the 
	 * specified attribute values.
	 */
	protected Element createKeyValueElement(Document doc, String elementType, String key, String value) {
		Element element = doc.createElement(elementType);
		element.setAttribute("key", key); //$NON-NLS-1$
		element.setAttribute("value", value); //$NON-NLS-1$
		return element;
	}
	
	protected Element createListElement(Document doc, String elementType, String listKey, List list) {
		Element listElement = doc.createElement(elementType);
		listElement.setAttribute("key", listKey); //$NON-NLS-1$
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			String value = (String) iterator.next();
			Element element = doc.createElement("listEntry"); //$NON-NLS-1$
			element.setAttribute("value", value); //$NON-NLS-1$
			listElement.appendChild(element);
		}		
		return listElement;
	}
	
	protected Element createMapElement(Document doc, String elementType, String mapKey, Map map) {
		Element mapElement = doc.createElement(elementType);
		mapElement.setAttribute("key", mapKey); //$NON-NLS-1$
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			Element element = doc.createElement("mapEntry"); //$NON-NLS-1$
			element.setAttribute("key", key); //$NON-NLS-1$
			element.setAttribute("value", value); //$NON-NLS-1$
			mapElement.appendChild(element);
		}		
		return mapElement;		
	}
	
	protected void initializeFromXML(Element root) throws CoreException {
		if (!root.getNodeName().equalsIgnoreCase("launchConfiguration")) { //$NON-NLS-1$
			throw getInvalidFormatDebugException();
		}
		
		// read type
		String id = root.getAttribute("type"); //$NON-NLS-1$
		if (id == null) {
			throw getInvalidFormatDebugException();
		} 
		
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(id);
		if (type == null) {
			String message= MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_missing_type, new Object[]{id}); 
			throw new DebugException(
					new Status(
					 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
					 DebugException.MISSING_LAUNCH_CONFIGURATION_TYPE, message, null)
				);
		}
		setType(type);
		
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short nodeType = node.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String nodeName = element.getNodeName();
				
				if (nodeName.equalsIgnoreCase("stringAttribute")) { //$NON-NLS-1$
					setStringAttribute(element);
				} else if (nodeName.equalsIgnoreCase("intAttribute")) { //$NON-NLS-1$
					setIntegerAttribute(element);
				} else if (nodeName.equalsIgnoreCase("booleanAttribute"))  { //$NON-NLS-1$
					setBooleanAttribute(element);
				} else if (nodeName.equalsIgnoreCase("listAttribute")) {   //$NON-NLS-1$
					setListAttribute(element);					
				} else if (nodeName.equalsIgnoreCase("mapAttribute")) {    //$NON-NLS-1$
					setMapAttribute(element);										
				}
			}
		}
	}	
	
	protected void setStringAttribute(Element element) throws CoreException {
		String key = getKeyAttribute(element);
		String value = getValueAttribute(element);
		setAttribute(key, value);
	}
	
	protected void setIntegerAttribute(Element element) throws CoreException {
		String key = getKeyAttribute(element);
		String value = getValueAttribute(element);
		setAttribute(key, new Integer(value));
	}
	
	protected void setBooleanAttribute(Element element) throws CoreException {
		String key = getKeyAttribute(element);
		String value = getValueAttribute(element);
		setAttribute(key, Boolean.valueOf(value));
	}
	
	protected void setListAttribute(Element element) throws CoreException {
		String listKey = element.getAttribute("key");  //$NON-NLS-1$
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		List list = new ArrayList(entryCount);
		for (int i = 0; i < entryCount; i++) {
			Node node = nodeList.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element subElement = (Element) node;
				String nodeName = subElement.getNodeName();				
				if (!nodeName.equalsIgnoreCase("listEntry")) { //$NON-NLS-1$
					throw getInvalidFormatDebugException();
				}
				String value = getValueAttribute(subElement);
				list.add(value);
			}
		}
		setAttribute(listKey, list);
	}
		
	protected void setMapAttribute(Element element) throws CoreException {
		String mapKey = element.getAttribute("key");  //$NON-NLS-1$
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		Map map = new HashMap(entryCount);
		for (int i = 0; i < entryCount; i++) {
			Node node = nodeList.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element subElement = (Element) node;
				String nodeName = subElement.getNodeName();				
				if (!nodeName.equalsIgnoreCase("mapEntry")) { //$NON-NLS-1$
					throw getInvalidFormatDebugException();
				}
				String key = getKeyAttribute(subElement);
				String value = getValueAttribute(subElement);
				map.put(key, value);
			}
		}
		setAttribute(mapKey, map);
	}
		
	protected String getKeyAttribute(Element element) throws CoreException {
		String key = element.getAttribute("key");   //$NON-NLS-1$
		if (key == null) {
			throw getInvalidFormatDebugException();
		}
		return key;
	}
	
	protected String getValueAttribute(Element element) throws CoreException {
		String value = element.getAttribute("value");   //$NON-NLS-1$
		if (value == null) {
			throw getInvalidFormatDebugException();
		}
		return value;
	}
	
	protected DebugException getInvalidFormatDebugException() {
		return 
			new DebugException(
				new Status(
				 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationInfo_Invalid_launch_configuration_XML__10, null 
				)
			);
	}
	
	/**
	 * Two <code>LaunchConfigurationInfo</code> objects are equal if and only if they have the
	 * same type and they have the same set of attributes with the same values.
	 * 
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		
		// Make sure it's a LaunchConfigurationInfo object
		if (!(obj instanceof LaunchConfigurationInfo)) {
			return false;
		}
		
		// Make sure the types are the same
		LaunchConfigurationInfo other = (LaunchConfigurationInfo) obj;
		if (!fType.getIdentifier().equals(other.getType().getIdentifier())) {
			return false;
		}
		
		// Make sure the attributes are the same
		return compareAttributes(fAttributes, other.getAttributeTable());
	}
	
	/**
	 * Returns whether the two attribute maps are equal, consulting
	 * registered comparator extensions.
	 * 
	 * @param map1 attribute map
	 * @param map2 attribute map
	 * @return whether the two attribute maps are equal
	 */
	protected boolean compareAttributes(HashMap map1, HashMap map2) {
		LaunchManager manager = (LaunchManager)DebugPlugin.getDefault().getLaunchManager();
		if (map1.size() == map2.size()) {
			Iterator attributes = map1.keySet().iterator();
			while (attributes.hasNext()) {
				String key = (String)attributes.next();
				Object attr1 = map1.get(key);
				Object attr2 = map2.get(key);
				if (attr2 == null) {
					return false;
				}
				Comparator comp = manager.getComparator(key);
				if (comp == null) {
					if (!attr1.equals(attr2)) {
						return false;
					}
				} else {
					if (comp.compare(attr1, attr2) != 0) {
						return false;
					}
				}
			}
			return true;	
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fType.hashCode() + fAttributes.size();
	}

}

