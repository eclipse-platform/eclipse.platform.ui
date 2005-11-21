/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.contexts.SuspendTriggerAdapterFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.PerspectiveManager;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupFacility;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupManager;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointOrganizerManager;
import org.eclipse.debug.internal.ui.views.breakpoints.OtherBreakpointCategory;
import org.eclipse.debug.internal.ui.views.console.ProcessConsoleManager;
import org.eclipse.debug.internal.ui.views.launch.DebugElementAdapterFactory;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;
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
	private static DebugUIPlugin fgDebugUIPlugin = null;
	
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
	private ProcessConsoleManager fProcessConsoleManager = null;
	
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
    
    /**
     * Image descriptor registry used for images with common overlays.
     * 
     * @since 3.1
     */
    private ImageDescriptorRegistry fImageDescriptorRegistry;
    
    /**
     * Service tracker and service used for finding plug-ins associated
     * with a class.
     */
    private ServiceTracker fServiceTracker;
    private PackageAdmin fPackageAdminService;
	
    /**
     * Dummy launch node representing a launch that is waiting
     * for a build to finish before proceeding. This node exists
     * to provide immediate feedback to the user in the Debug view and
     * allows termination, which equates to cancellation of the launch.
     */
	public static class PendingLaunch extends Launch {
        private Job fJob;
        public PendingLaunch(ILaunchConfiguration launchConfiguration, String mode, Job job) {
            super(launchConfiguration, mode, null);
            fJob= job;
        }

        // Allow the user to terminate the dummy launch as a means of
        // cancelling the launch while waiting for a build to finish.
        public boolean canTerminate() {
            return true;
        }

        public void terminate() throws DebugException {
            fJob.cancel();
        }
    }
	
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
	 * Returns the bundle the given class originated from.
	 * 
	 * @param clazz a class
	 * @return the bundle the given class originated from, or <code>null</code>
	 * 	if unable to be determined
	 * @since 3.1
	 */
	public Bundle getBundle(Class clazz) {
		if (fPackageAdminService != null) {
			return fPackageAdminService.getBundle(clazz);
		}
		return null;
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
		Bundle bundle = Platform.getBundle(element.getNamespace());
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
            if (fProcessConsoleManager != null) {
                fProcessConsoleManager.shutdown();
            }
            
            BreakpointOrganizerManager.getDefault().shutdown();
            
			if (fPerspectiveManager != null) {
				fPerspectiveManager.shutdown();
			}
			if (fLaunchConfigurationManager != null) {
				fLaunchConfigurationManager.shutdown();
			}
			
			if (fStepFilterManager != null) {
				fStepFilterManager.shutdown();
			}
			
			ColorManager.getDefault().dispose();
			
			if (fgPresentation != null) {
				fgPresentation.dispose();
			}
            
            if (fImageDescriptorRegistry != null) {
                fImageDescriptorRegistry.dispose();
            }
            
            SourceLookupFacility.shutdown();
			
			DebugElementHelper.dispose();
			
			fServiceTracker.close();
			fPackageAdminService = null;
			
		} finally {
			super.stop(context);
		}
	}

	/**
	 * @see AbstractUIPlugin#startup()
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		// make sure the perspective manager is created
		// and be the first debug event listener
		fPerspectiveManager = new PerspectiveManager();
		fPerspectiveManager.startup();		
		
		// Listen to launches to lazily create "launch processors"
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
        
        // start the breakpoint organizer manager
        BreakpointOrganizerManager.getDefault();
		
		IAdapterManager manager= Platform.getAdapterManager();
		DebugElementAdapterFactory propertiesFactory = new DebugElementAdapterFactory();
		manager.registerAdapters(propertiesFactory, ILaunchManager.class);
		manager.registerAdapters(propertiesFactory, ILaunch.class);
		manager.registerAdapters(propertiesFactory, IDebugTarget.class);
		manager.registerAdapters(propertiesFactory, IProcess.class);
		manager.registerAdapters(propertiesFactory, IThread.class);
		manager.registerAdapters(propertiesFactory, IStackFrame.class);
		manager.registerAdapters(propertiesFactory, IRegisterGroup.class);
		manager.registerAdapters(propertiesFactory, IVariable.class);
		manager.registerAdapters(propertiesFactory, IRegister.class);
		manager.registerAdapters(propertiesFactory, IExpression.class);
		manager.registerAdapters(propertiesFactory, IExpressionManager.class);
        manager.registerAdapters(propertiesFactory, OtherBreakpointCategory.class);
        manager.registerAdapters(propertiesFactory, IDebugElement.class);
		DebugUIAdapterFactory uiFactory = new DebugUIAdapterFactory();
		manager.registerAdapters(uiFactory, ILaunchConfiguration.class);
		manager.registerAdapters(uiFactory, ILaunchConfigurationType.class);
		SuspendTriggerAdapterFactory factory = new SuspendTriggerAdapterFactory();
		manager.registerAdapters(factory, ILaunch.class);
		getStandardDisplay().asyncExec(
			new Runnable() {
				public void run() {
					//initialize the selected resource manager
					SelectedResourceManager.getDefault();
					// forces launch shortcuts to be intialized so their key-bindings work
					getLaunchConfigurationManager().getLaunchShortcuts();
				}
			});	
		
		fServiceTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
		fServiceTracker.open();
		fPackageAdminService = (PackageAdmin) fServiceTracker.getService();
		
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
	 * @deprecated Saving has been moved to the launch delegate <code>LaunchConfigurationDelegate</code> to allow for scoped saving
	 * of resources that are only involved in the current launch, no longer the entire workspace
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
	 * 
	 * @deprecated this method is no longer to be used. It is an artifact from 2.0, and all saving is now done with the
	 * launch delegate <code>LaunchConfigurationDelegate</code>
	 */
	public static boolean saveAndBuild() {
		boolean status = true;
		String saveDirty = getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		boolean buildBeforeLaunch = getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH);
		
		// If we're ignoring dirty editors, check if we need to build
		if (saveDirty.equals(MessageDialogWithToggle.NEVER)) {
			if (buildBeforeLaunch) {
				return doBuild();
			}
		} else {
			status = saveAllEditors(saveDirty.equals(MessageDialogWithToggle.PROMPT));
			if (status && buildBeforeLaunch) {
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
			String title= DebugUIMessages.DebugUIPlugin_Run_Debug_1; 
			String message= DebugUIMessages.DebugUIPlugin_Build_error__Check_log_for_details__2; 
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
	 * Returns the process console manager. The manager will be created lazily on 
	 * the first access.
	 * 
	 * @return ProcessConsoleManager
	 */
	public ProcessConsoleManager getProcessConsoleManager() {
		if (fProcessConsoleManager == null) {
			fProcessConsoleManager = new ProcessConsoleManager();
		}
		return fProcessConsoleManager;
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
		getProcessConsoleManager().startup();
		
		if (fStepFilterManager == null) {
			getStepFilterManager().launchAdded(launch);
		}
		
		getLaunchConfigurationManager().startup();
		SourceLookupManager.getDefault();
	}
	
	/**
	 * Returns the persepective manager.
	 * 
	 * @return
	 */
	public PerspectiveManager getPerspectiveManager() {
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
	public void launchChanged(ILaunch launch) {}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {}

	/**
	 * Save dirty editors before launching, according to preferences.
	 * 
	 * @return whether to proceed with launch 
	 * @deprecated Saving has been moved to the launch delegate <code>LaunchConfigurationDelegate</code> to allow for scoped saving
	 * of resources that are only involved in the current launch, no longer the entire workspace
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
		final IJobManager jobManager = Platform.getJobManager();
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean wait = false;
		
		if (jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0 || jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD).length >0) {
			String waitForBuild = store.getString(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD);

			if (waitForBuild.equals(MessageDialogWithToggle.PROMPT)) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(getShell(), DebugUIMessages.DebugUIPlugin_23, DebugUIMessages.DebugUIPlugin_24, null, false, store, IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD); // 
				
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
						jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
						jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
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
			if ((ce.getStatus().getSeverity() & (IStatus.ERROR | IStatus.WARNING)) == 0) {
				// If the exception is a CoreException with a status other
				// than ERROR or WARNING, don't open an error dialog.
				return;
			}
		}
		DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIMessages.DebugUITools_Error_1, DebugUIMessages.DebugUITools_Exception_occurred_during_launch_2, t); // 		
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
		final IJobManager jobManager = Platform.getJobManager();
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();

		boolean wait = (jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0) || (jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD).length > 0);
		
		String waitPref = store.getString(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD);
		if (wait) { // if there are build jobs running, do we wait or not??
			if (waitPref.equals(MessageDialogWithToggle.PROMPT)) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(getShell(), DebugUIMessages.DebugUIPlugin_23, DebugUIMessages.DebugUIPlugin_24, null, false, store, IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD); // 
				
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
		Job job = new Job(DebugUIMessages.DebugUITools_3) { 
			public IStatus run(final IProgressMonitor monitor) {
				try {
					if(waitInJob) {						
						StringBuffer buffer= new StringBuffer(configuration.getName());
						buffer.append(DebugUIMessages.DebugUIPlugin_0); 
						ILaunchConfigurationWorkingCopy workingCopy= configuration.copy(buffer.toString());
						workingCopy.setAttribute(ATTR_LAUNCHING_CONFIG_HANDLE, configuration.getMemento());
						final ILaunch pendingLaunch= new PendingLaunch(workingCopy, mode, this);
                        
						DebugPlugin.getDefault().getLaunchManager().addLaunch(pendingLaunch);
                        IJobChangeListener listener= new IJobChangeListener() {
                            public void sleeping(IJobChangeEvent event) {
                            }
                            public void scheduled(IJobChangeEvent event) {
                            }
                            public void running(IJobChangeEvent event) {
                            }
                            public void done(IJobChangeEvent event) {
                                DebugPlugin.getDefault().getLaunchManager().removeLaunch(pendingLaunch);
                                removeJobChangeListener(this);
                            }
                            public void awake(IJobChangeEvent event) {
                            }
                            public void aboutToRun(IJobChangeEvent event) {
                            }
                        };
                        addJobChangeListener(listener);

						try {
							jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
							jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
						} catch (InterruptedException e) {
							// just continue.
						}
                        DebugPlugin.getDefault().getLaunchManager().removeLaunch(pendingLaunch);
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
		job.setName(DebugUIMessages.DebugUITools_8); 
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
            } else if (index > 0) {
                //DBCS languages use "(&X)" format
                if (title.charAt(index - 1) == '(' && title.length() >= index + 3 && title.charAt(index + 2) == ')') {
                    String first = title.substring(0, index - 1);
                    String last = title.substring(index + 3);
                    title = first + last;
                } else if (index < (title.length() - 1)) {
                    String first = title.substring(0, index);
                    String last = title.substring(index + 1);
                    title = first + last;
                }
            }
        }
        return title;
    }

    /**
     * Returns the image descriptor registry used for this plugin.
     * 
     * @since 3.1
     */
    public static ImageDescriptorRegistry getImageDescriptorRegistry() {
        if (getDefault().fImageDescriptorRegistry == null) {
            getDefault().fImageDescriptorRegistry = new ImageDescriptorRegistry();
        }
        return getDefault().fImageDescriptorRegistry;
    }
    
    /**
     * Returns an image descriptor for the icon referenced by the given attribute
     * and configuration element, or <code>null</code> if none.
     * 
     * @param element
     * @param attr
     * @return image descriptor or <code>null</code>
     */
    public static ImageDescriptor getImageDescriptor(IConfigurationElement element, String attr) {
		Bundle bundle = Platform.getBundle(element.getNamespace());
		String iconPath = element.getAttribute(attr);
		if (iconPath != null) {
			URL iconURL = Platform.find(bundle , new Path(iconPath));
			if (iconURL != null) {
				return ImageDescriptor.createFromURL(iconURL);
			}
		}    	
		return null;
    }
}

