/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IMarker;

/**
 * This interface defines constants used for marker properties
 * and attributes.
 *
 * @see IMarker
 */
interface IMarkerConstants {
    public static final String PREFIX = "org.eclipse.ui.tasklist."; //$NON-NLS-1$

    public static final String P_PRIORITY_IMAGE = PREFIX + "priorityImage"; //$NON-NLS-1$

    public static final String P_COMPLETE_IMAGE = PREFIX + "completeImage"; //$NON-NLS-1$

    public static final String P_RESOURCE_NAME = PREFIX + "resourceName"; //$NON-NLS-1$

    public static final String P_CONTAINER_NAME = PREFIX + "containerName"; //$NON-NLS-1$

    public static final String P_LINE_AND_LOCATION = PREFIX + "lineAndLocation"; //$NON-NLS-1$
}