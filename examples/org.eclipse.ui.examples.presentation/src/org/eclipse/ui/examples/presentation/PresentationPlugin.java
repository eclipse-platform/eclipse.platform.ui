package org.eclipse.ui.examples.presentation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PresentationPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static PresentationPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public PresentationPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.ui.examples.presentation.PresentationPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	
	/**
	 * Returns the given image. The image will be managed by the plugin's
	 * image registry.
	 * 
	 * @param imageName a pathname relative to the icons directory of
	 * this project.
	 */
	public Image getImage(String imageName) {
		ImageRegistry reg = getImageRegistry();
		
		Image result = reg.get(imageName);
		
		if (result != null) {
			return result;
		}
		
		result = getImageDescriptor(imageName).createImage();
		
		reg.put(imageName, result);
		
		return result;
	}

	/**
	 * Returns the given image descriptor. The caller will be responsible
	 * for deallocating the image if it creates the image from the descriptor
	 * 
	 * @param imageName is a pathname relative to the icons directory 
	 * within this project.
	 */
	public ImageDescriptor getImageDescriptor(String imageName) {		
		ImageDescriptor desc;
		try {
			desc =
				ImageDescriptor.createFromURL(
					new URL(
							plugin.getBundle().getEntry("/"), //$NON-NLS-1$
							"icons/" + imageName)); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			desc = ImageDescriptor.getMissingImageDescriptor();
		}
					
		return desc;
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static PresentationPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PresentationPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
