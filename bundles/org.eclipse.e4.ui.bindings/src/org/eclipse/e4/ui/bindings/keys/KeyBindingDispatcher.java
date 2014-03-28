/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.bindings.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.KeyAssistDialog;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * <p>
 * Controls the keyboard input into the workbench key binding architecture. This allows key events
 * to be programmatically pushed into the key binding architecture -- potentially triggering the
 * execution of commands. It is used by the <code>e4 Workbench</code> to listen for events on the
 * <code>Display</code>.
 * </p>
 */
public class KeyBindingDispatcher {

	private KeyAssistDialog keyAssistDialog = null;

	/**
	 * A display filter for handling key bindings. This filter can either be enabled or disabled. If
	 * disabled, the filter does not process incoming events. The filter starts enabled.
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
		@Override
		public final void handleEvent(final Event event) {
			if (!enabled) {
				return;
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

	/** The collection of keys that are to be processed out-of-order. */
	static KeySequence outOfOrderKeys;

	static {

		try {
			outOfOrderKeys = KeySequence.getInstance("ESC DEL"); //$NON-NLS-1$
		} catch (ParseException e) {
			outOfOrderKeys = KeySequence.getInstance();
			// String message = "Could not parse out-of-order keys definition: 'ESC DEL'.  Continuing with no out-of-order keys."; //$NON-NLS-1$
			// TODO we need to do some logging here
		}
	}

	/**
	 * Generates any key strokes that are near matches to the given event. The first such key stroke
	 * is always the exactly matching key stroke.
	 * 
	 * @param event
	 *            The event from which the key strokes should be generated; must not be
	 *            <code>null</code>.
	 * @return The set of nearly matching key strokes. It is never <code>null</code>, but may be
	 *         empty.
	 */
	public static List<KeyStroke> generatePossibleKeyStrokes(Event event) {
		final List<KeyStroke> keyStrokes = new ArrayList<KeyStroke>(3);

		/*
		 * If this is not a keyboard event, then there are no key strokes. This can happen if we are
		 * listening to focus traversal events.
		 */
		if ((event.stateMask == 0) && (event.keyCode == 0) && (event.character == 0)) {
			return keyStrokes;
		}

		// Add each unique key stroke to the list for consideration.
		final int firstAccelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
		keyStrokes.add(SWTKeySupport.convertAcceleratorToKeyStroke(firstAccelerator));

		// We shouldn't allow delete to undergo shift resolution.
		if (event.character == SWT.DEL) {
			return keyStrokes;
		}

		final int secondAccelerator = SWTKeySupport
				.convertEventToUnshiftedModifiedAccelerator(event);
		if (secondAccelerator != firstAccelerator) {
			keyStrokes.add(SWTKeySupport.convertAcceleratorToKeyStroke(secondAccelerator));
		}

		final int thirdAccelerator = SWTKeySupport.convertEventToModifiedAccelerator(event);
		if ((thirdAccelerator != secondAccelerator) && (thirdAccelerator != firstAccelerator)) {
			keyStrokes.add(SWTKeySupport.convertAcceleratorToKeyStroke(thirdAccelerator));
		}

		return keyStrokes;
	}

