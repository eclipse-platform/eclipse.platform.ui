/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.commands.CommandException;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.commands.KeysPreferencePage;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

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
final class KeyAssistDialog extends Dialog {

    /**
     * The translation bundle in which to look up internationalized text.
     */
    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(KeyAssistDialog.class.getName());

    /**
     * The activity manager for the associated workbench.
     */
    private final IActivityManager activityManager;

    /**
     * The command manager for the associated workbench.
     */
    private final ICommandManager commandManager;

    /**
     * The name of the command that was selected when the key assist dialog last
     * closed. This is only remembered until <code>clearRememberedState()</code>
     * is called.
     */
    private String commandName = null;

    /**
     * The ordered list of command identifiers corresponding to the table.
     */
    private final List commands = new ArrayList();

    /**
     * The table containing of the possible completions. This value is
     * <code>null</code> until the dialog is created.
     */
    private Table completionsTable = null;

    /**
     * The key binding state for the associated workbench.
     */
    private final KeyBindingState keyBindingState;

    /**
     * The key sequence that was selected when the key assist dialog last
     * closed. This is only remembered until <code>clearRememberedState()</code>
     * is called.
     */
    private String keySequence = null;

    /**
     * The width of the shell when it was previously open. This is only
     * remembered until <code>clearRememberedState()</code> is called.
     */
    private int previousWidth = -1;

    /**
     * The key binding listener for the associated workbench.
     */
    private final WorkbenchKeyboard workbenchKeyboard;

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
        super(null);
        setShellStyle(SWT.NO_TRIM);
        setBlockOnOpen(false);

