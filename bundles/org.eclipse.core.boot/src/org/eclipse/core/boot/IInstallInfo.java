package org.eclipse.core.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
 
/**
 * Interface for accessing selected installation information. 
 * In particular, the interface methods can be used to access 
 * information about the installed configurations and components.
 *
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @deprecated Interface will be removed before milestone 3. 
 * Being functionally replaced by @see IPlatformConfiguration
 */
public interface IInstallInfo {
/**
 * Returns the configuration identifier of the installed application.
 *
 * @return a configuration identifier, or <code>null</code> if no
 *      application is installed.
 * @see #getConfigurationInstallURLFor
 * @deprecated Interface will be removed before milestone 3. 
 * Being functionally replaced by @see IPlatformConfiguration
 */
public String getApplicationConfigurationIdentifier();
/**
 * Returns URL at which the identified component installation information
 * is located.
 *
 * @param componentId The component identifier returned by other methods
 *      in this interface.
 * @return the URL indicating where the component information is located.
 * @see #getInstalledComponentIdentifiers
 * @deprecated
 */
public URL getComponentInstallURLFor(String componentId);
/**
 * Returns URL at which the identified configuration installation information
 * is located.
 *
 * @param configurationId The configuration identifier returned by other 
 *      methods in this interface.
 * @return the URL indicating where the configuration information is located.
 * @see #getInstalledConfigurationIdentifiers
 * @deprecated 
 */
public URL getConfigurationInstallURLFor(String configurationId);
/**
 * Returns an array of identifiers of the installed components. 
 *
 * @return an array of component identifiers, or an empty array
 *      if no components are installed.
 * @see #getComponentInstallURLFor
 * @deprecated
 */
public String[] getInstalledComponentIdentifiers();
/**
 * Returns an array of identifiers of the installed configurations.
 *
 * @return an array of configuration identifiers, or an empty array
 *      if no configurations are installed.
 * @see #getConfigurationInstallURLFor
 * @deprecated
 */
public String[] getInstalledConfigurationIdentifiers();
}
