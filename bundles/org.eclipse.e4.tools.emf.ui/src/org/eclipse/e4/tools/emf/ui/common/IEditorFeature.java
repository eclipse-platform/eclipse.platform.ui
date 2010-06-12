package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.List;

import org.eclipse.emf.ecore.EClass;

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
