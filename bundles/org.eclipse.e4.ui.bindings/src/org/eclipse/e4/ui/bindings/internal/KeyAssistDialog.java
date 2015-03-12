/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.bindings.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <p>
 * A dialog displaying a list of key bindings. The dialog will execute a command if it is selected.
 * </p>
 * <p>
 * The methods on this class are not thread-safe and must be run from the UI thread.
 * </p>
 *
 * @since 3.1
 */
public class KeyAssistDialog extends PopupDialog {

	/**
	 * The data key for the binding stored on an SWT widget. The key is a fully-qualified name, but
	 * in reverse order. This is so that the equals method will detect misses faster.
	 */
	private static final String BINDING_KEY = "Binding.bindings.jface.eclipse.org"; //$NON-NLS-1$

	/**
	 * The value of <code>previousWidth</code> to set if there is no remembered width.
	 */
	private static final int NO_REMEMBERED_WIDTH = -1;

	/**
	 * The ordered list of command identifiers corresponding to the table.
	 */
	private List<Binding> bindings = new ArrayList<Binding>();

	/**
	 * The table containing of the possible completions. This value is <code>null</code> until the
	 * dialog is created.
	 */
	private Table completionsTable = null;

	/**
	 * The width of the shell when it was previously open. This is only remembered until
	 * <code>clearRememberedState()</code> is called.
	 */
	private int previousWidth = NO_REMEMBERED_WIDTH;

	/**
	 * The key binding listener for the associated workbench.
	 */
	private KeyBindingDispatcher workbenchKeyboard;

	/**
	 * A sorted map of conflicts or partial matches to be used when the dialog pops up.
	 *
	 * @since 3.3
	 */
	private Collection<Binding> matches;

	private IEclipseContext context;

	/**
	 * Constructs a new instance of <code>KeyAssistDialog</code>. When the dialog is first
	 * constructed, it contains no widgets. The dialog is first created with no parent. If a parent
	 * is required, call <code>setParentShell()</code>. Also, between uses, it might be necessary to
	 * call <code>setParentShell()</code> as well.
	 *
	 * @param context
	 *            The context in which this dialog is created; must not be <code>null</code>.
	 * @param associatedKeyboard
	 *            The key binding listener for the workbench; must not be <code>null</code>.
	 */
	public KeyAssistDialog(IEclipseContext context, KeyBindingDispatcher associatedKeyboard) {
		super((Shell) null, PopupDialog.INFOPOPUP_SHELLSTYLE, true, false, false, false, null, null);
		//super(null, PopupDialog.INFOPOPUP_SHELLSTYLE, true, false, false, false, false, DIALOG_TITLE, getKeySequenceString()); //$NON-NLS-1$

		this.context = context;
		this.workbenchKeyboard = associatedKeyboard;
	}

	/**
	 * Clears out the remembered state of the key assist dialog. This includes its width, as well as
	 * the selected binding.
	 */
	public void clearRememberedState() {
		previousWidth = NO_REMEMBERED_WIDTH;
	}

	/**
	 * Closes this shell, but first remembers some state of the dialog. This way it will have a
	 * response if asked to open the dialog again or if asked to open the keys preference page. This
	 * does not remember the internal state.
	 *
	 * @return Whether the shell was already closed.
	 */
	@Override
	public boolean close() {
		return close(false);
	}

	/**
	 * Closes this shell, but first remembers some state of the dialog. This way it will have a
	 * response if asked to open the dialog again or if asked to open the keys preference page.
	 *
	 * @param rememberState
	 *            Whether the internal state should be remembered.
	 * @return Whether the shell was already closed.
	 */
	public boolean close(boolean rememberState) {
		return close(rememberState, true);
	}

	/**
	 * Closes this shell, but first remembers some state of the dialog. This way it will have a
	 * response if asked to open the dialog again or if asked to open the keys preference page.
	 *
	 * @param rememberState
	 *            Whether the internal state should be remembered.
	 * @param resetState
	 *            Whether the state should be reset.
	 * @return Whether the shell was already closed.
	 */
	private boolean close(boolean rememberState, boolean resetState) {
		Shell shell = getShell();
		if (rememberState) {

			// Remember the previous width.
			int widthToRemember;
			if ((shell != null) && (!shell.isDisposed())) {
				widthToRemember = getShell().getSize().x;
			} else {
				widthToRemember = NO_REMEMBERED_WIDTH;
			}

			this.previousWidth = widthToRemember;
			completionsTable = null;
		}
		matches = null;
		boolean popupClosed = super.close();
		if (resetState) {
			workbenchKeyboard.resetState();
		}
		return popupClosed;
	}

