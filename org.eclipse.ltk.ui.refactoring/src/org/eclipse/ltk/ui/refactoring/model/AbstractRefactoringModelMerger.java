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
 * Partial implementation of a refactoring-aware resource mapping merger.
 * <p>
 * This class provides support to determine incoming refactorings during model
 * merging and model update, and displays a refactoring wizard to apply the
 * refactorings to the local workspace.
 * </p>
 * <p>
 * Note: this class is designed to be extended by clients. Programming language
 * implementers which need a refactoring-aware resource mapping merger to
 * associate with their model provider may extend this class to implement
 * language-specific project dependency rules.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see org.eclipse.team.core.mapping.IResourceMappingMerger
 * 
 * @since 3.2
 */
public abstract class AbstractRefactoringModelMerger extends DefaultResourceMappingMerger {

	/**
	 * Creates a new abstract refactoring model merger.
	 * 
	 * @param provider
	 *            the associated model provider
	 */
	protected AbstractRefactoringModelMerger(final ModelProvider provider) {
		super(provider);
	}

	/**
	 * Returns the dependent projects of the projects associated with the
	 * incoming refactorings.
	 * <p>
	 * Subclasses must implement this method to return the dependent projects
	 * according to the semantics of the associated programming language. The
	 * result of this method is used to decide whether the resource mapping
	 * merger should execute the incoming refactorings in order to fix up
	 * references in dependent projects.
	 * </p>
	 * 
	 * @param projects
	 *            the projects associated with the incoming refactorings in the
	 *            synchronization scope.
	 * @return the dependent projects, or an empty array
	 */
	protected abstract IProject[] getDependencies(IProject[] projects);
}