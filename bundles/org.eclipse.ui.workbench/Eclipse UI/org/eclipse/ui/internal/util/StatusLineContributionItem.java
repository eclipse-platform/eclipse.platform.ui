/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.util;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @issue needs Javadoc
 */
public class StatusLineContributionItem extends ContributionItem {

    public final static int DEFAULT_CHAR_WIDTH = 40;

    private int charWidth;

    private CLabel label;

    /**
     * The composite into which this contribution item has been placed. This
     * will be <code>null</code> if this instance has not yet been
     * initialized.
     */
    private Composite statusLine = null;

    private String text = Util.ZERO_LENGTH_STRING;

    private int widthHint = -1;

    private int heightHint = -1;

    public StatusLineContributionItem(String id) {
        this(id, DEFAULT_CHAR_WIDTH);
    }

    public StatusLineContributionItem(String id, int charWidth) {
        super(id);
        this.charWidth = charWidth;
        setVisible(false); // no text to start with
    }

    public void fill(Composite parent) {
        statusLine = parent;

        Label sep = new Label(parent, SWT.SEPARATOR);
        label = new CLabel(statusLine, SWT.SHADOW_NONE);

        if (widthHint < 0) {
            GC gc = new GC(statusLine);
            gc.setFont(statusLine.getFont());
            FontMetrics fm = gc.getFontMetrics();
            widthHint = fm.getAverageCharWidth() * charWidth;
            heightHint = fm.getHeight();
            gc.dispose();
        }

        StatusLineLayoutData data = new StatusLineLayoutData();
        data.widthHint = widthHint;
        label.setLayoutData(data);
        label.setText(text);

        data = new StatusLineLayoutData();
        data.heightHint = heightHint;
        sep.setLayoutData(data);

    }

    /**
     * An accessor for the current location of this status line contribution
     * item -- relative to the display.
     * 
     * @return The current location of this status line; <code>null</code> if
     *         not yet initialized.
     */
    public Point getDisplayLocation() {
        if ((label != null) && (statusLine != null)) {
            return statusLine.toDisplay(label.getLocation());
        }

        return null;
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
}