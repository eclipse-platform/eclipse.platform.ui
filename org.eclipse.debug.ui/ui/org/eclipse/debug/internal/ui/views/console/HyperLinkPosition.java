package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.text.Position;

/**
 */
public class HyperLinkPosition extends Position {
	
	public static final String HYPER_LINK_CATEGORY = DebugUIPlugin.getUniqueIdentifier() + ".HYPER_LINK";
	
	private IConsoleHyperLink fLink = null;

	/**
	 * 
	 */
	public HyperLinkPosition(IConsoleHyperLink link) {
		super(link.getOffset(), link.getLength());
		fLink = link;
	}
	
	public IConsoleHyperLink getHyperLink() {
		return fLink;
	}

}
