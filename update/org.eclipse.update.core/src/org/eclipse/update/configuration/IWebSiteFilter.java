package org.eclipse.update.configuration;

import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Web site filter interface. Clients implement this
 * interface and register it with the <samp>org.eclipse.update.core.
 * webFilter</samp> extension point in order to filter the
 * document based on the currently active configuration.
 */

public interface IWebSiteFilter {
/**
 * Sets the configuration element obtained from the plug-in registry.
 * Implementors can use this object to get access to the attributes
 * defined in the plug-in manifest file.
 * @param config the associated configuration element
 */
	public void setConfigurationElement(IConfigurationElement config);	
/**
 * Filters the data from the input stream and passes it to the
 * output stream. Filters can remove or insert certain portions 
 * of the document or insert the new ones.
 * @param input the incoming document stream
 * @param output the resulting document stream
 * @throws CoreException if something irregular happens during the filtering
 */
	public void filter(InputStream input, OutputStream output) throws CoreException;

}