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

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class CharsetManager implements IManager {
	public static final IPath ENCODING_FILE = new Path(".encoding"); //$NON-NLS-1$
	private Workspace workspace;
	private IResourceChangeListener listener;
	CharsetManagerJob job;
	final boolean[] isSaving = new boolean[1];
	public CharsetManager(Workspace workspace) {
		this.workspace = workspace;
	}
	public void startup(IProgressMonitor monitor) throws CoreException {
		job = new CharsetManagerJob();
		listener = new Listener();
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}
	public void shutdown(IProgressMonitor monitor) {
		workspace.removeResourceChangeListener(listener);
	}
	public void setCharsetFor(IPath resourcePath, String newCharset) throws CoreException {
		setCharsetFor(resourcePath, newCharset, true);
	}
	public void setCharsetFor(IPath resourcePath, String newCharset, boolean save) throws CoreException {
		Assert.isLegal(resourcePath.segmentCount() >= 1);
		Project project;
		Map charsets;
		synchronized (this) {
			project = (Project) workspace.getRoot().findMember(resourcePath.segment(0));
			charsets = getSettings(project);
			if (newCharset == null || newCharset.trim().length() == 0)
				charsets.remove(resourcePath.removeFirstSegments(1));
			else
				charsets.put(resourcePath.removeFirstSegments(1), newCharset);
		}
		if (save)
			storeSettings(project, charsets);
	}
	public String getCharsetFor(IPath resourcePath) throws CoreException {
		Assert.isLegal(resourcePath.segmentCount() >= 1);
		synchronized (this) {
			Project project = (Project) workspace.getRoot().findMember(resourcePath.segment(0));
			Map charsets = getSettings(project);
			return charsets == null ? null : (String) charsets.get(resourcePath.removeFirstSegments(1));
		}
	}
	/*
	 * Retrieves the settings for the given project, reading them from disk if needed.
	 */
	Map getSettings(IProject project) throws CoreException {
		ProjectInfo info = (ProjectInfo) ((Project) project).getResourceInfo(false, false);
		Map charsets = info.getCharsets();
		if (charsets != null)
			return charsets;
		info.setCharsets(charsets = new HashMap());
		IFile charsetStore = project.getFile(ENCODING_FILE);
		if (!charsetStore.exists())
			return charsets;
		try {
			loadSettings(charsets, charsetStore.getContents());
		} catch (IOException e) {
			//TODO: should log a better message here
			String message = e.getMessage() == null ? "" : e.getMessage(); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
		return charsets;
	}
	/*
	 * Unmarshals settings from the given stream.  
	 */
	private void loadSettings(Map charsets, InputStream contents) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				int separator = line.indexOf(':');
				if (separator == -1 || separator == line.length() - 1)
					continue;
				IPath path = new Path(line.substring(0, separator));
				String charset = line.substring(separator + 1);
				charsets.put(path, charset);
			}
		} finally {
			try {
				reader.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
	/*
	 * Create a serializable representation for the settings.
	 */
	InputStream createContents(final Map charsets) {
		ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(200);
		PrintWriter writer = new PrintWriter(tmpOutput);
		for (Iterator iter = charsets.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			writer.print(entry.getKey());
			writer.print(':');
			writer.println((String) entry.getValue());
		}
		writer.close();
		return new ByteArrayInputStream(tmpOutput.toByteArray());
	}
	void storeSettings(Project project) throws CoreException {
		storeSettings(project, ((ProjectInfo) project.getResourceInfo(false, false)).getCharsets());
	}
	private void storeSettings(final Project project, final Map charsets) throws CoreException {
		synchronized (isSaving) {
			// only one storeSettings operation happens at a time
			while (isSaving[0])
				try {
					isSaving.wait();
				} catch (InterruptedException e) {
					return;
				}
			isSaving[0] = true;
		}
		try {
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					if (!project.exists())
						return;
					final IFile charsetStore = project.getFile(ENCODING_FILE);
					if (charsets == null || charsets.isEmpty()) {
						charsetStore.delete(false, false, null);
						return;
					}
					InputStream contents = createContents(charsets);
					if (charsetStore.exists())
						charsetStore.setContents(contents, false, false, null);
					else
						charsetStore.create(contents, false, null);
				}
			}, project, 0, null);
		} finally {
			synchronized (isSaving) {
				isSaving[0] = false;
				isSaving.notifyAll();
			}
		}
	}
	/*
	 * Forgets the in-memory state. This will cause the state to be fetched
	 * from the file system (when needed).
	 */
	synchronized void invalidateModel(IProject project) {
		ProjectInfo info = (ProjectInfo) ((Project) project).getResourceInfo(false, false);
		if (info != null)
			info.setCharsets(null);
	}
	class Listener implements IResourceChangeListener {
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
			List projectsToSave = new ArrayList();
			for (int i = 0; i < projectDeltas.length; i++) {
				//nothing to do if a project has been added/removed
				if (projectDeltas[i].getKind() != IResourceDelta.CHANGED)
					continue;
				Project affectedProject = (Project) projectDeltas[i].getResource();
				// has the encoding store changed? if so, discard cache and 
				// ignore any other changes
				synchronized (isSaving) {
					if (!isSaving[0]) {
						IResourceDelta encodingStoreChange = projectDeltas[i].findMember(ENCODING_FILE);
						if (encodingStoreChange != null) {
							// just invalidate the project's in-memory state
							invalidateModel(affectedProject);
							continue;
						}
					}
				}
				// ensures the project info has been populated from the file system
				Map charsets;
				try {
					charsets = getSettings(affectedProject);
				} catch (CoreException e) {
					String message = e.getMessage() == null ? "" : e.getMessage(); //$NON-NLS-1$
					IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, message, e);
					ResourcesPlugin.getPlugin().getLog().log(status);
					continue;
				}
				if (charsets == null || charsets.isEmpty())
					continue;
				// process any entry changes
				if (processEntryChanges(projectDeltas[i], charsets))
					// if any of the entries have changed, we need to save 
					projectsToSave.add(affectedProject);
			}
			job.addChanges(projectsToSave);
		}
		private boolean processEntryChanges(IResourceDelta projectDelta, Map charsets) {
			// check each resource with user-set encoding to see if it has
			// been moved/deleted
			boolean resourceChanges = false;
			for (Iterator iter = charsets.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				IPath resourcePath = (IPath) entry.getKey();
				// here we handle only folder/file settings
				if (resourcePath.isEmpty())
					continue;
				IResourceDelta memberDelta = projectDelta.findMember(resourcePath);
				// no changes for the given resource
				if (memberDelta == null)
					continue;
				if (memberDelta.getKind() == IResourceDelta.REMOVED) {
					iter.remove();
					if ((memberDelta.getFlags() & IResourceDelta.MOVED_TO) == 0)
						continue;
					try {
						setCharsetFor(memberDelta.getMovedToPath(), (String) entry.getValue(), false);
					} catch (CoreException e) {
						//TODO: should log a better message here
						String message = e.getMessage() == null ? "" : e.getMessage(); //$NON-NLS-1$
						IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, message, e);
						ResourcesPlugin.getPlugin().getLog().log(status);
					}
					resourceChanges = true;
				}
			}
			return resourceChanges;
		}
	}
	/**
	 * This job implementation is used to allow the resource change listener
	 * to schedule operations that need to modify the workspace. 
	 */
	private class CharsetManagerJob extends Job {
		private List asyncChanges = new ArrayList();
		public CharsetManagerJob() {
			super("Charset Updater"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}
		protected IStatus run(IProgressMonitor monitor) {
			Project next;
			while ((next = getNextChange()) != null)
				try {
					storeSettings(next);
				} catch (CoreException e) {
					// TODO should produce a better message here
					String message = e.getMessage() == null ? "" : e.getMessage(); //$NON-NLS-1$
					return new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, message, e);
				}
			return new Status(IStatus.OK, ResourcesPlugin.PI_RESOURCES, 0, "", null); //$NON-NLS-1$
		}
		public boolean shouldRun() {
			synchronized (asyncChanges) {
				return !asyncChanges.isEmpty();
			}
		}
		public Project getNextChange() {
			synchronized (asyncChanges) {
				return asyncChanges.isEmpty() ? null : (Project) asyncChanges.remove(asyncChanges.size() - 1);
			}
		}
		public void addChanges(List newChanges) {
			if (newChanges.isEmpty())
				return;
			synchronized (asyncChanges) {
				asyncChanges.addAll(newChanges);
				asyncChanges.notify();
			}
			schedule();
		}
	}
}