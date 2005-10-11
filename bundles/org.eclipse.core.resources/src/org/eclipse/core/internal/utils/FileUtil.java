/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * Static utility methods for manipulating Files and URIs.
 */
public class FileUtil {
	/**
	 * Singleton buffer created to prevent buffer creations in the
	 * transferStreams method.  Used as an optimization, based on the assumption
	 * that multiple writes won't happen in a given instance of FileStore.
	 */
	private static final byte[] buffer = new byte[8192];

	/**
	 * Converts a ResourceAttributes object into an IFileInfo object.
	 * @param attributes The resource attributes
	 * @return The file info
	 */
	public static IFileInfo attributesToFileInfo(ResourceAttributes attributes) {
		IFileInfo fileInfo = EFS.createFileInfo();
		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, attributes.isReadOnly());
		fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, attributes.isExecutable());
		fileInfo.setAttribute(EFS.ATTRIBUTE_ARCHIVE, attributes.isArchive());
		fileInfo.setAttribute(EFS.ATTRIBUTE_HIDDEN, attributes.isHidden());
		return fileInfo;
	}

	/**
	 * Converts an IFileInfo object into a ResourceAttributes object.
	 * @param fileInfo The file info
	 * @return The resource attributes
	 */
	public static ResourceAttributes fileInfoToAttributes(IFileInfo fileInfo) {
		ResourceAttributes attributes = new ResourceAttributes();
		attributes.setReadOnly(fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		attributes.setArchive(fileInfo.getAttribute(EFS.ATTRIBUTE_ARCHIVE));
		attributes.setExecutable(fileInfo.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));
		attributes.setHidden(fileInfo.getAttribute(EFS.ATTRIBUTE_HIDDEN));
		return attributes;
	}

	/**
	 * Closes a stream and ignores any resulting exception. This is useful
	 * when doing stream cleanup in a finally block where secondary exceptions
	 * are not worth logging.
	 */
	public static void safeClose(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception. This is useful
	 * when doing stream cleanup in a finally block where secondary exceptions
	 * are not worth logging.
	 */
	public static void safeClose(OutputStream out) {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Converts a path to a URI
	 */
	public static URI toURI(IPath path) {
		if (path.isAbsolute()) {
			String filePath = path.toFile().getAbsolutePath();
			if (File.separatorChar != '/')
				filePath = filePath.replace(File.separatorChar, '/');
			final int length = filePath.length();
			StringBuffer pathBuf = new StringBuffer(length + 1);
			//There must be a leading slash in a hierarchical URI
			if (length > 0 && (filePath.charAt(0) != '/'))
				pathBuf.append('/');
			//additional double-slash for UNC paths to distinguish from host separator
			if (path.isUNC())
				pathBuf.append('/').append('/');
			pathBuf.append(filePath);
			try {
				return new URI(EFS.SCHEME_FILE, null, pathBuf.toString(), null);
			} catch (URISyntaxException e) {
				IllegalArgumentException iae = new IllegalArgumentException();
				iae.initCause(e);
				throw iae;
			}
		}
		return URI.create(path.toString());
	}

	public static final void transferStreams(InputStream source, OutputStream destination, String path, IProgressMonitor monitor) throws CoreException {
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
						String msg = NLS.bind(Messages.localstore_failedReadDuringWrite, path);
						throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, new Path(path), msg, e);
					}
					if (bytesRead == -1)
						break;
					try {
						destination.write(buffer, 0, bytesRead);
					} catch (IOException e) {
						String msg = NLS.bind(Messages.localstore_couldNotWrite, path);
						throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, new Path(path), msg, e);
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
	 * Not intended for instantiation.
	 */
	private FileUtil() {
		super();
	}
}