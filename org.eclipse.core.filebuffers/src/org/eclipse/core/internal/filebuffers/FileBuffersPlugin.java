package org.eclipse.core.internal.filebuffers;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;


/**
 * The main plugin class to be used in the desktop.
 */
public class FileBuffersPlugin extends Plugin {
	
	public final static String PLUGIN_ID= "org.eclipse.core.filebuffers";  //$NON-NLS-1$
	
	/** The shared plug-in instance */
	private static FileBuffersPlugin fgPlugin;
	/** The resource bundle */
	private ResourceBundle fResourceBundle;
	/** The file buffer manager */
	private ITextFileBufferManager fTextFileBufferManager;
	
	/**
	 * The constructor.
	 */
	public FileBuffersPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin = this;
		try {
			fResourceBundle= ResourceBundle.getBundle("org.eclipse.core.buffer.internal,text.TextBufferPluginResources");  //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static FileBuffersPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= FileBuffersPlugin.getDefault().getResourceBundle();
		try {
			return (bundle!=null ? bundle.getString(key) : key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return fResourceBundle;
	}
	
	/**
	 * Returns the text file buffer manager of this plug-in.
	 * 
	 * @return the text file buffer manager of this plug-in
	 */
	public ITextFileBufferManager getBufferedFileManager()  {
		if (fTextFileBufferManager == null)
			fTextFileBufferManager= new TextFileBufferManager();
		return fTextFileBufferManager;
	}
}
