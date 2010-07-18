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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

public class Util {
	public static final boolean isNullOrEmpty(String element) {
		return element == null || element.trim().length() == 0;
	}

	public static final boolean isImport(EObject object) {
		return object.eContainingFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS;
	}

	public static final void addClasses(EPackage ePackage, List<FeatureClass> list) {
		for (EClassifier c : ePackage.getEClassifiers()) {
			if (c instanceof EClass) {
				EClass eclass = (EClass) c;
				if (eclass != ApplicationPackageImpl.Literals.APPLICATION && !eclass.isAbstract() && !eclass.isInterface() && eclass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
					list.add(new FeatureClass(eclass.getName(), eclass));
				}
			}
		}

		for (EPackage eSubPackage : ePackage.getESubpackages()) {
			addClasses(eSubPackage, list);
		}
	}

	public static List<InternalPackage> loadPackages() {
		List<InternalPackage> packs = new ArrayList<InternalPackage>();

		for (Entry<String, Object> regEntry : EPackage.Registry.INSTANCE.entrySet()) {
			if (regEntry.getValue() instanceof EPackage) {
				EPackage ePackage = (EPackage) regEntry.getValue();
				InternalPackage iePackage = new InternalPackage(ePackage);
				boolean found = false;
				for (EClassifier cl : ePackage.getEClassifiers()) {
					if (cl instanceof EClass) {
						EClass eClass = (EClass) cl;
						if (eClass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
							if (!eClass.isInterface() && !eClass.isAbstract()) {
								found = true;
								InternalClass ieClass = new InternalClass(iePackage, eClass);
								iePackage.classes.add(ieClass);
								for (EReference f : eClass.getEAllReferences()) {
									ieClass.features.add(new InternalFeature(ieClass, f));
								}
							}
						}
					}
				}
				if (found) {
					packs.add(iePackage);
				}
			}
		}

		return packs;
	}

	public static boolean moveElementByIndex(EditingDomain editingDomain, MUIElement element, boolean liveModel, int index) {
		if (liveModel) {
			MElementContainer<MUIElement> container = element.getParent();
			container.getChildren().remove(element);

			if (index >= 0) {
				container.getChildren().add(index, element);
			} else {
				container.getChildren().add(element);
			}

			container.setSelectedElement(element);
			return true;
		} else {
			MElementContainer<MUIElement> container = element.getParent();
			Command cmd = MoveCommand.create(editingDomain, container, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, element, index);

			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
				return true;
			}
			return false;
		}
	}

	public static class InternalPackage {
		public final EPackage ePackage;
		public List<InternalClass> classes = new ArrayList<InternalClass>();

		public InternalPackage(EPackage ePackage) {
			this.ePackage = ePackage;
		}

		@Override
		public String toString() {
			return ePackage.toString();
		}

		public List<EClass> getAllClasses() {
			ArrayList<EClass> rv = new ArrayList<EClass>(classes.size());
			for (InternalClass c : classes) {
				rv.add(c.eClass);
			}
			return rv;
		}
	}

	public static class InternalClass {
		public final InternalPackage pack;
		public final EClass eClass;
		public List<InternalFeature> features = new ArrayList<InternalFeature>();

		public InternalClass(InternalPackage pack, EClass eClass) {
			this.eClass = eClass;
			this.pack = pack;
		}
	}

	public static class InternalFeature {
		public final InternalClass clazz;
		public final EStructuralFeature feature;

		public InternalFeature(InternalClass clazz, EStructuralFeature feature) {
			this.clazz = clazz;
			this.feature = feature;
		}

	}
}
