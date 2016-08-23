/*******************************************************************************
 * Copyright (c) 2016 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eugen Neufeld - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.Comparator;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.model.internal.PositionInfo;

/**
 * Custom {@link Comparator} that operates on {@link MStringModelFragment} model
 * fragments wrapped in a {@link ModelFragmentWrapper}. The comparator will
 * order the fragments in the logical way in which they should be merged, based
 * on their defined {@link MStringModelFragment#getPositionInList()
 * positionInList} , as follows:
 * <li><b>Index</b> - all elements with an index are merged first. Lowest to
 * highest index.
 * <li><b>No position defined</b> - all contributions without any information
 * are sorted in the order in which they are read from the extension points and
 * added to the end of the list
 * <li><b>First/Last</b> - all elements with a first/last tag. If there are
 * several elements in the same container with the same tag, then those elements
 * are sorted in the order as they come form the extension registry.
 * <li><b> After/Before</b> - the after and before contributions are ordered
 * considering potential dependencies between fragments. If the dependency is
 * not resolvable eg points to the application model the element is sorted to
 * the front of the after/before list.
 *
 */
public class ModelFragmentComparator implements Comparator<ModelFragmentWrapper> {

	@Override
	public int compare(ModelFragmentWrapper o1, ModelFragmentWrapper o2) {
		if (o1 == o2)
			return 0;

		PositionDescription posInfo1 = getPositionDescription(o1);
		PositionDescription posInfo2 = getPositionDescription(o2);

		switch (posInfo1.getPlace()) {
		case NONE:
			switch (posInfo2.getPlace()) {
			case INDEX:
			case NONE:
				return 1;
			default:
				return -1;
			}
		case ABSOLUTE:
			switch (posInfo2.getPlace()) {
			case RELATIVE:
				return -1;
			default:
				return 1;
			}
		case INDEX:
			switch (posInfo2.getPlace()) {
			case INDEX:
				return posInfo1.getPositionReferenceAsInteger() - posInfo2.getPositionReferenceAsInteger();
			default:
				return -1;
			}
		case RELATIVE:
			switch (posInfo2.getPlace()) {
			case RELATIVE:
				boolean hasElement = false;
				for (MApplicationElement element : o2.getModelFragment().getElements()) {
					hasElement |= ModelUtils.findElementById(element, posInfo1.getReference()) != null;
					if (hasElement)
						break;
				}
				if (hasElement)
					return 1;
				hasElement = false;
				for (MApplicationElement element : o1.getModelFragment().getElements()) {
					hasElement |= ModelUtils.findElementById(element, posInfo2.getReference()) != null;
					if (hasElement)
						break;
				}
				if (hasElement)
					return -1;
				return -1;
			default:
				return 1;
			}
		default:
			// should never be reached
			return 1;
		}
	}

	private PositionDescription getPositionDescription(ModelFragmentWrapper wrapper) {
		if (!MStringModelFragment.class.isInstance(wrapper.getModelFragment()))
			return new PositionDescription(PositionPlace.NONE, null);
		MStringModelFragment stringFragment = (MStringModelFragment) wrapper.getModelFragment();
		if (stringFragment.getPositionInList() == null)
			return new PositionDescription(PositionPlace.NONE, null);
		String posInList = stringFragment.getPositionInList().trim();
		PositionInfo posInfo = PositionInfo.parse(posInList);
		if (posInfo == null)
			return new PositionDescription(PositionPlace.NONE, null);
		switch (posInfo.getPosition()) {
		case AFTER:
		case BEFORE:
			return new PositionDescription(PositionPlace.RELATIVE, posInfo.getPositionReference());
		case FIRST:
		case LAST:
			return new PositionDescription(PositionPlace.ABSOLUTE, posInfo.getPositionReference());
		case INDEX:
			return new PositionDescription(PositionPlace.INDEX, posInfo.getPositionReference());
		default:
			return new PositionDescription(PositionPlace.NONE, null);
		}
	}

	/**
	 * Inner Class used to describe the Position to which the fragment should be
	 * sorted.
	 */
	private class PositionDescription {
		private final PositionPlace place;
		private final String reference;

		PositionDescription(PositionPlace place, String reference) {
			this.place = place;
			this.reference = reference;
		}

		public PositionPlace getPlace() {
			return place;
		}

		public String getReference() {
			return reference;
		}

		public int getPositionReferenceAsInteger() {
			return Integer.parseInt(reference);
		}

	}

	/**
	 * Inner class used to describe the position to merge to.
	 */
	private enum PositionPlace {
		NONE, ABSOLUTE, INDEX, RELATIVE
	}
}
