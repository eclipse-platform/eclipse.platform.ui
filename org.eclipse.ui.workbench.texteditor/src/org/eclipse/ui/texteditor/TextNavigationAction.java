/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/


package org.eclipse.ui.texteditor;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;


/**
 * An <code>IAction</code> wrapper for text widget navigational and selection actions.
 * @since 2.0
 */
public class TextNavigationAction implements IAction {
	
	/** The text widget */
	private StyledText fTextWidget;
	/** The styled text action id */
	private int fAction;
	/** The action's action id */
	private String fActionId;
	/** This action's action definition id */
	private String fActionDefinitionId;
	
	
	/**
	 * Creates a new <code>TextNavigationAction</code>.
	 * @param textWidget the text widget
	 * @param action the styled text widget action
	 */
	public TextNavigationAction(StyledText textWidget, int action) {
		fTextWidget= textWidget;
		fAction= action;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		fTextWidget.invokeAction(fAction);
	}

	/*
	 * @see IAction#runWithEvent(Event)
	 */
	public void runWithEvent(Event event) {
		run();
	}
	
	/*
	 * @see IAction#setActionDefinitionId(String)
	 */
	public void setActionDefinitionId(String id) {
		fActionDefinitionId= id;
	}
	
	/*
	 * @see IAction#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {
		return fActionDefinitionId;
	}
	
	/*
	 * @see IAction#setId(String)
	 */
	public void setId(String id) {
		fActionId= id;
	}	
	
	/*
	 * @see IAction#getId()
	 */
	public String getId() {
		return fActionId;
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
	 * @see IAction#setAccelerator(int)
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
