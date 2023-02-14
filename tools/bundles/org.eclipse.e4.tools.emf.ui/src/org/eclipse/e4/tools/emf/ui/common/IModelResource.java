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
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;

public interface IModelResource {
	public IObservableList<EObject> getRoot();

	public boolean isSaveable();

	public IStatus save();

	public EditingDomain getEditingDomain();

	public boolean isDirty();

	public void replaceRoot(EObject eobject);

	public void addModelListener(ModelListener listener);

	public void removeModelListener(ModelListener listener);

	public interface ModelListener {
		public void dirtyChanged();

		public void commandStackChanged();
	}
}
