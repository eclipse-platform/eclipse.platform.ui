package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.net.*;

public interface IManifestDescriptor extends IInstallable {

/**
 * Returns the Discovery URL-Name pair that matches the  parameter,
 * or <code>null</code> if there is no such Discovery URL-Name pair.
 *
 * @param url the Discovery URL to search for (e.g. <code>"http://www.example.com"</code>).
 * @return the Discovery URL-Name pair, or <code>null</code> if not found
 */
public IURLNamePair getDiscoveryURL(URL url);
/**
 * Returns the list of URL-Name pairs where new items related to this Product or Component can be found
 *
 * @return the Discovery URL-Name pairs of this product or component
 */
public IURLNamePair[] getDiscoveryURLs();
/**
 * Returns the URL of this product or component's install manifest file. 
 * e.g. ..../.install/.components/compid_label_version/install.xml
 *
 * @return the URL of this product or component's install manifest file. 
 */
public URL getInstallManifestURL() ;
/**
 * Returns the URL of this product/component's install directory. 
 * This is the .install/.components/compid_label_version or
 * .install/.products/prodid_label_version directory where 
 * product and component manifest files are stored.
 *
 * @return the URL of this product or component's install directory
 */
public URL getInstallURL() ;
/**
 * Returns the manifest type.  Can be "Product" or "Component"
 *
 * @return the manifest type (<code> "Product" </code> or <code> "Component" </code> )
 */
public String getManifestType();
/**
 * Returns the URL of this product or component's jar manifest file. 
 * e.g. ..../.install/.components/compid_label_version/META-INF/MANIFEST.MF 
 *
 * @return the URL of this product or component's jar manifest file. 
 */
public URL getManifestURL() ;
/**
 * Returns the manifest version identifier.
 *
 * @return the manifest version identifier
 */
public String getManifestVersion();
public IUMRegistry getUMRegistry() ;
/**
 * Returns the unique identifier of this component or configuration.
 * This identifier is a non-empty string and is unique 
 * within the Update Manager.
 *
 * @return the unique identifier of the component or configuration (e.g. <code>"SGD8-TR62-872F-AFCD"</code>)
 */
public String getUniqueIdentifier();
/**
 * Returns the Update URL-Name pair that matches the  parameter,
 * or <code>null</code> if there is no such Update URL-Name pair.
 *
 * @param url the Update URL to search for (e.g. <code>"http://www.example.com"</code>).
 * @return the Update URL-Name pair, or <code>null</code> if not found
 */
public IURLNamePair getUpdateURL(URL url);
/**
 * Returns the list of URL-Name pairs where updates to this product or component can be found.
 *
 * @return the update URL-Name pairs of this product or component
 */
public IURLNamePair[] getUpdateURLs();
/**
 * Returns the component or configuration version identifier.
 *
 * @return the component or configuration version identifier
 */
public VersionIdentifier getVersionIdentifier();
/**
 * Returns the component or configuration version string.
 *
 * @return the component or configuration version string
 */
public String getVersionStr();
}
