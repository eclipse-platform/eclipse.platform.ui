/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.KeySupport;
import org.eclipse.ui.keys.ParseException;

import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.commands.CommandManager;
import org.eclipse.ui.internal.util.StatusLineContributionItem;

/**
 * <p>
 * Controls the keyboard input into the workbench key binding architecture.
 * This allows key events to be programmatically pushed into the key binding
 * architecture -- potentially triggering the execution of commands. It is used
 * by the <code>Workbench</code> to listen for events on the <code>Display</code>.
 * </p>
 * <p>
 * This class is not designed to be thread-safe. It is assumed that all access
 * to the <code>press</code> method is done through the event loop. Accessing
 * this method outside the event loop can cause corruption of internal state.
 * </p>
 * 
 * @since 3.0
 */
public class WorkbenchKeyboard {

	static {
		initializeOutOfOrderKeys();
	}

	/**
	 * The properties key for the key strokes that should be processed out of
	 * order.
	 */
	static final String OUT_OF_ORDER_KEYS = "OutOfOrderKeys"; //$NON-NLS-1$
	/** The collection of keys that are to be processed out-of-order. */
	static KeySequence outOfOrderKeys;

	/**
	 * Generates any key strokes that are near matches to the given event. The
	 * first such key stroke is always the exactly matching key stroke.
	 * 
	 * @param event
	 *            The event from which the key strokes should be generated;
	 *            must not be <code>null</code>.
	 * @return The set of nearly matching key strokes. It is never <code>null</code>
	 *         and never empty.
	 */
	public static List generatePossibleKeyStrokes(Event event) {
		List keyStrokes = new ArrayList();
		KeyStroke keyStroke;

		keyStrokes.add(
			KeySupport.convertAcceleratorToKeyStroke(
				KeySupport.convertEventToUnmodifiedAccelerator(event)));
		keyStroke =
			KeySupport.convertAcceleratorToKeyStroke(
				KeySupport.convertEventToUnshiftedModifiedAccelerator(event));
		if (!keyStrokes.contains(keyStroke)) {
			keyStrokes.add(keyStroke);
		}
		keyStroke =
			KeySupport.convertAcceleratorToKeyStroke(
				KeySupport.convertEventToModifiedAccelerator(event));
		if (!keyStrokes.contains(keyStroke)) {
			keyStrokes.add(keyStroke);
		}
		return keyStrokes;
	}

