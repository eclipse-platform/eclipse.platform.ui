/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Uses the default editor for the EClass to calculate the label
 *
 * @author Steven Spungin
 */
public class EClassLabelProvider extends LabelProvider {
	private final ModelEditor editor;

	public EClassLabelProvider(ModelEditor editor) {
		this.editor = editor;
	}

	@Override
	public String getText(Object element) {
		final EClass eclass = (EClass) element;
		final AbstractComponentEditor<?> elementEditor = editor.getEditor(eclass);
		if (elementEditor != null) {
			return elementEditor.getLabel(element);
		}
		return eclass.getName();
	}
}