	/**
	 * <p>
	 * Determines whether the given event represents a key press that should be handled as an
	 * out-of-order event. An out-of-order key press is one that is passed to the focus control
	 * first. Only if the focus control fails to respond will the regular key bindings get applied.
	 * </p>
	 * <p>
	 * Care must be taken in choosing which keys are chosen as out-of-order keys. This method has
	 * only been designed and test to work with the unmodified "Escape" key stroke.
	 * </p>
	 * 
	 * @param keyStrokes
	 *            The key stroke in which to look for out-of-order keys; must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the key is an out-of-order key; <code>false</code> otherwise.
	 */
	private static boolean isOutOfOrderKey(List<KeyStroke> keyStrokes) {
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
	 * The time in milliseconds to wait after pressing a key before displaying the key assist
	 * dialog.
	 */
	private static final int DELAY = 1000;

	private EBindingService bindingService;

	private IEclipseContext context;

	private EHandlerService handlerService;

	/**
	 * The listener that runs key events past the global key bindings.
	 */
	private final KeyDownFilter keyDownFilter = new KeyDownFilter();

	/**
	 * The single out-of-order listener used by the workbench. This listener is attached to one
	 * widget at a time, and is used to catch key down events after all processing is done. This
	 * technique is used so that some keys will have their native behaviour happen first.
	 * 
	 * @since 3.1
	 */
	private final OutOfOrderListener outOfOrderListener = new OutOfOrderListener(this);

	/**
	 * The single out-of-order verify listener used by the workbench. This listener is attached to
	 * one</code> StyledText</code> at a time, and is used to catch verify events after all
	 * processing is done. This technique is used so that some keys will have their native behaviour
	 * happen first.
	 * 
	 * @since 3.1
	 */
	private final OutOfOrderVerifyListener outOfOrderVerifyListener = new OutOfOrderVerifyListener(
			outOfOrderListener);

	/**
	 * The mode is the current state of the key binding architecture. In the case of multi-stroke
	 * key bindings, this can be a partially complete key binding.
	 */
	private KeySequence state = KeySequence.getInstance();

	private long startTime;

	@Inject
	@Optional
	private Logger logger;

	/**
	 * Performs the actual execution of the command by looking up the current handler from the
	 * command manager. If there is a handler and it is enabled, then it tries the actual execution.
	 * Execution failures are logged. When this method completes, the key binding state is reset.
	 * 
	 * @param parameterizedCommand
	 *            The command that should be executed; should not be <code>null</code>.
	 * @param trigger
	 *            The triggering event; may be <code>null</code>.
	 * @return <code>true</code> if there was a handler; <code>false</code> otherwise.
	 * @throws CommandException
	 *             if the handler does not complete execution for some reason. It is up to the
	 *             caller of this method to decide whether to log the message, display a dialog, or
	 *             ignore this exception entirely.
	 */
	public final boolean executeCommand(final ParameterizedCommand parameterizedCommand,
			final Event trigger) throws CommandException {

		// Reset the key binding state (close window, clear status line, etc.)
		resetState(false);

		final EHandlerService handlerService = getHandlerService();
		final Command command = parameterizedCommand.getCommand();

		final IEclipseContext staticContext = EclipseContextFactory.create("keys-staticContext"); //$NON-NLS-1$
		staticContext.set(Event.class, trigger);

		final boolean commandDefined = command.isDefined();
		// boolean commandEnabled;
		boolean commandHandled = false;

		try {
			// commandEnabled = handlerService.canExecute(parameterizedCommand, staticContext);
			Object obj = HandlerServiceImpl.lookUpHandler(context, command.getId());
			if (obj != null) {
				if (obj instanceof IHandler) {
					commandHandled = ((IHandler) obj).isHandled();
				} else {
					commandHandled = true;
				}
			}

			handlerService.executeHandler(parameterizedCommand, staticContext);
			final Object commandException = staticContext.get(HandlerServiceImpl.HANDLER_EXCEPTION);
			if (commandException instanceof CommandException) {
				commandHandled = false;
				if (logger != null && commandException instanceof ExecutionException) {
					logger.error((Throwable) commandException);
				}
			}
			/*
			 * Now that the command has executed (and had the opportunity to use the remembered
			 * state of the dialog), it is safe to delete that information.
			 */
			if (keyAssistDialog != null) {
				keyAssistDialog.clearRememberedState();
			}
		} finally {
			staticContext.dispose();
		}
		return (commandDefined && commandHandled);
	}

	/**
	 * <p>
	 * Launches the command matching a the typed key. This filter an incoming
	 * <code>SWT.KeyDown</code> or <code>SWT.Traverse</code> event at the level of the display
	 * (i.e., before it reaches the widgets). It does not allow processing in a dialog or if the key
	 * strokes does not contain a natural key.
	 * </p>
	 * <p>
	 * Some key strokes (defined as a property) are declared as out-of-order keys. This means that
	 * they are processed by the widget <em>first</em>. Only if the other widget listeners do no
	 * useful work does it try to process key bindings. For example, "ESC" can cancel the current
	 * widget action, if there is one, without triggering key bindings.
	 * </p>
	 * 
	 * @param event
	 *            The incoming event; must not be <code>null</code>.
	 */
	private void filterKeySequenceBindings(Event event) {
		/*
		 * Only process key strokes containing natural keys to trigger key bindings.
		 */
		if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
			return;
		}

		// Allow special key out-of-order processing.
		List<KeyStroke> keyStrokes = generatePossibleKeyStrokes(event);
		if (isOutOfOrderKey(keyStrokes)) {
			Widget widget = event.widget;
			if ((event.character == SWT.DEL)
					&& ((event.stateMask & SWT.MODIFIER_MASK) == 0)
					&& ((widget instanceof Text) || (widget instanceof Combo)
							|| (widget instanceof Browser) || (widget instanceof CCombo))) {
				/*
				 * KLUDGE. Bug 54654. The text widget relies on no listener doing any work before
				 * dispatching the native delete event. This does not work, as we are restricted to
				 * listeners. However, it can be said that pressing a delete key in a text widget
				 * will never use key bindings. This can be shown be considering how the event
				 * dispatching is expected to work in a text widget. So, we should do nothing ...
				 * ever.
				 */
				return;

			} else if (widget instanceof StyledText) {

				if (event.type == SWT.KeyDown) {
					/*
					 * KLUDGE. Some people try to do useful work in verify listeners. The way verify
					 * listeners work in SWT, we need to verify the key as well; otherwise, we can't
					 * detect that useful work has been done.
					 */
					if (!outOfOrderVerifyListener.isActive(event.time)) {
						((StyledText) widget).addVerifyKeyListener(outOfOrderVerifyListener);
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
			 * Otherwise, we count on a key down arriving eventually. Expecting out of order
			 * handling on Ctrl+Tab, for example, is a bad idea (stick to keys that are not window
			 * traversal keys).
			 */

		} else {
			processKeyEvent(keyStrokes, event);
		}
	}

	private EBindingService getBindingService() {
		if (bindingService == null) {
			bindingService = (EBindingService) context.get(EBindingService.class.getName());
		}
		return bindingService;
	}

	private EHandlerService getHandlerService() {
		if (handlerService == null) {
			handlerService = (EHandlerService) context.get(EHandlerService.class.getName());
		}
		return handlerService;
	}

	private Display getDisplay() {
		return Display.getCurrent();
	}

	/**
	 * An accessor for the filter that processes key down and traverse events on the display.
	 * 
	 * @return The global key down and traverse filter; never <code>null</code>.
	 */
	public KeyDownFilter getKeyDownFilter() {
		return keyDownFilter;
	}

	/**
	 * Determines whether the key sequence is a perfect match for any command. If there is a match,
	 * then the corresponding command identifier is returned.
	 * 
	 * @param keySequence
	 *            The key sequence to check for a match; must never be <code>null</code>.
	 * @return The perfectly matching command; <code>null</code> if no command matches.
	 */
	private ParameterizedCommand getPerfectMatch(KeySequence keySequence) {
		Binding perfectMatch = getBindingService().getPerfectMatch(keySequence);
		return perfectMatch == null ? null : perfectMatch.getParameterizedCommand();
	}

	/**
	 * Changes the key binding state to the given value. This should be an incremental change, but
	 * there are no checks to guarantee this is so. It also sets up a <code>Shell</code> to be
	 * displayed after one second has elapsed. This shell will show the user the possible
	 * completions for what they have typed.
	 * 
	 * @param sequence
	 *            The new key sequence for the state; should not be <code>null</code>.
	 */
	private void incrementState(final KeySequence sequence) {
		state = sequence;
		// Record the starting time.
		startTime = System.currentTimeMillis();
		final long myStartTime = startTime;
		final Display display = getDisplay();
		display.timerExec(DELAY, new Runnable() {
			@Override
			public void run() {
				if ((System.currentTimeMillis() > (myStartTime - DELAY))
						&& (startTime == myStartTime)) {
					Collection<Binding> partialMatches = bindingService.getPartialMatches(sequence);
					openKeyAssistShell(partialMatches);
				}
			}
		});

	}

	/**
	 * Opens a <code>KeyAssistDialog</code> to assist the user in completing a multi-stroke key
	 * binding. This method lazily creates a <code>keyAssistDialog</code> and shares it between
	 * executions.
	 */
	private final void openKeyAssistShell(final Collection<Binding> bindings) {
		if (keyAssistDialog == null) {
			keyAssistDialog = new KeyAssistDialog(context, this);
		}
		if (keyAssistDialog.getShell() == null) {
			keyAssistDialog.setParentShell(getDisplay().getActiveShell());
		}
		keyAssistDialog.open(bindings);
	}

	/**
	 * Determines whether the key sequence partially matches on of the active key bindings.
	 * 
	 * @param keySequence
	 *            The key sequence to check for a partial match; must never be <code>null</code>.
	 * @return <code>true</code> if there is a partial match; <code>false</code> otherwise.
	 */
	private boolean isPartialMatch(KeySequence keySequence) {
		return getBindingService().isPartialMatch(keySequence);
	}

	/**
	 * Determines whether the key sequence perfectly matches on of the active key bindings.
	 * 
	 * @param keySequence
	 *            The key sequence to check for a perfect match; must never be <code>null</code>.
	 * @return <code>true</code> if there is a perfect match; <code>false</code> otherwise.
	 */
	private boolean isPerfectMatch(KeySequence keySequence) {
		return getBindingService().isPerfectMatch(keySequence);
	}

	/**
	 * @param potentialKeyStrokes
	 * @param event
	 * @return
	 */
	public boolean press(List<KeyStroke> potentialKeyStrokes, Event event) {
		KeySequence errorSequence = null;
		Collection<Binding> errorMatch = null;

		KeySequence sequenceBeforeKeyStroke = state;
		for (Iterator<KeyStroke> iterator = potentialKeyStrokes.iterator(); iterator.hasNext();) {
			KeySequence sequenceAfterKeyStroke = KeySequence.getInstance(sequenceBeforeKeyStroke,
					iterator.next());
			if (isPartialMatch(sequenceAfterKeyStroke)) {
				incrementState(sequenceAfterKeyStroke);
				return true;

			} else if (isPerfectMatch(sequenceAfterKeyStroke)) {
				final ParameterizedCommand cmd = getPerfectMatch(sequenceAfterKeyStroke);
				try {
					return executeCommand(cmd, event) || !sequenceBeforeKeyStroke.isEmpty();
				} catch (final CommandException e) {
					return true;
				}

			} else if ((keyAssistDialog != null)
					&& (keyAssistDialog.getShell() != null)
					&& ((event.keyCode == SWT.ARROW_DOWN) || (event.keyCode == SWT.ARROW_UP)
							|| (event.keyCode == SWT.ARROW_LEFT)
							|| (event.keyCode == SWT.ARROW_RIGHT) || (event.keyCode == SWT.CR)
							|| (event.keyCode == SWT.PAGE_UP) || (event.keyCode == SWT.PAGE_DOWN))) {
				// We don't want to swallow keyboard navigation keys.
				return false;

			} else {
				Collection<Binding> matches = getBindingService().getConflictsFor(
						sequenceAfterKeyStroke);
				if (matches != null && !matches.isEmpty()) {
					errorSequence = sequenceAfterKeyStroke;
					errorMatch = matches;
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
	 * <code>ICommandManager</code>. If work is carried out, then the event is stopped here (i.e.,
	 * <code>event.doit = false</code>). It does not do any processing if there are no matching key
	 * strokes.
	 * </p>
	 * <p>
	 * If the active <code>Shell</code> is not the same as the one to which the state is associated,
	 * then a reset occurs.
	 * </p>
	 * 
	 * @param keyStrokes
	 *            The set of all possible matching key strokes; must not be <code>null</code>.
	 * @param event
	 *            The event to process; must not be <code>null</code>.
	 */
	void processKeyEvent(List<KeyStroke> keyStrokes, Event event) {
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

	public void resetState() {
		resetState(true);
	}

	private void resetState(boolean clearRememberedState) {
		startTime = Long.MAX_VALUE;
		state = KeySequence.getInstance();
		closeMultiKeyAssistShell();
		if (keyAssistDialog != null && clearRememberedState) {
			keyAssistDialog.clearRememberedState();
		}
	}

	final public KeySequence getBuffer() {
		return state;
	}

	@Inject
	public void setContext(IEclipseContext context) {
		this.context = context;
	}

	/**
	 * Closes the multi-stroke key binding assistant shell, if it exists and isn't already disposed.
	 */
	private void closeMultiKeyAssistShell() {
		if (keyAssistDialog != null) {
			final Shell shell = keyAssistDialog.getShell();
			if ((shell != null) && (!shell.isDisposed()) && (shell.isVisible())) {
				keyAssistDialog.close(true);
			}
		}
	}

}
