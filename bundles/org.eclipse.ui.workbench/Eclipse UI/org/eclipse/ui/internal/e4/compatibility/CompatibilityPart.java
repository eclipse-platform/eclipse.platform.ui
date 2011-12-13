/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.internal.ErrorViewPart;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public abstract class CompatibilityPart {

	public static final String COMPATIBILITY_EDITOR_URI = "platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"; //$NON-NLS-1$

	public static final String COMPATIBILITY_VIEW_URI = "platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$

	@Inject
	Composite composite;

	@Inject
	Logger logger;

	IWorkbenchPart wrapped;

	MPart part;

	@Inject
	private IEventBroker eventBroker;

	private boolean beingDisposed = false;

	private boolean alreadyDisposed = false;

	/**
	 * This handler will be notified when the part's widget has been un/set.
	 */
	private EventHandler widgetSetHandler = new EventHandler() {
		public void handleEvent(Event event) {
			// check that we're looking at our own part and that the widget is
			// being unset
			if (event.getProperty(UIEvents.EventTags.ELEMENT) == part
					&& event.getProperty(UIEvents.EventTags.NEW_VALUE) == null) {
				 Assert.isTrue(!composite.isDisposed(),
										"The widget should not have been disposed at this point"); //$NON-NLS-1$
				beingDisposed = true;
				WorkbenchPartReference reference = getReference();
				// notify the workbench we're being closed
				((WorkbenchPage) reference.getPage()).firePartClosed(CompatibilityPart.this);
			}
		}
	};

	/**
	 * This handler will be notified when the part's client object has been
	 * un/set.
	 */
	private EventHandler objectSetHandler = new EventHandler() {
		public void handleEvent(Event event) {
			// check that we're looking at our own part and that the object is
			// being set
			if (event.getProperty(UIEvents.EventTags.ELEMENT) == part
					&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
				WorkbenchPartReference reference = getReference();
				// notify the workbench we've been opened
				((WorkbenchPage) reference.getPage()).firePartOpened(CompatibilityPart.this);
			}
		}
	};

	CompatibilityPart(MPart part) {
		this.part = part;
	}

	public abstract WorkbenchPartReference getReference();

	protected boolean createPartControl(final IWorkbenchPart legacyPart, Composite parent) {
		try {
			legacyPart.createPartControl(parent);
		} catch (RuntimeException e) {
			logger.error(e);

			try {
				// couldn't create the part, dispose of it
				legacyPart.dispose();
			} catch (Exception ex) {
				// client code may have errors so we need to catch it
				logger.error(ex);
			}

			// dispose the site that was originally initialized for this part
			internalDisposeSite();

			// create a new error part notifying the user of the failure
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					"Failed to create the part's controls", e); //$NON-NLS-1$
			WorkbenchPartReference reference = getReference();
			wrapped = reference.createErrorPart(status);
			try {
				reference.initialize(wrapped);
				wrapped.createPartControl(parent);
			} catch (RuntimeException ex) {
				// failed to create the error part, log it
				logger.error(ex);
			} catch (PartInitException ex) {
				WorkbenchPlugin.log("Unable to initialize error part", ex.getStatus()); //$NON-NLS-1$
			}
		}
		return true;
	}

	@Focus
	void delegateSetFocus() {
		try {
			wrapped.setFocus();
		} catch (Exception e) {
			if (logger != null) {
				String msg = "Error setting focus to : " + part.getClass().getName(); //$NON-NLS-1$
				msg += ' ' + part.getLocalizedLabel();
				logger.error(e, msg);
			}
		}
	}

	private void invalidate() {
		WorkbenchPartReference reference = getReference();
		reference.invalidate();

		if (wrapped != null) {
			try {
				wrapped.dispose();
			} catch (Exception e) {
				// client code may have errors so we need to catch it
				logger.error(e);
			}
		}

		internalDisposeSite();
		alreadyDisposed = true;
	}

	private String computeLabel() {
		if (wrapped instanceof ErrorEditorPart || wrapped instanceof ErrorViewPart) {
			return getReference().getTitle();
		}
		
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

	/**
	 * Returns whether this part is being disposed. This is used for
	 * invalidating this part so that it is not returned when a method expects a
	 * "working" part.
	 * <p>
	 * See bug 308492.
	 * </p>
	 * 
	 * @return if the part is currently being disposed
	 */
	public boolean isBeingDisposed() {
		return beingDisposed;
	}

	IWorkbenchPart createPart(WorkbenchPartReference reference) throws PartInitException {
		// ask our reference to instantiate the part through the registry
		return reference.createPart();
	}

	boolean handlePartInitException(PartInitException e) {
		WorkbenchPartReference reference = getReference();
		reference.invalidate();
		if (wrapped instanceof IEditorPart) {
			try {
				wrapped.dispose();
			} catch (Exception ex) {
				// client code may have errors so we need to catch it
				logger.error(ex);
			}
		}
		internalDisposeSite();

		alreadyDisposed = false;
		WorkbenchPlugin.log("Unable to create part", e.getStatus()); //$NON-NLS-1$

		wrapped = reference.createErrorPart(e.getStatus());
		try {
			reference.initialize(wrapped);
		} catch (PartInitException ex) {
			WorkbenchPlugin.log("Unable to initialize error part", ex.getStatus()); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	@PostConstruct
	public void create() {
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, widgetSetHandler);
		eventBroker.subscribe(UIEvents.Contribution.TOPIC_OBJECT, objectSetHandler);

		WorkbenchPartReference reference = getReference();

		try {
			wrapped = createPart(reference);
			// invoke init methods
			reference.initialize(wrapped);
		} catch (PartInitException e) {
			if (!handlePartInitException(e)) {
				return;
			}
		}

		// hook reference listeners to the part
		// reference.hookPropertyListeners();

		Composite parent = new Composite(composite, SWT.NONE);
		parent.setLayout(new FillLayout());
		if (!createPartControl(wrapped, parent)) {
			return;
		}

		part.setLabel(computeLabel());
		part.setTooltip(wrapped.getTitleToolTip());
		updateImages(part);

		if (wrapped instanceof ISaveablePart) {
			part.setDirty(((ISaveablePart) wrapped).isDirty());
		}

		wrapped.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				switch (propId) {
				case IWorkbenchPartConstants.PROP_TITLE:
					part.setLabel(computeLabel());
					part.setTooltip(wrapped.getTitleToolTip());

					updateImages(part);
					break;
				case IWorkbenchPartConstants.PROP_DIRTY:
					if (wrapped instanceof ISaveablePart) {
						((MDirtyable) part).setDirty(((ISaveablePart) wrapped).isDirty());
					}
					break;
				case IWorkbenchPartConstants.PROP_INPUT:
					WorkbenchPartReference ref = getReference();
					((WorkbenchPage) ref.getSite().getPage()).firePartInputChanged(ref);
					break;
				}
			}
		});
	}

	void updateTabImages(MUIElement element) {
		// Try to update the image if we're using a CTF
		MUIElement refParent = element.getParent();
		if (!(refParent instanceof MPartStack)) {
			return;
		}

		if (!(refParent.getWidget() instanceof CTabFolder)) {
			return;
		}

		CTabFolder ctf = (CTabFolder) refParent.getWidget();
		if (ctf.isDisposed()) {
			return;
		}

		CTabItem[] items = ctf.getItems();
		for (CTabItem item : items) {
			if (item.getData(AbstractPartRenderer.OWNING_ME) == element) {
				item.setImage(wrapped.getTitleImage());
			}
		}
	}

	abstract void updateImages(MPart part);

	public void deactivateActionBars(boolean forceHide) {
		PartSite site = getReference().getSite();
		site.deactivateActionBars(forceHide);
	}

	@PreDestroy
	void destroy() {
		if (!alreadyDisposed) {
			invalidate();
		}

		eventBroker.unsubscribe(widgetSetHandler);
		eventBroker.unsubscribe(objectSetHandler);
	}

	/**
	 * Disposes of the 3.x part's site if it has one. Subclasses may override
	 * but must call <code>super.disposeSite()</code> in its implementation.
	 */
	private void internalDisposeSite() {
		PartSite site = getReference().getSite();
		if (site != null) {
			disposeSite(site);
		}
	}

	/**
	 * Disposes of the 3.x part's site if it has one. Subclasses may override
	 * but must call <code>super.disposeSite()</code> in its implementation.
	 */
	void disposeSite(PartSite site) {
		deactivateActionBars(site instanceof ViewSite);
		site.dispose();
	}

	@Persist
	void doSave() {
		if (wrapped instanceof ISaveablePart) {
			SaveableHelper.savePart((ISaveablePart) wrapped, wrapped, getReference().getSite()
					.getWorkbenchWindow(), false);
		}
	}

	public IWorkbenchPart getPart() {
		return wrapped;
	}

	public MPart getModel() {
		return part;
	}
}