	/**
	 * Sets the position for the dialog based on the position of the workbench window. The dialog is
	 * flush with the bottom right corner of the workbench window. However, the dialog will not
	 * appear outside of the display's client area.
	 *
	 * @param size
	 *            The final size of the dialog; must not be <code>null</code>.
	 */
	private void configureLocation(Point size) {
		Shell shell = getShell();

		Shell workbenchWindowShell = (Shell) shell.getParent();
		int xCoord;
		int yCoord;
		if (workbenchWindowShell != null) {
			/*
			 * Position the shell at the bottom right corner of the workbench window
			 */
			Rectangle workbenchWindowBounds = workbenchWindowShell.getBounds();
			xCoord = workbenchWindowBounds.x + workbenchWindowBounds.width - size.x - 10;
			yCoord = workbenchWindowBounds.y + workbenchWindowBounds.height - size.y - 10;

		} else {
			xCoord = 0;
			yCoord = 0;

		}
		Rectangle bounds = new Rectangle(xCoord, yCoord, size.x, size.y);
		shell.setBounds(getConstrainedShellBounds(bounds));
	}

	/**
	 * Sets the size for the dialog based on its previous size. The width of the dialog is its
	 * previous width, if it exists. Otherwise, it is simply the packed width of the dialog. The
	 * maximum width is 40% of the workbench window's width. The dialog's height is the packed
	 * height of the dialog to a maximum of half the height of the workbench window.
	 *
	 * @return The size of the dialog
	 */
	private Point configureSize() {
		Shell shell = getShell();

		// Get the packed size of the shell.
		shell.pack();
		Point size = shell.getSize();

		// Use the previous width if appropriate.
		if ((previousWidth != NO_REMEMBERED_WIDTH) && (previousWidth > size.x)) {
			size.x = previousWidth;
		}

		// Enforce maximum sizing.
		Shell workbenchWindowShell = (Shell) shell.getParent();
		if (workbenchWindowShell != null) {
			Point workbenchWindowSize = workbenchWindowShell.getSize();
			int maxWidth = workbenchWindowSize.x * 2 / 5;
			int maxHeight = workbenchWindowSize.y / 2;
			if (size.x > maxWidth) {
				size.x = maxWidth;
			}
			if (size.y > maxHeight) {
				size.y = maxHeight;
			}
		}

		// Set the size for the shell.
		shell.setSize(size);
		return size;
	}

	/**
	 * Creates the content area for the key assistant. This creates a table and places it inside the
	 * composite. The composite will contain a list of all the key bindings.
	 *
	 * @param parent
	 *            The parent composite to contain the dialog area; must not be <code>null</code>.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// Create a composite for the dialog area.
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout(compositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(parent.getBackground());

		// Layout the partial matches.
		Collection<Binding> bindings;
		// if we're going to display a list of conflicts or partial matches...
		if (matches != null) {
			bindings = matches;
		}
		// else just grab the entire list of active bindings
		else {
			bindings = getActiveBindings();
		}

		if (bindings == null || bindings.isEmpty()) {
			createEmptyDialogArea(composite);
		} else {
			createTableDialogArea(composite, bindings);
		}
		return composite;
	}

	/**
	 * Creates an empty dialog area with a simple message saying there were no matches. This is used
	 * if no partial matches could be found. This should not really ever happen, but might be
	 * possible if the commands are changing while waiting for this dialog to open.
	 *
	 * @param parent
	 *            The parent composite for the dialog area; must not be <code>null</code>.
	 */
	private void createEmptyDialogArea(Composite parent) {
		Label noMatchesLabel = new Label(parent, SWT.NULL);
		noMatchesLabel.setText("No matches"); //$NON-NLS-1$
		noMatchesLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		noMatchesLabel.setBackground(parent.getBackground());
	}

