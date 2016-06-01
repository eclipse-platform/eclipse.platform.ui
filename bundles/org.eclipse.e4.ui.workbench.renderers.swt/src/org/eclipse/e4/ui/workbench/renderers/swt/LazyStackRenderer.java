/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 441150, 472654
 *     Fabio Zadrozny (fabiofz@gmail.com) - Bug 436763
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 457939
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
		@Override
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(element instanceof MGenericStack<?>))
				return;

			@SuppressWarnings("unchecked")
			MGenericStack<MUIElement> stack = (MGenericStack<MUIElement>) element;
			if (stack.getRenderer() != LazyStackRenderer.this)
				return;
			LazyStackRenderer lsr = (LazyStackRenderer) stack.getRenderer();

			// Gather up the elements that are being 'hidden' by this change
			MUIElement oldSel = (MUIElement) event.getProperty(UIEvents.EventTags.OLD_VALUE);
			if (oldSel != null) {
				hideElementRecursive(oldSel);
			}

			if (stack.getSelectedElement() != null)
				lsr.showTab(stack.getSelectedElement());
		}
	};

	public void init(IEventBroker eventBroker) {
		// Ensure that there only ever *one* listener. Each subclass
		// will call this method
		eventBroker.unsubscribe(lazyLoader);

		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT, lazyLoader);
	}

	/**
	 * @param eventBroker
	 */
	public void contextDisposed(IEventBroker eventBroker) {
		eventBroker.unsubscribe(lazyLoader);
	}

	@Override
	public void postProcess(MUIElement element) {
		if (!(element instanceof MPerspectiveStack)
				&& (!(element instanceof MGenericStack<?>) || isMinimizedStack(element))) {
			return;
		}

		@SuppressWarnings("unchecked")
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
		IPresentationEngine renderer = context.get(IPresentationEngine.class);

		for (MUIElement element : me.getChildren()) {
			if (!element.isToBeRendered() || !element.isVisible())
				continue;
			boolean lazy = true;

			// Special case: we also render any placeholder that refers to
			// an *existing* part, this doesn't break lazy loading since the
			// part is already there...see bug 378138 for details
			if (element instanceof MPlaceholder) {
				MPlaceholder ph = (MPlaceholder) element;
				if (ph.getRef() instanceof MPart && ph.getRef().getWidget() != null) {
					lazy = false;
				}
			}

			if (lazy) {
				createTab(me, element);
			} else {
				renderer.createGui(element);
			}
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
		MUIElement curSel = element.getParent().getSelectedElement();
		if (curSel != null) {
			showElementRecursive(curSel);
		}
	}

	private void hideElementRecursive(MUIElement element) {
		if (element == null || element.getWidget() == null)
			return;

		if (element instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) element;
			element = ph.getRef();
		}

		// Hide any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			element.setVisible(false);
		}

		if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being hidden
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			hideElementRecursive(curSel);
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			for (MUIElement childElement : container.getChildren()) {
				hideElementRecursive(childElement);
			}

			// OK, now process detached windows
			if (element instanceof MWindow) {
				for (MWindow w : ((MWindow) element).getWindows()) {
					hideElementRecursive(w);
				}
			} else if (element instanceof MPerspective) {
				for (MWindow w : ((MPerspective) element).getWindows()) {
					hideElementRecursive(w);
				}
			}
		}
	}

	private void showElementRecursive(MUIElement element) {
		if (!element.isToBeRendered())
			return;

		if (element instanceof MPartStack && element.getRenderer() instanceof StackRenderer) {
			MPartStack stackModel = (MPartStack) element;
			StackRenderer sr = (StackRenderer) element.getRenderer();
			CTabFolder ctf = (CTabFolder) element.getWidget();

			MUIElement curSel = stackModel.getSelectedElement();
			MPart part = (MPart) ((curSel instanceof MPlaceholder) ? ((MPlaceholder) curSel).getRef() : curSel);

			// Ensure that the placeholder's ref is set correctly before
			// adjusting its toolbar
			if (curSel instanceof MPlaceholder) {
				part.setCurSharedRef((MPlaceholder) curSel);
			}
			sr.adjustTopRight(ctf);
		}

		if (element instanceof MPlaceholder && element.getWidget() != null) {
			MPlaceholder ph = (MPlaceholder) element;
			MUIElement ref = ph.getRef();
			ref.setCurSharedRef(ph);

			Composite phComp = (Composite) ph.getWidget();
			Control refCtrl = (Control) ph.getRef().getWidget();

			// If the parent changes we need to adjust the bounds of the child
			// we do not call layout() because this could lead to
			// a big amount of layout calls in unrelated places e.g. none
			// visible children of a CTabFolder (see 460745)
			if (refCtrl.getParent() != phComp) {
				refCtrl.setParent(phComp);
				refCtrl.setSize(phComp.getSize());
			}

			element = ref;
		}

		if (element instanceof MContext) {
			IEclipseContext context = ((MContext) element).getContext();
			if (context != null) {
				IEclipseContext newParentContext = modelService.getContainingContext(element);
				if (context.getParent() != newParentContext) {
					context.setParent(newParentContext);
				}
			}
		}

		Shell layoutShellLater = null;
		// Show any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			int visCount = 0;
			for (MUIElement kid : ((MWindow) element).getChildren()) {
				if (kid.isToBeRendered() && kid.isVisible())
					visCount++;
			}
			if (visCount > 0) {
				element.setVisible(true);
				Object widget = element.getWidget();
				if (widget instanceof Shell) {
					Shell shell = (Shell) widget;
					layoutShellLater = shell;
				}
			}
		}

		if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being visible
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			if (curSel == null && container.getChildren().size() > 0)
				curSel = container.getChildren().get(0);
			if (curSel != null)
				showElementRecursive(curSel);
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			List<MUIElement> kids = new ArrayList<>(container.getChildren());
			for (MUIElement childElement : kids) {
				showElementRecursive(childElement);
			}

			// OK, now process detached windows
			if (element instanceof MWindow) {
				for (MWindow w : ((MWindow) element).getWindows()) {
					showElementRecursive(w);
				}
			} else if (element instanceof MPerspective) {
				for (MWindow w : ((MPerspective) element).getWindows()) {
					showElementRecursive(w);
				}
			}
		}

		// i.e.: Bug 436763: after we make items visible, if we made a new
		// floating shell visible, we have to re-layout it for its contents to
		// become correct.
		if (layoutShellLater != null) {
			layoutShellLater.layout(true, true);
		}
	}

	private boolean isMinimizedStack(MUIElement stack) {
		return stack.getTags().contains(IPresentationEngine.MINIMIZED)
				&& !stack.getTags().contains(IPresentationEngine.ACTIVE);
	}
}
