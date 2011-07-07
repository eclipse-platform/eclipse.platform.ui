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
