/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class StatusLineCLabelContribution extends ContributionItem {
	
	public final static int DEFAULT_CHAR_WIDTH = 40; 
	
	private int charWidth;
	private CLabel label;
	private Image image;
	private String text = ""; //$NON-NLS-1$
	private int widthHint = -1;

	private Listener listener;
	
	public StatusLineCLabelContribution(String id) {
		this(id, DEFAULT_CHAR_WIDTH, null);
	}
	
	public StatusLineCLabelContribution(String id, int charWidth, Listener listener) {
		super(id);
		this.listener = listener;
		this.charWidth = charWidth;
		setVisible(false); // no text to start with
	}
	
	public void fill(Composite parent) {	
		label = new CLabel(parent, SWT.SHADOW_IN);
		StatusLineLayoutData statusLineLayoutData = new StatusLineLayoutData();
		
		if (widthHint < 0) {
			GC gc = new GC(parent);
			gc.setFont(parent.getFont());
			widthHint = gc.getFontMetrics().getAverageCharWidth() * charWidth;
			gc.dispose();
		}
		
		statusLineLayoutData.widthHint = widthHint;
		label.setLayoutData(statusLineLayoutData);
		label.setText(text);
		label.setImage(image);
		if(listener != null) {
			label.addListener(SWT.MouseDown, listener);
		}
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		if (text == null)
			throw new NullPointerException();
		
		this.text = text;
		
		if (label != null && !label.isDisposed())
			label.setText(this.text);
		
		if (this.text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				IContributionManager contributionManager = getParent();
				
				if (contributionManager != null)
					contributionManager.update(true);
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				IContributionManager contributionManager = getParent();
				
				if (contributionManager != null)
					contributionManager.update(true);
			}
		}
	}
	
	public void setImage(Image image) {
		if (image == null)
			throw new NullPointerException();
		
		this.image = image;
		
		if (label != null && !label.isDisposed())
			label.setImage(this.image);
		
		if (!isVisible()) {
			setVisible(true);
			IContributionManager contributionManager = getParent();
			
			if (contributionManager != null)
				contributionManager.update(true);
		}
	}
}
