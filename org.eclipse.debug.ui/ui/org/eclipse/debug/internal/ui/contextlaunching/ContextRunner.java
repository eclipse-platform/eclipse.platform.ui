/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contextlaunching;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutSelectionDialog;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListDialog;

import com.ibm.icu.text.MessageFormat;

/**
 * Static runner for context launching to provide the base capability of context 
 * launching to more than one form of action (drop down, toolbar, view, etc)
 * 
 * @see ContextLaunchingAction
 * @see ContextLaunchingToolbarAction
 * @see ILaunchListener
 * 
 *  @since 3.3
 *  EXPERIMENTAL
 *  CONTEXTLAUNCHING
 */
public final class ContextRunner {
	
	/**
	 * The singleton instance of the context runner
	 */
	private static ContextRunner fgInstance = null;
	
	/**
	 * Returns the singleton instance of <code>ContextRunner</code>
	 * @return the singleton instance of <code>ContextRunner</code>
	 */
	public static ContextRunner getDefault() {
		if(fgInstance == null) {
			fgInstance = new ContextRunner();
		}
		return fgInstance;
	}
	
	/**
	 * Performs the context launching given the object context and the mode to launch in.
	 * @param mode the mode to launch in
	 */
	public void launch(String mode) {
		try {
			//1. resolve resource
			Object context = getCurrentContext();
			ILaunchConfiguration config = null;
			if(context != null) {
				if(context instanceof IAdaptable) {
					IResource resource = (IResource) ((IAdaptable)context).getAdapter(IResource.class);
					if(resource != null) {
						selectAndLaunch(resource, mode);
						return;
					}
				}
			}
			//2. launch last if no resource
			config = getLastLaunch(mode);
			if(config != null) {
				DebugUITools.launch(config, mode);
				return;
			}
			
		}
		catch(CoreException ce) {DebugUIPlugin.log(ce);}
	}
	
	/**
	 * Returns the last thing launched from the launch history 
	 * @param mode the mode
	 * @return the last <code>ILaunchConfiguration</code> launched or <code>null</code> if none
	 */
	protected ILaunchConfiguration getLastLaunch(String mode) {
		ILaunchGroup group = resolveLaunchGroup(mode);
		if(group != null) {
			LaunchHistory history = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchHistory(group.getIdentifier());
			if(history != null) {
				return history.getRecentLaunch();
			}
		}
		return null;
	}
	
	/**
	 * Returns the <code>ILaunchGroup</code> that corresponds to the specified mode
	 * @param mode the mode to find the launch group
	 * @return the <code>ILaunchGroup</code> that corresponds to the specified mode, or <code>null</code>
	 */
	protected ILaunchGroup resolveLaunchGroup(String mode) {
		//TODO might not return the group we want
		ILaunchGroup[] groups = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroups();
		for(int i = 0; i < groups.length; i++) {
			if(groups[i].getMode().equals(mode) && groups[i].getCategory() == null) {
				return groups[i];
			}
		}
		return null;
	}
	
