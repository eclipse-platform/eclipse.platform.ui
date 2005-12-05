/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.expressions;

import org.eclipse.core.expressions.IPropertyTester;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Tests the following properties for IResources:
 * 
 * <p>
 * <ul>
 * <li><i>projectNature</i>: Determines if the receiver is an accessible IProject which
 * has as the given value as one of its natures.</li>
 * </ul>
 * </p>
 * 
 * @since 3.2
 * 
 */
public class ResourcePropertyTester extends PropertyTester implements
		IPropertyTester {

	static class Properties {

		public static final int INVALID_PROPERTY = -1;

		public static final String PROJECT_NATURE_LITERAL = "projectNature"; //$NON-NLS-1$

		public static final int PROJECT_NATURE = 0;

		public static int getValue(String aPropertyLiteral) {
			if (PROJECT_NATURE_LITERAL.equals(aPropertyLiteral))
				return PROJECT_NATURE;
			return INVALID_PROPERTY;
		}
	}

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) receiver)
					.getAdapter(IResource.class);
			switch (Properties.getValue(property)) {
			case Properties.PROJECT_NATURE:

				if (resource != null && resource.getType() == IResource.PROJECT) {
					IProject project = ((IProject) resource);
					try {
						return project.isAccessible()
								&& project.hasNature((String) expectedValue);
					} catch (CoreException e) {
						return false;
					}
				}
				break;
			}

		}
		return false;
	}

}
