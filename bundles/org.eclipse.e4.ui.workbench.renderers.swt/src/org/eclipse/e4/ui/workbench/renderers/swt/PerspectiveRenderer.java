/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.emf.ecore.EObject;
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
		IStylingEngine stylingEngine = (IStylingEngine) getContext(element)
				.get(IStylingEngine.SERVICE_NAME);
		stylingEngine.setClassname(perspArea, "perspectiveLayout"); //$NON-NLS-1$

		return perspArea;
	}

	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		super.processContents(container);

		IPresentationEngine renderer = (IPresentationEngine) context
				.get(IPresentationEngine.class.getName());

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

		MPerspective persp = (MPerspective) ((EObject) element).eContainer();
		if (persp.getWidget() instanceof Composite) {
			Composite comp = (Composite) persp.getWidget();
			return comp.getShell();
		}

		return null;
	}
}
