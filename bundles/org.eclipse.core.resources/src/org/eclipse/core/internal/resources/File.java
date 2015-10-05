/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Sergey Prigogin (Google) - [464072] Refresh on Access ignored during text search
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

/**
 * The standard implementation of {@link IFile}.
 */
public class File extends Resource implements IFile {

	protected File(IPath path, Workspace container) {
		super(path, container);
	}

	@Override
	public void appendContents(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = NLS.bind(Messages.resources_settingContents, getFullPath());
			monitor.beginTask(message, Policy.totalWork);
			Assert.isNotNull(content, "Content cannot be null."); //$NON-NLS-1$
			if (workspace.shouldValidate)
				workspace.validateSave(this);
			final ISchedulingRule rule = workspace.getRuleFactory().modifyRule(this);
			try {
				workspace.prepareOperation(rule, monitor);
				ResourceInfo info = getResourceInfo(false, false);
				checkAccessible(getFlags(info));
				workspace.beginOperation(true);
				IFileInfo fileInfo = getStore().fetchInfo();
				internalSetContents(content, fileInfo, updateFlags, true, Policy.subMonitorFor(monitor, Policy.opWork));
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				throw e;
			} finally {
				workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
			FileUtil.safeClose(content);
		}
	}

	@Override
	public void appendContents(InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		// funnel all operations to central method
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
		appendContents(content, updateFlags, monitor);
	}

	/**
	 * Changes this file to be a folder in the resource tree and returns
	 * the newly created folder.  All related
	 * properties are deleted.  It is assumed that on disk the resource is
	 * already a folder/directory so no action is taken to delete the disk
	 * contents.
	 * <p>
	 * <b>This method is for the exclusive use of the local resource manager</b>
	 */
	public IFolder changeToFolder() throws CoreException {
		getPropertyManager().deleteProperties(this, IResource.DEPTH_ZERO);
		IFolder result = workspace.getRoot().getFolder(path);
		if (isLinked()) {
			IPath location = getRawLocation();
			delete(IResource.NONE, null);
			result.createLink(location, IResource.ALLOW_MISSING_LOCAL, null);
		} else {
			workspace.deleteResource(this);
			workspace.createResource(result, false);
		}
		return result;
	}

