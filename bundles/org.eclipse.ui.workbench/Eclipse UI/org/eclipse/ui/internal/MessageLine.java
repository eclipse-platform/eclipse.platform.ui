/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A message line displaying a status.
 */
public class MessageLine extends CLabel {
	
	private static final RGB ERROR_BACKGROUND_RGB = new RGB(230, 226, 221);
	
	private Color fNormalMsgAreaBackground;
	private Color fErrorMsgAreaBackground;	

	/**
	 * Creates a new message line as a child of the given parent.
	 */
	public MessageLine(Composite parent) {
		this(parent, SWT.LEFT);
	}

	/**
	 * Creates a new message line as a child of the parent and with the given SWT stylebits.
	 */
	public MessageLine(Composite parent, int style) {
		super(parent, style);
		fNormalMsgAreaBackground= null;
		fErrorMsgAreaBackground= null;
	}

	
	private Image findImage(IStatus status) {
		if (status.isOK()) {
			return null;
		} else if (status.matches(IStatus.ERROR)) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		} else if (status.matches(IStatus.WARNING)) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		} else if (status.matches(IStatus.INFO)) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}
		return null;
	}

	/**
	 * Sets the message and image to the given status.
	 * <code>null</code> is a valid argument and will set the empty text and no image
	 */
	public void setErrorStatus(IStatus status) {
		if (status != null) {
			String message= status.getMessage();
			if (message != null && message.length() > 0) {
				setText(message);
				setImage(findImage(status));
				if (fErrorMsgAreaBackground == null) {
					fErrorMsgAreaBackground= new Color(getDisplay(), ERROR_BACKGROUND_RGB);
				}
				setBackground(fErrorMsgAreaBackground);
				return;
			}
		}		
		setText(""); //$NON-NLS-1$	
		setImage(null);
		setBackground(fNormalMsgAreaBackground);	
	}
	
	/*
	 * @see Widget#dispose()
	 */
	public void dispose() {
		if (fErrorMsgAreaBackground != null) {
			fErrorMsgAreaBackground.dispose();
			fErrorMsgAreaBackground= null;
		}
		super.dispose();
	}
}

