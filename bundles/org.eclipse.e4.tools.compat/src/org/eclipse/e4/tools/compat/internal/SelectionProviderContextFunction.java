/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 421453
 ******************************************************************************/
package org.eclipse.e4.tools.compat.internal;

import java.util.List;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.services.ISelectionProviderService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

public class SelectionProviderContextFunction extends ContextFunction {

	@Override
	public Object compute(final IEclipseContext context) {
		return new ISelectionProviderService() {
			@Override
			public void setSelection(Object selection) {
				ISelectionProvider pv = context.get(ISelectionProvider.class);

				if( selection == null ) {
					pv.setSelection(StructuredSelection.EMPTY);
				} else if (selection instanceof ISelection) {
					pv.setSelection((ISelection) selection);
				} else if (selection instanceof List<?>) {
					pv.setSelection(new StructuredSelection((List<?>) selection));
				} else if (selection instanceof Object[]) {
					pv.setSelection(new StructuredSelection(
							(Object[]) selection));
				} else {
					pv.setSelection(new StructuredSelection(selection));
				}
			}
		};
	}
}