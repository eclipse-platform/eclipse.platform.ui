/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Property tester for the 'refactoringPropertiesEnabled' property.
 * 
 * @since 3.3
 */
public final class RefactoringPropertyPageTester extends PropertyTester {

	/** The property name */
	public static final String PROPERTY_NAME= "refactoringPropertiesEnabled"; //$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 */
	public boolean test(Object receiver, String property, Object[] arguments, Object expected) {
		if (PROPERTY_NAME.equals(property)) {
			if (receiver instanceof IAdaptable) {
				final IAdaptable adaptable= (IAdaptable) receiver;
				final IResource resource= (IResource) adaptable.getAdapter(IResource.class);
				if (resource instanceof IProject) {
					final IProject project= (IProject) resource;
					return project.isAccessible();
				}
			}
		}
		return false;
	}
}