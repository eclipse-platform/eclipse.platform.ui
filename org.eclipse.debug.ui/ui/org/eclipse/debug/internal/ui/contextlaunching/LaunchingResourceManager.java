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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.ILaunchHistoryChangedListener;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import com.ibm.icu.text.MessageFormat;

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
public class LaunchingResourceManager implements IPropertyChangeListener, IWindowListener, ISelectionListener, ILaunchHistoryChangedListener, ILaunchesListener2 {
	
	/**
	 *The set of label update listeners
	 */
	private ListenerList fLabelListeners = new ListenerList(); 
	
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
	 * The selection has changed and we need to update the labels
	 */
	private boolean fUpdateLabel = true;
	
	/**
	 * Set of windows that have been opened and that we have registered selection listeners with
	 */
	private HashSet fWindows = new HashSet();
	
	/**
	 * Cache of IResource -> ILaunchConfiguration[] used during a tooltip update job. 
	 * The cache is cleared after each tooltip update job is complete.
	 */
	private HashMap fConfigCache = new HashMap();
	
	/**
	 * Cache of IResource -> LaunchShortcutExtension used during a tooltip update job.
	 * The cache is cleared after each tooltip update job is complete.
	 */
	private HashMap fExtCache = new HashMap();
	
	/**
	 * Constant denoting the empty string;
	 */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Provides a mouse tracker listener for the launching main toolbar 
	 */
	private MouseTrackAdapter fMouseListener = new MouseTrackAdapter() {
		public void mouseEnter(MouseEvent e) {
			if(fUpdateLabel) {
				fUpdateLabel = false;
				fCurrentLabels.clear();
				Job job = new Job("Compute launch button tooltip") { //$NON-NLS-1$
					protected IStatus run(IProgressMonitor monitor) {
						computeLabels();
						fConfigCache.clear();
						fExtCache.clear();
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
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
	public void addLaunchLabelUpdateListener(ILaunchLabelChangedListener listener) {
		fLabelListeners.add(listener);
	}
	
	/**
	 * Removes the specified <code>AbstractLaunchHistoryAction</code> from the listing of registered 
	 * listeners
	 * @param action the action to remove
	 * @param group the launch group
	 * @return true if the action was removed from the listing of <code>AbstractLaunchHistoryAction</code> listeners,
	 * false otherwise
	 */
	public void removeLaunchLabelChangedListener(ILaunchLabelChangedListener listener) {
		fLabelListeners.remove(listener);
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
	 * Returns if the the last launch configuration should be launched if the selected resource is not launchable and context launching is enabled
	 * @return true if the last launched should be launched, false otherwise
	 */
	protected boolean shouldLaunchLast() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_LAST_IF_NOT_LAUNCHABLE);
	}
	
	/**
	 * Computes the current listing of labels for the given <code>IResource</code> context change or the 
	 * current launch history changed event
	 */
	protected void computeLabels() {
		ILaunchGroup group = null;
		ILaunchConfiguration config = null;
		String label = null;
		Object[] listeners = fLabelListeners.getListeners();
		for(int i = 0; i < listeners.length; i++) {
			group = ((ILaunchLabelChangedListener)listeners[i]).getLaunchGroup();
			if(group != null) {
				if(isContextLaunchEnabled() && !group.getIdentifier().equals("org.eclipse.ui.externaltools.launchGroup")) { //$NON-NLS-1$
					label = getResourceLabel(SelectedResourceManager.getDefault().getSelectedResource(), group);
				}
				else {
					config = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getFilteredLastLaunch(group.getIdentifier());
					if(config != null) {
						label = appendLaunched(config);
					}
				}
				fCurrentLabels.put(group, label);
				label = null;
			}
		}
		notifyLabelChanged();
	}
	
	/**
	 * Notifies all registered listeners that the known labels have changed
	 */
	protected void notifyLabelChanged() {
		Object[] listeners = fLabelListeners.getListeners();
		for(int i = 0; i < listeners.length; i++) {
			((ILaunchLabelChangedListener)listeners[i]).labelChanged();
		}
	}
	
	/**
	 * Appends the text '(already running)' to the tooltip label if there is a launch currently
	 * running (not terminated) with the same backing launch configuration as the one specified
	 * @param config
	 * @return the appended string for the tooltip label or the configuration name (default)
	 */
	private String appendLaunched(ILaunchConfiguration config) {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		boolean launched = false;
		ILaunchConfiguration tmp = null;
		for(int i = 0; i < launches.length; i++) {
			tmp = launches[i].getLaunchConfiguration();
			if(tmp != null) {
				if(!launches[i].isTerminated() && tmp.equals(config)) {
					launched = true;
					break;
				}
			}
		}
		if(launched) {
			return MessageFormat.format(ContextMessages.LaunchingResourceManager_0, new String[] {config.getName()});
		}
		return config.getName();
	}
	
	/**
	 * Returns the label for the last launched configuration or and empty string if there was no last launch.
	 * @param group
	 * @return the name of the last launched configuration, altered with '(running)' if needed, or the empty
	 * string if there is no last launch.
	 */
	protected String getlastLaunchedLabel(ILaunchGroup group) {
		ILaunchConfiguration config = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getFilteredLastLaunch(group.getIdentifier());
		if(config != null) {
			return appendLaunched(config);
		}
		return EMPTY_STRING;
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
				String label = getlastLaunchedLabel(group);
				if(!EMPTY_STRING.equals(label)) {
					return label;
				}
			}
			//otherwise try to determine if there is a way to launch it
			List shortcuts = ContextRunner.getDefault().getLaunchShortcutsForEmptySelection();
			if(!shortcuts.isEmpty()) {
				return ContextMessages.ContextRunner_14;
			}
			else {
				return EMPTY_STRING;
			}
		}
		LaunchConfigurationManager lcm = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		//see if the context is a shared configuration
		ILaunchConfiguration config = lcm.isSharedConfig(resource);
		if(config != null) {
			return appendLaunched(config);
		}
		List configs = (List) fConfigCache.get(resource);
		if(configs == null) {
			configs = lcm.getApplicableLaunchConfigurations(resource);
			fConfigCache.put(resource, configs);
		}
		int csize = configs.size();
		if(csize == 1) {
			return appendLaunched((ILaunchConfiguration)configs.get(0));
		}
		else if(csize > 1) {
			config = lcm.getMRUConfiguration(configs, group, resource);
			if(config != null) {
				return appendLaunched(config);
			}
			else {
				return ContextMessages.ContextRunner_14;
			}
		}
		else {
			List exts = (List) fExtCache.get(resource);
			if(exts == null) {
				exts = lcm.getLaunchShortcuts(resource);
				fExtCache.put(resource, exts);
			}
			int esize = exts.size();
			if(esize == 0) {
				if(shouldCheckParent()) {
					IProject project = resource.getProject();
					if(project != null && !project.equals(resource)) {
						return getResourceLabel(project, group);
					}
				}
				else if(shouldLaunchLast()) {
					return getlastLaunchedLabel(group);
				}
				else {
					return ContextMessages.ContextRunner_15;
				}
			}
			if(esize == 1) {
				return resource.getName();
			}
			else {
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
		DebugUIPlugin.getDefault().getLaunchConfigurationManager().addLaunchHistoryListener(this);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
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
		DebugUIPlugin.getDefault().getLaunchConfigurationManager().removeLaunchHistoryListener(this);
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		IWorkbenchWindow window = null;
		ToolBar bar = null;
		for(Iterator iter = fToolbars.keySet().iterator(); iter.hasNext();) {
			window = (IWorkbenchWindow) iter.next();
			bar = (ToolBar) fToolbars.get(window);
			if(bar != null && !bar.isDisposed()) {
				bar.removeMouseTrackListener(fMouseListener);
			}
		}
		for(Iterator iter = fWindows.iterator(); iter.hasNext();) {
			((IWorkbenchWindow)iter.next()).getSelectionService().removeSelectionListener(this);
		}
		fWindows.clear();
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
		if(fWindows.remove(window)) {
			window.getSelectionService().removeSelectionListener(this);
		}
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		if(fWindows.add(window)) {
			window.getSelectionService().addSelectionListener(this);
		}
	}
	
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
		if(event.getProperty().equals(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH) ||
				event.getProperty().equals(IInternalDebugUIConstants.PREF_LAUNCH_LAST_IF_NOT_LAUNCHABLE)) {
			if(isContextLaunchEnabled()) {
				windowActivated(DebugUIPlugin.getActiveWorkbenchWindow());
			}
			fUpdateLabel = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fUpdateLabel = isContextLaunchEnabled();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.ILaunchHistoryChangedListener#launchHistoryChanged()
	 */
	public void launchHistoryChanged() {
		//this always must be set to true, because as the history is loaded these events are fired, and we need to
		//update on workspace load.
		fUpdateLabel = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
		fUpdateLabel = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
		fUpdateLabel = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {}
}
