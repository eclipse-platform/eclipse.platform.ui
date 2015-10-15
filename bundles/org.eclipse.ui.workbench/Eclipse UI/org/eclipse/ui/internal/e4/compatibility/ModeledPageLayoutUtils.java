/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.e4.compatibility;

import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;

public class ModeledPageLayoutUtils {

	private EModelService modelService;

	public ModeledPageLayoutUtils(EModelService modelService) {
		this.modelService = modelService;
	}

	public int plRelToSwt(int rel) {
		switch (rel) {
		case IPageLayout.BOTTOM:
			return SWT.BOTTOM;
		case IPageLayout.LEFT:
			return SWT.LEFT;
		case IPageLayout.RIGHT:
			return SWT.RIGHT;
		case IPageLayout.TOP:
			return SWT.TOP;
		default:
			return 0;
		}
	}

	public void insert(MUIElement toInsert, MUIElement relTo, int swtSide, float ratio) {
		int pct = (int) (ratio * 10000);
		insert(toInsert, relTo, swtSide, pct);
	}

	public void insert(MUIElement toInsert, MUIElement relTo, int swtSide, int ratio) {
		if (toInsert == null || relTo == null)
			return;

		MElementContainer<MUIElement> relParent = relTo.getParent();
		if (relParent != null) {
			List<MUIElement> children = relParent.getChildren();
			int index = children.indexOf(relTo);
			MPartSashContainer psc = modelService.createModelElement(MPartSashContainer.class);
			psc.setContainerData(relTo.getContainerData());
			relParent.getChildren().add(index + 1, psc);

			switch (swtSide) {
			case SWT.LEFT:
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				psc.getChildren().add((MPartSashContainerElement) relTo);
				toInsert.setContainerData("" + ratio); //$NON-NLS-1$
				relTo.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(true);
				break;
			case SWT.RIGHT:
				psc.getChildren().add((MPartSashContainerElement) relTo);
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				relTo.setContainerData("" + ratio); //$NON-NLS-1$
				toInsert.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(true);
				break;
			case SWT.TOP:
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				psc.getChildren().add((MPartSashContainerElement) relTo);
				toInsert.setContainerData("" + ratio); //$NON-NLS-1$
				relTo.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(false);
				break;
			case SWT.BOTTOM:
				psc.getChildren().add((MPartSashContainerElement) relTo);
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				relTo.setContainerData("" + ratio); //$NON-NLS-1$
				toInsert.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(false);
				break;
			}

			if (relTo.isToBeRendered() || toInsert.isToBeRendered()) {
				// one of the items to be inserted should be rendered, render
				// all parent elements as well
				resetToBeRenderedFlag(psc, true);
			} else {
				// no child elements need to be rendered, the parent part sash
				// container does not need to be rendered either then
				psc.setToBeRendered(false);
			}
			return;
		}
	}

	public void resetToBeRenderedFlag(MUIElement element, boolean toBeRendered) {
		MUIElement parent = element.getParent();
		while (parent != null && !(parent instanceof MPerspective)) {
			parent.setToBeRendered(toBeRendered);
			parent = parent.getParent();
		}
		element.setToBeRendered(toBeRendered);
	}

	public MPartStack createStack(String id, boolean visible) {
		MPartStack newStack = modelService.createModelElement(MPartStack.class);
		newStack.setElementId(id);
		newStack.setToBeRendered(visible);
		return newStack;
	}

}
