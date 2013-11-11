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
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EObject;

public class TrimmedWindowEditor extends WindowEditor {
	private IListProperty TRIMMED_WINDOW__TRIM_BARS = EMFProperties.list(BasicPackageImpl.Literals.TRIMMED_WINDOW__TRIM_BARS);

	@Inject
	public TrimmedWindowEditor() {
		super();
	}

	@Override
	public IObservableList getChildList(Object element) {
		IObservableList list = super.getChildList(element);

		if (getEditor().isModelFragment() && Util.isImport((EObject) element)) {
			return list;
		}

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_TRIMMED_WINDOW_TRIMS, TRIMMED_WINDOW__TRIM_BARS, element, Messages.TrimmedWindowEditor_TrimBars) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		return list;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.TrimmedWindowEditor_TreeLabel;
	}
}