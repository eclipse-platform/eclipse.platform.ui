/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.*;
import org.eclipse.core.internal.localstore.Bucket.Visitor;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;


/**
 * @since 3,1
 */
public class BucketTree {
	public static final int DEPTH_INFINITE = Integer.MAX_VALUE;
	public static final int DEPTH_ONE = 1;
	public static final int DEPTH_ZERO = 0;

	private final static int SEGMENT_LENGTH = 2;
	private final static long SEGMENT_QUOTA = (long) Math.pow(2, 4 * SEGMENT_LENGTH); // 1 char = 2 ^ 4 = 0x10	

	private static final String VERSION_FILE_EXT = ".version"; //$NON-NLS-1$

	protected Bucket current;

	private Workspace workspace;

	public BucketTree(Workspace workspace, Bucket bucket) {
		this.current = bucket;
		this.workspace = workspace;
	}

	/**
	 * From a starting point in the tree, visit all nodes under it. 
	 * @param visitor
	 * @param base
	 * @param depth
	 */
	public void accept(Bucket.Visitor visitor, IPath base, int depth) throws CoreException {
		if (Path.ROOT.equals(base)) {
			current.load(null, locationFor(Path.ROOT));
			if (current.accept(visitor, base, DEPTH_ZERO) != Visitor.CONTINUE)
				return;
			if (depth == DEPTH_ZERO)
				return;
			boolean keepVisiting = true;
			depth--;
			IProject[] projects = workspace.getRoot().getProjects();
			for (int i = 0; keepVisiting && i < projects.length; i++) {
				IPath projectPath = projects[i].getFullPath();
				keepVisiting = internalAccept(visitor, projectPath, locationFor(projectPath), depth, 1);
			}
		} else
			internalAccept(visitor, base, locationFor(base), depth, 0);
	}

	public void close() throws CoreException {
		current.save();
		saveVersion();
	}

	public Bucket getCurrent() {
		return current;
	}

	public File getVersionFile() {
		return new File(locationFor(Path.ROOT), current.getFileName() + VERSION_FILE_EXT);
	}

	/**
	 * This will never be called for a bucket for the workspace root.
	 *  
	 * @return whether to continue visiting other branches 
	 */
	private boolean internalAccept(Bucket.Visitor visitor, IPath base, File bucketDir, int depthRequested, int currentDepth) throws CoreException {
		current.load(base.segment(0), bucketDir);
		int outcome = current.accept(visitor, base, depthRequested);
		if (outcome != Visitor.CONTINUE)
			return outcome == Visitor.RETURN;
		if (depthRequested <= currentDepth)
			return true;
		File[] subDirs = bucketDir.listFiles();
		if (subDirs == null)
			return true;
		for (int i = 0; i < subDirs.length; i++)
			if (subDirs[i].isDirectory())
				if (!internalAccept(visitor, base, subDirs[i], depthRequested, currentDepth + 1))
					return false;
		return true;
	}

	public void loadBucketFor(IPath path) throws CoreException {
		current.load(Path.ROOT.equals(path) ? null : path.segment(0), locationFor(path));
	}

	private File locationFor(IPath resourcePath) {
		IPath baseLocation = workspace.getMetaArea().locationFor(resourcePath);
		int segmentCount = resourcePath.segmentCount();
		baseLocation = baseLocation.append(Bucket.INDEXES_DIR_NAME);
		// the root or a project
		if (segmentCount <= 1)
			return baseLocation.toFile();
		// a folder or file
		IPath location = baseLocation;
		// the last segment is ignored
		for (int i = 1; i < segmentCount - 1; i++)
			// translate all segments except the first one (project name)
			location = location.append(translateSegment(resourcePath.segment(i)));
		return location.toFile();
	}

	/**
	 * Writes the version tag to a file on disk.
	 */
	private void saveVersion() throws CoreException {
		File versionFile = getVersionFile();
		if (!versionFile.getParentFile().exists())
			versionFile.getParentFile().mkdirs();
		FileOutputStream stream = null;
		boolean failed = false;
		try {
			stream = new FileOutputStream(versionFile);
			stream.write(current.getVersion());
		} catch (IOException e) {
			failed = true;
			String message = NLS.bind(Messages.resources_writeWorkspaceMeta, versionFile.getAbsolutePath()); 
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, e);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				if (!failed) {
					String message = NLS.bind(Messages.resources_writeWorkspaceMeta, versionFile.getAbsolutePath());
					throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, e);
				}
			}
		}
	}

	private String translateSegment(String segment) {
		// String.hashCode algorithm is API
		return Long.toHexString(Math.abs(segment.hashCode()) % SEGMENT_QUOTA);
	}
}
