/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
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
 *     Steven Spungin <steven@spungin.tv> - Bug 436908
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 372799, 446864
 *     Snjezana Peco <snjezana.peco@redhat.com> - Bug 414888
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 503379
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.internal.ErrorViewPart;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.IWorkbenchPartOrientation;
import org.osgi.service.event.EventHandler;

public abstract class CompatibilityPart implements ISelectionChangedListener {

	public static final String COMPATIBILITY_EDITOR_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"; //$NON-NLS-1$

	public static final String COMPATIBILITY_VIEW_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$

	public static final String ENABLE_DEPENDENCY_INJECTION_FOR_E3_PARTS = "ENABLE_DEPENDENCY_INJECTION_FOR_E3"; ////$NON-NLS-1$

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

	private boolean focusDelegatingInProgress;

	/**
	 * This handler will be notified when the part's widget has been un/set.
	 */
	private EventHandler widgetSetHandler = event -> {
		// check that we're looking at our own part and that the widget is
		// being unset
		if (event.getProperty(UIEvents.EventTags.ELEMENT) == part
				&& event.getProperty(UIEvents.EventTags.NEW_VALUE) == null) {
			Assert.isTrue(!composite.isDisposed(), "The widget should not have been disposed at this point"); //$NON-NLS-1$
			beingDisposed = true;
			WorkbenchPartReference reference = getReference();
			// notify the workbench we're being closed
			WorkbenchPage page = (WorkbenchPage) reference.getPage();
			if (page == null) {
				return;
			}
			page.firePartDeactivatedIfActive(part);
			page.firePartHidden(part);
			page.firePartClosed(CompatibilityPart.this);
		}
	};

	/**
	 * This handler will be notified when the part's client object has been un/set.
	 */
	private EventHandler objectSetHandler = event -> {
		// check that we're looking at our own part and that the object is
		// being set
		if (event.getProperty(UIEvents.EventTags.ELEMENT) == part
				&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
			WorkbenchPartReference reference = getReference();
			// notify the workbench we've been opened
			((WorkbenchPage) reference.getPage()).firePartOpened(CompatibilityPart.this);
		}
	};

	private ISelectionChangedListener postListener = e -> {
		ESelectionService selectionService = part.getContext().get(ESelectionService.class);
		selectionService.setPostSelection(e.getSelection());
	};

	private IWorkbenchPart legacyPart;

	CompatibilityPart(MPart part) {
		this.part = part;
	}

	@PersistState
	void persistState() {
		ContextInjectionFactory.invoke(wrapped, PersistState.class, part.getContext(), null);
	}

	public abstract WorkbenchPartReference getReference();

	protected boolean createPartControl(final IWorkbenchPart legacyPart, Composite parent) {
		this.legacyPart = legacyPart;
		IWorkbenchPartSite site = null;
		try {
			site = legacyPart.getSite();
			part.getContext().set(Composite.class, parent);
			// call createPartControl after dependency injection
			if (part.getTags().contains(IWorkbenchConstants.TAG_USE_DEPENDENCY_INJECTION)) {
				ContextInjectionFactory.inject(legacyPart, part.getContext());
			}
			legacyPart.createPartControl(parent);

		} catch (Throwable e) {
			logger.error(e);

			try {
				// couldn't create the part, dispose of it
				legacyPart.dispose();
			} catch (Exception ex) {
				// client code may have errors so we need to catch it
				logger.error(ex);
			}

			// dispose the site that was originally initialized for this part
			internalDisposeSite(site);

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
				WorkbenchPlugin.log("Unable to initialize error part", ex); //$NON-NLS-1$
			}
		}

