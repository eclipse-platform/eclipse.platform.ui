package org.eclipse.ui.internal.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

public final class ImageSupport {

	public static ImageDescriptor getImageDescriptor(String path) {
		try {
			URL url = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
			url = new URL(url, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException eMalformedURL) {
			System.err.println(eMalformedURL);
			return null;
		}
	}
	
	private ImageSupport() {
	}	
}
