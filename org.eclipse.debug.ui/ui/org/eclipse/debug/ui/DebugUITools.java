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
package org.eclipse.debug.ui;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.memory.MemoryRenderingManager;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupFacility;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIUtils;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.memory.IMemoryRenderingManager;
import org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.IConsole;

/**
 * This class provides utilities for clients of the debug UI.
 * <p>
 * Images retrieved from this facility should not be disposed.
 * The images will be disposed when this plugin is shutdown.
 * </p>
 * <p>
 * This class is not intended to be subclassed or instantiated.
 * </p>
 */
public class DebugUITools {
	
	/**
	 * Returns the shared image managed under the given key, or <code>null</code>
	 * if none.
	 * <p>
	 * Note that clients <b>MUST NOT</b> dispose the image returned by this method.
	 * </p>
	 * <p>
	 * See <code>IDebugUIConstants</code> for available images.
	 * </p>
	 *
	 * @param key the image key
	 * @return the image, or <code>null</code> if none
	 * @see IDebugUIConstants
	 */
	public static Image getImage(String key) {
		return DebugPluginImages.getImage(key);
	}
	
	/**
	 * Returns the shared image descriptor managed under the given key, or
	 * <code>null</code> if none.
	 * <p>
	 * See <code>IDebugUIConstants</code> for available image descriptors.
	 * </p>
	 *
	 * @param key the image descriptor key
	 * @return the image descriptor, or <code>null</code> if none
	 * @see IDebugUIConstants
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return DebugPluginImages.getImageDescriptor(key);
	}
		
	/**
	 * Returns the default image descriptor for the given element
	 * or <code>null</code> if none is defined.
	 */
	public static ImageDescriptor getDefaultImageDescriptor(Object element) {
		String imageKey= getDefaultImageKey(element);
		if (imageKey == null) {
			return null;
		}
		return DebugPluginImages.getImageDescriptor(imageKey);
	}
	
	private static String getDefaultImageKey(Object element) {
		return ((DefaultLabelProvider)DebugUIPlugin.getDefaultLabelProvider()).getImageKey(element);
	}
	
	/**
	 * Returns the preference store for the debug UI plugin.
	 *
	 * @return preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * Returns a new debug model presentation that delegates to
	 * appropriate debug models.
	 * <p>
	 * It is the client's responsibility dispose the presentation.
	 * </p>
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 * @return a debug model presentation
	 * @since 2.0
	 */
	public static IDebugModelPresentation newDebugModelPresentation() {
		return new DelegatingModelPresentation();
	}
	
