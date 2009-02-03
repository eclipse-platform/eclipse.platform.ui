/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionServiceValue implements IComputedValue {
	static class SelectionWritableValue extends WritableValue implements
			ISelectionService {

		public Object getSelection(Class api) {
			Object value = getValue();
			if (api.isInstance(value)) {
				return value;
			}

			if (value instanceof IStructuredSelection) {
				value = ((IStructuredSelection) value).getFirstElement();
			}
			if (api.isInstance(value)) {
				return value;
			} else if (value != null) {
				return Platform.getAdapterManager().loadAdapter(value,
						api.getName());
			}
			return null;
		}
	}

	public Object compute(IEclipseContext context) {
		return new SelectionWritableValue();
	}

}
