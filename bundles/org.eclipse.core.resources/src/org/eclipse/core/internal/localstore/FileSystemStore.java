package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.*;
import java.io.*;

public class FileSystemStore implements ILocalStoreConstants {
public FileSystemStore() {
}
public void copy(File source, File destination, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("copying", new String[] {source.getAbsolutePath()}), 1);
		Policy.checkCanceled(monitor);
		if (source.isDirectory())
			copyDirectory(source, destination, depth, Policy.subMonitorFor(monitor, 1));
		else
			copyFile(source, destination, Policy.subMonitorFor(monitor, 1));
	} finally {
		monitor.done();
	}
}
protected void copyDirectory(File source, File destination, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String[] children = source.list();
		if(children == null){
			children = new String[0];
		}

		monitor.beginTask(Policy.bind("copying", new String[] { source.getAbsolutePath()}), children.length);
		// create directory
		writeFolder(destination);

		// depth
		if (depth == IResource.DEPTH_ZERO)
			return;
		if (depth == IResource.DEPTH_ONE)
			depth = IResource.DEPTH_ZERO;

		// copy children
		for (int i = 0; i < children.length; i++)
			copy(new File(source, children[i]), new File(destination, children[i]), depth, Policy.subMonitorFor(monitor, 1));
	} finally {
		monitor.done();
	}
}
protected void copyFile(File target, File destination, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int totalWork = 1 + ((int) target.length() / 8192);
		monitor.beginTask(Policy.bind("copying", new String[] {target.getAbsolutePath()}), totalWork);
		write(destination, read(target), false, monitor);
	} finally {
		monitor.done();
	}
}
public void delete(File target) throws CoreException {
	if (!Workspace.clear(target))
		throw new ResourceException(IResourceStatus.FAILED_DELETE_LOCAL, new Path(target.getAbsolutePath()), Policy.bind("couldnotDelete", new String[] {target.getAbsolutePath()}), null);
}
public boolean delete(java.io.File root, MultiStatus status) {
	boolean failedRecursive = false;
	if (root.isDirectory()) {
		String[] list = root.list();
		// for some unknown reason, list() can return null.  
		// Just skip the children If it does.
		if (list != null)
			for (int i = 0; i < list.length; i++)
				// try best effort on all children so put logical OR at end
				failedRecursive = !delete(new java.io.File(root, list[i]), status) || failedRecursive;
	}
	boolean failedThis = false;
	try {
		// don't try to delete the root if one of the children failed
		if (!failedRecursive && root.exists())
			failedThis = !root.delete();
	} catch (Exception e) {
		// we caught a runtime exception so log it
		String message = "Exception trying to delete file: " + root.getAbsolutePath();
		status.add(new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, message));
		return false;
	}
	if (failedThis) {
		String message = null;
		if (CoreFileSystemLibrary.isReadOnly(root.getAbsolutePath()))
			message = "Could not delete read-only resource: " + root.getAbsolutePath();
		else
			message = Policy.bind("couldnotDelete", new String[] { root.getAbsolutePath()});
		status.add(new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, message));
	}
	return !(failedRecursive || failedThis);
}
public void move(File source, File destination, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("moving", new String[] {source.getAbsolutePath()}), 2);
		if (destination.exists()) {
			if (!force)
				throw new ResourceException(IResourceStatus.EXISTS_LOCAL, new Path(destination.getAbsolutePath()), Policy.bind("resourceExists", null), null);
			else
				try {
					delete(destination);
				} catch (CoreException e) {
					String message = "Could not move file";
					throw new ResourceException(-1, new Path(destination.getAbsolutePath()), message, e);
				}
		}
		if (!source.renameTo(destination)) {
			boolean success = false;
			try {
				copy(source, destination, IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 1));
				success = true;
			} finally {
				if (success)
					Workspace.clear(source);
				else
					Workspace.clear(destination);
			}
			monitor.worked(1);
		}
	} finally {
		monitor.done();
	}
}
/**
 * Returns an input stream containing the contents of the given
 * file as maintained by this store.  The user of the returned
 * stream is responsible for closing the stream when finished.
 *
 * @exception CoreException if the content of
 *		the resource cannot be accessed.
 */
public InputStream read(File target) throws CoreException {
	try {
		return new FileInputStream(target);
	} catch (FileNotFoundException e) {
		String message;
		if (!target.exists())
			message = Policy.bind("fileNotFound", null);
		else
			if (target.isDirectory())
				message = Policy.bind("resourceFolder", null);
			else
				message = Policy.bind("couldNotRead", null);
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, new Path(target.getAbsolutePath()), message, e);
	}
}
/**
 * This method also closes both streams.
 */
public static void transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor) throws IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		byte[] buffer = new byte[8192];
		while (true) {
			int bytesRead = source.read(buffer);
			if (bytesRead == -1)
				break;
			destination.write(buffer, 0, bytesRead);
			monitor.worked(1);
		}
	} finally {
		try {
			source.close();
		} catch (IOException e) {
		}
		try {
			destination.close();
		} catch (IOException e) {
		}
	}
}
/**
 * Content cannot be null and it is closed even if the operation is not
 * completed successfully.
 */
public void write(File target, InputStream content, boolean append, IProgressMonitor monitor) throws CoreException {
	try {
		try {
			writeFolder(new File(target.getParent()));
			FileOutputStream output = new FileOutputStream(target.getAbsolutePath(), append);
			transferStreams(content, output, monitor);
		} finally {
			content.close();
		}
	} catch (IOException e) {
		String message = null;
		if (CoreFileSystemLibrary.isReadOnly(target.getAbsolutePath()))
			message = "Could not write to read-only file: " + target.getAbsolutePath();
		else
			message = Policy.bind("couldNotWrite", null);
		throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, new Path(target.getAbsolutePath()), message, e);
	}
}
public void writeFolder(File target) throws CoreException {
	if (!target.exists())
		target.mkdirs();
	if (!target.isDirectory())
		throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, new Path(target.getAbsolutePath()), Policy.bind("couldNotCreateFolder", null), null);
}
}
