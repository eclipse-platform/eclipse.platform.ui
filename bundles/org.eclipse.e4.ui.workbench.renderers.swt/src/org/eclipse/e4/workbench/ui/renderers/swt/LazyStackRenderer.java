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
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
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
			if (lsr == null)
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
			// NOTE: This code will cause a SELECTED_ELEMENT change on the
			// stack, leading to the tab being shown
			selPart = stack.getChildren().get(0);
			stack.setSelectedElement(selPart);
		}
	}

	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		Widget parentWidget = getParentWidget(me);
		if (parentWidget == null)
			return;

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
			element = ((MPlaceholder) element).getRef();
		}

		// Hide any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			element.setVisible(false);
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
			if (ref instanceof MContext) {
				IEclipseContext context = ((MContext) ref).getContext();
				IEclipseContext newParentContext = getContext(ph);
				if (context.getParent() != newParentContext)
					context.setParent(newParentContext);
			}

			Composite phComp = (Composite) ph.getWidget();
			Control refCtrl = (Control) ph.getRef().getWidget();
			refCtrl.setParent(phComp);
			phComp.layout(new Control[] { refCtrl }, SWT.DEFER);

			element = ref;
		}

		// Show any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
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

	public void swap(MPlaceholder placeholder) {
		MUIElement element = placeholder.getRef();

		MElementContainer<MUIElement> elementParent = element.getParent();
		int elementIndex = elementParent.getChildren().indexOf(element);
		MElementContainer<MUIElement> phParent = placeholder.getParent();
		int phIndex = phParent.getChildren().indexOf(placeholder);

		// Remove the two elements from their respective parents
		elementParent.getChildren().remove(element);
		phParent.getChildren().remove(placeholder);

		// swap over the UIElement info
		boolean onTop = element.isOnTop();
		boolean vis = element.isVisible();
		boolean tbr = element.isToBeRendered();
		String cd = element.getContainerData();

		element.setOnTop(placeholder.isOnTop());
		element.setVisible(placeholder.isVisible());
		element.setToBeRendered(placeholder.isToBeRendered());
		element.setContainerData(placeholder.getContainerData());

		placeholder.setOnTop(onTop);
		placeholder.setVisible(vis);
		placeholder.setToBeRendered(tbr);
		placeholder.setContainerData(cd);

		// Add the elements back into the new parents
		elementParent.getChildren().add(elementIndex, placeholder);
		phParent.getChildren().add(phIndex, element);

		if (elementParent.getSelectedElement() == element)
			elementParent.setSelectedElement(null);

		if (phParent.getSelectedElement() == null)
			phParent.setSelectedElement(element);

		// directly manage the widget reparent if the parent exists
		if (element.getWidget() instanceof Control) {
			// Swap the control's layout data
			// HACK!! for now use the placeholder's 'renderer' att to hold the
			// value
			Control c = (Control) element.getWidget();
			Object phLayoutData = placeholder.getRenderer();
			placeholder.setRenderer(c.getLayoutData());
			c.setLayoutData(phLayoutData);

			// If the new parent has already been rendered directly move the
			// control
			if (phParent.getWidget() instanceof Composite) {
				AbstractPartRenderer renderer = (AbstractPartRenderer) phParent
						.getRenderer();
				Composite newParent = (Composite) renderer
						.getUIContainer(element);
				c.setParent(newParent);
				Control[] changed = { c };

				// Fix the Z-order
				MUIElement prevElement = null;
				for (MUIElement kid : phParent.getChildren()) {
					if (kid == element) {
						if (prevElement == null) {
							c.moveAbove(null); // first one, on top
						} else {
							c.moveBelow((Control) prevElement.getWidget());
						}
					} else if (kid.getWidget() != null) {
						prevElement = kid;
					}
				}
				newParent.getShell().layout(changed, SWT.CHANGED | SWT.DEFER);
				if (newParent instanceof CTabFolder) {
					CTabFolder ctf = (CTabFolder) newParent;
					if (ctf.getSelection() == null) {
						ctf.setSelection(0);
					}
				}
			}
		}
	}
}
