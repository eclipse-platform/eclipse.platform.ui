package org.eclipse.update.internal.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import org.eclipse.update.core.UpdateManagerPlugin;

public class UpdateManagerUtils {
	
	/**
	 * return the urlString if it is a absolute URL
	 * otherwise, return the default URL if the urlString is null
	 * if teh urlString or the defautl URL are relatives, prepend the rootURL to it
	 */
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
			if (urlString.startsWith("/") && urlString.length()>1)
				urlString = urlString.substring(1);		
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




	/**
	 * returns a translated String
	 */
	public static String getResourceString(String infoURL,ResourceBundle bundle){
		String result = null;
		if (infoURL!=null){
			result = UpdateManagerPlugin.getDefault().getDescriptor().getResourceString(infoURL,bundle);
		}
		return result;
	};
	
}