/*******************************************************************************
 * Copyright (c) 2014 David Berger <david.berger@logicals.com> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     David Berger <david.berger@logicals.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class E4PartWrapper extends ViewPart {

	public static final String E4_WRAPPER_KEY = "e4Wrapper"; //$NON-NLS-1$
	MPart wrappedPart;

	private E4PartWrapper(MPart part) {
		wrappedPart = part;
		setPartName(part.getLabel());
	}

	public static E4PartWrapper getE4PartWrapper(MPart part) {
		if (part != null) {
			if (part.getTransientData().get(E4_WRAPPER_KEY) instanceof E4PartWrapper) {
				return (E4PartWrapper) part.getTransientData().get(E4_WRAPPER_KEY);
			}
			E4PartWrapper newWrapper = new E4PartWrapper(part);
			part.getTransientData().put(E4_WRAPPER_KEY, newWrapper);
			return newWrapper;
		}
		return null;
	}

	@Override
	public void createPartControl(Composite parent) {
	}

	@Override
	public void setFocus() {
		Object object = wrappedPart.getObject();
		IEclipseContext context = wrappedPart.getContext();
		if (object != null && context != null) {
			ContextInjectionFactory.invoke(object, Focus.class, context);
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Focused: " + object, null); //$NON-NLS-1$
			}
		} else {
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG,
						"Focus not set, object or context missing: " + object + ", " + context, //$NON-NLS-1$ //$NON-NLS-2$
						new IllegalStateException());
			}
		}
	}
}
