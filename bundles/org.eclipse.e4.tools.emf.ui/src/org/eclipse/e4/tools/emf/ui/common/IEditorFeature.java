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

import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Contribute features for reference in the application model.
 */
public interface IEditorFeature {
	public class FeatureClass {
		public final String label;
		public final EClass eClass;

		public FeatureClass(String label, EClass eClass) {
			this.label = label;
			this.eClass = eClass;
		}
	}

	/**
	 * Return a list of {@link FeatureClass} elements, where
	 * {@link FeatureClass#eClass} must be castable to <b>eClass</b> and will be
	 * stored in <b>feature</b>.
	 * 
	 * @param eClass
	 *            your contribution must be castable to
	 * @param feature
	 *            your contributions storage
	 *            {@link org.eclipse.emf.ecore.EReference} target
	 * @return the list of selectable alternatives for storage in <b>feature</b>
	 */
	public List<FeatureClass> getFeatureClasses(EClass eClass, EStructuralFeature feature);
}
