package org.eclipse.core.runtime.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * An object which represents the user-defined contents of a plug-in
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class PluginDescriptorModel extends PluginModel {

	// DTD properties (included in plug-in manifest)
	private String pluginClass = null;
	private PluginPrerequisiteModel[] requires = null;

	// transient properties (not included in plug-in manifest)
	private boolean enabled = true; // whether or not the plugin definition loaded ok
/**
 * Creates a new plug-in descriptor model in which all fields
 * are <code>null</code>.
 */
public PluginDescriptorModel() {
	super();
}
/*
 * Returns true if this plugin has all of it's prerequisites and is,
 * therefore enabled.
 */
public boolean getEnabled() {
	return enabled;
}
/**
 * Returns the fully qualified name of the Java class which implements
 * the runtime support for this plug-in.
 *
 * @return the name of this plug-in's runtime class or <code>null</code>.
 */
public String getPluginClass() {
	return pluginClass;
}
/**
 * Returns the unique identifier of the plug-in related to this model
 * or <code>null</code>.  
 * This identifier is a non-empty string and is unique 
 * within the plug-in registry.
 *
 * @return the unique identifier of the plug-in related to this model
 *		(e.g. <code>"com.example"</code>) or <code>null</code>. 
 */
public String getPluginId() {
	return getId();
}
/**
 * Returns the prerequisites of this plug-in.
 *
 * @return the prerequisites of this plug-in or <code>null</code>
 */
public PluginPrerequisiteModel[] getRequires() {
	return requires;
}
/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	super.markReadOnly();
	if (requires != null)
		for (int i = 0; i < requires.length; i++)
			requires[i].markReadOnly();
}
/*
 * Sets the value of the field 'enabled' to the parameter 'value'.
 * If this plugin is enabled (default) it is assumed to have all
 * of it's prerequisites.
 *
 * @param value set to false if this plugin should be disabled and
 * true otherwise.
 */
public void setEnabled(boolean value) {
	enabled = value;
}
/**
 * Sets the fully qualified name of the Java class which implements
 * the runtime support for this plug-in.
 * This object must not be read-only.
 *
 * @param value the name of this plug-in's runtime class.
 *		May be <code>null</code>.
 */
public void setPluginClass(String value) {
	assertIsWriteable();
	pluginClass = value;
}
/**
 * Sets the prerequisites of this plug-in.
 * This object must not be read-only.
 *
 * @param value the prerequisites of this plug-in.  May be <code>null</code>.
 */
public void setRequires(PluginPrerequisiteModel[] value) {
	assertIsWriteable();
	requires = value;
}
}
