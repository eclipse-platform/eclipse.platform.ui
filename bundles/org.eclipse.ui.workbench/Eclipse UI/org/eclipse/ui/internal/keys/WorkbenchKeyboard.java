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
package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.CommandException;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.NotHandledException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.ws.WorkbenchCommandSupport;
import org.eclipse.ui.internal.contexts.ws.WorkbenchContextSupport;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ParseException;
import org.eclipse.ui.keys.SWTKeySupport;

/**
 * <p>
 * Controls the keyboard input into the workbench key binding architecture. This
 * allows key events to be programmatically pushed into the key binding
 * architecture -- potentially triggering the execution of commands. It is used
 * by the <code>Workbench</code> to listen for events on the
 * <code>Display</code>.
 * </p>
 * <p>
 * This class is not designed to be thread-safe. It is assumed that all access
 * to the <code>press</code> method is done through the event loop. Accessing
 * this method outside the event loop can cause corruption of internal state.
 * </p>
 * 
 * @since 3.0
 */
public final class WorkbenchKeyboard {

    /**
     * Whether the keyboard should kick into debugging mode. This causes real
     * key bindings trapped by the key binding architecture to be reported.
     */
    private static final boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

    /**
     * Whether the keyboard should report every event received by its global
     * filter.
     */
    private static final boolean DEBUG_VERBOSE = Policy.DEBUG_KEY_BINDINGS_VERBOSE;

    /**
     * The time in milliseconds to wait after pressing a key before displaying
     * the key assist dialog.
     */
    private static final int DELAY = 1000;

    /** The collection of keys that are to be processed out-of-order. */
    static KeySequence outOfOrderKeys;

    /**
     * The translation bundle in which to look up internationalized text.
     */
    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(WorkbenchKeyboard.class.getName());

    static {

        try {
            outOfOrderKeys = KeySequence.getInstance("ESC DEL"); //$NON-NLS-1$
        } catch (ParseException e) {
            outOfOrderKeys = KeySequence.getInstance();
            String message = "Could not parse out-of-order keys definition: 'ESC DEL'.  Continuing with no out-of-order keys."; //$NON-NLS-1$
            WorkbenchPlugin.log(message, new Status(IStatus.ERROR,
                    WorkbenchPlugin.PI_WORKBENCH, 0, message, e));
        }
    }

    /**
     * Generates any key strokes that are near matches to the given event. The
     * first such key stroke is always the exactly matching key stroke.
     * 
     * @param event
     *            The event from which the key strokes should be generated; must
     *            not be <code>null</code>.
     * @return The set of nearly matching key strokes. It is never
     *         <code>null</code>, but may be empty.
     */
    public static List generatePossibleKeyStrokes(Event event) {
        final List keyStrokes = new ArrayList(3);

        /*
         * If this is not a keyboard event, then there are no key strokes. This
         * can happen if we are listening to focus traversal events.
         */
        if ((event.stateMask == 0) && (event.keyCode == 0)
                && (event.character == 0)) {
            return keyStrokes;
        }

        // Add each unique key stroke to the list for consideration.
        final int firstAccelerator = SWTKeySupport
                .convertEventToUnmodifiedAccelerator(event);
        keyStrokes.add(SWTKeySupport
                .convertAcceleratorToKeyStroke(firstAccelerator));

        // We shouldn't allow delete to undergo shift resolution.
        if (event.character == SWT.DEL) {
            return keyStrokes;
        }

        final int secondAccelerator = SWTKeySupport
                .convertEventToUnshiftedModifiedAccelerator(event);
        if (secondAccelerator != firstAccelerator) {
            keyStrokes.add(SWTKeySupport
                    .convertAcceleratorToKeyStroke(secondAccelerator));
        }

        final int thirdAccelerator = SWTKeySupport
                .convertEventToModifiedAccelerator(event);
        if ((thirdAccelerator != secondAccelerator)
                && (thirdAccelerator != firstAccelerator)) {
            keyStrokes.add(SWTKeySupport
                    .convertAcceleratorToKeyStroke(thirdAccelerator));
        }

        return keyStrokes;
    }

