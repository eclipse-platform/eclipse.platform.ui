package org.eclipse.ui.internal.util;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

public final class ImageSupport {

	public static ImageDescriptor getImageDescriptor(String path) {
		URL url = BundleUtility.find(PlatformUI.PLUGIN_ID, path);
		return ImageDescriptor.createFromURL(url);
	}

	private ImageSupport() {
	}
}