        this.activityManager = workbench.getActivitySupport()
                .getActivityManager();
        this.commandManager = workbench.getCommandSupport().getCommandManager();
        this.keyBindingState = associatedState;
        this.workbenchKeyboard = associatedKeyboard;
    }

    /**
     * Clears out the remembered state of the key assist dialog. This includes
     * its width, as well as the selected command name and key sequence.
     */
    final void clearRememberedState() {
        previousWidth = -1;
        commandName = null;
        keySequence = null;
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
        final Shell shell = getShell();
        if (rememberState) {
            // Remember the previous width.
            if ((shell != null) && (!shell.isDisposed())) {
                previousWidth = getShell().getSize().x;
            }

            // Remember the selected command name and key sequence.
            if ((completionsTable != null) && (!completionsTable.isDisposed())) {
                final int selectedIndex = completionsTable.getSelectionIndex();
                if (selectedIndex != -1) {
                    final TableItem selectedItem = completionsTable
                            .getItem(selectedIndex);
                    commandName = selectedItem.getText(0);
                    keySequence = selectedItem.getText(1);
                } else {
                    commandName = Util.ZERO_LENGTH_STRING;
                    keySequence = Util.ZERO_LENGTH_STRING;
                }
            }
            completionsTable = null;
        }

        keyBindingState.reset();
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
        final Display display = shell.getDisplay();

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
        final Point location = new Point(xCoord, yCoord);

        // Constrains the position within the display's client area.
        final Rectangle displayBounds = display.getClientArea();
        final int displayRightEdge = displayBounds.x + displayBounds.width;
        if (location.x < displayBounds.x) {
            location.x = displayBounds.x;
        } else if ((location.x + size.x) > displayRightEdge) {
            location.x = displayRightEdge - size.x;
        }
        final int displayBottomEdge = displayBounds.y + displayBounds.height;
        if (location.y < displayBounds.y) {
            location.y = displayBounds.y;
        } else if ((location.y + size.y) > displayBottomEdge) {
            location.y = displayBottomEdge - size.y;
        }

        // Set the location.
        shell.setLocation(location);
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
        if ((previousWidth != -1) && (previousWidth > size.x)) {
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
     * Creates and returns the contents of this dialog's button bar.
     * <p>
     * The <code>Dialog</code> implementation of this framework method lays
     * out a button bar and calls the <code>createButtonsForButtonBar</code>
     * framework method to populate it. Subclasses may override.
     * </p>
     * <p>
     * The returned control's layout data must be an instance of
     * <code>GridData</code>.
     * </p>
     * 
     * @param parent
     *            the parent composite to contain the button bar
     * @return the button bar control
     */
    protected Control createButtonBar(Composite parent) {
        // Create a composite for the button bar contents.
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setBackground(parent.getBackground());

        /*
         * Figure out which key is used to open the key assist. If no key, then
         * just return.
         */
        final ICommand command = commandManager
                .getCommand("org.eclipse.ui.window.showKeyAssist"); //$NON-NLS-1$
        KeySequence keyBinding = null;
        if (command.isDefined()) {
            final List keyBindings = command.getKeySequenceBindings();
            final Iterator keyBindingItr = keyBindings.iterator();
            final KeySequence currentState = keyBindingState
                    .getCurrentSequence();
            final int prefixSize = currentState.getKeyStrokes().size();

            // Try to find the first possible matching key binding.
            while (keyBindingItr.hasNext()) {
                keyBinding = ((IKeySequenceBinding) keyBindingItr.next())
                        .getKeySequence();

                // Now just double-check to make sure the key is still possible.
                if (prefixSize > 0) {
                    if (keyBinding.startsWith(currentState, false)) {
                        /*
                         * Okay, so we have a partial match. Replace the key
                         * binding with the required suffix completion.
                         */
                        final List newKeyStrokes = new ArrayList(keyBinding
                                .getKeyStrokes());
                        for (int i = 0; i < prefixSize; i++) {
                            newKeyStrokes.remove(0);
                        }
                        keyBinding = KeySequence.getInstance(newKeyStrokes);
                        break;

                    }

                    /*
                     * The prefix doesn't match, so null out the key binding and
                     * try again.
                     */
                    keyBinding = null;
                    continue;

                }

                // There is no prefix, so just grab the first.
                break;
            }
        }
        if (keyBinding == null) {
            return composite; // couldn't find a suitable key binding
        }

        // Create a horizontal separator line
        // Looks silly in this dialog, but might be useful in the general case.
        //        final Label separator = new Label(composite, SWT.SEPARATOR
        //                | SWT.HORIZONTAL | SWT.LINE_DOT);
        //        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Create the text widget
        final Label text = new Label(composite, SWT.NONE);
        text.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
        text.setBackground(composite.getBackground());
        text.setText(MessageFormat.format(Util.translateString(RESOURCE_BUNDLE,
                "openPreferencePage"), //$NON-NLS-1$
                new Object[] { keyBinding.format() }));

        return composite;
    }

    /**
     * Creates the contents of the dialog. This simply changes the colour of the
     * background to be <code>SWT.COLOR_INFO_BACKGROUND</code> and then calls
     * the super method.
     * 
     * @param parent
     *            The parent composite to contain the dialog contents; must not
     *            be <code>null</code>.
     * @return The control for the dialog contents; must not be
     *         <code>null</code>.
     */
    protected final Control createContents(final Composite parent) {
        // Create a black border around the outside of the shell.
        final Display display = parent.getDisplay();
        parent.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        final GridLayout shellLayout = new GridLayout(1, false);
        shellLayout.marginHeight = 1;
        shellLayout.marginWidth = 1;
        parent.setLayout(shellLayout);

        /*
         * Hook up the listeners and register the shell with the context
         * support.
         */
        registerListeners();
        registerShellType();

        // Create the top-level composite for the dialog
        final Composite contents = new Composite(parent, SWT.NONE);
        contents.setBackground(display
                .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        final GridLayout contentLayout = new GridLayout();
        contentLayout.marginHeight = 1;
        contentLayout.marginWidth = 1;
        contents.setLayout(contentLayout);
        contents.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Initialize the dialog units.
        initializeDialogUnits(contents);

        // Create the dialog area and button bar
        dialogArea = createDialogArea(contents);
        buttonBar = createButtonBar(contents);

        // Make sure everyone is using the right font.
        applyDialogFont(contents);

        return contents;

    }

    /**
     * Creates the dialog area for the key assistant shell. This creates a table
     * and places it inside the composite. The composite will contain a list of
     * all the key bindings.
     * 
     * @param parent
     *            The parent composite to contain the dialog area; must not be
     *            <code>null</code>.
     * @return The control for the dialog area; must not be <code>null</code>.
     */
    protected final Control createDialogArea(final Composite parent) {
        // Create a composite for the dialog area.
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout compositeLayout = new GridLayout();
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        composite.setLayout(compositeLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setBackground(parent.getBackground());

        // Layout the partial matches.
        final SortedMap partialMatches = getPartialMatches();
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
     * <code>previousWidth</code> if it is not <code>-1</code>.
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
        completionsTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.SINGLE);
        final GridData gridData = new GridData(GridData.FILL_BOTH);
        completionsTable.setLayoutData(gridData);
        completionsTable.setBackground(parent.getBackground());
        completionsTable.setLinesVisible(true);

        // Initialize the columns and rows.
        commands.clear();
        final TableColumn columnCommandName = new TableColumn(completionsTable,
                SWT.LEFT, 0);
        final TableColumn columnKeySequence = new TableColumn(completionsTable,
                SWT.LEFT, 1);
        final Iterator itemsItr = partialMatches.entrySet().iterator();
        while (itemsItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) itemsItr.next();
            final KeySequence sequence = (KeySequence) entry.getValue();
            final ICommand command = (ICommand) entry.getKey();
            try {
                final String[] text = { command.getName(), sequence.format() };
                final TableItem item = new TableItem(completionsTable, SWT.NULL);
                item.setText(text);
                commands.add(command);
            } catch (NotDefinedException e) {
                // Not much to do, but this shouldn't really happen.
            }
        }

        columnKeySequence.pack();
        if (previousWidth != -1) {
            columnKeySequence.setWidth(previousWidth);
        }
        columnCommandName.pack();

        /*
         * If you double-click on the table, it should execute the selected
         * command.
         */
        completionsTable.addSelectionListener(new SelectionAdapter() {
            public final void widgetDefaultSelected(final SelectionEvent event) {
                executeKeyBinding();
            }
        });
    }

    /**
     * Edits the remembered selection in the preference dialog.
     */
    private final void editKeyBinding() {
        // Create a preference dialog on the keys preference page.
        final String keysPageId = "org.eclipse.ui.preferencePages.Keys"; //$NON-NLS-1$
        final WorkbenchPreferenceDialog dialog = WorkbenchPreferenceDialog
                .createDialogOn(keysPageId);

        // Select the right command on the preference page.
        final IPreferencePage page = dialog.getCurrentPage();
        if (page instanceof KeysPreferencePage) {
            final KeysPreferencePage keysPreferencePage = (KeysPreferencePage) page;
            keysPreferencePage.editCommand(commandName, keySequence);
        }

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
    private final void executeKeyBinding() {
        // Try to execute the corresponding command.
        final int selectionIndex = completionsTable.getSelectionIndex();
        if (selectionIndex >= 0) {
            final ICommand command = (ICommand) commands.get(selectionIndex);
            try {
                workbenchKeyboard.executeCommand(command.getId());
            } catch (final CommandException e) {
                workbenchKeyboard.logException(e);
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
        final Map partialMatches = commandManager
                .getPartialMatches(keyBindingState.getCurrentSequence());

        // Create a sorted map that sorts based on lexicographical order.
        final SortedMap sortedMatches = new TreeMap(new Comparator() {
            public final int compare(final Object a, final Object b) {
                ICommand commandA = (ICommand) a;
                ICommand commandB = (ICommand) b;
                try {
                    return commandA.getName().compareTo(commandB.getName());
                } catch (final NotDefinedException e) {
                    throw new AssertionError(e);
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
            final String commandId = (String) entry.getValue();
            final ICommand command = commandManager.getCommand(commandId);
            if (command.isDefined()
                    && activityManager.getIdentifier(command.getId())
                            .isEnabled()) {
                sortedMatches.put(command, entry.getKey());
            }
        }

        return sortedMatches;

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
        if ((commandName != null) && (keySequence != null)) {
            editKeyBinding();
            clearRememberedState();
            return Window.OK;
        }

        // If the dialog is already open, dispose the shell and recreate it.
        final Shell shell = getShell();
        if (shell != null) {
            close();
        }
        create();

        // Configure the size and location.
        final Point size = configureSize();
        configureLocation(size);

        // Call the super method.
        return super.open();
    }

    /**
     * Adds a deactivation listener to the shell. This listener will close the
     * shell if it ever deactivates. This listener should be attached when the
     * shell is created, not opened.
     */
    private final void registerListeners() {
        final Shell shell = getShell();
        shell.addListener(SWT.Deactivate, new Listener() {
            public void handleEvent(Event event) {
                close();
            }
        });
    }

    /**
     * Registers the shell as the same type as its parent with the context
     * support. This ensures that it does not modify the current state of the
     * application.
     */
    private final void registerShellType() {
        final Shell shell = getShell();
        final IWorkbenchContextSupport contextSupport = keyBindingState
                .getAssociatedWindow().getWorkbench().getContextSupport();
        contextSupport.registerShell(shell, contextSupport
                .getShellType((Shell) shell.getParent()));
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
