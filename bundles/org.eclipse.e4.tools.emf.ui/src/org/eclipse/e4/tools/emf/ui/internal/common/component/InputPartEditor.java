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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class InputPartEditor extends PartEditor {

	@Inject
	public InputPartEditor() {
		super();
	}

	@Override
	public String getLabel(Object element) {
		return Messages.InputPartEditor_Label;
	}

	@Override
	protected void createSubformElements(Composite parent, EMFDataBindingContext context, IObservableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		ControlFactory.createTextField(parent, Messages.InputPartEditor_InputURI, master, context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.INPUT__INPUT_URI));
	}
}
