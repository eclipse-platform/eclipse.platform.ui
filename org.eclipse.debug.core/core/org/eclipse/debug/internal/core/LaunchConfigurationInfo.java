package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.runtime.CoreException;
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
	 * Returns the <code>String</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @exception if the attribute with the given key exists
	 *  but is not a <code>String</code>
	 */
	protected String getStringAttribute(String key, String defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof String) {
				return (String)attr;
			} else {
				throw new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchConfigurationInfo.Attribute_{0}_is_not_of_type_java.lang.String._1"), new String[] {key}), null //$NON-NLS-1$
					)
				);
			}
		}
		return defaultValue;
	}
	
	/**
	 * Returns the <code>int</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @exception if the attribute with the given key exists
	 *  but is not an <code>int</code>
	 */
	protected int getIntAttribute(String key, int defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof Integer) {
				return ((Integer)attr).intValue();
			} else {
				throw new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchConfigurationInfo.Attribute_{0}_is_not_of_type_int._2"), new String[] {key}), null //$NON-NLS-1$
					)
				);
			}
		}
		return defaultValue;
	}
	
	/**
	 * Returns the <code>boolean</code> attribute with the
	 * given key or the given default value if undefined.
	 * 
	 * @return attribute specified by given key or the defaultValue
	 *  if undefined
	 * @exception if the attribute with the given key exists
	 *  but is not a <code>boolean</code>
	 */
	protected boolean getBooleanAttribute(String key, boolean defaultValue) throws CoreException {
		Object attr = getAttributeTable().get(key);
		if (attr != null) {
			if (attr instanceof Boolean) {
				return ((Boolean)attr).booleanValue();
			} else {
				throw new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchConfigurationInfo.Attribute_{0}_is_not_of_type_boolean._3"), new String[] {key}), null //$NON-NLS-1$
					)
				);
			}
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
		copy.setAttributeTable((HashMap)getAttributeTable().clone());
		return copy;
	}
	
	/**
	 * Sets the given attribute to the given value. Only
	 * working copy's should use this API.
	 * 
	 * @param key attribute key
	 * @param value attribuet value
	 */
	protected void setAttribute(String key, Object value) {
		getAttributeTable().put(key, value);
	}
	
	/**
	 * Returns the content of this info as XML
	 * 
	 * @return the content of this info as XML
	 * @exception IOException if an exception occurrs creating the XML
	 */
	protected String getAsXML() throws IOException {

		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement("launchConfiguration"); //$NON-NLS-1$
		doc.appendChild(configRootElement);
		
		configRootElement.setAttribute("type", getType().getIdentifier()); //$NON-NLS-1$
		
		Iterator keys = getAttributeTable().keySet().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			Object value = getAttributeTable().get(key);
			Element element = null;
			String valueString = null;
			if (value instanceof String) {
				valueString = (String)value;
				element = doc.createElement("stringAttribute"); //$NON-NLS-1$
			} else if (value instanceof Integer) {
				valueString = ((Integer)value).toString();
				element = doc.createElement("intAttribute"); //$NON-NLS-1$
			} else if (value instanceof Boolean) {
				valueString = ((Boolean)value).toString();
				element = doc.createElement("booleanAttribute"); //$NON-NLS-1$
			}
			element.setAttribute("key", key); //$NON-NLS-1$
			element.setAttribute("value", valueString); //$NON-NLS-1$
			configRootElement.appendChild(element);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		serializer.asDOMSerializer().serialize(doc);
		return writer.toString();
			
	}
	
	protected void initializeFromXML(Element root) throws CoreException {
		DebugException invalidFormat = 
			new DebugException(
				new Status(
				 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("LaunchConfigurationInfo.Invalid_launch_configuration_XML._10"), null //$NON-NLS-1$
				)
			);		
			
		if (!root.getNodeName().equalsIgnoreCase("launchConfiguration")) { //$NON-NLS-1$
			throw invalidFormat;
		}
		
		// read type
		String id = root.getAttribute("type"); //$NON-NLS-1$
		if (id == null) {
			throw invalidFormat;
		} else {
			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(id);
			if (type == null) {
				throw invalidFormat;
			}
			setType(type);
		}
		
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				String nodeName = entry.getNodeName();
				String key = entry.getAttribute("key"); //$NON-NLS-1$
				if (key == null) {
					throw invalidFormat;
				}
				String value = entry.getAttribute("value"); //$NON-NLS-1$
				if (value == null) {
					throw invalidFormat;
				}
				if (nodeName.equalsIgnoreCase("stringAttribute")) { //$NON-NLS-1$
					setAttribute(key, value);
				} else if (nodeName.equalsIgnoreCase("intAttribute")) { //$NON-NLS-1$
					setAttribute(key, new Integer(value));
				} else if (nodeName.equalsIgnoreCase("booleanAttribute"))  { //$NON-NLS-1$
					setAttribute(key, new Boolean(value));
				}
			}
		}
	}	
	
}

