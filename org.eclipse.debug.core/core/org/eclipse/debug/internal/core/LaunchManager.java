package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;

/**
 * Manages registered launches.
 *
 * @see ILaunchManager
 */
public class LaunchManager extends PlatformObject implements ILaunchManager  {
	
	/**
	 * Constant for use as local name part of <code>QualifiedName</code>
	 * for persisting the default launcher.
	 */
	 private static final String DEFAULT_LAUNCHER= "launcher"; //$NON-NLS-1$
	 
	 /**
	  * Types of notifications
	  */
	 private static final int REGISTERED = 0;
	 private static final int DEREGISTERED = 1;

	/**
	 * Collection of launches
	 */
	protected Vector fLaunches= new Vector(10);

	/**
	 * Collection of listeners
	 */
	protected ListenerList fListeners= new ListenerList(5);
		
	/**
	 * @see ILaunchManager
	 */
	public void addLaunchListener(ILaunchListener listener) {
		fListeners.add(listener);
	}
	
	/**
	 * @see ILaunchManager
	 */
	public void deregisterLaunch(ILaunch launch) {
		if (launch == null) {
			return;
		}
		fLaunches.remove(launch);
		fireUpdate(launch, DEREGISTERED);
	}

	/**
	 * @see ILaunchManager
	 */
	public ILaunch findLaunch(IProcess process) {
		synchronized (fLaunches) {
			for (int i= 0; i < fLaunches.size(); i++) {
				ILaunch l= (ILaunch) fLaunches.elementAt(i);
				IProcess[] ps= l.getProcesses();
				for (int j= 0; j < ps.length; j++) {
					if (ps[j].equals(process)) {
							return l;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @see ILaunchManager
	 */
	public ILaunch findLaunch(IDebugTarget target) {
		synchronized (fLaunches) {
			for (int i= 0; i < fLaunches.size(); i++) {
				ILaunch l= (ILaunch) fLaunches.elementAt(i);
				if (target.equals(l.getDebugTarget())) {
					return l;
				}
			}
		}
		return null;
	}

	/**
	 * Fires notification to the listeners that a launch has been (de)registered.
	 */
	public void fireUpdate(ILaunch launch, int update) {
		Object[] copiedListeners= fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			ILaunchListener listener = (ILaunchListener)copiedListeners[i];
			switch (update) {
				case REGISTERED:
					listener.launchRegistered(launch);
					break;
				case DEREGISTERED:
					listener.launchDeregistered(launch);
					break;
			}
		}
	}

	/**
	 * @see ILaunchManager
	 */
	public IDebugTarget[] getDebugTargets() {
		List targets= new ArrayList(fLaunches.size());
		if (fLaunches.size() > 0) {
			Iterator e= fLaunches.iterator();
			while (e.hasNext()) {
				IDebugTarget target= ((ILaunch) e.next()).getDebugTarget();
				if (target != null)
					targets.add(target);
			}
		}
		return (IDebugTarget[])targets.toArray(new IDebugTarget[targets.size()]);
	}

	/**
	 * @see ILaunchManager
	 */
	public ILauncher getDefaultLauncher(IProject project) throws CoreException {
		ILauncher launcher= null;
		if ((project != null) && project.isOpen()) {
			String launcherID = project.getPersistentProperty(new QualifiedName(DebugPlugin.PLUGIN_ID, DEFAULT_LAUNCHER));
			if (launcherID != null) {
				launcher= getLauncher(launcherID);
			}
		}
		return launcher;
	}
		
	/**
	 * Returns the launcher with the given id, or <code>null</code>.
	 */
	public ILauncher getLauncher(String id) {
		ILauncher[] launchers= getLaunchers();
		for (int i= 0; i < launchers.length; i++) {
			if (launchers[i].getIdentifier().equals(id)) {
				return launchers[i];
			}
		}
		return null;
	}

	/**
	 * @see ILaunchManager
	 */
	public ILauncher[] getLaunchers() {
		return DebugPlugin.getDefault().getLaunchers();
	}
	
	/**
	 * @see ILaunchManager
	 */
	public ILauncher[] getLaunchers(String mode) {
		ILauncher[] launchers = getLaunchers();
		ArrayList list = new ArrayList();
		for (int i = 0; i < launchers.length; i++) {
			if (launchers[i].getModes().contains(mode)) {
				list.add(launchers[i]);
			}
		}
		return (ILauncher[])list.toArray(new ILauncher[list.size()]);
	}

	/**
	 * @see ILaunchManager
	 */
	public ILaunch[] getLaunches() {
		return (ILaunch[])fLaunches.toArray(new ILaunch[fLaunches.size()]);
	}

	/**
	 * @see ILaunchManager
	 */
	public IProcess[] getProcesses() {
		List allProcesses= new ArrayList(fLaunches.size());
		if (fLaunches.size() > 0) {
			Iterator e= fLaunches.iterator();
			while (e.hasNext()) {
				IProcess[] processes= ((ILaunch) e.next()).getProcesses();
				for (int i= 0; i < processes.length; i++) {
					allProcesses.add(processes[i]);
				}
			}
		}
		return (IProcess[])allProcesses.toArray(new IProcess[allProcesses.size()]);
	}

	/**
	 * @see ILaunchManager
	 */
	public void registerLaunch(ILaunch launch) {
		fLaunches.add(launch);
		fireUpdate(launch, REGISTERED);
	}
	
	/**
	 * @see ILaunchManager 
	 */
	public void removeLaunchListener(ILaunchListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * @see ILaunchManager
	 */
	public void setDefaultLauncher(IProject resource, ILauncher launcher) throws CoreException {
		String id = null;
		if (launcher != null) {
			id = launcher.getIdentifier();
		}
		resource.setPersistentProperty(new QualifiedName(DebugPlugin.PLUGIN_ID, DEFAULT_LAUNCHER), id);
	}

	/**
	 * Terminates/Disconnects any active debug targets/processes.
	 */
	public void shutdown() {
		ILaunch[] launches = getLaunches();
		for (int i= 0; i < launches.length; i++) {
			ILaunch launch= launches[i];
			try {
				launch.terminate();
			} catch (DebugException e) {
				DebugCoreUtils.logError(e);
			}
		}
	}	
}
