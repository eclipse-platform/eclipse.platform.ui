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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import org.eclipse.core.internal.localstore.SafeFileInputStream;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * This class contains legacy code only.  It is being used to read workspace
 * descriptions which are obsolete.
 */
public class WorkspaceDescriptionReader implements IModelObjectConstants {
	/** constants */
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];

	public WorkspaceDescriptionReader() {
		super();
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
		List<Object> result = new ArrayList<Object>(list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				result.add(read(node.getChildNodes().item(0)));
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * A value was discovered in the workspace description file that was not a number.
	 * Log the exception.
	 */
	private void logNumberFormatException(String value, NumberFormatException e) {
		String msg = NLS.bind(Messages.resources_readWorkspaceMetaValue, value);
		Policy.log(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, null, msg, e));
	}

	public Object read(InputStream input) {
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(input);
			return read(document.getFirstChild());
		} catch (IOException e) {
			// ignore
		} catch (SAXException e) {
			// ignore
		} catch (ParserConfigurationException e) {
			// ignore
		}
		return null;
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
				if (node.getNodeName().equals(WORKSPACE_DESCRIPTION))
					return readWorkspaceDescription(node);
			case Node.TEXT_NODE :
				String value = node.getNodeValue();
				return value == null ? null : value.trim();
			default :
				return node.toString();
		}
	}

	/**
	 * read (String, String) hashtables
	 */
	protected WorkspaceDescription readWorkspaceDescription(Node node) {
		// get values
		String name = getString(node, NAME);
		String autobuild = getString(node, AUTOBUILD);
		String snapshotInterval = getString(node, SNAPSHOT_INTERVAL);
		String applyFileStatePolicy = getString(node, APPLY_FILE_STATE_POLICY);
		String fileStateLongevity = getString(node, FILE_STATE_LONGEVITY);
		String maxFileStateSize = getString(node, MAX_FILE_STATE_SIZE);
		String maxFileStates = getString(node, MAX_FILE_STATES);
		String[] buildOrder = getStrings(searchNode(node, BUILD_ORDER));

		// build instance
		//invalid values are skipped and defaults are used instead
		WorkspaceDescription description = new WorkspaceDescription(name);
		if (autobuild != null)
			//if in doubt (value is corrupt) we want autobuild on
			description.setAutoBuilding(!autobuild.equals(Integer.toString(0)));
		if (applyFileStatePolicy != null)
			//if in doubt (value is corrupt) we want applyFileLimits on
			description.setApplyFileStatePolicy(!applyFileStatePolicy.equals(Integer.toString(0)));
		try {
			if (fileStateLongevity != null)
				description.setFileStateLongevity(Long.parseLong(fileStateLongevity));
		} catch (NumberFormatException e) {
			logNumberFormatException(fileStateLongevity, e);
		}
		try {
			if (maxFileStateSize != null)
				description.setMaxFileStateSize(Long.parseLong(maxFileStateSize));
		} catch (NumberFormatException e) {
			logNumberFormatException(maxFileStateSize, e);
		}
		try {
			if (maxFileStates != null)
				description.setMaxFileStates(Integer.parseInt(maxFileStates));
		} catch (NumberFormatException e) {
			logNumberFormatException(maxFileStates, e);
		}
		if (buildOrder != null)
			description.internalSetBuildOrder(buildOrder);
		try {
			if (snapshotInterval != null)
				description.setSnapshotInterval(Long.parseLong(snapshotInterval));
		} catch (NumberFormatException e) {
			logNumberFormatException(snapshotInterval, e);
		}
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
