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

import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.DecoratedTextCellEditor;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.keys.IBindingService;

/**
 * ContentAssistField utilizes the concepts of a {@link DecoratedField} and the
 * {@link ContentAssistCommandAdapter} to provide a decorated field that shows a
 * content assist cue when it gets focus and invokes content assist for a
 * specified command.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public class ContentAssistCellEditor extends DecoratedTextCellEditor {

	private ContentAssistCommandAdapter adapter;

	/**
	 * Construct a content assist field that shows a content assist cue and can
	 * assist the user with choosing content for the field.
	 * 
	 * @param parent
	 *            the parent of the decorated field.
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
	public ContentAssistCellEditor(Composite parent,
			IContentProposalProvider proposalProvider, String commandId,
			char[] autoActivationCharacters) {

		super(parent);
		adapter = new ContentAssistCommandAdapter(getTextControl(),
				new TextContentAdapter(), proposalProvider, commandId,
				autoActivationCharacters);

		getDecoratedField().addFieldDecoration(getFieldDecoration(),
				SWT.LEFT | SWT.TOP, true);

	}

	/**
	 * Set the boolean flag that determines whether the content assist is
	 * enabled.
	 * 
	 * @param enabled
	 *            <code>true</code> if content assist is enabled and
	 *            responding to user input, <code>false</code> if it is
	 *            ignoring user input.
	 * 
	 */
	public void setEnabled(boolean enabled) {
		adapter.setEnabled(enabled);
		if (enabled)
			getDecoratedField().showDecoration(getFieldDecoration());
		else
			getDecoratedField().hideDecoration(getFieldDecoration());
	}

	/*
	 * Get a field decoration appropriate for cueing the user, including a
	 * description of the active key binding.
	 * 
	 */
	private FieldDecoration getFieldDecoration() {
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		// Look for a decoration installed for this command.
		String decId = IWorkbenchFieldDecorationConstants.CONTENT_ASSIST_CUE
				+ adapter.getCommandId();
		FieldDecoration dec = registry.getFieldDecoration(decId);

		// If there is not one, base it on the content assist decoration without
		// a keybinding
		if (dec == null) {
			FieldDecoration originalDec = registry
					.getFieldDecoration(IWorkbenchFieldDecorationConstants.CONTENT_ASSIST_CUE);

			registry.registerFieldDecoration(decId, null, originalDec
					.getImage());
			dec = registry.getFieldDecoration(decId);
		}
		// Always update the decoration text since the key binding may
		// have changed since it was last retrieved.
		IBindingService bindingService = (IBindingService) PlatformUI
				.getWorkbench().getAdapter(IBindingService.class);
		dec.setDescription(NLS.bind(
				WorkbenchMessages.ContentAssist_Cue_Description_Key,
				bindingService.getBestActiveBindingFormattedFor(adapter
						.getCommandId())));

		// Now return the field decoration
		return dec;
	}
}
