/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 506306
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class PerspectiveRenderer extends SWTPartRenderer {


	@Override
	public Widget createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPerspective)
				|| !(parent instanceof Composite))
			return null;

		Composite perspArea = new Composite((Composite) parent, SWT.NONE);
		perspArea.setLayout(new FillLayout());
		IStylingEngine stylingEngine = getContext(element).get(IStylingEngine.class);
		stylingEngine.setClassname(perspArea, "perspectiveLayout"); //$NON-NLS-1$

		return perspArea;
	}

	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		super.processContents(container);

		IPresentationEngine renderer = context.get(IPresentationEngine.class);

		MPerspective persp = (MPerspective) ((MUIElement) container);
		Shell shell = ((Composite) persp.getWidget()).getShell();
		for (MWindow dw : persp.getWindows()) {
			renderer.createGui(dw, shell, persp.getContext());
		}
	}

	@Override
	public Object getUIContainer(MUIElement element) {
		if (!(element instanceof MWindow))
			return super.getUIContainer(element);

		MUIElement persp = modelService.getContainer(element);
		if (persp.getWidget() instanceof Composite) {
			Composite comp = (Composite) persp.getWidget();
			return comp.getShell();
		}

		return null;
	}
}
