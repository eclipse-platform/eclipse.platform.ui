/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.fieldassist;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.swt.IFocusService;

/**
 * ContentAssistCommandAdapter extends {@link ContentProposalAdapter} to invoke
 * content proposals using a specified {@link org.eclipse.core.commands.Command}.
 * The ability to specify a {@link org.eclipse.jface.bindings.keys.KeyStroke}
 * that explicitly invokes content proposals is hidden by this class, and
 * instead the String id of a command is used. If no command id is specified by
 * the client, then the default workbench content assist command is used.
 * <p>
 * As of 3.3, ContentAssistCommandAdapter can be optionally configured to
 * install the content assist decoration on its control.
 * <p>
 * This class is not intended to be subclassed.
 * 
 * @since 3.2
 */
public class ContentAssistCommandAdapter extends ContentProposalAdapter {

	private class ContentAssistHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) {
			openProposalPopup();
			return null;
		}

		void setEnabled(boolean enabled) {
			this.setBaseEnabled(enabled);
		}
	}

	// ID used in the decoration registry.
	private static final String CONTENT_ASSIST_DECORATION_ID = "org.eclipse.ui.fieldAssist.ContentAssistField"; //$NON-NLS-1$

	// ID prefix used when registering our control with the focus service. 
	private static final String CONTROL_ID_PREFIX = "org.eclipse.ui.fieldAssist.ContentAssistField.ControlID"; //$NON-NLS-1$

	private String commandId;

	/**
	 * The command id used for content assist. (value
	 * <code>"org.eclipse.ui.edit.text.contentAssist.proposals"</code>)
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_CONTENT_ASSIST}
	 */
	public static final String CONTENT_PROPOSAL_COMMAND= IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST;

	// Default autoactivation delay in milliseconds
	// TODO: This should eventually be controlled by
	// a platform UI preference.
	private static final int DEFAULT_AUTO_ACTIVATION_DELAY = 500;

	private ContentAssistHandler proposalHandler = new ContentAssistHandler();
	private ControlDecoration decoration;

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for the field. No visual indicator of content assist is
	 * shown.
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
	 * @param commandId
	 *            the String id of the command that will invoke the content
	 *            assistant. If not supplied, the default value will be
	 *            "org.eclipse.ui.edit.text.contentAssist.proposals".
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of the
	 *            specified command id.
	 */
	public ContentAssistCommandAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider, String commandId,
			char[] autoActivationCharacters) {
		this(control, controlContentAdapter, proposalProvider, commandId,
				autoActivationCharacters, false);
	}

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
	 * @param commandId
	 *            the String id of the command that will invoke the content
	 *            assistant. If not supplied, the default value will be
	 *            "org.eclipse.ui.edit.text.contentAssist.proposals".
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of the
	 *            specified command id.
	 * @param installDecoration
	 *            A boolean that specifies whether a content assist control
	 *            decoration should be installed. The client is responsible for
	 *            ensuring that adequate space is reserved for the decoration.
	 *            Clients that want more fine-grained control of the
	 *            decoration's location or appearance should use
	 *            <code>false</code> for this parameter, creating their own
	 *            {@link ControlDecoration} and managing it directly.
	 * @since 3.3
	 */
	public ContentAssistCommandAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider, String commandId,
			char[] autoActivationCharacters, boolean installDecoration) {
		super(control, controlContentAdapter, proposalProvider, null,
				autoActivationCharacters);
		this.commandId = commandId;
		if (commandId == null) {
			this.commandId= IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST;
		}

		// If no autoactivation characters were specified, set them to the empty
		// array so that we don't get the alphanumeric auto-trigger of our
		// superclass.
		if (autoActivationCharacters == null) {
			this.setAutoActivationCharacters(new char[] {});
		}
		// Set a default autoactivation delay.
		setAutoActivationDelay(DEFAULT_AUTO_ACTIVATION_DELAY);

		// Activate the handler
		activateHandler(control);

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

	/**
	 * Return the string command ID of the command used to invoke content
	 * assist.
	 * 
	 * @return the command ID of the command that invokes content assist.
	 */
	public String getCommandId() {
		return commandId;
	}

	/*
	 * Return the field decoration that should be used to indicate that content
	 * assist is available for a field. Ensure that the decoration text includes
	 * the correct key binding.
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
			FieldDecoration originalDec = registry
					.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);

			registry.registerFieldDecoration(decId, null, originalDec
					.getImage());
			dec = registry.getFieldDecoration(decId);
		}
		// Always update the decoration text since the key binding may
		// have changed since it was last retrieved.
		IBindingService bindingService = (IBindingService) PlatformUI
				.getWorkbench().getService(IBindingService.class);
		dec
				.setDescription(NLS
						.bind(
								WorkbenchMessages.ContentAssist_Cue_Description_Key,
								bindingService
										.getBestActiveBindingFormattedFor(getCommandId())));

		// Now return the field decoration
		return dec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Overridden to hide and show the content assist decoration
	 * 
	 * @see org.eclipse.jface.fieldassist.ContentProposalAdapter#setEnabled(boolean)
	 * @since 3.3
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (decoration != null) {
			if (enabled) {
				decoration.show();
			} else {
				decoration.hide();
			}
		}
		proposalHandler.setEnabled(enabled);
	}

	private void activateHandler(final Control control) {
		IFocusService fs = (IFocusService) PlatformUI.getWorkbench()
				.getService(IFocusService.class);
		final IHandlerService hs = (IHandlerService) PlatformUI.getWorkbench().getService(
				IHandlerService.class);
		if (fs != null && hs != null) {
			fs.addFocusTracker(control, CONTROL_ID_PREFIX + hashCode());
			final IHandlerActivation handlerActivation = hs.activateHandler(commandId,
					proposalHandler, new Expression() {
						public EvaluationResult evaluate(IEvaluationContext context) {
							return context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_NAME) == control ? EvaluationResult.TRUE
									: EvaluationResult.FALSE;
						}

						public void collectExpressionInfo(final ExpressionInfo info) {
							info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_NAME);
						}

				});
			control.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					hs.deactivateHandler(handlerActivation);
				}
			});
		}
	}
}
