/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistantExtension;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.ISourceViewerExtension4;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;


/**
 * Helper class to make navigation key bindings work for the content assistant
 * and the quick assist assistant while the editor has focus.
 * <p>
 * Clients normally don't need to use that class as the setup is done by the
 * framework.
 * </p>
 *
 * @since 3.4
 */
public final class KeyBindingSupportForAssistant implements ICompletionListener {


	private static final class ReplacedCommand {

		private IHandler handler;

		private Command command;

		ReplacedCommand(String commandId, ICommandService commandService) {
			this.command= commandService.getCommand(commandId);
			replaceWith(null);
		}

		ReplacedCommand(String commandId, IHandler handler, ICommandService commandService) {
			this.command= commandService.getCommand(commandId);
			replaceWith(handler);
		}

		void activate() {
			if (handler != null) {
				/*
				 *  Next check ensures that we don't overwrite newly activated editor contributions.
				 *  For details see https://bugs.eclipse.org/bugs/show_bug.cgi?id=297834.
				 */
				if (!handler.getClass().isInstance(command.getHandler()))
					command.setHandler(handler);
			}
		}

		private void replaceWith(IHandler newHandler) {
			if (command.isHandled()) {
				handler= command.getHandler();
				command.setHandler(newHandler);
			}
		}
	}


	private List fReplacedCommands;
	private ContentAssistantFacade fContentAssistantFacade;
	private IQuickAssistAssistant fQuickAssistAssistant;

	/**
	 * Creates the support for a content assistant facade.
	 *
	 * @param contentAssistFacade the content assist facade
	 * @deprecated As of 3.5, this is a NOP since the framework installs this now
	 */
	public KeyBindingSupportForAssistant(ContentAssistantFacade contentAssistFacade) {
	}

	/**
	 * Creates the support for a content assistant facade.
	 *
	 * @param sourceViewerExtension the source viewer extension
	 * @since 3.5
	 */
	public KeyBindingSupportForAssistant(ISourceViewerExtension4 sourceViewerExtension) {
		Assert.isLegal(sourceViewerExtension != null);
		fContentAssistantFacade= sourceViewerExtension.getContentAssistantFacade();
		if (fContentAssistantFacade != null)
			fContentAssistantFacade.addCompletionListener(this);
	}

	/**
	 * Creates the support for a content assistant facade.
	 *
	 * @param contentAssistant the content assist facade
	 * @deprecated As of 3.5, this is a NOP since the framework installs this now
	 */
	public KeyBindingSupportForAssistant(ContentAssistant contentAssistant) {
	}

	/**
	 * Creates the support for a quick assist assistant.
	 *
	 * @param quickAssistAssistant the quick assist assistant.
	 */
	public KeyBindingSupportForAssistant(IQuickAssistAssistant quickAssistAssistant) {
		Assert.isLegal(quickAssistAssistant != null);
		fQuickAssistAssistant= quickAssistAssistant;
		fQuickAssistAssistant.addCompletionListener(this);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionListener#assistSessionStarted(org.eclipse.jface.text.contentassist.ContentAssistEvent)
	 * @since 3.4
	 */
	public void assistSessionStarted(ContentAssistEvent event) {
		ICommandService commandService= (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandler handler= getHandler(ContentAssistant.SELECT_NEXT_PROPOSAL_COMMAND_ID);
		fReplacedCommands= new ArrayList(10);
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.LINE_DOWN, handler, commandService));
		handler= getHandler(ContentAssistant.SELECT_PREVIOUS_PROPOSAL_COMMAND_ID);
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.LINE_UP, handler, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.LINE_START, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.LINE_END, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.PAGE_UP, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.PAGE_DOWN, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.TEXT_START, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.TEXT_END, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.SCROLL_LINE_UP, commandService));
		fReplacedCommands.add(new ReplacedCommand(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN, commandService));
	}

	/**
	 * Returns the handler for the given command identifier.
	 * <p>
	 * The same handler instance will be returned when called a more than once
	 * with the same command identifier.
	 * </p>
	 *
	 * @param commandId the command identifier
	 * @return the handler for the given command identifier
	 */
	private IHandler getHandler(String commandId) {
		if (fContentAssistantFacade != null)
			return fContentAssistantFacade.getHandler(commandId);
		if (fQuickAssistAssistant instanceof IQuickAssistAssistantExtension)
			return ((IQuickAssistAssistantExtension)fQuickAssistAssistant).getHandler(commandId);
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionListener#assistSessionEnded(org.eclipse.jface.text.contentassist.ContentAssistEvent)
	 * @since 3.4
	 */
	public void assistSessionEnded(ContentAssistEvent event) {
		if (fReplacedCommands == null)
			return;

		Iterator iter= fReplacedCommands.iterator();
		while (iter.hasNext())
			((ReplacedCommand)iter.next()).activate();
		fReplacedCommands= null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionListener#selectionChanged(org.eclipse.jface.text.contentassist.ICompletionProposal, boolean)
	 * @since 3.4
	 */
	public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
	}

	public void dispose() {
		if (fContentAssistantFacade != null) {
			fContentAssistantFacade.removeCompletionListener(this);
			fContentAssistantFacade= null;
		}

		if (fQuickAssistAssistant != null) {
			fQuickAssistAssistant.removeCompletionListener(this);
			fQuickAssistAssistant= null;
		}
	}
}