/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.templates;

import org.eclipse.jface.text.templates.ContextType;
import org.eclipse.jface.text.templates.GlobalVariables;


/**
 * A very simple context type.
 */
public class BuildFileContextType extends ContextType {

	/** This context's id */
	public static final String BUILDFILE_CONTEXT_TYPE= "org.eclipse.ant.ui.templateContextType.buildFile"; //$NON-NLS-1$

	/**
	 * Creates a new XML context type. 
	 */
	public BuildFileContextType() {
		addGlobalResolvers();
	}

	private void addGlobalResolvers() {
		addResolver(new GlobalVariables.Cursor());
		addResolver(new GlobalVariables.WordSelection());
		addResolver(new GlobalVariables.LineSelection());
		addResolver(new GlobalVariables.Dollar());
		addResolver(new GlobalVariables.Date());
		addResolver(new GlobalVariables.Year());
		addResolver(new GlobalVariables.Time());
		addResolver(new GlobalVariables.User());
	}
}
