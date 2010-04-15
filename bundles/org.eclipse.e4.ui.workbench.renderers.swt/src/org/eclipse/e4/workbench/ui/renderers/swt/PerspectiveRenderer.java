/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class PerspectiveRenderer extends SWTPartRenderer {

	public PerspectiveRenderer() {
		super();
	}

	public Widget createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPerspective)
				|| !(parent instanceof Composite))
			return null;

		Composite perspArea = new Composite((Composite) parent, SWT.NONE);
		IStylingEngine stylingEngine = (IStylingEngine) getContext(element)
				.get(IStylingEngine.SERVICE_NAME);
		stylingEngine.setClassname(perspArea, "perspectiveLayout"); //$NON-NLS-1$
		perspArea.setLayout(new FillLayout());

		return perspArea;
	}
}
