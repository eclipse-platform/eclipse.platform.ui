/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.PerspectiveManager;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.internal.ui.views.console.ConsoleDocumentManager;
import org.eclipse.debug.internal.ui.views.memory.IMemoryBlockViewSynchronizer;
import org.eclipse.debug.internal.ui.views.memory.MemoryBlockViewSynchronizer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;

/**
 * The Debug UI Plugin.
 *
 */
public class DebugUIPlugin extends AbstractUIPlugin implements ILaunchListener {
	
	/**
	 * Unique identifier constant (value <code>"org.eclipse.debug.ui"</code>)
	 * for the Debug UI plug-in.
	 */
	private static final String PI_DEBUG_UI = "org.eclipse.debug.ui"; //$NON-NLS-1$
	
	/**
	 * The singleton debug plugin instance
	 */
	private static DebugUIPlugin fgDebugUIPlugin= null;
	
	/**
	 * A utility presentation used to obtain labels
	 */
	protected static IDebugModelPresentation fgPresentation = null;

	/**
	 * Default label provider
	 */	
	private static DefaultLabelProvider fgDefaultLabelProvider;
	
	/**
	 * Launch configuration attribute - used by the stand-in launch
	 * config working copies that are created while a launch is waiting
	 * for a build to finish. This attribute allows the EditLaunchConfigurationAction
	 * to access the original config if the user asks to edit it.
	 */
	public static String ATTR_LAUNCHING_CONFIG_HANDLE= getUniqueIdentifier() + "launching_config_handle"; //$NON-NLS-1$
	
	/**
	 * Flag indicating whether the debug UI is in trace
	 * mode. When in trace mode, extra debug information
	 * is produced.
	 */
	private boolean fTrace = false;	
	
	/**
	 * Singleton console document manager
	 */
	private ConsoleDocumentManager fConsoleDocumentManager = null;
	
	/**
	 * Perspective manager
	 */
	private PerspectiveManager fPerspectiveManager = null;
	
	/**
	 * Launch configuration manager
	 */
	private LaunchConfigurationManager fLaunchConfigurationManager = null;
	
	/**
	 * Step filter manager
	 */
	private StepFilterManager fStepFilterManager = null;
	
	
	private MemoryBlockViewSynchronizer fMemBlkViewSynchronizer = null;
	
	/**
	 * Returns whether the debug UI plug-in is in trace
	 * mode.
	 * 
	 * @return whether the debug UI plug-in is in trace
	 *  mode
	 */
	public boolean isTraceMode() {
		return fTrace;
	}
	
	/**
	 * Logs the given message if in trace mode.
	 * 
	 * @param String message to log
	 */
	public static void logTraceMessage(String message) {
		if (getDefault().isTraceMode()) {
			IStatus s = new Status(IStatus.WARNING, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, message, null);
			getDefault().getLog().log(s);
		}
	}

