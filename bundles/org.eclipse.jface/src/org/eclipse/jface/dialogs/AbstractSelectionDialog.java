/*******************************************************************************
 * Copyright (c) 2015-2019 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 446616
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 460381
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
	// the final collection of selected elements
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
	 * @return the collection of selected elements, or
	 *         <code>Collections.emptyList()</code> if no result was set
	 */
	public Collection<T> getResult() {
		return result != null ? result : Collections.emptyList();
	}

	/**
	 * Returns an <code>java.util.Optional&lt;T&gt;</code> containing the first
	 * element from the collection of selections made by the user. Returns
	 * {@link Optional#empty()} if no element has been selected.
	 *
	 * @return an <code>java.util.Optional&lt;T&gt;</code> containing the first
	 *         result element if one exists. Otherwise {@link Optional#empty()} is
	 *         returned.
	 * @since 3.16
	 */
	public Optional<T> getFirstResult() {
		if (result != null) {
			return result.stream().findFirst();
		}
		return Optional.empty();
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
	 * Set the selections made by the user.
	 * <p>
	 * The result may be accessed using <code>getResult</code>.
	 * </p>
	 *
	 * @param newUserSelection collection of selected elements
	 */
	protected void setResult(Collection<T> newUserSelection) {
		result = newUserSelection;
	}

	/**
	 * Set the selections made by the user.
	 * <p>
	 * The result may be accessed using <code>getResult</code>.
	 * </p>
	 *
	 * @param newUserSelection - the new values
	 */
	protected void setResult(T... newUserSelection) {
		if (newUserSelection == null) {
			result = null;
		} else {
			result = Arrays.asList(newUserSelection);
		}
	}

	/**
	 * Set the selections obtained from a viewer.
	 *
	 * @param selection selection obtained from a viewer
	 * @param target    target type to check for <code>instanceof</code>
	 * @since 3.16
	 */
	protected void setResult(ISelection selection, Class<T> target) {
		List<T> selected = null;
		if (selection instanceof IStructuredSelection && target != null) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			selected = ((List<?>) structured.toList()).stream().filter(p -> target.isInstance(p))
					.map(p -> target.cast(p)).collect(Collectors.toList());
		}
		setResult(selected);
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
