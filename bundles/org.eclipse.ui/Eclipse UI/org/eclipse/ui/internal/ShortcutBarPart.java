package org.eclipse.ui.internal;

import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;

/**
 * A ShortcutBarPart acts as a target for drag and drop events in
 * a PerspectivePresentation.  It is not intended to act as a real part.
 */
public class ShortcutBarPart extends LayoutPart {
	private ToolBarManager tbm;
	private ToolBar toolbar;
/**
 * ShortcutBarPart constructor comment.
 * @param id java.lang.String
 */
public ShortcutBarPart(ToolBarManager tbm) {
	super("ShortcutBarPart");
	this.tbm = tbm;
	this.toolbar = tbm.getControl();
	toolbar.setData((IPartDropTarget)this);
}
/**
 * Creates the SWT control
 */
final public void createControl(Composite parent) {
	// Nothing to do.
}
/**
 * Get the part control.  This method may return null.
 */
final public Control getControl() {
	return toolbar;
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	return this;
}
}
