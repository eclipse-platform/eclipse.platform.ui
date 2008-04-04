/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A <code>LabelRetargetAction</code> extends the behavior of
 * RetargetAction.  It will track the enable state, label, and 
 * tool tip text of the target action..
 * <p>
 * This class may be instantiated. It is not intented to be subclassed.
 * </p>
 *
 * @since 2.0 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LabelRetargetAction extends RetargetAction {
    private String defaultText;

    private String defaultToolTipText;

    private ImageDescriptor defaultHoverImage;

    private ImageDescriptor defaultImage;

    private ImageDescriptor defaultDisabledImage;

    private String acceleratorText;

    /**
     * Constructs a LabelRetargetAction with the given action id and text.
     * 
     * @param actionID the retargetable action id
     * @param text the action's text, or <code>null</code> if there is no text
     */
    public LabelRetargetAction(String actionID, String text) {
        this(actionID, text, IAction.AS_UNSPECIFIED);
    }

    /**
     * Constructs a RetargetAction with the given action id, text and style.
     * 
     * @param actionID the retargetable action id
     * @param text the action's text, or <code>null</code> if there is no text
     * @param style one of <code>AS_PUSH_BUTTON</code>, <code>AS_CHECK_BOX</code>,
     * 		<code>AS_DROP_DOWN_MENU</code>, <code>AS_RADIO_BUTTON</code>, and
     * 		<code>AS_UNSPECIFIED</code>.
     * @since 3.0
     */
    public LabelRetargetAction(String actionID, String text, int style) {
        super(actionID, text, style);
        this.defaultText = text;
        this.defaultToolTipText = text;
        acceleratorText = LegacyActionTools.extractAcceleratorText(text);
    }

    /**
     * The action handler has changed.  Update self.
     */
    protected void propagateChange(PropertyChangeEvent event) {
        super.propagateChange(event);
        String prop = event.getProperty();
        if (prop.equals(IAction.TEXT)) {
            String str = (String) event.getNewValue();
            super.setText(appendAccelerator(str));
        } else if (prop.equals(IAction.TOOL_TIP_TEXT)) {
            String str = (String) event.getNewValue();
            super.setToolTipText(str);
        } else if (prop.equals(IAction.IMAGE)) {
            updateImages(getActionHandler());
        }
    }

    /**
     * Sets the action handler.  Update self.
     */
    protected void setActionHandler(IAction handler) {
        // Run the default behavior.
        super.setActionHandler(handler);

        // Now update the label, tooltip and images.
        if (handler == null) {
            super.setText(defaultText);
            super.setToolTipText(defaultToolTipText);
        } else {
            // If no text is specified by the handler, use the default text.  Fixes 22529.
            String handlerText = handler.getText();
            if (handlerText == null || handlerText.length() == 0) {
                handlerText = defaultText;
            }
            super.setText(appendAccelerator(handlerText));
            super.setToolTipText(handler.getToolTipText());
        }
        updateImages(handler);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void setDisabledImageDescriptor(ImageDescriptor image) {
        super.setDisabledImageDescriptor(image);
        defaultDisabledImage = image;
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void setHoverImageDescriptor(ImageDescriptor image) {
        super.setHoverImageDescriptor(image);
        defaultHoverImage = image;
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void setImageDescriptor(ImageDescriptor image) {
        super.setImageDescriptor(image);
        defaultImage = image;
    }

    /**
     * Sets the action's label text to the given value.
     */
    public void setText(String text) {
        super.setText(text);
        acceleratorText = LegacyActionTools.extractAcceleratorText(text);
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

    /**
     * Ensures the accelerator is correct in the text (handlers are not
     * allowed to change the accelerator).
     */
    private String appendAccelerator(String newText) {
        if (newText == null) {
			return null;
		}

        // Remove any accelerator
        String str = removeAcceleratorText(newText);
        // Append our accelerator
        if (acceleratorText != null) {
			str = str + acceleratorText;
		}
        return str;
    }

    /**
     * Updates the images for this action based on the given handler.
     */
    private void updateImages(IAction handler) {
        if (handler == null) {
            super.setHoverImageDescriptor(defaultHoverImage);
            super.setImageDescriptor(defaultImage);
            super.setDisabledImageDescriptor(defaultDisabledImage);
        } else {
            // use the default images if the handler has no images set
            ImageDescriptor hoverImage = handler.getHoverImageDescriptor();
            ImageDescriptor image = handler.getImageDescriptor();
            ImageDescriptor disabledImage = handler
                    .getDisabledImageDescriptor();
            if (hoverImage != null || image != null || disabledImage != null) {
                super.setHoverImageDescriptor(hoverImage);
                super.setImageDescriptor(image);
                super.setDisabledImageDescriptor(disabledImage);
            } else {
                super.setHoverImageDescriptor(defaultHoverImage);
                super.setImageDescriptor(defaultImage);
                super.setDisabledImageDescriptor(defaultDisabledImage);
            }
        }
    }

}
