/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Control for a toolbar.
 */
public class ToolbarData extends RequestData {

	ToolbarButton[] buttons;

	public ToolbarData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		loadButtons();
	}

	private void loadButtons() {
		String[] names = request.getParameterValues("name"); //$NON-NLS-1$
		String[] tooltips = request.getParameterValues("tooltip"); //$NON-NLS-1$
		String[] images = request.getParameterValues("image"); //$NON-NLS-1$
		String[] actions = request.getParameterValues("action"); //$NON-NLS-1$
		String[] states = request.getParameterValues("state"); //$NON-NLS-1$

		if (names == null || tooltips == null || images == null
				|| actions == null || states == null
				|| names.length != tooltips.length
				|| names.length != images.length
				|| names.length != actions.length
				|| names.length != states.length) {
			buttons = new ToolbarButton[0];
			return;
		}

		List buttonList = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			if (states[i].startsWith("hid")) { //$NON-NLS-1$
				continue;
			}
			if ("".equals(names[i])) //$NON-NLS-1$
				buttonList.add(new ToolbarButton());
			else
				buttonList.add(new ToolbarButton(names[i], ServletResources
						.getString(tooltips[i], request), preferences
						.getImagesDirectory()
						+ "/" + images[i], //$NON-NLS-1$
						actions[i], "on".equalsIgnoreCase(states[i]))); //$NON-NLS-1$
		}
		// add implicit maximize/restore button on all toolbars
		if (isIE() || isMozilla()
				&& "1.2.1".compareTo(getMozillaVersion()) <= 0 //$NON-NLS-1$
				|| (isSafari() && "120".compareTo(getSafariVersion()) <= 0)) { //$NON-NLS-1$
			buttonList.add(new ToolbarButton("maximize_restore", //$NON-NLS-1$
					getMaximizeTooltip(), preferences.getImagesDirectory()
							+ "/" + "maximize.gif", //$NON-NLS-1$ //$NON-NLS-2$
					"restore_maximize", false)); //$NON-NLS-1$
		}
		buttons = (ToolbarButton[]) buttonList
				.toArray(new ToolbarButton[buttonList.size()]);
	}

	public ToolbarButton[] getButtons() {
		return buttons;
	}

	public String getName() {
		if (request.getParameter("view") == null) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		else
			return request.getParameter("view"); //$NON-NLS-1$
	}

	public String getTitle() {
		if (request.getParameter("view") == null) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		else
			return ServletResources.getString(request.getParameter("view"), //$NON-NLS-1$
					request);
	}

	public String getScript() {
		return request.getParameter("script"); //$NON-NLS-1$
	}
	public String getMaximizeImage() {
		return preferences.getImagesDirectory() + "/e_maximize.gif"; //$NON-NLS-1$
	}
	public String getRestoreImage() {
		return preferences.getImagesDirectory() + "/e_restore.gif"; //$NON-NLS-1$
	}
	public String getMaximizeTooltip() {
		return ServletResources.getString("maximize", request); //$NON-NLS-1$
	}
	public String getRestoreTooltip() {
		return ServletResources.getString("restore", request); //$NON-NLS-1$
	}
}
