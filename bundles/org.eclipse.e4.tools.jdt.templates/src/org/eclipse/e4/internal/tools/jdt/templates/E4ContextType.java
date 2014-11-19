/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation (adapted from JDT's SWTContextType)
 *******************************************************************************/
package org.eclipse.e4.internal.tools.jdt.templates;

import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;

/**
 * The context type for templates inside e4 code.
 * The same class is used for several context types:
 * <dl>
 * <li>templates for all Java code locations</li>
 * <li>templates for member locations</li>
 * <li>templates for statement locations</li>
 * </dl>
 */
@SuppressWarnings("restriction")
public class E4ContextType extends AbstractJavaContextType {

	/**
	 * The context type id for templates working on all Java code locations in e4 projects
	 */
	public static final String ID_ALL = "e4"; //$NON-NLS-1$

	/**
	 * The context type id for templates working on member locations in e4 projects
	 */
	public static final String ID_MEMBERS = "e4-members"; //$NON-NLS-1$

	/**
	 * The context type id for templates working on statement locations in e4 projects
	 */
	public static final String ID_STATEMENTS = "e4-statements"; //$NON-NLS-1$

	@Override
	protected void initializeContext(JavaContext context) {
		if (!getId().equals(E4ContextType.ID_ALL)) { // a specific context must also allow the templates that work
			// everywhere
			context.addCompatibleContextType(E4ContextType.ID_ALL);
		}
	}
}
