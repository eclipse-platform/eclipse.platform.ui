/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.wrapper;

import java.io.*;
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * A simple file system implementation that acts as a wrapper around the
 * local file system.
 */
public class WrapperFileStore extends FileStore {

	private final IFileStore baseStore;

	public WrapperFileStore(IFileStore baseStore) {
		this.baseStore = baseStore;
	}

	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.childInfos(options, monitor);
	}

	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.childNames(options, monitor);
	}

	public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {
		IFileStore[] childStores = baseStore.childStores(options, monitor);
		for (int i = 0; i < childStores.length; i++)
			// replace ordinary file store with wrapper version
			childStores[i] = new WrapperFileStore(childStores[i]);
		return childStores;
	}

	public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		baseStore.copy(destination, options, monitor);
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		baseStore.delete(options, monitor);
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof WrapperFileStore))
			return false;
		return baseStore.equals(((WrapperFileStore) obj).baseStore);
	}

	public IFileInfo fetchInfo() {
		return baseStore.fetchInfo();
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.fetchInfo(options, monitor);
	}

	public IFileStore getBaseStore() {
		return baseStore;
	}

	public IFileStore getChild(IPath path) {
		return new WrapperFileStore(baseStore.getChild(path));
	}
	
	public IFileStore getFileStore(IPath path) {
		return new WrapperFileStore(baseStore.getFileStore(path));
	}

	public IFileStore getChild(String name) {
		return new WrapperFileStore(baseStore.getChild(name));
	}

	public IFileSystem getFileSystem() {
		return WrapperFileSystem.getInstance();
	}

	public String getName() {
		return baseStore.getName();
	}

	public IFileStore getParent() {
		IFileStore baseParent = baseStore.getParent();
		return baseParent == null ? null : new WrapperFileStore(baseParent);
	}

	public int hashCode() {
		return baseStore.hashCode();
	}

	public boolean isParentOf(IFileStore other) {
		if (!(other instanceof WrapperFileStore))
			return false;
		IFileStore otherBaseStore = ((WrapperFileStore) other).baseStore;
		return baseStore.isParentOf(otherBaseStore);
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		baseStore.mkdir(options, monitor);
		return this;
	}

	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		if (destination instanceof WrapperFileStore)
			destination = ((WrapperFileStore) destination).baseStore;
		baseStore.move(destination, options, monitor);
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.openInputStream(options, monitor);
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.openOutputStream(options, monitor);
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		baseStore.putInfo(info, options, monitor);
	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.toLocalFile(options, monitor);
	}

	public URI toURI() {
		return WrapperFileSystem.getWrappedURI(baseStore.toURI());
	}
}
