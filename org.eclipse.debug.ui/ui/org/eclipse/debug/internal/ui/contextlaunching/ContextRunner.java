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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutSelectionDialog;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;

import com.ibm.icu.text.MessageFormat;

/**
 * Static runner for context launching to provide the base capability of context 
 * launching to more than one form of action (drop down, toolbar, view, etc)
 * 
 * @see ContextLaunchingAction
 * @see ContextLaunchingToolbarAction
 * @see ILaunchListener
 * @see org.eclipse.debug.core.ILaunchManager
 * 
 *  @since 3.3
 *  EXPERIMENTAL
 *  CONTEXTLAUNCHING
 */
public class ContextRunner implements ILaunchListener {
	
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
	 * The underlying resource that is derived from the object context of the 
	 * run(Object, String) method
	 */
	private IResource fBackingResource = null;
	private boolean fMakeResourceDefault = false;
	private boolean fMakeProjectDefault = false;
	
	/**
	 * Performs the context launching given the object context and the mode to launch in.
	 * @param mode the mode to launch in
	 */
	public void launch(String mode) {
		try {
			Object context = getCurrentContext();
			ILaunchConfiguration config = isSharedConfig(context);
			if(config != null) { 
				DebugUITools.launch(config, mode);
				return;
			}
			config = isSharedConfigEditorInput(context);
			if(config != null) {
				DebugUITools.launch(config, mode);
				return;
			}
			if(context instanceof IAdaptable) {
				IAdaptable adapt = (IAdaptable) context;
				//try to get the ILaunchConfiguration adapter first
				config = (ILaunchConfiguration) adapt.getAdapter(ILaunchConfiguration.class);
				if(config != null) {
					DebugUITools.launch(config, mode);
					return;
				}
				else {
					//try to get the resource adapter from the context
					IResource resource = (IResource) adapt.getAdapter(IResource.class);
					if(resource != null) {
						fBackingResource = resource;
						config = getLaunchManager().getDefaultConfiguration(resource);
						if(config != null && config.exists()) {
							//the default config is available
							DebugUITools.launch(config, mode);
							return;
						}
						else {
							//there is no default config
							getLaunchManager().setDefaultConfiguration(resource, null);
							selectAndLaunch(fBackingResource, mode);
							return;
						}
					}
					else {
						handleUnknownContext(mode);
					}
				}
			}
			else {
				handleUnknownContext(mode);
			}
		}
		catch(CoreException ce) {DebugUIPlugin.log(ce);}
	}
	
