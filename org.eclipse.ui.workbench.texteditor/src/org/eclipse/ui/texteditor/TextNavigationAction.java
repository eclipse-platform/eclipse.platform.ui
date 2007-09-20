/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * @see IAction#run()
	 */
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

	/*
	 * @see IAction#runWithEvent(Event)
	 */
	public void runWithEvent(Event event) {
		run();
	}


// ----------------------------------------------------------------------------------------------------------------------------------
// All the subsequent methods are just empty method bodies.

	/*
	 * @see IAction#addPropertyChangeListener(IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	}

	/*
	 * @see IAction#getAccelerator()
	 */
	public int getAccelerator() {
		return 0;
	}

	/*
	 * @see IAction#getDescription()
	 */
	public String getDescription() {
		return null;
	}

	/*
	 * @see IAction#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return null;
	}

	/*
	 * @see IAction#getHelpListener()
	 */
	public HelpListener getHelpListener() {
		return null;
	}

	/*
	 * @see IAction#getHoverImageDescriptor()
	 */
	public ImageDescriptor getHoverImageDescriptor() {
		return null;
	}

	/*
	 * @see IAction#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/*
	 * @see IAction#getMenuCreator()
	 */
	public IMenuCreator getMenuCreator() {
		return null;
	}

	/*
	 * @see IAction#getStyle()
	 */
	public int getStyle() {
		return 0;
	}

	/*
	 * @see IAction#getText()
	 */
	public String getText() {
		return null;
	}

	/*
	 * @see IAction#getToolTipText()
	 */
	public String getToolTipText() {
		return null;
	}

	/*
	 * @see IAction#isChecked()
	 */
	public boolean isChecked() {
		return false;
	}

	/*
	 * @see IAction#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

	/*
	 * @see IAction#removePropertyChangeListener(IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
	}

	/*
	 * @see org.eclipse.jface.action.IAction#setAccelerator(int)
	 */
	public void setAccelerator(int keycode) {
	}

	/*
	 * @see IAction#setChecked(boolean)
	 */
	public void setChecked(boolean checked) {
	}

	/*
	 * @see IAction#setDescription(String)
	 */
	public void setDescription(String text) {
	}

	/*
	 * @see IAction#setDisabledImageDescriptor(ImageDescriptor)
	 */
	public void setDisabledImageDescriptor(ImageDescriptor newImage) {
	}

	/*
	 * @see IAction#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
	}

	/*
	 * @see IAction#setHelpListener(HelpListener)
	 */
	public void setHelpListener(HelpListener listener) {
	}

	/*
	 * @see IAction#setHoverImageDescriptor(ImageDescriptor)
	 */
	public void setHoverImageDescriptor(ImageDescriptor newImage) {
	}

	/*
	 * @see IAction#setImageDescriptor(ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor newImage) {
	}

	/*
	 * @see IAction#setMenuCreator(IMenuCreator)
	 */
	public void setMenuCreator(IMenuCreator creator) {
	}

	/*
	 * @see IAction#setText(String)
	 */
	public void setText(String text) {
	}

	/*
	 * @see IAction#setToolTipText(String)
	 */
	public void setToolTipText(String text) {
	}
}
