package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An object which represents the user-defined contents of an extension
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */

public class ConfigurationElementModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String value = null;
	private ConfigurationPropertyModel[] properties = null;
	private ConfigurationElementModel[] children = null;

	// transient properties (not included in plug-in manifest)
	private Object parent = null; // parent element or declaring extension
/**
 * Creates a new configuration element model in which all fields
 * are <code>null</code>.
 */
public ConfigurationElementModel() {}
/**
 * Returns the element which contains this element.  If this element
 * is an immediate child of an extension, the
 * returned value can be downcast to <code>ExtensionModel</code>.
 * Otherwise the returned value can be downcast to 
 * <code>ConfigurationElementModel</code>.
 *
 * @return the parent of this configuration element
 *  or <code>null</code>
 */
public Object getParent() {
	return parent;
}
/**
 * Returns the extension in which this configuration element is declared.
 * If this element is a top-level child of an extension, the returned value
 * is equivalent to <code>getParent</code>.
 *
 * @return the extension in which this configuration element is declared
 *  or <code>null</code>
 */
public ExtensionModel getParentExtension() {
	Object p = getParent();
	while (p != null && p instanceof ConfigurationElementModel)
		p = ((ConfigurationElementModel) p).getParent();
	return (ExtensionModel) p;
}
/**
 * Returns the properties associated with this element.
 *
 * @return the properties associated with this element
 *  or <code>null</code>
 */
public ConfigurationPropertyModel[] getProperties() {
	return properties;
}
/**
 * Returns this element's sub-elements.
 *
 * @return the sub-elements of this element or <code>null</code>
 */
public ConfigurationElementModel[] getSubElements() {
	return children;
}
/**
 * Returns the value of this element.
 * 
 * @return the value of this element or <code>null</code>
 */
public String getValue() {
	return value;
}
/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	super.markReadOnly();
	if (children != null)
		for (int i = 0; i < children.length; i++)
			children[i].markReadOnly();
	if (properties != null)
		for (int i = 0; i < properties.length; i++)
			properties[i].markReadOnly();
}
/**
 * Sets the parent of this element.  The supplied parent is either
 * an <code>ExtensionModel</code>, if this element is to be a 
 * direct child of an extension, or another <code>ConfigurationElement</code>.
 * This object must not be read-only.
 *
 * @param value the new parent of this element.  May be <code>null</code>.
 */
public void setParent(Object value) {
	assertIsWriteable();
	parent = value;
}
/**
 * Sets the properties associated with this element.  This object must not be read-only.
 *
 * @param value the properties to associate with this element.  May be <code>null</code>.
 */
public void setProperties(ConfigurationPropertyModel[] value) {
	assertIsWriteable();
	properties = value;
}
/**
 * Sets configuration elements contained by this element
 * This object must not be read-only.
 *
 * @param value the configuration elements to be associated with this element.  
 *		May be <code>null</code>.
 */
public void setSubElements(ConfigurationElementModel[] value) {
	assertIsWriteable();
	children = value;
}
/**
 * Sets the value of this element.  This object must not be read-only.
 * 
 * @param value the new value of this element.  May be <code>null</code>.
 */
public void setValue(String value) {
	assertIsWriteable();
	this.value = value;
}
}
