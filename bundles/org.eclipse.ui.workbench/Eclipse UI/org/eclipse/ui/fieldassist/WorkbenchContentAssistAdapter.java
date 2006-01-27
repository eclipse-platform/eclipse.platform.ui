/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.fieldassist;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * WorkbenchContentAssistAdapter extends {@link ContentProposalAdapter} to
 * invoke content proposals using a specified {@link ICommand}. The ability to
 * specify a {@link KeyStroke} that explicitly invokes content proposals is
 * hidden by this class, and instead the String id of a command is used. If no
 * command id is specified by the client, then the default workbench content
 * assist command is used.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public class WorkbenchContentAssistAdapter extends ContentProposalAdapter {

	private String commandId;

	/* package */static final String CONTENT_PROPOSAL_COMMAND = "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$

	private IHandlerService handlerService;

	private IHandlerActivation activeHandler;

	private IHandler proposalHandler = new AbstractHandler() {
		public Object execute(ExecutionEvent event) {
			openProposalPopup();
			return null;
		}

	};

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
	 *            an optional label provider which provides text and image
	 *            information for content proposals. Clients are responsible
	 *            for disposing the label provider when it is no longer needed.
	 * @param commandId
	 *            the String id of the command that will invoke the content
	 *            assistant. If not supplied, the default value will be
	 *            "org.eclipse.ui.edit.text.contentAssist.proposals".
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of the
	 *            specified command id.
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
	public WorkbenchContentAssistAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider,
			ILabelProvider labelProvider, String commandId,
			char[] autoActivationCharacters, boolean propagateKeys,
			boolean filterProposals, int acceptance) {
		super(control, controlContentAdapter, proposalProvider, labelProvider,
				null, autoActivationCharacters, propagateKeys, filterProposals,
				acceptance);
		this.commandId = commandId;
		if (commandId == null)
			this.commandId = CONTENT_PROPOSAL_COMMAND;

		// If no autoactivation characters were specified, set them to the empty
		// array
		// so that we don't get the alphanumeric auto-trigger of our superclass.
		if (autoActivationCharacters == null) {
			this.setAutoActivationCharacters(new char[] {});
		}

		// Add listeners to the control to manage activation of the handler
		addListeners(control);

		// Cache the handler service so we don't have to retrieve it each time
		this.handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getAdapter(IHandlerService.class);
	}

	/*
	 * Add the listeners needed in order to activate the content assist command
	 * on the control.
	 */
	private void addListeners(Control control) {
		control.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if (activeHandler != null)
					handlerService.deactivateHandler(activeHandler);
			}

			public void focusGained(FocusEvent e) {
				activeHandler = handlerService.activateHandler(commandId,
						proposalHandler);
			}
		});
	}
}
