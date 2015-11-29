/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.text.templates.persistence.TemplateStore;


/**
 * A content provider for the template preference page's table viewer.
 *
 * @since 3.0
 */
class TemplateContentProvider implements IStructuredContentProvider {

	/** The template store. */
	private TemplateStore fStore;

	@Override
	public Object[] getElements(Object input) {
		return fStore.getTemplateData(false);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fStore= (TemplateStore) newInput;
	}

	@Override
	public void dispose() {
		fStore= null;
	}

}

