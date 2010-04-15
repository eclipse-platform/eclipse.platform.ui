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

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.PostConstruct;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class PerspectiveStackRenderer extends LazyStackRenderer {

	@Inject
	IPresentationEngine renderer;

	@Inject
	IEventBroker eventBroker;

	@PostConstruct
	public void init() {
		super.init(eventBroker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer#createWidget
	 * (org.eclipse.e4.ui.model.application.MUIElement, java.lang.Object)
	 */
	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPerspectiveStack)
				|| !(parent instanceof Composite))
			return null;

		Composite perspStack = new Composite((Composite) parent, SWT.NONE);
		IStylingEngine stylingEngine = (IStylingEngine) getContext(element)
				.get(IStylingEngine.SERVICE_NAME);
		stylingEngine.setClassname(perspStack, "perspectiveLayout"); //$NON-NLS-1$
		perspStack.setLayout(new StackLayout());

		return perspStack;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.LazyStackRenderer#postProcess
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);

		MPerspectiveStack ps = (MPerspectiveStack) element;
		if (ps.getSelectedElement() != null
				&& ps.getSelectedElement().getWidget() != null) {
			Control ctrl = (Control) ps.getSelectedElement().getWidget();
			Composite psComp = (Composite) ps.getWidget();
			StackLayout sl = (StackLayout) psComp.getLayout();
			sl.topControl = ctrl;
			psComp.layout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.LazyStackRenderer#showTab(org
	 * .eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	protected void showTab(MUIElement tabElement) {
		super.showTab(tabElement);

		Control ctrl = (Control) tabElement.getWidget();
		if (ctrl == null) {
			ctrl = (Control) renderer.createGui(tabElement);
		}

		Composite psComp = ctrl.getParent();
		StackLayout sl = (StackLayout) psComp.getLayout();
		sl.topControl = ctrl;
		psComp.layout();

		ctrl.moveAbove(null);
	}
}
