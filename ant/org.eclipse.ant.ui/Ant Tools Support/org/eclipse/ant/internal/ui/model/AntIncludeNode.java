/*******************************************************************************
 * Copyright (c) 2019 arctis Softwaretechnologie GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Blaas (arctis Softwaretechnologie GmbH) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.model;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.xml.sax.Attributes;

public class AntIncludeNode extends AntImportNode {

	public AntIncludeNode(Task task, Attributes attributes) {
		super(task, attributes);
	}

	@Override
	protected ImageDescriptor getBaseImageDescriptor() {
		// TODO: set an include-descriptor
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_INCLUDE);
	}

	/**
	 * Execute the import. Returns <code>true</code> as the import adds to the Ant model
	 */
	@Override
	public boolean configure(boolean validateFully) {
		if (fConfigured) {
			return false;
		}

		try {
			getTask().maybeConfigure();
			// Get the node where the import happened.
			AntTaskNode importedFromNode = (AntTaskNode) getImportNode();
			// ImportNode is null => include happened from the top-file.
			if (importedFromNode != null) {
				String currentPrefix = this.getIncludePrefix(importedFromNode);
				ProjectHelper.setCurrentTargetPrefix(currentPrefix);
			} else {
				// Reset the currentTargetPrefix if the include-task has no import node.
				ProjectHelper.setCurrentTargetPrefix(null);
			}

			getTask().execute();
			fConfigured = true;
			return true;
		}
		catch (BuildException be) {
			handleBuildException(be, AntEditorPreferenceConstants.PROBLEM_IMPORTS);
		}
		catch (AntSecurityException se) {
			// Either a system exit or setting of system property was attempted.
			handleBuildException(new BuildException(AntModelMessages.AntImportNode_0), AntEditorPreferenceConstants.PROBLEM_SECURITY);
		}
		return false;
	}

	/**
	 * Gets the include prefix among the whole "include-hierarchy".
	 *
	 * @param startNode
	 *            The node where to start.
	 *
	 * @return The complete include-prefix.
	 */
	private String getIncludePrefix(AntTaskNode startNode) {
		AntTaskNode actualNode = startNode;
		StringBuilder builder = new StringBuilder();
		while (actualNode != null) {
			// Supposed to be non-null.
			String actualPrefixSeparator = ProjectHelper.getCurrentPrefixSeparator();
			String actualPrefix = this.getIncludePrefixSingleNode(actualNode);

			if (actualPrefix != null) {
				// Iteration happens in reverse order => prepend.
				builder.insert(0, actualPrefix + actualPrefixSeparator);
			}
			actualNode = (AntTaskNode) actualNode.getImportNode();
		}
		// Remove last separator.
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	/**
	 * Extracts the include prefix (currentTargetPrefix) for nested includes.
	 *
	 * @param importedFromNode
	 *            The node where the import belongs to.
	 * @return The include prefix or null.
	 */
	private String getIncludePrefixSingleNode(AntTaskNode importedFromNode) {
		Object asPropValue = importedFromNode.getTask().getRuntimeConfigurableWrapper().getAttributeMap().get("as"); //$NON-NLS-1$
		String includePrefix = (asPropValue != null ? asPropValue.toString() : null);

		// It may happen, that an include hasn't set the "as" attribute => the prefix needs to be retrieved elsewhere.
		if (includePrefix == null) {
			/*
			 * When the current build-file is parsed, its project-name is stored at a property-holder (see
			 * org.eclipse.ant.internal.ui.editor.utils.ProjectHelper) and associated with the current build-file's absolute path. To get the required
			 * project-name, just a lookup at the mentioned property-holder is required.
			 */
			includePrefix = this.extractProjectNameSingleNode(importedFromNode);
		}
		return includePrefix;
	}

	/**
	 * Retrieves the project name of the associated build-file by doing a lookup at a property-holder.
	 *
	 * @param importedFromNode
	 *            The node where the import belongs to.
	 * @return The project name or null if not existent.
	 */
	private String extractProjectNameSingleNode(AntTaskNode importedFromNode) {
		/*
		 * Retrieve correct file: An AntIncludeNode has the properties fFile and fFilePath. It seems, that the "fFile" property holds the path where
		 * the actual build-file is located (desired). But the "fFilePath" property points to another location (enclosing include-file?). Which file
		 * is retrieved after calling getIFile(), depends on the isExternal property. If it is set to false, the desired file is returned.
		 *
		 * => The property isExternal is temporary set to false and restored after the file could be retrieved. This should guarantee the correctness
		 * of any subsequent operation on the AntIncludeNode object.
		 */
		IFile projectSpecificBuildFile = this.handleCorrectBuildFile(importedFromNode);
		if (projectSpecificBuildFile != null) {
			return org.eclipse.ant.internal.ui.editor.utils.ProjectHelper.getProjectNameOfBuildFile(projectSpecificBuildFile);
		}
		return org.eclipse.ant.internal.ui.editor.utils.ProjectHelper.getProjectNameOfBuildFile(this.getFilePath());
	}

	/**
	 * If the isExternal-property is set to true, the wrong file is returned (the parent-file of the include?). To avoid this, the isExternal-property
	 * is temporary set to false and restore afterwards to guarantee correct subsequent processing.
	 *
	 * @param importedFromNode
	 *            The importedNode.
	 * @return The correct file.
	 */
	private IFile handleCorrectBuildFile(AntTaskNode importedFromNode) {
		Boolean isExternal = null;
		try {// Save property.
			isExternal = importedFromNode.isExternal();
			// External needs to be false? Otherwise the wrong file gets loaded...
			importedFromNode.setExternal(false);
			// Retrieve correct file.
			return importedFromNode.getIFile();
		}
		finally {
			// Restore the isExternal property after returning the correct file.
			importedFromNode.setExternal(isExternal);
		}
	}
}
