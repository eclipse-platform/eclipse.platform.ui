package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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
		IPluginDescriptor descriptor= DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint point= descriptor.getExtensionPoint(IDebugUIConstants.ID_DEBUG_MODEL_PRESENTATION);
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
	 * debug context, or if not called from the UI
	 * thread.
	 * 
	 * @return the currently selected debug context, or <code>null</code>
	 * @since 2.0
	 */
	public static IAdaptable getDebugContext() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow() ;
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
				if (part != null) {
					IDebugView view = (IDebugView)part.getAdapter(IDebugView.class);
					if (view != null) {
						Viewer viewer = view.getViewer();
						if (viewer != null) {
							ISelection s = viewer.getSelection();
							if (s != null) {
								if (s instanceof IStructuredSelection) {
									IStructuredSelection ss = (IStructuredSelection)s;
									if (ss.size() == 1) {
										Object element = ss.getFirstElement();
										if (element instanceof IAdaptable) {
											return (IAdaptable)element;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
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
	 * 
	 * @param shell the parent shell for the launch configuration dialog
	 * @param selection the initial selection for the dialog
	 * @param mode the mode (run or debug) in which to open the launch configuration dialog.
	 *  This should be one of the constants defined in <code>ILaunchManager</code>.
	 * @return the return code from opening the launch configuration dialog
	 * @since 2.0
	 */
	public static int openLaunchConfigurationDialog(Shell shell, IStructuredSelection selection, String mode) {
		LaunchConfigurationDialog dialog = new LaunchConfigurationDialog(shell, null, mode);
		dialog.setOpenMode(LaunchConfigurationDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION);
		dialog.setInitialSelection(selection);
		return dialog.open();
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
	 */
	public static boolean saveAndBuildBeforeLaunch() {
		return DebugUIPlugin.saveAndBuild();
	}
	
}