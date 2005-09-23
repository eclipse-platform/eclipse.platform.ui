/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Refactoring history participant which maintains project refactoring
 * histories.
 * 
 * @since 3.2
 */
public final class ProjectRefactoringHistoryParticipant implements IRefactoringHistoryParticipant {

	/** Workspace resource change listener */
	private final class WorkspaceChangeListener implements IResourceChangeListener {

		public void resourceChanged(final IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				final IResource resource= event.getResource();
				if (resource != null && resource.getType() == IResource.FILE) {
					final IFile file= (IFile) resource;
					if (file.getName().equals(PATH_HISTORY_FILE)) {
						// TODO: implement
					}
				}
			}
		}
	}

	/** The history file extension */
	private static final String EXTENSION_HISTORY_FILE= "history"; //$NON-NLS-1$

	/** The history file name */
	private static final String NAME_HISTORY_FILE= "refactorings"; //$NON-NLS-1$

	/** The history folder */
	private static final String NAME_HISTORY_FOLDER= ".refactorings"; //$NON-NLS-1$

	/** The history file path */
	private static final String PATH_HISTORY_FILE= NAME_HISTORY_FILE + "." + EXTENSION_HISTORY_FILE; //$NON-NLS-1$

	/** The resource listener, or <code>null</code> */
	private IResourceChangeListener fResourceListener= null;

	/**
	 * @inheritDoc
	 */
	public void connect() {
		if (fResourceListener == null) {
			fResourceListener= new WorkspaceChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener, IResourceChangeEvent.POST_CHANGE);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		if (fResourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fResourceListener= null;
		}
	}

	/**
	 * Returns the project history file for the specified project.
	 * <p>
	 * The file has to be manually closed after processing.
	 * </p>
	 * 
	 * @param project
	 *            the project
	 * @param mode
	 *            the file mode
	 * @return the project history file, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs
	 * @throws FileNotFoundException
	 *             if the history file cannot be found
	 */
	private RandomAccessFile getProjectHistoryFile(final IProject project, final String mode) throws CoreException, FileNotFoundException {
		Assert.isTrue(project.exists());
		Assert.isTrue(mode != null && !"".equals(mode)); //$NON-NLS-1$
		final IFolder folder= project.getFolder(NAME_HISTORY_FOLDER);
		if (!folder.exists())
			folder.create(true, true, null);
		if (folder.exists()) {
			final IFile file= folder.getFile(PATH_HISTORY_FILE);
			if (!file.exists())
				file.create(new ByteArrayInputStream(new byte[] {}), true, null);
			if (file.exists()) {
				final String path= file.getLocation().toOSString();
				return new RandomAccessFile(path, mode);
			}
		}
		return null;
	}

	/**
	 * Should a project refactoring history be maintained?
	 * 
	 * @param project
	 *            the project
	 * @return <code>true</code> if a history should be maintained,
	 *         <code>false</code> otherwise
	 */
	private boolean isHistoryEnabled(final IProject project) {
		final IScopeContext[] contexts= new IScopeContext[] { new ProjectScope(project)};
		final String preference= Platform.getPreferencesService().getString(RefactoringCorePlugin.getPluginId(), RefactoringHistory.PREFERENCE_ENABLE_PROJECT_REFACTORING_HISTORY, Boolean.FALSE.toString(), contexts);
		if (preference != null)
			return Boolean.valueOf(preference).booleanValue();
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public void pop(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final String name= descriptor.getProject();
		if (name != null && !"".equals(name)) { //$NON-NLS-1$
			final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (project != null && project.exists() && isHistoryEnabled(project)) {
				try {
					final RandomAccessFile file= getProjectHistoryFile(project, "rw"); //$NON-NLS-1$
					if (file != null) {
						try {
							removeHead(file);
						} finally {
							try {
								file.close();
							} catch (IOException exception) {
								// Do nothing
							}
						}
					}
				} catch (FileNotFoundException exception) {
					RefactoringCorePlugin.log(exception);
				} catch (IOException exception) {
					RefactoringCorePlugin.log(exception);
				}
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void push(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final String name= descriptor.getProject();
		if (name != null && !"".equals(name)) { //$NON-NLS-1$
			final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (project != null && project.exists() && isHistoryEnabled(project)) {
				try {
					final RandomAccessFile file= getProjectHistoryFile(project, "rw"); //$NON-NLS-1$
					if (file != null) {
						try {
							replaceHead(file, descriptor);
						} finally {
							try {
								file.close();
							} catch (IOException exception) {
								// Do nothing
							}
						}
					}
				} catch (FileNotFoundException exception) {
					RefactoringCorePlugin.log(exception);
				} catch (IOException exception) {
					RefactoringCorePlugin.log(exception);
				}
			}
		}
	}

	/**
	 * Removes the head descriptor of the history.
	 * 
	 * @param file
	 *            the history file
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private void removeHead(final RandomAccessFile file) throws IOException {
		Assert.isNotNull(file);
		final long length= file.length();
		if (length > 8)
			file.seek(length - 8);
		final long size= file.readLong();
		file.setLength(Math.max(length - size, 0));
	}

	/**
	 * Appends the specified descriptor as new head to the history.
	 * 
	 * @param file
	 *            the history file
	 * @param descriptor
	 *            the refactoring descriptor
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private void replaceHead(final RandomAccessFile file, final RefactoringDescriptor descriptor) throws IOException {
		Assert.isNotNull(file);
		Assert.isNotNull(descriptor);
		final long oldLength= file.length();
		file.seek(oldLength);
		ProjectRefactoringHistorySerializer.writeDescriptor(file, descriptor);
		final long newLength= file.length();
		file.writeLong(newLength - oldLength + 8);
	}
}
