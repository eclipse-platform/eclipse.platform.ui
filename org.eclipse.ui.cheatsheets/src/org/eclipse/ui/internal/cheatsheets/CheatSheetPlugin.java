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
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.cheatsheets.actions.CheatSheetMenu;
import org.eclipse.ui.internal.cheatsheets.registry.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class for cheat sheets.
 */

public class CheatSheetPlugin extends AbstractUIPlugin implements IStartup, ICheatSheetResource {

	//The shared instance of this plugin.
	private static CheatSheetPlugin plugin;

	//Resource bundle.
	private ResourceBundle resourceBundle;
	private CheatSheetHistory history;

	private static final String DEFAULT_CHEATSHEET_STATE_FILENAME = "cheatsheet.xml"; //$NON-NLS-1$
	private static final String MEMENTO_TAG_CHEATSHEET = "cheatsheet"; //$NON-NLS-1$
	private static final String MEMENTO_TAG_VERSION = "version"; //$NON-NLS-1$
	private static final String VERSION_STRING[] = { "0.0", "3.0.0" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String MEMENTO_TAG_CHEATSHEET_HISTORY = "cheatsheetHistory"; //$NON-NLS-1$	

/*
	public CheatSheetPlugin() {
		System.out.println("Here"); //$NON-NLS-1$
	}
	
*/
	
	/**
	 * The constructor.
	 */
	public CheatSheetPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;

		//we have this in the code, but resourceBundle is never used.
		//we are leaving it in for the future in case it is needed.
		try {
			resourceBundle = ResourceBundle.getBundle(ICheatSheetResource.CHEAT_SHEET_RESOURCE_ID);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

	}

	/**
	 * Adds the Cheat Sheet menu items to the help menu of the list of workbench windows.
	 * 
	 * @param windows the workbench windows that need to have there help menu updated
	 */
	private void addCheatSheetMenu(IWorkbenchWindow[] windows) {
		if (windows == null) {
			return;
		}

		CheatSheetCollectionElement collection = (CheatSheetCollectionElement) CheatSheetRegistryReader.getInstance().getCheatSheets();
		if (collection.getCheatSheets().length <= 0 && collection.getChildren().length <= 0) {
			return;
		}

		WindowLoop : for (int windowCount = 0; windowCount < windows.length; ++windowCount) {

			MenuManager cheatmanager = new MenuManager(getResourceString(ICheatSheetResource.CHEAT_SHEETS), ICheatSheetResource.CHEAT_SHEET_MENU_ID);

			WorkbenchWindow realwindow = (WorkbenchWindow) windows[windowCount];
			IMenuManager menubar = realwindow.getMenuBarManager();
			IContributionItem[] myitems = menubar.getItems();

			for (int i = 0; i < myitems.length; i++) {
				//System.out.println("The id of the item is: "+myitems[i].getId());	
				if (myitems[i].getId() != null && myitems[i].getId().equals(IWorkbenchActionConstants.M_HELP)) {
					IContributionItem helpitem = myitems[i];
					IContributionItem cheatsheetMenu = ((IMenuManager) helpitem).find(ICheatSheetResource.CHEAT_SHEET_MENU_ID);
					if (cheatsheetMenu == null) {
						((IMenuManager) helpitem).insertBefore(IWorkbenchActionConstants.HELP_START, cheatmanager);
					} else {
						break WindowLoop;
					}
					break;
				}
			}

			CheatSheetMenu cheatsheetMenuMenuItem = new CheatSheetMenu();
			cheatmanager.add(cheatsheetMenuMenuItem);
		}
	}

	/**
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		//get a handle to the help menu in the workbench.  It is a MenuManager type.
		final IWorkbench mybench = getPlugin().getWorkbench();

		IWorkbenchWindow[] windows = mybench.getWorkbenchWindows();
		addCheatSheetMenu(windows);

		mybench.addWindowListener(new IWindowListener() {
			public void windowActivated(IWorkbenchWindow window) {
			}
			public void windowDeactivated(IWorkbenchWindow window) {
			}
			public void windowClosed(IWorkbenchWindow window) {
			}
			public void windowOpened(IWorkbenchWindow window) {
				IWorkbenchWindow[] openedWindow = { mybench.getActiveWorkbenchWindow()};
				addCheatSheetMenu(openedWindow);
			}
		});
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		try {
			if(plugin != null) {
				return plugin.getDescriptor().getResourceString(key);
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
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Returns the CheatSheetHistory,
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
	 * Restores the state of the previously saved cheatsheet history
	 */
	private void restoreCheatSheetHistory() {
		Platform.run(new SafeRunnable() { //$NON-NLS-1$
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
				getCheatSheetHistory().saveState(memento.createChild(MEMENTO_TAG_CHEATSHEET_HISTORY)); //$NON-NLS-1$

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

	/**
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		// initialize the MRU history
		getCheatSheetHistory();
	}

	/**
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		// save the MRU history
		saveCheatSheetHistory();
	}

	private Class loadClass(String className, String classPluginId) {
		IPluginDescriptor desc = Platform.getPluginRegistry().getPluginDescriptor(classPluginId);
		if (desc == null) {
			return null;
		}
		Class aClass = null;
		try {
			aClass = desc.getPluginClassLoader().loadClass(className);
		} catch (Exception e) {
			return null;
		}
		return aClass;
	}

//	/* (non-Javadoc)
//	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
//	 */
//	public void start(BundleContext context) throws Exception {
//		super.start(context);
//
//		plugin = this;
//		startup();
//	}
//
//	/* (non-Javadoc)
//	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
//	 */
//	public void stop(BundleContext context) throws Exception {
//		super.stop(context);
//		shutdown();
//	}

}
