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
package org.eclipse.ui.externaltools.internal.variables;

import org.eclipse.debug.ui.variables.DefaultVariableExpander;
import org.eclipse.debug.ui.variables.ExpandVariableContext;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;


public class BuildTypeExpander extends DefaultVariableExpander {

	public String getText(String varTag, String varValue, ExpandVariableContext context) {
		return ExternalToolBuilder.getBuildType();
	}
}
