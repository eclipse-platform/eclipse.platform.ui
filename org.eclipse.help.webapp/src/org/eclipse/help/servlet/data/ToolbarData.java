package org.eclipse.help.servlet.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.servlet.WebappResources;

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
						WebappResources.getString(tooltips[i], request),
						getPrefs().getImagesDirectory() + "/" + images[i],
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
			return WebappResources.getString(request.getParameter("view"), request);
	}
	
	public String getScript() {
		return request.getParameter("script");
	}
}
