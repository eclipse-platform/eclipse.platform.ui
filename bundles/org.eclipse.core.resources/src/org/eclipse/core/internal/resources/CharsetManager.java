/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Manages user-defined encodings as preferences in the project content area.
 * 
 * @since 3.0
 */
public class CharsetManager implements IManager {
	protected final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi"); //$NON-NLS-1$

	/**
	 * This job implementation is used to allow the resource change listener
	 * to schedule operations that need to modify the workspace. 
	 */
	private class CharsetManagerJob extends Job {
		private List asyncChanges = new ArrayList();

		public CharsetManagerJob() {
			super(Policy.bind("resources.charsetUpdating")); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		public void addChanges(Set newChanges) {
			if (newChanges.isEmpty())
				return;
			synchronized (asyncChanges) {
				asyncChanges.addAll(newChanges);
				asyncChanges.notify();
			}
			schedule();
		}

		public IProject getNextChange() {
			synchronized (asyncChanges) {
				return asyncChanges.isEmpty() ? null : (IProject) asyncChanges.remove(asyncChanges.size() - 1);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_SETTING_CHARSET, Policy.bind("resources.updatingEncoding"), null); //$NON-NLS-1$			
			monitor = Policy.monitorFor(monitor);
			try {
				monitor.beginTask(Policy.bind("resources.charsetUpdating"), Policy.totalWork); //$NON-NLS-1$
				final ISchedulingRule rule = workspace.getRuleFactory().modifyRule(workspace.getRoot());
				try {
					workspace.prepareOperation(rule, monitor);
					workspace.beginOperation(true);
					IProject next;
					while ((next = getNextChange()) != null) {
						//just exit if the system is shutting down or has been shut down
						//it is too late to change the workspace at this point anyway
						if (systemBundle.getState() != Bundle.ACTIVE)
							return Status.OK_STATUS;
						try {
							if (next.isAccessible())
								getPreferences(next).flush();
						} catch (BackingStoreException e) {
							// we got an error saving					
							String detailMessage = Policy.bind("resources.savingEncoding"); //$NON-NLS-1$
							result.add(new ResourceStatus(IResourceStatus.FAILED_SETTING_CHARSET, next.getFullPath(), detailMessage, e));
						}
					}
					monitor.worked(Policy.opWork);
				} catch (OperationCanceledException e) {
					workspace.getWorkManager().operationCanceled();
					throw e;
				} finally {
					workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
				}
			} catch (CoreException ce) {
				return ce.getStatus();
			} finally {
				monitor.done();
			}
			return result; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
		 */
		public boolean shouldRun() {
			synchronized (asyncChanges) {
				return !asyncChanges.isEmpty();
			}
		}
	}

	class Listener implements IResourceChangeListener {

		private void processEntryChanges(IResourceDelta projectDelta, Set projectsToSave) {
			// check each resource with user-set encoding to see if it has
			// been moved/deleted
			boolean resourceChanges = false;
			IProject currentProject = (IProject) projectDelta.getResource();
			Preferences projectPrefs = getPreferences(currentProject);
			String[] affectedResources;
			try {
				affectedResources = projectPrefs.keys();
			} catch (BackingStoreException e) {
				// problems with the project scope... we gonna miss the changes (but will log)
				String message = Policy.bind("resources.readingEncoding"); //$NON-NLS-1$
				ResourcesPlugin.getPlugin().getLog().log(new ResourceStatus(IResourceStatus.FAILED_GETTING_CHARSET, currentProject.getFullPath(), message, e));
				return;
			}
			for (int i = 0; i < affectedResources.length; i++) {
				IResourceDelta memberDelta = projectDelta.findMember(new Path(affectedResources[i]));
				// no changes for the given resource
				if (memberDelta == null)
					continue;
				if (memberDelta.getKind() == IResourceDelta.REMOVED) {
					resourceChanges = true;
					// remove the setting for the original location - save its value though
					String currentValue = projectPrefs.get(affectedResources[i], null);
					projectPrefs.remove(affectedResources[i]);
					if ((memberDelta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
						// if moving, copy the setting for the new location
						IProject targetProject = workspace.getRoot().getProject(memberDelta.getMovedToPath().segment(0));
						Preferences targetPrefs = getPreferences(targetProject);
						targetPrefs.put(getKeyFor(memberDelta.getMovedToPath()), currentValue);
						if (targetProject != currentProject)
							projectsToSave.add(targetProject);
					}
				}
			}
			if (resourceChanges)
				projectsToSave.add(currentProject);
		}

		/**
		 * For any change to the encoding file or any resource with encoding
		 * set, just discard the cache for the corresponding project.
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (delta == null)
				return;
			IResourceDelta[] projectDeltas = delta.getAffectedChildren();
			// process each project in the delta
			Set projectsToSave = new HashSet();
			for (int i = 0; i < projectDeltas.length; i++)
				//nothing to do if a project has been added/removed/moved				
				if (projectDeltas[i].getKind() == IResourceDelta.CHANGED && (projectDeltas[i].getFlags() & IResourceDelta.OPEN) == 0)
					processEntryChanges(projectDeltas[i], projectsToSave);
			job.addChanges(projectsToSave);
		}
	}

	public static final String ENCODING_PREF_NODE = "encoding"; //$NON-NLS-1$		
	private static final String PROJECT_KEY = "<project>"; //$NON-NLS-1$
	CharsetManagerJob job;
	private IResourceChangeListener listener;
	private CharsetDeltaJob charsetListener;
	Workspace workspace;

	public CharsetManager(Workspace workspace) {
		this.workspace = workspace;
	}

	public String getCharsetFor(IPath resourcePath) {
		return getCharsetFor(resourcePath, false);
	}

	public String getCharsetFor(IPath resourcePath, boolean recurse) {
		Assert.isLegal(resourcePath.segmentCount() >= 1);
		IProject project = workspace.getRoot().getProject(resourcePath.segment(0));
		Preferences encodingSettings = getPreferences(project);
		return internalGetCharsetFor(resourcePath, encodingSettings, recurse);
	}

	private String internalGetCharsetFor(IPath resourcePath, Preferences encodingSettings, boolean recurse) {
		String charset = encodingSettings.get(getKeyFor(resourcePath), null);
		if (!recurse)
			return charset;
		while (charset == null && resourcePath.segmentCount() > 1) {
			resourcePath = resourcePath.removeLastSegments(1);
			charset = encodingSettings.get(getKeyFor(resourcePath), null);
		}
		// ensure we default to the workspace encoding if none is found
		return charset == null ? ResourcesPlugin.getEncoding() : charset;
	}

	String getKeyFor(IPath resourcePath) {
		return resourcePath.segmentCount() > 1 ? resourcePath.removeFirstSegments(1).toString() : PROJECT_KEY;
	}

	Preferences getPreferences(IProject project) {
		return new ProjectScope(project).getNode(ResourcesPlugin.PI_RESOURCES).node(ENCODING_PREF_NODE);
	}

	public void setCharsetFor(IPath resourcePath, String newCharset) throws CoreException {
		// for the workspace root we just set a preference in the instance scope
		if (resourcePath.segmentCount() == 0) {
			org.eclipse.core.runtime.Preferences resourcesPreferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			if (newCharset != null)
				resourcesPreferences.setValue(ResourcesPlugin.PREF_ENCODING, newCharset);
			else
				resourcesPreferences.setToDefault(ResourcesPlugin.PREF_ENCODING);
			ResourcesPlugin.getPlugin().savePluginPreferences();
			return;
		}
		// for all other cases, we set a property in the corresponding project
		IProject project = workspace.getRoot().getProject(resourcePath.segment(0));
		Preferences encodingSettings = getPreferences(project);
		if (newCharset == null || newCharset.trim().length() == 0)
			encodingSettings.remove(getKeyFor(resourcePath));
		else
			encodingSettings.put(getKeyFor(resourcePath), newCharset);
		try {
			// disable the listener so we don't react to changes made by ourselves 
			charsetListener.setDisabled(true);
			// save changes
			encodingSettings.flush();
		} catch (BackingStoreException e) {
			String message = Policy.bind("resources.savingEncoding"); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_SETTING_CHARSET, project.getFullPath(), message, e);
		} finally {
			charsetListener.setDisabled(false);
		}

	}

	public void shutdown(IProgressMonitor monitor) {
		workspace.removeResourceChangeListener(listener);
		if (charsetListener != null)
			charsetListener.shutdown();
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		job = new CharsetManagerJob();
		listener = new Listener();
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		charsetListener = new CharsetDeltaJob(workspace);
		charsetListener.startup();
	}

	public void charsetPreferencesChanged(IProject project) {
		charsetListener.charsetPreferencesChanged(project);
	}
}