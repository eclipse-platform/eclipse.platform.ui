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

import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.emf.edit.domain.EditingDomain;

public class RenderedToolBarEditor extends ToolBarEditor {

	public RenderedToolBarEditor(EditingDomain editingDomain, ModelEditor editor, IResourcePool resourcePool) {
		super(editingDomain, editor, resourcePool);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.RenderedToolBarEditor_TreeLabel;
	}
}
