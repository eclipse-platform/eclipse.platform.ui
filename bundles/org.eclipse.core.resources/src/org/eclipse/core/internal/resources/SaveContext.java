/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class SaveContext implements ISaveContext {
	protected Plugin plugin;
	protected int kind;
	protected boolean needDelta;
	protected boolean needSaveNumber;
	protected SafeFileTable fileTable;
	protected int previousSaveNumber;
	protected IProject project;

	protected SaveContext(Plugin plugin, int kind, IProject project) throws CoreException {
		this.plugin = plugin;
		this.kind = kind;
		this.project = project;
		needDelta = false;
		needSaveNumber = false;
		String pluginId = plugin.getBundle().getSymbolicName();
		fileTable = new SafeFileTable(pluginId);
		previousSaveNumber = getWorkspace().getSaveManager().getSaveNumber(pluginId);
	}

	public void commit() throws CoreException {
		if (needSaveNumber) {
			String pluginId = plugin.getBundle().getSymbolicName();
			IPath oldLocation = getWorkspace().getMetaArea().getSafeTableLocationFor(pluginId);
			getWorkspace().getSaveManager().setSaveNumber(pluginId, getSaveNumber());
			fileTable.setLocation(getWorkspace().getMetaArea().getSafeTableLocationFor(pluginId));
			fileTable.save();
			oldLocation.toFile().delete();
		}
	}

	/**
	 * @see ISaveContext
	 */
	public IPath[] getFiles() {
		return getFileTable().getFiles();
	}

	protected SafeFileTable getFileTable() {
		return fileTable;
	}

	/**
	 * @see ISaveContext
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * @see ISaveContext
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/**
	 * @see ISaveContext
	 */
	public int getPreviousSaveNumber() {
		return previousSaveNumber;
	}

	/**
	 * @see ISaveContext
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @see ISaveContext
	 */
	public int getSaveNumber() {
		int result = getPreviousSaveNumber() + 1;
		return result > 0 ? result : 1;
	}

	protected Workspace getWorkspace() {
		return (Workspace) ResourcesPlugin.getWorkspace();
	}

	public boolean isDeltaNeeded() {
		return needDelta;
	}

	/**
	 * @see ISaveContext
	 */
	public IPath lookup(IPath file) {
		return getFileTable().lookup(file);
	}

	/**
	 * @see ISaveContext
	 */
	public void map(IPath file, IPath location) {
		getFileTable().map(file, location);
	}

	/**
	 * @see ISaveContext
	 */
	public void needDelta() {
		needDelta = true;
	}

	/**
	 * @see ISaveContext
	 */
	public void needSaveNumber() {
		needSaveNumber = true;
	}
}
