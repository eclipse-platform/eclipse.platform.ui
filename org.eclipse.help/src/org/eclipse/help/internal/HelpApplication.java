package org.eclipse.help.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;

/**
 * Facade for Eclipse servlet
 */
public class HelpApplication implements IPlatformRunnable
{
	private static final String HELP_KEY = "org.eclipse.ui.help";
	private static final String HELP_SYSTEM_EXTENSION_ID =
		"org.eclipse.help.support";
	private static final String HELP_SYSTEM_CLASS_ATTRIBUTE = "class";
	private static IHelp helpSupport = null;

	/**
	 * To be called by the servlets.
	 */
	private URLConnection openConnection(String urlStr)
	{
		try
		{
			if (urlStr != null && urlStr.length() > 1)
			{
				URL url = new URL(urlStr);
				return url.openConnection();
			}
		}
		catch (IOException e)
		{
			// for debugging purposes
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * To be called by the standalone interface to display help
	 */
	private Boolean displayHelp(String href)
	{
		if (helpSupport == null)
			initializeHelpSupport();

		if (helpSupport != null)
		{
			if (href == null)
				helpSupport.displayHelp();
			else
				helpSupport.displayHelpResource(href);

			return new Boolean(true);
		}
		else
			return new Boolean(false);
	}
	
	/**
	 * To be called by the standalone interface to display help resources
	 */
	private Boolean displayHelpResource(String href)
	{
		if (helpSupport == null)
			initializeHelpSupport();

		if (helpSupport != null)
		{
			helpSupport.displayHelpResource(href);
			return new Boolean(true);
		}
		else
			return new Boolean(false);
	}
	
	/**
	 * To be called by the standalone interface to display context sensitive help
	 */
	private Boolean displayContext(String contextId, Integer X, Integer Y)
	{
		//System.out.println("display Context in facade " + contextId + X + Y);
		if (helpSupport == null)
			initializeHelpSupport();

		if (helpSupport != null)
		{
			int x = X.intValue();
			int y = Y.intValue();
			
			helpSupport.displayContext(contextId, x, y);
			return new Boolean(true);
		}
		else
			return new Boolean(false);

	}
	

	/**
	* Initializes the help support system by getting an instance via the extension
	* point.
	*/
	private static void initializeHelpSupport()
	{
		if (helpSupport != null)
			return;

		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IExtensionPoint point =
			pluginRegistry.getExtensionPoint(HELP_SYSTEM_EXTENSION_ID);
		if (point != null)
		{
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0)
			{
				// There should only be one extension/config element so we just take the first
				IConfigurationElement[] elements = extensions[0].getConfigurationElements();
				if (elements.length != 0)
				{ // Instantiate the app server
					try
					{
						;
						helpSupport =
							(IHelp) elements[0].createExecutableExtension(HELP_SYSTEM_CLASS_ATTRIBUTE);
					}
					catch (CoreException e)
					{
						// may need to change this
						HelpPlugin.getDefault().getLog().log(e.getStatus());
					}
				}
			}
		}

	}
	/**
	 * @param args array of objects
	 *  first is String command
	 *  rest are command parameters
	 */
	public Object run(Object args)
	{
		if (args == null || !(args instanceof Object[]))
			return null;
		Object[] argsArray = (Object[]) args;
		if (argsArray.length < 1
			|| !(argsArray[0] instanceof String)
			|| argsArray[0] == null)
			return null;
			
		String command = (String) argsArray[0];

		if ("openConnection".equals(command))
		{
			if ((argsArray.length == 2) && (argsArray[1] instanceof String))
				return openConnection((String) argsArray[1]);
		}
		else if ("displayHelp".equals(command))
		{
			if ((argsArray.length== 2)	)
				return displayHelp((String) argsArray[1]);
		}
		else if ("displayHelpResource".equals(command))
		{
			if ((argsArray.length== 2)	)
				return displayHelpResource((String) argsArray[1]);
		}
		else if ("displayContext".equals(command))
		{
			if ((argsArray.length== 4)	)
				return displayContext((String) argsArray[1], (Integer) argsArray[2], (Integer) argsArray[3]);
		}
		return null;
	}
}