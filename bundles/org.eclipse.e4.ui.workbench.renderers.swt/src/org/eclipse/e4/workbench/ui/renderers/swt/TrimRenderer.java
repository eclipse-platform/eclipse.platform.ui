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

import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MTrimStructure;
import org.eclipse.e4.ui.model.application.MUIElement;

import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class TrimRenderer extends SWTPartRenderer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#createWidget(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MTrimStructure)
				|| !(parent instanceof Composite))
			return null;

		Composite parentWidget = (Composite) parent;
		Composite trimmedComp = new Composite(parentWidget, SWT.NONE);
		TrimmedPartLayout trimLayout = new TrimmedPartLayout(trimmedComp);
		trimmedComp.setLayout(trimLayout);

		// NOTE: The client area is where our direct children go...
		return trimLayout.clientArea;
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
		if (!(me instanceof MTrimStructure))
			return;
		MTrimStructure trimModel = (MTrimStructure) me;

		// The trim's 'widget' is actually the client area of its layout
		// NOTE: the casts below expect this arrangement
		Composite clientArea = (Composite) me.getWidget();
		Composite trimmedComp = clientArea.getParent();
		TrimmedPartLayout layout = (TrimmedPartLayout) trimmedComp.getLayout();

		// construct the trim
		if (hasVisibleChildren(trimModel.getTop())) {
			layout.top = createTrim(trimmedComp, SWT.HORIZONTAL, trimModel,
					trimModel.getTop());
		}
		if (hasVisibleChildren(trimModel.getBottom())) {
			layout.bottom = createTrim(trimmedComp, SWT.HORIZONTAL, trimModel,
					trimModel.getBottom());
		}
		if (hasVisibleChildren(trimModel.getLeft())) {
			layout.left = createTrim(trimmedComp, SWT.VERTICAL, trimModel,
					trimModel.getLeft());
		}
		if (hasVisibleChildren(trimModel.getRight())) {
			layout.right = createTrim(trimmedComp, SWT.VERTICAL, trimModel,
					trimModel.getRight());
		}

		// TODO Auto-generated method stub
		super.processContents(me);
	}

	/**
	 * @param trimmedComp
	 * @param horizontal
	 * @param topTrim
	 * @return
	 */
	private Composite createTrim(Composite trimmedComp, int orientation,
			MTrimStructure<MUIElement> trimModel,
			MElementContainer<MUIElement> trimContainer) {
		Composite trimComposite = new Composite(trimmedComp, SWT.NONE);
		RowLayout trl = new RowLayout(orientation);
		trl.marginBottom = trl.marginTop = 1;
		trimComposite.setLayout(trl);

		// Now we can create the controls in the trim...
		// for (MUIElement tb : trimContainer.getChildren()) {
		// renderer.createGui((trimModel, trimComposite, tb);
		// }

		return trimComposite;
	}

	/**
	 * @param trimSide
	 * @return
	 */
	private boolean hasVisibleChildren(MElementContainer<MUIElement> trimSide) {
		if (trimSide == null)
			return false;

		EList<MUIElement> kids = trimSide.getChildren();
		return kids != null && kids.size() > 0;
	}
}
