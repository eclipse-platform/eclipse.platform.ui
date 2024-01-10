/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation (adapted from JDT's SWTContextType)
 *******************************************************************************/
package org.eclipse.e4.internal.tools.jdt.templates;

import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.IJavaContext;

/**
 * The context type for templates inside e4 code. The same class is used for
 * several context types:
 * <ul>
 * <li>templates for all Java code locations</li>
 * <li>templates for member locations</li>
 * <li>templates for statement locations</li>
 * </ul>
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
	protected void initializeContext(IJavaContext context) {
		if (!getId().equals(E4ContextType.ID_ALL)) { // a specific context must also allow the templates that work
			// everywhere
			context.addCompatibleContextType(E4ContextType.ID_ALL);
		}
	}
}
