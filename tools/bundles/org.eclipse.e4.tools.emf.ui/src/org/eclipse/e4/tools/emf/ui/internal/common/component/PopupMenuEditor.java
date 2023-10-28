/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.swt.widgets.Composite;

import jakarta.inject.Inject;

public class PopupMenuEditor extends MenuEditor {

	@Inject
	public PopupMenuEditor() {
		super();
	}

	@Override
	protected Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<MMenu> master,
			boolean rootMenu, boolean isImport) {
		Composite comp = super.createForm(parent, context, master, rootMenu, isImport);
		if (!isImport) {
			ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Context_Properties, UiPackageImpl.Literals.CONTEXT__PROPERTIES, VERTICAL_LIST_WIDGET_INDENT);
			ControlFactory.createStringListWidget(comp, Messages, this, Messages.ModelTooling_Context_Variables, UiPackageImpl.Literals.CONTEXT__VARIABLES, VERTICAL_LIST_WIDGET_INDENT);
		}

		return comp;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PopupMenuEditor_TreeLabel;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PopupMenuEditor_TreeLabelDescription;
	}
}
