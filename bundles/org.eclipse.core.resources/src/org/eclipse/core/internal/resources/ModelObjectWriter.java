package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.SafeFileOutputStream;
import org.eclipse.core.internal.utils.ArrayEnumeration;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
//
public class ModelObjectWriter implements IModelObjectConstants {
public ModelObjectWriter() {
}
protected String[] getReferencedProjects(ProjectDescription description) {
	IProject[] projects = description.getReferencedProjects();
	String[] result = new String[projects.length];
	for (int i = 0; i < projects.length; i++)
		result[i] = projects[i].getName();
	return result;
}
/**
 * The OutputStream is closed in this method.
 */
public void write(Object object, OutputStream output) throws IOException {
	try {
		XMLWriter writer = new XMLWriter(output);
		write(object, writer);
		writer.flush();
		writer.close();
	} finally {
		output.close();
	}
}
protected void write(Object obj, XMLWriter writer) throws IOException {
	if (obj instanceof BuildCommand) {
		write((BuildCommand) obj, writer);
		return;
	}
	if (obj instanceof ProjectDescription) {
		write((ProjectDescription) obj, writer);
		return;
	}
	if (obj instanceof WorkspaceDescription) {
		write((WorkspaceDescription) obj, writer);
		return;
	}
	writer.printTabulation();
	writer.println(obj.toString());
}
/**
 * The parameter tempLocation is a location to place our temp file (copy of the target one)
 * to be used in case we could not successfully write the new file.
 */
public void write(Object object, IPath location, IPath tempLocation) throws IOException {
	SafeFileOutputStream file = null;
	String tempPath = tempLocation == null ? null : tempLocation.toOSString();
	try {
		file = new SafeFileOutputStream(location.toOSString(), tempPath);
		write(object, file);
	} finally {
		if (file != null)
			file.close();
	}
}
protected void write(String name, String elementTagName, String[] array, XMLWriter writer) throws IOException {
	writer.startTag(name, null);
	for (int i = 0; i < array.length; i++)
		writer.printSimpleTag(elementTagName, array[i]);
	writer.endTag(name);
}
protected void write(String name, Collection collection, XMLWriter writer) throws IOException {
	writer.startTag(name, null);
	for (Iterator it = collection.iterator(); it.hasNext(); )
		write(it.next(), writer);
	writer.endTag(name);
}
/**
 * Write maps of (String, String).
 */
protected void write(String name, Map table, XMLWriter writer) throws IOException {
	writer.startTag(name, null);
	for (Iterator it = table.entrySet().iterator(); it.hasNext();) {
		Map.Entry entry =(Map.Entry)  it.next();
		String key = (String) entry.getKey();
		Object value = entry.getValue();
		writer.startTag(DICTIONARY, null);
		{
			writer.printSimpleTag(KEY, key);
			writer.printSimpleTag(VALUE, value);
		}
		writer.endTag(DICTIONARY);
	}
	writer.endTag(name);
}
protected void write(BuildCommand command, XMLWriter writer) throws IOException {
	writer.startTag(BUILD_COMMAND, null);
	if (command != null) {
		writer.printSimpleTag(NAME, command.getName());
		write(ARGUMENTS, command.getArguments(false), writer);
	}
	writer.endTag(BUILD_COMMAND);
}
protected void write(ProjectDescription description, XMLWriter writer) throws IOException {
	writer.startTag(PROJECT_DESCRIPTION, null);
	if (description != null) {
		writer.printSimpleTag(NAME, description.getName());
		String comment = description.getComment();
		writer.printSimpleTag(COMMENT, comment == null ? "" : comment);
		write(PROJECTS, PROJECT, getReferencedProjects(description), writer);
		write(BUILD_SPEC, Arrays.asList(description.getBuildSpec(false)), writer);
		write(NATURES, NATURE, description.getNatureIds(false), writer);
	}
	writer.endTag(PROJECT_DESCRIPTION);
}
protected void write(WorkspaceDescription description, XMLWriter writer) throws IOException {
	writer.startTag(WORKSPACE_DESCRIPTION, null);
	if (description != null) {
		writer.printSimpleTag(NAME, description.getName());
		writer.printSimpleTag(AUTOBUILD, description.isAutoBuilding() ? "1" : "0");
		writer.printSimpleTag(SNAPSHOTS_ENABLED, description.isSnapshotEnabled() ? "1" : "0");
		writer.printSimpleTag(OPERATIONS_PER_SNAPSHOT, new Integer(description.getOperationsPerSnapshot()));
		writer.printSimpleTag(DELTA_EXPIRATION_TIMESTAMP, new Long(description.getDeltaExpiration()));
		writer.printSimpleTag(FILE_STATE_LONGEVITY, new Long(description.getFileStateLongevity()));
		writer.printSimpleTag(MAX_FILE_STATE_SIZE, new Long(description.getMaxFileStateSize()));
		writer.printSimpleTag(MAX_FILE_STATES, new Integer(description.getMaxFileStates()));
		String[] order = description.getBuildOrder(false);
		if (order != null)
			write(BUILD_ORDER, PROJECT, order, writer);
	}
	writer.endTag(WORKSPACE_DESCRIPTION);
}
}
