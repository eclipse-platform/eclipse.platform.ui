/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 424986 (Documentation)
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

/**
 * Contribute a creator for a certain type of {@link EClass}. The types
 * supported by this creator are determined by querying
 * {@link #isSupported(EClass)}.
 */
public interface IContributionClassCreator {
	/**
	 * Called to determine if the EClass is supported by the contribution
	 *
	 * @param element
	 * @return <code>true</code> if supported
	 */
	public boolean isSupported(EClass element);

	/**
	 * Create and open editor for the model element. Called on the supported
	 * creation of a new model element.
	 *
	 * @param contribution
	 *            the {@link MContribution} element to be created and
	 *            subsequently opened in the editor
	 * @param domain
	 * @param project
	 * @param shell
	 */
	public void createOpen(MContribution contribution, EditingDomain domain, IProject project, Shell shell);
}
