package org.eclipse.team.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * TeamUIPlugin is the plugin for generic, non-provider specific,
 * team UI functionality in the workbench.
 */
public class TeamUIPlugin extends AbstractUIPlugin implements ISharedImages {

	private static TeamUIPlugin instance;
	public static final String ID = "org.eclipse.team.ui"; //$NON-NLS-1$
	
	// property change types
	public static String GLOBAL_IGNORES_CHANGED = "global_ignores_changed"; //$NON-NLS-1$

	private Hashtable imageDescriptors = new Hashtable(20);
	
	private List propertyChangeListeners = new ArrayList(5);

	public final static String ICON_PATH;
	static {
		final String iconPath[] = new String[1];
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				if (display.getIconDepth() > 4) {
					iconPath[0] = UIConstants.ICON_PATH_FULL;
				} else {
					iconPath[0] = UIConstants.ICON_PATH_BASIC;
				}
			}
		});
		ICON_PATH = iconPath[0];
	}

	/**
	 * Creates a new TeamUIPlugin.
	 * 
	 * @param descriptor  the plugin descriptor
	 */
	public TeamUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		instance = this;
	}
	/**
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @return the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		if (plugin.isPluginActivated()) {
			return element.createExecutableExtension(classAttribute);
		} else {
			final Object [] ret = new Object[1];
			final CoreException [] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					try {
						ret[0] = element.createExecutableExtension(classAttribute);
					} catch (CoreException e) {
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null)
				throw exc[0];
			else
				return ret[0];
		}	
	}
	/**
	 * Creates an image and places it in the image registry.
	 * 
	 * @param id  the identifier for the image
	 * @param baseURL  the base URL for the image
	 */
	protected void createImageDescriptor(String id, URL baseURL) {
		URL url = null;
		try {
			url = new URL(baseURL, UIConstants.ICON_PATH_FULL + id);
		} catch (MalformedURLException e) {
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		imageDescriptors.put(id, desc);
	}
	/**
	 * Convenience method to get the currently active workbench page
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		return getPlugin().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	/**
	 * Return the default instance of the receiver. This represents the runtime plugin.
	 * 
	 * @return the singleton plugin instance
	 */
	public static TeamUIPlugin getPlugin() {
		return instance;
	}
	/**
	 * Returns the image descriptor for the given image ID.
	 * Returns null if there is no such image.
	 * 
	 * @param id  the identifier for the image to retrieve
	 * @return the image associated with the given ID
	 */
	public ImageDescriptor getImageDescriptor(String id) {
		return (ImageDescriptor) imageDescriptors.get(id);
	}	
	/**
	 * Convenience method to get an image descriptor for an extension
	 * 
	 * @param extension  the extension declaring the image
	 * @param subdirectoryAndFilename  the path to the image
	 * @return the image
	 */
	public static ImageDescriptor getImageDescriptorFromExtension(IExtension extension, String subdirectoryAndFilename) {
		IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
		URL path = pluginDescriptor.getInstallURL();
		URL fullPathString = null;
		try {
			fullPathString = new URL(path,subdirectoryAndFilename);
			return ImageDescriptor.createFromURL(fullPathString);
		} catch (MalformedURLException e) {
		}
		return null;
	}
	/**
	 * Initializes the table of images used in this plugin.
	 */
	protected void initializeImages() {
		URL baseURL = getDescriptor().getInstallURL();

		// View decoration overlays
		createImageDescriptor(ISharedImages.IMG_DIRTY_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR, baseURL);
		
		createImageDescriptor(UIConstants.IMG_DLG_SYNC_INCOMING, baseURL);
		createImageDescriptor(UIConstants.IMG_DLG_SYNC_OUTGOING, baseURL);
		createImageDescriptor(UIConstants.IMG_DLG_SYNC_CONFLICTING, baseURL);
		createImageDescriptor(UIConstants.IMG_REFRESH, baseURL);
		createImageDescriptor(UIConstants.IMG_IGNORE_WHITESPACE, baseURL);
		createImageDescriptor(UIConstants.IMG_SYNC_MODE_CATCHUP, baseURL);
		createImageDescriptor(UIConstants.IMG_SYNC_MODE_RELEASE, baseURL);
		createImageDescriptor(UIConstants.IMG_SYNC_MODE_FREE, baseURL);
		createImageDescriptor(UIConstants.IMG_WIZBAN_SHARE, baseURL);
		createImageDescriptor(UIConstants.IMG_CONTENTS, baseURL);
	}
	/**
	 * Initializes the preferences for this plugin if necessary.
	 */
	protected void initializePreferences() {
		//IPreferenceStore store = getPreferenceStore();
	}
	
	/**
	 * Convenience method for logging statuses to the plugin log
	 * 
	 * @param status  the status to log
	 */
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}
	
	/**
	 * Register for changes made to Team UI properties.
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	
	/**
	 * Deregister as a Team UI property changes.
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}
	
	/**
	 * Broadcast a Team UI property change.
	 */
	public void broadcastPropertyChange(PropertyChangeEvent event) {
		for (Iterator it = propertyChangeListeners.iterator(); it.hasNext();) {
			IPropertyChangeListener listener = (IPropertyChangeListener) it.next();			
			listener.propertyChange(event);
		}
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		Policy.localize("org.eclipse.team.internal.ui.messages"); //$NON-NLS-1$
		initializeImages();
	}
}