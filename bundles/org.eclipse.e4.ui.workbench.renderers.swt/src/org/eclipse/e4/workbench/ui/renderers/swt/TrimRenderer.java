/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class TrimRenderer extends SWTPartRenderer {

	ControlListener childResizeListener = new ControlListener() {
		public void controlResized(ControlEvent e) {
			Control ctrl = (Control) e.widget;
			Control[] changed = { ctrl };
			ctrl.getShell().layout(changed, SWT.DEFER);
		}

		public void controlMoved(ControlEvent e) {
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#createWidget(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MTrimBar) || !(parent instanceof Composite))
			return null;

		Composite parentComp = (Composite) parent;
		if (!(parentComp.getLayout() instanceof TrimmedPartLayout))
			return null;

		MTrimBar trimModel = (MTrimBar) element;
		TrimmedPartLayout tpl = (TrimmedPartLayout) parentComp.getLayout();

		switch (trimModel.getSide().getValue()) {
		case SideValue.TOP_VALUE:
			return tpl.getTrimComposite(parentComp, SWT.TOP);
		case SideValue.BOTTOM_VALUE:
			return tpl.getTrimComposite(parentComp, SWT.BOTTOM);
		case SideValue.LEFT_VALUE:
			return tpl.getTrimComposite(parentComp, SWT.LEFT);
		case SideValue.RIGHT_VALUE:
			return tpl.getTrimComposite(parentComp, SWT.RIGHT);
		}

		return null; // unknown side
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartFactory#processContents
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		if (!(((MUIElement) me) instanceof MTrimBar))
			return;
		// MTrimContainer trimModel = (MTrimContainer) me;

		// TODO Auto-generated method stub
		super.processContents(me);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartRenderer#childRendered
	 * (org.eclipse.e4.ui.model.application.MElementContainer,
	 * org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);

		// Add a size change listener to auto-layout
		if (element.getWidget() instanceof Control) {
			final Control ctrl = (Control) element.getWidget();
			ctrl.addControlListener(childResizeListener);
		}
	}
}
