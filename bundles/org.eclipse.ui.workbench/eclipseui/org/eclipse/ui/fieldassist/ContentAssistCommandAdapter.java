/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.fieldassist;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.keys.IBindingService;

/**
 * ContentAssistCommandAdapter extends {@link ContentProposalAdapter} to invoke
 * content proposals using a specified
 * {@link org.eclipse.core.commands.Command}. The ability to specify a
 * {@link org.eclipse.jface.bindings.keys.KeyStroke} that explicitly invokes
 * content proposals is hidden by this class, and instead the String id of a
 * command is used. If no command id is specified by the client, then the
 * default workbench content assist command is used.
 * <p>
 * As of 3.3, ContentAssistCommandAdapter can be optionally configured to
 * install the content assist decoration on its control.
 * <p>
 * This class is not intended to be subclassed.
 *
 * @since 3.2
 */
public class ContentAssistCommandAdapter extends ContentProposalAdapter {

	private static final String CONTENT_ASSIST_DECORATION_ID = "org.eclipse.ui.fieldAssist.ContentAssistField"; //$NON-NLS-1$
	private String commandId;

	/**
	 * The command id used for content assist. (value
	 * <code>"org.eclipse.ui.edit.text.contentAssist.proposals"</code>)
	 *
	 * @deprecated As of 3.5, replaced by
	 *             {@link IWorkbenchCommandConstants#EDIT_CONTENT_ASSIST}
	 */
	@Deprecated
	public static final String CONTENT_PROPOSAL_COMMAND = IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST;

	// Default autoactivation delay in milliseconds
	// TODO: This should eventually be controlled by
	// a platform UI preference.
	private static final int DEFAULT_AUTO_ACTIVATION_DELAY = 500;

	private IHandlerService handlerService;

	private IHandlerActivation activeHandler;

	private IHandler proposalHandler = new AbstractHandler() {
		@Override
		public Object execute(ExecutionEvent event) {
			openProposalPopup();
			return null;
		}

	};
	private ControlDecoration decoration;

	/**
	 * Construct a content proposal adapter that can assist the user with choosing
	 * content for the field. No visual indicator of content assist is shown.
	 *
	 * @param control                  the control for which the adapter is
	 *                                 providing content assist. May not be
	 *                                 <code>null</code>.
	 * @param controlContentAdapter    the <code>IControlContentAdapter</code> used
	 *                                 to obtain and update the control's contents
	 *                                 as proposals are accepted. May not be
	 *                                 <code>null</code>.
	 * @param proposalProvider         the <code>IContentProposalProvider</code>
	 *                                 used to obtain content proposals for this
	 *                                 control, or <code>null</code> if no content
	 *                                 proposal is available.
	 * @param commandId                the String id of the command that will invoke
	 *                                 the content assistant. If not supplied, the
	 *                                 default value will be
	 *                                 "org.eclipse.ui.edit.text.contentAssist.proposals".
	 * @param autoActivationCharacters An array of characters that trigger
	 *                                 auto-activation of content proposal. If
	 *                                 specified, these characters will trigger
	 *                                 auto-activation of the proposal popup,
	 *                                 regardless of the specified command id.
	 */
	public ContentAssistCommandAdapter(Control control, IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider, String commandId, char[] autoActivationCharacters) {
		this(control, controlContentAdapter, proposalProvider, commandId, autoActivationCharacters, false);
	}

