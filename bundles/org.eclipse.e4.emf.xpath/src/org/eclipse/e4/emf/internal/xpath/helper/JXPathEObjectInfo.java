/*******************************************************************************
 * Copyright (c) 2010, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath.helper;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

public class JXPathEObjectInfo {
	private final EClass eClass;

	public JXPathEObjectInfo(EClass eClass) {
		this.eClass = eClass;
	}

	public EStructuralFeature[] getPropertyDescriptors() {
		return eClass.getEAllStructuralFeatures().toArray(new EStructuralFeature[0]);
	}

	public EStructuralFeature getPropertyDescriptor(String propertyName) {
		return eClass.getEStructuralFeature(propertyName);
	}

	public boolean isAtomic() {
		return false;
	}
}
