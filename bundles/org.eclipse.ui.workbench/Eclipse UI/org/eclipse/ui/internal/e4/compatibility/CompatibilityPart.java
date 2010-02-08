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

import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;

public abstract class CompatibilityPart {

	@Inject
	Composite composite;

	@Inject
	@Optional
	Logger logger;

	IWorkbenchPart wrapped;

	MPart part;

	CompatibilityPart(MPart part) {
		this.part = part;
	}

	protected abstract IWorkbenchPart createPart() throws PartInitException;

	protected abstract void initialize(IWorkbenchPart part) throws PartInitException;

	protected void createPartControl(final IWorkbenchPart part, Composite parent) {
		try {
			parent.addListener(SWT.Dispose, new Listener() {
				public void handleEvent(Event event) {
					try {
						ContextInjectionFactory.invoke(part, "dispose", CompatibilityPart.this.part //$NON-NLS-1$
								.getContext(), null);
					} catch (InvocationTargetException e) {
						if (logger != null) {
							logger.error(e);
						}
					}
				}
			});
			part.createPartControl(parent);
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
		}
	}

	public void delegateSetFocus() {
		wrapped.setFocus();
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
				switch (propId) {
				case IWorkbenchPartConstants.PROP_TITLE:
					part.setLabel(wrapped.getTitle());
					break;
				case IWorkbenchPartConstants.PROP_DIRTY:
					if (wrapped instanceof ISaveablePart) {
						((MDirtyable) part).setDirty(((ISaveablePart) wrapped).isDirty());
					}
					break;
				}
			}
		});
	}

	void doSave(@Optional IProgressMonitor monitor) {
		monitor = SubMonitor.convert(monitor);
		if (wrapped instanceof ISaveablePart) {
			((ISaveablePart) wrapped).doSave(monitor);
		}
	}

	public IWorkbenchPart getPart() {
		return wrapped;
	}

}
