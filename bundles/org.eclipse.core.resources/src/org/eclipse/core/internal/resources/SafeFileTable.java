/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import java.util.Properties;
import java.util.Set;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

/**
 * Represents a table of keys and paths used by a plugin to maintain its
 * configuration files' names.
 */
public class SafeFileTable {
	protected IPath location;
	protected Properties table;

	public SafeFileTable(String pluginId) throws CoreException {
		location = getWorkspace().getMetaArea().getSafeTableLocationFor(pluginId);
		restore();
	}

	public IPath[] getFiles() {
		Set<Object> set = table.keySet();
		String[] keys = set.toArray(new String[set.size()]);
		IPath[] files = new IPath[keys.length];
		for (int i = 0; i < keys.length; i++)
			files[i] = new Path(keys[i]);
		return files;
	}

	protected Workspace getWorkspace() {
		return (Workspace) ResourcesPlugin.getWorkspace();
	}

	public IPath lookup(IPath file) {
		String result = table.getProperty(file.toOSString());
		return result == null ? null : new Path(result);
	}

	public void map(IPath file, IPath aLocation) {
		if (aLocation == null)
			table.remove(file);
		else
			table.setProperty(file.toOSString(), aLocation.toOSString());
	}

	public void restore() throws CoreException {
		java.io.File target = location.toFile();
		table = new Properties();
		if (!target.exists())
			return;
		try {
			FileInputStream input = new FileInputStream(target);
			try {
				table.load(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			String message = Messages.resources_exSafeRead;
			throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
		}
	}

	public void save() throws CoreException {
		java.io.File target = location.toFile();
		try {
			FileOutputStream output = new FileOutputStream(target);
			try {
				table.store(output, "safe table"); //$NON-NLS-1$
				output.close();
			} finally {
				FileUtil.safeClose(output);
			}
		} catch (IOException e) {
			String message = Messages.resources_exSafeSave;
			throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
		}
	}

	public void setLocation(IPath location) {
		if (location != null)
			this.location = location;
	}
}
