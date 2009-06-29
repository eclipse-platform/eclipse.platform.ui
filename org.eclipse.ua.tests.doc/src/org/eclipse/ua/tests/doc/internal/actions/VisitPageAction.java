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

package org.eclipse.ua.tests.doc.internal.actions;

import org.eclipse.help.ILiveHelpAction;

public class VisitPageAction implements ILiveHelpAction {
	
	public static String lastPageVisited = "NO_PAGES_VISITED";
	
	public void setInitializationString(String data) {
		lastPageVisited = data;
	}

	public void run() {
	}

}
