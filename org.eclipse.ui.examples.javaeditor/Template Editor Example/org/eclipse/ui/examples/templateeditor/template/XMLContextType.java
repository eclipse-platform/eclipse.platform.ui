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
package org.eclipse.ui.examples.templateeditor.template;

import org.eclipse.jface.text.templates.ContextType;
import org.eclipse.jface.text.templates.GlobalVariables;


/**
 * A very simple context type.
 */
public class XMLContextType extends ContextType {

	/** This context's id */
	public static final String XML_CONTEXT_TYPE= "org.eclipse.ui.examples.templateeditor.xml"; //$NON-NLS-1$

	/**
	 * Creates a new XML context type. 
	 */
	public XMLContextType() {
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
