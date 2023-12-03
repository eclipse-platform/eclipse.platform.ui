/*******************************************************************************
 * Copyright (c) 2011, 2019 IBM Corporation and others.
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

package org.eclipse.e4.ui.workbench.addons.splitteraddon;

import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.service.event.Event;

/**
 * Listens for the IPresentationEngine's SPLIT_HORIZONTAL and SPLIT_VERTICAL tags being applied to
 * an MPart and takes the appropriate steps to split / unsplit the part
 */
public class SplitterAddon {

	private static final String DISABLE_SPLITTER_ADDON = "DisableSplitterAddon"; //$NON-NLS-1$

	@Inject
	EModelService ms;

	@Inject
	EPartService ps;

	@Inject
	MApplication app;

	/**
	 * Handles changes in tags
	 */
	@Inject
	@Optional
	private void subscribeTopicTagsChanged(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {

		if (app.getTags().contains(DISABLE_SPLITTER_ADDON)) {
			return;
		}

		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MPart)) {
			return;
		}

		MPart part = (MPart) changedObj;

		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
					IPresentationEngine.SPLIT_HORIZONTAL)) {
				splitPart(part, true);
			} else if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
					IPresentationEngine.SPLIT_VERTICAL)) {
				splitPart(part, false);
			}
		} else if (UIEvents.isREMOVE(event)) {
			MCompositePart compPart = SplitterAddon.findContainingCompositePart(part);
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
					IPresentationEngine.SPLIT_HORIZONTAL)) {
				unsplitPart(compPart);
			} else if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
					IPresentationEngine.SPLIT_VERTICAL)) {
				unsplitPart(compPart);
			}
		}
	}

	/**
	 * Finds the CompositePart containing the given part (if any)
	 *
	 * @return The MCompositePart or 'null' if none is found
	 */
	public static MCompositePart findContainingCompositePart(MPart part) {
		if (part == null) {
			return null;
		}

		MUIElement curParent = part.getParent();
		while (curParent != null && !(curParent instanceof MCompositePart)) {
			curParent = curParent.getParent();
		}

		return (MCompositePart) curParent;
	}

	private void unsplitPart(MCompositePart compositePart) {
		if (compositePart == null) {
			return;
		}

		List<MPart> innerElements = ms.findElements(compositePart, null, MPart.class, null);
		if (innerElements.size() < 3) {
			return;
		}

		MPart originalEditor = innerElements.get(1); // '0' is the composite part

		// Close the cloned editor *before* removing it from the model
		MPart clonedEditor = innerElements.get(2);
		clonedEditor.setToBeRendered(false);

		MElementContainer<MUIElement> compParent = compositePart.getParent();
		int index = compParent.getChildren().indexOf(compositePart);
		compParent.getChildren().remove(compositePart);
		originalEditor.getParent().getChildren().remove(originalEditor);
		compParent.getChildren().add(index, originalEditor);

		if (ps.getActivePart() == originalEditor) {
			ps.activate(null);
		}
		ps.activate(originalEditor);
	}

	private MCompositePart createCompositePart(MPart originalPart) {
		MCompositePart compPart = ms.createModelElement(MCompositePart.class);
		compPart.setElementId("Split Host(" + originalPart.getLabel() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		compPart.setLabel(originalPart.getLabel());
		compPart.setTooltip(originalPart.getTooltip());
		compPart.setIconURI(originalPart.getIconURI());
		compPart.setCloseable(true);
		compPart.setContributionURI(SplitHost.SPLIT_HOST_CONTRIBUTOR_URI);

		// Check if icon from MPart was overridden
		Object overriddenImage = originalPart.getTransientData().get(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY);
		if (overriddenImage != null) {
			compPart.getTransientData().put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY, overriddenImage);
		}

		// Always remove the composite part from the model
		compPart.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);

		return compPart;
	}

	void splitPart(MPart partToSplit, boolean horizontal) {
		MElementContainer<MUIElement> parent = partToSplit.getParent();
		int index = parent.getChildren().indexOf(partToSplit);

		MPart editorClone = (MPart) ms.cloneElement(partToSplit, null);

		MCompositePart compPart = createCompositePart(partToSplit);

		// Add the new composite part to the model
		compPart.getChildren().add(editorClone);
		compPart.setSelectedElement(editorClone);
		parent.getChildren().add(index, compPart);
		parent.setSelectedElement(compPart);

		// Now, add the original part into the composite
		int orientation = horizontal ? EModelService.ABOVE : EModelService.LEFT_OF;
		ms.insert(partToSplit, editorClone, orientation, 0.5f);

		ps.activate(partToSplit);
	}
}
