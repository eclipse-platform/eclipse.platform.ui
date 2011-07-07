/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.ui.IViewLayout;

public class ModeledViewLayout implements IViewLayout {
	private MUIElement viewME;

	public ModeledViewLayout(MPart view) {
		viewME = view;
	}

	public ModeledViewLayout(MPlaceholder placeholder) {
		viewME = placeholder;
	}

	public boolean getShowTitle() {
		return !viewME.getTags().contains(IPresentationEngine.NO_TITLE);
	}

	public boolean isCloseable() {
		return !viewME.getTags().contains(IPresentationEngine.NO_CLOSE);
	}

	public boolean isMoveable() {
		return !viewME.getTags().contains(IPresentationEngine.NO_MOVE);
	}

	public boolean isStandalone() {
		MUIElement parentElement = viewME.getParent();
		return !(parentElement instanceof MPartStack);
	}

	public void setCloseable(boolean closeable) {
		if (closeable)
			viewME.getTags().remove(IPresentationEngine.NO_CLOSE);
		else
			viewME.getTags().add(IPresentationEngine.NO_CLOSE);
	}

	public void setMoveable(boolean moveable) {
		if (moveable)
			viewME.getTags().remove(IPresentationEngine.NO_MOVE);
		else
			viewME.getTags().add(IPresentationEngine.NO_MOVE);
	}

}
