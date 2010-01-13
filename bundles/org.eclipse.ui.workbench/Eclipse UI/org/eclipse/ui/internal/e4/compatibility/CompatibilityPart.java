/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import javax.inject.Inject;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;

public abstract class CompatibilityPart {

	@Inject
	MPart part;

	@Inject
	Composite composite;

	IWorkbenchPart wrapped;

	protected abstract IWorkbenchPart createPart() throws PartInitException;

	protected abstract void initialize(IWorkbenchPart part) throws PartInitException;

	protected void createPartControl(IWorkbenchPart part, Composite parent) {
		part.createPartControl(parent);
	}

	public void delegateSetFocus() {
		wrapped.setFocus();
	}

	public void delegateDispose() {
		wrapped.dispose();
	}

	@PostConstruct
	public void create() throws PartInitException {
		wrapped = createPart();
		initialize(wrapped);
		createPartControl(wrapped, composite);
		delegateSetFocus();

		part.setLabel(wrapped.getTitle());
		part.setTooltip(wrapped.getTitleToolTip());

		wrapped.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				if (propId == IWorkbenchPartConstants.PROP_TITLE) {
					part.setLabel(wrapped.getTitle());
				}
			}
		});
	}

	public IWorkbenchPart getPart() {
		return wrapped;
	}

}
