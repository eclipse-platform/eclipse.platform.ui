/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.filesystem.local;

import java.io.*;
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.internal.filesystem.Messages;
import org.eclipse.core.internal.filesystem.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * File system implementation based on storage of files in the local
 * operating system's file system.
 */
public class LocalFile extends FileStore {
	/**
	 * The java.io.File that this store represents.
	 */
	protected final File file;
	/**
	 * The absolute file system path of the file represented by this store.
	 */
	protected final String filePath;

	private static int attributes(File aFile) {
		if (!aFile.exists() || aFile.canWrite())
			return NONE;
		return ATTRIBUTE_READ_ONLY;
	}

	public LocalFile(File file) {
		this.file = file;
		this.filePath = file.getAbsolutePath();
	}

	/**
	 * This method is called after a failure to modify a file or directory.
	 * Check to see if the parent is read-only and if so then
	 * throw an exception with a more specific message and error code.
	 * 
	 * @param target The file that we failed to modify
	 * @param exception The low level exception that occurred, or <code>null</code>
	 * @throws CoreException A more specific exception if the parent is read-only
	 */
	private void checkReadOnlyParent(File target, Throwable exception) throws CoreException {
		File parent = target.getParentFile();
		if (parent != null && (attributes(parent) & ATTRIBUTE_READ_ONLY) != 0) {
			String message = NLS.bind(Messages.readOnlyParent, target.getAbsolutePath());
			Policy.error(ERROR_PARENT_READ_ONLY, message, exception);
		}
	}

	public String[] childNames(int options, IProgressMonitor monitor) {
		String[] names = file.list();
		return (names == null ? EMPTY_STRING_ARRAY : names);
	}

	public void copy(IFileStore destFile, int options, IProgressMonitor monitor) throws CoreException {
		if (destFile instanceof LocalFile) {
			File source = file;
			File destination = ((LocalFile) destFile).file;
			//handle case variants on a case-insensitive OS, or copying between
			//two equivalent files in an environment that supports symbolic links.
			//in these nothing needs to be copied (and doing so would likely lose data)
			try {
				if (source.getCanonicalFile().equals(destination.getCanonicalFile())) {
					//nothing to do
					return;
				}
			} catch (IOException e) {
				String message = NLS.bind(Messages.couldNotRead, source.getAbsolutePath());
				Policy.error(ERROR_READ, message, e);
			}
		}
		//fall through to super implementation
		super.copy(destFile, options, monitor);
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		//TODO add progress
		monitor = Policy.monitorFor(monitor);
		String message = Messages.deleteProblem;
		MultiStatus result = new MultiStatus(Policy.PI_FILE_SYSTEM, ERROR_DELETE, message, null);
		internalDelete(file, filePath, result);
		if (!result.isOK())
			throw new CoreException(result);
	}

	public boolean equals(Object obj) {
		if (obj instanceof LocalFile)
			return file.equals(((LocalFile) obj).file);
		return false;
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) {
		if (LocalFileNatives.usingNatives()) {
			IFileInfo info = LocalFileNatives.fetchFileInfo(filePath);
			//natives don't set the file name on all platforms
			if (info.getName().length() == 0)
				info.setName(file.getName());
			return info;
		}
		//in-lined non-native implementation
		IFileInfo info = FileSystemCore.createFileInfo();
		info.setName(file.getName());
		final long lastModified = file.lastModified();
		info.setLastModified(lastModified);
		info.setExists(lastModified > 0);
		info.setLength(file.length());
		info.setAttribute(ATTRIBUTE_DIRECTORY, file.isDirectory());
		info.setAttribute(ATTRIBUTE_READ_ONLY, file.exists() && !file.canWrite());
		return info;
	}

