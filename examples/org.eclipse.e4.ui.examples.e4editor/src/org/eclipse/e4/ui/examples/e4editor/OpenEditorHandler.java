/*******************************************************************************
* Copyright (c) 2025 Feilim Breatnach and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which accompanies this distribution,
* and is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors: Feilim Breatnach, Pilz Ireland
*******************************************************************************/

package org.eclipse.e4.ui.examples.e4editor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenEditorHandler {

	public static final String DUMMY_EDITOR_DESCRIPTOR_ID = "org.eclipse.e4.ui.examples.e4editor.partdescriptor.e4editor";

	@Execute
	public void execute(EModelService modelService, MApplication application, EPartService partService) {
		Predicate<MPart> isEditorAlreadyOpenFilter = part -> DUMMY_EDITOR_DESCRIPTOR_ID.equals(part.getElementId());
		Optional<MPart> alreadyOpenMatchingPart = partService.getParts().stream().filter(isEditorAlreadyOpenFilter)
				.findFirst();

		MPart dummyPart = alreadyOpenMatchingPart.orElse(partService.createPart(DUMMY_EDITOR_DESCRIPTOR_ID));
		if (alreadyOpenMatchingPart.isEmpty()) {
			dummyPart = partService.createPart(DUMMY_EDITOR_DESCRIPTOR_ID);

			// not entirely necessary: but for consistency let's place our dummy editor
			// where editor instances appear
			Optional<MPartStack> primaryDataStack = findPrimaryConfiguationAreaPartStack(application, modelService);
			if (primaryDataStack.isPresent()) {
				primaryDataStack.get().getChildren().add(dummyPart);
			}
		} else {
			dummyPart = alreadyOpenMatchingPart.get();
		}

		partService.showPart(DUMMY_EDITOR_DESCRIPTOR_ID, PartState.ACTIVATE);
		partService.bringToTop(dummyPart);
	}

	private Optional<MPartStack> findPrimaryConfiguationAreaPartStack(MApplication application,
			EModelService modelService) {
		// find the part stack from the application model which represents the area
		// where our main 'editors' appear, ie. the main workspace area
		List<MArea> areaCandidates = modelService.findElements(application, org.eclipse.ui.IPageLayout.ID_EDITOR_AREA,
				MArea.class, null, EModelService.IN_SHARED_ELEMENTS);
		if (areaCandidates.size() == 1) {
			MArea primaryArea = areaCandidates.get(0);
			for (MPartSashContainerElement element : primaryArea.getChildren()) {
				if (element instanceof MPartStack) {
					return Optional.of((MPartStack) element);
				} else if (element instanceof MPartSashContainer) {
					return ((MPartSashContainer) element).getChildren().stream().filter(c -> c instanceof MPartStack)
							.map(c -> (MPartStack) c).findFirst();
				}
			}
		}

		return Optional.empty();
	}
}