	/**
	 * Handles the case where the context is unknown: Meaning that nothing can be launched.
	 * @param mode the mode
	 */
	protected void handleUnknownContext(String mode) {
		ILaunchConfiguration config = getLastLaunch(mode);
		if(config == null) {
			MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, ContextMessages.ContextRunner_7);
		}
		else {
			String prompt = DebugUIPlugin.getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_ALWAYS_RUN_LAST_LAUNCH);
			if(MessageDialogWithToggle.PROMPT.equals(prompt)) {
				ILaunchMode lmode = getLaunchManager().getLaunchMode(mode);
				MessageDialogWithToggle mdwt = MessageDialogWithToggle.openYesNoQuestion(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0,
						MessageFormat.format(ContextMessages.ContextRunner_1, new String[] {DebugUIPlugin.removeAccelerators(lmode.getLabel().toLowerCase())}), ContextMessages.ContextRunner_2, 
						false, null, null);
				int ret = mdwt.getReturnCode();
				boolean checked = mdwt.getToggleState();
				if(ret == IDialogConstants.YES_ID) {
					//get launch history for the given mode and do it 
					DebugUITools.launch(config, mode);
					if(checked) {
						DebugUIPlugin.getDefault().getPreferenceStore().putValue(IInternalDebugUIConstants.PREF_ALWAYS_RUN_LAST_LAUNCH, MessageDialogWithToggle.ALWAYS);
					}
				}
			}
			else if(MessageDialogWithToggle.ALWAYS.equals(prompt)) {
				DebugUITools.launch(config, mode);
			}
		}
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
	 * This method return if the editor input is from a shared java launch configuration file or not
	 * @param receiver the editor input to examine
	 * @return true if the editor input is from a shared launch configuration file, false otherwise.
	 */
	public ILaunchConfiguration isSharedConfigEditorInput(Object receiver) {
		if(receiver instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) receiver;
			return isSharedConfig(input.getFile());
		}
		return null;
	}
	
	/**
	 * Returns the launch group that corresponds to the specified mode
	 * @param mode the mode to find the launch group
	 * @return the launch group that corresponds to the specified mode
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
	 * Returns the shared config from the selected resource or <code>null</code> if the selected resources is not a shared config
	 * @param receiver
	 * @return the shared config from the selected resource or <code>null</code> if the selected resources is not a shared config
	 */
	public ILaunchConfiguration isSharedConfig(Object receiver) {
		if(receiver instanceof IFile) {
			IFile file = (IFile) receiver;
			String ext = file.getFileExtension();
			if(ext == null) {
				return null;
			}
			if(ext.equals("launch")) { //$NON-NLS-1$
				ILaunchConfiguration config = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(file);
				if(config != null && config.exists()) {
					return config;
				}
			}
		}
		return null;
	}
	
	/**
	 * Prompts the user to select a way of launching the current resource, where a 'way'
	 * is defined as a launch shortcut, and returns if a launch took place
	 * @param adapt the adaptable type the specified resource was derived from
	 * @param resource
	 * @return if the context was launched in the given mode or not
	 * @throws CoreException
	 */
	protected boolean selectAndLaunch(IResource resource, String mode) throws CoreException {
		boolean launched = false;
		if(launchDefault(resource, mode)) {
			return true;
		}
		List exts = getLaunchShortcuts(resource);
		if(exts.size() == 1) {
			//just launch it and set it as the default
			LaunchShortcutExtension ext = (LaunchShortcutExtension) exts.get(0);
			ext.launch(new StructuredSelection(resource), mode);
			return true;
		}
		else if(exts.size() < 1) {
			//prompt to try the parent containers
			String prompt = DebugUIPlugin.getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_ALWAYS_RUN_PROJECT_CONFIGURATION);
			if(MessageDialogWithToggle.ALWAYS.equals(prompt)) {
				//go ahead and check
				selectAndLaunch(resource.getProject(), mode);
			}
			else if(MessageDialogWithToggle.PROMPT.equals(prompt)) {
				MessageDialogWithToggle mdwt = MessageDialogWithToggle.openYesNoQuestion(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_3,
						MessageFormat.format(ContextMessages.ContextRunner_4, new String[] {fBackingResource.getName()}),
						ContextMessages.ContextRunner_2, false, null, null);
				int ret = mdwt.getReturnCode();
				boolean checked = mdwt.getToggleState();
				if(ret == IDialogConstants.YES_ID) {
					if(checked) {
						DebugUIPlugin.getDefault().getPreferenceStore().putValue(IInternalDebugUIConstants.PREF_ALWAYS_RUN_PROJECT_CONFIGURATION, MessageDialogWithToggle.ALWAYS);
					}
					selectAndLaunch(resource.getProject(), mode);
				}
			}
			return false;
		}
		else {
			boolean project = resource instanceof IProject;
			if(exts.isEmpty()) {
				MessageDialog.openError(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_5, ContextMessages.ContextRunner_6);
			}
			else {
				LaunchShortcutSelectionDialog dialog = new LaunchShortcutSelectionDialog(resource, mode, !project, project);
				if (dialog.open() == Window.OK) {
					Object[] result = dialog.getResult();
					if(result.length > 0) {
						fMakeProjectDefault = dialog.makeProjectDefault();
						fMakeResourceDefault = dialog.makeDefault();
						if(fMakeProjectDefault || fMakeResourceDefault) {
							getLaunchManager().addLaunchListener(this);
						}
						LaunchShortcutExtension method = (LaunchShortcutExtension) result[0];
						if(method != null) {
							method.launch(new StructuredSelection(resource), mode);
						}
					}
				}
			}
		}
		return launched;
	}
	
	/**
	 * Creates a listing of the launch shortcut extensions that are applicable to the underlying resource
	 * @param resource the underlying resource
	 * @return a listing of applicable launch shortcuts
	 * @throws CoreException
	 */
	public List getLaunchShortcuts(IResource resource) throws CoreException {
		List list = new ArrayList(); 
		List sc = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchShortcuts();
		List ctxt = new ArrayList();
		ctxt.add(resource);
		IEvaluationContext context = new EvaluationContext(null, ctxt);
		context.addVariable("selection", ctxt); //$NON-NLS-1$
		LaunchShortcutExtension ext = null;
		for(Iterator iter = sc.iterator(); iter.hasNext();) {
			ext = (LaunchShortcutExtension) iter.next();
			if(ext.evalEnablementExpression(context, ext.getContextualLaunchEnablementExpression())) {
				if(!list.contains(ext)) {
					list.add(ext);
				}
			}
		}
		return list;
	}
	
	/**
	 * Returns the current context to be considered for launching. 
	 * The returned object will be one of:
	 * <ol>
	 * <li>IEditorInput</li>
	 * <li>Object where <i>object</i> is the first element in the selection obtained from the 
	 * selection provider of the currently selected workbench part</li>
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
							return sel;
						}
						else {
							//default to returning the part, which can be further queried for adapters
							return part;
						}
					}
				}
			}
			
		}
		return null;
	}
	
	/**
	 * Returns the name of the currently selected context, or the empty string.
	 * This method can return null in the event the contributor of the selected context returns <code>null</code>
	 * as the resource name.
	 * @return the name of the currently selected context or the empty string. 
	 */
	public String getContextName() {
		Object o = getCurrentContext();
		ILaunchConfiguration config = isSharedConfig(o);
		if(config != null) {
			return config.getName();
		}
		else {
			config = isSharedConfigEditorInput(o);
			if(config != null) {
				return config.getName();
			}
			else {
				if(o instanceof IAdaptable) {
					IAdaptable adapt = (IAdaptable) o;
					Object a = adapt.getAdapter(ILaunchConfiguration.class);
					if(a != null) {
						return ((ILaunchConfiguration) a).getName();
					}
					else {
						a = adapt.getAdapter(IResource.class);
						if(a != null) {
							IResource res = (IResource) a;
							return (res.isAccessible() ? res.getName() : ""); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns if context launching is enabled
	 * @return if context launching is enabled
	 */
	public static boolean isContextLaunchEnabled() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH);
	}
	
	/**
	 * Returns the launch manager
	 * @return the launch manager
	 */
	protected LaunchManager getLaunchManager() {
		return (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		if(fBackingResource != null) {
			try {
				if(fMakeResourceDefault) {
					getLaunchManager().setDefaultConfiguration(fBackingResource, launch.getLaunchConfiguration());
				}
				if(fMakeProjectDefault) {
					getLaunchManager().setDefaultConfiguration(fBackingResource.getProject(), launch.getLaunchConfiguration());
				}
				getLaunchManager().removeLaunchListener(this);
				fMakeProjectDefault = false;
				fMakeResourceDefault = false;
			}
			catch(CoreException ce) {DebugUIPlugin.log(ce);}
		}
	}

	/**
	 * Handles launching the default launch configuration for the specified resource, if there is no default
	 * this method delegates to the <code>handleUnknownContext</code> method to figure out what to do
	 * @param res the resource to find the default configuration for
	 * @param mode the mode to launch it in
	 * @return true if the method launched something false otherwise
	 * @throws CoreException
	 */
	protected boolean launchDefault(IResource res, String mode) throws CoreException {
		ILaunchConfiguration config = getLaunchManager().getDefaultConfiguration(res);
		if(config != null && config.exists()) {
			DebugUITools.launch(config, mode);
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {}
	
}
