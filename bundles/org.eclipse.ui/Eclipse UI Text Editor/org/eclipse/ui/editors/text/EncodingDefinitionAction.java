/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The EncodingDefinitionAction is an Action created by the 
 * entries in an EncodingDefinition.
 */
class EncodingDefinitionAction extends Action {

	/** The target action */
	private IAction fAction;
	/** The default label if there is no target action */
	private EncodingDefinition fDefinition;
	/** The listener to pick up changes of the target action */
	private IPropertyChangeListener fListener= new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			update(event);
		}
	};

	/**
	 * Creates a new action using the values in newDefinition.
	 * @param newDefinition - the definition used to create the action
	 * 
	 */
	EncodingDefinitionAction(EncodingDefinition newDefinition) {
		fDefinition= newDefinition;
		setId(fDefinition.getId());
		setText(newDefinition.getLabel());

	}

	/**
	 * Updates to the changes of the underlying action.
	 *
	 * @param PropertyChangeEvent the change event describing the state change
	 */
	private void update(PropertyChangeEvent event) {
		if (ENABLED.equals(event.getProperty())) {
			Boolean bool= (Boolean) event.getNewValue();
			setEnabled(bool.booleanValue());
		} else if (TEXT.equals(event.getProperty()))
			setText((String) event.getNewValue());
		else if (TOOL_TIP_TEXT.equals(event.getProperty()))
			setToolTipText((String) event.getNewValue());
		else if (CHECKED.equals(event.getProperty())) {
			Boolean bool= (Boolean) event.getNewValue();
			setChecked(bool.booleanValue());
		}
	}

	/**
	 * Sets the underlying action.
	 *
	 * @param action the underlying action
	 */
	public void setAction(IAction action) {

		if (fAction != null) {
			fAction.removePropertyChangeListener(fListener);
			fAction= null;
		}

		fAction= action;

		if (fAction == null) {

			setEnabled(false);
			setText(fDefinition.getLabel());
			setToolTipText(""); //$NON-NLS-1$

		} else {

			setEnabled(fAction.isEnabled());
			setText(fAction.getText());
			setToolTipText(fAction.getToolTipText());
			fAction.addPropertyChangeListener(fListener);
		}
	}

	/*
	 * @see Action#run()
	 */
	public void run() {
		if (fAction != null)
			fAction.run();
	}

	/*
	 * @see IAction#getActionDefinitionId()
	 * @since 2.0
	 */
	public String getActionDefinitionId() {
		if (fAction != null)
			return fAction.getActionDefinitionId();
		return null;
	}

	/**
	 * Sets the action's help context id.
	 * 
	 * @param String the help context id
	 */
	public final void setHelpContextId(String contextId) {
		WorkbenchHelp.setHelp(this, contextId);
	}

}
