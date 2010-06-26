package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;

import org.eclipse.emf.ecore.EObject;

public class Util {
	public static final boolean isNullOrEmpty(String element) {
		return element == null || element.trim().length() == 0;
	}
	
	public static final boolean isImport(EObject object) {
		return object.eContainingFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS;
	}
}