	/**
	 * Initializes the <code>outOfOrderKeys</code> member variable using the
	 * keys defined in the properties file.
	 */
	private static void initializeOutOfOrderKeys() {
		// Get the key strokes which should be out of order.
		String keysText = WorkbenchMessages.getString(OUT_OF_ORDER_KEYS);
		outOfOrderKeys = KeySequence.getInstance();
		try {
			outOfOrderKeys = KeySequence.getInstance(keysText);
		} catch (ParseException e) {
			String message = "Could not parse out-of-order keys definition: '" + keysText + "'.  Continuing with no out-of-order keys."; //$NON-NLS-1$ //$NON-NLS-2$
			WorkbenchPlugin.log(
				message,
				new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e));
		}
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
	 * @return <code>true</code> if the key is an out-of-order key; <code>false</code>
	 *         otherwise.
	 */
	private static boolean isOutOfOrderKey(List keyStrokes) {
		// Compare to see if one of the possible key strokes is out of
		// order.
		Iterator keyStrokeItr = keyStrokes.iterator();
		while (keyStrokeItr.hasNext()) {
			if (outOfOrderKeys.getKeyStrokes().contains(keyStrokeItr.next())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * The listener that runs key events past the global key bindings.
	 */
	final Listener keySequenceBindingFilter = new Listener() {
		public void handleEvent(Event event) {
			filterKeySequenceBindings(event);
		}
	};

	/**
	 * The mode is the current state of the key binding architecture. In the
	 * case of multi-stroke key bindings, this can be a partially complete key
	 * binding.
	 */
	private KeySequence mode = KeySequence.getInstance();

	/**
	 * The listener that clears the mode during focus changes.
	 */
	final Listener modeCleaner = new Listener() {
		public void handleEvent(Event event) {
			setMode(KeySequence.getInstance());
		}
	};

	/**
	 * The listener that allows out-of-order key processing to hook back into
	 * the global key bindings.
	 */
	final OutOfOrderListener outOfOrderListener = new OutOfOrderListener(this);

	/**
	 * The listener that allows out-of-order key processing on <code>StyledText</code>
	 * widgets to detect useful work in a verify key listener.
	 */
	final OutOfOrderVerifyListener outOfOrderVerifyListener =
		new OutOfOrderVerifyListener(outOfOrderListener);

	/**
	 * The workbench on which this keyboard interface should act.
	 */
	final Workbench workbench;

	/**
	 * Constructs a new instance of <code>WorkbenchKeyboard</code> associated
	 * with a particular workbench.
	 * 
	 * @param associatedWorkbench
	 *            The workbench with which this keyboard interface should work.
	 */
	public WorkbenchKeyboard(Workbench associatedWorkbench) {
		workbench = associatedWorkbench;
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
		 * bindings
		 */
		if ((event.keyCode & SWT.MODIFIER_MASK) != 0)
			return;

		// Don't allow dialogs to process key bindings.
		if (event.widget instanceof Control) {
			Shell shell = ((Control) event.widget).getShell();
			if (shell.getParent() != null)
				return;
		}

		// Allow special key out-of-order processing.
		List keyStrokes = generatePossibleKeyStrokes(event);
		if (isOutOfOrderKey(keyStrokes)) {
			if (event.type == SWT.KeyDown) {
				Widget widget = event.widget;
				if (widget instanceof StyledText) {
					/*
					 * KLUDGE. Some people try to do useful work in verify
					 * listeners. The way verify listeners work in SWT, we need
					 * to verify the key as well; otherwise, we can detect that
					 * useful work has been done.
					 */
					 ((StyledText) widget).addVerifyKeyListener(outOfOrderVerifyListener);
				} else {
					widget.addListener(SWT.KeyDown, outOfOrderListener);
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
	 * An accessor for the filter that processes key events on the display.
	 * 
	 * @return The global key binding filter; never <code>null</code>.
	 */
	public Listener getFilter() {
		return keySequenceBindingFilter;
	}

	/**
	 * The current internal state of the key binding architecture, which
	 * represents the partial key sequence entered by the user.
	 * 
	 * @return The current partial match entered by the user; never <code>null</code>,
	 *         but may contain no strokes.
	 */
	private KeySequence getMode() {
		return mode;
	}

	/**
	 * Determines whether the key sequence is a perfect match for any command.
	 * If there is a match, then the corresponding command identifier is
	 * returned.
	 * 
	 * @param keySequence
	 *            The key sequence to check for a match; must never be <code>null</code>.
	 * @return The command identifier for the perfectly matching command;
	 *         <code>null</code> if no command matches.
	 */
	private String getPerfectMatch(KeySequence keySequence) {
		return workbench.getCommandManager().getPerfectMatch(keySequence);
	}

	/**
	 * Determines whether the key sequence partially matches on of the active
	 * key bindings.
	 * 
	 * @param keySequence
	 *            The key sequence to check for a partial match; must never be
	 *            <code>null</code>.
	 * @return <code>true</code> if there is a partial match; <code>false</code>
	 *         otherwise.
	 */
	private boolean isPartialMatch(KeySequence keySequence) {
		return workbench.getCommandManager().isPartialMatch(keySequence);
	}

	/**
	 * Determines whether the key sequence perfectly matches on of the active
	 * key bindings.
	 * 
	 * @param keySequence
	 *            The key sequence to check for a perfect match; must never be
	 *            <code>null</code>.
	 * @return <code>true</code> if there is a perfect match; <code>false</code>
	 *         otherwise.
	 */
	private boolean isPerfectMatch(KeySequence keySequence) {
		return workbench.getCommandManager().isPerfectMatch(keySequence);
	}

	/**
	 * Processes a key press with respect to the key binding architecture. This
	 * updates the mode of the command manager, and runs the current handler
	 * for the command that matches the key sequence, if any.
	 * 
	 * @param potentialKeyStrokes
	 *            The key strokes that could potentially match, in the order of
	 *            priority; must not be <code>null</code>.
	 * @param event
	 *            The event to pass to the action; may be <code>null</code>.
	 * @return <code>true</code> if a command is executed; <code>false</code>
	 *         otherwise.
	 */
	// TODO remove event parameter once key-modified actions are removed
	public boolean press(List potentialKeyStrokes, Event event) {
		KeySequence modeBeforeKeyStroke = getMode();

		for (Iterator iterator = potentialKeyStrokes.iterator(); iterator.hasNext();) {
			KeySequence modeAfterKeyStroke =
				KeySequence.getInstance(modeBeforeKeyStroke, (KeyStroke) iterator.next());

			if (isPartialMatch(modeAfterKeyStroke)) {
				setMode(modeAfterKeyStroke);
				return true;

			} else if (isPerfectMatch(modeAfterKeyStroke)) {
				String commandId = getPerfectMatch(modeAfterKeyStroke);
				Map actionsById = ((CommandManager) workbench.getCommandManager()).getActionsById();
				org.eclipse.ui.commands.IAction action =
					(org.eclipse.ui.commands.IAction) actionsById.get(commandId);

				if (action != null && action.isEnabled()) {
					try {
						action.execute(event);
					} catch (Exception e) {
						String message = "Action for command '" + commandId + "' failed to execute properly."; //$NON-NLS-1$ //$NON-NLS-2$
						WorkbenchPlugin.log(
							message,
							new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e));
					}
				}

				setMode(KeySequence.getInstance());
				return action != null || modeBeforeKeyStroke.isEmpty();

			}
		}

		setMode(KeySequence.getInstance());
		return false;
	}

	/**
	 * Actually performs the processing of the key event by interacting with
	 * the <code>ICommandManager</code>. If work is carried out, then the
	 * event is stopped here (i.e., <code>event.doit = false</code>).
	 * 
	 * @param keyStrokes
	 *            The set of all possible matching key strokes; must not be
	 *            <code>null</code>.
	 * @param event
	 *            The event to process; must not be <code>null</code>.
	 */
	void processKeyEvent(List keyStrokes, Event event) {
		if (press(keyStrokes, event)) {
			switch (event.type) {
				case SWT.KeyDown :
					event.doit = false;
					break;
				case SWT.Traverse :
					event.detail = SWT.TRAVERSE_NONE;
					event.doit = true;
					break;
				default :
					}

			event.type = SWT.NONE;
		}
	}

	/**
	 * A mutator for the current internal key binding state.
	 * 
	 * @param sequence
	 *            The current key sequence
	 */
	private void setMode(KeySequence sequence) {
		if (mode == null)
			throw new NullPointerException();

		mode = sequence;
		updateModeStatusLines();
	}

	/**
	 * Updates the text of the given window's mode line with the given text.
	 * 
	 * @param window
	 *            the window
	 * @param text
	 *            the text
	 */
	private void updateModeLine(IWorkbenchWindow window, String text) {
		if (window instanceof WorkbenchWindow) {
			IStatusLineManager statusLine = ((WorkbenchWindow) window).getStatusLineManager();
			// @issue implicit dependency on IDE's action builder
			IContributionItem item = statusLine.find("ModeContributionItem"); //$NON-NLS-1$
			if (item instanceof StatusLineContributionItem) {
				((StatusLineContributionItem) item).setText(text);
			}
		}
	}

	/**
	 * Updates the text of the mode lines with the current mode.
	 */
	private void updateModeStatusLines() {
		// Format the mode into text.
		String text = getMode().format();

		// Update each open window's status line.
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			updateModeLine(windows[i], text);
		}
	}
}