	@Override
	public void create(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
		final boolean monitorNull = monitor == null;
		monitor = Policy.monitorFor(monitor);
		try {
			String message = monitorNull ? "" : NLS.bind(Messages.resources_creating, getFullPath()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);
			checkValidPath(path, FILE, true);
			final ISchedulingRule rule = workspace.getRuleFactory().createRule(this);
			try {
				workspace.prepareOperation(rule, monitor);
				checkDoesNotExist();
				Container parent = (Container) getParent();
				ResourceInfo info = parent.getResourceInfo(false, false);
				parent.checkAccessible(getFlags(info));
				checkValidGroupContainer(parent, false, false);

				workspace.beginOperation(true);
				IFileStore store = getStore();
				IFileInfo localInfo = store.fetchInfo();
				if (BitMask.isSet(updateFlags, IResource.FORCE)) {
					if (!Workspace.caseSensitive) {
						if (localInfo.exists()) {
							String name = getLocalManager().getLocalName(store);
							if (name == null || localInfo.getName().equals(name)) {
								delete(true, null);
							} else {
								// The file system is not case sensitive and there is already a file
								// under this location.
								message = NLS.bind(Messages.resources_existsLocalDifferentCase, new Path(store.toString()).removeLastSegments(1).append(name).toOSString());
								throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), message, null);
							}
						}
					}
				} else {
					if (localInfo.exists()) {
						//return an appropriate error message for case variant collisions
						if (!Workspace.caseSensitive) {
							String name = getLocalManager().getLocalName(store);
							if (name != null && !localInfo.getName().equals(name)) {
								message = NLS.bind(Messages.resources_existsLocalDifferentCase, new Path(store.toString()).removeLastSegments(1).append(name).toOSString());
								throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), message, null);
							}
						}
						message = NLS.bind(Messages.resources_fileExists, store.toString());
						throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), message, null);
					}
				}
				monitor.worked(Policy.opWork * 40 / 100);

				info = workspace.createResource(this, updateFlags);
				boolean local = content != null;
				if (local) {
					try {
						internalSetContents(content, localInfo, updateFlags, false, Policy.subMonitorFor(monitor, Policy.opWork * 60 / 100));
					} catch (CoreException e) {
						// a problem happened creating the file on disk, so delete from the workspace and disk
						workspace.deleteResource(this);
						store.delete(EFS.NONE, null);
						throw e; // rethrow
					} catch (OperationCanceledException e) {
						// the operation of setting contents has been canceled, so delete the file from the workspace and disk
						workspace.deleteResource(this);
						store.delete(EFS.NONE, null);
						throw e;
					}
				}
				internalSetLocal(local, DEPTH_ZERO);
				if (!local)
					getResourceInfo(true, true).clearModificationStamp();
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				throw e;
			} finally {
				workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
			FileUtil.safeClose(content);
		}
	}

	@Override
	public void create(InputStream content, boolean force, IProgressMonitor monitor) throws CoreException {
		// funnel all operations to central method
		create(content, (force ? IResource.FORCE : IResource.NONE), monitor);
	}

	@Override
	public String getCharset() throws CoreException {
		return getCharset(true);
	}

	@Override
	public String getCharset(boolean checkImplicit) throws CoreException {
		// non-existing resources default to parent's charset
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		if (!exists(flags, false))
			return checkImplicit ? workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true) : null;
		checkLocal(flags, DEPTH_ZERO);
		try {
			return internalGetCharset(checkImplicit, info);
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
				return checkImplicit ? workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true) : null;
			}
			throw e;
		}
	}

	@Override
	public String getCharsetFor(Reader contents) throws CoreException {
		String charset;
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		if (exists(flags, true))
			// the file exists, look for user setting
			if ((charset = workspace.getCharsetManager().getCharsetFor(getFullPath(), false)) != null)
				// if there is a file-specific user setting, use it
				return charset;
		// tries to obtain a description from the contents provided
		IContentDescription description;
		try {
			// TODO need to take project specific settings into account
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			description = contentTypeManager.getDescriptionFor(contents, getName(), new QualifiedName[] {IContentDescription.CHARSET});
		} catch (IOException e) {
			String message = NLS.bind(Messages.resources_errorContentDescription, getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, getFullPath(), message, e);
		}
		if (description != null)
			if ((charset = description.getCharset()) != null)
				// the description contained charset info, we are done
				return charset;
		// could not find out the encoding based on the contents... default to parent's
		return workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true);
	}

	private String internalGetCharset(boolean checkImplicit, ResourceInfo info) throws CoreException {
		// if there is a file-specific user setting, use it
		String charset = workspace.getCharsetManager().getCharsetFor(getFullPath(), false);
		if (charset != null || !checkImplicit)
			return charset;
		// tries to obtain a description for the file contents
		IContentDescription description = workspace.getContentDescriptionManager().getDescriptionFor(this, info, true);
		if (description != null) {
			String contentCharset = description.getCharset();
			if (contentCharset != null)
				return contentCharset;
		}
		// could not find out the encoding based on the contents... default to parent's
		return workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true);
	}

	@Override
	public IContentDescription getContentDescription() throws CoreException {
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		checkAccessible(flags);
		checkLocal(flags, DEPTH_ZERO);
		boolean isSynchronized = isSynchronized(IResource.DEPTH_ZERO);
		// Throw an exception if out-of-sync and not auto-refresh enabled
		if (!isSynchronized && !getLocalManager().isLightweightAutoRefreshEnabled()) {
			String message = NLS.bind(Messages.localstore_resourceIsOutOfSync, getFullPath());
			throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, getFullPath(), message, null);
		}
		return workspace.getContentDescriptionManager().getDescriptionFor(this, info, isSynchronized);
	}

	@Override
	public InputStream getContents() throws CoreException {
		return getContents(getLocalManager().isLightweightAutoRefreshEnabled());
	}

	@Override
	public InputStream getContents(boolean force) throws CoreException {
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		checkAccessible(flags);
		checkLocal(flags, DEPTH_ZERO);
		return getLocalManager().read(this, force, null);
	}

	@Deprecated
	@Override
	public int getEncoding() throws CoreException {
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		checkAccessible(flags);
		checkLocal(flags, DEPTH_ZERO);
		return getLocalManager().getEncoding(this);
	}

	@Override
	public IFileState[] getHistory(IProgressMonitor monitor) {
		return getLocalManager().getHistoryStore().getStates(getFullPath(), monitor);
	}

	@Override
	public int getType() {
		return FILE;
	}

	protected void internalSetContents(InputStream content, IFileInfo fileInfo, int updateFlags, boolean append, IProgressMonitor monitor) throws CoreException {
		if (content == null)
			content = new ByteArrayInputStream(new byte[0]);
		getLocalManager().write(this, content, fileInfo, updateFlags, append, monitor);
		updateMetadataFiles();
		workspace.getAliasManager().updateAliases(this, getStore(), IResource.DEPTH_ZERO, monitor);
	}

	/**
	 * Optimized refreshLocal for files.  This implementation does not block the workspace
	 * for the common case where the file exists both locally and on the file system, and
	 * is in sync.  For all other cases, it defers to the super implementation.
	 */
	@Override
	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
		if (!getLocalManager().fastIsSynchronized(this))
			super.refreshLocal(IResource.DEPTH_ZERO, monitor);
	}

	@Override
	public void setContents(IFileState content, int updateFlags, IProgressMonitor monitor) throws CoreException {
		setContents(content.getContents(), updateFlags, monitor);
	}

	@Override
	public void setContents(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = NLS.bind(Messages.resources_settingContents, getFullPath());
			monitor.beginTask(message, Policy.totalWork);
			if (workspace.shouldValidate)
				workspace.validateSave(this);
			final ISchedulingRule rule = workspace.getRuleFactory().modifyRule(this);
			try {
				workspace.prepareOperation(rule, monitor);
				ResourceInfo info = getResourceInfo(false, false);
				checkAccessible(getFlags(info));
				workspace.beginOperation(true);
				IFileInfo fileInfo = getStore().fetchInfo();
				internalSetContents(content, fileInfo, updateFlags, false, Policy.subMonitorFor(monitor, Policy.opWork));
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				throw e;
			} finally {
				workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
			FileUtil.safeClose(content);
		}
	}

	@Override
	public long setLocalTimeStamp(long value) throws CoreException {
		//override to handle changing timestamp on project description file
		long result = super.setLocalTimeStamp(value);
		if (path.segmentCount() == 2 && path.segment(1).equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
			//handle concurrent project deletion
			ResourceInfo projectInfo = ((Project) getProject()).getResourceInfo(false, false);
			if (projectInfo != null)
				getLocalManager().updateLocalSync(projectInfo, result);
		}
		return result;
	}

	/**
	 * Treat the file specially if it represents a metadata file, which includes:
	 * - project description file (.project)
	 * - project preferences files (*.prefs)
	 *
	 * This method is called whenever it is discovered that a file has
	 * been modified (added, removed, or changed).
	 */
	public void updateMetadataFiles() throws CoreException {
		int count = path.segmentCount();
		String name = path.segment(1);
		// is this a project description file?
		if (count == 2 && name.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
			Project project = (Project) getProject();
			project.updateDescription();
			// Discard stale project natures on ProjectInfo
			ProjectInfo projectInfo = (ProjectInfo) project.getResourceInfo(false, true);
			projectInfo.discardNatures();
			return;
		}
		// check to see if we are in the .settings directory
		if (count == 3 && EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME.equals(name)) {
			ProjectPreferences.updatePreferences(this);
			return;
		}
	}

	@Deprecated
	@Override
	public void setCharset(String newCharset) throws CoreException {
		ResourceInfo info = getResourceInfo(false, false);
		checkAccessible(getFlags(info));
		workspace.getCharsetManager().setCharsetFor(getFullPath(), newCharset);
	}

	@Override
	public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = NLS.bind(Messages.resources_settingCharset, getFullPath());
			monitor.beginTask(message, Policy.totalWork);
			// need to get the project as a scheduling rule because we might be creating a new folder/file to
			// hold the project settings
			final ISchedulingRule rule = workspace.getRuleFactory().charsetRule(this);
			try {
				workspace.prepareOperation(rule, monitor);
				ResourceInfo info = getResourceInfo(false, false);
				checkAccessible(getFlags(info));
				workspace.beginOperation(true);
				workspace.getCharsetManager().setCharsetFor(getFullPath(), newCharset);
				info = getResourceInfo(false, true);
				info.incrementCharsetGenerationCount();
				monitor.worked(Policy.opWork);
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				throw e;
			} finally {
				workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public void setContents(InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		// funnel all operations to central method
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
		setContents(content, updateFlags, monitor);
	}

	@Override
	public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		// funnel all operations to central method
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
		setContents(source.getContents(), updateFlags, monitor);
	}
}
