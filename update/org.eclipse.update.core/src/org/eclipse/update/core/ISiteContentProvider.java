package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
 
 /**
  * Provides 
  * 
  */
 //FIXME: javadoc
 
public interface ISiteContentProvider {
	
	/**
	 * Returns the URL of this site
	 * 
	 * @return the site URL
	 * @since 2.0
	 */
	
	URL getURL();
			
	
	/**
	 * Returns a URL associated to the archiveID
	 * 
	 * @param id the archiveId
	 * @return a URL
	 * @throws CoreException 
	 * @since 2.0 
	 */

	URL getArchiveReference(String id)  throws CoreException;
	
	
		
	
}


