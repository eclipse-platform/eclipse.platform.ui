/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

public class Policy {
	public static final String DEBUG_FLAG = "/debug"; //$NON-NLS-1$
	public static final String DEBUG_CMDS_FLAG = "/trace/commands"; //$NON-NLS-1$
	public static final String DEBUG_CONTEXTS_FLAG = "/trace/eclipse.context"; //$NON-NLS-1$
	public static final String DEBUG_MENUS_FLAG = "/trace/menus"; //$NON-NLS-1$
	public static final String DEBUG_RENDERER_FLAG = "/trace/renderer"; //$NON-NLS-1$
	public static final String DEBUG_WORKBENCH_FLAG = "/trace/workbench"; //$NON-NLS-1$

	/***/
	public static boolean DEBUG;
	/***/
	public static boolean DEBUG_MENUS;
	/***/
	public static boolean DEBUG_CMDS;
	/***/
	public static boolean DEBUG_CONTEXTS;
	/***/
	public static boolean DEBUG_RENDERER;
	/***/
	public static boolean DEBUG_WORKBENCH;
}
