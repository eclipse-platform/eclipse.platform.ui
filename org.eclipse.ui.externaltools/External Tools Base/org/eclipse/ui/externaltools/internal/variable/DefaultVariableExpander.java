package org.eclipse.ui.externaltools.internal.variable;

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class DefaultVariableExpander implements IVariableExpander {

	private static DefaultVariableExpander instance;

	public static DefaultVariableExpander getDefault() {
		if (instance == null) {
			instance= new DefaultVariableExpander();
		}
		return instance;
	}

	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) {
		return null;
	}

	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) {
		return null;
	}

	public String getText(String varTag, String varValue, ExpandVariableContext context) {
		return null;
	}

}
