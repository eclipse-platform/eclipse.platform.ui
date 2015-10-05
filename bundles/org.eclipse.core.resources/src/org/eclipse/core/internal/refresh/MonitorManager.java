/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package org.eclipse.core.internal.refresh;

import java.util.*;
import java.util.Map.Entry;
import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.RefreshProvider;
import org.eclipse.core.runtime.*;

/**
 * Manages monitors by creating new monitors when projects are added and
 * removing monitors when projects are removed. Also handles the polling
 * mechanism when contributed native monitors cannot handle a project.
 *
 * @since 3.0
 */
class MonitorManager implements ILifecycleListener, IPathVariableChangeListener, IResourceChangeListener, IResourceDeltaVisitor {
	/**
	 * The PollingMonitor in charge of doing file-system polls.
	 */
	protected final PollingMonitor pollMonitor;
	/**
	 * The list of registered monitor factories.
	 */
	private RefreshProvider[] providers;
	/**
	 * Reference to the refresh manager.
	 */
	protected final RefreshManager refreshManager;
	/**
	 * A mapping of monitors to a list of resources each monitor is responsible for.
	 */
	protected final Map<IRefreshMonitor, List<IResource>> registeredMonitors;
	/**
	 * Reference to the workspace.
	 */
	protected IWorkspace workspace;

	public MonitorManager(IWorkspace workspace, RefreshManager refreshManager) {
		this.workspace = workspace;
		this.refreshManager = refreshManager;
		registeredMonitors = Collections.synchronizedMap(new HashMap<IRefreshMonitor, List<IResource>>(10));
		pollMonitor = new PollingMonitor(refreshManager);
	}

