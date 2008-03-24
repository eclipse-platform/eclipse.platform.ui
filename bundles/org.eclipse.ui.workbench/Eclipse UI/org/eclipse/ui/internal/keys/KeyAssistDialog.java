/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.preference.PreferenceDialog;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;

import com.ibm.icu.text.MessageFormat;

/**
 * <p>
 * A dialog displaying a list of key bindings. The dialog will execute a command
 * if it is selected.
 * </p>
 * <p>
 * The methods on this class are not thread-safe and must be run from the UI
 * thread.
 * </p>
 * 
 * @since 3.1
 */
final class KeyAssistDialog extends PopupDialog {

	/**
	 * The data key for the binding stored on an SWT widget. The key is a
	 * fully-qualified name, but in reverse order. This is so that the equals
	 * method will detect misses faster.
	 */
	private static final String BINDING_KEY = "Binding.bindings.jface.eclipse.org"; //$NON-NLS-1$

	/**
	 * The value of <code>previousWidth</code> to set if there is no
	 * remembered width.
	 */
	private static final int NO_REMEMBERED_WIDTH = -1;

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(KeyAssistDialog.class.getName());

	/**
	 * The activity manager for the associated workbench.
	 */
	private final IActivityManager activityManager;

	/**
	 * The binding service for the associated workbench.
	 */
	private final IBindingService bindingService;

	/**
	 * The binding that was selected when the key assist dialog last closed.
	 * This is only remembered until <code>clearRememberedState()</code> is
	 * called.
	 */
	private Binding binding = null;

	/**
	 * The ordered list of command identifiers corresponding to the table.
	 */
	private final List bindings = new ArrayList();

	/**
	 * The command service for the associated workbench.
	 */
	private final ICommandService commandService;

	/**
	 * The table containing of the possible completions. This value is
	 * <code>null</code> until the dialog is created.
	 */
	private Table completionsTable = null;

	/**
	 * Whether this dialog is currently holding some remembered state.
	 */
	private boolean hasRememberedState = false;

	/**
	 * The key binding state for the associated workbench.
	 */
	private final KeyBindingState keyBindingState;

	/**
	 * The width of the shell when it was previously open. This is only
	 * remembered until <code>clearRememberedState()</code> is called.
	 */
	private int previousWidth = NO_REMEMBERED_WIDTH;

	/**
	 * The key binding listener for the associated workbench.
	 */
	private final WorkbenchKeyboard workbenchKeyboard;

	/**
	 * A sorted map of conflicts to be used when the dialog pops up.
	 * 
	 * @since 3.3
	 */
	private SortedMap conflictMatches;

	/**
	 * Constructs a new instance of <code>KeyAssistDialog</code>. When the
	 * dialog is first constructed, it contains no widgets. The dialog is first
	 * created with no parent. If a parent is required, call
	 * <code>setParentShell()</code>. Also, between uses, it might be
	 * necessary to call <code>setParentShell()</code> as well.
	 * 
	 * @param workbench
	 *            The workbench in which this dialog is created; must not be
	 *            <code>null</code>.
	 * @param associatedKeyboard
	 *            The key binding listener for the workbench; must not be
	 *            <code>null</code>.
	 * @param associatedState
	 *            The key binding state associated with the workbench; must not
	 *            be <code>null</code>.
	 */
	KeyAssistDialog(final IWorkbench workbench,
			final WorkbenchKeyboard associatedKeyboard,
			final KeyBindingState associatedState) {
		super((Shell) null, PopupDialog.INFOPOPUP_SHELLSTYLE, true, false,
				false, false, null, null);

		this.activityManager = workbench.getActivitySupport()
				.getActivityManager();
		this.bindingService = (IBindingService) workbench
				.getService(IBindingService.class);
		this.commandService = (ICommandService) workbench
				.getService(ICommandService.class);
		this.keyBindingState = associatedState;
		this.workbenchKeyboard = associatedKeyboard;

		this.setInfoText(getKeySequenceString());
	}

