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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ImageAndMessageArea;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

/**
 * The PreferenceMessageArea is a subclass of DialogMessageArea that provides a
 * component for displaying a normal title message and an animated message area
 * for displaying errors and warnings.
 * 
 * @since 3.2
 */
class PreferenceMessageArea extends DialogMessageArea {
	
    private String lastMessageText;

    private int lastMessageType;

    private CLabel titleLabel;

	private ImageAndMessageArea messageArea;
	
	private PreferenceDialog preferenceDialog;
	
	private static int titleLabelOffset = -1;
	
	private ControlAnimator animator;

	

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
        
        messageArea = new ImageAndMessageArea(preferenceDialog.formTitleComposite, SWT.WRAP);
        messageArea.setFont(JFaceResources.getDialogFont());
   		messageArea.setVisible(false);
   		
   		animator = Policy.getAnimatorFactory().createAnimator(messageArea);
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

        messageArea.moveAbove(null);
        messageArea.setImage(newImage);
        
        messageArea.setText(newMessage);
        messageArea.setToolTipText(newMessage);
        lastMessageText = newMessage;
        
		int bottom = messageArea.getParent().getBounds().height;
		if (messageArea.isVisible() == false && messageArea.getBounds().y != bottom) {
			messageArea.setBounds(messageArea.getBounds().x, 
								  bottom, 
								  titleLabel.getBounds().width, 
								  messageArea.computeSize(SWT.DEFAULT,SWT.DEFAULT).y);
		}
		animator.setVisible(true);
		setMessageLayoutData(messageArea.getLayoutData());
	}

	/* (non-Javadoc) 
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#restoreTitle()
	 */
	public void restoreTitle() {
        titleLabel.setVisible(true);
		animator.setVisible(false);
		lastMessageText = null;
		lastMessageType = IMessageProvider.NONE;
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
