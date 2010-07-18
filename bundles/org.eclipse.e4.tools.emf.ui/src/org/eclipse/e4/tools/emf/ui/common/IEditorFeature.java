/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface IEditorFeature {
	public class FeatureClass {
		public final String label;
		public final EClass eClass;

		public FeatureClass(String label, EClass eClass) {
			this.label = label;
			this.eClass = eClass;
		}
	}

	public List<FeatureClass> getFeatureClasses(EClass eClass, EStructuralFeature feature);
}