	/**
	 * Returns if the parent project should be checked automatically
	 * @return true if the parent project should checked automatically, false otherwise
	 */
	protected boolean shouldCheckParent() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_PARENT_PROJECT);
	}
	
	/**
	 * Prompts the user to select a way of launching the current resource, where a 'way'
	 * is defined as a launch shortcut, and returns if a launch took place
	 * @param resource
	 * @param mode
	 * @return if the context was launched in the given mode or not
	 * @throws CoreException
	 */
	protected boolean selectAndLaunch(IResource resource, String mode) throws CoreException {
		List configs = getLaunchConfigurationManager().getApplicableLaunchConfigurations(resource);
		int csize = configs.size();
		if(csize == 1) {
			DebugUITools.launch((ILaunchConfiguration) configs.get(0), mode);
			return true;
		}
		if(csize < 1) {
			List exts = getLaunchConfigurationManager().getLaunchShortcuts(resource);
			int esize = exts.size();
			if(esize == 1) {
				LaunchShortcutExtension ext = (LaunchShortcutExtension) exts.get(0);
				ext.launch(new StructuredSelection(resource), mode);
				return true;
			}
			if(esize > 1) {
				return showShortcutSelectionDialog(resource, mode);
			}
			if(esize < 1) {
				IProject project = resource.getProject();
				if(project != null && !project.equals(resource)) {
					if(!shouldCheckParent()) {
						String msg = MessageFormat.format(ContextMessages.ContextRunner_10, new String[] {project.getName(), resource.getName(), resource.getName()});
						MessageDialogWithToggle mdwt = new MessageDialogWithToggle(DebugUIPlugin.getShell(), 
								ContextMessages.ContextRunner_11, 
								null, 
								msg,
								MessageDialog.QUESTION, 
								new String[] {IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL},
								0, 
								ContextMessages.ContextRunner_12,
								false);
						if(mdwt.open() == IDialogConstants.YES_ID) {
							DebugUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_LAUNCH_PARENT_PROJECT, mdwt.getToggleState());
							selectAndLaunch(project, mode);
						}
					}
					else {
						selectAndLaunch(project, mode);
					}
				}
				else {
					String msg = ContextMessages.ContextRunner_7;
					if(!resource.isAccessible()) {
						msg = MessageFormat.format(ContextMessages.ContextRunner_13, new String[] {resource.getName()});
					}
					MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, msg);
				}
			}
		}
		else if(csize > 1){
			ILaunchConfiguration config = getMRUConfiguration(configs, mode);
			if(config != null) {
				DebugUITools.launch(config, mode);
			}
			else {
				return showConfigurationSelectionDialog(configs, mode);
			}
		}
		return false;
	}
	
	/**
	 * Launches the first occurance of any one of the configurations in the provided list, if they are found in the launch history
	 * for the corresponding launch group
	 * @param configurations
	 * @param mode
	 * @return the associated launch configuration from the MRU listing or <code>null</code> if there isn't one
	 */
	protected ILaunchConfiguration getMRUConfiguration(List configurations, String mode) {
		ILaunchGroup group = resolveLaunchGroup(mode);
		if(group != null) {
			LaunchHistory history = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchHistory(group.getIdentifier());
			ILaunchConfiguration config = null;
			for(Iterator iter = configurations.iterator(); iter.hasNext();) {
				config = (ILaunchConfiguration) iter.next();
				if(history.contains(config)) {
					return config;
				}
			}
		}
		return null;
	}
	
	/**
	 * Presents the user with a dialog to pick the launch configuration to launch
	 * @param configurations the listing of applicable configurations to present
	 * @param mode the mode
	 * @return true if something was launched, false otherwise
	 */
	protected boolean showConfigurationSelectionDialog(List configurations, String mode) {
		ListDialog lsd = new ListDialog(DebugUIPlugin.getShell());
		lsd.setContentProvider(new ArrayContentProvider());
		lsd.setLabelProvider(new DefaultLabelProvider());
		lsd.setMessage(ContextMessages.ContextRunner_8);
		lsd.setTitle(ContextMessages.ContextRunner_9);
		lsd.setInput(configurations);
		if(lsd.open() == IDialogConstants.OK_ID) {
			ILaunchConfiguration config = (ILaunchConfiguration) lsd.getResult()[0];
			DebugUITools.launch(config, mode);
			return true;
		}
		return false;
	}
	
	/**
	 * Presents a selection dialog to the user to pick a launch shortcut
	 * @param resource the resource context
	 * @param mode the mode
	 * @return true if something was launched, false otherwise
	 */
	protected boolean showShortcutSelectionDialog(IResource resource, String mode) {
		LaunchShortcutSelectionDialog dialog = new LaunchShortcutSelectionDialog(resource, mode);
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if(result.length > 0) {
				LaunchShortcutExtension method = (LaunchShortcutExtension) result[0];
				if(method != null) {
					method.launch(new StructuredSelection(resource), mode);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the current context to be considered for launching. 
	 * The returned object will be one of:
	 * <ol>
	 * <li>IEditorInput</li>
	 * <li>Object where <i>object</i> is the first element in the selection obtained from the 
	 * selection provider of the currently selected workbench part</li>
	 * <li><code>null</code>, if the current context is unknown</li>
	 * </ol>
	 * @return the currently selected context to consider for launching, or <code>null</code>.
	 *
	 */
	public Object getCurrentContext() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page!= null) {
				IWorkbenchPart part = page.getActivePart();
				if(part != null) {
					if(part instanceof IEditorPart) {
						return ((IEditorPart)part).getEditorInput();
					}
					IWorkbenchPartSite site = part.getSite();
					if(site != null) {
						ISelectionProvider provider = site.getSelectionProvider();
						if(provider != null) {
							ISelection sel = provider.getSelection();
							if(sel instanceof IStructuredSelection) {
								StructuredSelection ss = (StructuredSelection) sel;
								if(ss.isEmpty()) {
									return part;
								}
								else {
									return ss.getFirstElement();
								}
							}
							else if(sel instanceof ITextSelection) {
								return part;
							}
						}
						else {
							return part;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the associated launch configuration name of the currently selected context, or the empty string.
	 * @param mode
	 * @return the associated launch configuration name of the currently selected context or the empty string. 
	 */
	public String getContextName(String mode) {
		Object o = getCurrentContext();
		ILaunchConfiguration config = getLaunchConfigurationManager().isSharedConfig(o);
		if(config != null) {
			return config.getName();
		}
		else {
			if(o instanceof IAdaptable) {
				Object a = ((IAdaptable)o).getAdapter(IResource.class);
				if(a != null) {
					IResource res = (IResource) a;
					List configs = getLaunchConfigurationManager().getApplicableLaunchConfigurations(res);
					int csize = configs.size();
					if(csize == 1) {
						return ((ILaunchConfiguration)configs.get(0)).getName();
					}
					else if(csize > 1) {
						config = getMRUConfiguration(configs, mode);
						if(config != null) {
							return config.getName();
						}
						else {
							//TODO could cause TVT issues
							return ContextMessages.ContextRunner_14;
						}
					}
					else {
						try {
							List exts = getLaunchConfigurationManager().getLaunchShortcuts(res);
							int esize = exts.size();
							if(esize == 0) {
								//TODO could cause TVT issues
								return ContextMessages.ContextRunner_15; 
							}
							else {
								//TODO could cause TVT issues
								return ContextMessages.ContextRunner_14;
							}
						}
						catch(CoreException ce) {DebugUIPlugin.log(ce);}
					}
				}
			}
		}
		config = getLastLaunch(mode);
		if(config != null) {
			return config.getName();
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns if context launching is enabled
	 * @return if context launching is enabled
	 */
	public boolean isContextLaunchEnabled() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH);
	}
	
	/**
	 * Returns the launch configuration manager
	 * @return the launch configuration manager
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
}
