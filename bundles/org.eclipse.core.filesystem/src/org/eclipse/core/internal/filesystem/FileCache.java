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
package org.eclipse.core.internal.filesystem;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * An instance of this class represents a directory on disk where cached
 * files can be stored. Files in the cache expire on VM exit.
 */
public class FileCache {
	private static final String CACHE_DIR_NAME = "filecache";//$NON-NLS-1$

	/**
	 * This flag is true at the beginning of the session, and is set to 
	 * false after the very first cache instance is created.
	 */
	private static boolean startup = true;

	private File cacheDir;

	/**
	 * Creates a new file cache.
	 * 
	 * @throws CoreException If the file cache could not be created
	 */
	public FileCache() throws CoreException {
		IPath location = Platform.getStateLocation(Platform.getBundle(Policy.PI_FILE_SYSTEM));
		File cacheParent = new File(location.toFile(), CACHE_DIR_NAME);
		if (startup) {
			startup = false;
			cleanOldCache(cacheParent);
			cacheParent.mkdirs();
		}
		//make sure we have a unique non-existing cache directory
		cacheDir = getUniqueDirectory(cacheParent, true);
	}

	/**
	 * Implements {@link FileStore#toLocalFile(int, IProgressMonitor)}
	 * @param source 
	 * @param monitor
	 * @return The cached file
	 * @throws CoreException
	 */
	public java.io.File cache(IFileStore source, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(NLS.bind(Messages.copying, toString()), 100);
			IFileInfo myInfo = source.fetchInfo(EFS.NONE, Policy.subMonitorFor(monitor, 25));
			if (!myInfo.exists())
				return new File(cacheDir, "Non-Existent-" + System.currentTimeMillis()); //$NON-NLS-1$
			File result;
			if (myInfo.isDirectory()) {
				result = getUniqueDirectory(cacheDir, false);
			} else {
				result = File.createTempFile(source.getFileSystem().getScheme(), "efs", cacheDir); //$NON-NLS-1$
			}
			monitor.worked(25);
			IFileStore resultStore = new LocalFile(result);
			source.copy(resultStore, EFS.OVERWRITE, Policy.subMonitorFor(monitor, 25));
			result.deleteOnExit();
			return result;
		} catch (IOException e) {
			Policy.error(EFS.ERROR_WRITE, NLS.bind(Messages.couldNotWrite, toString()));
			return null;//can't get here
		} finally {
			monitor.done();
		}
	}

	/**
	 * Performs initial cleanup of any old cached state left over from previous
	 * sessions.
	 */
	private void cleanOldCache(File cacheParent) throws CoreException {
		//clear any old cache - this could be moved to a background thread
		new LocalFile(cacheParent).delete(EFS.NONE, null);
	}

	/**
	 * Returns a new unique directory in the given parent directory.
	 * 
	 * @param parent
	 * @param create <code>true</code> if the directory should
	 * be created, and false otherwise.
	 * @return The unique directory
	 */
	private File getUniqueDirectory(File parent, boolean create) {
		File dir;
		long i = 0;
		//find an unused directory name
		do {
			dir = new File(parent, Long.toString(System.currentTimeMillis() + i++));
		} while (dir.exists());
		if (create)
			dir.mkdir();
		return dir;
	}
}
