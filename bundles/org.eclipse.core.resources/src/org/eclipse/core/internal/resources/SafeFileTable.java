package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Policy;
import java.io.*;
import java.util.*;
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
	Set set = table.keySet();
	String[] keys = (String[]) set.toArray(new String[set.size()]);
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
public void map(IPath file, IPath location) {
	if (location == null)
		table.remove(file);
	else
		table.setProperty(file.toOSString(), location.toOSString());
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
		String message = Policy.bind("resources.exSafeRead");
		throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
	}
}
public void save() throws CoreException {
	java.io.File target = location.toFile();
	try {
		FileOutputStream output = new FileOutputStream(target);
		try {
			table.store(output, "safe table");
		} finally {
			output.close();
		}
	} catch (IOException e) {
		String message = Policy.bind("resources.exSafeSave");
		throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
	}
}
public void setLocation(IPath location) {
	if (location != null)
		this.location = location;
}
}
