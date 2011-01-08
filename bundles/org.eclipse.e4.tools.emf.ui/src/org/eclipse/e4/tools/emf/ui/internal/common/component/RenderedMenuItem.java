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

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Composite;

public class RenderedMenuItem extends MenuItemEditor {

	public RenderedMenuItem(EditingDomain editingDomain, ModelEditor editor, IProject project, IResourcePool resourcePool) {
		super(editingDomain, editor, project, resourcePool);
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
