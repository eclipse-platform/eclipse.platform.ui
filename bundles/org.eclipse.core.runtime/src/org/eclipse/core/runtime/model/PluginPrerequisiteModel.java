package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

 import org.eclipse.core.runtime.PluginVersionIdentifier;	// for javadoc
/**
 * An object which represents the relationship between a plug-in and a
 * prerequisite plug-in in the dependent's plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class PluginPrerequisiteModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String plugin = null;
	private String version = null;
	private boolean match = false;
	private boolean export = false;
	private String resolvedVersion = null;
	private boolean optional = false;
/**
 * Creates a new plug-in prerequisite model in which all fields
 * are <code>null</code>.
 */
public PluginPrerequisiteModel() {
	super();
}
/**
 * Returns whether or not the code in this pre-requisite is exported.
 *
 * @return whether or not the code in this pre-requisite is exported
 */
public boolean getExport() {
	return export;
}
/**
 * Returns whether or not this pre-requisite requires an exact match.
 *
 * @return whether or not this pre-requisite requires an exact match
 */
public boolean getMatch() {
	return match;
}
/**
 * Returns whether this pre-requisite is optional.
 *
 * @return whether this pre-requisite is optional
 */
public boolean getOptional() {
	return optional;
}
/**
 * Returns the plug-in identifier of the prerequisite plug-in.
 * 
 * @return the plug-in identifier or <code>null</code>
 */
public String getPlugin() {
	return plugin;
}
/**
 * Returns the resolved version of the prerequisite plug-in.  The
 * returned value is in the format specified by <code>PluginVersionIdentifier</code>.
 *
 * @return the version of the prerequisite plug-in
 * @see PluginVersionIdentifier
 */
public String getResolvedVersion() {
	return resolvedVersion;
}
/**
 * Returns the version name of this plug-in.
 *
 * @return the version name of this plug-in or <code>null</code>
 */
public String getVersion() {
	return version;
}
/**
 * Sets whether or not the code in this pre-requisite is exported.
 * This object must not be read-only.
 *
 * @param value whether or not the code in this pre-requisite is exported
 */
public void setExport(boolean value) {
	assertIsWriteable();
	export = value;
}
/**
 * Sets whether or not this pre-requisite requires an exact match.
 * This object must not be read-only.
 *
 * @param value whether or not this pre-requisite requires an exact match
 */
public void setMatch(boolean value) {
	assertIsWriteable();
	match = value;
}
/**
 * Sets whether this pre-requisite is optional.
 * This object must not be read-only.
 *
 * @param value whether this pre-requisite is optional
 */
public void setOptional(boolean value) {
	assertIsWriteable();
	optional = value;
}
/**
 * Sets the plug-in identifier of this prerequisite plug-in.
 * This object must not be read-only.
 * 
 * @param value the prerequisite plug-in identifier.  May be <code>null</code>.
 */
public void setPlugin(String value) {
	assertIsWriteable();
	plugin = value;
}
/**
 * Sets the resolved version of the prerequisite plug-in.  The
 * given value is in the format specified by <code>PluginVersionIdentifier</code>.
 *
 * @param value the version of the prerequisite plug-in
 * @see PluginVersionIdentifier
 */
public void setResolvedVersion(String value) {
	assertIsWriteable();
	resolvedVersion = value;
}
/**
 * Sets the version name of this plug-in prerequisite.
 * This object must not be read-only.
 *
 * @param value the version name of this plug-in prerequisite.
 *		May be <code>null</code>.
 */
public void setVersion(String value) {
	assertIsWriteable();
	version = value;
}
}
