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
package org.eclipse.team.internal.ui;

import org.eclipse.core.resources.IResource;

/**
 * Input to a confirm prompt
 * 
 * @see PromptingDialog
 */
public interface IPromptCondition {
	/**
	 * Answers <code>true</code> if a prompt is required for this resource and
	 * false otherwise.
	 */
	public boolean needsPrompt(IResource resource);
	
	/**
	 * Answers the message to include in the prompt.
	 */
	public String promptMessage(IResource resource);
}
