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

import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarContainer;
import org.eclipse.e4.ui.model.application.MTrimmedPart;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class TrimPartFactory extends SWTPartFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#createWidget(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public Object createWidget(MPart<?> element) {
		if (!(element instanceof MTrimmedPart<?>))
			return null;

		Composite parentWidget = (Composite) getParentWidget(element);
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
	public <P extends MPart<?>> void processContents(MPart<P> me) {
		if (!(me instanceof MTrimmedPart<?>))
			return;
		MTrimmedPart<?> trimModel = (MTrimmedPart<?>) me;

		// The trim's 'widget' is actually the client area of its layout
		// NOTE: the casts below expect this arrangement
		Composite clientArea = (Composite) me.getWidget();
		Composite trimmedComp = clientArea.getParent();
		TrimmedPartLayout layout = (TrimmedPartLayout) trimmedComp.getLayout();

		// construct the trim
		if (hasVisibleChildren(trimModel.getTopTrim())) {
			layout.top = createTrim(trimmedComp, SWT.HORIZONTAL, trimModel,
					trimModel.getTopTrim());
		}
		if (hasVisibleChildren(trimModel.getBottomTrim())) {
			layout.bottom = createTrim(trimmedComp, SWT.HORIZONTAL, trimModel,
					trimModel.getBottomTrim());
		}
		if (hasVisibleChildren(trimModel.getLeftTrim())) {
			layout.left = createTrim(trimmedComp, SWT.VERTICAL, trimModel,
					trimModel.getLeftTrim());
		}
		if (hasVisibleChildren(trimModel.getRightTrim())) {
			layout.right = createTrim(trimmedComp, SWT.VERTICAL, trimModel,
					trimModel.getRightTrim());
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
			MTrimmedPart<?> trimModel, MToolBarContainer trimContainer) {
		Composite trimComposite = new Composite(trimmedComp, SWT.NONE);
		RowLayout trl = new RowLayout(orientation);
		trl.marginBottom = trl.marginTop = 1;
		trimComposite.setLayout(trl);

		// Now we can create the controls in the trim...
		for (MToolBar tb : trimContainer.getToolbars()) {
			createToolBar(trimModel, trimComposite, tb);
		}

		return trimComposite;
	}

	/**
	 * @param trimSide
	 * @return
	 */
	private boolean hasVisibleChildren(MToolBarContainer trimSide) {
		if (trimSide == null)
			return false;

		EList<?> kids = trimSide.getToolbars();
		return kids != null && kids.size() > 0;
	}
}
