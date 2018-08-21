/*******************************************************************************
 * Copyright (c) 2011-2015 EclipseSource Muenchen GmbH and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonas - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Helper to merge two Fragments Files into one.
 *
 * @author Jonas
 *
 */
public class FragmentMergeHelper {

	/**
	 * Merges a source {@link MModelFragments} into a target. Adds missing
	 * imports. Replaces existing imports, which get added as elements by those.
	 *
	 * @param sourceFragments
	 *            an {@link MModelFragments}
	 * @param targetFragments
	 *            an {@link MModelFragments}
	 */
	public static void merge(MModelFragments sourceFragments, MModelFragments targetFragments) {
		targetFragments.getFragments().addAll(sourceFragments.getFragments());
		final List<MApplicationElement> sourceImports = new ArrayList<>();

		consolidateImports(targetFragments);

		sourceImports.addAll(sourceFragments.getImports());
		for (final MApplicationElement sourceImport : sourceImports) {

			boolean doImport = true;
			for (final MApplicationElement targetImport : targetFragments.getImports()) {
				if (haveSameID(sourceImport, targetImport)) {
					doImport = false;
					changeReferenceToExistingElement(targetFragments.getFragments(), targetImport, sourceImport);
					break;
				}

			}
			if (doImport) {
				targetFragments.getImports().add(sourceImport);
			}

		}

	}

	/**
	 * Replaces imports by elements which got imported.
	 *
	 * @param targetFragments
	 */
	private static void consolidateImports(MModelFragments targetFragments) {
		final List<MModelFragment> fragments = targetFragments.getFragments();
		for (final MModelFragment fragment : fragments) {
			final List<MApplicationElement> elements = fragment.getElements();
			for (final MApplicationElement element : elements) {
				final MApplicationElement previousImport = getPreviousImport(element, targetFragments);
				if (previousImport != null) {
					changeReferenceToExistingElement(targetFragments.getFragments(), element, previousImport);
				}
			}
		}

	}


	/**
	 * Finds imports which have the same ID as an element which got imported.
	 *
	 * @param element
	 *            The {@link MApplicationElement} to search for in the imports
	 * @param targetFragments
	 *            The {@link MModelFragments} to search in
	 * @return The existing import or null if there is none.
	 */
	private static MApplicationElement getPreviousImport(MApplicationElement element, MModelFragments targetFragments) {

		final List<MApplicationElement> imports = targetFragments.getImports();
		for (final MApplicationElement imported : imports) {
			if (haveSameID(element, imported)) {
				return imported;
			}
		}
		return null;
	}

	/**
	 * @param fragments
	 *            The {@link MModelFragments} to search for existing references
	 * @param target
	 *            The element to link to
	 * @param source
	 *            The existing element to unlink
	 */
	private static void changeReferenceToExistingElement(List<MModelFragment> fragments, MApplicationElement target,
			MApplicationElement source) {
		final Collection<Setting> settings = EcoreUtil.UsageCrossReferencer.find((EObject) source, fragments);
		for (final Setting setting : settings) {
			if (setting.getEStructuralFeature().isMany()) {
				@SuppressWarnings("unchecked")
				final List<MApplicationElement> list = (List<MApplicationElement>) setting.getEObject()
				.eGet(setting.getEStructuralFeature());
				list.remove(source);
				list.add(target);
			} else {
				setting.getEObject().eSet(setting.getEStructuralFeature(), target);
			}
		}
		EcoreUtil.delete((EObject) source);

	}

	/**
	 *
	 * @param firstElement
	 *            an {@link MApplicationElement}
	 * @param secondElement
	 *            an {@link MApplicationElement}
	 * @return whether both elements have a non-empty and non-null ID and the
	 *         IDs are equal
	 */
	public static boolean haveSameID(MApplicationElement firstElement, MApplicationElement secondElement) {

		if (firstElement.getElementId() == null || secondElement.getElementId() == null) {
			return false;
		}
		if (firstElement.getElementId().isEmpty() || secondElement.getElementId().isEmpty()) {
			return false;
		}
		if (firstElement.getElementId().equals(secondElement.getElementId())) {
			return true;
		}
		return false;
	}

}