	/**
	 * Returns a new debug model presentation for specified
	 * debug model, or <code>null</code> if a presentation does
	 * not exist.
	 * <p>
	 * It is the client's responsibility dispose the presentation.
	 * </p>
	 * 
	 * @param identifier debug model identifier
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 * @return a debug model presentation, or <code>null</code>
	 * @since 2.0
	 */
	public static IDebugModelPresentation newDebugModelPresentation(String identifier) {
		IExtensionPoint point= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.ID_DEBUG_MODEL_PRESENTATION);
		if (point != null) {
			IExtension[] extensions= point.getExtensions();
			for (int i= 0; i < extensions.length; i++) {
				IExtension extension= extensions[i];
				IConfigurationElement[] configElements= extension.getConfigurationElements();
				for (int j= 0; j < configElements.length; j++) {
					IConfigurationElement elt= configElements[j];
					String id= elt.getAttribute("id"); //$NON-NLS-1$
					if (id != null && id.equals(identifier)) {
						return new LazyModelPresentation(elt);
					}
				}
			}
		}
		return null;
	}	
	
	/**
	 * Returns the currently selected element in the 
	 * debug view of the current workbench page,
	 * or <code>null</code> if there is no current
	 * debug context.
	 * <p>
	 * This method used to return <code>null</code> when called from a non-UI thread,
	 * but since 3.1, this methods also works when called from a non-UI thread.
	 * </p>
	 * @return the currently selected debug context, or <code>null</code>
	 * @since 2.0
	 */
	public static IAdaptable getDebugContext() {
	    return SelectedResourceManager.getDefault().getDebugContext();
	}

	/**
	 * Returns the currently selected resource in the active workbench window,
	 * or <code>null</code> if none. If an editor is active, the resource adapater
	 * assocaited with the editor is returned, if any.
	 * 
	 * @return selected resource or <code>null</code>
	 * @since 3.0
	 */
	public static IResource getSelectedResource() {
		return SelectedResourceManager.getDefault().getSelectedResource();
	}
			
	/**
	 * Returns the process associated with the current debug context.
	 * If there is no debug context currently, the most recently
	 * launched process is returned. If there is no current process
	 * <code>null</code> is returned.
	 * 
	 * @return the current process, or <code>null</code>
	 * @since 2.0
	 */
	public static IProcess getCurrentProcess() {
		IAdaptable context = getDebugContext();
		if (context == null) {
			ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
			if (launches.length > 0) {
				context = launches[launches.length - 1];
			}
		}
		if (context instanceof IDebugElement) {
			return ((IDebugElement)context).getDebugTarget().getProcess();
		}
		if (context instanceof IProcess) {
			return (IProcess)context;
		}
		if (context instanceof ILaunch) {
			ILaunch launch= (ILaunch)context;
			IDebugTarget target= launch.getDebugTarget();
			if (target != null) {
				IProcess process = target.getProcess();
				if (process != null) {
					return process;
				}
			}
			IProcess[] ps = launch.getProcesses();
			if (ps.length > 0) {
				return ps[ps.length - 1];
			}
		}
		return null;
	}

	/**
	 * Open the launch configuration dialog with the specified initial selection.
	 * The selection may be <code>null</code>, or contain any mix of 
	 * <code>ILaunchConfiguration</code> or <code>ILaunchConfigurationType</code>
	 * elements.
	 * <p>
	 * Before opening a new dialog, this method checks if there is an existing open
	 * launch configuration dialog.  If there is, this dialog is used with the
	 * specified selection.  If there is no existing dialog, a new one is created.
	 * </p>
	 * <p>
	 * Note that if an existing dialog is reused, the <code>mode</code> argument is ignored
	 * and the existing dialog keeps its original mode.
	 * </p>
	 * 
	 * @param shell the parent shell for the launch configuration dialog
	 * @param selection the initial selection for the dialog
	 * @param mode the mode (run or debug) in which to open the launch configuration dialog.
	 *  This should be one of the constants defined in <code>ILaunchManager</code>.
	 * @return the return code from opening the launch configuration dialog -
	 *  one  of <code>Window.OK</code> or <code>Window.CANCEL</code>
	 * @since 2.0
	 * @deprecated use openLaunchConfigurationDialogOnGroup(Shell, IStructuredSelection, String)
	 *  to specify the launch group that the dialog should be opened on. This method will open
	 *  on the launch group with the specified mode and a <code>null</code> category 
	 */
	public static int openLaunchConfigurationDialog(Shell shell, IStructuredSelection selection, String mode) {
		ILaunchGroup[] groups = getLaunchGroups();
		for (int i = 0; i < groups.length; i++) {
			ILaunchGroup group = groups[i];
			if (group.getMode().equals(mode) && group.getCategory() == null) {
				return openLaunchConfigurationDialogOnGroup(shell, selection, group.getIdentifier());
			}
		}
		return Window.CANCEL;
	}
	
	/**
	 * Open the launch configuration dialog with the specified initial selection.
	 * The selection may be <code>null</code>, or contain any mix of 
	 * <code>ILaunchConfiguration</code> or <code>ILaunchConfigurationType</code>
	 * elements.
	 * <p>
	 * Before opening a new dialog, this method checks if there is an existing open
	 * launch configuration dialog.  If there is, this dialog is used with the
	 * specified selection.  If there is no existing dialog, a new one is created.
	 * </p>
	 * <p>
	 * Note that if an existing dialog is reused, the <code>mode</code> argument is ignored
	 * and the existing dialog keeps its original mode.
	 * </p>
	 * 
	 * @param shell the parent shell for the launch configuration dialog
	 * @param selection the initial selection for the dialog
	 * @param groupIdentifier the identifier of the launch group to display (corresponds to
	 * the identifier of a launch group extension)
	 * @return the return code from opening the launch configuration dialog -
	 *  one  of <code>Window.OK</code> or <code>Window.CANCEL</code>
	 * @since 2.1
	 */
	public static int openLaunchConfigurationDialogOnGroup(Shell shell, IStructuredSelection selection, String groupIdentifier) {
		return openLaunchConfigurationDialogOnGroup(shell, selection, groupIdentifier, null);
	}
	
	/**
	 * Open the launch configuration dialog with the specified initial selection.
	 * The selection may be <code>null</code>, or contain any mix of 
	 * <code>ILaunchConfiguration</code> or <code>ILaunchConfigurationType</code>
	 * elements.
	 * <p>
	 * Before opening a new dialog, this method checks if there is an existing open
	 * launch configuration dialog.  If there is, this dialog is used with the
	 * specified selection.  If there is no existing dialog, a new one is created.
	 * </p>
	 * <p>
	 * Note that if an existing dialog is reused, the <code>mode</code> argument is ignored
	 * and the existing dialog keeps its original mode.
	 * </p>
	 * <p>
	 * If a status is specified, a status handler is consulted to handle the
	 * status. The status handler is passed the instance of the launch
	 * configuration dialog that is opened. This gives the status handler an
	 * opportunity to perform error handling/initialization as required.
	 * </p>
	 * @param shell the parent shell for the launch configuration dialog
	 * @param selection the initial selection for the dialog
	 * @param groupIdentifier the identifier of the launch group to display (corresponds to
	 * the identifier of a launch group extension)
	 * @param status the status to display in the dialog, or <code>null</code>
	 * if none
	 * @return the return code from opening the launch configuration dialog -
	 *  one  of <code>Window.OK</code> or <code>Window.CANCEL</code>
	 * @see org.eclipse.debug.core.IStatusHandler
	 * @since 2.1
	 */
	public static int openLaunchConfigurationDialogOnGroup(final Shell shell, final IStructuredSelection selection, final String groupIdentifier, final IStatus status) {
		final int[] result = new int[1];
		Runnable r = new Runnable() {
			/**
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				LaunchConfigurationsDialog dialog = (LaunchConfigurationsDialog) LaunchConfigurationsDialog.getCurrentlyVisibleLaunchConfigurationDialog();
				if (dialog != null) {
					dialog.setInitialSelection(selection);
					dialog.doInitialTreeSelection();
					if (status != null) {
						dialog.handleStatus(status); 
					}
					result[0] = Window.OK;
				} else {
					dialog = new LaunchConfigurationsDialog(shell, DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(groupIdentifier));
					dialog.setOpenMode(LaunchConfigurationsDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION);
					dialog.setInitialSelection(selection);
					dialog.setInitialStatus(status);
					result[0] = dialog.open();			
				}
			}
		};
		BusyIndicator.showWhile(DebugUIPlugin.getStandardDisplay(), r);
		return result[0];
	}
		
	/**
	 * Open the launch configuration properties dialog on the specified launch
	 * configuration.
	 *
	 * @param shell the parent shell for the launch configuration dialog
	 * @param configuration the configuration to display
	 * @param groupIdentifier group identifier of the launch group the launch configuration
	 * belongs to
	 * @return the return code from opening the launch configuration dialog -
	 *  one  of <code>Window.OK</code> or <code>Window.CANCEL</code>
	 * @since 2.1
	 */
	public static int openLaunchConfigurationPropertiesDialog(Shell shell, ILaunchConfiguration configuration, String groupIdentifier) {
		return openLaunchConfigurationPropertiesDialog(shell, configuration, groupIdentifier, null);
	}
	
	/**
	 * Open the launch configuration properties dialog on the specified launch
	 * configuration.
	 *
	 * @param shell the parent shell for the launch configuration dialog
	 * @param configuration the configuration to display
	 * @param groupIdentifier group identifier of the launch group the launch configuration
	 * belongs to
	 * @param status the status to display, or <code>null</code> if none
	 * @return the return code from opening the launch configuration dialog -
	 *  one  of <code>Window.OK</code> or <code>Window.CANCEL</code>
	 * @since 3.0
	 */
	public static int openLaunchConfigurationPropertiesDialog(Shell shell, ILaunchConfiguration configuration, String groupIdentifier, IStatus status) {
		LaunchGroupExtension group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(groupIdentifier);
		if (group != null) {
			LaunchConfigurationPropertiesDialog dialog = new LaunchConfigurationPropertiesDialog(shell, configuration, group);
			dialog.setInitialStatus(status);
			return dialog.open();
		} 
		
		return Window.CANCEL;
	}
	
	/**
     * Open the launch configuration dialog on the specified launch
     * configuration. The dialog displays the tabs for a single configuration
     * only (a tree of launch configuration is not displayed), and provides a
     * launch (run or debug) button.
     * <p>
     * If a status is specified, a status handler is consulted to handle the
     * status. The status handler is passed the instance of the launch
     * configuration dialog that is opened. This gives the status handler an
     * opportunity to perform error handling/initialization as required.
     * </p>
     * @param shell the parent shell for the launch configuration dialog
     * @param configuration the configuration to display
     * @param groupIdentifier group identifier of the launch group the launch configuration
     * belongs to
     * @param status the status to display, or <code>null</code> if none 
     * @return the return code from opening the launch configuration dialog -
     *  one  of <code>Window.OK</code> or <code>Window.CANCEL</code>
     * @since 2.1
     */
    public static int openLaunchConfigurationDialog(Shell shell, ILaunchConfiguration configuration, String groupIdentifier, IStatus status) {
    	LaunchGroupExtension group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(groupIdentifier);
    	if (group != null) {
    		LaunchConfigurationDialog dialog = new LaunchConfigurationDialog(shell, configuration, group);
    		dialog.setInitialStatus(status);
    		return dialog.open();
    	} 
    		
    	return Window.CANCEL;
    }
			
	/**
	 * Saves all dirty editors and builds the workspace according to current
	 * preference settings, and returns whether a launch should proceed.
	 * <p>
	 * The following preferences effect whether dirty editors are saved,
	 * and/or if the user is prompted to save dirty edtiors:<ul>
	 * <li>PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH</li>
	 * <li>PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH</li>
	 * <li>PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH</li>
	 * </ul>
	 * The following preference effects whether a build is performed before
	 * launching (if required):<ul>
	 * <li>PREF_BUILD_BEFORE_LAUNCH</li>
	 * </ul>
	 * </p>
	 * 
	 * @return whether a launch should proceed
	 * @since 2.0
	 * @deprecated Saving has been moved to the launch delegate <code>LaunchConfigurationDelegate</code> to allow for scoped saving
	 * of resources that are only involved in the current launch, no longer the entire workspace
	 */
	public static boolean saveAndBuildBeforeLaunch() {
		return DebugUIPlugin.saveAndBuild();
	}
	
	/**
	 * Saves all dirty editors according to current
	 * preference settings, and returns whether a launch should proceed.
	 * <p>
	 * The following preferences effect whether dirty editors are saved,
	 * and/or if the user is prompted to save dirty edtiors:<ul>
	 * <li>PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH</li>
	 * <li>PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH</li>
	 * <li>PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH</li>
	 * </ul>
	 * </p>
	 * 
	 * @return whether a launch should proceed
	 * @since 2.1
	 * @deprecated Saving has been moved to the launch delegate <code>LaunchConfigurationDelegate</code> to allow for scoped saving
	 * of resources that are only involved in the current launch, no longer the entire workspace
	 */
	public static boolean saveBeforeLaunch() {
		return DebugUIPlugin.preLaunchSave();
	}	
	
	/**
	 * Saves and builds the workspace according to current preference settings, and
	 * launches the given launch configuration in the specified mode.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode launch mode - run or debug
	 * @since 2.1
	 */
	public static void launch(final ILaunchConfiguration configuration, final String mode) {
		boolean launchInBackground= true;
		try {
			launchInBackground= configuration.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
		if (launchInBackground) {
			DebugUIPlugin.launchInBackground(configuration, mode);
		} else {
			DebugUIPlugin.launchInForeground(configuration, mode);
		}
	}
	

	
	/**
	 * Builds the workspace according to current preference settings, and launches
	 * the given configuration in the specified mode, returning the resulting launch
	 * object.
	 * <p>
	 * The following preference effects whether a build is performed before
	 * launching (if required):<ul>
	 * <li>PREF_BUILD_BEFORE_LAUNCH</li>
	 * </ul>
	 * </p>
	 * 
	 * @param configuration the configuration to launch
	 * @param mode the mode to launch in
	 * @param monitor progress monitor
	 * @return the resulting launch object
	 * @throws CoreException if building or launching fails
	 * @since 2.1
	 */
	public static ILaunch buildAndLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return DebugUIPlugin.buildAndLaunch(configuration, mode, monitor);
	}
	
	/**
	 * Returns the perspective to switch to when a configuration of the given type
	 * is launched in the given mode, or <code>null</code> if no switch should take
	 * place.
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode identifier
	 * @return perspective identifier or <code>null</code>
	 * @since 3.0
	 */
	public static String getLaunchPerspective(ILaunchConfigurationType type, String mode) {
		return DebugUIPlugin.getDefault().getPerspectiveManager().getLaunchPerspective(type, mode);
	}
	
	/**
	 * Sets the perspective to switch to when a configuration of the given type
	 * is launched in the given mode. <code>PERSPECTIVE_NONE</code> indicates no
	 * perspective switch should take place. <code>PERSPECTIVE_DEFAULT</code> indicates
	 * a default perspective switch should take place, as defined by the associated
	 * launch tab group extension.
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode identifier
	 * @param perspective identifier, <code>PERSPECTIVE_NONE</code>, or
	 *   <code>PERSPECTIVE_DEFAULT</code>
	 * @since 3.0
	 */
	public static void setLaunchPerspective(ILaunchConfigurationType type, String mode, String perspective) {
		DebugUIPlugin.getDefault().getPerspectiveManager().setLaunchPerspective(type, mode, perspective);
	}	
		
	/**
	 * Returns whether the given launch configuraiton is private. Generally,
	 * private launch configurations should not be displayed to the user. The
	 * private status of a launch configuration is determined by the
	 * <code>IDebugUIConstants.ATTR_PRIVATE</code> attribute.
	 * 
	 * @param configuration launch configuration
	 * @return whether the given launch configuration is private
	 * @since 3.0
	 */
	public static boolean isPrivate(ILaunchConfiguration configuration) {
		return !LaunchConfigurationManager.isVisible(configuration);
	}

	/**
	 * Sets whether step filters should be applied to step commands. This
	 * setting is a global option applied to all registered debug targets. 
	 * 
	 * @param useStepFilters whether step filters should be applied to step
	 *  commands
	 * @since 3.0
	 * @see org.eclipse.debug.core.model.IStepFilters
	 */
	public static void setUseStepFilters(boolean useStepFilters) {
		DebugUIPlugin.getDefault().getStepFilterManager().setUseStepFilters(useStepFilters);
	}
		
	/**
	 * Returns whether step filters are applied to step commands.
	 * 
	 * @return whether step filters are applied to step commands
	 * @since 3.0
	 * @see org.eclipse.debug.core.model.IStepFilters
	 */
	public static boolean isUseStepFilters() {
		return DebugUIPlugin.getDefault().getStepFilterManager().isUseStepFilters();
	}
		
	/**
	 * Returns the console associated with the given process, or 
	 * <code>null</code> if none.
	 * 
	 * @param process a process
	 * @return console associated with the given process, or 
	 * <code>null</code> if none
	 * @since 3.0
	 */
	public static IConsole getConsole(IProcess process) {
		return DebugUIPlugin.getDefault().getProcessConsoleManager().getConsole(process);
	}
	
	/**
	 * Returns the console associated with the given debug element, or 
	 * <code>null</code> if none.
	 * 
	 * @param element a debug model element
	 * @return console associated with the given element, or 
	 * <code>null</code> if none
	 * @since 3.0
	 */
	public static IConsole getConsole(IDebugElement element) {
		IProcess process = element.getDebugTarget().getProcess();
		if (process != null) {
			return getConsole(process);
		}
		return null;
	}	
	
	/**
	 * Returns all registered launch group extensions.
	 *  
	 * @return all registered launch group extensions
	 * @since 3.0
	 */
	public static ILaunchGroup[] getLaunchGroups() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroups();
	}
	
	/**
	 * Returns the launch group that the given launch configuration belongs
	 * to, for the specified mode, or <code>null</code> if none.
	 * 
	 * @param configuration
	 * @param mode
	 * @return the launch group the given launch configuration belongs to, for
	 * the specified mode, or <code>null</code> if none
	 * @since 3.0
	 */
	public static ILaunchGroup getLaunchGroup(ILaunchConfiguration configuration, String mode) {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configuration, mode);
	}
	
    /**
     * Performs source lookup on the given artifact and returns the result.
     * Optionally, a source locator may be specified.
     *  
     * @param artifact object for which source is to be resolved
     * @param locator the source locator to use, or <code>null</code>. When <code>null</code>
     *   a source locator is determined from the artifact, if possible. If the artifact
     *   is a debug element, the source locator from its associated launch is used. 
     * @return a source lookup result
     * @since 3.1
     */
    public static ISourceLookupResult lookupSource(Object artifact, ISourceLocator locator) {	
    	return SourceLookupFacility.getDefault().lookup(artifact, locator);
    }
	
    /**
     * Displays the given source lookup result in an editor in the given workbench
     * page. Has no effect if the result has an unknown editor id or editor input.
     * The editor is opened, positioned, and annotated.
     * <p>
     * Honors user preference for editors re-use.
     * </p> 
     * @param result source lookup result to display
     * @param page the page to display the result in
     * @since 3.1
     */
    public static void displaySource(ISourceLookupResult result, IWorkbenchPage page) {
    	SourceLookupFacility.getDefault().display(result, page);
    }
    
    /**
     * Returns the memory rendering manager.
     * 
     * @return the memory rendering manager
     * @since 3.1
     */
    public static IMemoryRenderingManager getMemoryRenderingManager() {
        return MemoryRenderingManager.getDefault();
    }
    
	/**
	 * Returns the image associated with the specified type of source container
	 * or <code>null</code> if none.
	 * 
	 * @param id unique identifier for a source container type
	 * @return image associated with the specified type of soure container
	 *    or <code>null</code> if none
	 * @since 3.2
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerType
	 */
	public static Image getSourceContainerImage(String id){
		return SourceLookupUIUtils.getSourceContainerImage(id);
	}
	
	/**
	 * Returns a new source container browser for the specified type of source container
	 * or <code>null</code> if a browser has not been registered.
	 * 
	 * @param id unique identifier for a source container type
	 * @return source container browser or <code>null</code> if none
	 * @since 3.2
	 * @see org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser
	 */
	public static ISourceContainerBrowser getSourceContainerBrowser(String id) {
		return SourceLookupUIUtils.getSourceContainerBrowser(id);
	}
}
