/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.cleanupaddon;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.SashLayout;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class CleanupAddon {
	@Inject
	IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	@Inject
	MApplication app;

	private EventHandler childrenHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (UIEvents.isREMOVE(event)) {
				final MElementContainer<?> container = (MElementContainer<?>) changedObj;
				MUIElement containerParent = container.getParent();

				// Determine the elements that should *not* ever be auto-destroyed
				if (container instanceof MApplication || container instanceof MPerspectiveStack
						|| container instanceof MMenuElement || container instanceof MTrimBar
						|| container instanceof MToolBar || container instanceof MArea
						|| container.getTags().contains(IPresentationEngine.NO_AUTO_COLLAPSE)) {
					return;
				}

				if (container instanceof MWindow && containerParent instanceof MApplication) {
					return;
				}

				Display display = Display.getCurrent();

				// Stall the removal to handle cases where the container is only transiently empty
				if (display != null) {
					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							// Remove it from the display if no visible children
							int tbrCount = modelService.toBeRenderedCount(container);

							// Cache the value since setting the TBR may change the result
							boolean lastStack = isLastEditorStack(container);
							if (tbrCount == 0 && !lastStack) {
								container.setToBeRendered(false);
							}

							// Remove it from the model if it has no children at all
							MElementContainer<?> lclContainer = container;
							if (lclContainer.getChildren().size() == 0) {
								MElementContainer<MUIElement> parent = container.getParent();
								if (parent != null && !lastStack) {
									container.setToBeRendered(false);
									parent.getChildren().remove(container);
								} else if (container instanceof MWindow) {
									// Must be a Detached Window
									MUIElement eParent = (MUIElement) ((EObject) container)
											.eContainer();
									if (eParent instanceof MPerspective) {
										((MPerspective) eParent).getWindows().remove(container);
									} else if (eParent instanceof MWindow) {
										((MWindow) eParent).getWindows().remove(container);
									}
								}
							} else if (container.getChildren().size() == 1
									&& container instanceof MPartSashContainer) {
								// if a sash container has only one element then remove it and move
								// its child up to where it used to be
								MUIElement theChild = container.getChildren().get(0);
								MElementContainer<MUIElement> parentContainer = container
										.getParent();
								if (parentContainer != null) {
									int index = parentContainer.getChildren().indexOf(container);

									// Magic check, are we unwrapping a sash container
									if (theChild instanceof MPartSashContainer) {
										if (container.getWidget() instanceof Composite) {
											Composite theComp = (Composite) container.getWidget();
											Object tmp = theChild.getWidget();
											theChild.setWidget(theComp);
											theComp.setLayout(new SashLayout(theComp, theChild));
											theComp.setData(AbstractPartRenderer.OWNING_ME,
													theChild);
											container.setWidget(tmp);
										}
									}

									theChild.setContainerData(container.getContainerData());
									container.getChildren().remove(theChild);
									parentContainer.getChildren().add(index, theChild);
									container.setToBeRendered(false);
									parentContainer.getChildren().remove(container);
								}
							}
						}
					});
				}
			}
		}
	};

	private EventHandler visibilityChangeHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			MUIElement changedObj = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
			if (changedObj instanceof MTrimBar
					|| ((Object) changedObj.getParent()) instanceof MToolBar)
				return;

			if (changedObj.getWidget() instanceof Shell) {
				((Shell) changedObj.getWidget()).setVisible(changedObj.isVisible());
			} else if (changedObj.getWidget() instanceof Rectangle) {
				if (changedObj.isVisible()) {
					// Make all the parents visible
					MUIElement parent = changedObj.getParent();
					if (!parent.isVisible())
						parent.setVisible(true);
				} else {
					// If there are no more 'visible' children then make the parent go away too
					MElementContainer<MUIElement> parent = changedObj.getParent();
					boolean makeInvisible = true;
					for (MUIElement kid : parent.getChildren()) {
						if (kid.isToBeRendered() && kid.isVisible()) {
							makeInvisible = false;
							break;
						}
					}
					if (makeInvisible)
						parent.setVisible(false);
				}
			} else if (changedObj.getWidget() instanceof Control) {
				Control ctrl = (Control) changedObj.getWidget();
				MElementContainer<MUIElement> parent = changedObj.getParent();
				if (parent == null || ((Object) parent) instanceof MToolBar) {
					return;
				}
				if (changedObj.isVisible()) {
					if (parent.getRenderer() != null) {
						Object myParent = ((AbstractPartRenderer) parent.getRenderer())
								.getUIContainer(changedObj);
						if (myParent instanceof Composite) {
							Composite parentComp = (Composite) myParent;
							ctrl.setParent(parentComp);

							Control prevControl = null;
							for (MUIElement childME : parent.getChildren()) {
								if (childME == changedObj)
									break;
								if (childME.getWidget() instanceof Control && childME.isVisible()) {
									prevControl = (Control) childME.getWidget();
								}
							}
							if (prevControl != null)
								ctrl.moveBelow(prevControl);
							else
								ctrl.moveAbove(null);
							ctrl.getShell().layout(new Control[] { ctrl }, SWT.DEFER);
						}

						// Check if the parent is visible
						if (!parent.isVisible())
							parent.setVisible(true);
					}
				} else {
					Shell limbo = (Shell) app.getContext().get("limbo");

					// Reparent the control to 'limbo'
					Composite curParent = ctrl.getParent();
					ctrl.setParent(limbo);
					curParent.layout(true);
					if (curParent.getShell() != curParent)
						curParent.getShell().layout(new Control[] { curParent }, SWT.DEFER);

					// Always leave Window's in the presentation
					if ((Object) parent instanceof MWindow)
						return;

					// If there are no more 'visible' children then make the parent go away too
					boolean makeParentInvisible = true;
					for (MUIElement kid : parent.getChildren()) {
						if (kid.isToBeRendered() && kid.isVisible()) {
							makeParentInvisible = false;
							break;
						}
					}

					// Special check: If a perspective goes invisibe we need to make its
					// PerspectiveStack invisible as well...see bug 369528
					if (makeParentInvisible)
						parent.setVisible(false);
				}
			}
		}
	};

	private EventHandler renderingChangeHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			MUIElement changedObj = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
			MElementContainer<MUIElement> container = null;
			if (changedObj.getCurSharedRef() != null)
				container = changedObj.getCurSharedRef().getParent();
			else
				container = changedObj.getParent();

			// this can happen for shared parts that aren't attached to any placeholders
			if (container == null) {
				return;
			}

			// never hide top-level windows
			MUIElement containerElement = container;
			if (containerElement instanceof MWindow && containerElement.getParent() != null) {
				return;
			}

			// These elements should neither be shown nor hidden based on their containment state
			if (isLastEditorStack(containerElement) || containerElement instanceof MPerspective
					|| containerElement instanceof MPerspectiveStack)
				return;

			Boolean toBeRendered = (Boolean) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (toBeRendered) {
				// Bring the container back if one of its children goes visible
				if (!container.isToBeRendered())
					container.setToBeRendered(true);
				if (!container.isVisible()
						&& !container.getTags().contains(IPresentationEngine.MINIMIZED))
					container.setVisible(true);
			} else {
				// Never hide the container marked as no_close
				if (container.getTags().contains(IPresentationEngine.NO_AUTO_COLLAPSE)) {
					return;
				}

				int visCount = modelService.countRenderableChildren(container);

				// Remove stacks with no visible children from the display (but not the
				// model)
				final MElementContainer<MUIElement> theContainer = container;
				if (visCount == 0) {
					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							int visCount = modelService.countRenderableChildren(theContainer);
							if (!isLastEditorStack(theContainer) && visCount == 0)
								theContainer.setToBeRendered(false);
						}
					});
				} else {
					// if there are rendered elements but none are 'visible' we should
					// make the container invisible as well
					boolean makeInvisible = true;

					// OK, we have rendered children, are they 'visible' ?
					for (MUIElement kid : container.getChildren()) {
						if (!kid.isToBeRendered())
							continue;
						if (kid.isVisible()) {
							makeInvisible = false;
							break;
						}
					}

					if (makeInvisible) {
						container.setVisible(false);
					}
				}
			}
		}
	};

	@PostConstruct
	void init(IEclipseContext context) {
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, childrenHandler);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, renderingChangeHandler);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_VISIBLE, visibilityChangeHandler);
	}

	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(renderingChangeHandler);
		eventBroker.unsubscribe(visibilityChangeHandler);
	}

	boolean isLastEditorStack(MUIElement element) {
		return modelService.isLastEditorStack(element);
	}
}
