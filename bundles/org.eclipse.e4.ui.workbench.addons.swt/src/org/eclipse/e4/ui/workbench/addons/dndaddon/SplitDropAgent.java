/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class SplitDropAgent extends DropAgent {
	private static final int NOWHERE = -1;

	private int curDockLocation = NOWHERE;
	private boolean onEdge = false;

	private MPartStack dropStack;
	private CTabFolder dropCTF;
	private Rectangle clientBounds;

	private Rectangle ctfBounds;

	private MUIElement outerRelTo;
	private Rectangle ocBounds;

	private SplitFeedbackOverlay feedback = null;

	/**
	 * @param manager
	 *            the DnDManager using this agent
	 */
	public SplitDropAgent(DnDManager manager) {
		super(manager);
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		if (!(dragElement instanceof MStackElement) && !(dragElement instanceof MPartStack))
			return false;

		dropStack = null;

		// Hack! allow splitting the 'empty' editor area stack
		if (info.curElement instanceof MPartStack) {
			MPartStack stack = (MPartStack) info.curElement;
			if (dndManager.getModelService().isLastEditorStack(stack))
				dropStack = stack;
		}

		if (dropStack == null) {
			if (!(info.curElement instanceof MStackElement)
					&& !dndManager.getModelService().isLastEditorStack(info.curElement))
				return false;

			// Detect placeholders
			MUIElement parent = info.curElement.getParent();
			if (info.curElement instanceof MPart && info.curElement.getCurSharedRef() != null)
				parent = info.curElement.getCurSharedRef().getParent();

			if (!(parent instanceof MPartStack) || !(parent.getWidget() instanceof CTabFolder))
				return false;

			dropStack = (MPartStack) parent;
		}

		// You can only drag MParts from window to window
		if (!(dragElement instanceof MPart)) {
			EModelService ms = dndManager.getModelService();
			MWindow dragElementWin = ms.getTopLevelWindowFor(dragElement);
			MWindow dropWin = ms.getTopLevelWindowFor(dropStack);
			if (dragElementWin != dropWin)
				return false;
		}

		// We can't split ourselves with if the element being dragged is the only element in the
		// stack (we check for '2' because the dragAgent puts a Drag Placeholder in the stack)
		MUIElement dragParent = dragElement.getParent();
		if (dragParent == dropStack && dropStack.getChildren().size() == 2)
			return false;

		dropCTF = (CTabFolder) dropStack.getWidget();

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragEnter(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public void dragEnter(MUIElement dragElement, DnDInfo info) {
		super.dragEnter(dragElement, info);

		clientBounds = dropCTF.getClientArea();
		clientBounds = Display.getCurrent().map(dropCTF, null, clientBounds);
		ctfBounds = dropCTF.getBounds();
		ctfBounds = Display.getCurrent().map(dropCTF.getParent(), null, ctfBounds);

		// Find the root of the dropStack's sash structure
		outerRelTo = dropStack.getParent();
		if (outerRelTo instanceof MPartSashContainer) {
			while (outerRelTo != null && !(outerRelTo.getWidget() instanceof Composite))
				outerRelTo = outerRelTo.getParent();
		}

		// If the stack is in an MArea or a Perspective then allow 'outer' docking
		if (outerRelTo instanceof MArea) {
			// If the relTo is in the MArea then use its 'curSharedRef'
			outerRelTo = outerRelTo.getCurSharedRef();
		} else if (outerRelTo instanceof MPartSashContainer) {
			MUIElement relToParent = outerRelTo.getParent();
			if (relToParent instanceof MArea) {
				outerRelTo = relToParent.getCurSharedRef();
			} else if (relToParent instanceof MPerspective) {
				outerRelTo = relToParent.getParent(); // PerspectiveStack
			} else {
				outerRelTo = null;
			}
		} else {
			outerRelTo = null;
		}

		if (outerRelTo != null) {
			Composite outerComposite = (Composite) outerRelTo.getWidget();
			ocBounds = outerComposite.getBounds();
			ocBounds = Display.getCurrent().map(outerComposite.getParent(), null, ocBounds);
		} else {
			ocBounds = null;
		}

		getDockLocation(info);
		showFeedback(curDockLocation);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		if (dndManager.getFeedbackStyle() != DnDManager.SIMPLE)
			unDock(dragElement);
		dndManager.clearOverlay();
		clearFeedback();
		curDockLocation = NOWHERE;

		super.dragLeave(dragElement, info);
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		if (dndManager.getFeedbackStyle() != DnDManager.HOSTED && curDockLocation != NOWHERE) {
			dock(dragElement, curDockLocation);
			reactivatePart(dragElement);
		}
		clearFeedback();
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#track(org.eclipse.e4.ui.model.application
	 * .ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {
		if (!clientBounds.contains(info.cursorPos))
			return false;

		boolean wasOnEdge = onEdge;
		int dockLocation = getDockLocation(info);

		if (feedback != null) {
			feedback.setFeedback(getEnclosed(), getModified());
		}

		if (dockLocation == curDockLocation && wasOnEdge == onEdge)
			return true;

		if (dropStack == dragElement && !onEdge)
			return false;

		curDockLocation = dockLocation;

		if (curDockLocation != NOWHERE) {
			showFeedback(curDockLocation);
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
		} else {
			unDock(dragElement);
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
		}

		return true;
	}

	/**
	 * for 'edges' you can modify the effect of the drop. If the drop area is at the edge of the
	 * perspective stack a modified drop will place it *outside* the perspectives. If the drop area
	 * is the shared area then a modified drop will drop *inside* the shared area.
	 *
	 * @return Whether this is a 'modified' drop.
	 */
	private boolean getModified() {
		if (!onEdge)
			return false;
		return dndManager.isModified;
	}

	/**
	 * @return Whether the feedback should show an outer 'enclosing' rectangle or two separate
	 *         rectangles.
	 */
	private boolean getEnclosed() {
		if (onEdge) {
			if (outerRelTo instanceof MPerspectiveStack)
				return !getModified();
			return getModified(); // 'Inner' drop
		}

		return true;
	}

	/**
	 * @param curDockLocation2
	 * @return
	 */
	private void showFeedback(int location) {
		if (location == NOWHERE)
			return;

		Rectangle feedbackBounds = null;

		if (!onEdge) {
			Rectangle bounds = new Rectangle(ctfBounds.x, ctfBounds.y, ctfBounds.width,
					ctfBounds.height);
			// bounds = Display.getCurrent().map(dropCTF.getParent(), null, bounds);
			feedbackBounds = bounds;
		} else {
			Rectangle bounds = new Rectangle(ocBounds.x, ocBounds.y, ocBounds.width,
					ocBounds.height);
			feedbackBounds = bounds;
		}

		if (feedback != null)
			feedback.dispose();
		int side = 0;
		if (location == EModelService.ABOVE) {
			side = SWT.TOP;
		} else if (location == EModelService.BELOW) {
			side = SWT.BOTTOM;
		} else if (location == EModelService.LEFT_OF) {
			side = SWT.LEFT;
		} else if (location == EModelService.RIGHT_OF) {
			side = SWT.RIGHT;
		}

		float pct = (float) (onEdge ? 0.34 : 0.50);

		clearFeedback();

		feedback = new SplitFeedbackOverlay(dropCTF.getShell(), feedbackBounds, side, pct,
				getEnclosed(), getModified());
		feedback.setVisible(true);
	}

	private void clearFeedback() {
		if (feedback == null)
			return;

		feedback.dispose();
		feedback = null;
	}

	private int getDockLocation(DnDInfo info) {
		if (outerRelTo != null) {
			int outerThreshold = 50;
			// Are we close to the 'outerBounds' ?
			if (info.cursorPos.y - ocBounds.y < outerThreshold) {
				onEdge = true;
				return EModelService.ABOVE;
			} else if ((ocBounds.y + ocBounds.height) - info.cursorPos.y < outerThreshold) {
				onEdge = true;
				return EModelService.BELOW;
			} else if (info.cursorPos.x - ocBounds.x < outerThreshold) {
				onEdge = true;
				return EModelService.LEFT_OF;
			} else if ((ocBounds.x + ocBounds.width) - info.cursorPos.x < outerThreshold) {
				onEdge = true;
				return EModelService.RIGHT_OF;
			}
		}

		onEdge = false;

		int dx = info.cursorPos.x - clientBounds.x;
		int dy = info.cursorPos.y - clientBounds.y;
		int dxr = (clientBounds.x + clientBounds.width) - info.cursorPos.x;
		int dyr = (clientBounds.y + clientBounds.height) - info.cursorPos.y;
		int minDx = Math.min(dx, dxr);
		int minDy = Math.min(dy, dyr);

		if (minDx < minDy)
			return dx < dxr ? EModelService.LEFT_OF : EModelService.RIGHT_OF;

		return dy < dyr ? EModelService.ABOVE : EModelService.BELOW;
	}

	protected void unDock(MUIElement dragElement) {
		dndManager.clearOverlay();
		clearFeedback();
		dndManager.setHostBounds(null);
		dndManager.setDragHostVisibility(true);
	}

	protected boolean dock(MUIElement dragElement, int where) {
		dndManager.setDragHostVisibility(false);
		MPartSashContainerElement relTo = dropStack;
		MPartStack toInsert;

		if (crossSharedAreaBoundary(dragElement, dropStack)) {
			if (!dndManager.isModified) {
				relTo = (MPartSashContainerElement) outerRelTo;
			}
		} else if (getModified()) {
			relTo = (MPartSashContainerElement) outerRelTo;
		}

		if (dragElement instanceof MPartStack) {
			toInsert = (MPartStack) dragElement;

			// Ensure we restore the stack to the presentation first
			if (toInsert.getTags().contains(IPresentationEngine.MINIMIZED)) {
				toInsert.getTags().remove(IPresentationEngine.MINIMIZED);
			}

			toInsert.getParent().getChildren().remove(toInsert);
		} else {
			// wrap it in a stack if it's a part
			MStackElement stackElement = (MStackElement) dragElement;
			toInsert = BasicFactoryImpl.eINSTANCE.createPartStack();
			toInsert.getChildren().add(stackElement);
			toInsert.setSelectedElement(stackElement);
		}

		float pct = (float) (onEdge ? 0.34 : 0.50);
		dndManager.getModelService().insert(toInsert, relTo, where, pct);

		return true;
	}

	/**
	 * @param dragElement
	 * @param dropStack2
	 * @return
	 */
	private boolean crossSharedAreaBoundary(MUIElement dragElement, MPartStack dropStack) {
		EModelService ms = dndManager.getModelService();
		boolean deNotInSA = (ms.getElementLocation(dragElement) & EModelService.IN_SHARED_AREA) == 0;
		boolean dsInSA = (ms.getElementLocation(dropStack) & EModelService.IN_SHARED_AREA) != 0;

		return deNotInSA && dsInSA;
	}
}
