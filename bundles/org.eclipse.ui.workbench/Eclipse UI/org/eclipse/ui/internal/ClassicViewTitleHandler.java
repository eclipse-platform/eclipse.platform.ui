/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IViewReference;

/**
 * The ClassicViewTitleHandler is a class that handles all of the view title
 * functionality in the current way.
 */
public class ClassicViewTitleHandler extends ViewTitleHandler {

	CLabel titleLabel;

	/**
	 * Create a new instance of the receiver with the supplied pane.
	 * 
	 * @param parentPane
	 */
	public ClassicViewTitleHandler(ViewPane parentPane) {
		super(parentPane);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#overImage(int)
	 */
	public boolean overImage(int x) {
		if (titleLabel.getImage() == null) {
			return false;
		} else {
			return x < titleLabel.getImage().getBounds().width;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#createLabel(org.eclipse.swt.widgets.Composite)
	 */
	public CLabel createLabel(Composite control) {
		//		Title.
		titleLabel = new CLabel(control, SWT.SHADOW_NONE);
		titleLabel.setAlignment(SWT.LEFT);
		titleLabel.setBackground(null, null);
		titleLabel.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if ((e.button == 1) && overImage(e.x))
					pane.showPaneMenu();
			}
			public void mouseDoubleClick(MouseEvent event) {
				pane.doZoom();
			}
		});
		// Listen for popup menu mouse event
		titleLabel.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MenuDetect) {
					pane.showPaneMenu(titleLabel, new Point(event.x, event.y));
				}
			}
		});
		return titleLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#getDragHandle()
	 */
	public Control getDragHandle() {
		return titleLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#hasPaneMenu()
	 */
	public boolean hasPaneMenu() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#getLabel()
	 */
	public CLabel getLabel() {
		return titleLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#getText()
	 */
	public String getText() {

		if ((titleLabel != null) && !titleLabel.isDisposed())
			return titleLabel.getText();
		else
			return "disposed"; //$NON-NLS-1$

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#hasLabel()
	 */
	public boolean hasLabel() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ViewTitleHandler#updateLabel(org.eclipse.ui.IViewReference)
	 */
	public void updateLabel(IViewReference ref) {
		boolean changed = false;

		// only update if text or image has changed
		String text = ref.getTitle();
		if (text == null)
			text = ""; //$NON-NLS-1$
		if (!text.equals(titleLabel.getText())) {
			titleLabel.setText(text);
			changed = true;
		}
		Image image = ref.getTitleImage();
		if (image != titleLabel.getImage()) {
			titleLabel.setImage(image);
			changed = true;
		}
		// only relayout if text or image has changed
		if (changed) {
			((Composite) pane.getControl()).layout();
		}

		String tooltip = ref.getTitleToolTip();
		if (!(tooltip == null
			? titleLabel.getToolTipText() == null
			: tooltip.equals(titleLabel.getToolTipText()))) {
			titleLabel.setToolTipText(ref.getTitleToolTip());
			changed = true;
		}

		if (changed) {
			// XXX: Workaround for 1GCGA89: SWT:ALL - CLabel tool tip does
			// not always update properly
			titleLabel.update();

			// notify the page that this view's title has changed
			// in case it needs to update its fast view button
			pane.page.updateTitle(ref);
		}

	}

}
