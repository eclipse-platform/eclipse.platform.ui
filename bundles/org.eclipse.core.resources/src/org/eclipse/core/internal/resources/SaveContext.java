package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.ISaveContext;
import java.util.Map;

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
	String pluginId = plugin.getDescriptor().getUniqueIdentifier();
	fileTable = new SafeFileTable(pluginId);
	previousSaveNumber = getWorkspace().getSaveManager().getSaveNumber(pluginId);
}
public void commit() throws CoreException {
	if (needSaveNumber) {
		String pluginId = plugin.getDescriptor().getUniqueIdentifier();
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
public boolean isSaveNumberNeeded() {
	return needSaveNumber;
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
