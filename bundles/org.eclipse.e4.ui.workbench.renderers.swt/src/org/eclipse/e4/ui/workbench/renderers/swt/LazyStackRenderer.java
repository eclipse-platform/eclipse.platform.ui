/*******************************************************************************
 * Copyright (c) 2008, 2021 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 441150, 472654
 *     Fabio Zadrozny (fabiofz@gmail.com) - Bug 436763
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 457939
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 564561
 *     Ole Osterhagen <ole@osterhagen.info> - Issue 230
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.eclipse.core.runtime.Assert.isNotNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
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
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
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
	private EventHandler lazyLoader = event -> {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (!(element instanceof MGenericStack<?>)) {
			return;
		}

		@SuppressWarnings("unchecked")
		MGenericStack<MUIElement> stack = (MGenericStack<MUIElement>) element;
		if (stack.getRenderer() != LazyStackRenderer.this) {
			return;
		}
		LazyStackRenderer lsr = (LazyStackRenderer) stack.getRenderer();

		Control widget = (Control) stack.getWidget();
		widget.setRedraw(false);
		try {
			// Gather up the elements that are being 'hidden' by this change
			MUIElement oldSel = (MUIElement) event.getProperty(UIEvents.EventTags.OLD_VALUE);
			if (oldSel != null) {
				hideElementRecursive(oldSel);
			}

			if (stack.getSelectedElement() != null) {
				lsr.showTab(stack.getSelectedElement());
			}
		} finally {
			widget.setRedraw(true);
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
	public void childRendered(MElementContainer<MUIElement> parentElement, MUIElement element) {
		super.childRendered(parentElement, element);

		if (parentElement.getSelectedElement() != element) {
			// Make sure that everything is hidden
			hideElementRecursive(element);
		}
	}

	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement, MUIElement child) {
		super.hideChild(parentElement, child);

		hideElementRecursive(child);
	}

	@Inject
	@Optional
	private void subscribePartTopicToolbar(@UIEventTopic(UIEvents.Part.TOPIC_TOOLBAR) Event event) {
		Object obj = event.getProperty(UIEvents.EventTags.ELEMENT);
		Object value = event.getProperty(UIEvents.EventTags.NEW_VALUE);
		if (!(obj instanceof MPart) || !(value instanceof MToolBar)) {
			return;
		}

		MUIElement element = (MUIElement) obj;
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}

		MElementContainer<MUIElement> parent = element.getParent();
		if (parent.getRenderer() != LazyStackRenderer.this) {
			return;
		}

		// A new ToolBar is added to a MPart in a lazy stack; make it visible when it is
		// for the selected part, otherwise hide it.
		MToolBar toolbar = (MToolBar) value;
		if (element == parent.getSelectedElement()) {
			toolbar.setVisible(true);
		} else {
			toolbar.setVisible(false);
		}
	}

	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		// Lazy Loading: here we only process the contents through childAdded,
		// we specifically do not render them
		IPresentationEngine renderer = context.get(IPresentationEngine.class);

		for (MUIElement element : me.getChildren()) {
			// Make sure that everything is hidden
			hideElementRecursive(element);

			if (!element.isToBeRendered() || !element.isVisible()) {
				continue;
			}
			boolean lazy = true;

			// Special case: we also render any placeholder that refers to
			// an *existing* part, this doesn't break lazy loading since the
			// part is already there...see bug 378138 for details
			if (element instanceof MPlaceholder) {
				MPlaceholder placeholder = (MPlaceholder) element;
				isNotNull(placeholder.getRef(),
						"Placeholder " + placeholder.getElementId() + " does not point to a valid reference"); //$NON-NLS-1$ //$NON-NLS-2$
				if (placeholder.getRef().getTags().contains(IPresentationEngine.NO_RESTORE)) {
					continue;
				}
				if (placeholder.getRef() instanceof MPart && placeholder.getRef().getWidget() != null) {
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
		if (element == null) {
			return;
		}

		// Recursively hide placeholder refs if reference is current
		if (element instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) element;
			if (ph.getRef() != null && ph.getRef().getCurSharedRef() == ph) {
				hideElementRecursive(ph.getRef());
			}
		}

		// Hide any floating windows
		if (element instanceof MWindow) {
			element.setVisible(false);
		}

		if (element instanceof MPart) {
			MToolBar toolbar = ((MPart) element).getToolbar();
			if (toolbar != null) {
				toolbar.setVisible(false);
			}
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
		if (!element.isToBeRendered()) {
			return;
		}

		if (element instanceof MPartStack && element.getRenderer() instanceof StackRenderer) {
			MPartStack stackModel = (MPartStack) element;

			MUIElement curSel = stackModel.getSelectedElement();

			if (curSel != null) {
				showElementRecursive(curSel);
			}
		}

		if (element instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) element;
			MUIElement ref = ph.getRef();
			ref.setCurSharedRef(ph);

			Composite phComp = (Composite) ph.getWidget();
			Control refCtrl = (Control) ph.getRef().getWidget();

			if (phComp != null && refCtrl != null && refCtrl.getParent() != phComp) {
				refCtrl.setParent(phComp);
				refCtrl.requestLayout();
			}

			showElementRecursive(ref);
		}

		if (element instanceof MPart) {
			MToolBar toolbar = ((MPart) element).getToolbar();
			if (toolbar != null) {
				toolbar.setVisible(true);

				// Ensure that the toolbar control is under its 'real' parent
				AbstractPartRenderer renderer = (AbstractPartRenderer) element.getRenderer();
				if (renderer != null && renderer.getUIContainer(toolbar) instanceof Composite composite
						&& toolbar.getWidget() instanceof Control control && control.getParent() != composite) {
					control.setParent(composite);
				}
			}
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
				if (kid.isToBeRendered() && kid.isVisible()) {
					visCount++;
				}
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
			if (curSel == null && container.getChildren().size() > 0) {
				curSel = container.getChildren().get(0);
			}
			if (curSel != null) {
				showElementRecursive(curSel);
			}
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