		if (site != null) {
			ISelectionProvider selectionProvider = site.getSelectionProvider();
			if (selectionProvider != null) {
				selectionProvider.addSelectionChangedListener(this);

				if (selectionProvider instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) selectionProvider).addPostSelectionChangedListener(postListener);
				} else {
					selectionProvider.addSelectionChangedListener(postListener);
				}
				ESelectionService selectionService = part.getContext().get(ESelectionService.class);
				selectionService.setSelection(selectionProvider.getSelection());
			}
		}
		return true;
	}

	@Focus
	void delegateSetFocus() {
		if (focusDelegatingInProgress) {
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Ignored attempt to set focus during set focus for: " + this, //$NON-NLS-1$
						null);
			}
			return;
		}
		if (Policy.DEBUG_FOCUS) {
			Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Starting set focus for: " + this, null); //$NON-NLS-1$
		}
		focusDelegatingInProgress = true;
		try {
			// first involve @Focus if present
			if (part.getTags().contains(IWorkbenchConstants.TAG_USE_DEPENDENCY_INJECTION)) {
				ContextInjectionFactory.invoke(wrapped, Focus.class, part.getContext(), null);
			}
			// ensure to comply to the 3.x API contract
			wrapped.setFocus();
		} catch (Exception e) {
			if (logger != null) {
				String msg = "Error setting focus to : " + part.getClass().getName(); //$NON-NLS-1$
				msg += ' ' + part.getLocalizedLabel();
				logger.error(e, msg);
			}
		} finally {
			focusDelegatingInProgress = false;
		}
		if (Policy.DEBUG_FOCUS) {
			Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Done with set focus for: " + this, null); //$NON-NLS-1$
		}
	}

	private void invalidate() {
		IWorkbenchPartSite site = null;
		if (wrapped != null) {
			site = wrapped.getSite();
			if (site != null) {
				ISelectionProvider selectionProvider = site.getSelectionProvider();
				if (selectionProvider != null) {
					selectionProvider.removeSelectionChangedListener(this);

					if (selectionProvider instanceof IPostSelectionProvider) {
						((IPostSelectionProvider) selectionProvider).removePostSelectionChangedListener(postListener);
					} else {
						selectionProvider.removeSelectionChangedListener(postListener);
					}
				}
			}
		}

		WorkbenchPartReference reference = getReference();
		reference.invalidate();

		if (wrapped != null) {
			try {
				wrapped.dispose();
			} catch (Exception e) {
				// client code may have errors so we need to catch it
				logger.error(e);
			}
			wrapped = null;
		}

		internalDisposeSite(site);
		alreadyDisposed = true;
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

	/**
	 * Returns whether this part is being disposed. This is used for invalidating
	 * this part so that it is not returned when a method expects a "working" part.
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
		WorkbenchPlugin.log("Unable to create part", e); //$NON-NLS-1$
		WorkbenchPartReference reference = getReference();
		IWorkbenchPartSite site = reference.getSite();
		reference.invalidate();
		if (wrapped instanceof IEditorPart) {
			try {
				wrapped.dispose();
			} catch (Exception ex) {
				// client code may have errors so we need to catch it
				logger.error(ex);
			}
		}
		internalDisposeSite(site);

		alreadyDisposed = false;

		wrapped = reference.createErrorPart(e.getStatus());
		try {
			reference.initialize(wrapped);
		} catch (PartInitException ex) {
			WorkbenchPlugin.log("Unable to initialize error part", ex); //$NON-NLS-1$
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
		} catch (Throwable e) {
			if (!handlePartInitException(new PartInitException(e.getMessage(), e))) {
				return;
			}
		}

		// hook reference listeners to the part
		// reference.hookPropertyListeners();

		int style = SWT.NONE;
		if (wrapped instanceof IWorkbenchPartOrientation) {
			style = ((IWorkbenchPartOrientation) wrapped).getOrientation();
		}

		Composite parent = new Composite(composite, style);
		parent.setLayout(new FillLayout());
		if (!createPartControl(wrapped, parent)) {
			return;
		}

		// Only update 'valid' parts
		if (!(wrapped instanceof ErrorEditorPart) && !(wrapped instanceof ErrorViewPart)) {
			part.setLabel(computeLabel());
			part.getTransientData().put(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY, wrapped.getTitleToolTip());
			part.getTransientData().put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY, wrapped.getTitleImage());
		}

		ISaveablePart saveable = SaveableHelper.getSaveable(wrapped);
		if (saveable != null && SaveableHelper.isDirtyStateSupported(wrapped)) {
			part.setDirty(saveable.isDirty());
		}

		wrapped.addPropertyListener((source, propId) -> {
			switch (propId) {
			case IWorkbenchPartConstants.PROP_TITLE:
				part.setLabel(computeLabel());

				if (wrapped.getTitleImage() != null) {
					Image newImage = wrapped.getTitleImage();
					part.getTransientData().put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY, newImage);
				}
				String titleToolTip = wrapped.getTitleToolTip();
				if (titleToolTip != null) {
					part.getTransientData().put(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY, titleToolTip);
				}
				break;
			case IWorkbenchPartConstants.PROP_DIRTY:
				boolean supportsDirtyState = SaveableHelper.isDirtyStateSupported(wrapped);
				if (!supportsDirtyState) {
					part.setDirty(false);
					return;
				}
				ISaveablePart saveable1 = SaveableHelper.getSaveable(wrapped);
				if (saveable1 != null) {
					part.setDirty(saveable1.isDirty());
				} else if (part.isDirty()) {
					// reset if the wrapped legacy part do not exposes
					// saveable adapter anymore, see bug 495567 comment 6
					part.setDirty(false);
				}
				break;
			case IWorkbenchPartConstants.PROP_INPUT:
				WorkbenchPartReference ref = getReference();
				((WorkbenchPage) ref.getSite().getPage()).firePartInputChanged(ref);
				break;
			}
		});
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
	 * Disposes of the 3.x part's site if it has one. Subclasses may override but
	 * must call <code>super.disposeSite()</code> in its implementation.
	 */
	private void internalDisposeSite(IWorkbenchPartSite site) {
		if (site instanceof PartSite) {
			disposeSite((PartSite) site);
		}
	}

	/**
	 * Disposes of the 3.x part's site if it has one. Subclasses may override but
	 * must call <code>super.disposeSite()</code> in its implementation.
	 */
	void disposeSite(PartSite site) {
		site.dispose();
		if (part.getTags().contains(IWorkbenchConstants.TAG_USE_DEPENDENCY_INJECTION)) {
			ContextInjectionFactory.uninject(legacyPart, part.getContext());
		}
		legacyPart = null;
	}

	@Persist
	void doSave() {
		ISaveablePart saveable = SaveableHelper.getSaveable(wrapped);
		if (saveable != null) {
			SaveableHelper.savePart(saveable, wrapped, getReference().getSite().getWorkbenchWindow(), false);
		}
		// ContextInjectionFactory.invoke(wrapped, Persist.class, part.getContext(),
		// null);
	}

	public IWorkbenchPart getPart() {
		return wrapped;
	}

	public MPart getModel() {
		return part;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		ESelectionService selectionService = part.getContext().get(ESelectionService.class);
		selectionService.setSelection(e.getSelection());
	}

	protected void clearMenuItems() {
		// in the workbench, view menus are re-created on startup
		for (MMenu menu : part.getMenus()) {
			menu.getChildren().clear();
		}
	}

	@Override
	public String toString() {
		// WARNING: don't call part.toString() here because model can to try to
		// prints us and cause stack overflow
		StringBuilder builder = new StringBuilder();
		builder.append("CompatibilityPart ["); //$NON-NLS-1$
		if (part != null) {
			builder.append("partId="); //$NON-NLS-1$
			builder.append(part.getElementId());
			builder.append(", properties="); //$NON-NLS-1$
			builder.append(part.getProperties());
			builder.append(", tags="); //$NON-NLS-1$
			builder.append(part.getTags());
		}
		if (wrapped != null) {
			builder.append(", wrapped="); //$NON-NLS-1$
			builder.append(wrapped.getClass());
		}
		if (legacyPart != null) {
			builder.append(", legacyPart="); //$NON-NLS-1$
			builder.append(legacyPart.getClass());
		}
		builder.append(", beingDisposed="); //$NON-NLS-1$
		builder.append(beingDisposed);
		builder.append(", alreadyDisposed="); //$NON-NLS-1$
		builder.append(alreadyDisposed);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

}
