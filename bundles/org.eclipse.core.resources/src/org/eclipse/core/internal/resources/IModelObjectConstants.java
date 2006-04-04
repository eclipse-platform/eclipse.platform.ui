/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

public interface IModelObjectConstants {
	public static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
	public static final String AUTOBUILD = "autobuild"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "buildCommand"; //$NON-NLS-1$
	public static final String BUILD_ORDER = "buildOrder"; //$NON-NLS-1$
	public static final String BUILD_SPEC = "buildSpec"; //$NON-NLS-1$
	public static final String BUILD_TRIGGERS = "triggers"; //$NON-NLS-1$
	public static final String TRIGGER_AUTO = "auto"; //$NON-NLS-1$
	public static final String TRIGGER_CLEAN = "clean"; //$NON-NLS-1$
	public static final String TRIGGER_FULL = "full"; //$NON-NLS-1$
	public static final String TRIGGER_INCREMENTAL = "incremental"; //$NON-NLS-1$
	public static final String COMMENT = "comment"; //$NON-NLS-1$
	public static final String DICTIONARY = "dictionary"; //$NON-NLS-1$
	public static final String FILE_STATE_LONGEVITY = "fileStateLongevity"; //$NON-NLS-1$
	public static final String KEY = "key"; //$NON-NLS-1$
	public static final String LOCATION = "location"; //$NON-NLS-1$
	public static final String LOCATION_URI = "locationURI"; //$NON-NLS-1$
	public static final String MAX_FILE_STATE_SIZE = "maxFileStateSize"; //$NON-NLS-1$
	public static final String MAX_FILE_STATES = "maxFileStates"; //$NON-NLS-1$
	/**
	 * The project relative path is called the link name for backwards compatibility
	 */
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String NATURE = "nature"; //$NON-NLS-1$
	public static final String NATURES = "natures"; //$NON-NLS-1$
	public static final String SNAPSHOT_INTERVAL = "snapshotInterval"; //$NON-NLS-1$
	public static final String PROJECT = "project"; //$NON-NLS-1$
	public static final String PROJECT_DESCRIPTION = "projectDescription"; //$NON-NLS-1$
	public static final String PROJECTS = "projects"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String WORKSPACE_DESCRIPTION = "workspaceDescription"; //$NON-NLS-1$
	public static final String LINKED_RESOURCES = "linkedResources"; //$NON-NLS-1$
	public static final String LINK = "link"; //$NON-NLS-1$
}
