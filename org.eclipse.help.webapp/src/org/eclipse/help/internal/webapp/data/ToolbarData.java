/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.webapp.AbstractButton;

/**
 * Control for a toolbar.
 */
public class ToolbarData extends RequestData {

	private static final String BUTTON_EXTENSION_POINT = "org.eclipse.help.webapp.toolbarButton"; //$NON-NLS-1$
	private ToolbarButton[] buttons;
	private String[] scriptFiles;
	

	public ToolbarData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		loadButtons();
	}

	/*
	 * Returns whether or not this toolbar has a menu button (has an arrow with drop
	 * down menu).
	 */
	public boolean hasMenu() {
		for (int i=0;i<buttons.length;++i) {
			if ("menu".equals(buttons[i].getAction())) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
	
	private void loadButtons() {
		String[] names = request.getParameterValues("name"); //$NON-NLS-1$
		String[] tooltips = request.getParameterValues("tooltip"); //$NON-NLS-1$
		String[] images = request.getParameterValues("image"); //$NON-NLS-1$
		String[] actions = request.getParameterValues("action"); //$NON-NLS-1$
		String[] params = request.getParameterValues("param"); //$NON-NLS-1$
		String[] states = request.getParameterValues("state"); //$NON-NLS-1$

		if (names == null || tooltips == null || images == null
				|| actions == null || params == null || states == null
				|| names.length != tooltips.length
				|| names.length != images.length
				|| names.length != actions.length
				|| names.length != params.length
				|| names.length != states.length) {
			buttons = new ToolbarButton[0];
			scriptFiles = new String[0];
			return;
		}

		List buttonList = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			if ("".equals(names[i])) //$NON-NLS-1$
				buttonList.add(new ToolbarButton());
			else
				buttonList.add(new ToolbarButton(names[i], ServletResources
						.getString(tooltips[i], request), preferences
						.getImagesDirectory()
						+ "/e_" + images[i], //$NON-NLS-1$
						actions[i], params[i], states[i]));
		}
		
		addExtensionButtons(buttonList);
		
		// add implicit maximize/restore button on all toolbars
		if (isIE() || isMozilla()
				&& "1.2.1".compareTo(getMozillaVersion()) <= 0 //$NON-NLS-1$
				|| (isSafari() && "120".compareTo(getSafariVersion()) <= 0)) { //$NON-NLS-1$
			buttonList.add(new ToolbarButton("maximize_restore", //$NON-NLS-1$
					getMaximizeTooltip(), preferences.getImagesDirectory()
							+ "/" + "maximize.gif", //$NON-NLS-1$ //$NON-NLS-2$
					"restore_maximize", null, "off")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		buttons = (ToolbarButton[]) buttonList
				.toArray(new ToolbarButton[buttonList.size()]);
	}

	private void addExtensionButtons(List buttonList) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor(BUTTON_EXTENSION_POINT);
		
		List extensionButtons = new ArrayList();
		List scripts = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			Object obj = null;
			try {
				obj = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				HelpWebappPlugin.logError("Create extension failed:[" //$NON-NLS-1$
						+ BUTTON_EXTENSION_POINT + "].", e); //$NON-NLS-1$
			}
			if (obj instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) obj;
				String toolbarName = request.getParameter("view"); //$NON-NLS-1$
				if (toolbarName == null)
				{
					toolbarName = request.getParameter("toolbar"); //$NON-NLS-1$
				}
				if (button.isAddedToToolbar(toolbarName)) {	
				    extensionButtons.add(button);
				}
			}
		}
		
		Collections.sort(extensionButtons);

		for (Iterator iter = extensionButtons.iterator(); iter.hasNext();) {
			AbstractButton button = (AbstractButton) iter.next();
			String scriptFile = button.getJavaScriptURL();
			if (scriptFile != null) {
				scripts.add(UrlUtil.getRelativePath(request, scriptFile));
			}
			ToolbarButton toolButton = new ToolbarButton(button.getId(), 
					button.getTooltip(UrlUtil.getLocaleObj(request, response)),
					request.getContextPath() + button.getImageURL(), 
					button.getAction(),
					"", //$NON-NLS-1$
					button.getState());
			 buttonList.add(toolButton);
		}	
		scriptFiles = (String[]) scripts.toArray(new String[scripts.size()]);
	}

	public ToolbarButton[] getButtons() {
		return buttons;
	}

	public String getName() {
		if (request.getParameter("view") == null) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		return request.getParameter("view"); //$NON-NLS-1$
	}

	public String getTitle() {
		if (request.getParameter("view") == null) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
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
	
	public String[] getScriptFiles() {
		return scriptFiles;
	}
}
