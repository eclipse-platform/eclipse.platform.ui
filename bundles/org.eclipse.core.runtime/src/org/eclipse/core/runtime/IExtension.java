package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An extension declared in a plug-in.
 * All information is obtained from the declaring plug-in's 
 * manifest (<code>plugin.xml</code>) file. 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IExtension {
/**
 * Returns all configuration elements declared by this extension.
 * These elements are a direct reflection of the configuration 
 * markup supplied in the manifest (<code>plugin.xml</code>)
 * file for the plug-in that declares this extension.
 * Returns an empty array if this extension does not declare any
 * configuration elements.
 *
 * @return the configuration elements declared by this extension 
 */
public IConfigurationElement[] getConfigurationElements();
/** 
 * Returns the descriptor of the plug-in that declares this extension.
 *
 * @return the plug-in that declares this extension
 */
public IPluginDescriptor getDeclaringPluginDescriptor();
/**
 * Returns the unique identifier of the extension point
 * that this extension gets plugged into.
 *
 * @return the unique identifier of the relevant extension point
 */
public String getExtensionPointUniqueIdentifier();
/**
 * Returns a displayable label for this extension.
 * Returns the empty string if no label for this extension
 * is specified in the plug-in manifest file.
 * <p> Note that any translation specified in the plug-in manifest
 * file is automatically applied.
 * <p>
 *
 * @see IPluginDescriptor#getResourceString 
 *
 * @return a displayable string label for this extension,
 *    possibly the empty string
 */
public String getLabel();
/**
 * Returns the simple identifier of this extension, or <code>null</code>
 * if this extension does not have an identifier.
 * This identifier is specified in the plug-in manifest (<code>plugin.xml</code>) 
 * file as a non-empty string containing no period characters 
 * (<code>'.'</code>) and must be unique within the defining plug-in.
 *
 * @return the simple identifier of the extension (e.g. <code>"main"</code>)
 *  or <code>null</code>
 */
public String getSimpleIdentifier();
/**
 * Returns the unique identifier of this extension, or <code>null</code>
 * if this extension does not have an identifier.
 * If available, this identifier is unique within the plug-in registry, and
 * is composed of the identifier of the plug-in that declared
 * this extension and this extension's simple identifier.
 *
 * @return the unique identifier of the extension
 *    (e.g. <code>"com.example.acme.main"</code>), or <code>null</code>
 */
public String getUniqueIdentifier();
}
