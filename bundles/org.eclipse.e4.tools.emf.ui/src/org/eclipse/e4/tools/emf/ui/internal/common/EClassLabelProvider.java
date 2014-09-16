/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Uses the default editor for the EClass to calculate the label
 *
 * @author Steven Spungin
 *
 */
public class EClassLabelProvider extends LabelProvider {
	private ModelEditor editor;

	public EClassLabelProvider(ModelEditor editor) {
		this.editor = editor;
	}

	@Override
	public String getText(Object element) {
		EClass eclass = (EClass) element;
		AbstractComponentEditor elementEditor = editor.getEditor(eclass);
		if (elementEditor != null) {
			return elementEditor.getLabel(element);
		} else {
			return eclass.getName();
		}
	}
}
