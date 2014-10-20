/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * Factory for <code>org.eclipse.e4.workbench.emf.workbench.Composite</code>
 */
public class CompositeRenderer extends SWTPartRenderer {

	public CompositeRenderer() {
		super();
	}

	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPart) || !(parent instanceof Composite))
			return null;

		final Widget parentWidget = (Widget) parent;
		String policy = "VerticalComposite"; //compositeModel.getPolicy(); //$NON-NLS-1$

		if (policy != null && (policy.equals("HorizontalComposite") //$NON-NLS-1$
				|| policy.equals("VerticalComposite"))) { //$NON-NLS-1$
			return new Composite((Composite) parentWidget, SWT.NONE);
		}

		return null;
	}

	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);

		String policy = "VerticalComposite"; //compositeModel.getPolicy(); //$NON-NLS-1$
		if (policy != null && policy.endsWith("Composite")) { //$NON-NLS-1$
			Composite composite = (Composite) element.getWidget();
			Control[] children = composite.getChildren();
			GridLayout gl = new GridLayout(
					policy.startsWith("Horizontal") ? children.length : 1, false); //$NON-NLS-1$
			gl.horizontalSpacing = 0;
			gl.verticalSpacing = 0;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			composite.setLayout(gl);
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				boolean grabV = !(child instanceof ToolBar);
				boolean grabH = !(child instanceof ToolBar);
				child.setLayoutData(new GridData(SWT.FILL, SWT.FILL, grabH,
						grabV));
			}
		}
	}
}
