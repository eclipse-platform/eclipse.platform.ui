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


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.webapp.servlet.ServletResources;


/**
 * Control for a toolbar.
 */
public class ToolbarData extends RequestData {

	ToolbarButton[] buttons;

	public ToolbarData(ServletContext context, HttpServletRequest request) {
		super(context, request);
		loadButtons();
	}

	private void loadButtons() {
		String[] names = request.getParameterValues("name");
		String[] tooltips = request.getParameterValues("tooltip");
		String[] images = request.getParameterValues("image");
		String[] actions = request.getParameterValues("action");

		if (names == null
			|| tooltips == null
			|| images == null
			|| actions == null
			|| names.length != tooltips.length
			|| names.length != images.length
			|| names.length != actions.length) {
			buttons = new ToolbarButton[0];
			return;
		}

		buttons = new ToolbarButton[names.length];
		for (int i = 0; i < buttons.length; i++) {
			if ("".equals(names[i]))
				buttons[i] = new ToolbarButton();
			else
				buttons[i] =
					new ToolbarButton(
						names[i],
						ServletResources.getString(tooltips[i], request),
						preferences.getImagesDirectory() + "/" + images[i],
						actions[i]);
		}
	}

	public ToolbarButton[] getButtons() {
		return buttons;
	}
	
	public String getTitle() {
		if (request.getParameter("view") == null)
			return "";
		else 
			return ServletResources.getString(request.getParameter("view"), request);
	}
	
	public String getScript() {
		return request.getParameter("script");
	}
}
