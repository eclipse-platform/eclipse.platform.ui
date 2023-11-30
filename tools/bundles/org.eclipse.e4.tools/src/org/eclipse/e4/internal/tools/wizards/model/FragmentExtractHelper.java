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
 * Jonas Helming - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * @author Jonas Helming
 */
public class FragmentExtractHelper {

	/**
	 * Imports referenced commands
	 *
	 * @param moe
	 *            elements to resolved referenced commands for
	 * @param importedElements
	 *            already imported Elements
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void resolveImports(MApplicationElement moe, Map<MApplicationElement, MApplicationElement> importedElements) {
		final EObject element = (EObject)moe;
		for (final EContentsEList.FeatureIterator featureIterator =
				(EContentsEList.FeatureIterator)element.eCrossReferences().iterator();
				featureIterator.hasNext(); )
		{
			final EObject objectToBeImported = (EObject) featureIterator.next();
			final EReference eReference = (EReference)featureIterator.feature();
			MApplicationElement alreadyImportedElement = null;
			if (objectToBeImported instanceof MApplicationElement) {
				alreadyImportedElement = importedElements.get((MApplicationElement) objectToBeImported);
			}
			if(alreadyImportedElement==null){
				alreadyImportedElement = (MApplicationElement) EcoreUtil.copy(objectToBeImported);
				importedElements.put((MApplicationElement) objectToBeImported, alreadyImportedElement);
			}
			if (eReference.isMany()) {
				final EList<EObject> ref = (EList<EObject>) element.eGet(eReference);
				ref.remove(objectToBeImported);
				ref.add((EObject) alreadyImportedElement);
			} else {
				element.eSet(eReference, alreadyImportedElement);
			}
		}

	}

	/**
	 * @param extractedElements
	 *            the {@link MApplicationElement}s to be extracted
	 */
	public static MModelFragments createInitialModel(List<MApplicationElement> extractedElements) {
		final MModelFragments mModelFragments = MFragmentFactory.INSTANCE.createModelFragments();
		final HashMap<MApplicationElement, MApplicationElement> importCommands = new HashMap<>();
		final HashMap<String, MStringModelFragment> createdFragments = new HashMap<>();
		for (final MApplicationElement moe : extractedElements) {
			final EObject eObject = (EObject) moe;
			final TreeIterator<EObject> eAllContents = eObject.eAllContents();
			boolean hasNext = eAllContents.hasNext();
			if (!hasNext) {
				FragmentExtractHelper.resolveImports(moe, importCommands);
			}
			while (hasNext) {
				final MApplicationElement next = (MApplicationElement) eAllContents.next();
				FragmentExtractHelper.resolveImports(next, importCommands);
				hasNext = eAllContents.hasNext();
			}
			final MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject) moe);
			final String featurename = ((EObject) moe).eContainmentFeature().getName();
			final String containerID = ((MApplicationElement) ((EObject) moe).eContainer()).getElementId();
			final String combinedID = containerID + featurename;
			MStringModelFragment modelFragment = createdFragments.get(combinedID);
			if (modelFragment == null) {
				modelFragment = MFragmentFactory.INSTANCE.createStringModelFragment();
				modelFragment.setParentElementId(containerID);
				modelFragment.setFeaturename(featurename);
				createdFragments.put(combinedID, modelFragment);
				mModelFragments.getFragments().add(modelFragment);
			}
			modelFragment.getElements().add(copy);

		}

		final Set<MApplicationElement> keySet = importCommands.keySet();
		for (final MApplicationElement key : keySet) {
			mModelFragments.getImports().add(importCommands.get(key));
		}

		return mModelFragments;
	}

}
