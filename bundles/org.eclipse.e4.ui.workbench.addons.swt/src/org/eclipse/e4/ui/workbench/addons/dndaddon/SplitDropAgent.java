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

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class SplitDropAgent extends DropAgent {
	private static final int NOWHERE = -1;

	private MPartStack dropStack;
	private CTabFolder dropCTF;
	private Rectangle clientBounds;
	private String weight;
	private int curDockLocation = NOWHERE;

	private Rectangle ctfBounds;

	private MUIElement outerRelTo;
	private Rectangle ocBounds;
	private boolean outerDock;

	/**
	 * @param modelService
	 *            The model service related to this agent
	 * 
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

		weight = dropStack.getContainerData();
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

		curDockLocation = NOWHERE;

		super.dragLeave(dragElement, info);
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		if (dndManager.getFeedbackStyle() != DnDManager.HOSTED && curDockLocation != NOWHERE) {
			dock(dragElement, curDockLocation);
		}
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

		boolean curOuter = outerDock;
		int dockLocation = getDockLocation(info);
		if (dockLocation == curDockLocation && curOuter == outerDock)
			return true;

		if (dropStack == dragElement && !outerDock)
			return false;

		curDockLocation = dockLocation;

		if (curDockLocation != NOWHERE) {
			Rectangle dockBounds = getDockBounds(curDockLocation);
			if (dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
				dock(dragElement, curDockLocation);
			} else if (dndManager.getFeedbackStyle() == DnDManager.GHOSTED) {
				dndManager.setHostBounds(dockBounds);
			}
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
		} else {
			unDock(dragElement);
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
		}

		return true;
	}

	/**
	 * @param curDockLocation2
	 * @return
	 */
	private Rectangle getDockBounds(int location) {
		if (!outerDock) {
			Rectangle bounds = new Rectangle(ctfBounds.x, ctfBounds.y, ctfBounds.width,
					ctfBounds.height);
			dndManager.frameRect(ctfBounds);

			if (location == EModelService.ABOVE) {
				bounds.height /= 2;
			} else if (location == EModelService.BELOW) {
				bounds.height /= 2;
				bounds.y += bounds.height;
			} else if (location == EModelService.LEFT_OF) {
				bounds.width /= 2;
			} else if (location == EModelService.RIGHT_OF) {
				bounds.width /= 2;
				bounds.x += bounds.width;
			}

			bounds.x += 8;
			bounds.y += 8;
			bounds.width -= 16;
			bounds.height -= 16;
			dndManager.addFrame(bounds);
		} else {
			Rectangle bounds = new Rectangle(ocBounds.x, ocBounds.y, ocBounds.width,
					ocBounds.height);
			int splitWidth = (int) (bounds.width * 0.34);
			int splitHeight = (int) (bounds.height * 0.34);
			if (location == EModelService.ABOVE) {
				Rectangle topRect = new Rectangle(bounds.x, bounds.y, bounds.width, splitHeight);
				Rectangle bottomRect = new Rectangle(bounds.x, bounds.y + splitHeight + 3,
						bounds.width, bounds.height - splitHeight - 3);
				dndManager.frameRect(topRect);
				dndManager.frameRect(bottomRect);
			} else if (location == EModelService.BELOW) {
				Rectangle topRect = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height
						- splitHeight);
				Rectangle bottomRect = new Rectangle(bounds.x, bounds.y + bounds.height
						- splitHeight + 8, bounds.width, splitHeight - 8);
				dndManager.frameRect(topRect);
				dndManager.addFrame(bottomRect);
			} else if (location == EModelService.LEFT_OF) {
				Rectangle leftRect = new Rectangle(bounds.x, bounds.y, splitWidth, bounds.height);
				Rectangle rightRect = new Rectangle(bounds.x + splitWidth + 8, bounds.y,
						bounds.width - splitWidth - 8, bounds.height);
				dndManager.frameRect(leftRect);
				dndManager.addFrame(rightRect);
			} else if (location == EModelService.RIGHT_OF) {
				Rectangle leftRect = new Rectangle(bounds.x, bounds.y, bounds.width - splitWidth,
						bounds.height);
				Rectangle rightRect = new Rectangle(bounds.x + bounds.width - splitWidth + 8,
						bounds.y, splitWidth - 8, bounds.height);
				dndManager.frameRect(leftRect);
				dndManager.addFrame(rightRect);
			}
		}
		return null;
	}

	private int getDockLocation(DnDInfo info) {
		if (outerRelTo != null) {
			// Are we close to the 'outerBounds' ?
			if (info.cursorPos.x - ocBounds.x < 30) {
				outerDock = true;
				return EModelService.LEFT_OF;
			}
			if ((ocBounds.x + ocBounds.width) - info.cursorPos.x < 30) {
				outerDock = true;
				return EModelService.RIGHT_OF;
			}
			if (info.cursorPos.y - ocBounds.y < 30) {
				outerDock = true;
				return EModelService.ABOVE;
			}
			if ((ocBounds.y + ocBounds.height) - info.cursorPos.y < 30) {
				outerDock = true;
				return EModelService.BELOW;
			}
		}

		outerDock = false;

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
		dndManager.setHostBounds(null);
		dndManager.setDragHostVisibility(true);
	}

	protected boolean dock(MUIElement dragElement, int where) {
		dndManager.setDragHostVisibility(false);
		MPartSashContainerElement relTo = dropStack;
		MPartStack toInsert;

		if (outerDock) {
			relTo = (MPartSashContainerElement) outerRelTo;
		}

		if (dragElement instanceof MPartStack) {
			toInsert = (MPartStack) dragElement;
			toInsert.getParent().getChildren().remove(toInsert);
		} else {
			// wrap it in a stack if it's a part
			MStackElement stackElement = (MStackElement) dragElement;
			toInsert = BasicFactoryImpl.eINSTANCE.createPartStack();
			toInsert.getChildren().add(stackElement);
			toInsert.setSelectedElement(stackElement);
		}

		int ratio = outerDock ? 34 : 50; // an 'outer' dock should take less real estate
		MUIElement relToParent = relTo.getParent();
		dndManager.getModelService().insert(toInsert, relTo, where, ratio);

		// Force the new sash to have the same weight as the original element
		if (relTo.getParent() != relToParent && !outerDock)
			relTo.getParent().setContainerData(weight);
		dndManager.update();

		return true;
	}
}