	public IFileStore getChild(String name) {
		return new LocalFile(new File(file, name));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getFileSystem()
	 */
	public IFileSystem getFileSystem() {
		return LocalFileSystem.getInstance();
	}

	public String getName() {
		return file.getName();
	}

	public IFileStore getParent() {
		File parent = file.getParentFile();
		return parent == null ? null : new LocalFile(parent);
	}

	public int hashCode() {
		return file.hashCode();
	}

	/**
	 * Deletes the given file recursively, adding failure info to
	 * the provided status object.  The filePath is passed as a parameter
	 * to optimize java.io.File object creation.
	 */
	private boolean internalDelete(File target, String pathToDelete, MultiStatus status) {
		boolean failedRecursive = false;
		if (target.isDirectory()) {
			String[] list = target.list();
			if (list == null)
				list = EMPTY_STRING_ARRAY;
			int parentLength = pathToDelete.length();
			for (int i = 0, imax = list.length; i < imax; i++) {
				//optimized creation of child path object
				StringBuffer childBuffer = new StringBuffer(parentLength + list[i].length() + 1);
				childBuffer.append(pathToDelete);
				childBuffer.append(File.separatorChar);
				childBuffer.append(list[i]);
				String childName = childBuffer.toString();
				// try best effort on all children so put logical OR at end
				failedRecursive = !internalDelete(new java.io.File(childName), childName, status) || failedRecursive;
			}
		}
		boolean failedThis = false;
		try {
			// don't try to delete the root if one of the children failed
			if (!failedRecursive && target.exists())
				failedThis = !target.delete();
		} catch (Exception e) {
			// we caught a runtime exception so log it
			String message = NLS.bind(Messages.couldnotDelete, target.getAbsolutePath());
			status.add(new Status(IStatus.ERROR, Policy.PI_FILE_SYSTEM, ERROR_DELETE, message, e));
			return false;
		}
		if (failedThis) {
			String message = null;
			if (fetchInfo().isReadOnly())
				message = NLS.bind(Messages.couldnotDeleteReadOnly, target.getAbsolutePath());
			else
				message = NLS.bind(Messages.couldnotDelete, target.getAbsolutePath());
			status.add(new Status(IStatus.ERROR, Policy.PI_FILE_SYSTEM, ERROR_DELETE, message, null));
		}
		return !(failedRecursive || failedThis);
	}

	public boolean isParentOf(IFileStore other) {
		if (!(other instanceof LocalFile))
			return false;
		String thisPath = filePath;
		String thatPath = ((LocalFile) other).filePath;
		int thisLength = thisPath.length();
		int thatLength = thatPath.length();
		//if equal then not a parent
		if (thisLength >= thatLength)
			return false;
		if (thatPath.indexOf(thisPath) != 0)
			return false;
		//The common portion must end with a separator character for this to be a parent of that
		return thisPath.charAt(thisLength - 1) == File.separatorChar || thatPath.charAt(thisLength) == File.separatorChar;
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		boolean shallow = (options & SHALLOW) != 0;
		//must be a directory
		if (shallow)
			file.mkdir();
		else
			file.mkdirs();
		if (!file.isDirectory()) {
			checkReadOnlyParent(file, null);
			String message = NLS.bind(Messages.couldNotCreateFolder, filePath);
			Policy.error(ERROR_WRITE, message);
		}
		return this;
	}

	public void move(IFileStore destFile, int options, IProgressMonitor monitor) throws CoreException {
		if (!(destFile instanceof LocalFile)) {
			super.move(destFile, options, monitor);
			return;
		}
		File source = file;
		File destination = ((LocalFile) destFile).file;
		boolean overwrite = (options & OVERWRITE) != 0;
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(NLS.bind(Messages.moving, source.getAbsolutePath()), 10);
			//this flag captures case renaming on a case-insensitive OS, or moving
			//two equivalent files in an environment that supports symbolic links.
			//in these cases we NEVER want to delete anything
			boolean sourceEqualsDest = false;
			try {
				sourceEqualsDest = source.getCanonicalFile().equals(destination.getCanonicalFile());
			} catch (IOException e) {
				String message = NLS.bind(Messages.couldNotMove, source.getAbsolutePath());
				Policy.error(ERROR_WRITE, message, e);
			}
			if (!sourceEqualsDest && !overwrite && destination.exists()) {
				String message = NLS.bind(Messages.fileExists, destination.getAbsolutePath());
				Policy.error(ERROR_EXISTS, message);
			}
			if (source.renameTo(destination)) {
				// double-check to ensure we really did move
				// since java.io.File#renameTo sometimes lies
				if (!sourceEqualsDest && source.exists()) {
					// XXX: document when this occurs
					if (destination.exists()) {
						// couldn't delete the source so remove the destination and throw an error
						// XXX: if we fail deleting the destination, the destination (root) may still exist
						new LocalFile(destination).delete(NONE, null);
						String message = NLS.bind(Messages.couldnotDelete, source.getAbsolutePath());
						Policy.error(ERROR_DELETE, message);
					}
					// source exists but destination doesn't so try to copy below
				} else {
					if (!destination.exists()) {
						// neither the source nor the destination exist. this is REALLY bad
						String message = NLS.bind(Messages.failedMove, source.getAbsolutePath(), destination.getAbsolutePath());
						Policy.error(ERROR_WRITE, message);
					}
					//the move was successful
					monitor.worked(10);
					return;
				}
			}
			// for some reason renameTo didn't work
			if (sourceEqualsDest) {
				String message = NLS.bind(Messages.couldNotMove, source.getAbsolutePath());
				Policy.error(ERROR_WRITE, message, null);
			}
			// fall back to default implementation
			super.move(destFile, options, Policy.subMonitorFor(monitor, 10));
		} finally {
			monitor.done();
		}
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(null, 1);
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			String message;
			if (!file.exists())
				message = NLS.bind(Messages.fileNotFound, filePath);
			else if (file.isDirectory())
				message = NLS.bind(Messages.notAFile, filePath);
			else
				message = NLS.bind(Messages.couldNotRead, filePath);
			Policy.error(ERROR_READ, message, e);
			return null;
		} finally {
			monitor.done();
		}
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(null, 1);
			return new FileOutputStream(file, (options & APPEND) != 0);
		} catch (FileNotFoundException e) {
			checkReadOnlyParent(file, e);
			String message;
			String path = filePath;
			if (file.isDirectory())
				message = NLS.bind(Messages.notAFile, path);
			else
				message = NLS.bind(Messages.couldNotWrite, path);
			Policy.error(ERROR_WRITE, message, e);
			return null;
		} finally {
			monitor.done();
		}
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		if ((options & SET_ATTRIBUTES) != 0) {
			if (LocalFileNatives.usingNatives()) {
				LocalFileNatives.setFileInfo(filePath, info, options);
			} else {
				//non-native implementation
				if (info.isReadOnly())
					file.setReadOnly();
			}
		}
		//native does not currently set last modified
		if ((options & SET_LAST_MODIFIED) != 0)
			file.setLastModified(info.getLastModified());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#toString()
	 */
	public String toString() {
		return file.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#toURI()
	 */
	public URI toURI() {
		return file.toURI();
	}
}