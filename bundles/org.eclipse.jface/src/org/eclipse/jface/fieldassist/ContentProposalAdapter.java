/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.fieldassist;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * ContentProposalAdapter can be used to attach content proposal behavior to a
 * control. This behavior includes obtaining proposals, opening a popup dialog,
 * managing the content of the control relative to the selections in the popup,
 * and optionally opening up a secondary popup to further describe proposals.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public class ContentProposalAdapter {

	/**
	 * Flag that controls the printing of debug info.
	 */
	public static final boolean DEBUG = false;

	/**
	 * Indicates that a chosen proposal should be inserted into the field.
	 */
	public static final int PROPOSAL_INSERT = 1;

	/**
	 * Indicates that a chosen proposal should replace the entire contents of
	 * the field.
	 */
	public static final int PROPOSAL_REPLACE = 2;

	/**
	 * Indicates that the contents of the control should not be modified when a
	 * proposal is chosen. This is typically used when a client needs more
	 * specialized behavior when a proposal is chosen. In this case, clients
	 * typically register an IContentProposalListener so that they are notified
	 * when a proposal is chosen.
	 */
	public static final int PROPOSAL_IGNORE = 3;

	/*
	 * The object that provides content proposals.
	 */
	private IContentProposalProvider proposalProvider;

	/*
	 * A label provider used to display proposals in the popup, and to extract
	 * Strings from non-String proposals.
	 */
	private ILabelProvider labelProvider;

	/*
	 * The control for which content proposals are provided.
	 */
	private Control control;

	/*
	 * The adapter used to extract the String contents from an arbitrary
	 * control.
	 */
	private IControlContentAdapter controlContentAdapter;

	/*
	 * The popup used to show proposals.
	 */
	private ContentProposalPopup popup;

	/*
	 * The keystroke that signifies content proposals should be shown.
	 */
	private KeyStroke triggerKeyStroke;

	/*
	 * The String containing characters that auto-activate the popup.
	 */
	private String autoActivateString;

	/*
	 * A flag indicating how an accepted proposal should affect the control. One
	 * of PROPOSAL_IGNORE, PROPOSAL_INSERT, or PROPOSAL_REPLACE.
	 */
	private int acceptance;

	/*
	 * A boolean that indicates whether keystrokes received while the proposal
	 * popup is open should also be propagated to the control.
	 */
	private boolean propagateKeyStroke;

	/*
	 * The listener we install on the control.
	 */
	private Listener controlListener;

	/*
	 * The list of listeners who wish to be notified when something significant
	 * happens with the proposals.
	 */
	private ListenerList proposalListeners = new ListenerList();

	/*
	 * Flag that indicates whether the adapter is enabled. In some cases,
	 * adapters may be installed but depend upon outside state.
	 */
	private boolean isEnabled = true;

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for the field.
	 * 
	 * @param control
	 *            the control for which the adapter is providing content assist.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and
	 *            update the control's contents as proposals are accepted.
	 * @param proposalProvider
	 *            the <code>IContentProposalProvider</code> used to obtain
	 *            content proposals for this control, or <code>null</code> if
	 *            no content proposal is available.
	 * @param labelProvider
	 *            the label provider which provides text and image information
	 *            for content proposals. A <code>null</code> value indicates
	 *            that a default label provider is sufficient for any content
	 *            proposals that may occur.
	 * @param keyStroke
	 *            the keystroke that will invoke the content proposal popup. If
	 *            this value is <code>null</code>, then proposals will be
	 *            activated automatically when any of the trigger characters are
	 *            typed.
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. This parameter is only consulted if the keyStroke
	 *            paramter is <code>null</code>. If the keyStroke parameter
	 *            is <code>null</code> and this keyStroke is null, then all
	 *            alphanumeric characters will be considered as auto-activating
	 *            characters.
	 * @param propagateKeyStroke
	 *            a boolean that indicates whether the triggering keystroke
	 *            should be propagated to the adapted control, as well as
	 *            subsequent keystrokes typed into the control when the proposal
	 *            popup is open.
	 * @param acceptance
	 *            a constant indicating how an accepted proposal should affect
	 *            the control's content. Should be one of
	 *            <code>PROPOSAL_INSERT</code>, <code>PROPOSAL_REPLACE</code>,
	 *            or <code>PROPOSAL_IGNORE</code>
	 * 
	 */
	public ContentProposalAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider,
			ILabelProvider labelProvider, KeyStroke keyStroke,
			char[] autoActivationCharacters, boolean propagateKeyStroke,
			int acceptance) {
		super();
		this.control = control;
		this.controlContentAdapter = controlContentAdapter;
		this.proposalProvider = proposalProvider;
		this.labelProvider = labelProvider;
		this.triggerKeyStroke = keyStroke;
		if (autoActivationCharacters != null)
			this.autoActivateString = new String(autoActivationCharacters);
		this.propagateKeyStroke = propagateKeyStroke;
		this.acceptance = acceptance;
		addControlListener(control);
	}

	/**
	 * Get the control on which the content proposal adapter is installed.
	 * 
	 * @return the control on which the proposal adapter is installed.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Get the label provider that is used to show proposals. A default label
	 * provider will be used if one has not been set.
	 * 
	 * @return the {@link ILabelProvider} used to show proposals
	 */
	public ILabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = new LabelProvider();
		}
		return labelProvider;
	}

	/**
	 * Set the label provider that is used to show proposals.
	 * 
	 * @param labelProvider
	 *            the (@link ILabelProvider} used to show proposals.
	 */
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Return the proposal provider that provides content proposals given the
	 * current content of the field. A value of <code>null</code> indicates
	 * that there are no content proposals available for the field.
	 * 
	 * @return the {@link IContentProposalProvider} used to show proposals. May
	 *         be <code>null</code>.
	 */
	public IContentProposalProvider getContentProposalProvider() {
		return proposalProvider;
	}

	/**
	 * Set the content proposal provider that is used to show proposals.
	 * 
	 * @param proposalProvider
	 *            the {@link IContentProposalProvider} used to show proposals
	 */
	public void setContentProposalProvider(
			IContentProposalProvider proposalProvider) {
		this.proposalProvider = proposalProvider;
	}

	/**
	 * Return the content adapter that can get or retrieve the text contents
	 * from the adapter's control. This method is used when a client, such as a
	 * content proposal listener, needs to update the control's contents
	 * manually.
	 * 
	 * @return the {@link IControlContentAdapter} which can update the control
	 *         text.
	 */
	public IControlContentAdapter getControlContentAdapter() {
		return controlContentAdapter;
	}

	/**
	 * Set the boolean flag that determines whether the adapter is enabled.
	 * 
	 * @param enabled
	 *            <code>true</code> if the adapter is enabled and responding
	 *            to user input, <code>false</code> if it is ignoring user
	 *            input.
	 * 
	 */
	public void setEnabled(boolean enabled) {
		// If we are disabling it while it's proposing content, close the
		// content proposal popup.
		if (isEnabled && !enabled) {
			if (popup != null)
				popup.close();
		}
		isEnabled = enabled;
	}

	/**
	 * Add the specified listener to the list of content proposal listeners that
	 * are notified when content proposals are chosen.
	 * </p>
	 * 
	 * @param listener
	 *            the IContentProposalListener to be added as a listener. Must
	 *            not be <code>null</code>. If an attempt is made to register
	 *            an instance which is already registered with this instance,
	 *            this method has no effect.
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposalListener
	 */
	public void addContentProposalListener(IContentProposalListener listener) {
		proposalListeners.add(listener);
	}

	/*
	 * Add our listener to the control. Debug information to be left in until
	 * this support is stable on all platforms.
	 */
	private void addControlListener(Control control) {
		if (DEBUG)
			System.out
					.println("ContentProposalListener#installControlListener()"); //$NON-NLS-1$

		if (controlListener != null)
			return;
		controlListener = new Listener() {
			public void handleEvent(Event e) {
				if (!isEnabled)
					return;

				switch (e.type) {
				case SWT.KeyDown:
					if (DEBUG)
						dump("keyDown OK", e); //$NON-NLS-1$
					if (triggerKeyStroke != null) {
						// Either there are no modifiers for the trigger and we
						// check the character field...
						if ((triggerKeyStroke.getModifierKeys() == KeyStroke.NO_KEY && triggerKeyStroke
								.getNaturalKey() == e.character)
								||
								// ...or there are modifiers, in which case the
								// keycode and state must match
								(triggerKeyStroke.getNaturalKey() == e.keyCode && ((triggerKeyStroke
										.getModifierKeys() & e.stateMask) == triggerKeyStroke
										.getModifierKeys()))) {
							e.doit = propagateKeyStroke;
							/*
							 * Open the popup in an async so that this keystroke
							 * finishes processing before the popup registers
							 * its own listeners.
							 */
							if (popup == null) {
								getControl().getDisplay().asyncExec(
										new Runnable() {
											public void run() {
												openProposalPopup();
											}
										});
							}
							return;
						}
					} else {
						/*
						 * The trigger keystroke is null, signifying
						 * auto-activation. We check for the specified
						 * auto-activation characters, or if none are specified,
						 * alphanumeric characters.
						 */
						if (e.character != 0) {
							boolean autoActivated = false;
							if (autoActivateString != null) {
							  if (autoActivateString.indexOf(e.character) >= 0) {
								  autoActivated = true;
							  }
							} else if (Character.isLetterOrDigit(e.character)) {
								autoActivated = true;
							}
							/*
							 * When autoactivating, we do not open the proposal popup in
							 * an async, because we want the target popup to process the key
							 * stroke.
							 */
							if (autoActivated) {
								e.doit = propagateKeyStroke;
								if (popup == null)
									openProposalPopup();
							}
						}
					}
					// If the popup is not open, we always propagate keys.
					// If the popup is open, consult the flag.
					e.doit = popup == null || propagateKeyStroke;
					break;

				default:
					break;
				}
			}

			/**
			 * Dump the given events to "standard" output.
			 * 
			 * @param who
			 *            who is dumping the event
			 * @param e
			 *            the event
			 */
			private void dump(String who, Event e) {
				StringBuffer sb = new StringBuffer(
						"--- [ContentProposalAdapter]\n"); //$NON-NLS-1$
				sb.append(who);
				sb.append(" - e: keyCode=" + e.keyCode + hex(e.keyCode)); //$NON-NLS-1$
				sb.append("; character=" + e.character + hex(e.character)); //$NON-NLS-1$
				sb.append("; stateMask=" + e.stateMask + hex(e.stateMask)); //$NON-NLS-1$
				sb.append("; doit=" + e.doit); //$NON-NLS-1$
				sb.append("; detail=" + e.detail + hex(e.detail)); //$NON-NLS-1$
				sb.append("; widget=" + e.widget); //$NON-NLS-1$
				System.out.println(sb);
			}

			private String hex(int i) {
				return "[0x" + Integer.toHexString(i) + ']'; //$NON-NLS-1$
			}
		};
		control.addListener(SWT.KeyDown, controlListener);

		if (DEBUG)
			System.out
					.println("ContentProposalAdapter#installControlListener() - installed"); //$NON-NLS-1$
	}

	/*
	 * Open the proposal popup.
	 */
	private void openProposalPopup() {
		if (popup == null) {
			popup = new ContentProposalPopup(this, control, proposalProvider,
					null, labelProvider);
			popup.open();
			popup.getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					popup = null;
				}
			});
		}
	}

	/**
	 * A content proposal has been accepted. Update the control contents
	 * accordingly and notify any listeners.
	 * 
	 * @param text
	 *            the text that was accepted as the proposal.
	 */
	public void proposalAccepted(String text) {
		switch (acceptance) {
		case (PROPOSAL_REPLACE):
			setControlContent(text);
			break;
		case (PROPOSAL_INSERT):
			insertControlContent(text);
			break;
		default:
			// do nothing. Typically a listener is installed to handle this in
			// a custom way.
			break;
		}

		// In all cases, notify listeners of an accepted proposal.
		final Object[] listenerArray = proposalListeners.getListeners();
		for (int i = 0; i < listenerArray.length; i++)
			((IContentProposalListener) listenerArray[i])
					.proposalAccepted(text);
	}

	/**
	 * Return the text content of the control.
	 * 
	 * @return the String contents of the control. Never <code>null</code>.
	 */
	public String getControlContent() {
		if (isValid())
			return controlContentAdapter.getControlContents(control);
		return ""; //$NON-NLS-1$
	}

	/*
	 * Set the text content of the control to the specified text.
	 */
	private void setControlContent(String text) {
		if (isValid())
			controlContentAdapter.setControlContents(control, text);
	}

	/*
	 * Insert the specified text into the control content.
	 */
	private void insertControlContent(String text) {
		if (isValid())
			controlContentAdapter.insertControlContents(control, text);
	}

	/*
	 * Check that the control and content adapter are valid.
	 */
	private boolean isValid() {
		return control != null && !control.isDisposed()
				&& controlContentAdapter != null;
	}
}
