package org.eclipse.ui.views.tasklist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.resources.IMarker;

/**
 * This interface defines constants used for marker properties
 * and attributes.
 *
 * @see IMarker
 */
/* package */ interface IMarkerConstants {
	public static final String PREFIX = "org.eclipse.ui.tasklist.";
	public static final String P_PRIORITY_IMAGE = PREFIX+"priorityImage";
	public static final String P_COMPLETE_IMAGE = PREFIX+"completeImage";
	public static final String P_RESOURCE_NAME = PREFIX+"resourceName";
	public static final String P_CONTAINER_NAME = PREFIX+"containerName";
	public static final String P_LINE_AND_LOCATION = PREFIX+"lineAndLocation";
}
