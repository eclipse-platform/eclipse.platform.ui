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
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
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
	private IPresentationEngine renderer;

	@Inject
	private IEventBroker eventBroker;

	@PostConstruct
	public void init() {
		super.init(eventBroker);
	}

	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPerspectiveStack) || !(parent instanceof Composite))
			return null;

		Composite perspStack = new Composite((Composite) parent, SWT.NONE);
		IStylingEngine stylingEngine = getContext(element).get(IStylingEngine.class);
		stylingEngine.setClassname(perspStack, "perspectiveLayout"); //$NON-NLS-1$
		perspStack.setLayout(new StackLayout());

		return perspStack;
	}

	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);

		MPerspectiveStack ps = (MPerspectiveStack) element;
		if (ps.getSelectedElement() != null && ps.getSelectedElement().getWidget() != null) {
			Control ctrl = (Control) ps.getSelectedElement().getWidget();
			Composite psComp = (Composite) ps.getWidget();
			StackLayout sl = (StackLayout) psComp.getLayout();
			sl.topControl = ctrl;
			ctrl.requestLayout();
		}
	}

	@Override
	protected void showTab(MUIElement tabElement) {
		MPerspective persp = (MPerspective) tabElement;

		Control ctrl = (Control) persp.getWidget();
		if (ctrl == null) {
			ctrl = (Control) renderer.createGui(persp);
		} else if (ctrl.getParent() != persp.getParent().getWidget()) {
			Composite parent = (Composite) persp.getParent().getWidget();
			ctrl.setParent(parent);
		}

		super.showTab(persp);

		// relayout the perspective
		Composite psComp = ctrl.getParent();
		StackLayout sl = (StackLayout) psComp.getLayout();
		if (sl != null) {
			sl.topControl = ctrl;
			ctrl.requestLayout();
		}

		ctrl.moveAbove(null);

		// Force a context switch
		IEclipseContext context = persp.getContext();
		context.get(EPartService.class).switchPerspective(persp);

		// Move any other controls to 'limbo'
		Control[] kids = psComp.getChildren();
		Shell limbo = (Shell) context.get("limbo"); //$NON-NLS-1$
		for (Control child : kids) {
			if (child != ctrl) {
				child.setParent(limbo);
			}
		}
	}
}
