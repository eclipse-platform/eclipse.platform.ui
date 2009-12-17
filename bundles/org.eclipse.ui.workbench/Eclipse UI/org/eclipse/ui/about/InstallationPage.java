/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.about;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract base implementation for an installation dialog page.
 * <p>
 * Clients should extend this class and include the name of the subclass in an
 * extension contributed to the workbench's installation pages extension point
 * (named <code>"org.eclipse.ui.installationPages"</code>). For example, the
 * plug-in's XML markup might contain:
 * 
 * <pre>
 * &LT;extension point="org.eclipse.ui.installationPages"&GT;
 *      &LT;page id="com.example.myplugin.installInfo"
 *         name="Example Details"
 *         class="com.example.myplugin.MyInstallationPage" /&GT;
 * &LT;/extension&GT;
 * </pre>
 * 
 * </p>
 * 
 * @since 3.5
 */
public abstract class InstallationPage extends DialogPage {

	private IInstallationPageContainer container;

	/**
	 * Sets or clears the message for this page.
	 * <p>
	 * This message has no effect when the receiver is used in an
	 * IInstallationPageContainer.
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 */
	public void setMessage(String newMessage) {
		super.setMessage(newMessage);
	}

	/**
	 * Sets the message for this page with an indication of what type of message
	 * it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or <code>ERROR</code>.
	 * </p>
	 * <p>
	 * This message has no effect when the receiver is used in an
	 * IInstallationPageContainer.
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 */
	public void setMessage(String newMessage, int newType) {
		super.setMessage(newMessage, newType);
	}

	/**
	 * Set the page container that is hosting this page. This method is
	 * typically called by the container itself so that the pages have access to
	 * the container when registering buttons using
	 * {@link IInstallationPageContainer#registerPageButton(InstallationPage, Button)}
	 * or performing other container-related tasks.
	 * 
	 * @param container
	 *            the container that is hosting the page.
	 */
	public void setPageContainer(IInstallationPageContainer container) {
		this.container = container;
	}

	/**
	 * Create the buttons that belong to this page using the specified parent.
	 * 
	 * @param parent
	 *            the parent to use for the buttons.
	 * 
	 * @see #createButton(Composite, int, String)
	 * @see #buttonPressed(int)
	 */
	public void createPageButtons(Composite parent) {
		// By default, there are no page-specific buttons
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * This method creates a standard push button, registers it for selection
	 * events, and registers it as a button belonging to this page. Subclasses
	 * should not make any assumptions about the visibility, layout, or
	 * presentation of this button inside the dialog.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code>
	 *            constants for standard dialog button ids)
	 * @param label
	 *            the label from the button
	 * @return the new button
	 * 
	 * @see #createPageButtons(Composite)
	 * @see #buttonPressed(int)
	 */
	protected Button createButton(Composite parent, int id, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		container.registerPageButton(this, button);
		return button;
	}

	/**
	 * Notifies that this page's button with the given id has been pressed.
	 * Subclasses should extend this method to handle the buttons created in
	 * {@link #createButton(Composite, int, String)}
	 * 
	 * @param buttonId
	 *            the id of the button that was pressed (see
	 *            <code>IDialogConstants.*_ID</code> constants)
	 */
	protected void buttonPressed(int buttonId) {
	}

	/**
	 * Get the page container that is hosting this page. This method is
	 * typically used when registering buttons using
	 * {@link IInstallationPageContainer#registerPageButton(InstallationPage, Button)}
	 * or performing other container-related tasks.
	 * 
	 * @return the container that is hosting the page.
	 */
	protected IInstallationPageContainer getPageContainer() {
		return container;
	}

}
