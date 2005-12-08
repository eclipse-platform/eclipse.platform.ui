/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.model;

import org.eclipse.team.internal.ui.mapping.DefaultResourceMappingMerger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.ModelProvider;

/**
 * Partial implementation of a refactoring-aware logical model merger.
 * <p>
 * Note: this class is intended to be extended outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public abstract class AbstractRefactoringModelMerger extends DefaultResourceMappingMerger {

	/**
	 * Creates a new abstract refactoring model merger.
	 * 
	 * @param provider
	 *            the model provider
	 */
	protected AbstractRefactoringModelMerger(final ModelProvider provider) {
		super(provider);
	}

	/**
	 * Returns the dependent projects of the specified projects.
	 * <p>
	 * Subclasses must implement this method to return the dependent projects
	 * according to the semantics of the programming language.
	 * </p>
	 * 
	 * @param projects
	 *            the projects
	 * @return the dependent projects, or an empty array
	 */
	protected abstract IProject[] getDependentProjects(IProject[] projects);
}