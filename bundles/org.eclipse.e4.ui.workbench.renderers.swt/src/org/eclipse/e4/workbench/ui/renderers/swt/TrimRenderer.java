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
import org.eclipse.e4.ui.model.application.MTrimContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.SideValue;
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
		if (!(element instanceof MTrimContainer)
				|| !(parent instanceof Composite))
			return null;

		Composite parentComp = (Composite) parent;
		if (!(parentComp.getLayout() instanceof TrimmedPartLayout))
			return null;

		MTrimContainer<?> trimModel = (MTrimContainer<?>) element;
		TrimmedPartLayout tpl = (TrimmedPartLayout) parentComp.getLayout();

		switch (trimModel.getSide().getValue()) {
		case SideValue.TOP_VALUE:
			tpl.top = new Composite(parentComp, SWT.NONE);
			tpl.top.setLayout(new RowLayout(SWT.HORIZONTAL));
			return tpl.top;
		case SideValue.BOTTOM_VALUE:
			tpl.bottom = new Composite(parentComp, SWT.NONE);
			tpl.bottom.setLayout(new RowLayout(SWT.HORIZONTAL));
			return tpl.bottom;
		case SideValue.LEFT_VALUE:
			tpl.left = new Composite(parentComp, SWT.NONE);
			tpl.left.setLayout(new RowLayout(SWT.VERTICAL));
			return tpl.left;
		case SideValue.RIGHT_VALUE:
			tpl.right = new Composite(parentComp, SWT.NONE);
			tpl.right.setLayout(new RowLayout(SWT.VERTICAL));
			return tpl.right;
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
		if (!(me instanceof MTrimContainer))
			return;
		// MTrimContainer trimModel = (MTrimContainer) me;

		// TODO Auto-generated method stub
		super.processContents(me);
	}

}
