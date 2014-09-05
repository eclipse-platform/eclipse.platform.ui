/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.NewPropertySheetHandler;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;

/**
 * @since 3.5
 * 
 */
public class TestNewPropertySheetHandler extends NewPropertySheetHandler {

	public static final String ID = NewPropertySheetHandler.ID + "Test";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.NewPropertySheetHandler#execute(org.eclipse
	 * .core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return super.execute(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.NewPropertySheetHandler#getShowInContext
	 * (org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public PropertyShowInContext getShowInContext(ExecutionEvent event)
			throws ExecutionException {
		return super.getShowInContext(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.NewPropertySheetHandler#findPropertySheet
	 * (org.eclipse.core.commands.ExecutionEvent,
	 * org.eclipse.ui.views.properties.PropertyShowInContext)
	 */
	@Override
	protected PropertySheet findPropertySheet(ExecutionEvent event,
			PropertyShowInContext context) throws PartInitException,
			ExecutionException {
		return super.findPropertySheet(event, context);
	}
}
