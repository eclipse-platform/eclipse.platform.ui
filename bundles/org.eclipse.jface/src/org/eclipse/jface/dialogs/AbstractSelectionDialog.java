/*******************************************************************************
 * Copyright (c) 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 446616
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The abstract implementation of a selection dialog. It can be primed with
 * initial selections (<code>setInitialSelection</code>), and returns the final
 * selection (via <code>getResult</code>) after completion.
 * <p>
 * Clients may subclass this dialog to inherit its selection facilities.
 * </p>
 *
 * @param <T>
 *            which declares the type of the elements in the
 *            {@link AbstractSelectionDialog}.
 * @since 3.11
 *
 */
public abstract class AbstractSelectionDialog<T> extends TrayDialog {
	// the final collection of selected elements, or null if this dialog was
	// canceled
	private Collection<T> result;

	// a list of the initially-selected elements
	private List<T> initialSelection;

	// title of dialog
	private String title;

	// message to show user
	private String message = ""; //$NON-NLS-1$

	// dialog bounds strategy
	private int dialogBoundsStrategy = Dialog.DIALOG_PERSISTLOCATION | Dialog.DIALOG_PERSISTSIZE;

	// dialog settings for storing bounds
	private IDialogSettings dialogBoundsSettings = null;

	/**
	 * Creates a dialog instance.
	 *
	 * @param parentShell
	 *            the parent shell
	 */
	protected AbstractSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	/**
	 * Creates the message area for this dialog.
	 * <p>
	 * This method is provided to allow subclasses to decide where the message
	 * will appear on the screen.
	 * </p>
	 *
	 * @param composite
	 *            the parent composite
	 * @return the message label
	 */
	protected Label createMessageArea(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		if (message != null) {
			label.setText(message);
		}
		label.setFont(composite.getFont());
		return label;
	}

	/**
	 * Returns the collection of initial element selections.
	 *
	 * @return Collection
	 */
	protected List<T> getInitialSelection() {
		if (null == initialSelection) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(initialSelection);
	}

	/**
	 * Returns the message for this dialog.
	 *
	 * @return the message for this dialog
	 */
	protected String getMessage() {
		return message;
	}

	/**
	 * Returns the collection of selections made by the user.
	 *
	 * @return the collection of selected elements, or <code>null</code> if no
	 *         result was set
	 */
	public Collection<T> getResult() {
		return result;
	}

	/**
	 * Sets the initial selection in this selection dialog to the given
	 * elements.
	 *
	 * @param selectedElements
	 *            the elements to select
	 */
	public void setInitialSelection(T... selectedElements) {
		initialSelection = Arrays.asList(selectedElements);
	}

	/**
	 * Sets the initial selection in this selection dialog to the given
	 * elements.
	 *
	 * @param selectedElements
	 *            the List of elements to select
	 */
	public void setInitialSelection(Collection<T> selectedElements) {
		initialSelection = new ArrayList<>(selectedElements);
	}

	/**
	 * Sets the message for this dialog.
	 *
	 * @param message
	 *            the message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Set the selections made by the user, or <code>null</code> if the
	 * selection was canceled.
	 *
	 * @param newUserSelection
	 *            collection of selected elements, or <code>null</code> if
	 *            Cancel was pressed
	 */
	protected void setResult(Collection<T> newUserSelection) {
		if (newUserSelection == null) {
			result = Collections.emptyList();
		} else {
			result = newUserSelection;
		}
	}

	/**
	 * Set the selections made by the user, or <code>null</code> if the
	 * selection was canceled.
	 * <p>
	 * The selections may accessed using <code>getResult</code>.
	 * </p>
	 *
	 * @param newUserSelection
	 *            - the new values
	 */
	protected void setResult(T... newUserSelection) {
		if (newUserSelection == null) {
			result = Collections.emptyList();
		} else {
			result = Arrays.asList(newUserSelection);
		}
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title
	 *            the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Set the dialog settings that should be used to save the bounds of this
	 * dialog. This method is provided so that clients that directly use
	 * SelectionDialogs without subclassing them may specify how the bounds of
	 * the dialog are to be saved.
	 *
	 * @param settings
	 *            the {@link IDialogSettings} that should be used to store the
	 *            bounds of the dialog
	 *
	 * @param strategy
	 *            the integer constant specifying how the bounds are saved.
	 *            Specified using {@link Dialog#DIALOG_PERSISTLOCATION} and
	 *            {@link Dialog#DIALOG_PERSISTSIZE}.
	 *
	 * @since 3.2
	 *
	 * @see Dialog#getDialogBoundsStrategy()
	 * @see Dialog#getDialogBoundsSettings()
	 */
	public void setDialogBoundsSettings(IDialogSettings settings, int strategy) {
		dialogBoundsStrategy = strategy;
		dialogBoundsSettings = settings;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return dialogBoundsSettings;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return dialogBoundsStrategy;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
