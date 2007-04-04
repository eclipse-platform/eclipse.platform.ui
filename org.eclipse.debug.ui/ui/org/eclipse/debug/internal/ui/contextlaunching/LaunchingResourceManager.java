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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.ILaunchLabelChangedListener;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * This manager is used to calculate the labels for the current resource or for the current 
 * state of the launch history, depending on the enabled status of contextual launching. More specifically
 * if contextual launching is enabled the calculated labels are for the current resource, otherwise 
 * the calculated labels are for the current state of the launch history.
 * 
 * Any actions interested in being notified of launch label updates need to register with this manager, and implement
 * the <code>ILaunchLabelChangedListener</code> interface.
 * 
 * @see ILaunchLabelChangedListener
 * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction
 * 
 * @since 3.3
 */
public class LaunchingResourceManager implements IPropertyChangeListener, IWindowListener {
	
	/**
	 *The set of label update listeners
	 */
	private HashSet fLabelListeners = new HashSet(); 
	
	/**
	 * The map of ToolBars that have mouse tracker listeners associated with them:
	 * stored as Map<IWorkbenchWindow, ToolBar>
	 */
	private HashMap fToolbars = new HashMap();
	
	/**
	 * the map of current labels
	 */
	private HashMap fCurrentLabels = new HashMap();
	
	/**
	 * Provides a mouse tracker listener for the launching main toolbar 
	 */
	private MouseTrackAdapter fMouseListener = new MouseTrackAdapter() {
		public void mouseEnter(MouseEvent e) {
			computeLabels();
		}
	};
	
