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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.internal.localstore.SafeFileOutputStream;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;

//
public class ModelObjectWriter implements IModelObjectConstants {

	/**
	 * Returns the string representing the serialized set of build triggers for
	 * the given command
	 */
	private static String triggerString(BuildCommand command) {
		StringBuffer buf = new StringBuffer();
		if (command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD))
			buf.append(TRIGGER_AUTO).append(',');
		if (command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD))
			buf.append(TRIGGER_CLEAN).append(',');
		if (command.isBuilding(IncrementalProjectBuilder.FULL_BUILD))
			buf.append(TRIGGER_FULL).append(',');
		if (command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD))
			buf.append(TRIGGER_INCREMENTAL).append(',');
		return buf.toString();
	}

	public ModelObjectWriter() {
		super();
	}

	protected String[] getReferencedProjects(ProjectDescription description) {
		IProject[] projects = description.getReferencedProjects();
		String[] result = new String[projects.length];
		for (int i = 0; i < projects.length; i++)
			result[i] = projects[i].getName();
		return result;
	}

	protected void write(BuildCommand command, XMLWriter writer) {
		writer.startTag(BUILD_COMMAND, null);
		if (command != null) {
			writer.printSimpleTag(NAME, command.getName());
			if (shouldWriteTriggers(command))
				writer.printSimpleTag(BUILD_TRIGGERS, triggerString(command));
			write(ARGUMENTS, command.getArguments(false), writer);
		}
		writer.endTag(BUILD_COMMAND);
	}

	/**
	 * Returns whether the build triggers for this command should be written.
	 */
	private boolean shouldWriteTriggers(BuildCommand command) {
		//only write triggers if command is configurable and there exists a trigger
		//that the builder does NOT respond to.  I.e., don't write out on the default
		//cases to avoid dirtying .project files unnecessarily.	
		if (!command.isConfigurable())
			return false;
		return !command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD) || 
			!command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD) || 
			!command.isBuilding(IncrementalProjectBuilder.FULL_BUILD) || 
			!command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD);
	}

	protected void write(LinkDescription description, XMLWriter writer) {
		writer.startTag(LINK, null);
		if (description != null) {
			writer.printSimpleTag(NAME, description.getProjectRelativePath());
			writer.printSimpleTag(TYPE, Integer.toString(description.getType()));
			//use ASCII string of URI to ensure spaces are encoded
			writeLocation(description.getLocationURI(), writer);
		}
		writer.endTag(LINK);
	}

	/**
	 * Writes a location to the XML writer.  For backwards compatibility,
	 * local file system locations are written and read using a different tag
	 * from non-local file systems.
	 * @param location
	 * @param writer
	 */
	private void writeLocation(URI location, XMLWriter writer) {
		if (EFS.SCHEME_FILE.equals(location.getScheme())) {
			writer.printSimpleTag(LOCATION, FileUtil.toPath(location).toPortableString());
		} else {
			writer.printSimpleTag(LOCATION_URI, location.toASCIIString());
		}
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
		if (obj instanceof LinkDescription) {
			write((LinkDescription) obj, writer);
			return;
		}
		writer.printTabulation();
		writer.println(obj.toString());
	}

	protected void write(ProjectDescription description, XMLWriter writer) throws IOException {
		writer.startTag(PROJECT_DESCRIPTION, null);
		if (description != null) {
			writer.printSimpleTag(NAME, description.getName());
			String comment = description.getComment();
			writer.printSimpleTag(COMMENT, comment == null ? "" : comment); //$NON-NLS-1$
			write(PROJECTS, PROJECT, getReferencedProjects(description), writer);
			write(BUILD_SPEC, Arrays.asList(description.getBuildSpec(false)), writer);
			write(NATURES, NATURE, description.getNatureIds(false), writer);
			HashMap links = description.getLinks();
			if (links != null) {
				// ensure consistent order of map elements
				List sorted = new ArrayList(links.values());
				Collections.sort(sorted);
				write(LINKED_RESOURCES, sorted, writer);
			}
		}
		writer.endTag(PROJECT_DESCRIPTION);
	}

	protected void write(String name, Collection collection, XMLWriter writer) throws IOException {
		writer.startTag(name, null);
		for (Iterator it = collection.iterator(); it.hasNext();)
			write(it.next(), writer);
		writer.endTag(name);
	}

	/**
	 * Write maps of (String, String).
	 */
	protected void write(String name, Map table, XMLWriter writer) {
		writer.startTag(name, null);
		
		// ensure consistent order of map elements
		List sorted = new ArrayList(table.keySet());
		Collections.sort(sorted);
		
		for (Iterator it = sorted.iterator(); it.hasNext();) {
			String key = (String) it.next();
			Object value = table.get(key);
			writer.startTag(DICTIONARY, null);
			{
				writer.printSimpleTag(KEY, key);
				writer.printSimpleTag(VALUE, value);
			}
			writer.endTag(DICTIONARY);
		}
		writer.endTag(name);
	}

	protected void write(String name, String elementTagName, String[] array, XMLWriter writer) {
		writer.startTag(name, null);
		for (int i = 0; i < array.length; i++)
			writer.printSimpleTag(elementTagName, array[i]);
		writer.endTag(name);
	}

	protected void write(WorkspaceDescription description, XMLWriter writer) {
		writer.startTag(WORKSPACE_DESCRIPTION, null);
		if (description != null) {
			writer.printSimpleTag(NAME, description.getName());
			writer.printSimpleTag(AUTOBUILD, description.isAutoBuilding() ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.printSimpleTag(SNAPSHOT_INTERVAL, new Long(description.getSnapshotInterval()));
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