	/**
	 * Clears out the remembered state of the key assist dialog. This includes
	 * its width, as well as the selected binding.
	 */
	final void clearRememberedState() {
		previousWidth = NO_REMEMBERED_WIDTH;
		binding = null;
		hasRememberedState = false;
	}

	/**
	 * Closes this shell, but first remembers some state of the dialog. This way
	 * it will have a response if asked to open the dialog again or if asked to
	 * open the keys preference page. This does not remember the internal state.
	 * 
	 * @return Whether the shell was already closed.
	 */
	public final boolean close() {
		return close(false);
	}

	/**
	 * Closes this shell, but first remembers some state of the dialog. This way
	 * it will have a response if asked to open the dialog again or if asked to
	 * open the keys preference page.
	 * 
	 * @param rememberState
	 *            Whether the internal state should be remembered.
	 * @return Whether the shell was already closed.
	 */
	public final boolean close(final boolean rememberState) {
		return close(rememberState, true);
	}

	/**
	 * Closes this shell, but first remembers some state of the dialog. This way
	 * it will have a response if asked to open the dialog again or if asked to
	 * open the keys preference page.
	 * 
	 * @param rememberState
	 *            Whether the internal state should be remembered.
	 * @param resetState
	 *            Whether the state should be reset.
	 * @return Whether the shell was already closed.
	 */
	private final boolean close(final boolean rememberState,
			final boolean resetState) {
		final Shell shell = getShell();
		if (rememberState) {
			// Remember the previous width.
			final int widthToRemember;
			if ((shell != null) && (!shell.isDisposed())) {
				widthToRemember = getShell().getSize().x;
			} else {
				widthToRemember = NO_REMEMBERED_WIDTH;
			}

			// Remember the selected command name and key sequence.
			final Binding bindingToRemember;
			if ((completionsTable != null) && (!completionsTable.isDisposed())) {
				final int selectedIndex = completionsTable.getSelectionIndex();
				if (selectedIndex != -1) {
					final TableItem selectedItem = completionsTable
							.getItem(selectedIndex);
					bindingToRemember = (Binding) selectedItem
							.getData(BINDING_KEY);
				} else {
					bindingToRemember = null;
				}
			} else {
				bindingToRemember = null;
			}

			rememberState(widthToRemember, bindingToRemember);
			completionsTable = null;
		}

		if (resetState) {
			keyBindingState.reset();
		}
		return super.close();
	}

	/**
	 * Sets the position for the dialog based on the position of the workbench
	 * window. The dialog is flush with the bottom right corner of the workbench
	 * window. However, the dialog will not appear outside of the display's
	 * client area.
	 * 
	 * @param size
	 *            The final size of the dialog; must not be <code>null</code>.
	 */
	private final void configureLocation(final Point size) {
		final Shell shell = getShell();

		final Shell workbenchWindowShell = keyBindingState
				.getAssociatedWindow().getShell();
		final int xCoord;
		final int yCoord;
		if (workbenchWindowShell != null) {
			/*
			 * Position the shell at the bottom right corner of the workbench
			 * window
			 */
			final Rectangle workbenchWindowBounds = workbenchWindowShell
					.getBounds();
			xCoord = workbenchWindowBounds.x + workbenchWindowBounds.width
					- size.x - 10;
			yCoord = workbenchWindowBounds.y + workbenchWindowBounds.height
					- size.y - 10;

		} else {
			xCoord = 0;
			yCoord = 0;

		}
		final Rectangle bounds = new Rectangle(xCoord, yCoord, size.x, size.y);
		shell.setBounds(getConstrainedShellBounds(bounds));
	}

