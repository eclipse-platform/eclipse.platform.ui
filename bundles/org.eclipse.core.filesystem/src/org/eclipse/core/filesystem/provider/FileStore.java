/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.filesystem.provider;

import java.io.*;
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.internal.filesystem.Messages;
import org.eclipse.core.internal.filesystem.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * The abstract superclass of all {@link IFileStore} implementations.  All
 * file stores must subclass this base class, implementing all abstract
 * methods according to their specification in the {@link IFileStore} API.
 * 
 * Mention these should be "handle-like" lightweight objects.
 */
public abstract class FileStore extends PlatformObject implements IFileStoreConstants, IFileStore {
	/**
	 * Singleton buffer created to avoid buffer creations in the
	 * transferStreams method.  Used as an optimization, based on the assumption
	 * that multiple writes won't happen in a given instance of FileStore.
	 */
	private static final byte[] buffer = new byte[8192];

	/**
	 * A file info array of size zero that can be used as a return value for methods
	 * that return IFileInfo[] to avoid creating garbage objects.
	 */
	protected static final IFileInfo[] EMPTY_FILE_INFO_ARRAY = new IFileInfo[0];

	/**
	 * A string array of size zero that can be used as a return value for methods
	 * that return String[] to avoid creating garbage objects.
	 */
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * Closes a stream and ignores any resulting exception.
	 */
	private static void safeClose(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception.
	 */
	private static void safeClose(OutputStream out) {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Transfers the contents of an input stream to an output stream, using a large
	 * buffer.
	 * 
	 * @param source The input stream to transfer
	 * @param destination The destination stream of the transfer
	 * @param path A path representing the data being transferred for use in error
	 * messages.
	 * @param monitor A progress monitor.  The monitor is assumed to have
	 * already done beginWork with one unit of work allocated per buffer load
	 * of contents to be transferred.
	 * @throws CoreException
	 */
	private static final void transferStreams(InputStream source, OutputStream destination, String path, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			/*
			 * Note: although synchronizing on the buffer is thread-safe,
			 * it may result in slower performance in the future if we want 
			 * to allow concurrent writes.
			 */
			synchronized (buffer) {
				while (true) {
					int bytesRead = -1;
					try {
						bytesRead = source.read(buffer);
					} catch (IOException e) {
						String msg = NLS.bind(Messages.failedReadDuringWrite, path);
						Policy.error(FAILED_READ_LOCAL, msg, e);
					}
					if (bytesRead == -1)
						break;
					try {
						destination.write(buffer, 0, bytesRead);
					} catch (IOException e) {
						String msg = NLS.bind(Messages.couldNotWrite, path);
						Policy.error(FAILED_WRITE_LOCAL, msg, e);
					}
					monitor.worked(1);
				}
			}
		} finally {
			safeClose(source);
			safeClose(destination);
		}
	}

	/**
	 * The default implementation of {@link IFileStore#childInfos(int, IProgressMonitor)}.
	 * Subclasses may override.
	 */
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) {
		IFileStore[] childStores = childStores(options, monitor);
		IFileInfo[] childInfos = new IFileInfo[childStores.length];
		for (int i = 0; i < childStores.length; i++) {
			childInfos[i] = childStores[i].fetchInfo();
		}
		return childInfos;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract String[] childNames(int options, IProgressMonitor monitor);

	/**
	 * The default implementation of {@link IFileStore#childStores(int, IProgressMonitor)}.
	 * Subclasses may override.
	 */
	public IFileStore[] childStores(int options, IProgressMonitor monitor) {
		String[] children = childNames(options, monitor);
		IFileStore[] wrapped = new IFileStore[children.length];
		for (int i = 0; i < wrapped.length; i++)
			wrapped[i] = getChild(children[i]);
		return wrapped;
	}

	/**
	 * The default implementation of {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.
	 * Subclasses may override.
	 */
	public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		Policy.checkCanceled(monitor);
		final IFileInfo sourceInfo = fetchInfo(NONE, null);
		if (sourceInfo.isDirectory())
			copyDirectory(sourceInfo, destination, options, monitor);
		else
			copyFile(sourceInfo, destination, options, monitor);
	}

