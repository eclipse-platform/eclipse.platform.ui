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
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.swt.widgets.Composite;

public class RenderedMenuItem extends MenuItemEditor {

	@Inject
	public RenderedMenuItem() {
		super();
	}

	@Override
	protected void createFormSubTypeForm(Composite parent, EMFDataBindingContext context, WritableValue master) {

	}

	@Override
	public String getLabel(Object element) {
		return Messages.RenderedMenuItem_TreeLabel;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.RenderedMenuItem_TreeLabelDescription;
	}
}
