/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x.compat;

import java.util.List;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ISelectionProviderService;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

public class SelectionProviderContextFunction extends ContextFunction {

	@Override
	public Object compute(final IEclipseContext context) {
		return new ISelectionProviderService() {
			
			public void setSelection(Object selection) {
				ISelectionProvider pv = context.get(ISelectionProvider.class);
				
				if( selection instanceof List<?> ) {
					pv.setSelection(new StructuredSelection((List<?>)selection));	
				} else {
					pv.setSelection(new StructuredSelection(selection));
				}
				
			}
		};
	}
}