	/**
	 * Recursively copies a directory as specified by 
	 * {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.
	 * 
	 * @param sourceInfo The current file information for the source of the move
	 * @param destination The destination of the copy.
	 * @param options bit-wise or of option flag constants (
	 * {@link IFileStoreConstants#OVERWRITE} or {@link IFileStoreConstants#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The <code>OVERWRITE</code> flag is not specified and a file of the
	 * same name already exists at the copy destination.</li>
	 * </ul>
	 */
	protected void copyDirectory(IFileInfo sourceInfo, IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		try {
			IFileStore[] children = childStores(NONE, null);
			monitor.beginTask(NLS.bind(Messages.copying, toString()), children.length);
			// create directory
			destination.mkdir(NONE, Policy.subMonitorFor(monitor, 0));

			if ((options & SHALLOW) != 0)
				return;
			// copy children
			for (int i = 0; i < children.length; i++)
				children[i].copy(destination.getChild(children[i].getName()), options, Policy.subMonitorFor(monitor, 1));
		} finally {
			monitor.done();
		}
	}

	/**
	 * Copies a file as specified by 
	 * {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.

	 * @param sourceInfo The current file information for the source of the move
	 * @param destination The destination of the copy.
	 * @param options bit-wise or of option flag constants (
	 * {@link IFileStoreConstants#OVERWRITE} or {@link IFileStoreConstants#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The <code>OVERWRITE</code> flag is not specified and a file of the
	 * same name already exists at the copy destination.</li>
	 * </ul>
	 */
	protected void copyFile(IFileInfo sourceInfo, IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		try {
			if ((options & OVERWRITE) == 0 && destination.fetchInfo().exists())
				Policy.error(IFileStoreConstants.EXISTS_LOCAL, NLS.bind(Messages.fileExists, destination));
			long length = sourceInfo.getLength();
			int totalWork;
			if (length == -1)
				totalWork = IProgressMonitor.UNKNOWN;
			else
				totalWork = 1 + (int) (length / buffer.length);
			String sourcePath = toString();
			monitor.beginTask(NLS.bind(Messages.copying, sourcePath), totalWork);
			InputStream in = null;
			OutputStream out = null;
			try {
				destination.getParent().mkdir(NONE, null);
				in = openInputStream(NONE, Policy.subMonitorFor(monitor, 0));
				out = destination.openOutputStream(NONE, Policy.subMonitorFor(monitor, 0));
				transferStreams(in, out, sourcePath, monitor);
				transferAttributes(sourceInfo, destination);
			} catch (CoreException e) {
				safeClose(in);
				safeClose(out);
				//if we failed to write, try to cleanup the half written file
				if (!destination.fetchInfo(0, null).exists())
					destination.delete(NONE, null);
				throw e;
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract void delete(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#fetchInfo()
	 */
	public IFileInfo fetchInfo() {
		return fetchInfo(IFileStoreConstants.NONE, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract IFileInfo fetchInfo(int options, IProgressMonitor monitor);

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getChild(java.lang.String)
	 */
	public abstract IFileStore getChild(String name);

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getName()
	 */
	public abstract String getName();

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getParent()
	 */
	public abstract IFileStore getParent();

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#isParentOf(org.eclipse.core.filesystem.IFileStore)
	 */
	public boolean isParentOf(IFileStore other) {
		while (true) {
			other = other.getParent();
			if (other == null)
				return false;
			if (this.equals(other))
				return true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#move(org.eclipse.core.filesystem.IFileStore, int, org.eclipse.core.runtime.IProgressMonitor)
	 * Default implementation uses other primitives. Subclasses can override
	 * to provide an implementation that is optimized for a particular file system.
	 */
	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(NLS.bind(Messages.moving, destination.toString()), 100);
			copy(destination, options, Policy.subMonitorFor(monitor, 70));
			delete(NONE, Policy.subMonitorFor(monitor, 30));
		} catch (CoreException e) {
			//throw new error to indicate failure occurred during a move
			String message = NLS.bind(Messages.couldNotMove, toString());
			Policy.error(FAILED_WRITE_LOCAL, message, e);
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#openInputStream(int, IProgressMonitor)
	 */
	public abstract InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#openOutputStream(int, IProgressMonitor)
	 */
	public abstract OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#setFileInfo(org.eclipse.core.filesystem.IFileInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Default implementation of IFileStore.#toString(). This default implementation
	 * returns a string equal to the one returned by #toURI().toString(). Subclasses
	 * may override to provide a more specific string representation of this store.
	 */
	public String toString() {
		return toURI().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#toURI()
	 */
	public abstract URI toURI();

	private void transferAttributes(IFileInfo sourceInfo, IFileStore destination) throws CoreException {
		int options = IFileStoreConstants.SET_ATTRIBUTES | IFileStoreConstants.SET_LAST_MODIFIED;
		destination.putInfo(sourceInfo, options, null);
	}
}
