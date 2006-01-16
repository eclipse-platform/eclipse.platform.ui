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
import org.eclipse.jface.util.Assert;
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
	 * A boolean that indicates whether keys events received while the proposal
	 * popup is open should also be propagated to the control.
	 */
	private boolean propagateKeys;

	/*
	 * A boolean that indicates whether proposals should be filtered as keys are
	 * typed when the popup is open.
	 */
	private boolean filterProposals;

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

	/*
	 * The delay in milliseconds used when autoactivating the popup.
	 */
	private int autoActivationDelay = 0;

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for the field.
	 * 
	 * @param control
	 *            the control for which the adapter is providing content assist.
	 *            May not be <code>null</code>.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and
	 *            update the control's contents as proposals are accepted. May
	 *            not be <code>null</code>.
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
	 *            activated automatically when any of the auto activation
	 *            characters are typed.
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of whether
	 *            an explicit invocation keyStroke was specified. If this
	 *            parameter is <code>null</code>, then only a specified
	 *            keyStroke will invoke content proposal. If this parameter is
	 *            <code>null</code> and the keyStroke parameter is
	 *            <code>null</code>, then all alphanumeric characters will
	 *            auto-activate content proposal.
	 * @param propagateKeys
	 *            a boolean that indicates whether key events (including
	 *            auto-activation characters) should be propagated to the
	 *            adapted control when the proposal popup is open.
	 * @param filterProposals
	 *            a boolean that indicates whether the proposal popup should
	 *            filter its contents based on keys typed when it is open
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
			char[] autoActivationCharacters, boolean propagateKeys,
			boolean filterProposals, int acceptance) {
		super();
		// We always assume the control and content adapter are valid.
		Assert.isNotNull(control);
		Assert.isNotNull(controlContentAdapter);
		this.control = control;
		this.controlContentAdapter = controlContentAdapter;

		// The rest of these may be null
		this.proposalProvider = proposalProvider;
		this.labelProvider = labelProvider;
		this.triggerKeyStroke = keyStroke;
		if (autoActivationCharacters != null)
			this.autoActivateString = new String(autoActivationCharacters);
		this.propagateKeys = propagateKeys;
		this.filterProposals = filterProposals;
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
	 * Return the array of characters on which the popup is autoactivated.
	 * 
	 * @return An array of characters that trigger auto-activation of content
	 *         proposal. If specified, these characters will trigger
	 *         auto-activation of the proposal popup, regardless of whether an
	 *         explicit invocation keyStroke was specified. If this parameter is
	 *         <code>null</code>, then only a specified keyStroke will invoke
	 *         content proposal. If this value is <code>null</code> and the
	 *         keyStroke value is <code>null</code>, then all alphanumeric
	 *         characters will auto-activate content proposal.
	 */
	public char[] getAutoActivationCharacters() {
		if (autoActivateString == null)
			return null;
		return autoActivateString.toCharArray();
	}

	/**
	 * Set the array of characters that will trigger autoactivation of the
	 * popup.
	 * 
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of whether
	 *            an explicit invocation keyStroke was specified. If this
	 *            parameter is <code>null</code>, then only a specified
	 *            keyStroke will invoke content proposal. If this parameter is
	 *            <code>null</code> and the keyStroke value is
	 *            <code>null</code>, then all alphanumeric characters will
	 *            auto-activate content proposal.
	 * 
	 */
	public void setAutoActivationCharacters(char[] autoActivationCharacters) {
		this.autoActivateString = new String(autoActivationCharacters);
	}

	/**
	 * Set the delay, in milliseconds, used before any autoactivation is
	 * triggered.
	 * 
	 * @return the time in milliseconds that will pass before a popup is
	 *         automatically opened
	 */
	public int getAutoActivationDelay() {
		return autoActivationDelay;

	}

	/**
	 * Set the delay, in milliseconds, used before autoactivation is triggered.
	 * 
	 * @param delay
	 *            the time in milliseconds that will pass before a popup is
	 *            automatically opened
	 */
	public void setAutoActivationDelay(int delay) {
		autoActivationDelay = delay;

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
							// We never propagate an explicit keystroke
							// invocation
							e.doit = false;
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
					}
					/*
					 * The triggering keystroke was not invoked. Check for
					 * autoactivation characters.
					 */
					if (e.character != 0) {
						boolean autoActivated = false;
						// Auto-activation characters were specified. Check
						// them.
						if (autoActivateString != null) {
							if (autoActivateString.indexOf(e.character) >= 0) {
								autoActivated = true;
							}
							// Auto-activation characters were not specified. If
							// there was no key stroke specified, assume
							// activation for alphanumeric characters.
						} else if (triggerKeyStroke == null
								&& Character.isLetterOrDigit(e.character)) {
							autoActivated = true;
						}
						/*
						 * When autoactivating, we check the autoactivation
						 * delay.
						 */
						if (autoActivated) {
							e.doit = propagateKeys;
							if (popup == null) {
								if (autoActivationDelay > 0) {
									Runnable runnable = new Runnable() {
										public void run() {
											try {
												Thread
														.sleep(autoActivationDelay);
											} catch (InterruptedException e) {
											}
											if (!isValid())
												return;
											getControl().getDisplay().syncExec(
													new Runnable() {
														public void run() {
															openProposalPopup();
														}
													});
										}
									};
									Thread t = new Thread(runnable);
									t.start();
								} else {
									openProposalPopup();
								}
							}
						}
					}

					// If the popup is not open, we always propagate keys.
					// If the popup is open, consult the flag.
					e.doit = popup == null || propagateKeys;
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
					null, labelProvider, filterProposals);
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
	 * @param proposal
	 *            the accepted proposal
	 */
	public void proposalAccepted(IContentProposal proposal) {
		switch (acceptance) {
		case (PROPOSAL_REPLACE):
			setControlContent(proposal.getContent(), proposal
					.getCursorPosition());
			break;
		case (PROPOSAL_INSERT):
			insertControlContent(proposal.getContent(), proposal
					.getCursorPosition());
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
					.proposalAccepted(proposal);
	}

	/*
	 * Set the text content of the control to the specified text, setting the
	 * cursorPosition at the desired location within the new contents.
	 */
	private void setControlContent(String text, int cursorPosition) {
		if (isValid())
			controlContentAdapter.setControlContents(control, text,
					cursorPosition);
	}

	/*
	 * Insert the specified text into the control content, setting the
	 * cursorPosition at hte desired location within the new contents.
	 */
	private void insertControlContent(String text, int cursorPosition) {
		if (isValid())
			controlContentAdapter.insertControlContents(control, text,
					cursorPosition);
	}

	/*
	 * Check that the control and content adapter are valid.
	 */
	private boolean isValid() {
		return control != null && !control.isDisposed()
				&& controlContentAdapter != null;
	}
}
