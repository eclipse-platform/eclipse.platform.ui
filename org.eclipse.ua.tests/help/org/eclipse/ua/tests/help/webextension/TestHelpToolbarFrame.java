/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webextension;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.webapp.AbstractFrame;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class TestHelpToolbarFrame extends AbstractFrame {

	public String getName() {
		return "testHelpToolbarFrame";
	}

	public String getURL() {
		return "/titlesearch/jsp/advanced/testHelpToolbarFrame.jsp";
	}
	
	public int getLocation() {
		return AbstractFrame.HELP_TOOLBAR;
	}
	
	public String getSize() {
		return "*";
	}
	
    public String getFrameAttributes() {
    	return "\"marginwidth=\"0\" marginheight=\"0\" frameborder=\"0\" scrolling=\"no\""; //$NON-NLS-1$
    }
	
	public boolean isVisible() {
		return Platform.getPreferencesService().getBoolean
	    (UserAssistanceTestPlugin.getPluginId(), "extraFrame", false, null);
	}

}
