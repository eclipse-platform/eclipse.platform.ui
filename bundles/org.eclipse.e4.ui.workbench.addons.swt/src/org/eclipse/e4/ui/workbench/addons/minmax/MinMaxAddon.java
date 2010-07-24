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

package org.eclipse.e4.ui.workbench.addons.minmax;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabFolder2Adapter;
import org.eclipse.e4.ui.widgets.CTabFolderEvent;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPageLayout;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Addon supporting standard drag and drop management
 */
public class MinMaxAddon {
	private static String trimURI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack"; //$NON-NLS-1$

	static String ID_SUFFIX = "(minimized)"; //$NON-NLS-1$

	// tags representing the min/max state
	public static String MINIMIZED = "Minimized"; //$NON-NLS-1$
	public static String MINIMIZED_BY_ZOOM = "MinimizedByZoom"; //$NON-NLS-1$
	public static String EA_MAXIMIZED = "EAMaximized"; //$NON-NLS-1$

	@Inject
	IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	@Inject
	private IEclipseContext context;

	private EventHandler ctfListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			Widget widget = (Widget) event.getProperty(EventTags.NEW_VALUE);
			if (changedElement instanceof MPartStack && widget instanceof CTabFolder
					&& changedElement.getElementId() != null) {
				final CTabFolder folder = (CTabFolder) widget;
				if (!changedElement.getTags().contains("EditorStack")) { //$NON-NLS-1$
					folder.setMinimizeVisible(true);
					folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
						public void minimize(CTabFolderEvent event) {
							changedElement.getTags().add(MINIMIZED);
						}

						public void restore(CTabFolderEvent event) {
							changedElement.getTags().remove(MINIMIZED);
						}
					});
				} else {
					folder.setMaximizeVisible(true);
					folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
						public void maximize(CTabFolderEvent event) {
							MWindow window = modelService.getTopLevelWindowFor(changedElement);
							MPerspective curPersp = modelService.getActivePerspective(window);
							curPersp.getTags().add(EA_MAXIMIZED);
						}

						public void restore(CTabFolderEvent event) {
							MWindow window = modelService.getTopLevelWindowFor(changedElement);
							MPerspective curPersp = modelService.getActivePerspective(window);
							curPersp.getTags().remove(EA_MAXIMIZED);
						}
					});

					folder.addMouseListener(new MouseListener() {
						public void mouseUp(MouseEvent e) {
						}

						public void mouseDown(MouseEvent e) {
						}

						public void mouseDoubleClick(MouseEvent e) {
							MWindow window = modelService.getTopLevelWindowFor(changedElement);
							MPerspective curPersp = modelService.getActivePerspective(window);
							if (curPersp.getTags().contains(EA_MAXIMIZED))
								curPersp.getTags().remove(EA_MAXIMIZED);
							else
								curPersp.getTags().add(EA_MAXIMIZED);
						}
					});
				}
			}
		}
	};

	private EventHandler perspectiveChangeListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MPerspectiveStack))
				return;

			MPerspectiveStack ps = (MPerspectiveStack) changedElement;
			final MPerspective curPersp = ps.getSelectedElement();
			if (curPersp != null) {
				MWindow win = modelService.getTopLevelWindowFor(curPersp);
				MPartStack eStack = getEditorStack(win);
				CTabFolder ctf = (CTabFolder) eStack.getWidget();
				if (ctf != null) {
					// Set the CTF state
					boolean isMax = curPersp.getTags().contains(EA_MAXIMIZED);
					ctf.setMaximized(isMax);
				}
			}
		}
	};

	private EventHandler initListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (changedElement.getWidget() instanceof CTabFolder
					&& changedElement.getTags().contains("EditorStack")) { //$NON-NLS-1$
				CTabFolder ctf = (CTabFolder) changedElement.getWidget();
				MWindow win = modelService.getTopLevelWindowFor(changedElement);
				MPerspective curPersp = modelService.getActivePerspective(win);
				if (curPersp.getTags().contains(EA_MAXIMIZED))
					ctf.setMaximized(true);

				// This is for startup only...
				eventBroker.unsubscribe(initListener);
			}
		}
	};

	private EventHandler tagChangeListener = new EventHandler() {
		public void handleEvent(Event event) {
			final Object changedElement = event.getProperty(EventTags.ELEMENT);
			String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);
			String tag = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			String oldVal = (String) event.getProperty(UIEvents.EventTags.OLD_VALUE);
			if (UIEvents.EventTypes.ADD.equals(eventType)) {
				if (MINIMIZED.equals(tag)) {
					handleMinimize((MUIElement) changedElement);
				} else if (EA_MAXIMIZED.equals(tag)) {
					MPerspective persp = (MPerspective) changedElement;
					MWindow win = modelService.getTopLevelWindowFor(persp);
					maximizeEA(getEditorStack(win));
				}
			} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				if (MINIMIZED.equals(oldVal)) {
					handleRestore((MUIElement) changedElement);
				} else if (EA_MAXIMIZED.equals(oldVal)) {
					MPerspective persp = (MPerspective) changedElement;
					MWindow win = modelService.getTopLevelWindowFor(persp);
					unmaximizeEA(getEditorStack(win));
				}
			}
		}

		private void handleRestore(MUIElement element) {
			if (element instanceof MPartStack && element.getWidget() instanceof CTabFolder) {
				restoreStack((MPartStack) element);
			}
		}

		private void handleMinimize(MUIElement element) {
			if (element instanceof MPartStack && element.getWidget() instanceof CTabFolder) {
				minimizeStack((MPartStack) element);
			}
		}
	};

	private EventHandler perspectiveRemovedListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MPerspectiveStack))
				return;

			String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);
			if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				MUIElement removed = (MUIElement) event.getProperty(UIEvents.EventTags.OLD_VALUE);
				String perspectiveId = removed.getElementId();
				// System.out.println("Perspective Removed: " + removed.getElementId());
				MWindow window = modelService.getTopLevelWindowFor(changedElement);
				MTrimBar bar = modelService.getTrim((MTrimmedWindow) window, SideValue.TOP);

				// gather up any minimized stacks for this perspective...
				List<MToolControl> toRemove = new ArrayList<MToolControl>();
				for (MUIElement child : bar.getChildren()) {
					String trimElementId = child.getElementId();
					if (child instanceof MToolControl && trimElementId.contains(perspectiveId)) {
						toRemove.add((MToolControl) child);
					}
				}

				// ...and remove them
				for (MToolControl minStack : toRemove) {
					minStack.setToBeRendered(false);
					bar.getChildren().remove(minStack);
				}
			}
		}
	};

	@PostConstruct
	void hookListeners() {
		String topic = UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET);
		eventBroker.subscribe(topic, null, ctfListener, false);
		topic = UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN);
		eventBroker.subscribe(topic, null, perspectiveRemovedListener, false);
		topic = UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT);
		eventBroker.subscribe(topic, null, perspectiveChangeListener, false);
		topic = UIEvents.buildTopic(UIEvents.ApplicationElement.TOPIC,
				UIEvents.ApplicationElement.TAGS);
		eventBroker.subscribe(topic, null, tagChangeListener, false);
		topic = UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET);
		eventBroker.subscribe(topic, null, initListener, false);
	}

	/**
	 * @param win
	 * @return
	 */
	protected MPartStack getEditorStack(MWindow win) {
		MUIElement ea = modelService.find(IPageLayout.ID_EDITOR_AREA, win);
		List<MPartStack> eStacks = modelService.findElements(ea, null, MPartStack.class, null);
		if (eStacks.size() == 0)
			return null;
		return eStacks.get(0);
	}

	@PreDestroy
	void unhookListeners() {
		eventBroker.unsubscribe(ctfListener);
		eventBroker.unsubscribe(perspectiveRemovedListener);
		eventBroker.unsubscribe(perspectiveChangeListener);
		eventBroker.unsubscribe(tagChangeListener);
		eventBroker.unsubscribe(initListener);
	}

	void minimizeStack(MPartStack stack) {
		MTrimmedWindow window = (MTrimmedWindow) modelService.getTopLevelWindowFor(stack);
		Shell winShell = (Shell) window.getWidget();

		// Is there already a TrimControl there ?
		String trimId = stack.getElementId() + getMinimizedStackSuffix(stack);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);

		if (trimStack == null) {
			trimStack = MenuFactoryImpl.eINSTANCE.createToolControl();
			trimStack.setElementId(trimId);
			trimStack.setContributionURI(trimURI);

			Rectangle winBounds = winShell.getBounds();
			int winCenterX = winBounds.width / 2;
			Control stackCtrl = (Control) stack.getWidget();
			Rectangle stackBounds = stackCtrl.getBounds();
			stackBounds = winShell.getDisplay().map(stackCtrl, winShell, stackBounds);
			int stackCenterX = stackBounds.x + (stackBounds.width / 2);
			SideValue side = stackCenterX < winCenterX ? SideValue.LEFT : SideValue.RIGHT;
			MTrimBar bar = modelService.getTrim(window, side);

			MToolControl spacer = (MToolControl) modelService.find("PerspectiveSpacer", bar);
			if (spacer != null) {
				int spacerIndex = bar.getChildren().indexOf(spacer);
				bar.getChildren().add(spacerIndex - 1, trimStack);
				Control ctrl = (Control) trimStack.getWidget();
				if (ctrl != null) {
					Control spacerCtrl = (Control) spacer.getWidget();
					ctrl.moveAbove(spacerCtrl);
				}
			} else {
				bar.getChildren().add(trimStack);
				if (!bar.isToBeRendered())
					bar.setToBeRendered(true);
			}
		} else {
			// get the parent trim bar, see bug 320756
			MUIElement parent = trimStack.getParent();
			if (parent.getWidget() == null) {
				// ask it to be rendered
				parent.setToBeRendered(true);
				// create the widget
				context.get(IPresentationEngine.class).createGui(parent, winShell,
						window.getContext());
			}
			trimStack.setToBeRendered(true);
		}

		// Button Hack to show a 'restore' button while avoiding the 'minimized' layout
		CTabFolder ctf = (CTabFolder) stack.getWidget();
		if (ctf != null) {
			ctf.setMinimizeVisible(false);
			ctf.setMaximizeVisible(true);
			ctf.setMaximized(true);
		}

		stack.setVisible(false);
	}

	void restoreStack(MPartStack stack) {
		MWindow window = modelService.getTopLevelWindowFor(stack);
		String trimId = stack.getElementId() + getMinimizedStackSuffix(stack);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);
		TrimStack ts = (TrimStack) trimStack.getObject();
		ts.restoreStack();

		stack.getTags().remove(MINIMIZED_BY_ZOOM);

		// Button Hack to show a 'restore' button while avoiding the 'minimized' layout
		CTabFolder ctf = (CTabFolder) stack.getWidget();
		if (ctf != null) {
			ctf.setMinimizeVisible(true);
			ctf.setMaximizeVisible(false);
			ctf.setMaximized(false);
		}
	}

	void maximizeEA(MPartStack stack) {
		MWindow win = modelService.getTopLevelWindowFor(stack);
		MPerspective persp = modelService.getActivePerspective(win);
		MUIElement toSearch = persp != null ? persp : win;
		List<MPartStack> stacks = modelService.findElements(toSearch, null, MPartStack.class, null);
		for (MPartStack theStack : stacks) {
			if (!theStack.getTags().contains("EditorStack") && theStack.getWidget() != null
					&& !theStack.getTags().contains(MINIMIZED)) {
				theStack.getTags().add(MINIMIZED_BY_ZOOM);
				theStack.getTags().add(MINIMIZED);
			}
		}

		// Remember that the EA is max'd in this perspective
		persp.getTags().add(EA_MAXIMIZED);

		CTabFolder ctf = (CTabFolder) stack.getWidget();
		ctf.setMaximized(true);
	}

	void unmaximizeEA(MPartStack stack) {
		MWindow win = modelService.getTopLevelWindowFor(stack);
		MPerspective persp = modelService.getActivePerspective(win);
		MUIElement toSearch = persp != null ? persp : win;
		List<MPartStack> stacks = modelService.findElements(toSearch, null, MPartStack.class, null);
		for (MPartStack theStack : stacks) {
			if (!theStack.getTags().contains("EditorStack") && theStack.getWidget() != null
					&& theStack.getTags().contains(MINIMIZED)
					&& theStack.getTags().contains(MINIMIZED_BY_ZOOM)) {
				theStack.getTags().remove(MINIMIZED_BY_ZOOM);
				theStack.getTags().remove(MINIMIZED);
			}
		}

		// Forget that the EA is max'd in this perspective for this window
		persp.getTags().remove(EA_MAXIMIZED);

		CTabFolder ctf = (CTabFolder) stack.getWidget();
		ctf.setMaximized(false);
	}

	/**
	 * @param stack
	 * @return
	 */
	private String getMinimizedStackSuffix(MPartStack stack) {
		String id = ID_SUFFIX;
		MPerspective persp = modelService.getPerspectiveFor(stack);
		if (persp != null) {
			id = '(' + persp.getElementId() + ')';
		}
		return id;
	}
}
