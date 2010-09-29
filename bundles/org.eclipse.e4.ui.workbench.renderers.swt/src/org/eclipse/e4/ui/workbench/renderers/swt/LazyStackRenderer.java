/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * This class encapsulates the functionality necessary to manage stacks of parts
 * in a 'lazy loading' manner. For these stacks only the currently 'active'
 * child <b>most</b> be rendered so in this class we over ride that default
 * behavior for processing the stack's contents to prevent all of the contents
 * from being rendered, calling 'childAdded' instead. This not only saves time
 * and SWT resources but is necessary in an IDE world where we must not
 * arbitrarily cause plug-in loading.
 * 
 */
public abstract class LazyStackRenderer extends SWTPartRenderer {
	private EventHandler lazyLoader = new EventHandler() {
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(element instanceof MGenericStack<?>))
				return;

			MGenericStack<MUIElement> stack = (MGenericStack<MUIElement>) element;
			LazyStackRenderer lsr = (LazyStackRenderer) stack.getRenderer();
			if (lsr != LazyStackRenderer.this)
				return;

			// Gather up the elements that are being 'hidden' by this change
			MUIElement oldSel = (MUIElement) event
					.getProperty(UIEvents.EventTags.OLD_VALUE);
			if (oldSel != null) {
				List<MUIElement> goingHidden = new ArrayList<MUIElement>();
				hideElementRecursive(oldSel, goingHidden);
			}

