package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A PlaceHolder is a non-visible stand-in for a IVisualPart.
 */
public class PartPlaceholder extends LayoutPart {
	public PartPlaceholder(String id) {
		super(id);
	}
/**
 * Creates the SWT control
 */
public void createControl(Composite parent) {
}
/**
 * Get the part control.  This method may return null.
 */
public Control getControl() {
	return null;
}
/**
 * Returns true if this part is visible.
 */
public boolean isVisible() {
	return false;
}
}
