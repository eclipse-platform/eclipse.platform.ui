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
package org.eclipse.help.internal.webapp.data;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Control for a toolbar.
 */
public class ToolbarData extends RequestData {

	ToolbarButton[] buttons;

	public ToolbarData(
		ServletContext context,
		HttpServletRequest request,
		HttpServletResponse response) {
		super(context, request, response);
		loadButtons();
	}

	private void loadButtons() {
		String[] names = request.getParameterValues("name");
		String[] tooltips = request.getParameterValues("tooltip");
		String[] images = request.getParameterValues("image");
		String[] actions = request.getParameterValues("action");
		String[] states = request.getParameterValues("state");

		if (names == null
			|| tooltips == null
			|| images == null
			|| actions == null
			|| states == null
			|| names.length != tooltips.length
			|| names.length != images.length
			|| names.length != actions.length
			|| names.length != states.length) {
			buttons = new ToolbarButton[0];
			return;
		}

		List buttonList = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			if(states[i].startsWith("hid")){
				continue;
			}
			if ("".equals(names[i]))
				buttonList.add(new ToolbarButton());
			else
				buttonList.add(
					new ToolbarButton(
						names[i],
						ServletResources.getString(tooltips[i], request),
						preferences.getImagesDirectory() + "/" + images[i],
						actions[i],
						"on".equalsIgnoreCase(states[i])));
		}
		// add implicit maximize/restore button on all toolbars
		if (isIE() || isMozilla()
				&& "1.2.1".compareTo(getMozillaVersion()) <= 0) {
			buttonList.add(new ToolbarButton("maximize_restore",
					"",
					preferences.getImagesDirectory() + "/" + "maximize.gif",
					"restore_maximize", false));
		}
		buttons = (ToolbarButton[])buttonList.toArray(new ToolbarButton[buttonList.size()]);
	}

	public ToolbarButton[] getButtons() {
		return buttons;
	}

	public String getName() {
		if (request.getParameter("view") == null)
			return "";
		else
			return request.getParameter("view");
	}

	public String getTitle() {
		if (request.getParameter("view") == null)
			return "";
		else
			return ServletResources.getString(
				request.getParameter("view"),
				request);
	}

	public String getScript() {
		return request.getParameter("script");
	}
	public String getMaximizeImage() {
		return preferences.getImagesDirectory() + "/e_maximize.gif";
	}
	public String getRestoreImage() {
		return preferences.getImagesDirectory() + "/e_restore.gif";
	}
}
