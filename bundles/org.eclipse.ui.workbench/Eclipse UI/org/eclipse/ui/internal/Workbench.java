/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.application.IWorkbenchPreferences;
import org.eclipse.ui.application.WorkbenchAdviser;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.fonts.FontDefinition;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.UIStats;

/**
 * The workbench class represents the top of the Eclipse user interface. 
 * Its primary responsability is the management of workbench windows, dialogs,
 * wizards, and other workbench-related windows.
 * <p>
 * Note that any code that is run during the creation of a workbench instance
 * should not required access to the display.
 * </p><p>
 * Note that this internal class changed significantly between 2.1 and 3.0.
 * Applications that used to define subclasses of this internal class need to
 * be rewritten to use the new workbench adviser API.
 * </p>
 */
public final class Workbench implements IWorkbench {
	private static final String VERSION_STRING[] = { "0.046", "2.0" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String DEFAULT_WORKBENCH_STATE_FILENAME = "workbench.xml"; //$NON-NLS-1$
	private static final int RESTORE_CODE_OK = 0;
	private static final int RESTORE_CODE_RESET = 1;
	private static final int RESTORE_CODE_EXIT = 2;

	/**
	 * Holds onto the only instance of Workbench.
	 */
	private static Workbench instance;
	
	private WindowManager windowManager;
	private WorkbenchWindow activatedWindow;
	private EditorHistory editorHistory;
	private boolean runEventLoop = true;
	private boolean isStarting = true;
	private boolean isClosing = false;
	
	/**
	 * PlatformUI return code (as opposed to IPlatformRunnable return code).
	 */
	private int returnCode;
	
	private ListenerList windowListeners = new ListenerList();
	
	/**
	 * Adviser providing application-specific configuration and customization
	 * of the workbench.
	 */
	private WorkbenchAdviser adviser;

	/**
	 * Object for configuring the workbench. Lazily initialized to
	 * an instance unique to the workbench instance.
	 */
	private WorkbenchConfigurer workbenchConfigurer;

	/**
	 * Creates a new workbench.
	 * 
	 * @param adviser the application-specific adviser that configures and
	 * specializes this workbench instance
	 */
	private Workbench(WorkbenchAdviser adviser) {
		super();

		if (instance != null) {
			throw new IllegalStateException(WorkbenchMessages.getString("Workbench.CreatingWorkbenchTwice")); //$NON-NLS-1$
		}		
		if (adviser == null) {
			throw new IllegalArgumentException(WorkbenchMessages.getString("Workbench.InvalidAdviser")); //$NON-NLS-1$
		}
		this.adviser = adviser;
		Workbench.instance = this;
	}
	
	/**
	 * Returns the one and only instance of the workbench
	 * or <code>null</code> if not created yet.
	 * 
	 * @return the workbench or <code>null</code> if not created yet.
	 */
	public static final Workbench getInstance() {
		return instance;
	}
	
	/**
	 * Creates the workbench and associates it with the given workbench adviser,
	 * and runs the workbench UI. This entails processing and dispatching
	 * events until the workbench is closed or restarted.
	 * <p>
	 * This method is intended to be called by <code>PlatformUI</code>.
	 * Fails if the workbench UI has already been created.
	 * </p>
	 * 
	 * @param adviser the application-specific adviser that configures and
	 * specializes the workbench
	 * @return return code {@link PlatformUI#RETURN_OK RETURN_OK} for normal
	 * exit; {@link PlatformUI#RETURN_RESTART RETURN_RESTART} if the workbench
	 * was terminated with a call to
	 * {@link IWorkbench#restart IWorkbench.restart}; other values reserved
	 * for future use
	 */
	public static final int createAndRunWorkbench(WorkbenchAdviser adviser) {
		// create the workbench instance
		Workbench workbench = new Workbench(adviser);
		// run the workbench event loop
		return workbench.runUI();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public void addWindowListener(IWindowListener l) {
		windowListeners.add(l);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public void removeWindowListener(IWindowListener l) {
		windowListeners.remove(l);
	}

	/**
	 * Fire window opened event.
	 */
	protected void fireWindowOpened(IWorkbenchWindow window) {
		Object list[] = windowListeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IWindowListener) list[i]).windowOpened(window);
		}
	}
	/**
	 * Fire window closed event.
	 */
	protected void fireWindowClosed(IWorkbenchWindow window) {
		if (activatedWindow == window) {
			// Do not hang onto it so it can be GC'ed
			activatedWindow = null;
		}
					
		Object list[] = windowListeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IWindowListener) list[i]).windowClosed(window);
		}
	}
	/**
	 * Fire window activated event.
	 */
	protected void fireWindowActivated(IWorkbenchWindow window) {
		Object list[] = windowListeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IWindowListener) list[i]).windowActivated(window);
		}
	}
	/**
	 * Fire window deactivated event.
	 */
	protected void fireWindowDeactivated(IWorkbenchWindow window) {
		Object list[] = windowListeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IWindowListener) list[i]).windowDeactivated(window);
		}
	}

	/**
	 * Closes the workbench. Assumes that the busy cursor is active.
	 * 
	 * @param force true if the close is mandatory, and false if the close is
	 * allowed to fail
	 * @return true if the close succeeded, and false otherwise
	 */
	private boolean busyClose(final boolean force) {
		
		// save any open editors if they are dirty
		isClosing = saveAllEditors(!force);
		if (!force && !isClosing) {
			return false;
		}

		IPreferenceStore store = getPreferenceStore();
		boolean closeEditors = store.getBoolean(IWorkbenchPreferences.SHOULD_CLOSE_EDITORS_ON_EXIT);
		if (closeEditors) {
			Platform.run(new SafeRunnable() {
				public void run() {
					IWorkbenchWindow windows[] = getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						IWorkbenchPage pages[] = windows[i].getPages();
						for (int j = 0; j < pages.length; j++) {
							isClosing = isClosing && pages[j].closeAllEditors(false);
						}
					}
				}
			});
			if (!force && !isClosing) {
				return false;
			}
		}

		if (getWorkbenchConfigurer().getSaveAndRestore()) {
			Platform.run(new SafeRunnable() {
				public void run() {
					XMLMemento mem = recordWorkbenchState();
					//Save the IMemento to a file.
					saveMementoToFile(mem);
				}
				public void handleException(Throwable e) {
					String message;
					if (e.getMessage() == null) {
						message = WorkbenchMessages.getString("ErrorClosingNoArg"); //$NON-NLS-1$
					} else {
						message = WorkbenchMessages.format("ErrorClosingOneArg", new Object[] { e.getMessage()}); //$NON-NLS-1$
					}
	
					if (!MessageDialog.openQuestion(null, WorkbenchMessages.getString("Error"), message)) { //$NON-NLS-1$
						isClosing = false;
					}
				}
			});
		}
		if (!force && !isClosing) {
			return false;
		}

		Platform.run(new SafeRunnable(WorkbenchMessages.getString("ErrorClosing")) { //$NON-NLS-1$
			public void run() {
				if (isClosing || force)
					isClosing = windowManager.close();
			}
		});

		if (!force && !isClosing) {
			return false;
		}

		runEventLoop = false;
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public boolean saveAllEditors(boolean confirm) {
		final boolean finalConfirm = confirm;
		final boolean[] result = new boolean[1];
		result[0] = true;

		Platform.run(new SafeRunnable(WorkbenchMessages.getString("ErrorClosing")) { //$NON-NLS-1$
			public void run() {
				//Collect dirtyEditors
				ArrayList dirtyEditors = new ArrayList();
				ArrayList dirtyEditorsInput = new ArrayList();
				IWorkbenchWindow windows[] = getWorkbenchWindows();
				for (int i = 0; i < windows.length; i++) {
					IWorkbenchPage pages[] = windows[i].getPages();
					for (int j = 0; j < pages.length; j++) {
						WorkbenchPage page = (WorkbenchPage) pages[j];
						IEditorPart editors[] = page.getDirtyEditors();
						for (int k = 0; k < editors.length; k++) {
							IEditorPart editor = editors[k];
							if (editor.isDirty()) {
								if (!dirtyEditorsInput.contains(editor.getEditorInput())) {
									dirtyEditors.add(editor);
									dirtyEditorsInput.add(editor.getEditorInput());
								}
							}
						}
					}
				}
				if (dirtyEditors.size() > 0) {
					IWorkbenchWindow w = getActiveWorkbenchWindow();
					if (w == null)
						w = windows[0];
					result[0] = EditorManager.saveAll(dirtyEditors, finalConfirm, w);
				}
			}
		});
		return result[0];
	}
	
	/**
	 * Opens a new workbench window and page with a specific perspective.
	 *
	 * Assumes that busy cursor is active.
	 */
	private IWorkbenchWindow busyOpenWorkbenchWindow(String perspID, IAdaptable input) throws WorkbenchException {
		// Create a workbench window (becomes active window)
		WorkbenchWindow newWindow = newWorkbenchWindow();
		newWindow.create(); // must be created before adding to window manager
		windowManager.add(newWindow);

		// Create the initial page.
		newWindow.busyOpenPage(perspID, input);

		// Open after opening page, to avoid flicker.
		newWindow.open();

		return newWindow;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public boolean close() {
		return close(PlatformUI.RETURN_OK, false);
	}
	/**
	 * Closes the workbench, returning the given return code from the run method.
	 * If forced, the workbench is closed no matter what.
	 * 
	 * @param returnCode {@link PlatformUI#RETURN_OK RETURN_OK} for normal exit; 
	 * {@link PlatformUI#RETURN_RESTART RETURN_RESTART} if the workbench was terminated
	 * with a call to {@link IWorkbench#restart IWorkbench.restart}; 
	 * {@link PlatformUI#RETURN_UNSTARTABLE RETURN_UNSTARTABLE} if the workbench could
	 * not be started; other values reserved for future use
	 * @param force true to force the workbench close, and false for a "soft"
	 * close that can be canceled
	 * @return true if the close was successful, and false if the close was
	 * canceled
	 */
	/* package */ boolean close(int returnCode, final boolean force) {
		this.returnCode = returnCode;
		final boolean[] ret = new boolean[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				ret[0] = busyClose(force);
			}
		});
		return ret[0];
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		// Display will be null if SWT has not been initialized or
		// this method was called from wrong thread.
		Display display = Display.getCurrent();
		if (display == null)
			return null;

		// Look at the current shell and up its parent
		// hierarchy for a workbench window.
		Control shell = display.getActiveShell();
		while (shell != null) {
			Object data = shell.getData();
			if (data instanceof IWorkbenchWindow)
				return (IWorkbenchWindow) data;
			shell = shell.getParent();
		}
		
		// Look for the window that was last known being
		// the active one
		WorkbenchWindow win = getActivatedWindow();
		if (win != null) {
			return win;
		}
		
		// Look at all the shells and pick the first one
		// that is a workbench window.
		Shell shells[] = display.getShells();
		for (int i = 0; i < shells.length; i++) {
			Object data = shells[i].getData();
			if (data instanceof IWorkbenchWindow)
				return (IWorkbenchWindow) data;
		}
		
		// Can't find anything!
		return null;
	}

	/*
	 * Returns the editor history.
	 */
	protected EditorHistory getEditorHistory() {
		if (editorHistory == null) {
			IPreferenceStore store = getPreferenceStore();
			editorHistory = new EditorHistory(store.getInt(IPreferenceConstants.RECENT_FILES));
		}
		return editorHistory;
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IEditorRegistry getEditorRegistry() {
		return WorkbenchPlugin.getDefault().getEditorRegistry();
	}
	
	/*
	 * Returns the number for a new window.  This will be the first
	 * number > 0 which is not used to identify another window in
	 * the workbench.
	 */
	private int getNewWindowNumber() {
		// Get window list.
		Window[] windows = windowManager.getWindows();
		int count = windows.length;

		// Create an array of booleans (size = window count).
		// Cross off every number found in the window list.
		boolean checkArray[] = new boolean[count];
		for (int nX = 0; nX < count; nX++) {
			if (windows[nX] instanceof WorkbenchWindow) {
				WorkbenchWindow ww = (WorkbenchWindow) windows[nX];
				int index = ww.getNumber() - 1;
				if (index >= 0 && index < count)
					checkArray[index] = true;
			}
		}

		// Return first index which is not used.
		// If no empty index was found then every slot is full.
		// Return next index.
		for (int index = 0; index < count; index++) {
			if (!checkArray[index])
				return index + 1;
		}
		return count + 1;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IPerspectiveRegistry getPerspectiveRegistry() {
		return WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public PreferenceManager getPreferenceManager() {
		return WorkbenchPlugin.getDefault().getPreferenceManager();
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IPreferenceStore getPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public ISharedImages getSharedImages() {
		return WorkbenchPlugin.getDefault().getSharedImages();
	}
	/**
	 * Returns the window manager for this workbench.
	 * 
	 * @return the window manager
	 */
	/* package */ WindowManager getWindowManager() {
		return windowManager;
	}

	/*
	 * Answer the workbench state file.
	 */
	private File getWorkbenchStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getStateLocation();
		path = path.append(DEFAULT_WORKBENCH_STATE_FILENAME);
		return path.toFile();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public int getWorkbenchWindowCount() {
		return windowManager.getWindowCount();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchWindow[] getWorkbenchWindows() {
		Window[] windows = windowManager.getWindows();
		IWorkbenchWindow[] dwindows = new IWorkbenchWindow[windows.length];
		System.arraycopy(windows, 0, dwindows, 0, windows.length);
		return dwindows;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkingSetManager getWorkingSetManager() {
		return WorkbenchPlugin.getDefault().getWorkingSetManager();
	}

	public void updateActiveGestureBindingService() {		
	}

	public void updateActiveKeyBindingService() {
		WorkbenchWindow activeWindow = (WorkbenchWindow) getActiveWorkbenchWindow();
		
		if (activeWindow != null)
			activeWindow.updateContextAndHandlerManager();			
	}
		
	/*
	 * Initializes the workbench now that the display is created.
	 *
	 * @return true if init succeeded.
	 */
	private boolean init(AboutInfo aboutInfo) {	
		// setup debug mode if required.
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			WorkbenchPlugin.DEBUG = true;
			ModalContext.setDebugMode(true);
		}

		// create workbench window manager
		windowManager = new WindowManager();

		// allow the workbench configurer to initialize
		getWorkbenchConfigurer().init();

		initializeImages(aboutInfo);
		initializeFonts();
		initializeColors();

		// now that the workbench is sufficiently initialized, let the adviser have a turn.
		adviser.initialize(getWorkbenchConfigurer());
		
		// configure use of color icons in toolbars
		boolean useColorIcons = getPreferenceStore().getBoolean(IPreferenceConstants.COLOR_ICONS);
		ActionContributionItem.setUseColorIconsInToolbars(useColorIcons);
		
		// initialize workbench single-click vs double-click behavior	
		initializeSingleClickOption();

		// attempt to restore a previous workbench state
		try {
			UIStats.start(UIStats.RESTORE_WORKBENCH, "Workbench"); //$NON-NLS-1$

			adviser.preStartup();

			int restoreCode = openPreviousWorkbenchState();
			if (restoreCode == RESTORE_CODE_EXIT) {
				return false;
			}
			if (restoreCode == RESTORE_CODE_RESET) {
				openFirstTimeWindow();
			}
		} finally {
			UIStats.end(UIStats.RESTORE_WORKBENCH, "Workbench"); //$NON-NLS-1$
		}

		forceOpenPerspective();
		
		isStarting = false;
		return true;
	}

	private void initializeSingleClickOption() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean openOnSingleClick = store.getBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK);
		boolean selectOnHover = store.getBoolean(IPreferenceConstants.SELECT_ON_HOVER);
		boolean openAfterDelay = store.getBoolean(IPreferenceConstants.OPEN_AFTER_DELAY);
		int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK : OpenStrategy.DOUBLE_CLICK;
		if (openOnSingleClick) {
			if (selectOnHover)
				singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
			if (openAfterDelay)
				singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
		}
		OpenStrategy.setOpenMethod(singleClickMethod);
	}

	/*
	 * Initializes the workbench fonts with the stored values.
	 */
	private void initializeFonts() {
		IPreferenceStore store = getPreferenceStore();
		FontRegistry registry = JFaceResources.getFontRegistry();

		//Iterate through the definitions and initialize thier
		//defaults in the preference store.
		FontDefinition[] definitions = FontDefinition.getDefinitions();
		ArrayList fontsToSet = new ArrayList();
		for (int i = 0; i < definitions.length; i++) {
			FontDefinition definition = definitions[i];
			String fontKey = definition.getId();
			installFont(fontKey, registry, store);
			String defaultsTo = definitions[i].getDefaultsTo();
			if (defaultsTo != null){
				PreferenceConverter.setDefault(
					store,
					definition.getId(),
					PreferenceConverter.
						getDefaultFontDataArray(store,defaultsTo));
				
				//If there is no value in the registry pass though the mapping
				if(!registry.hasValueFor(fontKey)) {
					fontsToSet.add(definition);
				}
			}
		}
		
		
		/*
		 * Now that all of the font have been initialized anything
		 * that is still at its defaults and has a defaults to
		 * needs to have its value set in the registry.
		 * Post process to be sure that all of the fonts have the correct
		 * setting before there is an update.
		 */		
		Iterator updateIterator = fontsToSet.iterator();
		while(updateIterator.hasNext()){
			FontDefinition update = (FontDefinition) updateIterator.next();
			registry.put(update.getId(),registry.getFontData(update.getDefaultsTo()));
		}
	}
	/*
	 * Installs the given font in the font registry.
	 * 
	 * @param fontKey the font key
	 * @param registry the font registry
	 * @param store the preference store from which to obtain font data
	 */
	private void installFont(String fontKey, FontRegistry registry, IPreferenceStore store) {
		if (store.isDefault(fontKey))
			return;
		FontData[] font = PreferenceConverter.getFontDataArray(store, fontKey);
		registry.put(fontKey, font);
	}
	
	/*
	 * Initialize the workbench images.
	 * 
	 * @since 3.0
	 */
	private void initializeImages(AboutInfo aboutInfo) {
		// initialize the product image obtained from the product info file
		ImageDescriptor descriptor = aboutInfo.getWindowImage();
		if (descriptor != null) {
			WorkbenchImages.getImageRegistry().put(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD, descriptor);
			Image image = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD);
			if (image != null) {
				Window.setDefaultImage(image);
			}
		} else {
			// Avoid setting a missing image as the window default image
			WorkbenchImages.getImageRegistry().put(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD, ImageDescriptor.getMissingImageDescriptor());
		}
	}
	
	/*
	 * Initialize the workbench colors.
	 * 
	 * @since 3.0
	 */
	private void initializeColors() {
		// @issue some colors are generic; some are app-specific
		WorkbenchColors.startup();
	}

	/*
	 * Returns true if the workbench is in the process of closing
	 */
	/* package */ boolean isClosing() {
		return isClosing;
	}
	
	/*
	 * Returns true if the workbench is in the process of starting
	 */
	/* package */ boolean isStarting() {
		return isStarting;
	}
	
 	/*
 	 * Creates a new workbench window.
 	 * 
 	 * @return the new workbench window
    */
 	private WorkbenchWindow newWorkbenchWindow() {
 		return new WorkbenchWindow(getNewWindowNumber());
	}

	/*
	 * If a perspective was specified on the command line (-perspective)
	 * then force that perspective to open in the active window.
	 */	
	private void forceOpenPerspective() {
		if (getWorkbenchWindowCount() == 0) {
			// there should be an open window by now, bail out.
			return;
		}
		
		String perspId = null;
		String[] commandLineArgs = Platform.getCommandLineArgs();
		for (int i = 0; i < commandLineArgs.length - 1; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-perspective")) { //$NON-NLS-1$
				perspId = commandLineArgs[i + 1];
				break;
			}
		}
		if (perspId == null) {
			return;
		}
		IPerspectiveDescriptor desc = getPerspectiveRegistry().findPerspectiveWithId(perspId);
		if (desc == null) {
			return;
		}

		IWorkbenchWindow win = getActiveWorkbenchWindow();
		if (win == null) {
			win = getWorkbenchWindows()[0];
		}
		try {
			showPerspective(perspId, win);
		} catch (WorkbenchException e) {
			String msg = "Workbench exception showing specified command line perspective on startup."; //$NON-NLS-1$
			WorkbenchPlugin.log(msg, new Status(Status.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e));
		}
	}

	/*
	 * Create the initial workbench window.
	 */
	private void openFirstTimeWindow() {
		
		// create the workbench window
		WorkbenchWindow newWindow = newWorkbenchWindow();
		newWindow.create();
		windowManager.add(newWindow);

		// Create the initial page.
		try {
			newWindow.openPage(getPerspectiveRegistry().getDefaultPerspective(), getAdviser().getDefaultWindowInput());
		} catch (WorkbenchException e) {
			ErrorDialog.openError(newWindow.getShell(), WorkbenchMessages.getString("Problems_Opening_Page"), //$NON-NLS-1$
			e.getMessage(), e.getStatus());
		}
		newWindow.open();
	}
	
	/*
	 * Create the workbench UI from a persistence file.
	 * 
	 * @return RESTORE_CODE_OK if a window was opened,
	 * RESTORE_CODE_RESET if no window was opened but one should be,
	 * and RESTORE_CODE_EXIT if the workbench should close immediately
	 */
	private int openPreviousWorkbenchState() {
		
		if (!getPreferenceStore().getBoolean(IWorkbenchPreferences.SHOULD_SAVE_WORKBENCH_STATE)) {
			return RESTORE_CODE_RESET;
		}
		// Read the workbench state file.
		final File stateFile = getWorkbenchStateFile();
		// If there is no state file cause one to open.
		if (!stateFile.exists())
			return RESTORE_CODE_RESET;

		final int result[] = { RESTORE_CODE_OK };
		Platform.run(new SafeRunnable(WorkbenchMessages.getString("ErrorReadingState")) { //$NON-NLS-1$
			public void run() throws Exception {
				FileInputStream input = new FileInputStream(stateFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
				IMemento memento = XMLMemento.createReadRoot(reader);

				// Validate known version format
				String version = memento.getString(IWorkbenchConstants.TAG_VERSION);
				boolean valid = false;
				for (int i = 0; i < VERSION_STRING.length; i++) {
					if (VERSION_STRING[i].equals(version)) {
						valid = true;
						break;
					}
				}
				if (!valid) {
					reader.close();
					MessageDialog.openError(
						(Shell) null,
						WorkbenchMessages.getString("Restoring_Problems"), //$NON-NLS-1$
					WorkbenchMessages.getString("Invalid_workbench_state_ve")); //$NON-NLS-1$
					stateFile.delete();
					result[0] = RESTORE_CODE_RESET;
					return;
				}

				// Validate compatible version format
				// We no longer support the release 1.0 format
				if (VERSION_STRING[0].equals(version)) {
					reader.close();
					boolean ignoreSavedState = new MessageDialog(
						null, 
						WorkbenchMessages.getString("Workbench.incompatibleUIState"), //$NON-NLS-1$
						null,
						WorkbenchMessages.getString("Workbench.incompatibleSavedStateVersion"), //$NON-NLS-1$
						MessageDialog.WARNING,
						new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
						0).open() == 0; 	// OK is the default
					if (ignoreSavedState) {
						stateFile.delete();
						result[0] = RESTORE_CODE_RESET;
					} else {
						result[0] = RESTORE_CODE_EXIT;
					}
					return;
				}

				// Restore the saved state
				IStatus restoreResult = restoreState(memento);
				reader.close();
				if (restoreResult.getSeverity() == IStatus.ERROR) {
					ErrorDialog.openError(
						null,
						WorkbenchMessages.getString("Workspace.problemsTitle"), //$NON-NLS-1$
						WorkbenchMessages.getString("Workbench.problemsRestoringMsg"), //$NON-NLS-1$
						restoreResult);
				}
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				result[0] = RESTORE_CODE_RESET;
				stateFile.delete();
			}

		});
		// ensure at least one window was opened
		if (result[0] == RESTORE_CODE_OK && windowManager.getWindows().length == 0)
			result[0] = RESTORE_CODE_RESET;
		return result[0];
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchWindow openWorkbenchWindow(IAdaptable input) throws WorkbenchException {
		return openWorkbenchWindow(getPerspectiveRegistry().getDefaultPerspective(), input);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchWindow openWorkbenchWindow(final String perspID, final IAdaptable input) throws WorkbenchException {
		// Run op in busy cursor.
		final Object[] result = new Object[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					result[0] = busyOpenWorkbenchWindow(perspID, input);
				} catch (WorkbenchException e) {
					result[0] = e;
				}
			}
		});
		if (result[0] instanceof IWorkbenchWindow) {
			return (IWorkbenchWindow) result[0];
		} else if (result[0] instanceof WorkbenchException) {
			throw (WorkbenchException) result[0];
		} else {
			throw new WorkbenchException(WorkbenchMessages.getString("Abnormal_Workbench_Conditi")); //$NON-NLS-1$
		}
	}

	/*
	 * Record the workbench UI in a document
	 */
	private XMLMemento recordWorkbenchState() {
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		IStatus status = saveState(memento);
		if (status.getSeverity() != IStatus.OK) {
			ErrorDialog.openError((Shell)null,
				WorkbenchMessages.getString("Workbench.problemsSaving"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Workbench.problemsSavingMsg"), //$NON-NLS-1$
			status);
		}
		return memento;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public boolean restart() {
		// this is the return code from run() to trigger a restart
		return close(PlatformUI.RETURN_RESTART, false);
	}
	
	/*
	 * Restores the state of the previously saved workbench
	 */
	private IStatus restoreState(IMemento memento) {

		MultiStatus result = new MultiStatus(
			PlatformUI.PLUGIN_ID,IStatus.OK,
			WorkbenchMessages.getString("Workbench.problemsRestoring"),null); //$NON-NLS-1$
		IMemento childMem;
		try {
			UIStats.start(UIStats.RESTORE_WORKBENCH,"MRUList"); //$NON-NLS-1$
			IMemento mruMemento = memento.getChild(IWorkbenchConstants.TAG_MRU_LIST); //$NON-NLS-1$
			if (mruMemento != null) {
				result.add(getEditorHistory().restoreState(mruMemento));
			}
		} finally {
			UIStats.end(UIStats.RESTORE_WORKBENCH,"MRUList"); //$NON-NLS-1$
		}
		// Get the child windows.
		IMemento[] children = memento.getChildren(IWorkbenchConstants.TAG_WINDOW);

		// Read the workbench windows.
		for (int x = 0; x < children.length; x++) {
			childMem = children[x];
			WorkbenchWindow newWindow = newWorkbenchWindow();
			newWindow.create();

			// allow the application to specify an initial perspective to open
			IPerspectiveDescriptor desc = getAdviser().getInitialWindowPerspective(newWindow.getWindowConfigurer());
			result.merge(newWindow.restoreState(childMem, desc));
			windowManager.add(newWindow);
			try {
				getAdviser().postWindowRestore(newWindow.getWindowConfigurer());
			} catch (WorkbenchException e) {
				result.add(e.getStatus());
			}
			newWindow.open();
		}
		return result;
	}

	/**
	 * Returns an array of all plugins that extend the 
	 * <code>org.eclipse.ui.startup</code> extension point.
	 */
	public IPluginDescriptor[] getEarlyActivatedPlugins() {
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_STARTUP);
		IExtension[] extensions = point.getExtensions();
		IPluginDescriptor result[] = new IPluginDescriptor[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			result[i] = extensions[i].getDeclaringPluginDescriptor();
		}
		return result;
	}
	
	/*
	 * Starts all plugins that extend the <code>org.eclipse.ui.startup</code>
	 * extension point, and that the user has not disabled via the preference page.
	 */
	private void startPlugins() {
		Runnable work = new Runnable() {
			IPreferenceStore store = getPreferenceStore();
			final String pref = store.getString(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
			public void run() {
				IPluginDescriptor descriptors[] = getEarlyActivatedPlugins();
				for (int i = 0; i < descriptors.length; i++) {
					final IPluginDescriptor pluginDescriptor = descriptors[i];
					SafeRunnable code = new SafeRunnable() {
						public void run() throws Exception {
							String id = pluginDescriptor.getUniqueIdentifier() + IPreferenceConstants.SEPARATOR;
							if (pref.indexOf(id) < 0) {
								Plugin plugin = pluginDescriptor.getPlugin();
								IStartup startup = (IStartup) plugin;
								startup.earlyStartup();
							}
						}
						public void handleException(Throwable exception) {
							WorkbenchPlugin.log("Unhandled Exception", new Status(IStatus.ERROR, "org.eclipse.ui", 0, "Unhandled Exception", exception)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					};
					Platform.run(code);
				}
			}
		};

		Thread thread = new Thread(work);
		thread.start();
	}

	/**
	 * Creates the <code>Display</code> to be used by the workbench.
	 */
	private Display createDisplay(AboutInfo aboutInfo) {
		// setup the application name used by SWT to lookup resources on some platforms
		String appName = aboutInfo.getAppName();
		if (appName != null) {
			Display.setAppName(appName);
		}
		
		// create the display
		Display display = null;
		if (Policy.DEBUG_SWT_GRAPHICS) {
			DeviceData data = new DeviceData();
			data.tracking = true;
			display = new Display(data);
		} else {
			display = new Display();
		}
		
		// workaround for 1GEZ9UR and 1GF07HN
		display.setWarnings(false);
		
		// react to display close event by closing the workhench nicely
		display.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = close();
			}
		});
		
		return display;
	}
	
	/**
	 * Internal method for running the workbench UI. This entails processing
	 * and dispatching events until the workbench is closed or restarted.
	 * 
	 * @return return code {@link PlatformUI#RETURN_OK RETURN_OK} for normal exit; 
	 * {@link PlatformUI#RETURN_RESTART RETURN_RESTART} if the workbench was terminated
	 * with a call to {@link IWorkbench#restart IWorkbench.restart}; 
	 * {@link PlatformUI#RETURN_UNSTARTABLE RETURN_UNSTARTABLE} if the workbench could
	 * not be started; other values reserved for future use
	 * @since 3.0
	 */
	private int runUI() {
		UIStats.start(UIStats.START_WORKBENCH,"Workbench"); //$NON-NLS-1$

		// get the primary feature info loaded
		AboutInfo aboutInfo = null;
		try {
			aboutInfo = getWorkbenchConfigurer().getPrimaryFeatureAboutInfo();
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Error reading primary feature's about.ini file", e.getStatus()); //$NON-NLS-1$
			return PlatformUI.RETURN_UNSTARTABLE;
		}

		// create and startup the display for the workbench
		Display display = createDisplay(aboutInfo);
				
		try {
			// install backstop to catch exceptions thrown out of event loop
			Window.IExceptionHandler handler = new ExceptionHandler();
			Window.setExceptionHandler(handler);
			
			// initialize workbench and restore or open one window
			boolean initOK = init(aboutInfo);
			
			// drop the splash screen now that a workbench window is up
			Platform.endSplash();
			
			// let the adviser run its start up code
			if (initOK) {
				adviser.postStartup(); // may trigger a close/restart
			}
			
			if (initOK && runEventLoop) {
				// start eager plug-ins
				startPlugins();
				
				display.asyncExec(new Runnable() {
					public void run() {
						UIStats.end(UIStats.START_WORKBENCH,"Workbench"); //$NON-NLS-1$
					}
				});
				
				// the event loop
				runEventLoop(handler, display);
			}
			
			// shutdown in an orderly way after event loop finishes
			shutdown();
		} finally {
			// mandatory clean up
			if (!display.isDisposed()) {
				display.dispose();
			}
		}
		
		// restart or exit based on returnCode
		Workbench.instance = null;
		return returnCode;
	}

	/*
	 * Runs an event loop for the workbench.
	 */
	private void runEventLoop(Window.IExceptionHandler handler, Display display) {
		runEventLoop = true;
		while (runEventLoop) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable t) {
				handler.handleException(t);
			}
		}
	}
	
	/*
	 * Saves the current state of the workbench so it can be restored later on
	 */
	private IStatus saveState(IMemento memento) {
		MultiStatus result = new MultiStatus(
			PlatformUI.PLUGIN_ID,IStatus.OK,
			WorkbenchMessages.getString("Workbench.problemsSaving"),null); //$NON-NLS-1$

		// Save the version number.
		memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING[1]);

		// Save the workbench windows.
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int nX = 0; nX < windows.length; nX++) {
			WorkbenchWindow window = (WorkbenchWindow) windows[nX];
			IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_WINDOW);
			result.merge(window.saveState(childMem));
		}
		result.add(getEditorHistory().saveState(memento.createChild(IWorkbenchConstants.TAG_MRU_LIST))); //$NON-NLS-1$
		return result;
	}
	
	/*
	 * Save the workbench UI in a persistence file.
	 */
	private boolean saveMementoToFile(XMLMemento memento) {
		// Save it to a file.
		File stateFile = getWorkbenchStateFile();
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
			writer.close();
		} catch (IOException e) {
			stateFile.delete();
			MessageDialog.openError((Shell) null, WorkbenchMessages.getString("SavingProblem"), //$NON-NLS-1$
			WorkbenchMessages.getString("ProblemSavingState")); //$NON-NLS-1$
			return false;
		}

		// Success !
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window) throws WorkbenchException {
		Assert.isNotNull(perspectiveId);

		// If the specified window has the requested perspective open, then the window
		// is given focus and the perspective is shown. The page's input is ignored.
		WorkbenchWindow win = (WorkbenchWindow) window;
		if (win != null) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				IPerspectiveDescriptor perspectives[] = page.getOpenedPerspectives();
				for (int i = 0; i < perspectives.length; i++) {
					IPerspectiveDescriptor persp = perspectives[i];
					if (perspectiveId.equals(persp.getId())) {
						win.getShell().open();
						page.setPerspective(persp);
						return page;
					}
				}
			}
		}

		// If another window that has the workspace root as input and the requested
		// perpective open and active, then the window is given focus.
		IAdaptable input = adviser.getDefaultWindowInput();
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			win = (WorkbenchWindow) windows[i];
			if (window != win) {
				WorkbenchPage page = win.getActiveWorkbenchPage();
				if (page != null) {
					boolean inputSame = false;
					if (input == null)
						inputSame = (page.getInput() == null);
					else
						inputSame = input.equals(page.getInput());
					if (inputSame) {
						Perspective persp = page.getActivePerspective();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							Shell shell = win.getShell();
							shell.open();
							if(shell.getMinimized())
								shell.setMinimized(false);
							return page;
						}
					}
				}
			}
		}

		// Otherwise the requested perspective is opened and shown in the specified
		// window or in a new window depending on the current user preference for opening
		// perspectives, and that window is given focus.
		win = (WorkbenchWindow) window;
		if (win != null) {
			IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
			int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
			IWorkbenchPage page = win.getActiveWorkbenchPage();
			IPerspectiveDescriptor persp = null;
			if (page != null)
				persp = page.getPerspective();

			// Only open a new window if user preference is set and the window
			// has an active perspective.
			if (IPreferenceConstants.OPM_NEW_WINDOW == mode && persp != null) {
				IWorkbenchWindow newWindow = openWorkbenchWindow(perspectiveId, input);
				return newWindow.getActivePage();
			} else {
				IPerspectiveDescriptor desc = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
				if (desc == null)
					throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
				win.getShell().open();
				if (page == null)
					page = win.openPage(perspectiveId, input);
				else
					page.setPerspective(desc);
				return page;
			}
		}

		// Just throw an exception....
		throw new WorkbenchException(WorkbenchMessages.format("Workbench.showPerspectiveError", new Object[] { perspectiveId })); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window, IAdaptable input) throws WorkbenchException {
		Assert.isNotNull(perspectiveId);

		// If the specified window has the requested perspective open and the same requested
		// input, then the window is given focus and the perspective is shown.
		boolean inputSameAsWindow = false;
		WorkbenchWindow win = (WorkbenchWindow) window;
		if (win != null) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				boolean inputSame = false;
				if (input == null)
					inputSame = (page.getInput() == null);
				else
					inputSame = input.equals(page.getInput());
				if (inputSame) {
					inputSameAsWindow = true;
					IPerspectiveDescriptor perspectives[] = page.getOpenedPerspectives();
					for (int i = 0; i < perspectives.length; i++) {
						IPerspectiveDescriptor persp = perspectives[i];
						if (perspectiveId.equals(persp.getId())) {
							win.getShell().open();
							page.setPerspective(persp);
							return page;
						}
					}
				}
			}
		}

		// If another window has the requested input and the requested
		// perpective open and active, then that window is given focus.
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			win = (WorkbenchWindow) windows[i];
			if (window != win) {
				WorkbenchPage page = win.getActiveWorkbenchPage();
				if (page != null) {
					boolean inputSame = false;
					if (input == null)
						inputSame = (page.getInput() == null);
					else
						inputSame = input.equals(page.getInput());
					if (inputSame) {
						Perspective persp = page.getActivePerspective();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							win.getShell().open();
							return page;
						}
					}
				}
			}
		}

		// If the specified window has the same requested input but not the requested
		// perspective, then the window is given focus and the perspective is opened and shown
		// on condition that the user preference is not to open perspectives in a new window.
		win = (WorkbenchWindow) window;
		if (inputSameAsWindow && win != null) {
			IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
			int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);

			if (IPreferenceConstants.OPM_NEW_WINDOW != mode) {
				IWorkbenchPage page = win.getActiveWorkbenchPage();
				IPerspectiveDescriptor desc = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
				if (desc == null)
					throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
				win.getShell().open();
				if (page == null)
					page = win.openPage(perspectiveId, input);
				else
					page.setPerspective(desc);
				return page;
			}
		}

		// If the specified window has no active perspective, then open the
		// requested perspective and show the specified window.
		if (win != null) {
			IWorkbenchPage page = win.getActiveWorkbenchPage();
			IPerspectiveDescriptor persp = null;
			if (page != null)
				persp = page.getPerspective();
			if (persp == null) {
				IPerspectiveDescriptor desc = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
				if (desc == null)
					throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
				win.getShell().open();
				if (page == null)
					page = win.openPage(perspectiveId, input);
				else
					page.setPerspective(desc);
				return page;
			}
		}

		// Otherwise the requested perspective is opened and shown in a new window, and the
		// window is given focus.
		IWorkbenchWindow newWindow = openWorkbenchWindow(perspectiveId, input);
		return newWindow.getActivePage();
	}

	/*
	 * Shuts down the application.
	 */
	private void shutdown() {
		// shutdown application-specific portions first
		adviser.postShutdown();
		
		// shutdown the rest of the workbench
		WorkbenchColors.shutdown();
		JFaceColors.disposeColors();
		if(getDecoratorManager() != null) {
			((DecoratorManager) getDecoratorManager()).shutdown();
		}
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IDecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	/*
	 * Returns the workbench window which was last known being
	 * the active one, or <code>null</code>.
	 */
	private WorkbenchWindow getActivatedWindow() {
		if (activatedWindow != null) {
			Shell shell = activatedWindow.getShell();
			if (shell != null && !shell.isDisposed()) {
				return activatedWindow;
			}
		}
		
		return null;
	}
	
	/*
	 * Sets the workbench window which was last known being the
	 * active one, or <code>null</code>.
	 */
	/* package */ void setActivatedWindow(WorkbenchWindow window) {
		activatedWindow = window;
	}
	
	/**
	 * Returns the unique object that applications use to configure the
	 * workbench.
	 * <p>
	 * IMPORTANT This method is declared package-private to prevent regular
	 * plug-ins from downcasting IWorkbench to Workbench and getting
	 * hold of the workbench configurer that would allow them to tamper with the
	 * workbench. The workbench configurer is available only to the application.
	 * </p>
	 */
	/* package */ WorkbenchConfigurer getWorkbenchConfigurer() {
		if (workbenchConfigurer == null) {
			workbenchConfigurer = new WorkbenchConfigurer();
		}
		return workbenchConfigurer;
	}
	
	/**
	 * Returns the workbench adviser that created this workbench.
	 * <p>
	 * IMPORTANT This method is declared package-private to prevent regular
	 * plug-ins from downcasting IWorkbench to Workbench and getting
	 * hold of the workbench adviser that would allow them to tamper with the
	 * workbench. The workbench adviser is internal to the application.
	 * </p>
	 */
	/* package */ WorkbenchAdviser getAdviser() {
		return adviser;
	}
	
	/**
	 * Returns the default workbench window input.
	 * 
	 * @return the default window input or <code>null</code> if none
	 */
	public IAdaptable getDefaultWindowInput() {
		return getAdviser().getDefaultWindowInput();
	}
}