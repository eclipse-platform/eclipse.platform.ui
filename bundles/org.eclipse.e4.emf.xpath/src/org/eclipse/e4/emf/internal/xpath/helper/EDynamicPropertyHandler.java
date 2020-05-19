/*******************************************************************************
 * Copyright (c) 2019 itemis AG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Karsten Thoms <karsten.thoms@itemis.de> - Initial implementation and API
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath.helper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * A dynamic property handler for EMF classes.
 *
 * @author Karsten Thoms &lt;karsten.thoms@itemis.de&gt;
 */
public class EDynamicPropertyHandler implements DynamicPropertyHandler {
	private Map<EClass, String[]> eClass2PropNames = new HashMap<>();

	@Override
	public String[] getPropertyNames(Object object) {
		Assert.isLegal(object instanceof EObject);
		EClass eClass = ((EObject) object).eClass();
		return eClass2PropNames.computeIfAbsent(eClass,
				clz -> clz.getEAllStructuralFeatures().stream().map(EStructuralFeature::getName).toArray(String[]::new));
	}

	@Override
	public Object getProperty(Object object, String propertyName) {
		Assert.isLegal(object instanceof EObject);
		EObject eObject = (EObject) object;
		EStructuralFeature feature = eObject.eClass().getEStructuralFeature(propertyName);
		return feature != null ? eObject.eGet(feature) : null;
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		Assert.isLegal(object instanceof EObject);
		EObject eObject = (EObject) object;
		EStructuralFeature feature = eObject.eClass().getEStructuralFeature(propertyName);
		if (feature != null) {
			eObject.eSet(feature, value);
		}
	}
}
