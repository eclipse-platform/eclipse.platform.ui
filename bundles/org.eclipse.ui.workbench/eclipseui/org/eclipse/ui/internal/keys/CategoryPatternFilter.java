/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.keys.model.BindingElement;

class CategoryPatternFilter extends PatternFilter {
	private boolean filterCategories;
	final Category uncategorized;

	public CategoryPatternFilter(boolean filterCategories, Category c) {
		uncategorized = c;
		filterCategories(filterCategories);
	}

	public void filterCategories(boolean b) {
		filterCategories = b;
		if (filterCategories) {
			setPattern("org.eclipse.ui.keys.optimization.false"); //$NON-NLS-1$
		} else {
			setPattern("org.eclipse.ui.keys.optimization.true"); //$NON-NLS-1$
		}
	}

	public boolean isFilteringCategories() {
		return filterCategories;
	}

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (filterCategories) {
			final ParameterizedCommand cmd = getCommand(element);
			try {
				if (cmd != null && cmd.getCommand().getCategory() == uncategorized) {
					return false;
				}
			} catch (NotDefinedException e) {
				return false;
			}
		}
		return super.isLeafMatch(viewer, element);
	}

	private ParameterizedCommand getCommand(Object element) {
		if (element instanceof BindingElement) {
			Object modelObject = ((BindingElement) element).getModelObject();
			if (modelObject instanceof Binding) {
				return ((Binding) modelObject).getParameterizedCommand();
			} else if (modelObject instanceof ParameterizedCommand) {
				return (ParameterizedCommand) modelObject;
			}
		}
		return null;
	}
}
