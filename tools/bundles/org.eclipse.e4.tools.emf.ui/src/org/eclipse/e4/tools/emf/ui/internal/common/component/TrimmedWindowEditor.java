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

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.emf.ecore.EObject;

public class TrimmedWindowEditor extends WindowEditor {
	@Inject
	public TrimmedWindowEditor() {
		super();
	}

	@Override
	public IObservableList<Object> getChildList(Object element) {
		IObservableList<Object> list = super.getChildList(element);

		if (getEditor().isModelFragment() && Util.isImport((EObject) element)) {
			return list;
		}

		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_TRIMMED_WINDOW_TRIMS, E4Properties.windowTrimBars(),
				(MTrimmedWindow) element, Messages.TrimmedWindowEditor_TrimBars));
		return list;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.TrimmedWindowEditor_TreeLabel;
	}
}