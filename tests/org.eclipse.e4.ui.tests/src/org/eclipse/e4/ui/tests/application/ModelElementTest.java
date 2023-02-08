/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.junit.Test;

public class ModelElementTest {

	@Test
	public void testForMApplicationInterface() {
		List<EClass> failedClasses = new ArrayList<>();
		checkPackageForMApplicationInterface(failedClasses, ApplicationPackageImpl.eINSTANCE);
		if (failedClasses.size() > 0) {
			StringBuilder b = new StringBuilder(
					"The following concrete classes don't implement 'MApplicationElement':\n");
			for (EClass c : failedClasses) {
				b.append("* " + c.getName() + "\n");
			}
			System.err.println(b.toString());
			fail(b.toString());
		}
	}

	private void checkPackageForMApplicationInterface(List<EClass> failedClasses, EPackage ePackage) {
		for (EClassifier classifier : ePackage.getEClassifiers()) {
			if (classifier instanceof EClass) {
				EClass c = (EClass) classifier;
				if (!c.isInterface() && c != ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP
						&& c != ApplicationPackageImpl.Literals.STRING_TO_OBJECT_MAP) {
					if (!MApplicationElement.class.isAssignableFrom(c.getInstanceClass())) {
						failedClasses.add(c);
					}
				}
			}
		}
		for (EPackage subPackage : ePackage.getESubpackages()) {
			checkPackageForMApplicationInterface(failedClasses, subPackage);
		}
	}

	@Test
	public void testForOptimalBaseClass() {
		Map<EClass, EClass> failedClasses = new LinkedHashMap<>();
		checkPackageForOptimalBaseClass(failedClasses, ApplicationPackageImpl.eINSTANCE);
		if (failedClasses.size() > 0) {
			StringBuilder b = new StringBuilder(
					"The following concrete classes have a sub-optimal first super type:\n");
			for (Map.Entry<EClass, EClass> entry : failedClasses.entrySet()) {
				EClass c = entry.getKey();
				EClass actualESuperType = c.getESuperTypes().get(0);
				EClass bestESuperType = entry.getValue();
				b.append("* " + c.getName() + " extends " + actualESuperType.getName() + " with "
						+ getReusedGeneratedFeatureCount(actualESuperType) + " reused features " + " instead of "
						+ bestESuperType.getName() + " with " + getReusedGeneratedFeatureCount(bestESuperType)
						+ " reused features\n");
			}
			System.err.println(b.toString());
			fail(b.toString());
		}
	}

	private void checkPackageForOptimalBaseClass(Map<EClass, EClass> failedClasses, EPackage ePackage) {
		for (EClassifier classifier : ePackage.getEClassifiers()) {
			// For these three cases the difference is very small.
			if (classifier != UiPackageImpl.Literals.IMPERATIVE_EXPRESSION
					&& classifier != MenuPackageImpl.Literals.HANDLED_MENU_ITEM
					&& classifier != MenuPackageImpl.Literals.HANDLED_TOOL_ITEM && classifier instanceof EClass) {
				EClass c = (EClass) classifier;
				EList<EClass> eSuperTypes = c.getESuperTypes();
				int bestReusedGeneratedFeatureCount = 0;
				EClass bestESuperType = null;
				for (EClass eSuperType : eSuperTypes) {
					int reusedGeneratedFeatureCount = getReusedGeneratedFeatureCount(eSuperType);
					if (reusedGeneratedFeatureCount > bestReusedGeneratedFeatureCount) {
						bestESuperType = eSuperType;
						bestReusedGeneratedFeatureCount = reusedGeneratedFeatureCount;
					}
				}

				if (bestESuperType != null && eSuperTypes.indexOf(bestESuperType) != 0) {
					failedClasses.put(c, bestESuperType);
				}
			}
		}
		for (EPackage subPackage : ePackage.getESubpackages()) {
			checkPackageForOptimalBaseClass(failedClasses, subPackage);
		}
	}

	private int getReusedGeneratedFeatureCount(EClass eClass) {
		if (eClass.isInterface()) {
			EList<EClass> eSuperTypes = eClass.getESuperTypes();
			if (!eSuperTypes.isEmpty()) {
				return getReusedGeneratedFeatureCount(eSuperTypes.get(0));
			}
			return 0;
		}
		return eClass.getEAllStructuralFeatures().size();
	}
}
