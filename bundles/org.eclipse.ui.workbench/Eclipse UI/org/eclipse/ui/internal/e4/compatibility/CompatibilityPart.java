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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.util.Util;

public abstract class CompatibilityPart {

	public static final String COMPATIBILITY_EDITOR_URI = "platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"; //$NON-NLS-1$

	public static final String COMPATIBILITY_VIEW_URI = "platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$

	@Inject
	Composite composite;

	@Inject
	Logger logger;

	IWorkbenchPart wrapped;

	MPart part;

	CompatibilityPart(MPart part) {
		this.part = part;
	}

	public abstract WorkbenchPartReference getReference();

	protected abstract IStatusLineManager getStatusLineManager();

	protected void createPartControl(final IWorkbenchPart legacyPart, Composite parent) {
		parent.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				WorkbenchPartReference reference = getReference();
				// notify the workbench we're being closed
				((WorkbenchPage) reference.getPage()).firePartClosed(CompatibilityPart.this);

				reference.invalidate();

				if (wrapped != null) {
					wrapped.dispose();
				}

				PartSite site = reference.getSite();
				if (site != null) {
					site.dispose();
				}
			}
		});

		try {
			legacyPart.createPartControl(parent);
		} catch (RuntimeException e) {
			logger.error(e);
		}
	}

	public void delegateSetFocus() {
		wrapped.setFocus();
	}

	private String computeLabel() {
		if (wrapped instanceof IWorkbenchPart2) {
			String label = ((IWorkbenchPart2) wrapped).getPartName();
			return Util.safeString(label);
		}

		IWorkbenchPartSite site = wrapped.getSite();
		if (site != null) {
			return Util.safeString(site.getRegisteredName());
		}
		return Util.safeString(wrapped.getTitle());
	}

	@PostConstruct
	public void create() throws PartInitException {
		WorkbenchPartReference reference = getReference();
		// ask our reference to instantiate the part through the registry
		wrapped = reference.createPart();
		// invoke init methods
		reference.initialize(wrapped);
		// hook reference listeners to the part
		// reference.hookPropertyListeners();

		createPartControl(wrapped, composite);
		delegateSetFocus();

		part.setLabel(wrapped.getTitle());
		part.setTooltip(wrapped.getTitleToolTip());

		wrapped.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				switch (propId) {
				case IWorkbenchPartConstants.PROP_TITLE:
					part.setLabel(computeLabel());
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

	public MPart getModel() {
		return part;
	}
}
