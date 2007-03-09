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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This manager is used to calculate the current resource and its labels on certain
 * selection and part activated events
 * 
 * @since 3.3
 */
public class ContextLaunchingResourceManager implements ISelectionListener, IPartListener, IWindowListener {
	
	/**
	 * The map of listeners arranged as: Map<ILaunchGroup, Set<AbstractLaunchHistoryAction>>
	 */
	private HashMap fListeners = new HashMap(); 
	
	/**
	 * The set of windows
	 */
	private HashSet fWindows = new HashSet();
	
	/**
	 * the map of current labels
	 */
	private HashMap fCurrentLabels = new HashMap();
	
	/**
	 * The current <code>IResource</code>
	 */
	private IResource fCurrentResource = null;
	
	/**
	 * Allows an <code>AbstractLaunchHistoryAction</code> to register with this manager to be notified
	 * of a context (<code>IResource</code>) change and have its updateToolTip(..) method called back to.
	 * @param action the action to add
	 * @param group the launch group
	 * @return true if the <code>AbstractLaunchHistoryAction</code> was added as a listener, false otherwise
	 */
	public boolean addUpdateListener(AbstractLaunchHistoryAction action, ILaunchGroup group) {
		Set set = (Set) fListeners.get(group);
		if(set == null) {
			set = new HashSet();
			fListeners.put(group, set);
		}
		return set.add(action);
	}
	
	/**
	 * Removes the specified <code>AbstractLaunchHistoryAction</code> from the listing of registered 
	 * listeners
	 * @param action the action to remove
	 * @param group the launch group
	 * @return true if the action was removed from the listing of <code>AbstractLaunchHistoryAction</code> listeners,
	 * false otherwise
	 */
	public boolean removeUpdateListener(AbstractLaunchHistoryAction action, ILaunchGroup group) {
		Set set = (Set) fListeners.get(group);
		if(set == null) {
			return false;
		}
		return set.remove(action);
	}
	
	/**
	 * Called to notify the listing of listeners that they should update
	 */
	protected void notifyListeners() {
		Set set = null;
		for(Iterator iter = fListeners.keySet().iterator(); iter.hasNext();) {
			set = (Set) fListeners.get(iter.next());
			for(Iterator iter2 = set.iterator(); iter2.hasNext();) {
				((AbstractLaunchHistoryAction)iter2.next()).launchHistoryChanged();
			}
		}
	}
	
	/**
	 * Returns the current resource label to be displayed.
	 * 
	 * @param group the launch group to get the label for
	 * @return the current resource label;
	 */
	public String getContextLabel(ILaunchGroup group) {
		String label = (String) fCurrentLabels.get(group); 
		if(label == null) {
			//equates to only trying once as all labels are generated when we computer labels
			computeLabels();
			label = (String) fCurrentLabels.get(group);
		}
		return label;
	}
	
	/**
	 * Returns the current <code>IResource</code> associated with the current selection, which could be either 
	 * a <code>IStructuredSelection</code> from a viewer, or a <code>TextSelection</code> from an editor.
	 * 
	 * @return the associated <code>IResource</code> of the currently selected object
	 */
	public IResource getCurrentResource() {
		return fCurrentResource;
	}
	
	/**
	 * Returns the launch configuration manager
	 * @return the launch configuration manager
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
	
	/**
	 * Returns if the parent project should be checked automatically
	 * @return true if the parent project should checked automatically, false otherwise
	 */
	protected boolean shouldCheckParent() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_PARENT_PROJECT);
	}
	
	/**
	 * Computes the current listing of labels for the given <code>IResource</code> context change
	 */
	protected void computeLabels() {
		fCurrentLabels.clear();
		ILaunchGroup group = null;
		for(Iterator iter = fListeners.keySet().iterator(); iter.hasNext();) {
			group = (ILaunchGroup) iter.next();
			fCurrentLabels.put(group, getResourceLabel(fCurrentResource, group));
		}
	}
	
	/**
	 * Returns the label for the specified resource or the empty string, never <code>null</code>
	 * @param resource
	 * @param group
	 * @return the label for the resource or the empty string, never <code>null</code>
	 */
	protected String getResourceLabel(IResource resource, ILaunchGroup group) {
		if(fCurrentResource == null) {
			//no resource try last launch like the runner does
			if(group != null) {
				LaunchHistory history = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchHistory(group.getIdentifier());
				if(history != null) {
					ILaunchConfiguration config = history.getRecentLaunch();
					if(config != null) {
						return config.getName();
					}
				}
			}
			//otherwise try to determine if there is a way to launch it
			try {
				List shortcuts = ContextRunner.getDefault().getLaunchShortcutsForEmptySelection();
				if(!shortcuts.isEmpty()) {
					return ContextMessages.ContextRunner_14;
				}
				else {
					return ""; //$NON-NLS-1$
				}
			}
			catch(CoreException ce) {DebugUIPlugin.log(ce);}
		}
		List configs = getLaunchConfigurationManager().getApplicableLaunchConfigurations(resource);
		int csize = configs.size();
		ILaunchConfiguration config = null;
		if(csize == 1) {
			return ((ILaunchConfiguration)configs.get(0)).getName();
		}
		else if(csize > 1) {
			config = getLaunchConfigurationManager().getMRUConfiguration(configs, group);
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
				List exts = getLaunchConfigurationManager().getLaunchShortcuts(resource);
				int esize = exts.size();
				if(esize == 0) {
					IProject project = resource.getProject();
					if(project != null && !project.equals(resource)) {
						if(shouldCheckParent()) {
							return getResourceLabel(project, group);
						}
						else {
							//TODO could cause TVT issues
							return ContextMessages.ContextRunner_15;
						}
					}
				}
				if(esize == 1) {
					return resource.getName();
				}
				else {
					//TODO could cause TVT issues
					return ContextMessages.ContextRunner_14;
				}
			}
			catch(CoreException ce) {DebugUIPlugin.log(ce);}
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Starts up the manager
	 */
	public void startup() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if(workbench != null) {
			workbench.addWindowListener(this);
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if(window != null) {
				if(fWindows.add(window)) {
					window.getSelectionService().addSelectionListener(this);
					window.getPartService().addPartListener(this);
				}
			}
		}
	}

	/**
	 * Shutdown and clean up the manager
	 */
	public void shutdown() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if(workbench != null) {
			workbench.removeWindowListener(this);
		}
		for(Iterator iter = fWindows.iterator(); iter.hasNext();) {
			IWorkbenchWindow window = (IWorkbenchWindow)iter.next();
			window.getSelectionService().removeSelectionListener(this);
			window.getPartService().removePartListener(this);
		}
		fWindows.clear();
		fListeners.clear();
		fCurrentLabels.clear();
		fCurrentResource = null;
	}
	
	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(!(part instanceof IEditorPart)) {
			if(selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if(!ss.isEmpty()) {
					Object o = ss.getFirstElement();
					if(o instanceof IAdaptable) {
						fCurrentResource = (IResource) ((IAdaptable)o).getAdapter(IResource.class);
						computeLabels();
						notifyListeners();
					}
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if(part instanceof IEditorPart) {
			fCurrentResource = (IResource) ((IEditorPart)part).getEditorInput().getAdapter(IResource.class);
			computeLabels();
			notifyListeners();
		}
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {}

	/**
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {}

	/**
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		if(fWindows.add(window)) {
			window.getSelectionService().addSelectionListener(this);
			window.getPartService().addPartListener(this);
		}
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		if(fWindows.remove(window)) {
			window.getSelectionService().removeSelectionListener(this);
			window.getPartService().removePartListener(this);
		}
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {}
}
