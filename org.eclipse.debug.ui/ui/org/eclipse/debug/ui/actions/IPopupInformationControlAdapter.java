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
package org.eclipse.debug.ui.actions;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for a control presenting information in a <code>PopupInformationControl</code>. 
 * The information is given in textual form. It can  be either the content itself 
 * or a description of the content.
 * <p>
 * Clients using a <code>PopupInformationControl</code> must supply an
 * implementation of this interface.
 * </p>
 * <p>
 * This interface is yet experimental.
 * </p>
 * @see org.eclipse.debug.ui.actions.PopupInformationControl
 * @since 3.0
 */
public interface IPopupInformationControlAdapter {
	
	/**
	 * Returns whether this Control Adapter has the focus.
	 * 
	 * @return <code>true</code> when the IPopupInformationControlAdapter 
	 * has the focus otherwise <code>false</code>
	 */
	public boolean isFocusControl();

	/**
	 * Returns whether there is anything to display. Popup will not be
	 * displayed if false is returned.
	 * @return <code>true</code> if there is contents to be displayed.
	 */
	public boolean hasContents();

	/**
	 * Sets the information to be presented by this information control.
	 * 
	 * @param information the information to be presented, or a description
	 * of the information, depending on imlementation.
	 */
	public void setInformation(String information);
	
	/**
	 * Creates and returns a composite to display the information prestented 
	 * by the InformationControl 
	 * @param parent The parent Shell
	 * @return The graphical presentation of the controls information
	 */
	public Composite createInformationComposite(Shell parent);
	
	/**
	 * Returns the dialog settings to be used for persisting size information 
	 * about the popup. If this adapter does not want its size to be persisted,
	 * it should return <code>null</code>.
	 *  
	 * @return The dialog settings to use for persistance,
	 *  or <code>null</code> if persistance is not desired
	 */
	public IDialogSettings getDialogSettings();
	
	/**
	 * Returns the text to be used on the popup's label.
	 * @return the text to be used on the popup's label.
	 */
	public String getLabel();
	
	/**
	 * Returns the ActionDefinitionId of the command that created the pop-up. Used to 
	 * override the KeyBinding for disposal of the popup.
	 * @return The actionDefinitionId of the command that created the pop-up.
	 */
	public String getActionDefinitionId();
}
