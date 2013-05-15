/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

/**
 * A common facility for parsing the
 * <code>org.eclipse.e4.ui.workbench/.options</code> file.
 * 
 */
public class Policy {
	public static final String DEBUG = "/debug"; //$NON-NLS-1$
	public static final String DEBUG_CMDS = "/trace/commands"; //$NON-NLS-1$
	public static final String DEBUG_MENUS = "/trace/menus"; //$NON-NLS-1$
	public static final String DEBUG_CONTEXTS = "/trace/eclipse.context"; //$NON-NLS-1$
	public static final String DEBUG_CONTEXTS_VERBOSE = "/trace/eclipse.context.verbose"; //$NON-NLS-1$
	public static final String DEBUG_WORKBENCH = "/trace/workbench"; //$NON-NLS-1$
	public static final String DEBUG_RENDERER = "/trace/renderer"; //$NON-NLS-1$
}