	/**
	 * Returns if context launching is enabled
	 * @return if context launching is enabled
	 */
	public static boolean isContextLaunchEnabled() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH);
	}
	
	/**
	 * Allows an <code>AbstractLaunchHistoryAction</code> to register with this manager to be notified
	 * of a context (<code>IResource</code>) change and have its updateToolTip(..) method called back to.
	 * @param action the action to add
	 * @param group the launch group
	 * @return true if the <code>AbstractLaunchHistoryAction</code> was added as a listener, false otherwise
	 */
	public boolean addLaunchLabelUpdateListener(ILaunchLabelChangedListener listener) {
		return fLabelListeners.add(listener);
	}
	
	/**
	 * Removes the specified <code>AbstractLaunchHistoryAction</code> from the listing of registered 
	 * listeners
	 * @param action the action to remove
	 * @param group the launch group
	 * @return true if the action was removed from the listing of <code>AbstractLaunchHistoryAction</code> listeners,
	 * false otherwise
	 */
	public boolean removeLaunchLabelChangedListener(ILaunchLabelChangedListener listener) {
		return fLabelListeners.remove(listener);
	}
	
	/**
	 * Returns the current resource label to be displayed.
	 * 
	 * @param group the launch group to get the label for
	 * @return the current resource label;
	 */
	public String getLaunchLabel(ILaunchGroup group) {
		return (String) fCurrentLabels.get(group); 
	}
	
	/**
	 * Returns if the parent project should be checked automatically
	 * @return true if the parent project should checked automatically, false otherwise
	 */
	protected boolean shouldCheckParent() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_PARENT_PROJECT);
	}
	
	/**
	 * Computes the current listing of labels for the given <code>IResource</code> context change or the 
	 * current launch history changed event
	 */
	protected void computeLabels() {
		fCurrentLabels.clear();
		ILaunchGroup group = null;
		boolean changed = false;
		ILaunchConfiguration config = null;
		String label = null;
		for(Iterator iter = fLabelListeners.iterator(); iter.hasNext();) {
			group = ((ILaunchLabelChangedListener) iter.next()).getLaunchGroup();
			if(group != null) {
				if(isContextLaunchEnabled() && !group.getIdentifier().equals("org.eclipse.ui.externaltools.launchGroup")) { //$NON-NLS-1$
					label = getResourceLabel(SelectedResourceManager.getDefault().getSelectedResource(), group);
				}
				else {
					config = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getFilteredLastLaunch(group.getIdentifier());
					if(config != null) {
						label = config.getName();
					}
					else {
						//need to force an update to display the default label.
						//case: no launch history, switch off context launching, label needs to update
						label = null;
						changed |= true;
					}
				}
				changed |= fCurrentLabels.put(group, label) != null;
			}
		}
		if(changed) {
			//notify the listeners of a label update
			for(Iterator iter = fLabelListeners.iterator(); iter.hasNext();) {
				((ILaunchLabelChangedListener)iter.next()).labelChanged();
			}
		}
	}
	
	/**
	 * Returns the label for the specified resource or the empty string, never <code>null</code>
	 * @param resource
	 * @param group
	 * @return the label for the resource or the empty string, never <code>null</code>
	 */
	protected String getResourceLabel(IResource resource, ILaunchGroup group) {
		if(resource == null) {
			//no resource try last launch like the runner does
			if(group != null) {
				ILaunchConfiguration config = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getFilteredLastLaunch(group.getIdentifier());
				if(config != null) {
					return config.getName();
				}
			}
			//otherwise try to determine if there is a way to launch it
			List shortcuts = ContextRunner.getDefault().getLaunchShortcutsForEmptySelection();
			if(!shortcuts.isEmpty()) {
				return ContextMessages.ContextRunner_14;
			}
			else {
				return ""; //$NON-NLS-1$
			}
		}
		LaunchConfigurationManager lcm = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		//see if the context is a shared configuration
		ILaunchConfiguration config = lcm.isSharedConfig(resource);
		if(config != null) {
			return config.getName();
		}
		List configs = lcm.getApplicableLaunchConfigurations(resource);
		int csize = configs.size();
		if(csize == 1) {
			return ((ILaunchConfiguration)configs.get(0)).getName();
		}
		else if(csize > 1) {
			config = lcm.getMRUConfiguration(configs, group);
			if(config != null) {
				return config.getName();
			}
			else {
				//TODO could cause TVT issues
				return ContextMessages.ContextRunner_14;
			}
		}
		else {
			List exts = lcm.getLaunchShortcuts(resource);
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
	}
	
	/**
	 * Starts up the manager
	 */
	public void startup() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if(workbench != null) {
			workbench.addWindowListener(this);
		}
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Shutdown and clean up the manager
	 */
	public void shutdown() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if(workbench != null) {
			workbench.removeWindowListener(this);
		}
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		IWorkbenchWindow window = null;
		ToolBar bar = null;
		for(Iterator iter = fToolbars.keySet().iterator(); iter.hasNext();) {
			window = (IWorkbenchWindow) iter.next();
			bar = (ToolBar) fToolbars.remove(window);
			if(bar != null && !bar.isDisposed()) {
				bar.removeMouseTrackListener(fMouseListener);
			}
		}
		fToolbars.clear();
		fLabelListeners.clear();
		fCurrentLabels.clear();
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		if(!fToolbars.containsKey(window)) {
			addMouseListener(window);
		}
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		ToolBar bar = (ToolBar) fToolbars.remove(window);
		if(bar != null && !bar.isDisposed()) {
			bar.removeMouseTrackListener(fMouseListener);
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
	
	/**
	 * Adds a mouse listener to the launch toolbar 
	 * 
	 * @param window
	 */
	private void addMouseListener(IWorkbenchWindow window) {
		CoolBarManager cmgr = ((WorkbenchWindow)window).getCoolBarManager();
		if(cmgr != null) {
			IContributionItem item = cmgr.find("org.eclipse.debug.ui.launchActionSet"); //$NON-NLS-1$
			if(item instanceof ToolBarContributionItem) {
				ToolBarManager tmgr = (ToolBarManager) ((ToolBarContributionItem)item).getToolBarManager();
				ToolBar bar = tmgr.getControl();
				if(bar != null && !bar.isDisposed()) {
					bar.addMouseTrackListener(fMouseListener);
					fToolbars.put(window, bar);
				}
			}
		}
	}
	
	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH)) {
			if(isContextLaunchEnabled()) {
				windowActivated(DebugUIPlugin.getActiveWorkbenchWindow());
			}
		}
	}
}
