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
package org.eclipse.ui.views.navigator;

import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 */
public class ProjectContentProvider extends WorkbenchContentProvider implements INavigatorContentProvider {
	String id;
	NavigatorContentHandler provider;
	
public void init (NavigatorContentHandler provider, String id) {
		this.provider = provider;
		this.id = id;
	}
}