	/**
	 * Creates a dialog area with a table of the partial matches for the current key binding state.
	 * The table will be either the minimum width, or <code>previousWidth</code> if it is not
	 * <code>NO_REMEMBERED_WIDTH</code>.
	 *
	 * @param parent
	 *            The parent composite for the dialog area; must not be <code>null</code>.
	 * @param partialMatches
	 *            The lexicographically sorted map of partial matches for the current state; must
	 *            not be <code>null</code> or empty.
	 */
	private void createTableDialogArea(Composite parent, Collection<Binding> partialMatches) {
		// Layout the table.
		completionsTable = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		completionsTable.setLayoutData(gridData);
		completionsTable.setBackground(parent.getBackground());
		completionsTable.setLinesVisible(true);

		// Initialize the columns and rows.
		bindings.clear();
		TableColumn columnCommandName = new TableColumn(completionsTable, SWT.LEFT, 0);
		TableColumn columnKeySequence = new TableColumn(completionsTable, SWT.LEFT, 1);
		Iterator<Binding> itemsItr = partialMatches.iterator();
		while (itemsItr.hasNext()) {
			Binding binding = itemsItr.next();
			String sequence = binding.getTriggerSequence().format();
			ParameterizedCommand command = binding.getParameterizedCommand();
			try {
				String[] text = { command.getName(), sequence };
				TableItem item = new TableItem(completionsTable, SWT.NULL);
				item.setText(text);
				item.setData(BINDING_KEY, binding);
				bindings.add(binding);
			} catch (NotDefinedException e) {
				// Not much to do, but this shouldn't really happen.
			}
		}

		Dialog.applyDialogFont(parent);
		columnKeySequence.pack();
		if (previousWidth != NO_REMEMBERED_WIDTH) {
			columnKeySequence.setWidth(previousWidth);
		}
		columnCommandName.pack();
		if (completionsTable.getItems().length > 0) {
			completionsTable.setSelection(0);
		}

		/*
		 * If you double-click on the table, it should execute the selected command.
		 */
		completionsTable.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				executeKeyBinding(event);
			}
		});
	}

	/**
	 * Handles the default selection event on the table of possible completions. This attempts to
	 * execute the given command.
	 */
	private void executeKeyBinding(Event trigger) {
		int selectionIndex = completionsTable.getSelectionIndex();
		// Try to execute the corresponding command.
		if (selectionIndex >= 0) {
			close();
			Binding binding = bindings.get(selectionIndex);
			try {
				// workbenchKeyboard.updateShellKludge(null);
				workbenchKeyboard.executeCommand(binding.getParameterizedCommand(), trigger);
			} catch (CommandException e) {
				// WorkbenchPlugin.log(binding.getParameterizedCommand().toString(), e);
				// TODO we probably need to log something here.
				System.err.println(binding.getParameterizedCommand().toString() + " : " + e); //$NON-NLS-1$
			}
		}
	}

	private Collection<Binding> getActiveBindings() {

		EBindingService bindingService = context.getActiveLeaf().get(EBindingService.class);

		Iterator<Binding> iter, matchesIter;
		Binding binding, bindingToAdd;
		Collection<Binding> matchesForCommand;
		Collection<Binding> activeBindings = bindingService.getActiveBindings();
		Collection<Binding> conflictBindings = bindingService.getAllConflicts();
		Collection<Binding> sortedMatches = new TreeSet<Binding>(new Comparator<Binding>() {
			@Override
			public int compare(Binding binding1, Binding binding2) {
				ParameterizedCommand cmdA = binding1.getParameterizedCommand();
				ParameterizedCommand cmdB = binding2.getParameterizedCommand();
				int result = 0;
				try {
					result = cmdA.getName().compareTo(cmdB.getName());
				} catch (NotDefinedException e) {
					// whaaa?
				}
				return result;
			}
		});

		// if the active scheme is not the default scheme then we should clean up the active
		// bindings list... if we find multiple bindings for the same command and they are for
		// different schemes, then we need to handle which one should be displayed in the dialog
		if (activeBindings != null) {
			iter = activeBindings.iterator();
			while (iter.hasNext()) {
				binding = iter.next();
				matchesForCommand = bindingService
						.getBindingsFor(binding.getParameterizedCommand());
				// if there is more than one match, then look for a binding that does not belong to
				// the default scheme. If they all belong to the default scheme or they all do NOT
				// belong to the default scheme, then arbitrarily choose one
				if (matchesForCommand != null && matchesForCommand.size() > 1) {
					bindingToAdd = null;

					matchesIter = matchesForCommand.iterator();
					while (matchesIter.hasNext()) {
						bindingToAdd = matchesIter.next();
						if (!bindingToAdd.getSchemeId().equals(EBindingService.DEFAULT_SCHEME_ID)) {
							sortedMatches.add(bindingToAdd);
							break;
						}
					}
					// if they're all the same, arbitrarily choose one
					if (bindingToAdd != null) {
						sortedMatches.add(bindingToAdd);
					}
				}
				// if there is only one match, then just add it
				else if (matchesForCommand != null && matchesForCommand.size() == 1) {
					sortedMatches.addAll(matchesForCommand);
				}
			}
		}
		if (conflictBindings != null) {
			iter = conflictBindings.iterator();
			while (iter.hasNext()) {
				binding = iter.next();
				sortedMatches.add(binding);
			}
		}
		return sortedMatches;
	}

	/**
	 * Opens this dialog. This method can be called multiple times on the same dialog. This only
	 * opens the dialog if there is no remembered state; if there is remembered state, then it tries
	 * to open the preference page instead.
	 *
	 * @return The return code from this dialog.
	 */
	@Override
	public int open() {
		// If the dialog is already open, dispose the shell and recreate it.
		Shell shell = getShell();
		if (shell != null) {
			close(false, false);
			return Window.OK;
		}
		create();
		// Bug 412001. Stop ShellActivationListener from creating a context for this.
		getShell().setData("org.eclipse.e4.ui.ignoreDialog", Boolean.TRUE); //$NON-NLS-1$

		// Configure the size and location.
		Point size = configureSize();
		configureLocation(size);

		// Call the super method.
		return super.open();
	}

	/**
	 * Opens this dialog with the list of bindings for the user to select from.
	 *
	 * @return The return code from this dialog.
	 * @since 3.3
	 */
	public int open(Collection<Binding> bindings) {
		matches = new TreeSet<Binding>(new Comparator<Binding>() {
			@Override
			public int compare(Binding a, Binding b) {
				Binding bindingA = a;
				Binding bindingB = b;
				ParameterizedCommand commandA = bindingA.getParameterizedCommand();
				ParameterizedCommand commandB = bindingB.getParameterizedCommand();
				try {
					return commandA.getName().compareTo(commandB.getName());
				} catch (NotDefinedException e) {
					// should not happen
					return 0;
				}
			}
		});
		matches.addAll(bindings);

		// If the dialog is already open, dispose the shell and recreate it.
		Shell shell = getShell();
		if (shell != null) {
			close(false, false);
			return Window.OK;
		}
		create();
		// Bug 369860. Stop ShellActivationListener from creating a context for this.
		getShell().setData("org.eclipse.e4.ui.ignoreDialog", Boolean.TRUE); //$NON-NLS-1$
		// Configure the size and location.
		Point size = configureSize();
		configureLocation(size);

		// Call the super method.
		return super.open();
	}

	/**
	 * Exposing this within the keys package.
	 *
	 * @param newParentShell
	 *            The new parent shell; this value may be <code>null</code> if there is to be no
	 *            parent.
	 */
	@Override
	public void setParentShell(Shell newParentShell) {
		super.setParentShell(newParentShell);
	}

	/**
	 * Returns the currently selected binding from the table if the table is not disposed
	 *
	 * @return the currently selected binding or <code>null</code>
	 */
	protected Binding getSelectedBinding() {
		if ((completionsTable != null) && (!completionsTable.isDisposed())) {
			int selectedIndex = completionsTable.getSelectionIndex();
			if (selectedIndex != -1) {
				TableItem selectedItem = completionsTable.getItem(selectedIndex);
				return (Binding) selectedItem.getData(BINDING_KEY);
			}
		}
		return null;
	}
}