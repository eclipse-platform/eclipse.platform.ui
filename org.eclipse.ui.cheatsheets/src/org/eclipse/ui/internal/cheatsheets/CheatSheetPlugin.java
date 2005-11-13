/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class for cheat sheets.
 */

public class CheatSheetPlugin extends AbstractUIPlugin {

	//The shared instance of this plugin.
	static CheatSheetPlugin plugin;

	//Resource bundle.
	//private boolean resourceBundleInitialized = false;
	//private ResourceBundle resourceBundle;
	private CheatSheetHistory history = null;
	private DocumentBuilder documentBuilder = null;
	
	private static final String DEFAULT_CHEATSHEET_STATE_FILENAME = "cheatsheet.xml"; //$NON-NLS-1$
	private static final String MEMENTO_TAG_CHEATSHEET = "cheatsheet"; //$NON-NLS-1$
	private static final String MEMENTO_TAG_VERSION = "version"; //$NON-NLS-1$
	private static final String VERSION_STRING[] = { "0.0", "3.0.0" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String MEMENTO_TAG_CHEATSHEET_HISTORY = "cheatsheetHistory"; //$NON-NLS-1$	

	public static final IPath ICONS_PATH = new Path("$nl$/icons/"); //$NON-NLS-1$	
	public static final String T_OBJ = "obj16/"; //$NON-NLS-1$
	public static final String T_ELCL = "elcl16/"; //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public CheatSheetPlugin() {
		super();
	}

	/**
	 * Returns the shared instance.
	 */
	public static CheatSheetPlugin getPlugin() {
		return plugin;
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
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, Messages.ERROR_CREATING_DOCUMENT_BUILDER, e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
		}
		return documentBuilder;
	}

	protected void initializeImageRegistry(ImageRegistry reg) {
		IPath path = ICONS_PATH.append(T_OBJ).append("cheatsheet_obj.gif");//$NON-NLS-1$
		ImageDescriptor imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_OBJ, imageDescriptor);
		
		path = ICONS_PATH.append(T_OBJ).append("skip_status.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_SKIP, imageDescriptor);

		path = ICONS_PATH.append(T_OBJ).append("complete_status.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_COMPLETE, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("linkto_help.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_HELP, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("start_cheatsheet.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_START, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("restart_cheatsheet.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_RESTART, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("start_task.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_START, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("skip_task.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_SKIP, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("complete_task.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
		reg.put(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_COMPLETE, imageDescriptor);

		path = ICONS_PATH.append(T_ELCL).append("restart_task.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getPlugin().getBundle(), path);
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
					String message = Messages.ERROR_READING_STATE_FILE;
					IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
					CheatSheetPlugin.getPlugin().getLog().log(status);
				} finally {
					try {
						if (reader != null)
							reader.close();
					} catch (IOException e) {
						// Not much to do, just catch the exception and keep going.
						String message = Messages.ERROR_READING_STATE_FILE;
						IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
						CheatSheetPlugin.getPlugin().getLog().log(status);
					}
				}
			}
			public void handleException(Throwable e) {
				String message = Messages.ERROR_READING_STATE_FILE;
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
					String message = Messages.ERROR_WRITING_STATE_FILE;
					IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
					CheatSheetPlugin.getPlugin().getLog().log(status);
				} finally {
					try {
						if (writer != null)
							writer.close();
					} catch (IOException e) {
						String message = Messages.ERROR_WRITING_STATE_FILE;
						IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
						CheatSheetPlugin.getPlugin().getLog().log(status);
					}
				}
			}
			public void handleException(Throwable e) {
				String message = Messages.ERROR_WRITING_STATE_FILE;
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

	/*
	 * Since 3.1.1. Load from icon paths with $NL$
	 */
	public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url= Platform.find(bundle, path);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		return null;
	}
	
}
