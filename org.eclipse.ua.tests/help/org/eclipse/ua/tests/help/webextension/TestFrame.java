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

import org.eclipse.help.webapp.AbstractFrame;

public class TestFrame extends AbstractFrame {

	public String getName() {
		return "testFrame";
	}

	public String getURL() {
		return "/titlesearch/jsp/advanced/testFrame.jsp";
	}
	
	public String getSize() {
		return "24";
	}

}
