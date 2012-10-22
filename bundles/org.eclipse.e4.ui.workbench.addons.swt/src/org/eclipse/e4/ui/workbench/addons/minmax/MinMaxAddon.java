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
import org.eclipse.e4.ui.internal.workbench.swt.AnimationEngine;
import org.eclipse.e4.ui.internal.workbench.swt.FaderAnimationFeedback;
import org.eclipse.e4.ui.model.application.MAddon;
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
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class MinMaxAddon {

	/**
	 * The identifier for the shared area in the Eclipse Platform. This value should be identical to
	 * the value defined in org.eclipse.ui.IPageLayout.ID_EDITOR_AREA.
	 */
	private static final String ID_EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$

	private static final String GLOBAL_CACHE_ID = "Global";

	static String ID_SUFFIX = "(minimized)"; //$NON-NLS-1$

	// tags representing the min/max state (h
	private static String MINIMIZED = IPresentationEngine.MINIMIZED;
	private static String MAXIMIZED = IPresentationEngine.MAXIMIZED;
	private static String MINIMIZED_BY_ZOOM = IPresentationEngine.MINIMIZED_BY_ZOOM;

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

	@Inject
	MAddon minMaxAddon;

	private EventHandler perspSavedListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MPerspective savedPersp = (MPerspective) event.getProperty(EventTags.ELEMENT);
			String cache = getTrimCache(savedPersp);
			minMaxAddon.getPersistedState().put(savedPersp.getElementId(), cache);
		}

		private String getTrimCache(MPerspective savedPersp) {
			MWindow topWin = modelService.getTopLevelWindowFor(savedPersp);
			String perspIdStr = '(' + savedPersp.getElementId() + ')';

			String cache = getWinCache(topWin, perspIdStr);
			for (MWindow dw : savedPersp.getWindows()) {
				cache += getWinCache(dw, perspIdStr);
			}

			return cache;
		}

		private String getWinCache(MWindow win, String perspIdStr) {
			String winStr = ""; //$NON-NLS-1$

			List<MPartStack> stackList = modelService.findElements(win, null, MPartStack.class,
					null);
			for (MPartStack stack : stackList) {
				winStr += getStackTrimLoc(stack, perspIdStr);
			}
			return winStr;
		}

		private String getStackTrimLoc(MPartStack stack, String perspIdStr) {
			MWindow stackWin = modelService.getTopLevelWindowFor(stack);// getContainingWindow(stack);
			MUIElement tcElement = modelService.find(stack.getElementId() + perspIdStr, stackWin);
			if (tcElement == null)
				return ""; //$NON-NLS-1$

			MTrimBar bar = (MTrimBar) ((MUIElement) tcElement.getParent());
			int sideVal = bar.getSide().getValue();
			int index = bar.getChildren().indexOf(tcElement);
			return stack.getElementId() + ' ' + sideVal + ' ' + index + "#"; //$NON-NLS-1$
		}
	};

	private EventHandler perspOpenedListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MPerspective openedPersp = (MPerspective) event.getProperty(EventTags.ELEMENT);

			// Find any minimized stacks and show their trim
			MWindow topWin = modelService.getTopLevelWindowFor(openedPersp);
			showMinimizedTrim(topWin);
			for (MWindow dw : openedPersp.getWindows()) {
				showMinimizedTrim(dw);
			}
		}

		private void showMinimizedTrim(MWindow win) {
			List<MPartStack> stackList = modelService.findElements(win, null, MPartStack.class,
					null);
			for (MPartStack stack : stackList) {
				if (stack.getTags().contains(IPresentationEngine.MINIMIZED)) {
					createTrim(stack);
				}
			}
		}
	};

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
			// HACK! If this is an empty stack treat it as though it was the editor area
			// and tear down any open trim stacks (see bug 384814)
			CTabFolder ctf = (CTabFolder) e.widget;
			MUIElement element = (MUIElement) ctf.getData(AbstractPartRenderer.OWNING_ME);
			if (element instanceof MPartStack && ctf.getItemCount() == 0) {
				MWindow window = modelService.getTopLevelWindowFor(element);
				if (window != null) {
					List<MToolControl> tcList = modelService.findElements(window, null,
							MToolControl.class, null);
					for (MToolControl tc : tcList) {
						if (tc.getObject() instanceof TrimStack) {
							TrimStack ts = (TrimStack) tc.getObject();
							ts.showStack(false);
						}
					}
				}
			}
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

				// Only fire if we're in the 'tab' area
				if (e.y > ctf.getTabHeight())
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
					if (!winShell.isDisposed()) {
						winShell.layout(true, true);
					}
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

			if (!(changedObj instanceof MUIElement))
				return;

			final MUIElement changedElement = (MUIElement) changedObj;

			if (UIEvents.isADD(event)) {
				if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE, MINIMIZED)) {
					minimize(changedElement);
				} else if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE, MAXIMIZED)) {
					maximize(changedElement);
				}
			} else if (UIEvents.isREMOVE(event)) {
				if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE, MINIMIZED)) {
					restore(changedElement);
				} else if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE, MAXIMIZED)) {
					unzoom(changedElement);
				}
			}
		}
	};

	private EventHandler perspectiveRemovedListener = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MPerspectiveStack)
					|| modelService.getTopLevelWindowFor(changedElement) == null)
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
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, widgetListener);
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, perspectiveRemovedListener);
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT,
				perspectiveChangeListener);
		eventBroker.subscribe(UIEvents.ApplicationElement.TOPIC_TAGS, tagChangeListener);
		eventBroker.subscribe(UIEvents.ApplicationElement.TOPIC_ELEMENTID, idChangeListener);

		eventBroker.subscribe(UIEvents.UILifeCycle.PERSPECTIVE_SAVED, perspSavedListener);
		eventBroker.subscribe(UIEvents.UILifeCycle.PERSPECTIVE_OPENED, perspOpenedListener);
	}

	@PreDestroy
	void unhookListeners() {
		eventBroker.unsubscribe(widgetListener);
		eventBroker.unsubscribe(perspectiveRemovedListener);
		eventBroker.unsubscribe(perspectiveChangeListener);
		eventBroker.unsubscribe(tagChangeListener);
		eventBroker.unsubscribe(idChangeListener);
		eventBroker.unsubscribe(perspSavedListener);
		eventBroker.unsubscribe(perspOpenedListener);
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

	boolean isEmptyPerspectiveStack(MUIElement element) {
		if (!(element instanceof MPerspectiveStack))
			return false;
		MPerspectiveStack ps = (MPerspectiveStack) element;
		return ps.getChildren().size() == 0;
	}

	void minimize(MUIElement element) {
		// Can't minimize a non-rendered element
		if (!element.isToBeRendered())
			return;

		if (isEmptyPerspectiveStack(element)) {
			element.setVisible(false);
			return;
		}

		createTrim(element);
		element.setVisible(false);
		adjustCTFButtons(element);
		// Activate a part other than the trimStack so that if the tool item is pressed
		// immediately it will still open the stack.
		partService.requestActivation();
	}

	void restore(MUIElement element) {
		if (isEmptyPerspectiveStack(element)) {
			element.setVisible(true);
			element.getTags().remove(MINIMIZED_BY_ZOOM);
			return;
		}

		MWindow window = modelService.getTopLevelWindowFor(element);
		String trimId = element.getElementId() + getMinimizedElementSuffix(element);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);
		if (trimStack == null)
			return;

		TrimStack ts = (TrimStack) trimStack.getObject();
		ts.restoreStack();

		adjustCTFButtons(element);
		element.getTags().remove(MINIMIZED_BY_ZOOM);
	}

	void maximize(final MUIElement element) {
		if (!element.isToBeRendered())
			return;

		MWindow win = getWindowFor(element);
		MPerspective persp = modelService.getActivePerspective(win);

		List<MUIElement> elementsToMinimize = new ArrayList<MUIElement>();
		int loc = modelService.getElementLocation(element);
		if ((loc & EModelService.OUTSIDE_PERSPECTIVE) != 0) {
			// Minimize all other global stacks
			List<MPartStack> globalStacks = modelService.findElements(win, null, MPartStack.class,
					null, EModelService.OUTSIDE_PERSPECTIVE);
			for (MPartStack gStack : globalStacks) {
				if (gStack == element || !gStack.isToBeRendered())
					continue;

				if (gStack.getWidget() != null && !gStack.getTags().contains(MINIMIZED)) {
					elementsToMinimize.add(gStack);
				}
			}

			// Minimize the Perspective Stack
			MUIElement perspStack = null;
			if (persp == null) {
				// special case for windows with no perspectives (eg bug 372614:
				// intro part with no perspectives). We know we're outside
				// of the perspective stack, so find it top-down
				List<MPerspectiveStack> pStacks = modelService.findElements(win, null,
						MPerspectiveStack.class, null);
				perspStack = (pStacks.size() > 0) ? pStacks.get(0) : null;
			} else {
				perspStack = persp.getParent();
			}
			if (perspStack != null) {
				if (perspStack.getElementId() == null || perspStack.getElementId().length() == 0)
					perspStack.setElementId("PerspectiveStack"); //$NON-NLS-1$

				elementsToMinimize.add(perspStack);
			}
		} else {
			List<MPartStack> stacks = modelService.findElements(persp == null ? win : persp, null,
					MPartStack.class, null, EModelService.PRESENTATION);
			for (MPartStack theStack : stacks) {
				if (theStack == element || !theStack.isToBeRendered())
					continue;

				// Exclude stacks in DW's
				if (getWindowFor(theStack) != win)
					continue;

				loc = modelService.getElementLocation(theStack);
				if (loc != EModelService.IN_SHARED_AREA && theStack.getWidget() != null
						&& !theStack.getTags().contains(MINIMIZED)) {
					elementsToMinimize.add(theStack);
				}
			}

			// Find the editor 'area'
			if (persp != null) {
				MPlaceholder eaPlaceholder = (MPlaceholder) modelService
						.find(ID_EDITOR_AREA, persp);
				if (element != eaPlaceholder && eaPlaceholder != null
						&& eaPlaceholder.isToBeRendered()) {
					elementsToMinimize.add(eaPlaceholder);
				}
			}
		}

		Shell hostShell = (Shell) modelService.getTopLevelWindowFor(element).getWidget();
		FaderAnimationFeedback fader = new FaderAnimationFeedback(hostShell);
		AnimationEngine engine = new AnimationEngine(win.getContext(), fader, 300);
		engine.schedule();

		// Restore any currently maximized element
		restoreMaximizedElement(element, win);

		for (MUIElement toMinimize : elementsToMinimize) {
			toMinimize.getTags().add(MINIMIZED);
			toMinimize.getTags().add(MINIMIZED_BY_ZOOM);
		}

		adjustCTFButtons(element);
	}

	/**
	 * Restore any currently maximized element (except the one we're in the process of maximizing
	 * 
	 * @param element
	 * @param win
	 */
	private void restoreMaximizedElement(final MUIElement element, MWindow win) {
		MPerspective elePersp = modelService.getPerspectiveFor(element);
		List<String> maxTag = new ArrayList<String>();
		maxTag.add(MAXIMIZED);
		List<MUIElement> curMax = modelService.findElements(win, null, MUIElement.class, maxTag);
		if (curMax.size() > 0) {
			for (MUIElement maxElement : curMax) {
				MPerspective maxPersp = modelService.getPerspectiveFor(maxElement);
				if (maxPersp != elePersp)
					continue;
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
		AnimationEngine engine = new AnimationEngine(win.getContext(), fader, 300);
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
		if (element != eaPlaceholder && eaPlaceholder != null
				&& eaPlaceholder.getTags().contains(MINIMIZED_BY_ZOOM)) {
			eaPlaceholder.getTags().remove(MINIMIZED);
		}

		// Find the Perspective Stack
		int loc = modelService.getElementLocation(element);
		if ((loc & EModelService.OUTSIDE_PERSPECTIVE) != 0) {
			List<MPerspectiveStack> psList = modelService.findElements(win, null,
					MPerspectiveStack.class, null);
			if (psList.size() == 1) {
				MPerspectiveStack perspStack = psList.get(0);
				if (element != perspStack && perspStack != null
						&& perspStack.getTags().contains(MINIMIZED_BY_ZOOM)) {
					perspStack.getTags().remove(MINIMIZED);
				}
			}
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
			trimStack.getTags().add("TrimStack"); //$NON-NLS-1$

			// Check if we have a cached location
			MTrimBar bar = getBarForElement(element, window);
			int index = getCachedIndex(element);
			if (index == -1 || index >= bar.getChildren().size())
				bar.getChildren().add(trimStack);
			else
				bar.getChildren().add(index, trimStack);

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

	private String getCachedInfo(MUIElement element) {
		String cacheId = GLOBAL_CACHE_ID;
		MPerspective persp = modelService.getPerspectiveFor(element);
		if (persp != null)
			cacheId = persp.getElementId();
		String cacheInfo = minMaxAddon.getPersistedState().get(cacheId);

		return cacheInfo;
	}

	private int getCachedIndex(MUIElement element) {
		String cache = getCachedInfo(element);
		if (cache == null)
			return -1;

		String[] stacks = cache.split("#"); //$NON-NLS-1$
		for (String stackInfo : stacks) {
			String[] vals = stackInfo.split(" "); //$NON-NLS-1$
			if (vals[0].equals(element.getElementId())) {
				return Integer.parseInt(vals[2]);
			}
		}
		return -1;
	}

	private SideValue getCachedBar(MUIElement element) {
		String cache = getCachedInfo(element);
		if (cache == null)
			return null;

		String[] stacks = cache.split("#"); //$NON-NLS-1$
		for (String stackInfo : stacks) {
			String[] vals = stackInfo.split(" "); //$NON-NLS-1$
			if (vals[0].equals(element.getElementId())) {
				int sideVal = Integer.parseInt(vals[1]);
				return SideValue.get(sideVal);
			}
		}
		return null;
	}

	private MTrimBar getBarForElement(MUIElement element, MTrimmedWindow window) {
		SideValue side = getCachedBar(element);
		if (side == null) {
			Shell winShell = (Shell) window.getWidget();
			Rectangle winBounds = winShell.getBounds();
			int winCenterX = winBounds.width / 2;
			Control stackCtrl = (Control) element.getWidget();
			Rectangle stackBounds = stackCtrl.getBounds();
			stackBounds = winShell.getDisplay().map(stackCtrl, winShell, stackBounds);
			int stackCenterX = stackBounds.x + (stackBounds.width / 2);
			side = stackCenterX < winCenterX ? SideValue.LEFT : SideValue.RIGHT;
		}
		MTrimBar bar = modelService.getTrim(window, side);

		return bar;
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
