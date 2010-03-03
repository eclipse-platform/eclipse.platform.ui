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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PartDescriptorEditor extends PartEditor {

	@Override
	protected Composite createForm(Composite parent, DataBindingContext context, IObservableValue master) {
		Composite comp = super.createForm(parent,context,master);

		IValueProperty textProp = WidgetProperties.text();

		Label l = new Label(parent, SWT.NONE);
		l.setText("Label");

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFProperties.value(MApplicationPackage.Literals.UI_LABEL__LABEL).observeDetail(master));

		// ------------------------------------------------------------


		return comp;
	}
}
