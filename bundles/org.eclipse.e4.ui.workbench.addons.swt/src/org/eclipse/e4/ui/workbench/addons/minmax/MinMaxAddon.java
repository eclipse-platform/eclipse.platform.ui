/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
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
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.internal.FaderAnimationFeedback;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class MinMaxAddon {

	/**
	 * The identifier for the shared area in the Eclipse Platform. This value should be identical to
	 * the value defined in org.eclipse.ui.IPageLayout.ID_EDITOR_AREA.
	 */
	private static final String ID_EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$

	static String ID_SUFFIX = "(minimized)"; //$NON-NLS-1$

	// tags representing the min/max state
	public static String MINIMIZED = "Minimized"; //$NON-NLS-1$
	public static String MAXIMIZED = "Maximized"; //$NON-NLS-1$
	public static String MINIMIZED_BY_ZOOM = "MinimizedByZoom"; //$NON-NLS-1$

	@Inject
	IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	@Inject
	private IEclipseContext context;

	@Inject
	private EPartService partService;

	// Allow 'local' changes to the tags
	private boolean ignoreTagChanges = false;

	private CTabFolder2Adapter CTFButtonListener = new CTabFolder2Adapter() {
		private MUIElement getElementToChange(CTabFolderEvent event) {
			CTabFolder ctf = (CTabFolder) event.widget;
			MUIElement element = (MUIElement) ctf.getData(AbstractPartRenderer.OWNING_ME);
			if (element instanceof MArea)
				return element.getCurSharedRef();

			MUIElement parentElement = element.getParent();
			while (parentElement != null && !(parentElement instanceof MArea))
				parentElement = parentElement.getParent();

			return parentElement != null ? parentElement.getCurSharedRef() : element;
		}

		public void maximize(CTabFolderEvent event) {
			setState(getElementToChange(event), MAXIMIZED);
		}

		public void minimize(CTabFolderEvent event) {
			setState(getElementToChange(event), MINIMIZED);
		}

		public void restore(CTabFolderEvent event) {
			setState(getElementToChange(event), null);
		}
	};

	private MouseListener CTFDblClickListener = new MouseListener() {
		public void mouseUp(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
		}

		private MUIElement getElementToChange(MouseEvent event) {
			CTabFolder ctf = (CTabFolder) event.widget;
			MUIElement element = (MUIElement) ctf.getData(AbstractPartRenderer.OWNING_ME);
			if (element instanceof MArea) {
				// set the state on the placeholder
				return element.getCurSharedRef();
			}

			MUIElement parentElement = element.getParent();
			while (parentElement != null && !(parentElement instanceof MArea))
				parentElement = parentElement.getParent();

			return parentElement != null ? parentElement.getCurSharedRef() : element;
		}

		public void mouseDoubleClick(MouseEvent e) {
			// only maximize if the primary mouse button was used
			if (e.button == 1) {
				CTabFolder ctf = (CTabFolder) e.widget;
				if (!ctf.getMaximizeVisible())
					return;

				MUIElement elementToChange = getElementToChange(e);
				if (!elementToChange.getTags().contains(MAXIMIZED)) {
					setState(elementToChange, MAXIMIZED);
				} else {
					setState(elementToChange, null);
				}
			}
		}
	};

	private EventHandler widgetListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MPartStack) && !(changedElement instanceof MArea))
				return;

			final CTabFolder ctf = getCTFFor(changedElement);
			if (ctf == null)
				return;

			MUIElement stateElement = changedElement;
			if (changedElement instanceof MPartStack) {
				MPartStack stack = (MPartStack) changedElement;
				MArea area = getAreaFor(stack);
				if (area != null && !(area.getWidget() instanceof CTabFolder))
					stateElement = area.getCurSharedRef();
			} else if (changedElement instanceof MArea)
				stateElement = changedElement.getCurSharedRef();

			adjustCTFButtons(stateElement);

			ctf.removeCTabFolder2Listener(CTFButtonListener); // Prevent multiple instances
			ctf.addCTabFolder2Listener(CTFButtonListener);

			ctf.removeMouseListener(CTFDblClickListener); // Prevent multiple instances
			ctf.addMouseListener(CTFDblClickListener);
		}
	};

	private void setState(MUIElement element, String state) {
		element.getTags().remove(MINIMIZED_BY_ZOOM);
		if (MINIMIZED.equals(state)) {
			element.getTags().remove(MAXIMIZED);
			element.getTags().add(MINIMIZED);
		} else if (MAXIMIZED.equals(state)) {
			element.getTags().remove(MINIMIZED);
			element.getTags().add(MAXIMIZED);
		} else {
			element.getTags().remove(MINIMIZED);
			element.getTags().remove(MAXIMIZED);
		}

	}

	private EventHandler perspectiveChangeListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MPerspectiveStack))
				return;

			MPerspectiveStack ps = (MPerspectiveStack) changedElement;
			MWindow window = modelService.getTopLevelWindowFor(ps);
			List<MToolControl> tcList = modelService.findElements(window, null, MToolControl.class,
					null);

			final MPerspective curPersp = ps.getSelectedElement();
			if (curPersp != null) {
				// Show any minimized stack from the current perspective
				String perspId = '(' + curPersp.getElementId() + ')';
				for (MToolControl tc : tcList) {
					if (tc.getObject() instanceof TrimStack && tc.getElementId().contains(perspId)) {
						tc.setVisible(true);
					}
				}

				// Find the editor 'area'
				MPlaceholder eaPlaceholder = (MPlaceholder) modelService.find(ID_EDITOR_AREA,
						curPersp);
				adjustCTFButtons(eaPlaceholder);
			}

			// Hide any minimized stacks from the old perspective
			if (event.getProperty(EventTags.OLD_VALUE) instanceof MPerspective) {
				MPerspective oldPersp = (MPerspective) event.getProperty(EventTags.OLD_VALUE);
				String perspId = '(' + oldPersp.getElementId() + ')';
				for (MToolControl tc : tcList) {
					if (tc.getObject() instanceof TrimStack && tc.getElementId().contains(perspId)) {
						TrimStack ts = (TrimStack) tc.getObject();
						ts.showStack(false);
						tc.setVisible(false);
					}
				}
			}

			final Shell winShell = (Shell) window.getWidget();
			winShell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					winShell.layout(true, true);
				}
			});
		}
	};

	/**
	 * If a perspective ID changes fix any TrimStacks that reference the old id to point at the new
	 * id.
	 * 
	 * This keeps trim stacks attached to the correct perspective when a perspective is saved with a
	 * new name.
	 */
	private EventHandler idChangeListener = new EventHandler() {
		public void handleEvent(Event event) {
			Object changedObject = event.getProperty(EventTags.ELEMENT);

			// Only care about MPerspective id changes
			if (!(changedObject instanceof MPerspective))
				return;

			MPerspective perspective = (MPerspective) changedObject;

			String newID = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			String oldID = (String) event.getProperty(UIEvents.EventTags.OLD_VALUE);

			// pattern is trimStackID(perspectiveID)
			newID = '(' + newID + ')';
			oldID = '(' + oldID + ')';

			// Search the trim for the window containing the perspective
			MWindow perspWin = modelService.getTopLevelWindowFor(perspective);
			if (perspWin == null)
				return;

			List<MToolControl> trimStacks = modelService.findElements(perspWin, null,
					MToolControl.class, null);
			for (MToolControl trimStack : trimStacks) {
				// Only care about MToolControls that are TrimStacks
				if (TrimStack.CONTRIBUTION_URI.equals(trimStack.getContributionURI()))
					trimStack.setElementId(trimStack.getElementId().replace(oldID, newID));
			}
		}
	};

	private EventHandler tagChangeListener = new EventHandler() {
		public void handleEvent(Event event) {
			if (ignoreTagChanges)
				return;

			Object changedObj = event.getProperty(EventTags.ELEMENT);
			String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);
			String tag = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			String oldVal = (String) event.getProperty(UIEvents.EventTags.OLD_VALUE);

			if (!(changedObj instanceof MUIElement))
				return;

			final MUIElement changedElement = (MUIElement) changedObj;

			if (UIEvents.EventTypes.ADD.equals(eventType)) {
				if (MINIMIZED.equals(tag)) {
					minimize(changedElement);
				} else if (MAXIMIZED.equals(tag)) {
					maximize(changedElement);
				}
			} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				if (MINIMIZED.equals(oldVal)) {
					restore(changedElement);
				} else if (MAXIMIZED.equals(oldVal)) {
					unzoom(changedElement);
				}
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
		eventBroker.subscribe(topic, widgetListener);
		topic = UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN);
		eventBroker.subscribe(topic, perspectiveRemovedListener);
		topic = UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT);
		eventBroker.subscribe(topic, perspectiveChangeListener);
		topic = UIEvents.buildTopic(UIEvents.ApplicationElement.TOPIC,
				UIEvents.ApplicationElement.TAGS);
		eventBroker.subscribe(topic, tagChangeListener);
		topic = UIEvents.buildTopic(UIEvents.ApplicationElement.TOPIC,
				UIEvents.ApplicationElement.ELEMENTID);
		eventBroker.subscribe(topic, idChangeListener);

	}

	@PreDestroy
	void unhookListeners() {
		eventBroker.unsubscribe(widgetListener);
		eventBroker.unsubscribe(perspectiveRemovedListener);
		eventBroker.unsubscribe(perspectiveChangeListener);
		eventBroker.unsubscribe(tagChangeListener);
		eventBroker.unsubscribe(idChangeListener);
	}

	private MArea getAreaFor(MPartStack stack) {
		MUIElement parent = stack.getParent();
		while (parent != null) {
			if (parent instanceof MArea)
				return (MArea) parent;
			parent = parent.getParent();
		}
		return null;
	}

	private void setCTFButtons(CTabFolder ctf, MUIElement stateElement, boolean hideButtons) {
		if (hideButtons) {
			ctf.setMinimizeVisible(false);
			ctf.setMaximizeVisible(false);
		} else {
			if (stateElement.getTags().contains(MINIMIZED)) {
				ctf.setMinimizeVisible(false);
				ctf.setMaximizeVisible(true);
				ctf.setMaximized(true);
			} else if (stateElement.getTags().contains(MAXIMIZED)) {
				ctf.setMinimizeVisible(true);
				ctf.setMaximizeVisible(true);
				ctf.setMaximized(true);
			} else {
				ctf.setMinimizeVisible(true);
				ctf.setMaximizeVisible(true);
				ctf.setMinimized(false);
				ctf.setMaximized(false);
				ctf.layout();
			}
		}
	}

	/**
	 * Set the state of the min / max buttons on the CTF based on the model element's state. The
	 * input is expected to be the element that contains the min/max state info which should either
	 * be an MPartStack or an MPlaceholder for the shared area.
	 * 
	 * @param element
	 *            The element to test
	 */
	private void adjustCTFButtons(MUIElement element) {
		if (!(element instanceof MPartStack) && !(element instanceof MPlaceholder))
			return;

		CTabFolder ctf = getCTFFor(element);
		if (ctf == null)
			return;

		if (element instanceof MPlaceholder) {
			setCTFButtons(ctf, element, false);
		} else {
			MArea area = getAreaFor((MPartStack) element);
			if (area == null) {
				setCTFButtons(ctf, element, false);
			}
		}
	}

	private CTabFolder getCTFFor(MUIElement element) {
		if (element instanceof MArea) {
			if (element.getWidget() instanceof CTabFolder)
				return (CTabFolder) element.getWidget();
			List<MPartStack> stacks = modelService.findElements(element, null, MPartStack.class,
					null);
			for (MPartStack stack : stacks) {
				if (stack.getWidget() instanceof CTabFolder)
					return (CTabFolder) stack.getWidget();
			}
		} else if (element.getWidget() instanceof CTabFolder)
			return (CTabFolder) element.getWidget();
		else if (element instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) element;
			if (ph.getRef() instanceof MArea) {
				return getCTFFor(ph.getRef());
			}
		}
		return null;
	}

	void minimize(MUIElement element) {
		createTrim(element);
		element.setVisible(false);
		adjustCTFButtons(element);
		// Activate a part other than the trimStack so that if the tool item is pressed
		// immediately it will still open the stack.
		partService.requestActivation();
	}

	void restore(MUIElement element) {
		MWindow window = modelService.getTopLevelWindowFor(element);
		String trimId = element.getElementId() + getMinimizedElementSuffix(element);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);
		TrimStack ts = (TrimStack) trimStack.getObject();
		ts.restoreStack();

		adjustCTFButtons(element);
		element.getTags().remove(MINIMIZED_BY_ZOOM);
	}

	void maximize(final MUIElement element) {
		MWindow win = getWindowFor(element);
		MPerspective persp = modelService.getActivePerspective(win);

		Shell hostShell = (Shell) modelService.getTopLevelWindowFor(element).getWidget();
		FaderAnimationFeedback fader = new FaderAnimationFeedback(hostShell);
		AnimationEngine engine = new AnimationEngine(fader, 300);
		engine.schedule();

		List<String> maxTag = new ArrayList<String>();
		maxTag.add(MAXIMIZED);
		List<MUIElement> curMax = modelService.findElements(persp == null ? win : persp, null,
				MUIElement.class, maxTag);
		if (curMax.size() > 0) {
			for (MUIElement maxElement : curMax) {
				if (maxElement == element)
					continue;
				ignoreTagChanges = true;
				try {
					maxElement.getTags().remove(MAXIMIZED);
				} finally {
					ignoreTagChanges = false;
				}
			}
		}

		List<MPartStack> stacks = modelService.findElements(persp == null ? win : persp, null,
				MPartStack.class, null, EModelService.PRESENTATION);
		for (MPartStack theStack : stacks) {
			if (theStack == element)
				continue;

			// Exclude stacks in DW's
			if (getWindowFor(theStack) != win)
				continue;

			int loc = modelService.getElementLocation(theStack);
			if (loc != EModelService.IN_SHARED_AREA && theStack.getWidget() != null
					&& !theStack.getTags().contains(MINIMIZED)) {
				theStack.getTags().add(MINIMIZED_BY_ZOOM);
				theStack.getTags().add(MINIMIZED);
			}
		}

		// Find the editor 'area'
		if (persp != null) {
			MPlaceholder eaPlaceholder = (MPlaceholder) modelService.find(ID_EDITOR_AREA, persp);
			if (element != eaPlaceholder && eaPlaceholder != null) {
				eaPlaceholder.getTags().add(MINIMIZED_BY_ZOOM);
				eaPlaceholder.getTags().add(MINIMIZED);
			}
		}

		adjustCTFButtons(element);
	}

	/**
	 * Return the MWindow containing this element (if any). This may either be a 'top level' window
	 * -or- a detached window. This allows the min.max code to only affect elements in the window
	 * containing the element.
	 * 
	 * @param element
	 *            The element to check
	 * 
	 * @return the window containing the element.
	 */
	private MWindow getWindowFor(MUIElement element) {
		MUIElement parent = element.getParent();

		// We rely here on the fact that a DW's 'getParent' will return
		// null since it's not in the 'children' hierarchy
		while (parent != null && !(parent instanceof MWindow))
			parent = parent.getParent();

		// A detached window will end up with getParent() == null
		return (MWindow) parent;
	}

	void unzoom(final MUIElement element) {
		MWindow win = modelService.getTopLevelWindowFor(element);
		MPerspective persp = modelService.getActivePerspective(win);

		Shell hostShell = (Shell) win.getWidget();
		FaderAnimationFeedback fader = new FaderAnimationFeedback(hostShell);
		AnimationEngine engine = new AnimationEngine(fader, 300);
		engine.schedule();

		List<MPartStack> stacks = modelService.findElements(win, null, MPartStack.class, null,
				EModelService.PRESENTATION);
		for (MPartStack theStack : stacks) {
			if (theStack.getWidget() != null && theStack.getTags().contains(MINIMIZED)
					&& theStack.getTags().contains(MINIMIZED_BY_ZOOM)) {
				theStack.getTags().remove(MINIMIZED);
			}
		}

		// Find the editor 'area'
		MPlaceholder eaPlaceholder = (MPlaceholder) modelService.find(ID_EDITOR_AREA,
				persp == null ? win : persp);
		if (element != eaPlaceholder && eaPlaceholder != null) {
			eaPlaceholder.getTags().remove(MINIMIZED);
		}

		adjustCTFButtons(element);
	}

	private void createTrim(MUIElement element) {
		MTrimmedWindow window = (MTrimmedWindow) getWindowFor(element);
		Shell winShell = (Shell) window.getWidget();

		// Is there already a TrimControl there ?
		String trimId = element.getElementId() + getMinimizedElementSuffix(element);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);

		if (trimStack == null) {
			trimStack = MenuFactoryImpl.eINSTANCE.createToolControl();
			trimStack.setElementId(trimId);
			trimStack.setContributionURI(TrimStack.CONTRIBUTION_URI);

			Rectangle winBounds = winShell.getBounds();
			int winCenterX = winBounds.width / 2;
			Control stackCtrl = (Control) element.getWidget();
			Rectangle stackBounds = stackCtrl.getBounds();
			stackBounds = winShell.getDisplay().map(stackCtrl, winShell, stackBounds);
			int stackCenterX = stackBounds.x + (stackBounds.width / 2);
			SideValue side = stackCenterX < winCenterX ? SideValue.LEFT : SideValue.RIGHT;
			MTrimBar bar = modelService.getTrim(window, side);

			bar.getChildren().add(trimStack);
			bar.setVisible(true);

			// get the parent trim bar, see bug 320756
			if (bar.getWidget() == null) {
				// ask it to be rendered
				bar.setToBeRendered(true);

				// create the widget
				context.get(IPresentationEngine.class)
						.createGui(bar, winShell, window.getContext());
			}
		} else {
			// get the parent trim bar, see bug 320756
			MUIElement parent = trimStack.getParent();
			parent.setVisible(true);
			if (parent.getWidget() == null) {
				// ask it to be rendered
				parent.setToBeRendered(true);
				// create the widget
				context.get(IPresentationEngine.class).createGui(parent, winShell,
						window.getContext());
			}
			trimStack.setToBeRendered(true);
		}
	}

	private String getMinimizedElementSuffix(MUIElement element) {
		String id = ID_SUFFIX;
		MPerspective persp = modelService.getPerspectiveFor(element);
		if (persp != null) {
			id = '(' + persp.getElementId() + ')';
		}
		return id;
	}
}
