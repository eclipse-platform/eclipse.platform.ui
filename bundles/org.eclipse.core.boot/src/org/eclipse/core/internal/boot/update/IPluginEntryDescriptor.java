package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.net.*;

public interface IPluginEntryDescriptor  {
/**
 * A Plugin "entry" descriptor holds information about a plug-in
 * obtained from the containing Component's jar manifest file.  
 * 
 * 
 */
/**
 * Returns the name of the directory of this component in .install/.components
 * This is usually made up of compid_label_version_deltaId
 *
 *
 * @return the component's directory name in .install/.components
 */
public String getDirName();
/**
 * Returns a list of files packaged within this plug-in
 * These are the Name: entries in the component jar manifest
 *
 * @return an array of files packaged within this plug-in
 */
public String[] getFiles();
/**
 * Returns the URL of this plug-in's install directory. 
 * This is the ..../plugins/plugin-dir directory where plug-in
 * files are stored.
 *
 * @return the URL of this plug-in's install directory
 */
public URL getInstallURL() ;
/**
 * Returns a displayable label (name) for this plug-in.
 * Returns the empty string if no label for this plug-in
 * is specified in its install manifest file.
 * <p> Note that any translation specified in the install manifest
 * file is automatically applied.  LINDA
 * </p>
 *
 * @see #getResourceString 
 *
 * @return a displayable string label for this plug-in,
 *    possibly the empty string
 */
public String getLabel();
/**
 * Returns the unique identifier of this plug-in entry.
 * This identifier is a non-empty string and is unique 
 * within the registry.
 *
 * @return the unique identifier of the plug-in entry (e.g. <code>"com.example.myplugin"</code>)
 */
public String getUniqueIdentifier();
/**
 * Returns the plug-in version identifier.
 *
 * @return the plug-in version identifier
 */
public VersionIdentifier getVersionIdentifier();
/**
 * Returns the component or configuration version string.
 *
 * @return the component or configuration version string
 */
public String getVersionStr();
/**
 * Returns whether this plug-in has been selected for installation.  Default is false.
 *
 * @return whether this plug-in has been selected for installation.  (default is <code> "false" </code>)
 */
public boolean isSelected() ;
public void isSelected(boolean sel);
}
