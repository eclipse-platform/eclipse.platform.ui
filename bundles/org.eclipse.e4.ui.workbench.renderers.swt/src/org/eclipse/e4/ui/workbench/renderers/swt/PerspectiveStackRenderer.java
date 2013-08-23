/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

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
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#createWidget
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
	 * org.eclipse.e4.ui.workbench.renderers.swt.LazyStackRenderer#postProcess
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
	 * org.eclipse.e4.ui.workbench.renderers.swt.LazyStackRenderer#showTab(org
	 * .eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	protected void showTab(MUIElement tabElement) {
		MPerspective persp = (MPerspective) tabElement;

		Control ctrl = (Control) tabElement.getWidget();
		if (ctrl == null) {
			ctrl = (Control) renderer.createGui(tabElement);
		} else if (ctrl.getParent() != tabElement.getParent().getWidget()) {
			Composite parent = (Composite) tabElement.getParent().getWidget();
			ctrl.setParent(parent);
		}

		super.showTab(tabElement);

		// relayout the perspective
		Composite psComp = ctrl.getParent();
		StackLayout sl = (StackLayout) psComp.getLayout();
		if (sl != null) {
			sl.topControl = ctrl;
			psComp.layout();
		}

		ctrl.moveAbove(null);

		// Force a context switch
		if (tabElement instanceof MPerspective) {
			MPerspective selected = (MPerspective) tabElement.getParent()
					.getSelectedElement();
			if (selected != null) {
				IEclipseContext context = selected.getContext();
				context.get(EPartService.class).switchPerspective(selected);
			}
		}

		// Move any other controls to 'limbo'
		Control[] kids = ctrl.getParent().getChildren();
		Shell limbo = (Shell) persp.getContext().get("limbo"); //$NON-NLS-1$
		for (Control child : kids) {
			if (child != ctrl) {
				child.setParent(limbo);
			}
		}
	}
}