    /**
     * <p>
     * Determines whether the given event represents a key press that should be
     * handled as an out-of-order event. An out-of-order key press is one that
     * is passed to the focus control first. Only if the focus control fails to
     * respond will the regular key bindings get applied.
     * </p>
     * <p>
     * Care must be taken in choosing which keys are chosen as out-of-order
     * keys. This method has only been designed and test to work with the
     * unmodified "Escape" key stroke.
     * </p>
     * 
     * @param keyStrokes
     *            The key stroke in which to look for out-of-order keys; must
     *            not be <code>null</code>.
     * @return <code>true</code> if the key is an out-of-order key;
     *         <code>false</code> otherwise.
     */
    private static boolean isOutOfOrderKey(List keyStrokes) {
        // Compare to see if one of the possible key strokes is out of order.
        Iterator keyStrokeItr = keyStrokes.iterator();
        List outOfOrderKeyStrokes = outOfOrderKeys.getKeyStrokes();
        while (keyStrokeItr.hasNext()) {
            if (outOfOrderKeyStrokes.contains(keyStrokeItr.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The command manager to be used to resolve key bindings. This member
     * variable should never be <code>null</code>.
     */
    private final ICommandManager commandManager;

    /**
     * The listener that runs key events past the global key bindings.
     */
    private final Listener keyDownFilter = new Listener() {

        public void handleEvent(Event event) {
            if (DEBUG && DEBUG_VERBOSE) {
                System.out.print("KEYS >>> Listener.handleEvent(type = "); //$NON-NLS-1$
                switch (event.type) {
                case SWT.KeyDown:
                    System.out.print("KeyDown"); //$NON-NLS-1$
                    break;
                case SWT.Traverse:
                    System.out.print("Traverse"); //$NON-NLS-1$
                    break;
                default:
                    System.out.print(event.type);
                }
                System.out.println(", stateMask = 0x" //$NON-NLS-1$
                        + Integer.toHexString(event.stateMask)
                        + ", keyCode = 0x" //$NON-NLS-1$
                        + Integer.toHexString(event.keyCode)
                        + ", character = 0x" //$NON-NLS-1$
                        + Integer.toHexString(event.character) + ")"); //$NON-NLS-1$
            }

            filterKeySequenceBindings(event);
        }
    };

    /**
     * The <code>KeyAssistDialog</code> displayed to the user to assist them
     * in completing a multi-stroke keyboard shortcut.
     * @since 3.1
     */
    private KeyAssistDialog keyAssistDialog = null;

    /**
     * The time at which the last timer was started. This is used to judge if a
     * sufficient amount of time has elapsed. This is simply the output of
     * <code>System.currentTimeMillis()</code>.
     */
    private long startTime = Long.MAX_VALUE;

    /**
     * The mode is the current state of the key binding architecture. In the
     * case of multi-stroke key bindings, this can be a partially complete key
     * binding.
     */
    private final KeyBindingState state;

    /**
     * The window listener responsible for maintaining internal state as the
     * focus moves between windows on the desktop.
     */
    private final IWindowListener windowListener = new IWindowListener() {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowActivated(IWorkbenchWindow window) {
            checkActiveWindow(window);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowClosed(IWorkbenchWindow window) {
            // Do nothing.
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowDeactivated(IWorkbenchWindow window) {
            // Do nothing
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowOpened(IWorkbenchWindow window) {
            // Do nothing.
        }
    };

    /**
     * The workbench on which this keyboard interface should act.
     */
    private final IWorkbench workbench;

    /**
     * Constructs a new instance of <code>WorkbenchKeyboard</code> associated
     * with a particular workbench.
     * 
     * @param associatedWorkbench
     *            The workbench with which this keyboard interface should work;
     *            must not be <code>null</code>.
     * @since 3.1
     */
    public WorkbenchKeyboard(Workbench associatedWorkbench) {
        workbench = associatedWorkbench;
        state = new KeyBindingState(associatedWorkbench);
        commandManager = workbench.getCommandSupport().getCommandManager();
        workbench.addWindowListener(windowListener);
    }

    /**
     * Verifies that the active workbench window is the same as the workbench
     * window associated with the state. This is used to verify that the state
     * is properly reset as focus changes. When they are not the same, the state
     * is reset and associated with the newly activated window.
     * 
     * @param window
     *            The activated window; must not be <code>null</code>.
     */
    private void checkActiveWindow(IWorkbenchWindow window) {
        if (!window.equals(state.getAssociatedWindow())) {
            resetState(true);
            state.setAssociatedWindow(window);
        }
    }

    /**
     * Closes the multi-stroke key binding assistant shell, if it exists and
     * isn't already disposed.
     */
    private void closeMultiKeyAssistShell() {
        if (keyAssistDialog != null) {
            final Shell shell = keyAssistDialog.getShell();
            if ((shell != null) && (!shell.isDisposed()) && (shell.isVisible())) {
                keyAssistDialog.close(true);
            }
        }
    }

    /**
     * Tests whether the given command is enabled.
     * 
     * @param command
     *            The command to test; must not be <code>null</code>.
     * @return <code>true</code> if the command is enabled or not defined;
     *         <code>false</code> otherwise.
     */
    private static boolean isEnabled(ICommand command) {
        try {
            Map attributeValuesByName = command.getAttributeValuesByName();

            if (attributeValuesByName.containsKey("enabled") //$NON-NLS-1$
                    && !Boolean.TRUE.equals(attributeValuesByName
                            .get("enabled"))) { //$NON-NLS-1$
                return false;
            }

            return true;

        } catch (NotHandledException eNotHandled) {
            return false;
        }
    }

    /**
     * Performs the actual execution of the command by looking up the current
     * handler from the command manager. If there is a handler and it is
     * enabled, then it tries the actual execution. Execution failures are
     * logged. When this method completes, the key binding state is reset.
     * 
     * @param commandId
     *            The identifier for the command that should be executed; should
     *            not be <code>null</code>.
     * @return <code>true</code> if there was a handler; <code>false</code>
     *         otherwise.
     * @throws CommandException
     *             if the handler does not complete execution for some reason.
     *             It is up to the caller of this method to decide whether to
     *             log the message, display a dialog, or ignore this exception
     *             entirely.
     */
    final boolean executeCommand(String commandId) throws CommandException {
        if (DEBUG) {
            System.out
                    .println("KEYS >>> WorkbenchKeyboard.executeCommand(commandId = '" //$NON-NLS-1$
                            + commandId + "')"); //$NON-NLS-1$
        }

        // Reset the key binding state (close window, clear status line, etc.)
        resetState(false);

        // Dispatch to the handler.
        ICommand command = commandManager.getCommand(commandId);

        if (DEBUG && DEBUG_VERBOSE) {
            if (!command.isDefined()) {
                System.out.println("KEYS >>>     not defined"); //$NON-NLS-1$
            } else if (!command.isHandled()) {
                System.out.println("KEYS >>>     not handled"); //$NON-NLS-1$
            } else if (!isEnabled(command)) {
                System.out.println("KEYS >>>     not enabled"); //$NON-NLS-1$
            } else {
                try {
                    Map attributeValuesByName = command
                            .getAttributeValuesByName();

                    System.out
                            .println("KEYS >>>     value for 'id' attribute = " //$NON-NLS-1$
                                    + attributeValuesByName.get("id")); //$NON-NLS-1$
                    System.out
                            .println("KEYS >>>     value for 'enabled' attribute = " //$NON-NLS-1$
                                    + attributeValuesByName.get("enabled")); //$NON-NLS-1$
                } catch (NotHandledException eNotHandled) {
                    System.out.println(eNotHandled);
                }
            }
        }

        final boolean commandDefined = command.isDefined();
        final boolean commandHandled = command.isHandled();

        if (commandDefined && commandHandled && isEnabled(command)) {
            command.execute(null);
        }

        /*
         * Now that the command has executed (and had the opportunity to use the
         * remembered state of the dialog), it is safe to delete that
         * information.
         */
        if (keyAssistDialog != null) {
            keyAssistDialog.clearRememberedState();
        }

        return (commandDefined && commandHandled);
    }

    /**
     * <p>
     * Launches the command matching a the typed key. This filter an incoming
     * <code>SWT.KeyDown</code> or <code>SWT.Traverse</code> event at the
     * level of the display (i.e., before it reaches the widgets). It does not
     * allow processing in a dialog or if the key strokes does not contain a
     * natural key.
     * </p>
     * <p>
     * Some key strokes (defined as a property) are declared as out-of-order
     * keys. This means that they are processed by the widget <em>first</em>.
     * Only if the other widget listeners do no useful work does it try to
     * process key bindings. For example, "ESC" can cancel the current widget
     * action, if there is one, without triggering key bindings.
     * </p>
     * 
     * @param event
     *            The incoming event; must not be <code>null</code>.
     */
    private void filterKeySequenceBindings(Event event) {
        /*
         * Only process key strokes containing natural keys to trigger key
         * bindings.
         */
        if ((event.keyCode & SWT.MODIFIER_MASK) != 0)
            return;

        // Allow special key out-of-order processing.
        List keyStrokes = generatePossibleKeyStrokes(event);
        if (isOutOfOrderKey(keyStrokes)) {
            Widget widget = event.widget;
            if ((event.character == SWT.DEL)
                    && ((event.stateMask & SWT.MODIFIER_MASK) == 0)
                    && ((widget instanceof Text) || (widget instanceof Combo))) {
                /*
                 * KLUDGE. Bug 54654. The text widget relies on no listener
                 * doing any work before dispatching the native delete event.
                 * This does not work, as we are restricted to listeners.
                 * However, it can be said that pressing a delete key in a text
                 * widget will never use key bindings. This can be shown be
                 * considering how the event dispatching is expected to work in
                 * a text widget. So, we should do nothing ... ever.
                 */
                return;

            } else if (widget instanceof StyledText) {

                if (event.type == SWT.KeyDown) {
                    /*
                     * KLUDGE. Some people try to do useful work in verify
                     * listeners. The way verify listeners work in SWT, we need
                     * to verify the key as well; otherwise, we can't detect
                     * that useful work has been done.
                     */
                    ((StyledText) widget)
                            .addVerifyKeyListener(new OutOfOrderVerifyListener(
                                    new OutOfOrderListener(this)));
                }

            } else {
                widget.addListener(event.type, new OutOfOrderListener(this));

            }

            /*
             * Otherwise, we count on a key down arriving eventually. Expecting
             * out of order handling on Ctrl+Tab, for example, is a bad idea
             * (stick to keys that are not window traversal keys).
             */

        } else {
            processKeyEvent(keyStrokes, event);

        }
    }

    /**
     * An accessor for the filter that processes key down and traverse events on
     * the display.
     * 
     * @return The global key down and traverse filter; never <code>null</code>.
     */
    public Listener getKeyDownFilter() {
        return keyDownFilter;
    }

    /**
     * Determines whether the key sequence is a perfect match for any command.
     * If there is a match, then the corresponding command identifier is
     * returned.
     * 
     * @param keySequence
     *            The key sequence to check for a match; must never be
     *            <code>null</code>.
     * @return The command identifier for the perfectly matching command;
     *         <code>null</code> if no command matches.
     */
    private String getPerfectMatch(KeySequence keySequence) {
        return commandManager.getPerfectMatch(keySequence);
    }

    /**
     * Changes the key binding state to the given value. This should be an
     * incremental change, but there are no checks to guarantee this is so. It
     * also sets up a <code>Shell</code> to be displayed after one second has
     * elapsed. This shell will show the user the possible completions for what
     * they have typed.
     * 
     * @param sequence
     *            The new key sequence for the state; should not be
     *            <code>null</code>.
     */
    private void incrementState(KeySequence sequence) {
        // Record the starting time.
        startTime = System.currentTimeMillis();
        final long myStartTime = startTime;

        // Update the state.
        state.setCurrentSequence(sequence);
        state.setAssociatedWindow(workbench.getActiveWorkbenchWindow());

        // After some time, open a shell displaying the possible completions.
        final Display display = workbench.getDisplay();
        display.timerExec(DELAY, new Runnable() {
            public void run() {
                if ((System.currentTimeMillis() > (myStartTime - DELAY))
                        && (startTime == myStartTime)) {
                    openMultiKeyAssistShell();
                }
            }
        });
    }

    /**
     * Determines whether the key sequence partially matches on of the active
     * key bindings.
     * 
     * @param keySequence
     *            The key sequence to check for a partial match; must never be
     *            <code>null</code>.
     * @return <code>true</code> if there is a partial match;
     *         <code>false</code> otherwise.
     */
    private boolean isPartialMatch(KeySequence keySequence) {
        return commandManager.isPartialMatch(keySequence);
    }

    /**
     * Determines whether the key sequence perfectly matches on of the active
     * key bindings.
     * 
     * @param keySequence
     *            The key sequence to check for a perfect match; must never be
     *            <code>null</code>.
     * @return <code>true</code> if there is a perfect match;
     *         <code>false</code> otherwise.
     */
    private boolean isPerfectMatch(KeySequence keySequence) {
        return commandManager.isPerfectMatch(keySequence);
    }

    /**
     * Logs the given exception, and opens a dialog explaining the failure.
     * 
     * @param e
     *            The exception to log; must not be <code>null</code>.
     */
    final void logException(final CommandException e) {
        Throwable nestedException = e.getCause();
        Throwable exception = (nestedException == null) ? e : nestedException;
        String message = Util.translateString(RESOURCE_BUNDLE,
                "ExecutionError.Message"); //$NON-NLS-1$
        String title = Util.translateString(RESOURCE_BUNDLE,
                "ExecutionError.Title"); //$NON-NLS-1$
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage == null) {
            exceptionMessage = message;
        }
        IStatus status = new Status(IStatus.ERROR,
                WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
        WorkbenchPlugin.log(message, status);
        ErrorDialog.openError(workbench.getActiveWorkbenchWindow().getShell(),
                title, message, status);
    }

    /**
     * Opens a <code>KeyAssistDialog</code> to assist the user in completing a
     * multi-stroke key binding. This method lazily creates a
     * <code>keyAssistDialog</code> and shares it between executions.
     */
    public final void openMultiKeyAssistShell() {
        if (keyAssistDialog == null) {
            keyAssistDialog = new KeyAssistDialog(workbench, this, state);
        }
        if (keyAssistDialog.getShell() == null) {
            final Shell parentShell = workbench.getDisplay().getActiveShell();
            keyAssistDialog.setParentShell(parentShell);
        }
        keyAssistDialog.open();
    }

    /**
     * Processes a key press with respect to the key binding architecture. This
     * updates the mode of the command manager, and runs the current handler for
     * the command that matches the key sequence, if any.
     * 
     * @param potentialKeyStrokes
     *            The key strokes that could potentially match, in the order of
     *            priority; must not be <code>null</code>.
     * @param event
     *            The event; may be <code>null</code>.
     * @return <code>true</code> if a command is executed; <code>false</code>
     *         otherwise.
     * @throws CommandException
     *             if the handler does not complete execution for some reason.
     *             It is up to the caller of this method to decide whether to
     *             log the message, display a dialog, or ignore this exception
     *             entirely.
     */
    public boolean press(List potentialKeyStrokes, Event event)
            throws CommandException {
        // TODO remove event parameter once key-modified actions are removed
        if (DEBUG && DEBUG_VERBOSE) {
            System.out
                    .println("KEYS >>> WorkbenchKeyboard.press(potentialKeyStrokes = " //$NON-NLS-1$
                            + potentialKeyStrokes + ")"); //$NON-NLS-1$
        }

        /*
         * TODO Kludge. A partial workaround for Bug 56231. This should be
         * removed once SWT fixes Bug 56231 such that activation works properly
         * on all platforms.
         */
        if ("gtk".equals(SWT.getPlatform())) { //$NON-NLS-1$
            final Widget widget = event.widget;
            final Shell shell;
            if ((widget instanceof Control) && (!widget.isDisposed())) {
                shell = ((Control) widget).getShell();
            } else {
                shell = Display.getCurrent().getActiveShell();
            }

            ((WorkbenchCommandSupport) workbench.getCommandSupport())
                    .processHandlerSubmissions(false, shell);
            ((WorkbenchContextSupport) workbench.getContextSupport())
                    .processEnabledSubmissions(false, shell);
        }

        KeySequence sequenceBeforeKeyStroke = state.getCurrentSequence();
        for (Iterator iterator = potentialKeyStrokes.iterator(); iterator
                .hasNext();) {
            KeySequence sequenceAfterKeyStroke = KeySequence.getInstance(
                    sequenceBeforeKeyStroke, (KeyStroke) iterator.next());
            if (isPartialMatch(sequenceAfterKeyStroke)) {
                incrementState(sequenceAfterKeyStroke);
                return true;

            } else if (isPerfectMatch(sequenceAfterKeyStroke)) {
                String commandId = getPerfectMatch(sequenceAfterKeyStroke);
                return (executeCommand(commandId) || !sequenceBeforeKeyStroke
                        .isEmpty());

            } else if ((keyAssistDialog != null)
                    && ((event.keyCode == SWT.ARROW_DOWN)
                            || (event.keyCode == SWT.ARROW_UP)
                            || (event.keyCode == SWT.ARROW_LEFT)
                            || (event.keyCode == SWT.ARROW_RIGHT)
                            || (event.keyCode == SWT.CR)
                            || (event.keyCode == SWT.PAGE_UP) || (event.keyCode == SWT.PAGE_DOWN))) {
                // We don't want to swallow keyboard navigation keys.
                return false;

            }
        }

        resetState(true);
        return false;
    }

    /**
     * <p>
     * Actually performs the processing of the key event by interacting with the
     * <code>ICommandManager</code>. If work is carried out, then the event
     * is stopped here (i.e., <code>event.doit = false</code>). It does not
     * do any processing if there are no matching key strokes.
     * </p>
     * <p>
     * If the active <code>Shell</code> is not the same as the one to which
     * the state is associated, then a reset occurs.
     * </p>
     * 
     * @param keyStrokes
     *            The set of all possible matching key strokes; must not be
     *            <code>null</code>.
     * @param event
     *            The event to process; must not be <code>null</code>.
     */
    void processKeyEvent(List keyStrokes, Event event) {
        // Dispatch the keyboard shortcut, if any.
        boolean eatKey = false;
        if (!keyStrokes.isEmpty()) {
            try {
                eatKey = press(keyStrokes, event);
            } catch (CommandException e) {
                logException(e);
                eatKey = true;
            }
        }

        if (eatKey) {
            switch (event.type) {
            case SWT.KeyDown:
                event.doit = false;
                break;
            case SWT.Traverse:
                event.detail = SWT.TRAVERSE_NONE;
                event.doit = true;
                break;
            default:
            }
            event.type = SWT.NONE;
        }
    }

    /**
     * Resets the state, and cancels any running timers. If there is a
     * <code>Shell</code> currently open, then it closes it.
     * 
     * @param clearRememberedState
     *            Whether the remembered state (dialog bounds) of the key assist
     *            should be forgotten immediately as well.
     */
    private final void resetState(final boolean clearRememberedState) {
        startTime = Long.MAX_VALUE;
        state.reset();
        closeMultiKeyAssistShell();
        if ((keyAssistDialog != null) && clearRememberedState) {
            keyAssistDialog.clearRememberedState();
        }
    }
}