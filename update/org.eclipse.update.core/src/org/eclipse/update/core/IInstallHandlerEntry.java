package org.eclipse.update.core;

import java.net.URL;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IInstallHandlerEntry {
	
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
	 *  relative to the feature.xml entry.
	 * @since 2.0 
	 */

	String getLibrary();
	
	/**
	 * Required handler name.
	 *  It is interpreted depending on the value of the library
	 *  specification. If library is not specified, the name
	 *  is intepreted as an identifier of a "global" install
	 *  handler registered in the installHandlers extension
	 *  point. If library is specified, the name is interpreted
	 *  as a fully qualified name of a class contained in the
	 *  library. In both cases, the resulting class must
	 *  implement IInstallHandler. The class is dynamically loaded and
	 *  called at specific points during feature processing.
	 *  The handler has visibility to the API classes from the update plug-in,
	 *  and Eclipse plug-ins required by the update plugin. 
	 * @since 2.0 
	 */

	String getHandlerName();

}

