package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.actions.*;

/**
 * A <code>LabelRetargetAction</code> extends the behavior of
 * RetargetAction.  It will track the enable state, label, and 
 * tool tip text of the target action..
 * <p>
 * This class may be instantiated. It is not intented to be subclassed.
 * </p>
 *
 * @since 2.0 
 */
public class LabelRetargetAction extends RetargetAction {
	private String defaultText;
	private String defaultToolTipText;
/**
 * Constructs a LabelRetargetAction.
 */
public LabelRetargetAction(String actionID, String text) {
	super(actionID, text);
	this.defaultText = text;
	this.defaultToolTipText = text;
}
/**
 * The action handler has changed.  Update self.
 */
protected void propogateChange(PropertyChangeEvent event) {
	super.propogateChange(event);
	if (event.getProperty().equals(Action.TEXT)) {
		String str = (String)event.getNewValue();
		super.setText(str);
	} 
	else if (event.getProperty().equals(Action.TOOL_TIP_TEXT)) {
		String str = (String)event.getNewValue();
		super.setToolTipText(str);
	}
}
/**
 * Set the action handler.  Update self.
 */
protected void setActionHandler(IAction handler) {
	// Run the default behavior.
	super.setActionHandler(handler);

	// Now update the label and tooltip.
	if (handler == null) {
		super.setText(defaultText);
		super.setToolTipText(defaultToolTipText);
	} else {
		super.setText(handler.getText());
		super.setToolTipText(handler.getToolTipText());
	}
}
/**
 * Sets the action's label text to the given value.
 */
public void setText(String text) {
	super.setText(text);
	defaultText = text;
}
/**
 * Sets the tooltip text to the given text.
 * The value <code>null</code> clears the tooltip text.
 */
public void setToolTipText(String text) {
	super.setToolTipText(text);
	defaultToolTipText = text;
}
}
