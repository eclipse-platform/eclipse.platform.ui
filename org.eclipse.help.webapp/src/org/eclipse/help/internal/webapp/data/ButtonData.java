/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.webapp.AbstractButton;

public class ButtonData extends RequestData {
	
	private static final String BUTTON_EXTENSION_POINT = "org.eclipse.help.webapp.toolbarButton"; //$NON-NLS-1$
	private List allButtons;

	public ButtonData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
	}
	
	public AbstractButton[] getButtons() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor(BUTTON_EXTENSION_POINT);
		if (allButtons == null) {
			allButtons = new ArrayList();
			for (int i = 0; i < elements.length; i++) {
				Object obj = null;
				try {
					obj = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					HelpWebappPlugin.logError("Create extension failed:[" //$NON-NLS-1$
							+ BUTTON_EXTENSION_POINT + "].", e); //$NON-NLS-1$
				}
				if (obj instanceof AbstractButton) {
					allButtons.add(obj);
				}
			}
			Collections.sort(allButtons);
		}

		List buttonList = new ArrayList();
		for (Iterator iter = allButtons.iterator(); iter.hasNext();) {
			AbstractButton button = (AbstractButton) iter.next();
			//if (button.isVisible() && button.getLocation() == location) {
				buttonList.add(button);
			//}
		}			
		AbstractButton[] buttons = (AbstractButton[]) buttonList.toArray(new AbstractButton[buttonList.size()]);
		return buttons;		
	}
	
	public String getImageUrl(AbstractButton button) {
		return request.getContextPath() + button.getImageURL();
	}

}