	/**
	 * Construct a content proposal adapter that can assist the user with choosing
	 * content for the field.
	 *
	 * @param control                  the control for which the adapter is
	 *                                 providing content assist. May not be
	 *                                 <code>null</code>.
	 * @param controlContentAdapter    the <code>IControlContentAdapter</code> used
	 *                                 to obtain and update the control's contents
	 *                                 as proposals are accepted. May not be
	 *                                 <code>null</code>.
	 * @param proposalProvider         the <code>IContentProposalProvider</code>
	 *                                 used to obtain content proposals for this
	 *                                 control, or <code>null</code> if no content
	 *                                 proposal is available.
	 * @param commandId                the String id of the command that will invoke
	 *                                 the content assistant. If not supplied, the
	 *                                 default value will be
	 *                                 "org.eclipse.ui.edit.text.contentAssist.proposals".
	 * @param autoActivationCharacters An array of characters that trigger
	 *                                 auto-activation of content proposal. If
	 *                                 specified, these characters will trigger
	 *                                 auto-activation of the proposal popup,
	 *                                 regardless of the specified command id.
	 * @param installDecoration        A boolean that specifies whether a content
	 *                                 assist control decoration should be
	 *                                 installed. The client is responsible for
	 *                                 ensuring that adequate space is reserved for
	 *                                 the decoration. Clients that want more
	 *                                 fine-grained control of the decoration's
	 *                                 location or appearance should use
	 *                                 <code>false</code> for this parameter,
	 *                                 creating their own {@link ControlDecoration}
	 *                                 and managing it directly.
	 * @since 3.3
	 */
	public ContentAssistCommandAdapter(Control control, IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider, String commandId, char[] autoActivationCharacters,
			boolean installDecoration) {
		super(control, controlContentAdapter, proposalProvider, null, autoActivationCharacters);
		this.commandId = commandId;
		if (commandId == null) {
			this.commandId = IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST;
		}

		// If no autoactivation characters were specified, set them to the empty
		// array so that we don't get the alphanumeric auto-trigger of our
		// superclass.
		if (autoActivationCharacters == null) {
			this.setAutoActivationCharacters(new char[] {});
		}
		// Set a default autoactivation delay.
		setAutoActivationDelay(DEFAULT_AUTO_ACTIVATION_DELAY);

		// Cache the handler service so we don't have to retrieve it each time
		this.handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);

		// Add listeners to the control to manage activation of the handler
		addListeners(control);

		if (control.isFocusControl()) {
			activateHandler();
		}

		if (installDecoration) {
			// Note top left is used for compatibility with 3.2, although
			// this may change to center alignment in the future.
			decoration = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
			decoration.setShowOnlyOnFocus(true);
			FieldDecoration dec = getContentAssistFieldDecoration();
			decoration.setImage(dec.getImage());
			decoration.setDescriptionText(dec.getDescription());
		}

	}

	/*
	 * Add the listeners needed in order to activate the content assist command on
	 * the control.
	 */
	private void addListeners(Control control) {
		control.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				deactivateHandler();
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (isEnabled()) {
					activateHandler();
				} else {
					deactivateHandler();
				}
			}
		});
		control.addDisposeListener(e -> deactivateHandler());
	}

	/**
	 * Return the string command ID of the command used to invoke content assist.
	 *
	 * @return the command ID of the command that invokes content assist.
	 */
	public String getCommandId() {
		return commandId;
	}

	/*
	 * Return the field decoration that should be used to indicate that content
	 * assist is available for a field. Ensure that the decoration text includes the
	 * correct key binding.
	 *
	 * @return the {@link FieldDecoration} that should be used to show content
	 * assist.
	 *
	 * @since 3.3
	 */
	private FieldDecoration getContentAssistFieldDecoration() {
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		// Look for a decoration installed for this particular command id.
		String decId = CONTENT_ASSIST_DECORATION_ID + getCommandId();
		FieldDecoration dec = registry.getFieldDecoration(decId);

		// If there is not one, base ours on the standard JFace one.
		if (dec == null) {
			FieldDecoration originalDec = registry.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);

			registry.registerFieldDecoration(decId, null, originalDec.getImage());
			dec = registry.getFieldDecoration(decId);
		}
		// Always update the decoration text since the key binding may
		// have changed since it was last retrieved.
		IBindingService bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		dec.setDescription(NLS.bind(WorkbenchMessages.ContentAssist_Cue_Description_Key,
				bindingService.getBestActiveBindingFormattedFor(getCommandId())));

		// Now return the field decoration
		return dec;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (decoration != null) {
			if (enabled) {
				decoration.show();
			} else {
				decoration.hide();
			}
		}
		if (getControl().isFocusControl()) {
			if (enabled) {
				activateHandler();
			} else {
				deactivateHandler();
			}
		}
	}

	private void activateHandler() {
		if (activeHandler == null) {
			activeHandler = handlerService.activateHandler(commandId, proposalHandler);
		}
	}

	private void deactivateHandler() {
		if (activeHandler != null) {
			handlerService.deactivateHandler(activeHandler);
			activeHandler = null;
		}
	}
}
