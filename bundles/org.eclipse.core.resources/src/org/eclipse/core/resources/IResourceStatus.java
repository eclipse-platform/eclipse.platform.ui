/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * Represents status related to resources in the Resources plug-in and
 * defines the relevant status code constants.
 * Status objects created by the Resources plug-in bear its unique id
 * (<code>ResourcesPlugin.PI_RESOURCES</code> and one of
 * these status codes.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.core.runtime.IStatus
 * @see ResourcesPlugin#PI_RESOURCES
 */
public interface IResourceStatus extends IStatus {

	/*
	 * Status code definitions
	 */

	// General constants [0-98]
	// Information Only [0-32]

	// Warnings [33-65]
	/** Status code constant (value 32) indicating that a given 
	 * nature set does not satisfy its constraints.
 	 * Severity: warning. Category: general.
 	 */
	public static final int INVALID_NATURE_SET = 35;

	// Errors [66-98]

	 
	/** Status code constant (value 75) indicating that a builder failed.
 	 * Severity: error. Category: general.
 	 */
	public static final int BUILD_FAILED = 75;
	
	/** Status code constant (value 76) indicating that an operation failed.
 	 * Severity: error. Category: general.
 	 */
	public static final int OPERATION_FAILED = 76;
	
	/** Status code constant (value 77) indicating an invalid value.
 	 * Severity: error. Category: general.
 	 */
	public static final int INVALID_VALUE = 77;

	// Local file system constants [200-298]
	// Information Only [200-232]

	// Warnings [233-265]

	/** Status code constant (value 234) indicating that a project
	 * description file (.project), was missing but it has been repaired.
 	 * Severity: warning. Category: local file system.
 	 */
	public static final int MISSING_DESCRIPTION_REPAIRED = 234;

	// Errors [266-298]
	
	/** Status code constant (value 268) indicating a resource unexpectedly 
	 * exists on the local file system.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int EXISTS_LOCAL = 268;
	
	/** Status code constant (value 269) indicating a resource unexpectedly 
	 * does not exist on the local file system.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int NOT_FOUND_LOCAL = 269;
	
	/** Status code constant (value 270) indicating the local file system location for
	 * a resource could not be computed. 
 	 * Severity: error. Category: local file system.
 	 */
	public static final int NO_LOCATION_LOCAL = 270;
	
	/** Status code constant (value 271) indicating an error occurred while
	 * reading part of a resource from the local file system.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int FAILED_READ_LOCAL = 271;
	
	/** Status code constant (value 272) indicating an error occurred while
	 * writing part of a resource to a the local file system.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int FAILED_WRITE_LOCAL = 272;
	
	/** Status code constant (value 273) indicating an error occurred while
	 * deleting a resource from a the local file system.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int FAILED_DELETE_LOCAL = 273;
	
	/** Status code constant (value 274) indicating the workspace view of
	 * the resource differs from that of the local file system.  The requested
	 * operation has been aborted to prevent the possible loss of data.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int OUT_OF_SYNC_LOCAL = 274;

	/** Status code constant (value 275) indicating this file system is not case
	 * sensitive and a resource that differs only in case unexpectedly exists on 
	 * the local file system.
 	 * Severity: error. Category: local file system.
 	 */
	public static final int CASE_VARIANT_EXISTS = 275;

	// Workspace constants [300-398]
	// Information Only [300-332]

	// Warnings [333-365]

	// Errors [366-398]
	
	/** Status code constant (value 366) indicating a resource exists in the
	 * workspace but is not of the expected type.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int RESOURCE_WRONG_TYPE = 366;

	/** Status code constant (value 367) indicating a resource unexpectedly 
	 * exists in the workspace.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int RESOURCE_EXISTS = 367;
	
	/** Status code constant (value 368) indicating a resource unexpectedly 
	 * does not exist in the workspace.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int RESOURCE_NOT_FOUND = 368;
	
	/** Status code constant (value 369) indicating a resource unexpectedly 
	 * does not have content local to the workspace.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int RESOURCE_NOT_LOCAL = 369;
	
	/** Status code constant (value 370) indicating a workspace
	 * is unexpectedly closed.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int WORKSPACE_NOT_OPEN = 370;
	
	/** Status code constant (value 372) indicating a project is
	 * unexpectedly closed.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int PROJECT_NOT_OPEN = 372;
	
	/** Status code constant (value 374) indicating that the path
	 * of a resource being created is occupied by an existing resource
	 * of a different type.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int PATH_OCCUPIED = 374;

	/** Status code constant (value 375) indicating that the sync partner
	 * is not registered with the workspace synchronizer.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int PARTNER_NOT_REGISTERED = 375;

	/** Status code constant (value 376) indicating a marker unexpectedly 
	 * does not exist in the workspace tree.
 	 * Severity: error. Category: workspace.
 	 */
	public static final int MARKER_NOT_FOUND = 376;
		
	// Internal constants [500-598]
	// Information Only [500-532]

	// Warnings [533-565]

	// Errors [566-598]
	
	/** Status code constant (value 566) indicating an error internal to the
	 * platform has occurred.
 	 * Severity: error. Category: internal.
 	 */
	public static final int INTERNAL_ERROR = 566;
	
	/** Status code constant (value 567) indicating the platform could not read
	 * some of its metadata.
 	 * Severity: error. Category: internal.
 	 */
	public static final int FAILED_READ_METADATA = 567;
	
	/** Status code constant (value 568) indicating the platform could not write
	 * some of its metadata.
 	 * Severity: error. Category: internal.
 	 */
	public static final int FAILED_WRITE_METADATA = 568;
	
	/** Status code constant (value 569) indicating the platform could not delete
	 * some of its metadata.
 	 * Severity: error. Category: internal.
 	 */
	public static final int FAILED_DELETE_METADATA = 569;
/**
 * Returns the path of the resource associated with this status.
 *
 * @return the path of the resource related to this status
 */
public IPath getPath();
}
