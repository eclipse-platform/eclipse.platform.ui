/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 202583
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public final class SharedImages {

	private SharedImages() { // do nothing
	}

	public final static String ICONS_PATH = "icons/"; //$NON-NLS-1$

	private static final String PATH_OBJ = ICONS_PATH + "obj16/"; //$NON-NLS-1$
	private static final String PATH_LCL = ICONS_PATH + "elcl16/"; //$NON-NLS-1$
	private static final String PATH_LCL_DISABLED = ICONS_PATH + "dlcl16/"; //$NON-NLS-1$
	private static final String PATH_EVENTS = ICONS_PATH + "eview16/"; //$NON-NLS-1$

	/* Event Details */
	public static final String DESC_PREV_EVENT = PATH_EVENTS + "event_prev.png"; //$NON-NLS-1$
	public static final String DESC_NEXT_EVENT = PATH_EVENTS + "event_next.png"; //$NON-NLS-1$

	public static final String DESC_CLEAR = PATH_LCL + "clear.png"; //$NON-NLS-1$
	public static final String DESC_CLEAR_DISABLED = PATH_LCL_DISABLED + "clear.png"; //$NON-NLS-1$
	public static final String DESC_OPEN_CONSOLE = PATH_LCL + "open_console_obj.png"; //$NON-NLS-1$
	public static final String DESC_REMOVE_LOG = PATH_LCL + "remove.png"; //$NON-NLS-1$
	public static final String DESC_REMOVE_LOG_DISABLED = PATH_LCL_DISABLED + "remove.png"; //$NON-NLS-1$
	public static final String DESC_EXPORT = PATH_LCL + "export_log.png"; //$NON-NLS-1$
	public static final String DESC_EXPORT_DISABLED = PATH_LCL_DISABLED + "export_log.png"; //$NON-NLS-1$
	public static final String DESC_FILTER = PATH_LCL + "filter_ps.png"; //$NON-NLS-1$
	public static final String DESC_FILTER_DISABLED = PATH_LCL_DISABLED + "filter_ps.png"; //$NON-NLS-1$
	public static final String DESC_IMPORT = PATH_LCL + "import_log.png"; //$NON-NLS-1$
	public static final String DESC_IMPORT_DISABLED = PATH_LCL_DISABLED + "import_log.png"; //$NON-NLS-1$
	public static final String DESC_OPEN_LOG = PATH_LCL + "open_log.png"; //$NON-NLS-1$
	public static final String DESC_OPEN_LOG_DISABLED = PATH_LCL_DISABLED + "open_log.png"; //$NON-NLS-1$
	public static final String DESC_PROPERTIES = PATH_LCL + "properties.png"; //$NON-NLS-1$
	public static final String DESC_PROPERTIES_DISABLED = PATH_LCL_DISABLED + "properties.png"; //$NON-NLS-1$
	public static final String DESC_READ_LOG = PATH_LCL + "restore_log.png"; //$NON-NLS-1$
	public static final String DESC_READ_LOG_DISABLED = PATH_LCL_DISABLED + "restore_log.png"; //$NON-NLS-1$

	public static final String DESC_ERROR_ST_OBJ = PATH_OBJ + "error_st_obj.png"; //$NON-NLS-1$
	public static final String DESC_ERROR_STACK_OBJ = PATH_OBJ + "error_stack.png"; //$NON-NLS-1$
	public static final String DESC_INFO_ST_OBJ = PATH_OBJ + "info_st_obj.png"; //$NON-NLS-1$
	public static final String DESC_OK_ST_OBJ = PATH_OBJ + "ok_st_obj.png"; //$NON-NLS-1$
	public static final String DESC_WARNING_ST_OBJ = PATH_OBJ + "warning_st_obj.png"; //$NON-NLS-1$
	public static final String DESC_HIERARCHICAL_LAYOUT_OBJ = PATH_OBJ + "hierarchical.png"; //$NON-NLS-1$

	public static ImageDescriptor getImageDescriptor(String key) {
		return Activator.getDefault().getImageRegistry().getDescriptor(key);
	}

	public static Image getImage(String key) {
		return Activator.getDefault().getImageRegistry().get(key);
	}

}
