package org.eclipse.update.internal.core;

import java.net.MalformedURLException;
import java.net.URL;

public class UpdateManagerUtils {
	
	
	public static URL getURL(URL rootURL,String urlString,String defaultURL){
		URL url = null;
		try {
			// if no URL , provide Default
			if (urlString == null || urlString.trim().equals("")) {
				
				// no URL, no default, return right now...
				if (defaultURL == null || defaultURL.trim().equals("")) 
					return null;
				else
					urlString = defaultURL;
			}
				
			// URL can be relative or absolute			
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				// the url is not an absolute URL
				// try relative
				url = new URL(rootURL, urlString);
			}
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		}
		return url;
	}
}