	/**
	 * Sets the size for the dialog based on its previous size. The width of the
	 * dialog is its previous width, if it exists. Otherwise, it is simply the
	 * packed width of the dialog. The maximum width is 40% of the workbench
	 * window's width. The dialog's height is the packed height of the dialog to
	 * a maximum of half the height of the workbench window.
	 * 
	 * @return The size of the dialog
	 */
	private final Point configureSize() {
		final Shell shell = getShell();

		// Get the packed size of the shell.
		shell.pack();
		final Point size = shell.getSize();

		// Use the previous width if appropriate.
		if ((previousWidth != NO_REMEMBERED_WIDTH) && (previousWidth > size.x)) {
			size.x = previousWidth;
		}

		// Enforce maximum sizing.
		final Shell workbenchWindowShell = keyBindingState
				.getAssociatedWindow().getShell();
		if (workbenchWindowShell != null) {
			final Point workbenchWindowSize = workbenchWindowShell.getSize();
			final int maxWidth = workbenchWindowSize.x * 2 / 5;
			final int maxHeight = workbenchWindowSize.y / 2;
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
	 * Returns a string representing the key sequence used to open this dialog.
	 * 
	 * @return the string describing the key sequence, or <code>null</code> if
	 *         it cannot be determined.
	 */
	private String getKeySequenceString() {
		final Command command = commandService
				.getCommand("org.eclipse.ui.window.showKeyAssist"); //$NON-NLS-1$
		final TriggerSequence[] keyBindings = bindingService
				.getActiveBindingsFor(new ParameterizedCommand(command, null));
		final int keyBindingsCount = keyBindings.length;
		final KeySequence currentState = keyBindingState.getCurrentSequence();
		final int prefixSize = currentState.getKeyStrokes().length;

		// Try to find the first possible matching key binding.
		KeySequence keySequence = null;
		for (int i = 0; i < keyBindingsCount; i++) {
			keySequence = (KeySequence) keyBindings[i];

			// Now just double-check to make sure the key is still possible.
			if (prefixSize > 0) {
				if (keySequence.startsWith(currentState, false)) {
					/*
					 * Okay, so we have a partial match. Replace the key binding
					 * with the required suffix completion.
					 */
					final KeyStroke[] oldKeyStrokes = keySequence
							.getKeyStrokes();
					final int newSize = oldKeyStrokes.length - prefixSize;
					final KeyStroke[] newKeyStrokes = new KeyStroke[newSize];
					System.arraycopy(oldKeyStrokes, prefixSize, newKeyStrokes,
							0, newSize);
					keySequence = KeySequence.getInstance(newKeyStrokes);
					break;
				}

				/*
				 * The prefix doesn't match, so null out the key binding and try
				 * again.
				 */
				keySequence = null;
				continue;

			}

			// There is no prefix, so just grab the first.
			break;
		}
		if (keySequence == null) {
			return null; // couldn't find a suitable key binding
		}

		return MessageFormat.format(Util.translateString(RESOURCE_BUNDLE,
				"openPreferencePage"), //$NON-NLS-1$
				new Object[] { keySequence.format() });
	}

	/**
	 * Creates the content area for the key assistant. This creates a table and
	 * places it inside the composite. The composite will contain a list of all
	 * the key bindings.
	 * 
	 * @param parent
	 *            The parent composite to contain the dialog area; must not be
	 *            <code>null</code>.
	 */
	protected final Control createDialogArea(final Composite parent) {
		// First, register the shell type with the context support
		registerShellType();

		// Create a composite for the dialog area.
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout compositeLayout = new GridLayout();
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout(compositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(parent.getBackground());

		// Layout the partial matches.
		final SortedMap partialMatches;
		if (conflictMatches != null) {
			partialMatches = conflictMatches;
			conflictMatches = null;
		} else {
			partialMatches = getPartialMatches();
		}

		if (partialMatches.isEmpty()) {
			createEmptyDialogArea(composite);
		} else {
			createTableDialogArea(composite, partialMatches);
		}
		return composite;
	}

	/**
	 * Creates an empty dialog area with a simple message saying there were no
	 * matches. This is used if no partial matches could be found. This should
	 * not really ever happen, but might be possible if the commands are
	 * changing while waiting for this dialog to open.
	 * 
	 * @param parent
	 *            The parent composite for the dialog area; must not be
	 *            <code>null</code>.
	 */
	private final void createEmptyDialogArea(final Composite parent) {
		final Label noMatchesLabel = new Label(parent, SWT.NULL);
		noMatchesLabel.setText(Util.translateString(RESOURCE_BUNDLE,
				"NoMatches.Message")); //$NON-NLS-1$
		noMatchesLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		noMatchesLabel.setBackground(parent.getBackground());
	}

	/**
	 * Creates a dialog area with a table of the partial matches for the current
	 * key binding state. The table will be either the minimum width, or
	 * <code>previousWidth</code> if it is not
	 * <code>NO_REMEMBERED_WIDTH</code>.
	 * 
	 * @param parent
	 *            The parent composite for the dialog area; must not be
	 *            <code>null</code>.
	 * @param partialMatches
	 *            The lexicographically sorted map of partial matches for the
	 *            current state; must not be <code>null</code> or empty.
	 */
	private final void createTableDialogArea(final Composite parent,
			final SortedMap partialMatches) {
		// Layout the table.
		completionsTable = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE);
		final GridData gridData = new GridData(GridData.FILL_BOTH);
		completionsTable.setLayoutData(gridData);
		completionsTable.setBackground(parent.getBackground());
		completionsTable.setLinesVisible(true);

		// Initialize the columns and rows.
		bindings.clear();
		final TableColumn columnCommandName = new TableColumn(completionsTable,
				SWT.LEFT, 0);
		final TableColumn columnKeySequence = new TableColumn(completionsTable,
				SWT.LEFT, 1);
		final Iterator itemsItr = partialMatches.entrySet().iterator();
		while (itemsItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) itemsItr.next();
			final String sequence = (String) entry.getValue();
			final Binding binding = (Binding) entry.getKey();
			final ParameterizedCommand command = binding
					.getParameterizedCommand();
			try {
				final String[] text = { command.getName(), sequence };
				final TableItem item = new TableItem(completionsTable, SWT.NULL);
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

		/*
		 * If you double-click on the table, it should execute the selected
		 * command.
		 */
		completionsTable.addListener(SWT.DefaultSelection, new Listener() {
			public final void handleEvent(final Event event) {
				executeKeyBinding(event);
			}
		});
	}

	/**
	 * Edits the remembered selection in the preference dialog.
	 */
	private final void editKeyBinding() {
		// Create a preference dialog on the keys preference page.
		final String keysPageId = "org.eclipse.ui.preferencePages.Keys"; //$NON-NLS-1$
		final PreferenceDialog dialog = PreferencesUtil
				.createPreferenceDialogOn(getShell(), keysPageId, null, binding);

		/*
		 * Forget the remembered state (so we don't get stuck editing
		 * preferences).
		 */
		clearRememberedState();

		// Open the dialog (blocking).
		dialog.open();
	}

	/**
	 * Handles the default selection event on the table of possible completions.
	 * This attempts to execute the given command.
	 */
	private final void executeKeyBinding(final Event trigger) {
		// Try to execute the corresponding command.
		final int selectionIndex = completionsTable.getSelectionIndex();
		if (selectionIndex >= 0) {
			final Binding binding = (Binding) bindings.get(selectionIndex);
			try {
				workbenchKeyboard.executeCommand(binding, trigger);
			} catch (final CommandException e) {
				workbenchKeyboard.logException(e, binding
						.getParameterizedCommand());
			}
		}
	}

	/**
	 * Gets the list of key bindings that are partial matches to the current key
	 * binding state.
	 * 
	 * @return A sorted map of key sequences (KeySequence) to command identifier
	 *         (String) representing the list of enabled commands that could
	 *         possibly complete the current key sequence.
	 */
	private final SortedMap getPartialMatches() {
		// Put all partial matches into the matches into the map.
		final Map partialMatches = bindingService
				.getPartialMatches(keyBindingState.getCurrentSequence());

		// Create a sorted map that sorts based on lexicographical order.
		final SortedMap sortedMatches = new TreeMap(new Comparator() {
			public final int compare(final Object a, final Object b) {
				final Binding bindingA = (Binding) a;
				final Binding bindingB = (Binding) b;
				final ParameterizedCommand commandA = bindingA
						.getParameterizedCommand();
				final ParameterizedCommand commandB = bindingB
						.getParameterizedCommand();
				try {
					return commandA.getName().compareTo(commandB.getName());
				} catch (final NotDefinedException e) {
					// should not happen
					return 0;
				}
			}
		});

		/*
		 * Remove those partial matches for which either the command is not
		 * identified or the activity manager believes the command is not
		 * enabled.
		 */
		final Iterator partialMatchItr = partialMatches.entrySet().iterator();
		while (partialMatchItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) partialMatchItr.next();
			final Binding binding = (Binding) entry.getValue();
			final Command command = binding.getParameterizedCommand()
					.getCommand();
			if (command.isDefined()
					&& activityManager.getIdentifier(command.getId())
							.isEnabled()) {
				TriggerSequence bestActiveBindingFor = bindingService.getBestActiveBindingFor(binding.getParameterizedCommand());
				sortedMatches.put(binding, bestActiveBindingFor==null?null:bestActiveBindingFor.format());
			}
		}

		return sortedMatches;

	}

	/**
	 * Returns whether the dialog is currently holding some remembered state.
	 * 
	 * @return <code>true</code> if the dialog has remembered state;
	 *         <code>false</code> otherwise.
	 */
	private final boolean hasRememberedState() {
		return hasRememberedState;
	}

	/**
	 * Opens this dialog. This method can be called multiple times on the same
	 * dialog. This only opens the dialog if there is no remembered state; if
	 * there is remembered state, then it tries to open the preference page
	 * instead.
	 * 
	 * @return The return code from this dialog.
	 */
	public final int open() {
		// If there is remember state, open the preference page.
		if (hasRememberedState()) {
			editKeyBinding();
			clearRememberedState();
			return Window.OK;
		}

		// If the dialog is already open, dispose the shell and recreate it.
		final Shell shell = getShell();
		if (shell != null) {
			close(false, false);
		}
		create();

		// Configure the size and location.
		final Point size = configureSize();
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
	public final int open(Collection bindings) {
		conflictMatches = new TreeMap(new Comparator() {
			public final int compare(final Object a, final Object b) {
				final Binding bindingA = (Binding) a;
				final Binding bindingB = (Binding) b;
				final ParameterizedCommand commandA = bindingA
						.getParameterizedCommand();
				final ParameterizedCommand commandB = bindingB
						.getParameterizedCommand();
				try {
					return commandA.getName().compareTo(commandB.getName());
				} catch (final NotDefinedException e) {
					// should not happen
					return 0;
				}
			}
		});
		Iterator i = bindings.iterator();
		while (i.hasNext()) {
			Binding b = (Binding) i.next();
			TriggerSequence bestActiveBindingFor = bindingService.getBestActiveBindingFor(b.getParameterizedCommand());
			conflictMatches.put(b, bestActiveBindingFor==null?null:bestActiveBindingFor.format());
		}

		// If the dialog is already open, dispose the shell and recreate it.
		final Shell shell = getShell();
		if (shell != null) {
			close(false, false);
		}
		create();

		// Configure the size and location.
		final Point size = configureSize();
		configureLocation(size);

		// Call the super method.
		return super.open();
	}

	/**
	 * Registers the shell as the same type as its parent with the context
	 * support. This ensures that it does not modify the current state of the
	 * application.
	 */
	private final void registerShellType() {
		final Shell shell = getShell();
		final IContextService contextService = (IContextService) keyBindingState
				.getAssociatedWindow().getWorkbench().getService(
						IContextService.class);
		contextService.registerShell(shell, contextService
				.getShellType((Shell) shell.getParent()));
	}

	/**
	 * Remembers the current state of this dialog.
	 * 
	 * @param previousWidth
	 *            The previous width of the dialog.
	 * @param binding
	 *            The binding to remember, may be <code>null</code> if none.
	 */
	private final void rememberState(final int previousWidth,
			final Binding binding) {
		this.previousWidth = previousWidth;
		this.binding = binding;
		hasRememberedState = true;
	}

	/**
	 * Exposing this within the keys package.
	 * 
	 * @param newParentShell
	 *            The new parent shell; this value may be <code>null</code> if
	 *            there is to be no parent.
	 */
	protected final void setParentShell(final Shell newParentShell) {
		super.setParentShell(newParentShell);
	}
}
