/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class for cheat sheets.
 */

public class CheatSheetPlugin extends AbstractUIPlugin {

	//The shared instance of this plugin.
	private static CheatSheetPlugin plugin;

	//Resource bundle.
	private boolean resourceBundleInitialized = false;
	private ResourceBundle resourceBundle;
	private CheatSheetHistory history = null;
	private DocumentBuilder documentBuilder = null;
	
	private static final String DEFAULT_CHEATSHEET_STATE_FILENAME = "cheatsheet.xml"; //$NON-NLS-1$
	private static final String MEMENTO_TAG_CHEATSHEET = "cheatsheet"; //$NON-NLS-1$
	private static final String MEMENTO_TAG_VERSION = "version"; //$NON-NLS-1$
	private static final String VERSION_STRING[] = { "0.0", "3.0.0" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String MEMENTO_TAG_CHEATSHEET_HISTORY = "cheatsheetHistory"; //$NON-NLS-1$	

	/**
	 * The constructor.
	 */
	public CheatSheetPlugin() {
		super();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		try {
			if(plugin != null) {
				return Platform.getResourceString(plugin.getBundle(), key);
			}
		} catch (MissingResourceException e) {
		}
		return key;
	}

	/**
	 * Returns the shared instance.
	 */
	public static CheatSheetPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Returns the formatted message for the given key in
	 * the resource bundle. 
	 *
	 * @param key the resource name
	 * @param args the message arguments
	 * @return the string
	 */
	public static String formatResourceString(String key, Object[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}

	/**
	 * Returns the image in Cheat Sheet's image registry with the given key, 
	 * or <code>null</code> if none.
	 * Convenience method equivalent to
	 * <pre>
	 * CheatSheetPlugin.getImageRegistry().get(key)
	 * </pre>
	 *
	 * @param key the key
	 * @return the image, or <code>null</code> if none
	 */
	public Image getImage(String key) {
		Image image = getImageRegistry().get(key);
		return image;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		//we have this in the code, but resourceBundle is never used.
		//we are leaving it in for the future in case it is needed.
		if (!resourceBundleInitialized) {
			// only try to initialize once 
			resourceBundleInitialized = true;
			try {
				resourceBundle = ResourceBundle.getBundle(ICheatSheetResource.CHEAT_SHEET_RESOURCE_ID);
			} catch (MissingResourceException x) {
				resourceBundle = null;
			}
		}
		return resourceBundle;
	}

	/**
	 * Returns the CheatSheetHistory
	 */
	public CheatSheetHistory getCheatSheetHistory() {
		if (history == null) {
			history = new CheatSheetHistory(CheatSheetRegistryReader.getInstance());
			restoreCheatSheetHistory();
		}
		return history;
	}

	/**
	 * Answer the workbench state file.
	 */
	private File getCheatSheetStateFile() {
		IPath path = CheatSheetPlugin.getPlugin().getStateLocation();
		path = path.append(DEFAULT_CHEATSHEET_STATE_FILENAME);
		return path.toFile();
	}

	/**
	 * Returns the DocumentBuilder to be used by the cheat sheets.
	 */
	public DocumentBuilder getDocumentBuilder() {
		if(documentBuilder == null) {
			try {
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_DOCUMENT_BUILDER), e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
		}
		return documentBuilder;
	}

	protected void initializeImageRegistry(ImageRegistry reg) {
		String imageFileName = "icons/full/obj16/skip_status.gif"; //$NON-NLS-1$
		URL imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_SKIP, imageDescriptor);

		imageFileName = "icons/full/obj16/complete_status.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_COMPLETE, imageDescriptor);

		imageFileName = "icons/full/clcl16/linkto_help.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_HELP, imageDescriptor);

		imageFileName = "icons/full/clcl16/start_cheatsheet.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_START, imageDescriptor);

		imageFileName = "icons/full/clcl16/restart_cheatsheet.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_RESTART, imageDescriptor);

		imageFileName = "icons/full/clcl16/start_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_START, imageDescriptor);

		imageFileName = "icons/full/clcl16/skip_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_SKIP, imageDescriptor);

		imageFileName = "icons/full/clcl16/complete_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_COMPLETE, imageDescriptor);

		imageFileName = "icons/full/clcl16/restart_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_RESTART, imageDescriptor);
	}

	/**
	 * Restores the state of the previously saved cheatsheet history
	 */
	private void restoreCheatSheetHistory() {
		Platform.run(new SafeRunnable() {
			public void run() {
				InputStreamReader reader = null;

				try {
					// Read the cheatsheet state file.
					final File stateFile = getCheatSheetStateFile();

					FileInputStream input = new FileInputStream(stateFile);
					reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$
					IMemento memento = XMLMemento.createReadRoot(reader);

					IMemento childMem = memento.getChild(MEMENTO_TAG_CHEATSHEET_HISTORY);
					if (childMem != null) {
						history.restoreState(childMem);
					}
				} catch (FileNotFoundException e) {
					// Do nothing, the file will not exist the first time the workbench in used.
				} catch (Exception e) {
					String message = getResourceString(ICheatSheetResource.ERROR_READING_STATE_FILE);
					IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
					CheatSheetPlugin.getPlugin().getLog().log(status);
				} finally {
					try {
						if (reader != null)
							reader.close();
					} catch (IOException e) {
						// Not much to do, just catch the exception and keep going.
						String message = getResourceString(ICheatSheetResource.ERROR_READING_STATE_FILE);
						IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
						CheatSheetPlugin.getPlugin().getLog().log(status);
					}
				}
			}
			public void handleException(Throwable e) {
				String message = getResourceString(ICheatSheetResource.ERROR_READING_STATE_FILE);
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
		});
	}

	/**
	 * Saves the current cheatsheet history so it can be restored later on
	 */
	private void saveCheatSheetHistory() {
		Platform.run(new SafeRunnable() {
			public void run() {
				XMLMemento memento = XMLMemento.createWriteRoot(MEMENTO_TAG_CHEATSHEET);

				// Save the version number.
				memento.putString(MEMENTO_TAG_VERSION, VERSION_STRING[1]);

				// Save perspective history.
				getCheatSheetHistory().saveState(memento.createChild(MEMENTO_TAG_CHEATSHEET_HISTORY));

				// Save the IMemento to a file.
				File stateFile = getCheatSheetStateFile();
				OutputStreamWriter writer = null;
				try {
					FileOutputStream stream = new FileOutputStream(stateFile);
					writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
					memento.save(writer);
				} catch (IOException e) {
					stateFile.delete();
					String message = getResourceString(ICheatSheetResource.ERROR_WRITING_STATE_FILE);
					IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
					CheatSheetPlugin.getPlugin().getLog().log(status);
				} finally {
					try {
						if (writer != null)
							writer.close();
					} catch (IOException e) {
						String message = getResourceString(ICheatSheetResource.ERROR_WRITING_STATE_FILE);
						IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
						CheatSheetPlugin.getPlugin().getLog().log(status);
					}
				}
			}
			public void handleException(Throwable e) {
				String message = getResourceString(ICheatSheetResource.ERROR_WRITING_STATE_FILE);
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		plugin = this;
		
		// allow the MRU history to be lazily initialized by getCheatSheetHistory
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		
		// save the MRU history if necessary
		// if we never restored history, let existing memento stand
		if (history != null) {
			saveCheatSheetHistory();
		}
		
		CheatSheetRegistryReader.getInstance().stop();
	}

}
