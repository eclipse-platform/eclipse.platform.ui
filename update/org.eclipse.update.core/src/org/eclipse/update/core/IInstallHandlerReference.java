package org.eclipse.update.core;

import java.net.URL;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IInstallHandlerReference {
	
	/**
	 * optional URL used for browser-triggered
	 *  installation handling. 
	 * @since 2.0 
	 */

	URL getURL();
	
	/**
	 * optional .jar library containing the install
	 *  handler classes. If specified, the referenced .jar
	 *  must be contained in the feature archive.
	 *  It is specified as a path within the feature archive,
	 *  relative to the feature.xml entry. If not specified,
	 *  the feature archive itself is used to load the install
	 *  handler classes. This attribute is only interpreted
	 *  if class attribute is also specified 
	 * @since 2.0 
	 */

	String getLibrary();
	
	/**
	 * optional fully qualified name of a class implementing
	 *  IInstallHandler. The class is dynamically loaded and
	 *  called at specific points during feature processing.
	 *  The handler has visibility to the API classes from the update plug-in,
	 *  and Eclipse plug-ins required by the update plugin. 
	 * @since 2.0 
	 */

	String getInstallHandlerClass();

}

