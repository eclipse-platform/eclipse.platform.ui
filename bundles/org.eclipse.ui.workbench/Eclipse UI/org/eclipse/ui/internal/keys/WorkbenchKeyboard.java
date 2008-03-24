/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.internal.InternalPolicy;
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
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.contexts.ContextService;
import org.eclipse.ui.internal.handlers.HandlerService;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.statushandlers.StatusManager;

import com.ibm.icu.text.MessageFormat;

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
	 * A display filter for handling key bindings. This filter can either be
	 * enabled or disabled. If disabled, the filter does not process incoming
	 * events. The filter starts enabled.
	 * 
	 * @since 3.1
	 */
	public final class KeyDownFilter implements Listener {

		/**
		 * Whether the filter is enabled.
		 */
		private transient boolean enabled = true;

		/**
		 * Handles an incoming traverse or key down event.
		 * 
		 * @param event
		 *            The event to process; must not be <code>null</code>.
		 */
		public final void handleEvent(final Event event) {
			if (!enabled) {
				return;
			}

			if (DEBUG && DEBUG_VERBOSE) {
				final StringBuffer buffer = new StringBuffer(
						"Listener.handleEvent(type = "); //$NON-NLS-1$
				switch (event.type) {
				case SWT.KeyDown:
					buffer.append("KeyDown"); //$NON-NLS-1$
					break;
				case SWT.Traverse:
					buffer.append("Traverse"); //$NON-NLS-1$
					break;
				default:
					buffer.append(event.type);
				}
				buffer.append(", stateMask = 0x" //$NON-NLS-1$
						+ Integer.toHexString(event.stateMask)
						+ ", keyCode = 0x" //$NON-NLS-1$
						+ Integer.toHexString(event.keyCode) + ", time = " //$NON-NLS-1$
						+ event.time + ", character = 0x" //$NON-NLS-1$
						+ Integer.toHexString(event.character) + ")"); //$NON-NLS-1$
				Tracing.printTrace("KEYS", buffer.toString()); //$NON-NLS-1$
			}

			filterKeySequenceBindings(event);
		}

		/**
		 * Returns whether the key binding filter is enabled.
		 * 
		 * @return Whether the key filter is enabled.
		 */
		public final boolean isEnabled() {
			return enabled;
		}

		/**
		 * Sets whether this filter should be enabled or disabled.
		 * 
		 * @param enabled
		 *            Whether key binding filter should be enabled.
		 */
		public final void setEnabled(final boolean enabled) {
			this.enabled = enabled;
		}
	}

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
		final KeyStroke[] outOfOrderKeyStrokes = outOfOrderKeys.getKeyStrokes();
		final int outOfOrderKeyStrokesLength = outOfOrderKeyStrokes.length;
		for (int i = 0; i < outOfOrderKeyStrokesLength; i++) {
			if (keyStrokes.contains(outOfOrderKeyStrokes[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The binding manager to be used to resolve key bindings. This member
	 * variable will be <code>null</code> if it has not yet been initialized.
	 */
	private IBindingService bindingService = null;

	/**
	 * The <code>KeyAssistDialog</code> displayed to the user to assist them
	 * in completing a multi-stroke keyboard shortcut.
	 * 
	 * @since 3.1
	 */
	private KeyAssistDialog keyAssistDialog = null;

	/**
	 * The listener that runs key events past the global key bindings.
	 */
	private final KeyDownFilter keyDownFilter = new KeyDownFilter();

	/**
	 * The single out-of-order listener used by the workbench. This listener is
	 * attached to one widget at a time, and is used to catch key down events
	 * after all processing is done. This technique is used so that some keys
	 * will have their native behaviour happen first.
	 * 
	 * @since 3.1
	 */
	private final OutOfOrderListener outOfOrderListener = new OutOfOrderListener(
			this);

	/**
	 * The single out-of-order verify listener used by the workbench. This
	 * listener is attached to one</code> StyledText</code> at a time, and is
	 * used to catch verify events after all processing is done. This technique
	 * is used so that some keys will have their native behaviour happen first.
	 * 
	 * @since 3.1
	 */
	private final OutOfOrderVerifyListener outOfOrderVerifyListener = new OutOfOrderVerifyListener(
			outOfOrderListener);

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
	 * Performs the actual execution of the command by looking up the current
	 * handler from the command manager. If there is a handler and it is
	 * enabled, then it tries the actual execution. Execution failures are
	 * logged. When this method completes, the key binding state is reset.
	 * 
	 * @param binding
	 *            The binding that should be executed; should not be
	 *            <code>null</code>.
	 * @param trigger
	 *            The triggering event; may be <code>null</code>.
	 * @return <code>true</code> if there was a handler; <code>false</code>
	 *         otherwise.
	 * @throws CommandException
	 *             if the handler does not complete execution for some reason.
	 *             It is up to the caller of this method to decide whether to
	 *             log the message, display a dialog, or ignore this exception
	 *             entirely.
	 */
	final boolean executeCommand(final Binding binding, final Event trigger)
			throws CommandException {
		final ParameterizedCommand parameterizedCommand = binding
				.getParameterizedCommand();

		if (DEBUG) {
			Tracing.printTrace("KEYS", //$NON-NLS-1$
					"WorkbenchKeyboard.executeCommand(commandId = '" //$NON-NLS-1$
							+ parameterizedCommand.getId() + "', parameters = " //$NON-NLS-1$
							+ parameterizedCommand.getParameterMap() + ')');
		}

		// Reset the key binding state (close window, clear status line, etc.)
		resetState(false);

		// Dispatch to the handler.
		final IHandlerService handlerService = (IHandlerService) workbench
				.getService(IHandlerService.class);
		final Command command = parameterizedCommand.getCommand();
		final boolean commandDefined = command.isDefined();
		final boolean commandHandled = command.isHandled();
		command.setEnabled(handlerService.getCurrentState());
		final boolean commandEnabled = command.isEnabled();

		if (DEBUG && DEBUG_VERBOSE) {
			if (!commandDefined) {
				Tracing.printTrace("KEYS", "    not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (!commandHandled) {
				Tracing.printTrace("KEYS", "    not handled"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (!commandEnabled) {
				Tracing.printTrace("KEYS", "    not enabled"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		try {
			handlerService.executeCommand(parameterizedCommand, trigger);
		} catch (final NotDefinedException e) {
			// The command is not defined. Forwarded to the IExecutionListener.
		} catch (final NotEnabledException e) {
			// The command is not enabled. Forwarded to the IExecutionListener.
		} catch (final NotHandledException e) {
			// There is no handler. Forwarded to the IExecutionListener.
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
		if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
			return;
		}

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
					if (!outOfOrderVerifyListener.isActive(event.time)) {
						((StyledText) widget)
								.addVerifyKeyListener(outOfOrderVerifyListener);
						outOfOrderVerifyListener.setActive(event.time);
					}
				}

			} else {
				if (!outOfOrderListener.isActive(event.time)) {
					widget.addListener(SWT.KeyDown, outOfOrderListener);
					outOfOrderListener.setActive(event.time);
				}

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
	public KeyDownFilter getKeyDownFilter() {
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
	 * @return The binding for the perfectly matching command; <code>null</code>
	 *         if no command matches.
	 */
	private Binding getPerfectMatch(KeySequence keySequence) {
		if (bindingService == null) {
			bindingService = (IBindingService) workbench
					.getService(IBindingService.class);
		}
		return bindingService.getPerfectMatch(keySequence);
	}

	final KeySequence getBuffer() {
		return state.getCurrentSequence();
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
		if (bindingService == null) {
			bindingService = (IBindingService) workbench
					.getService(IBindingService.class);
		}
		return bindingService.isPartialMatch(keySequence);
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
		if (bindingService == null) {
			bindingService = (IBindingService) workbench
					.getService(IBindingService.class);
		}
		return bindingService.isPerfectMatch(keySequence);
	}

	/**
	 * Logs the given exception, and opens a dialog explaining the failure.
	 * 
	 * @param e
	 *            The exception to log; must not be <code>null</code>.
	 * @param command
	 *            The parameterized command for the binding to execute; may be
	 *            <code>null</code>.
	 */
	final void logException(final CommandException e,
			final ParameterizedCommand command) {
		Throwable nestedException = e.getCause();
		Throwable exception = (nestedException == null) ? e : nestedException;

		// If we can, include the command name in the exception.
		String message = null;
		if (command != null) {
			try {
				final String name = command.getCommand().getName();
				message = MessageFormat.format(Util.translateString(
						RESOURCE_BUNDLE, "ExecutionError.MessageCommandName"), //$NON-NLS-1$
						new Object[] { name });
			} catch (final NotDefinedException nde) {
				// Fall through (message == null)
			}
		}
		if (message == null) {
			message = Util.translateString(RESOURCE_BUNDLE,
					"ExecutionError.Message"); //$NON-NLS-1$
		}

		String exceptionMessage = exception.getMessage();
		if (exceptionMessage == null) {
			exceptionMessage = exception.getClass().getName();
		}
		IStatus status = new Status(IStatus.ERROR,
				WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
		WorkbenchPlugin.log(message, status);
		StatusUtil.handleStatus(message, exception, StatusManager.SHOW);
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
			keyAssistDialog.setParentShell(Util.getShellToParentOn());
		}
		keyAssistDialog.open();
	}

	/**
	 * Opens the key assist dialog to offer the user the choice of a binding to
	 * pick from the collection of bindings.
	 * 
	 * @param bindings
	 *            a collection of Binding objects
	 * @since 3.3
	 */
	public final void openKeyAssistShell(final Collection bindings) {
		if (keyAssistDialog == null) {
			keyAssistDialog = new KeyAssistDialog(workbench,
					WorkbenchKeyboard.this, state);
		}
		if (keyAssistDialog.getShell() == null) {
			keyAssistDialog.setParentShell(Util.getShellToParentOn());
		}
		keyAssistDialog.open(bindings);
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
	 */
	public boolean press(List potentialKeyStrokes, Event event) {
		if (DEBUG && DEBUG_VERBOSE) {
			Tracing.printTrace("KEYS", //$NON-NLS-1$
					"WorkbenchKeyboard.press(potentialKeyStrokes = " //$NON-NLS-1$
							+ potentialKeyStrokes + ')');
		}

		/*
		 * KLUDGE. This works around a couple of specific problems in how GTK+
		 * works. The first problem is the ordering of key press events with
		 * respect to shell activation events. If on the event thread a dialog
		 * is about to open, and the user presses a key, the key press event
		 * will arrive before the shell activation event. From the perspective
		 * of Eclipse, this means that things like two "Open Type" dialogs can
		 * appear if "Ctrl+Shift+T" is pressed twice rapidly. For more
		 * information, please see Bug 95792. The second problem is simply a bug
		 * in GTK+, for which an incomplete workaround currently exists in SWT.
		 * This makes shell activation events unreliable. Please see Bug 56231
		 * and Bug 95222 for more information.
		 */
		if ("gtk".equals(SWT.getPlatform())) { //$NON-NLS-1$
			final Widget widget = event.widget;

			// Update the contexts.
			final ContextService contextService = (ContextService) workbench
					.getService(IContextService.class);
			if ((widget instanceof Control) && (!widget.isDisposed())) {
				final Shell shell = ((Control) widget).getShell();
				contextService.updateShellKludge(shell);
			} else {
				contextService.updateShellKludge();
			}

			// Update the handlers.
			final HandlerService handlerService = (HandlerService) workbench
					.getService(IHandlerService.class);
			if ((widget instanceof Control) && (!widget.isDisposed())) {
				final Shell shell = ((Control) widget).getShell();
				handlerService.updateShellKludge(shell);
			} else {
				handlerService.updateShellKludge();
			}
		}

		KeySequence errorSequence = null;
		Collection errorMatch = null;

		KeySequence sequenceBeforeKeyStroke = state.getCurrentSequence();
		for (Iterator iterator = potentialKeyStrokes.iterator(); iterator
				.hasNext();) {
			KeySequence sequenceAfterKeyStroke = KeySequence.getInstance(
					sequenceBeforeKeyStroke, (KeyStroke) iterator.next());
			if (isPartialMatch(sequenceAfterKeyStroke)) {
				incrementState(sequenceAfterKeyStroke);
				return true;

			} else if (isPerfectMatch(sequenceAfterKeyStroke)) {
				final Binding binding = getPerfectMatch(sequenceAfterKeyStroke);
				try {
					return executeCommand(binding, event)
							|| !sequenceBeforeKeyStroke.isEmpty();
				} catch (final CommandException e) {
					logException(e, binding.getParameterizedCommand());
					return true;
				}

			} else if ((keyAssistDialog != null)
					&& (keyAssistDialog.getShell() != null)
					&& ((event.keyCode == SWT.ARROW_DOWN)
							|| (event.keyCode == SWT.ARROW_UP)
							|| (event.keyCode == SWT.ARROW_LEFT)
							|| (event.keyCode == SWT.ARROW_RIGHT)
							|| (event.keyCode == SWT.CR)
							|| (event.keyCode == SWT.PAGE_UP) || (event.keyCode == SWT.PAGE_DOWN))) {
				// We don't want to swallow keyboard navigation keys.
				return false;

			} else {
				Collection match = (InternalPolicy.currentConflicts == null ? null
						: (Collection) InternalPolicy.currentConflicts
								.get(sequenceAfterKeyStroke));
				if (match != null) {
					errorSequence = sequenceAfterKeyStroke;
					errorMatch = match;
				}
			}
		}

		resetState(true);
		if (sequenceBeforeKeyStroke.isEmpty() && errorSequence != null) {
			openKeyAssistShell(errorMatch);
		}
		return !sequenceBeforeKeyStroke.isEmpty();
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
			eatKey = press(keyStrokes, event);
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
