/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;


/**
 * An <code>Action</code> wrapper for text widget navigation and selection actions.
 * @since 2.0
 */
public class TextNavigationAction extends Action {

	/** The text widget */
	private StyledText fTextWidget;
	/** The styled text action id */
	private int fAction;


	/**
	 * Creates a new <code>TextNavigationAction</code>.
	 * @param textWidget the text widget
	 * @param action the styled text widget action
	 */
	public TextNavigationAction(StyledText textWidget, int action) {
		fTextWidget= textWidget;
		fAction= action;
	}

	/**
	 * Returns the text widget this actions is bound to.
	 *
	 * @return returns the text widget this actions is bound to
	 */
	protected StyledText getTextWidget() {
		return fTextWidget;
	}

	@Override
	public void run() {
		Point selection= fTextWidget.getSelection();
		fTextWidget.invokeAction(fAction);
		fireSelectionChanged(selection);
	}

	private void doFireSelectionChanged(Point selection) {
		Event event= new Event();
		event.x= selection.x;
		event.y= selection.y;
		fTextWidget.notifyListeners(SWT.Selection, event);
	}

	/**
	 * Sends a selection event with the current selection to all
	 * selection listeners of the action's text widget
	 *
	 * @since 3.0
	 */
	protected void fireSelectionChanged() {
		fireSelectionChanged(null);
	}

	/**
	 * Fires a selection event to all selection listener of the action's
	 * text widget if the current selection differs from the given selection.
	 *
	 * @param oldSelection the old selection
	 * @since 3.0
	 */
	protected void fireSelectionChanged(Point oldSelection) {
		Point selection= fTextWidget.getSelection();
		if (oldSelection == null || !selection.equals(oldSelection))
			doFireSelectionChanged(selection);
	}

	@Override
	public void runWithEvent(Event event) {
		run();
	}


// ----------------------------------------------------------------------------------------------------------------------------------
// All the subsequent methods are just empty method bodies.

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	}

	@Override
	public int getAccelerator() {
		return 0;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return null;
	}

	@Override
	public HelpListener getHelpListener() {
		return null;
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public IMenuCreator getMenuCreator() {
		return null;
	}

	@Override
	public int getStyle() {
		return 0;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
	}

	@Override
	public void setAccelerator(int keycode) {
	}

	@Override
	public void setChecked(boolean checked) {
	}

	@Override
	public void setDescription(String text) {
	}

	@Override
	public void setDisabledImageDescriptor(ImageDescriptor newImage) {
	}

	@Override
	public void setEnabled(boolean enabled) {
	}

	@Override
	public void setHelpListener(HelpListener listener) {
	}

	@Override
	public void setHoverImageDescriptor(ImageDescriptor newImage) {
	}

	@Override
	public void setImageDescriptor(ImageDescriptor newImage) {
	}

	@Override
	public void setMenuCreator(IMenuCreator creator) {
	}

	@Override
	public void setText(String text) {
	}

	@Override
	public void setToolTipText(String text) {
	}
}
