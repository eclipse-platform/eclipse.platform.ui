/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.debug.model;

import org.eclipse.ant.internal.ui.launchConfigurations.MessageIds;

public class DebugMessageIds extends MessageIds {
	
	public final static String MESSAGE_DELIMITER= ","; //$NON-NLS-1$
	
	public final static String BUILD_STARTED=   "build_started"; //$NON-NLS-1$
	public final static String BUILD_FINISHED= "build_finished"; //$NON-NLS-1$
	public final static String TARGET_STARTED= "target_started"; //$NON-NLS-1$
	public final static String TARGET_FINISHED= "target_finished"; //$NON-NLS-1$
	public final static String TASK_STARTED= "task_started"; //$NON-NLS-1$
	public final static String TASK_FINISHED= "task_finished"; //$NON-NLS-1$
	
	public final static String STEP= "step"; //$NON-NLS-1$
	public final static String STEP_OVER= "step_over"; //$NON-NLS-1$
	public final static String STEP_INTO= "step_into"; //$NON-NLS-1$
	
	public final static String TERMINATE= "terminate"; //$NON-NLS-1$
	public final static String TERMINATED= "terminated"; //$NON-NLS-1$
	
	public final static String SUSPEND= "suspend"; //$NON-NLS-1$
	public final static String SUSPENDED= "suspended"; //$NON-NLS-1$
	
	public final static String RESUME= "resume"; //$NON-NLS-1$
	public final static String RESUMED= "resumed"; //$NON-NLS-1$
	
	public final static String STACK= "stack"; //$NON-NLS-1$
	
	public final static String ADD_BREAKPOINT= "add"; //$NON-NLS-1$
	public final static String REMOVE_BREAKPOINT= "remove"; //$NON-NLS-1$
	
	public final static String CLIENT_REQUEST= "client"; //$NON-NLS-1$
	public final static String BREAKPOINT= "breakpoint"; //$NON-NLS-1$
	
	public final static String PROPERTIES= "prop"; //$NON-NLS-1$
	public final static String PROPERTY_VALUE= "value"; //$NON-NLS-1$
}
