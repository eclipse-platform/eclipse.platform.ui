/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.SafeFileInputStream;
import org.eclipse.core.internal.utils.EmptyEnumeration;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
/**
 *
 */
public class ModelObjectReader implements IModelObjectConstants {
	/** constants */
	protected static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
public ModelObjectReader() {
}
protected Node getFirstChild(Node target, short type) {
	NodeList list = target.getChildNodes();
	for (int i = 0; i < list.getLength(); i++)
		if (list.item(i).getNodeType() == type)
			return list.item(i);
	return null;
}
protected IProject[] getProjects(Node target) {
	if (target == null)
		return EMPTY_PROJECT_ARRAY;
	NodeList list = target.getChildNodes();
	if (list.getLength() == 0)
		return EMPTY_PROJECT_ARRAY;
	List result = new ArrayList(list.getLength());
	for (int i = 0; i < list.getLength(); i++) {
		Node node = list.item(i);
		if (node.getNodeType() == Node.ELEMENT_NODE)
			result.add(ResourcesPlugin.getWorkspace().getRoot().getProject((String) read(node.getChildNodes().item(0))));
	}
	return (IProject[]) result.toArray(new IProject[result.size()]);
}
protected String getString(Node target, String tagName) {
	Node node = searchNode(target, tagName);
	return node != null ? (node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue()) : null;
}
protected String[] getStrings(Node target) {
	if (target == null)
		return null;
	NodeList list = target.getChildNodes();
	if (list.getLength() == 0)
		return EMPTY_STRING_ARRAY;
	List result = new ArrayList(list.getLength());
	for (int i = 0; i < list.getLength(); i++) {
		Node node = list.item(i);
		if (node.getNodeType() == Node.ELEMENT_NODE)
			result.add((String) read(node.getChildNodes().item(0)));
	}
	return (String[]) result.toArray(new String[result.size()]);
}
public Object read(InputStream input) {
	try {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(input);
		return read(document.getFirstChild());
	} catch (IOException e) {
	} catch (SAXException e) {
	} catch (ParserConfigurationException e) {
	}
	return null;
}
public Object read(IPath location) throws IOException {
	FileInputStream file = null;
	try {
		file = new FileInputStream(location.toFile());
		return read(file);
	} finally {
		if (file != null)
			file.close();
	}
}
public Object read(IPath location, IPath tempLocation) throws IOException {
	SafeFileInputStream file = new SafeFileInputStream(location.toOSString(), tempLocation.toOSString());
	try {
		return read(file);
	} finally {
		file.close();
	}
}
protected Object read(Node node) {
	if (node == null)
		return null;
	switch (node.getNodeType()) {
		case Node.ELEMENT_NODE :
			if (node.getNodeName().equals(BUILD_COMMAND))
				return readBuildCommand(node);
			if (node.getNodeName().equals(BUILD_SPEC))
				return readBuildSpec(node);
			if (node.getNodeName().equals(PROJECT_DESCRIPTION))
				return readProjectDescription(node);
			if (node.getNodeName().equals(WORKSPACE_DESCRIPTION))
				return readWorkspaceDescription(node);
		case Node.TEXT_NODE :
			String value = node.getNodeValue();
			return value == null ? null : value.trim();
		default :
			return node.toString();
	}
}
protected BuildCommand readBuildCommand(Node node) {
	// get values
	String name = getString(node, NAME);
	Hashtable arguments = readHashtable(searchNode(node, ARGUMENTS));

	// build instance
	BuildCommand command = new BuildCommand();
	command.setName(name);
	if (arguments != null)
		command.setArguments(arguments);
	return command;
}
protected ICommand[] readBuildSpec(Node node) {
	if (node == null)
		return null;
	List results = new ArrayList(5);
	NodeList list = node.getChildNodes();
	for (int i = 0; i < list.getLength(); i++)
		if (list.item(i).getNodeType() == Node.ELEMENT_NODE)
			if (list.item(i).getNodeName().equals(BUILD_COMMAND))
				results.add(readBuildCommand(list.item(i)));
	return (ICommand[]) results.toArray(new ICommand[results.size()]);
}
/**
 * read (String, String) hashtables
 */
protected Hashtable readHashtable(Node target) {
	if (target == null)
		return null;
	Hashtable result = new Hashtable(5);
	NodeList list = target.getChildNodes();
	for (int i = 0; i < list.getLength(); i++) {
		Node node = list.item(i);
		if (node.getNodeType() == Node.ELEMENT_NODE)
			if (node.getChildNodes().getLength() > 1) {
				String name = getString(node, KEY);
				String value = getString(node, VALUE);
				if (name != null)
					result.put(name, value == null ? "" : value); //$NON-NLS-1$
			}
	}
	return result;
}
protected ProjectDescription readProjectDescription(Node node) {
	// get values
	String name = getString(node, NAME);
	String comment = getString(node, COMMENT);
	IProject[] projects = getProjects(searchNode(node, PROJECTS));
	String location = getString(node, LOCATION);
	ICommand[] buildSpec = readBuildSpec(searchNode(node, BUILD_SPEC));
	String[] natures = getStrings(searchNode(node, NATURES));
	// build instance
	ProjectDescription description = new ProjectDescription();
	description.setName(name);
	description.setComment(comment);
	if (projects != null)
		description.setReferencedProjects(projects);
	if (location != null)
		description.setLocation(new Path(location));
	if (buildSpec != null)
		description.setBuildSpec(buildSpec);
	if (natures == null)
		natures = EMPTY_STRING_ARRAY;
	description.setNatureIds(natures);
	return description;
}
protected WorkspaceDescription readWorkspaceDescription(Node node) {
	// get values
	String name = getString(node, NAME);
	String autobuild = getString(node, AUTOBUILD);
	String snapshots = getString(node, SNAPSHOTS_ENABLED);
	String operations = getString(node, OPERATIONS_PER_SNAPSHOT);
	String snapshotInterval = getString(node, SNAPSHOT_INTERVAL);
	String deltaExpiration = getString(node, DELTA_EXPIRATION_TIMESTAMP);
	String fileStateLongevity = getString(node, FILE_STATE_LONGEVITY);
	String maxFileStateSize = getString(node, MAX_FILE_STATE_SIZE);
	String maxFileStates = getString(node, MAX_FILE_STATES);
	String[] buildOrder = getStrings(searchNode(node, BUILD_ORDER));

	// build instance
	WorkspaceDescription description = new WorkspaceDescription(name);
	if (autobuild != null)
		description.setAutoBuilding(autobuild.equals("1")); //$NON-NLS-1$
	if (operations != null)
		description.setOperationsPerSnapshot(new Integer(operations).intValue());
	if (snapshots != null)
		description.setSnapshotEnabled(snapshots.equals("1")); //$NON-NLS-1$
	if (deltaExpiration != null)
		description.setDeltaExpiration(new Long(deltaExpiration).longValue());
	if (fileStateLongevity != null)
		description.setFileStateLongevity(new Long(fileStateLongevity).longValue());
	if (maxFileStateSize != null)
		description.setMaxFileStateSize(new Long(maxFileStateSize).longValue());
	if (maxFileStates != null)
		description.setMaxFileStates(new Integer(maxFileStates).intValue());
	if (buildOrder != null)
		description.internalSetBuildOrder(buildOrder);
	if (snapshotInterval != null) 
		description.setSnapshotInterval(new Long(snapshotInterval).longValue());
	return description;
}
protected Node searchNode(Node target, String tagName) {
	NodeList list = target.getChildNodes();
	for (int i = 0; i < list.getLength(); i++) {
		if (list.item(i).getNodeName().equals(tagName))
			return list.item(i);
	}
	return null;
}
}