			if (stack.getSelectedElement() != null)
				lsr.showTab(stack.getSelectedElement());
		}
	};;

	public LazyStackRenderer() {
		super();
	}

	public void init(IEventBroker eventBroker) {
		// Ensure that there only ever *one* listener. Each subclass
		// will call this method
		eventBroker.unsubscribe(lazyLoader);

		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), lazyLoader);
	}

	/**
	 * @param eventBroker
	 */
	public void contextDisposed(IEventBroker eventBroker) {
		eventBroker.unsubscribe(lazyLoader);
	}

	public void postProcess(MUIElement element) {
		if (!(element instanceof MGenericStack<?>))
			return;

		MGenericStack<MUIElement> stack = (MGenericStack<MUIElement>) element;
		MUIElement selPart = stack.getSelectedElement();
		if (selPart != null) {
			showTab(selPart);
		} else if (stack.getChildren().size() > 0) {
			// Set the selection to the first renderable element
			for (MUIElement kid : stack.getChildren()) {
				if (kid.isToBeRendered() && kid.isVisible()) {
					stack.setSelectedElement(kid);
					break;
				}
			}
		}
	}

	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		// Lazy Loading: here we only process the contents through childAdded,
		// we specifically do not render them
		for (MUIElement part : me.getChildren()) {
			if (part.isToBeRendered())
				createTab(me, part);
		}
	}

	/**
	 * This method is necessary to allow the parent container to show affordance
	 * (i.e. tabs) for child elements -without- creating the actual part
	 * 
	 * @param me
	 *            The parent model element
	 * @param part
	 *            The child to show the affordance for
	 */
	protected void createTab(MElementContainer<MUIElement> me, MUIElement part) {
	}

	protected void showTab(MUIElement element) {
		// Now process any newly visible elements
		List<MUIElement> becomingVisible = new ArrayList<MUIElement>();
		MUIElement curSel = element.getParent().getSelectedElement();
		if (curSel != null) {
			showElementRecursive(curSel, becomingVisible);
		}
	}

	private void hideElementRecursive(MUIElement element,
			List<MUIElement> goingHidden) {
		if (element == null || element.getWidget() == null)
			return;

		if (element instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) element;
			MUIElement ref = ph.getRef();
			if (ref instanceof MPart && ph.getParent() != null
					&& ph.getParent().getWidget() instanceof CTabFolder) {
				// Reparent the existing Toolbar
				MPart part = (MPart) ref;
				CTabFolder ctf = (CTabFolder) ph.getParent().getWidget();
				IPresentationEngine renderer = part.getContext().get(
						IPresentationEngine.class);

				// Dispose the existing toolbar
				if (ctf.getTopRight() != null) {
					Control curTB = ctf.getTopRight();
					ctf.setTopRight(null);
					if (!curTB.isDisposed()) {
						MUIElement tbME = (MUIElement) curTB
								.getData(AbstractPartRenderer.OWNING_ME);
						if (tbME instanceof MRenderedToolBar)
							renderer.removeGui(tbME);
						else
							curTB.dispose();
					}
				}
			}

			element = ((MPlaceholder) element).getRef();
		}

		// Hide any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			element.setVisible(false);
		}

		// Hide any minimized stacks
		if (element instanceof MPartStack && !element.isVisible()) {
			MWindow window = modelService.getTopLevelWindowFor(element);
			String trimId = element.getElementId() + "(minimized)"; //$NON-NLS-1$
			MPerspective persp = modelService.getPerspectiveFor(element);
			if (persp != null)
				trimId = element.getElementId() + '(' + persp.getElementId()
						+ ')';
			MToolControl trimCtrl = (MToolControl) modelService.find(trimId,
					window);
			if (trimCtrl != null && trimCtrl.getObject() != null) {
				IEclipseContext ctxt = EclipseContextFactory.create();
				ctxt.set("show", false); //$NON-NLS-1$
				ContextInjectionFactory.invoke(trimCtrl.getObject(),
						Execute.class, ctxt);

				trimCtrl.setVisible(false);
			}
		}

		goingHidden.add(element);

		if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being hidden
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			hideElementRecursive(curSel, goingHidden);
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			for (MUIElement childElement : container.getChildren()) {
				hideElementRecursive(childElement, goingHidden);
			}

			// OK, now process detached windows
			if (element instanceof MWindow) {
				for (MWindow w : ((MWindow) element).getWindows()) {
					hideElementRecursive(w, goingHidden);
				}
			} else if (element instanceof MPerspective) {
				for (MWindow w : ((MPerspective) element).getWindows()) {
					hideElementRecursive(w, goingHidden);
				}
			}
		}
	}

	private void showElementRecursive(MUIElement element,
			List<MUIElement> becomingVisible) {
		if (!element.isToBeRendered())
			return;

		if (element instanceof MPlaceholder && element.getWidget() != null) {
			MPlaceholder ph = (MPlaceholder) element;
			MUIElement ref = ph.getRef();
			ref.setCurSharedRef(ph);

			Composite phComp = (Composite) ph.getWidget();
			Control refCtrl = (Control) ph.getRef().getWidget();
			refCtrl.setParent(phComp);
			phComp.layout(new Control[] { refCtrl }, SWT.DEFER);

			if (ref instanceof MPart
					&& ph.getParent().getWidget() instanceof CTabFolder) {
				// Reparent the existing Toolbar
				MPart part = (MPart) ref;
				CTabFolder ctf = (CTabFolder) ph.getParent().getWidget();
				IPresentationEngine renderer = modelService
						.getContainingContext(ph)
						.get(IPresentationEngine.class);

				// Dispose the existing toolbar
				if (ctf.getTopRight() != null) {
					Control curTB = ctf.getTopRight();
					ctf.setTopRight(null);
					if (!curTB.isDisposed()) {
						MUIElement tbME = (MUIElement) curTB
								.getData(AbstractPartRenderer.OWNING_ME);
						if (tbME instanceof MRenderedToolBar)
							renderer.removeGui(tbME);
						else
							curTB.dispose();
					}
				}

				MToolBar tbModel = part.getToolbar();
				if (tbModel != null) {
					Control c = (Control) renderer.createGui(tbModel, ctf,
							part.getContext());
					ctf.setTopRight(c, SWT.RIGHT | SWT.WRAP);
					ctf.layout();
				}
			}

			element = ref;
		}

		if (element instanceof MContext) {
			IEclipseContext context = ((MContext) element).getContext();
			if (context != null) {
				IEclipseContext newParentContext = modelService
						.getContainingContext(element);
				if (context.getParent() != newParentContext) {
					context.setParent(newParentContext);
				}
			}
		}

		// Show any minimized stacks
		if (element instanceof MPartStack && !element.isVisible()) {
			MWindow window = modelService.getTopLevelWindowFor(element);
			String trimId = element.getElementId() + "(minimized)"; //$NON-NLS-1$
			MPerspective persp = modelService.getPerspectiveFor(element);
			if (persp != null)
				trimId = element.getElementId() + '(' + persp.getElementId()
						+ ')';
			MToolControl trimCtrl = (MToolControl) modelService.find(trimId,
					window);
			if (trimCtrl != null)
				trimCtrl.setVisible(true);
		}

		// Show any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			int visCount = 0;
			for (MUIElement kid : ((MWindow) element).getChildren()) {
				if (kid.isToBeRendered() && kid.isVisible())
					visCount++;
			}
			if (visCount > 0)
				element.setVisible(true);
		}

		becomingVisible.add(element);

		if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being visible
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			if (curSel == null && container.getChildren().size() > 0)
				curSel = container.getChildren().get(0);
			if (curSel != null)
				showElementRecursive(curSel, becomingVisible);
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			List<MUIElement> kids = new ArrayList<MUIElement>(
					container.getChildren());
			for (MUIElement childElement : kids) {
				showElementRecursive(childElement, becomingVisible);
			}

			// OK, now process detached windows
			if (element instanceof MWindow) {
				for (MWindow w : ((MWindow) element).getWindows()) {
					showElementRecursive(w, becomingVisible);
				}
			} else if (element instanceof MPerspective) {
				for (MWindow w : ((MPerspective) element).getWindows()) {
					showElementRecursive(w, becomingVisible);
				}
			}
		}
	}
}
