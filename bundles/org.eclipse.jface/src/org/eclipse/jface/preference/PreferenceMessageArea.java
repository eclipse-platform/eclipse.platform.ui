/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.preference;

import org.eclipse.jface.dialogs.ControlAnimator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The PreferenceMessageArea is a subclass of DialogMessageArea that provides a
 * component for displaying a normal title message and an animated message area
 * for displaying errors and warnings.
 * 
 * @since 3.2
 * 
 * This class is not intended to be extended by clients.
 * 
 */
class PreferenceMessageArea extends DialogMessageArea {
	
    private String lastMessageText;

    private int lastMessageType;

    private CLabel titleLabel;

	private MessageArea messageArea;
	
	private static final String ELLIPSIS = "..."; //$NON-NLS-1$
	
	private PreferenceDialog preferenceDialog;
	
	private static int titleLabelOffset = -1;

	

	/**
	 * Creates a new instance of the receiver and passes along the 
	 * PreferenceDialog that creates it.
	 * 
	 * @param preferenceDialog The PreferenceDialog that creates
	 * the message area.
	 */
	public PreferenceMessageArea(PreferenceDialog preferenceDialog) {
		this.preferenceDialog = preferenceDialog;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {
	       
        // Message label
        titleLabel = new CLabel(parent, SWT.NONE);
        titleLabel.setFont(JFaceResources.getBannerFont());
        
        messageArea = new MessageArea(preferenceDialog.formTitleComposite, SWT.NONE);
        messageArea.setFont(JFaceResources.getDialogFont());
   		messageArea.setVisible(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#setMessageLayoutData(java.lang.Object)
	 */
	public void setMessageLayoutData(Object layoutData) {
		((FormData) layoutData).top = null;
		((FormData) layoutData).right = new FormAttachment(100,-getTitleLabelOffset());
	     messageArea.setLayoutData(layoutData);
	}


	/**
	 * Calculates the width difference between the title label and the parent composite
	 * of the message area. Required for when the workbench adds a toolbar to the
	 * preference dialog.
	 * 
	 * @return the width difference if the calculation yields a value > 0, otherwise it 
	 * 		will return 0.Subsequent calls will immediately return the width difference 
	 * 		if it has already been calculated and is > 0.
	 */
	private int getTitleLabelOffset() {
		if (titleLabelOffset == -1) {
			int offsetCalc = preferenceDialog.formTitleComposite.getBounds().width - 
							 titleLabel.getBounds().width;
			if (offsetCalc > 0)
				return titleLabelOffset = offsetCalc;
			
			return 0;
		}
		return titleLabelOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#updateText(java.lang.String, int)
	 */
	public void updateText(String newMessage, int newType) {
        Image newImage = null;
        switch (newType) {
        case IMessageProvider.NONE:
            if (newMessage == null) {
				restoreTitle();
			} else {
				showTitle(newMessage, null);
			}
            return;
        case IMessageProvider.INFORMATION:
            newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
            break;
        case IMessageProvider.WARNING:
            newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
            break;
        case IMessageProvider.ERROR:
            newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);

            break;
        }
        messageArea.setVisible(true);
        messageArea.moveAbove(null);
        messageArea.setImage(newImage);
        
        messageArea.setText(shortenText(newMessage,messageArea, 
        		newImage.getBounds().width + IDialogConstants.HORIZONTAL_SPACING));
        messageArea.setToolTipText(newMessage);
        lastMessageText = newMessage;
        
		int bottom = messageArea.getParent().getBounds().height;

        Rectangle titleLabelBounds = titleLabel.getBounds();
        if(Policy.getAnimator().getAnimationState() == ControlAnimator.CLOSED || 
           Policy.getAnimator().getAnimationState() == ControlAnimator.CLOSING){

            if(Policy.getAnimator().getAnimationState() == ControlAnimator.CLOSED) {
    			messageArea.setBounds(
    					titleLabelBounds.x,
    					bottom,
    					titleLabelBounds.width,
    					messageArea.computeSize(SWT.DEFAULT,SWT.DEFAULT).y);
    		}
    		Policy.getAnimator().setAnimationState(ControlAnimator.OPENING);
    		Policy.getAnimator().setVisible(true, messageArea);
        }
        setMessageLayoutData(messageArea.getLayoutData());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#restoreTitle()
	 */
	public void restoreTitle() {
        titleLabel.setVisible(true);
      if(Policy.getAnimator().getAnimationState() == ControlAnimator.OPENING || 
         Policy.getAnimator().getAnimationState() == ControlAnimator.OPEN){
  		Policy.getAnimator().setAnimationState(ControlAnimator.CLOSING);
  		Policy.getAnimator().setVisible(false, messageArea);
      }
      lastMessageText = null;
      lastMessageType = IMessageProvider.NONE;
	}


	/**
	 * Shortens the given text <code>textValue</code> so that its width in
	 * pixels does not exceed the width of the given control plus an extra 
	 * offset in pixels. Overrides characters in the center of the original 
	 * string with an ellipsis ("...") if necessary. If a <code>null</code> 
	 * value is given, <code>null</code> is returned.
	 * 
	 * @param textValue
	 *            the original string or <code>null</code>
	 * @param control
	 *            the control the string will be displayed on
	 * @param offset
	 * 			  additional pixel offset by which to shorten the text
	 * @return the string to display, or <code>null</code> if null was passed
	 *         in
	 * 
	 * @since 3.2
	 */
	private static String shortenText(String textValue, Control control, int offset) {
		if (textValue == null) {
			return null;
		}
		GC gc = new GC(control);
		int maxWidth = control.getBounds().width - 5 - offset;
		if (gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();
		int pivot = length / 2;
		int start = pivot;
		int end = pivot + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			String s = s1 + ELLIPSIS + s2;
			int l = gc.textExtent(s).x;
			if (l < maxWidth) {
				gc.dispose();
				return s;
			}
			start--;
			end++;
		}
		gc.dispose();
		return textValue;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#clearErrorMessage()
	 */
	public void clearErrorMessage() {
        if (lastMessageText == null) {
			restoreTitle();
		} else {
			updateText(lastMessageText, lastMessageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#setTitleLayoutData(java.lang.Object)
	 */
	public void setTitleLayoutData(Object layoutData) {
        titleLabel.setLayoutData(layoutData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#showTitle(java.lang.String, org.eclipse.swt.graphics.Image)
	 */
	public void showTitle(String titleMessage, Image titleImage) {
	       titleLabel.setImage(titleImage);
	        titleLabel.setText(titleMessage);
	        restoreTitle();
	        return;
	}
}