	/**
	 * Constructs the debug UI plugin
	 */
	public DebugUIPlugin() {
		super();
		fgDebugUIPlugin= this;
	}
		
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Returns the singleton instance of the debug plugin.
	 */
	public static DebugUIPlugin getDefault() {
		return fgDebugUIPlugin;
	}
	
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PI_DEBUG_UI;
	}

	public static IDebugModelPresentation getModelPresentation() {
		if (fgPresentation == null) {
			fgPresentation = new DelegatingModelPresentation();
		}
		return fgPresentation;
	}
	
	public LaunchConfigurationManager getLaunchConfigurationManager() {
		if (fLaunchConfigurationManager == null) {
			fLaunchConfigurationManager = new LaunchConfigurationManager();
		} 
		return fLaunchConfigurationManager;
	}


	public IMemoryBlockViewSynchronizer getMemoryBlockViewSynchronizer(){
	
		if (fMemBlkViewSynchronizer == null) {
			fMemBlkViewSynchronizer = new MemoryBlockViewSynchronizer();
		}
		
		return fMemBlkViewSynchronizer;
	}
	
	/**
	 * Returns the currently active workbench window or <code>null</code>
	 * if none.
	 * 
	 * @return the currently active workbench window or <code>null</code>
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the currently active workbench window shell or <code>null</code>
	 * if none.
	 * 
	 * @return the currently active workbench window shell or <code>null</code>
	 */
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}

	/**
	 * Returns the default label provider for the debug UI.
	 */
	public static ILabelProvider getDefaultLabelProvider() {
		if (fgDefaultLabelProvider == null) {
			fgDefaultLabelProvider = new DefaultLabelProvider();
		}
		return fgDefaultLabelProvider;
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
		Bundle bundle = Platform.getBundle(element.getDeclaringExtension().getNamespace());
		if (bundle.getState() == Bundle.ACTIVE) {
			return element.createExecutableExtension(classAttribute);
		} 
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
		if (exc[0] != null) {
			throw exc[0];
		}
		return ret[0];
	}	
	
	protected ImageRegistry createImageRegistry() {
		return DebugPluginImages.initializeImageRegistry();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			if (fPerspectiveManager != null) {
				fPerspectiveManager.shutdown();
			}
			if (fLaunchConfigurationManager != null) {
				fLaunchConfigurationManager.shutdown();
			}
			if (fConsoleDocumentManager != null) {
				fConsoleDocumentManager.shutdown();
			}
			if (fStepFilterManager != null) {
				fStepFilterManager.shutdown();
			}
			
			ColorManager.getDefault().dispose();
			
			if (fgPresentation != null) {
				fgPresentation.dispose();
			}
			
			if (fMemBlkViewSynchronizer != null){
				fMemBlkViewSynchronizer.shutdown();
			}
		} finally {
			super.stop(context);
		}
	}

	/**
	 * @see AbstractUIPlugin#startup()
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		// Listen to launches to lazily create "launch processors"
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		
		IAdapterManager manager= Platform.getAdapterManager();
		DebugUIPropertiesAdapterFactory propertiesFactory = new DebugUIPropertiesAdapterFactory();
		manager.registerAdapters(propertiesFactory, IDebugElement.class);
		manager.registerAdapters(propertiesFactory, IProcess.class);
		DebugUIAdapterFactory uiFactory = new DebugUIAdapterFactory();
		manager.registerAdapters(uiFactory, ILaunchConfiguration.class);
		manager.registerAdapters(uiFactory, ILaunchConfigurationType.class);
		getStandardDisplay().asyncExec(
			new Runnable() {
				public void run() {
					//initialize the selected resource manager
					SelectedResourceManager.getDefault();
				}
			});	
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore prefs) {
		//Debug PreferencePage
		prefs.setDefault(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, MessageDialogWithToggle.PROMPT);
		prefs.setDefault(IDebugUIConstants.PREF_SHOW_DEBUG_PERSPECTIVE_DEFAULT, IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
		prefs.setDefault(IDebugUIConstants.PREF_SHOW_RUN_PERSPECTIVE_DEFAULT, IDebugUIConstants.PERSPECTIVE_NONE);
		prefs.setDefault(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, true);
		prefs.setDefault(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.NEVER);
		prefs.setDefault(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.PROMPT);
		prefs.setDefault(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, MessageDialogWithToggle.ALWAYS);
		prefs.setDefault(IDebugUIConstants.PREF_REUSE_EDITOR, true);
		prefs.setDefault(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, false);
		prefs.setDefault(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, MessageDialogWithToggle.NEVER);
		prefs.setDefault(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, MessageDialogWithToggle.PROMPT);
		prefs.setDefault(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK, false);
		
		//View Management preference page
		prefs.setDefault(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
		prefs.setDefault(IInternalDebugUIConstants.PREF_TRACK_VIEWS, true);
		
		//ConsolePreferencePage
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_WRAP, false);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_WIDTH, 80);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK, 80000);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK, 100000);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH, 8);
		
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR, new RGB(0, 200, 125));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR, new RGB(255, 0, 0));
		
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.MEMORY_VIEW_UNBUFFERED_LINE_COLOR, new RGB(114, 119, 129));
		
		//LaunchHistoryPreferencePage
		prefs.setDefault(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, 10);
		
		//VariableViewsPreferencePage
		prefs.setDefault(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR, new RGB(255, 0, 0));
		prefs.setDefault(IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP, false);
		
		//Registers View
		prefs.setDefault(IDebugPreferenceConstants.REGISTERS_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		
		// Variable/Expression view default settings
		prefs.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + '+' + "org.eclipse.debug.ui.ShowDetailPaneAction", true); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + '+' + "org.eclipse.debug.ui.ShowDetailPaneAction", true); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + '+' + "org.eclipse.debug.ui.ShowTypeNamesAction", false); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + '+' + "org.eclipse.debug.ui.ShowTypeNamesAction", false);		 //$NON-NLS-1$
		
		// Step filter preferences
		prefs.setDefault(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS, false);
		
		// set default for column size preference
		prefs.setDefault(IDebugPreferenceConstants.PREF_COLUMN_SIZE, 
				IDebugPreferenceConstants.PREF_COLUMN_SIZE_DEFAULT);
	}

	protected IProcess getProcessFromInput(Object input) {
		IProcess processInput= null;
		if (input instanceof IProcess) {
			processInput= (IProcess) input;
		} else
			if (input instanceof ILaunch) {
				IDebugTarget target= ((ILaunch) input).getDebugTarget();
				if (target != null) {
					processInput= target.getProcess();
				} else {
					IProcess[] processes= ((ILaunch) input).getProcesses();
					if ((processes != null) && (processes.length > 0)) {
						processInput= processes[0];
					}
				}
			} else
				if (input instanceof IDebugElement) {
					processInput= ((IDebugElement) input).getDebugTarget().getProcess();
				}

		return processInput;
	}

	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message= null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}
	
	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Error within Debug UI: ", t); //$NON-NLS-1$
			log(status);	
		}
		ErrorDialog.openError(shell, title, message, status);
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		log(newErrorStatus("Error logged from Debug UI: ", t)); //$NON-NLS-1$
	}
	
	/**
	 * Logs the given message if in debug mode.
	 * 
	 * @param String message to log
	 */
	public static void logDebugMessage(String message) {
		if (getDefault().isDebugging()) {
			logErrorMessage(message);
		}
	}
	
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message the error message to log
	 */
	public static void logErrorMessage(String message) {
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log(newErrorStatus("Internal message logged from Debug UI: " + message, null)); //$NON-NLS-1$	
	}
	
	/**
	 * Returns a new error status for this plugin with the given message
	 * @param message the message to be included in the status
	 * @param exception the exception to be included in the status or <code>null</code> if none
	 * @return a new error status
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, message, exception);
	}
	
	/**
	 * Save all dirty editors in the workbench.
	 * Returns whether the operation succeeded.
	 * 
	 * @return whether all saving was completed
	 */
	protected static boolean saveAllEditors(boolean confirm) {
		if (getActiveWorkbenchWindow() == null) {
			return false;
		}
		return PlatformUI.getWorkbench().saveAllEditors(confirm);
	}	
	
	/**
	 * Save & build the workspace according to the user-specified preferences.  Return <code>false</code> if
	 * any problems were encountered, <code>true</code> otherwise.
	 */
	public static boolean saveAndBuild() {
		boolean status = true;
		String saveDirty = getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		boolean buildBeforeLaunch = getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH);
		boolean autobuilding = ResourcesPlugin.getWorkspace().isAutoBuilding();
		
		// If we're ignoring dirty editors, check if we need to build
		if (saveDirty.equals(MessageDialogWithToggle.NEVER)) {
			if (buildBeforeLaunch) {
				return doBuild();
			}
		} else {
			status = saveAllEditors(saveDirty.equals(MessageDialogWithToggle.PROMPT));
			if (status && !autobuilding && buildBeforeLaunch) {
				status = doBuild();
			}
		}
				
		return status;
	}
	
	private static boolean doBuild() {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
			return false;
		} catch (InvocationTargetException e) {
			String title= DebugUIMessages.getString("DebugUIPlugin.Run/Debug_1"); //$NON-NLS-1$
			String message= DebugUIMessages.getString("DebugUIPlugin.Build_error._Check_log_for_details._2"); //$NON-NLS-1$
			Throwable t = e.getTargetException();
			errorDialog(getShell(), title, message, t);
			return false;
		}
		return true;
	}	
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}	
	
	/**
	 * Returns the a color based on the type of output.
	 * Valid types:
	 * <li>CONSOLE_SYS_OUT_RGB</li>
	 * <li>CONSOLE_SYS_ERR_RGB</li>
	 * <li>CONSOLE_SYS_IN_RGB</li>
	 * <li>CHANGED_VARIABLE_RGB</li>
	 */
	public static Color getPreferenceColor(String type) {
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(getDefault().getPreferenceStore(), type));
	}

	/**
	 * Returns the console document manager. The manager will be created lazily on 
	 * the first access.
	 * 
	 * @return ConsoleDocumentManager
	 */
	public ConsoleDocumentManager getConsoleDocumentManager() {
		if (fConsoleDocumentManager == null) {
			fConsoleDocumentManager = new ConsoleDocumentManager();
		}
		return fConsoleDocumentManager;
	}
	
	/**
	 * Serializes a XML document into a string - encoded in UTF8 format,
	 * with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 * @throws TransformerException if an unrecoverable error occurs during the serialization
	 * @throws IOException if the encoding attempted to be used is not supported
	 */
	public static String serializeDocument(Document doc) throws TransformerException, IOException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		
		TransformerFactory factory= TransformerFactory.newInstance();
		
		Transformer transformer= factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		
		DOMSource source= new DOMSource(doc);
		StreamResult outputTarget= new StreamResult(s);
		transformer.transform(source, outputTarget);
		
		return s.toString("UTF8"); //$NON-NLS-1$			
	}
	
	/**
	 * Returns a Document that can be used to build a DOM tree
	 * @return the Document
	 * @throws ParserConfigurationException if an exception occurs creating the document builder
	 * @since 3.0
	 */
	public static Document getDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dfactory= DocumentBuilderFactory.newInstance();

		DocumentBuilder docBuilder= dfactory.newDocumentBuilder();
		Document doc= docBuilder.newDocument();
		return doc;
	}
	
	/**
	 * Determines and returns the selection in the specified window.  If nothing is
	 * actually selected, look for an active editor.
	 */
	public static IStructuredSelection resolveSelection(IWorkbenchWindow window) {
		if (window == null) {
			return null;
		}
		ISelection selection= window.getSelectionService().getSelection();
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			// there is no obvious selection - go fishing
			selection= null;
			IWorkbenchPage page= window.getActivePage();
			if (page == null) {
				//workspace is closed
				return null;
			}

			// first, see if there is an active editor, and try its input element
			IEditorPart editor= page.getActiveEditor();
			Object element= null;
			if (editor != null) {
				element= editor.getEditorInput();
			}

			if (selection == null && element != null) {
				selection= new StructuredSelection(element);
			}
		}
		return (IStructuredSelection)selection;
	}
	
	/**
	 * When the first launch is added, instantiate launch processors,
	 * and stop listening to launch notifications.
	 * <p>
	 * Launch processors are:
	 * <ul>
	 * <li>console document manager</li>
	 * <li>perspective manager</li>
	 * </ul>
	 * </p>
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		getConsoleDocumentManager().startup();
		
		if (fPerspectiveManager == null) {
			PerspectiveManager manager = getPerspectiveManager();
			manager.launchAdded(launch);
		}
		
		if (fStepFilterManager == null) {
			getStepFilterManager().launchAdded(launch);
		}
		
		getLaunchConfigurationManager().startup();
	}
	
	/**
	 * Returns the persepective manager - instantiating it if required.
	 * 
	 * @return
	 */
	public PerspectiveManager getPerspectiveManager() {
		if (fPerspectiveManager == null) {
			fPerspectiveManager = new PerspectiveManager();
			fPerspectiveManager.startup();			
		}
		return fPerspectiveManager;
	}
	
	/**
	 * Returns the singleton step filter manager.
	 * 
	 * @return the step filter manager
	 */
	public StepFilterManager getStepFilterManager() {
		if (fStepFilterManager == null) {
			fStepFilterManager = new StepFilterManager();
		}
		return fStepFilterManager;
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}

	/**
	 * Save dirty editors before launching, according to preferences.
	 * 
	 * @return whether to proceed with launch 
	 */
	public static boolean preLaunchSave() {
		String saveDirty = getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		if (saveDirty.equals(MessageDialogWithToggle.NEVER)) {
			return true;
		} 
		return saveAllEditors(saveDirty.equals(MessageDialogWithToggle.PROMPT));
	}
	
	/**
	 * Builds the workspace (according to preferences) and launches the given launch
	 * configuration in the specified mode. May return null if autobuild is in process and 
	 * user cancels the launch.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode launch mode - run or debug
	 * @param monitor progress monitor
	 * @exception CoreException if an exception occurs while building or launching
	 * @return resulting launch or <code>null</code> if user cancels
	 */
	public static ILaunch buildAndLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		boolean buildBeforeLaunch = getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH);
		IProgressMonitor subMonitor = monitor;
		String message = MessageFormat.format("{0}...", new String[]{configuration.getName()}); //$NON-NLS-1$
		if (buildBeforeLaunch) {
			monitor.beginTask(message, 200);
			return configuration.launch(mode, monitor, true);	
		} 
		subMonitor = monitor;
		subMonitor.beginTask(message, 100);
		return configuration.launch(mode, subMonitor); 
	}
	
	/**
	 * Saves and builds the workspace according to current preference settings and
	 * launches the given launch configuration in the specified mode in the
	 * foreground with a progress dialog. Reports any exceptions that occur
	 * in an error dialog.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode launch mode
	 * @since 3.0
	 */
	public static void launchInForeground(final ILaunchConfiguration configuration, final String mode) {
		if (!DebugUIPlugin.preLaunchSave()) {
			return;
		}
			
		final IJobManager jobManager = Platform.getJobManager();
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean wait = false;
		
		if (jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0 || jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD).length >0) {
			String waitForBuild = store.getString(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD);

			if (waitForBuild.equals(MessageDialogWithToggle.PROMPT)) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(getShell(), DebugUIMessages.getString("DebugUIPlugin.23"), DebugUIMessages.getString("DebugUIPlugin.24"), null, false, store, IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD); //$NON-NLS-1$ //$NON-NLS-2$
				
				switch (dialog.getReturnCode()) {
					case IDialogConstants.CANCEL_ID:
						return;
					case IDialogConstants.YES_ID:
						wait = false;
						break;
					case IDialogConstants.NO_ID:
						wait = true;
						break;
				}
			} else if (waitForBuild.equals(MessageDialogWithToggle.ALWAYS)) {
				wait = true;
			}
		}

		if (wait) {
			IWorkbench workbench = DebugUIPlugin.getDefault().getWorkbench();
			IProgressService progressService = workbench.getProgressService();
			final IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
						jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
					} catch (InterruptedException e) {
						// continue
					}
					if (!monitor.isCanceled()) {
						try {
							buildAndLaunch(configuration, mode, monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				}		
			};			
			try {
				progressService.busyCursorWhile(runnable);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e2) {
				handleInvocationTargetException(e2, configuration, mode);
			}
		} else {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						buildAndLaunch(configuration, mode, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}		
			};
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				handleInvocationTargetException(e, configuration, mode);
			} catch (InterruptedException e) {
			}							

		}
	}
	
	private static void handleInvocationTargetException(InvocationTargetException e, ILaunchConfiguration configuration, String mode) {
		Throwable targetException = e.getTargetException();
		Throwable t = e;
		if (targetException instanceof CoreException) {
			t = targetException;
		}
		if (t instanceof CoreException) {
			CoreException ce = (CoreException)t;
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(ce.getStatus());
			if (handler != null) {
				ILaunchGroup group = DebugUITools.getLaunchGroup(configuration, mode);
				if (group != null) {
					DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(configuration), group.getIdentifier(), ce.getStatus());
					return;
				}
			}
		}
		DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIMessages.getString("DebugUITools.Error_1"), DebugUIMessages.getString("DebugUITools.Exception_occurred_during_launch_2"), t); //$NON-NLS-1$ //$NON-NLS-2$		
	}
	
	/**
	 * Saves and builds the workspace according to current preference settings and
	 * launches the given launch configuration in the specified mode in a background
	 * Job with progress reported via the Job. Exceptions are reported in the Progress
	 * view.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode launch mode
	 * @since 3.0
	 */
	public static void launchInBackground(final ILaunchConfiguration configuration, final String mode) {
		if (!DebugUIPlugin.preLaunchSave()) {
			return;
		}

		final IJobManager jobManager = Platform.getJobManager();
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();

		boolean wait = (jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0) || (jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD).length > 0);
		
		String waitPref = store.getString(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD);
		if (wait) { // if there are build jobs running, do we wait or not??
			if (waitPref.equals(MessageDialogWithToggle.PROMPT)) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(getShell(), DebugUIMessages.getString("DebugUIPlugin.23"), DebugUIMessages.getString("DebugUIPlugin.24"), null, false, store, IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD); //$NON-NLS-1$ //$NON-NLS-2$
				
				switch (dialog.getReturnCode()) {
					case IDialogConstants.CANCEL_ID:
						return;
					case IDialogConstants.YES_ID:
						wait = true;
						break;
					case IDialogConstants.NO_ID:
						wait = false;
						break;
				}
			} else {
				wait = waitPref.equals(MessageDialogWithToggle.ALWAYS);
			}
		}
		
		final boolean waitInJob = wait;
		Job job = new Job(DebugUIMessages.getString("DebugUITools.3")) { //$NON-NLS-1$
			public IStatus run(IProgressMonitor monitor) {
				try {
					if(waitInJob) {
						try {
							jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
							jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
						} catch (InterruptedException e) {
							// just continue.
						}
					}
					
					if (!monitor.isCanceled()) {
						buildAndLaunch(configuration, mode, monitor);
					}
					
				} catch (CoreException e) {
					final IStatus status= e.getStatus();
					IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
					if (handler == null) {
						return status;
					}
					final ILaunchGroup group = DebugUITools.getLaunchGroup(configuration, mode);
					if (group == null) {
						return status;
					}
					Runnable r = new Runnable() {
						public void run() {
							DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(configuration), group.getIdentifier(), status);
						}	
					};
					DebugUIPlugin.getStandardDisplay().asyncExec(r);
				}
				return Status.OK_STATUS;
			}
		};

		IWorkbench workbench = DebugUIPlugin.getDefault().getWorkbench();
		IProgressService progressService = workbench.getProgressService();

		job.setPriority(Job.INTERACTIVE);
		job.setName(DebugUIMessages.getString("DebugUITools.8")); //$NON-NLS-1$
		if (wait) {
			progressService.showInDialog(workbench.getActiveWorkbenchWindow().getShell(), job); 
		}
		job.schedule();
	}

	/**
	 * Returns the label with any acclerators removed.
	 * 
	 * @return label without accelerators
	 */
	public static String removeAccelerators(String label) {
		String title = label;
		if (title != null) {
			// strip out any '&' (accelerators)
			int index = title.indexOf('&');
			if (index == 0) {
				title = title.substring(1);
			} else if (index > 0 && index < (title.length() - 1)){
				String first = title.substring(0, index);
				String last = title.substring(index + 1);
				title = first + last;
			}		
		}
		return title;
	}
}