	/**
	 * Queries extensions of the refreshProviders extension point, and
	 * creates the provider classes. Will never return <code>null</code>.
	 *
	 * @return RefreshProvider[] The array of registered <code>RefreshProvider</code>
	 *             objects or an empty array.
	 */
	private RefreshProvider[] getRefreshProviders() {
		if (providers != null)
			return providers;
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_REFRESH_PROVIDERS);
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		List<RefreshProvider> providerList = new ArrayList<>(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			RefreshProvider provider = null;
			try {
				provider = (RefreshProvider) configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				Policy.log(IStatus.WARNING, Messages.refresh_installError, e);
			}
			if (provider != null)
				providerList.add(provider);
		}
		providers = providerList.toArray(new RefreshProvider[providerList.size()]);
		return providers;
	}

	/**
	 * Collects the set of root resources that required monitoring. This
	 * includes projects and all linked resources.
	 */
	private List<IResource> getResourcesToMonitor() {
		final List<IResource> resourcesToMonitor = new ArrayList<>(10);
		IProject[] projects = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++) {
			if (!projects[i].isAccessible())
				continue;
			resourcesToMonitor.add(projects[i]);
			try {
				IResource[] members = projects[i].members();
				for (int j = 0; j < members.length; j++)
					if (members[j].isLinked())
						resourcesToMonitor.add(members[j]);
			} catch (CoreException e) {
				Policy.log(IStatus.WARNING, Messages.refresh_refreshErr, e);
			}
		}
		return resourcesToMonitor;
	}

	@Override
	public void handleEvent(LifecycleEvent event) {
		switch (event.kind) {
			case LifecycleEvent.PRE_LINK_DELETE :
			case LifecycleEvent.PRE_PROJECT_CLOSE :
			case LifecycleEvent.PRE_PROJECT_DELETE :
				unmonitor(event.resource);
				break;
		}
	}

	private boolean isMonitoring(IResource resource) {
		synchronized (registeredMonitors) {
			for (Iterator<IRefreshMonitor> i = registeredMonitors.keySet().iterator(); i.hasNext();) {
				List<IResource> resources = registeredMonitors.get(i.next());
				if ((resources != null) && (resources.contains(resource)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Installs a monitor on the given resource. Returns true if the polling
	 * monitor was installed, and false if a refresh provider was installed.
	 */
	boolean monitor(IResource resource) {
		if (isMonitoring(resource))
			return false;
		boolean pollingMonitorNeeded = true;
		RefreshProvider[] refreshProviders = getRefreshProviders();
		for (int i = 0; i < refreshProviders.length; i++) {
			IRefreshMonitor monitor = safeInstallMonitor(refreshProviders[i], resource);
			if (monitor != null) {
				registerMonitor(monitor, resource);
				pollingMonitorNeeded = false;
			}
		}
		if (pollingMonitorNeeded) {
			pollMonitor.monitor(resource);
			registerMonitor(pollMonitor, resource);
		}
		return pollingMonitorNeeded;
	}

	/* (non-Javadoc)
	 * @see IRefreshResult#monitorFailed
	 */
	public void monitorFailed(IRefreshMonitor monitor, IResource resource) {
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " monitor (" + monitor + ") failed to monitor resource: " + resource); //$NON-NLS-1$ //$NON-NLS-2$
		if (registeredMonitors == null || monitor == null)
			return;
		if (resource == null) {
			List<IResource> resources = registeredMonitors.get(monitor);
			if (resources == null || resources.isEmpty()) {
				registeredMonitors.remove(monitor);
				return;
			}
			// synchronized: protect the collection during iteration
			synchronized (registeredMonitors) {
				for (Iterator<IResource> i = resources.iterator(); i.hasNext();) {
					resource = i.next();
					pollMonitor.monitor(resource);
					registerMonitor(pollMonitor, resource);
				}
				registeredMonitors.remove(monitor);
			}
		} else {
			removeMonitor(monitor, resource);
			pollMonitor.monitor(resource);
			registerMonitor(pollMonitor, resource);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableChangeListener#pathVariableChanged(org.eclipse.core.resources.IPathVariableChangeEvent)
	 */
	@Override
	public void pathVariableChanged(IPathVariableChangeEvent event) {
		if (registeredMonitors.isEmpty())
			return;
		String variableName = event.getVariableName();
		Set<IResource> invalidResources = new HashSet<>();
		for (Iterator<List<IResource>> i = registeredMonitors.values().iterator(); i.hasNext();) {
			for (Iterator<IResource> j = i.next().iterator(); j.hasNext();) {
				IResource resource = j.next();
				IPath rawLocation = resource.getRawLocation();
				if (rawLocation != null) {
					if (rawLocation.segmentCount() > 0 && variableName.equals(rawLocation.segment(0)) && !invalidResources.contains(resource)) {
						invalidResources.add(resource);
					}
				}
			}
		}
		if (!invalidResources.isEmpty()) {
			for (Iterator<IResource> i = invalidResources.iterator(); i.hasNext();) {
				IResource resource = i.next();
				unmonitor(resource);
				monitor(resource);
			}
		}
	}

	private void registerMonitor(IRefreshMonitor monitor, IResource resource) {
		// synchronized: protect the collection during add
		synchronized (registeredMonitors) {
			List<IResource> resources = registeredMonitors.get(monitor);
			if (resources == null) {
				resources = new ArrayList<>(1);
				registeredMonitors.put(monitor, resources);
			}
			if (!resources.contains(resource))
				resources.add(resource);
		}
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " added monitor (" + monitor + ") on resource: " + resource); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void removeMonitor(IRefreshMonitor monitor, IResource resource) {
		// synchronized: protect the collection during remove
		synchronized (registeredMonitors) {
			List<IResource> resources = registeredMonitors.get(monitor);
			if (resources != null && !resources.isEmpty())
				resources.remove(resource);
			else
				registeredMonitors.remove(monitor);
		}
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " removing monitor (" + monitor + ") on resource: " + resource); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private IRefreshMonitor safeInstallMonitor(RefreshProvider provider, IResource resource) {
		Throwable t = null;
		try {
			return provider.installMonitor(resource, refreshManager);
		} catch (Exception e) {
			t = e;
		} catch (LinkageError e) {
			t = e;
		}
		IStatus error = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, Messages.refresh_installError, t);
		Policy.log(error);
		return null;
	}

	/**
	 * Start the monitoring of resources by all monitors.
	 */
	public void start() {
		boolean refreshNeeded = false;
		for (Iterator<IResource> i = getResourcesToMonitor().iterator(); i.hasNext();)
			refreshNeeded |= !monitor(i.next());
		workspace.getPathVariableManager().addChangeListener(this);
		workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		//adding the lifecycle listener twice does no harm
		((Workspace) workspace).addLifecycleListener(this);
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " starting monitor manager."); //$NON-NLS-1$
		//If not exclusively using polling, create a polling monitor and run it once, to catch
		//changes that occurred while the native monitor was turned off.
		if (refreshNeeded)
			new PollingMonitor(refreshManager).runOnce();
	}

	/**
	 * Stop the monitoring of resources by all monitors.
	 */
	public void stop() {
		workspace.removeResourceChangeListener(this);
		workspace.getPathVariableManager().removeChangeListener(this);
		// synchronized: protect the collection during iteration
		synchronized (registeredMonitors) {
			for (Iterator<IRefreshMonitor> i = registeredMonitors.keySet().iterator(); i.hasNext();) {
				IRefreshMonitor monitor = i.next();
				monitor.unmonitor(null);
			}
		}
		registeredMonitors.clear();
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " stopping monitor manager."); //$NON-NLS-1$
		pollMonitor.cancel();
	}

	void unmonitor(IResource resource) {
		if (resource == null || !isMonitoring(resource))
			return;
		synchronized (registeredMonitors) {
			for (Iterator<Entry<IRefreshMonitor, List<IResource>>> i = registeredMonitors.entrySet().iterator(); i.hasNext();) {
				Entry<IRefreshMonitor, List<IResource>> current = i.next();
				List<IResource> resources = current.getValue();
				if ((resources != null) && !resources.isEmpty() && resources.contains(resource)) {
					current.getKey().unmonitor(resource);
					resources.remove(resource);
				}
			}
		}
		if (resource.getType() == IResource.PROJECT)
			unmonitorLinkedContents((IProject) resource);
	}

	private void unmonitorLinkedContents(IProject project) {
		if (!project.isAccessible())
			return;
		IResource[] children = null;
		try {
			children = project.members();
		} catch (CoreException e) {
			Policy.log(IStatus.WARNING, Messages.refresh_refreshErr, e);
		}
		if (children != null && children.length > 0)
			for (int i = 0; i < children.length; i++)
				if (children[i].isLinked())
					unmonitor(children[i]);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null)
			return;
		try {
			delta.accept(this);
		} catch (CoreException e) {
			//cannot happen as our visitor doesn't throw exceptions
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) {
		if (delta.getKind() == IResourceDelta.ADDED) {
			IResource resource = delta.getResource();
			if (resource.isLinked())
				monitor(resource);
		}
		if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
			IProject project = (IProject) delta.getResource();
			if (project.isAccessible())
				monitor(project);
		}
		return true;
	}
}
