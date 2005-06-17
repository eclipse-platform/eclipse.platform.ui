/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * Action used by an editor action bar contributor to establish placeholders in
 * menus or action bars which can be retargeted to dynamically changing actions,
 * for example, those which come from the active editor. This action assumes that
 * the "wrapped" action sends out property change events in response to state
 * changes. It uses these change notifications to adapt its enabling state and
 * its visual presentation.
 */
public final class RetargetTextEditorAction extends ResourceAction {

	/** The target action. */
	private IAction fAction;
	/** The default label if there is no target action. */
	private String fDefaultText;
	/**
	 * The local help listener
	 * @since 2.1
	 */
	private HelpListener fLocalHelpListener;
	/** The listener to pick up changes of the target action. */
	private IPropertyChangeListener fListener= new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			update(event);
		}
	};

	/**
	 * Creates a new action. The action configures its initial visual
	 * representation from the given resource bundle. If this action's
	 * wrapped action is set to <code>null</code> it also uses the
	 * information in the resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param	style one of <code>IAction.AS_PUSH_BUTTON</code>, <code>IAction.AS_CHECK_BOX</code>,
 	 *			and <code>IAction.AS_RADIO_BUTTON</code>.
	 *
	 * @see ResourceAction#ResourceAction(ResourceBundle, String, int)
	 * @see IAction#AS_CHECK_BOX
	 * @see IAction#AS_DROP_DOWN_MENU
	 * @see IAction#AS_PUSH_BUTTON
	 * @see IAction#AS_RADIO_BUTTON
	 * @since 2.1
	 */
	public RetargetTextEditorAction(ResourceBundle bundle, String prefix, int style) {
		super(bundle, prefix, style);
		fDefaultText= getText();
		installHelpListener();
	}

	/**
	 * Creates a new action. The action configures its initial visual
	 * representation from the given resource bundle. If this action's
	 * wrapped action is set to <code>null</code> it also uses the
	 * information in the resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public RetargetTextEditorAction(ResourceBundle bundle, String prefix) {
		super(bundle, prefix);
		fDefaultText= getText();
		installHelpListener();
	}

	/**
	 * Creates a new action. The action configures its initial visual
	 * representation from the given resource bundle. If this action's
	 * wrapped action is set to <code>null</code> it also uses the
	 * information in the resource bundle. The action gets the given
	 * action id.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or <code>null</code> if none
	 * @param actionId the action id
	 * @param	style one of <code>IAction.AS_PUSH_BUTTON</code>, <code>IAction.AS_CHECK_BOX</code>,
 	 *			and <code>IAction.AS_RADIO_BUTTON</code>.
	 *
	 * @see ResourceAction#ResourceAction(ResourceBundle, String, int)
	 * @see IAction#AS_CHECK_BOX
	 * @see IAction#AS_DROP_DOWN_MENU
	 * @see IAction#AS_PUSH_BUTTON
	 * @see IAction#AS_RADIO_BUTTON
	 * @since 2.1
	 */
	public RetargetTextEditorAction(ResourceBundle bundle, String prefix, String actionId, int style) {
		super(bundle, prefix, style);
		fDefaultText= getText();
		setId(actionId);
		installHelpListener();
	}

	/**
	 * Creates a new action. The action configures its initial visual
	 * representation from the given resource bundle. If this action's
	 * wrapped action is set to <code>null</code> it also uses the
	 * information in the resource bundle. The action gets the given
	 * action id.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or <code>null</code> if none
	 * @param actionId the action id
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 * @since 2.0
	 */
	public RetargetTextEditorAction(ResourceBundle bundle, String prefix, String actionId) {
		super(bundle, prefix);
		fDefaultText= getText();
		setId(actionId);
		installHelpListener();
	}

	/**
	 * Updates to the changes of the underlying action.
	 *
	 * @param event the change event describing the state change
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
			if (getStyle() == AS_CHECK_BOX || getStyle() == AS_RADIO_BUTTON)
			   setChecked(false);
			setText(fDefaultText);
			setToolTipText(""); //$NON-NLS-1$

		} else {

			setEnabled(fAction.isEnabled());
			if (fAction.getStyle() == AS_CHECK_BOX || fAction.getStyle() ==  AS_RADIO_BUTTON)
			   super.setChecked(fAction.isChecked());
			setText(fAction.getText());
			setToolTipText(fAction.getToolTipText());
			fAction.addPropertyChangeListener(fListener);
		}
	}

	/**
	 * Installs the help listener.
	 *
	 * @since 2.1
	 */
	private void installHelpListener() {
		super.setHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				HelpListener listener= null;
				if (fAction != null) {
					// if we have a handler, see if it has a help listener
					listener= fAction.getHelpListener();
					if (listener == null)
						// use our own help listener
						listener= fLocalHelpListener;
				}
				if (listener != null)
					// pass on the event
					listener.helpRequested(e);
			}
		});
	}

	/**
	 * The <code>RetargetTextEditorAction</code> implementation of this method declared on
	 * <code>IAction</code> stores the help listener in a local field. The
	 * supplied listener is only used if there is no handler.
	 *
	 * @param listener the help listener
	 * @since 2.1
	 */
	public void setHelpListener(HelpListener listener) {
		fLocalHelpListener= listener;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		if (fAction != null)
			fAction.run();
	}
}
