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

import org.eclipse.emf.ecore.EClass;

/**
 * Contribute an editor for an application model element
 */
public interface IEditorDescriptor {
	/**
	 * @return supported model element
	 */
	public EClass getEClass();

	/**
	 * @return a class extending
	 *         {@link org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor}
	 *         capable of editing class returned in {@link #getEClass()}
	 */
	public Class<?> getEditorClass();
}
