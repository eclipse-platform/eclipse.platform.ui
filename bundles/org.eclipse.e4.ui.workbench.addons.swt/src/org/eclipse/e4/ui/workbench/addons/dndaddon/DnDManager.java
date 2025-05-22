/*******************************************************************************
 * Copyright (c) 2010, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.widgets.ImageBasedFrame;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;
import org.osgi.service.event.EventHandler;

class DnDManager {
	private static final Rectangle offScreenRect = new Rectangle(10000, -10000, 1, 1);

	public static final int HOSTED = 1;
	public static final int GHOSTED = 2;
	public static final int SIMPLE = 3;
	private int feedbackStyle = SIMPLE;

	Collection<DragAgent> dragAgents = new ArrayList<>();
	Collection<DropAgent> dropAgents = new ArrayList<>();

	DnDInfo info;
	DragAgent dragAgent;

	private MWindow dragWindow;

	private Shell dragHost;
	private Control dragCtrl;

	private Tracker tracker;
	boolean dragging;

	private Shell overlayFrame;
	private List<Rectangle> frames = new ArrayList<>();

	DragDetectListener dragDetector = e -> {
		if (dragging || e.widget.isDisposed()) {
			return;
		}

		info.update(e);
		dragAgent = getDragAgent(info);
		if (dragAgent != null) {
			try {
				dragging = true;
				isModified = (e.stateMask & SWT.MOD1) != 0;
				startDrag();
			} finally {
				dragging = false;
			}
		}
	};

	public void addFrame(Rectangle newRect) {
		frames.add(newRect);
		updateOverlay();
	}

	private List<Image> images = new ArrayList<>();
	private List<Rectangle> imageRects = new ArrayList<>();

	protected boolean isModified;

	public void addImage(Rectangle imageRect, Image image) {
		imageRects.add(imageRect);
		images.add(image);
		updateOverlay();
	}

	public DnDManager(MWindow topLevelWindow) {
		dragWindow = topLevelWindow;
		info = new DnDInfo(topLevelWindow);

		// Dragging stacks and parts
		dragAgents.add(new PartDragAgent(this));

		dropAgents.add(new StackDropAgent(this));
		dropAgents.add(new SplitDropAgent2(this));
		dropAgents.add(new DetachedDropAgent(this));

		// dragging trim
		dragAgents.add(new IBFDragAgent(this));
		dropAgents.add(new TrimDropAgent(this));

		// Register a 'dragDetect' against any stacks that get created
		hookWidgets();

		getDragShell().addDisposeListener(e -> dispose());
	}

	/**
	 * Do any widget specific logic hookup. We should break the model generic logic away from the
	 * specific platform by moving this code into an SWT-specific subclass
	 */
	private void hookWidgets() {
		EventHandler stackWidgetHandler = event -> {
			MUIElement element = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			// Only add listeners for stacks in *this* window
			MWindow elementWin = getModelService().getTopLevelWindowFor(element);
			if (elementWin != dragWindow) {
				return;
			}

			// Listen for drags starting in CTabFolders
			if (element.getWidget() instanceof CTabFolder || element.getWidget() instanceof ImageBasedFrame) {
				Control ctrl = (Control) element.getWidget();

				// Ensure there's only one drag detect listener per ctrl
				ctrl.removeDragDetectListener(dragDetector);
				ctrl.addDragDetectListener(dragDetector);
			}
		};

		IEventBroker eventBroker = dragWindow.getContext().get(IEventBroker.class);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, stackWidgetHandler);
	}

	public MWindow getDragWindow() {
		return dragWindow;
	}

	public EModelService getModelService() {
		return dragWindow.getContext().get(EModelService.class);
	}

	public Shell getDragShell() {
		return (Shell) (dragWindow.getWidget() instanceof Shell ? dragWindow.getWidget() : null);
	}

	protected void dispose() {
		clearOverlay();

		if (overlayFrame != null && !overlayFrame.isDisposed()) {
			overlayFrame.dispose();
		}
		overlayFrame = null;

		for (DragAgent agent : dragAgents) {
			agent.dispose();
		}
		for (DropAgent agent : dropAgents) {
			agent.dispose();
		}
		info.curElement = null;
		info = null;
	}

	private void track() {
		Display.getCurrent().syncExec(() -> {
			info.update();
			dragAgent.track(info);

			// Hack: Spin the event loop
			update();
		});
	}

	protected void startDrag() {
		// Create a new tracker for this drag instance
		Shell activeShell = Display.getCurrent().getActiveShell();
		if (activeShell == null) {
			return;
		}

		tracker = new Tracker(activeShell, SWT.NULL);
		tracker.setStippled(true);
		setRectangle(offScreenRect);

		tracker.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.MOD1) {
					isModified = false;
					track();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.MOD1) {
					isModified = true;
					track();
				}
			}
		});

		tracker.addListener(SWT.Move, event -> track());

		// Some control needs to capture the mouse during the drag or
		// other controls will interfere with the cursor
		getDragShell().setCapture(true);

		try {
			dragAgent.dragStart(info);

			// Run tracker until mouse up occurs or escape key pressed.
			boolean performDrop = tracker.open();

			// clean up
			finishDrag(performDrop);
		} finally {
			getDragShell().setCursor(null);
			getDragShell().setCapture(false);
		}
	}

	public void update() {
		Display.getCurrent().update();
	}

	private void finishDrag(boolean performDrop) {
		// Tear down any feedback
		if (tracker != null && !tracker.isDisposed()) {
			tracker.dispose();
			tracker = null;
		}

		if (overlayFrame != null) {
			overlayFrame.dispose();
			overlayFrame = null;
		}

		if (dragHost != null) {
			dragHost.dispose();
			dragHost = null;
		}

		// Perform either drop or cancel
		try {
			dragAgent.dragFinished(performDrop, info);
		} finally {
			dragAgent = null;
		}
	}

	public void setCursor(Cursor newCursor) {
		getDragShell().setCursor(newCursor);
	}

	public void setRectangle(Rectangle newRect) {
		if (tracker == null) {
			return;
		}

		Rectangle[] rectArray = { Geometry.copy(newRect) };
		tracker.setRectangles(rectArray);
	}

	public void hostElement(MUIElement element, int xOffset, int yOffset) {
		if (element == null) {
			if (dragHost != null && !dragHost.isDisposed()) {
				dragHost.dispose();
			}
			dragHost = null;
			return;
		}

		Shell parentShell = getDragShell();

		dragHost = new Shell(parentShell, SWT.NO_TRIM);
		dragHost.setBackground(parentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		dragHost.setLayout(new FillLayout());
		dragHost.setAlpha(120);
		Region shellRgn = new Region(dragHost.getDisplay());
		dragHost.setRegion(shellRgn);

		dragCtrl = (Control) element.getWidget();
		if (dragCtrl != null) {
			dragHost.setSize(dragCtrl.getSize());
		} else {
			dragHost.setSize(400, 400);
		}

		if (feedbackStyle == HOSTED) {
			// Special code to wrap the element in a CTF if it's coming from one
			MUIElement elementParent = element.getParent();
			if (elementParent instanceof MPartStack
					&& elementParent.getWidget() instanceof CTabFolder) {
				CTabFolder curCTF = (CTabFolder) elementParent.getWidget();
				dragHost.setSize(curCTF.getSize());
				CTabItem elementItem = getItemForElement(curCTF, element);
				assert (elementItem != null);

				IPresentationEngine renderingEngine = dragWindow.getContext().get(
						IPresentationEngine.class);
				CTabFolder ctf = new CTabFolder(dragHost, SWT.BORDER);
				CTabItem newItem = new CTabItem(ctf, SWT.NONE);
				newItem.setText(elementItem.getText());
				newItem.setImage(elementItem.getImage());

				element.getParent().getChildren().remove(element);
				dragCtrl = (Control) renderingEngine.createGui(element, ctf, getModelService()
						.getContainingContext(element));
				newItem.setControl(dragCtrl);
			}
		} else if (feedbackStyle == GHOSTED) {
			dragCtrl.setParent(dragHost);
			dragCtrl.setLocation(0, 0);
			dragHost.layout();
		}

		update();

		// Pass the shell to the info element for tracking
		dragHost.open();
		info.setDragHost(dragHost, xOffset, yOffset);
	}

	public void setDragHostVisibility(boolean visible) {
		if (dragHost == null || dragHost.isDisposed()) {
			return;
		}

		if (visible) {
			if (dragHost.getChildren().length > 0
					&& dragHost.getChildren()[0] instanceof CTabFolder) {
				CTabFolder ctf = (CTabFolder) dragHost.getChildren()[0];
				dragCtrl.setParent(ctf);
				dragHost.setVisible(true);
			} else {
				dragCtrl.setParent(dragHost);
			}
		} else {
			dragHost.setVisible(false);
		}
	}

	private CTabItem getItemForElement(CTabFolder elementCTF, MUIElement element) {
		for (CTabItem item : elementCTF.getItems()) {
			if (item.getData(AbstractPartRenderer.OWNING_ME) == element) {
				return item;
			}
		}
		return null;
	}

	public void clearOverlay() {
		frames.clear();
		images.clear();
		imageRects.clear();

		if (overlayFrame != null) {
			overlayFrame.setVisible(false);
		}
	}

	private void updateOverlay() {
		Rectangle bounds = getOverlayBounds();
		if (bounds == null) {
			clearOverlay();
			return;
		}

		if (overlayFrame == null) {
			overlayFrame = new Shell(getDragShell(), SWT.NO_TRIM | SWT.ON_TOP);
			overlayFrame.setData(DragAndDropUtil.IGNORE_AS_DROP_TARGET, Boolean.TRUE);
			overlayFrame.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
			overlayFrame.setAlpha(150);

			IStylingEngine stylingEngine = dragWindow.getContext().get(IStylingEngine.class);
			stylingEngine.setClassname(overlayFrame, "DragFeedback"); //$NON-NLS-1$
			stylingEngine.style(overlayFrame);

			overlayFrame.addPaintListener(e -> {
				for (int i = 0; i < images.size(); i++) {
					Image image = images.get(i);
					Rectangle iRect = imageRects.get(i);
					e.gc.drawImage(image, iRect.x, iRect.y);
				}
			});
		}

		// Reset for this set of overlays
		overlayFrame.setBounds(bounds);

		Region curRegion = overlayFrame.getRegion();
		if (curRegion != null && !curRegion.isDisposed()) {
			curRegion.dispose();
		}

		Region rgn = new Region();

		// Add frames
		for (Rectangle frameRect : frames) {
			int x = frameRect.x - bounds.x;
			int y = frameRect.y - bounds.y;

			if (frameRect.width > 6) {
				Rectangle outerBounds = new Rectangle(x - 3, y - 3, frameRect.width + 6,
						frameRect.height + 6);
				rgn.add(outerBounds);
				Rectangle innerBounds = new Rectangle(x, y, frameRect.width, frameRect.height);
				rgn.subtract(innerBounds);
			} else {
				Rectangle itemBounds = new Rectangle(x, y, frameRect.width, frameRect.height);
				rgn.add(itemBounds);
			}
		}

		// Add images
		for (int i = 0; i < images.size(); i++) {
			Rectangle ir = imageRects.get(i);
			Image im = images.get(i);

			int x = ir.x - bounds.x;
			int y = ir.y - bounds.y;

			rgn.add(x, y, im.getBounds().width, im.getBounds().height);
		}

		overlayFrame.setRegion(rgn);
		// Region object needs to be disposed at the right point in time
		addResourceDisposeListener(overlayFrame, rgn);
		overlayFrame.setVisible(true);
	}

	private void addResourceDisposeListener(Control control, final Resource resource) {
		control.addDisposeListener(e -> {
			if (!resource.isDisposed()) {
				resource.dispose();
			}
		});
	}

	private Rectangle getOverlayBounds() {
		Rectangle bounds = null;
		for (Rectangle fr : frames) {
			if (fr.width > 6) {
				Rectangle outerBounds = fr.clone();
				outerBounds.x -= 3;
				outerBounds.y -= 3;
				outerBounds.width += 6;
				outerBounds.height += 6;
				if (bounds == null) {
					bounds = outerBounds;
				}
				bounds.add(outerBounds);
			} else {
				if (bounds == null) {
					bounds = fr;
				}
				bounds.add(fr);
			}
		}

		for (Rectangle ir : imageRects) {
			if (bounds == null) {
				bounds = ir;
			}
			bounds.add(ir);
		}

		return bounds;
	}

	public void frameRect(Rectangle bounds) {
		clearOverlay();
		if (bounds != null) {
			addFrame(bounds);
		}
	}

	public void addDragAgent(DragAgent newAgent) {
		if (!dragAgents.contains(newAgent)) {
			dragAgents.add(newAgent);
		}
	}

	public void removeDragAgent(DragAgent agentToRemove) {
		dragAgents.remove(agentToRemove);
	}

	public void addDropAgent(DropAgent newAgent) {
		if (!dropAgents.contains(newAgent)) {
			dropAgents.add(newAgent);
		}
	}

	public void removeDropAgent(DropAgent agentToRemove) {
		dropAgents.remove(agentToRemove);
	}

	private DragAgent getDragAgent(DnDInfo info) {
		for (DragAgent agent : dragAgents) {
			if (agent.canDrag(info)) {
				return agent;
			}
		}
		return null;
	}

	public DropAgent getDropAgent(MUIElement dragElement, DnDInfo info) {
		for (DropAgent agent : dropAgents) {
			if (agent.canDrop(dragElement, info)) {
				return agent;
			}
		}
		return null;
	}

	/**
	 * @return Return the feedback style
	 */
	public int getFeedbackStyle() {
		return feedbackStyle;
	}

	public void setHostBounds(Rectangle newBounds) {
		if (dragHost == null || dragHost.isDisposed()) {
			return;
		}

		info.setDragHostBounds(newBounds);
		update();
	}
}
