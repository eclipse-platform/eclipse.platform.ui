/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ui.IPromptCondition;

public class DummyPromptCondition implements IPromptCondition {
	public boolean needsPrompt(IResource resource) {
		return false;
	}
	public String promptMessage(IResource resource) {
		// this method should never be called
		return resource.getName();
	}
}
