package org.eclipse.ui.externaltools.internal.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

public class BuildTypeExpander extends DefaultVariableExpander {

	public String getText(String varTag, String varValue, ExpandVariableContext context) {
		return context.getBuildType();
	}
}
