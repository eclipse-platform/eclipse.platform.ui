/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.presentation.barebones;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Bare-bones stack presentation. The currently selected part fills the entire
 * presentation area, and all other parts are invisible. Does not provide a 
 * system menu, pane menu, trim, drag/drop, toolbars, or any way to switch 
 * parts.
 */
public class BareBonesPartPresentation extends StackPresentation {

/**
 * Main widget for the presentation
 */
private Composite presentationControl;

/**
 * Currently selected part
 */
private IPresentablePart current;

/**
 * Creates a new bare-bones part presentation, given the parent composite and 
 * an IStackPresentationSite interface that will be used to communicate with 
 * the workbench.
 * 
 * @param stackSite interface to the workbench
 */
public BareBonesPartPresentation(Composite parent, 
		IStackPresentationSite stackSite) {
	super(stackSite);		
	
	// Create a top-level control for the presentation.
	presentationControl = new Composite(parent, SWT.NONE);
	
	// Add a dispose listener. This will call the presentationDisposed()
	// method when the widget is destroyed.
	presentationControl.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			presentationDisposed();
		}
	});
}

public void dispose() {
	// Dispose the main presentation widget. This will cause 
	// presentationDisposed to be called, which will do the real cleanup.
	presentationControl.dispose();
}

/**
 * Perform any cleanup. This method should remove any listeners that were
 * attached to other objects. This gets called when the presentation
 * widget is disposed. This is safer than cleaning up in the dispose() 
 * method, since this code will run even if some unusual circumstance 
 * destroys the Shell without first calling dispose().
 */
protected void presentationDisposed() {
	// Remove any listeners that were attached to any IPresentableParts or
	// global Eclipse resources. This is necessary in order to prevent
	// memory leaks. Currently, there is nothing to do.
}

public void setBounds(Rectangle bounds) {
	// Set the bounds of the presentation widget
	presentationControl.setBounds(bounds);
	
	// Update the bounds of the currently visible part
	updatePartBounds();
}

private void updatePartBounds() {
	if (current != null) {
		current.setBounds(presentationControl.getBounds());
	}
}

public Point computeMinimumSize() {
	return new Point(16,16);
}

public void setVisible(boolean isVisible) {
	
	// Make the presentation widget visible
	presentationControl.setVisible(isVisible);
	
	// Make the currently visible part visible
	if (current != null) {
		current.setVisible(isVisible);
	}

	if (isVisible) {
		// Restore the bounds of the currently visible part. 
		// IPartPresentations can be used by multiple StackPresentations,
		// although only one such presentation is ever visible at a time.
		// It is possible that some other presentation has changed the
		// bounds of the part since it was last visible, so we need to
		// update the part's bounds when the presentation becomes visible.
		updatePartBounds();
	}
}

public void selectPart(IPresentablePart toSelect) {
	// Ignore redundant selections
	if (toSelect == current) {
		return;
	}
	
	// If there was an existing part selected, make it invisible
	if (current != null) {
		current.setVisible(false);
	}
	
	// Select the new part
	current = toSelect;
	
	// Ordering is important here. We need to make the part
	// visible before updating its bounds, or the call to setBounds
	// may be ignored.
	
	if (current != null) {
		// Make the newly selected part visible
		current.setVisible(true);
	}
	
	// Update the bounds of the newly selected part
	updatePartBounds();
}

public Control[] getTabList(IPresentablePart part) {
	return new Control[] {part.getControl()};
}

public Control getControl() {
	return presentationControl;
}

// Methods that don't do anything yet
public void setActive(int newState) {}
public void setState(int state) {}
public void addPart(IPresentablePart newPart, Object cookie) {}
public void removePart(IPresentablePart oldPart) {}
public StackDropResult dragOver(Control currentControl, Point location) {return null;}
public void showSystemMenu() {}
public void showPaneMenu() {}
}
