package org.eclipse.update.internal.core;

import java.net.MalformedURLException;
import java.net.URL;
/**
 */
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.webdav.http.client.HttpClient;

public class UpdateManagerPlugin extends Plugin {
	private static UpdateManagerPlugin _pluginInstance = null;
	private	HttpClient client =null;
/**
 * UpdateManagerPlugin constructor comment.
 */
public UpdateManagerPlugin( IPluginDescriptor pluginDescriptor ) {
	super( pluginDescriptor );

	_pluginInstance = this;
}
/**
 * Returns the image descriptor of an image file relative to this plugin
 * @return org.eclipse.jface.resource.ImageDescriptor
 * @param strImage java.lang.String
 */
public static ImageDescriptor getImageDescriptor(String strImage) {

	UpdateManagerPlugin plugin = getPluginInstance();

	if (plugin != null) {

		// Obtain my plugin descriptor
		//----------------------------
		IPluginDescriptor pluginDescriptor = plugin.getDescriptor();

		// Determine where I am installed
		//-------------------------------
		URL path = pluginDescriptor.getInstallURL();

		// Add the relative file location to the install location
		// Create the image descriptor
		//-------------------------------------------------------
		URL urlFullPathString = null;

		try {
			urlFullPathString = new URL(path, strImage);
			return ImageDescriptor.createFromURL(urlFullPathString);
		}

		catch (MalformedURLException e) {
		}
	}
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (2001/03/06 10:34:52)
 * @return org.eclipse.update.internal.core.UpdateManagerPlugin
 */
public static UpdateManagerPlugin getPluginInstance() {
	return _pluginInstance;
}
/**
 * Shuts down this plug-in and discards all plug-in state.
 * <p>
 * This method should be re-implemented in subclasses that need to do something
 * when the plug-in is shut down.  Implementors should call the inherited method
 * to ensure that any system requirements can be met.
 * </p>
 * <p>
 * Plug-in shutdown code should be robust. In particular, this method
 * should always make an effort to shut down the plug-in. Furthermore,
 * the code should not assume that the plug-in was started successfully,
 * as this method will be invoked in the event of a failure during startup.
 * </p>
 * <p>
 * Note 1: If a plug-in has been started, this method will be automatically
 * invoked by the platform when the platform is shut down.
 * </p>
 * <p>
 * Note 2: This method is intended to perform simple termination
 * of the plug-in environment. The platform may terminate invocations
 * that do not complete in a timely fashion.
 * </p>
 * <b>Cliens must never explicitly call this method.</b>
 *
 * @exception CoreException if this method fails to shut down
 *   this plug-in 
 */
public void shutdown() throws CoreException {
	if(client!=null) client.close(); 
}
/**
 * Starts up this plug-in.
 * <p>
 * This method should be overridden in subclasses that need to do something
 * when this plug-in is started.  Implementors should call the inherited method
 * to ensure that any system requirements can be met.
 * </p>
 * <p>
 * If this method throws an exception, it is taken as an indication that
 * plug-in initialization has failed; as a result, the plug-in will not
 * be activated; moreover, the plug-in will be marked as disabled and 
 * ineligible for activation for the duration.
 * </p>
 * <p>
 * Plug-in startup code should be robust. In the event of a startup failure,
 * the plug-in's <code>shutdown</code> method will be invoked automatically,
 * in an attempt to close open files, etc.
 * </p>
 * <p>
 * Note 1: This method is automatically invoked by the platform 
 * the first time any code in the plug-in is executed.
 * </p>
 * <p>
 * Note 2: This method is intended to perform simple initialization 
 * of the plug-in environment. The platform may terminate initializers 
 * that do not complete in a timely fashion.
 * </p>
 * <b>Cliens must never explicitly call this method.</b>
 *
 * @exception CoreException if this plug-in did not start up properly
 */
public void startup() throws CoreException {
	// Setup HTTP client
	//------------------
	client = new HttpClient();
	client.setAuthenticator(new AuthorizationDatabase());
	URLHandler.setHttpClient(client);
}
}
