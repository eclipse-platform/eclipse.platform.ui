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

package org.eclipse.ui.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

public class StatusLineContributionItem extends ContributionItem {

	public final static int DEFAULT_CHAR_WIDTH = 40; 
	
	private int charWidth;
	private Collection labels = new ArrayList();
	private String text = Util.ZERO_LENGTH_STRING;
	private int widthHint = -1;

	public StatusLineContributionItem(String id) {
		this(id, DEFAULT_CHAR_WIDTH);
	}

	public StatusLineContributionItem(String id, int charWidth) {
		super(id);
		this.charWidth = charWidth;
	}

	public void fill(Composite parent) {	
		final CLabel label = new CLabel(parent, SWT.SHADOW_IN);
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
		synchronized (labels) {
			labels.add(label);
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (text == null)
			throw new NullPointerException();

		this.text = text;
		
		synchronized (labels) {
			final Iterator labelItr = labels.iterator();
			while (labelItr.hasNext()) {
				final CLabel label = (CLabel) labelItr.next();
				if ((label == null) || (label.isDisposed())) {
					labelItr.remove();
				} else {
					label.setText(this.text);
				}
			}
		}
		
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
}
