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
package org.eclipse.jface.text.source;

import org.eclipse.core.commands.IHandler;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension4;


/**
 * Facade to allow minimal access to the given content assistant.
 * <p>
 * The offered API access can grow over time.
 * </p>
 *
 * @since 3.4
 */
public final class ContentAssistantFacade {

	private IContentAssistant fContentAssistant;

	/**
	 * Creates a new facade.
	 *
	 * @param contentAssistant the content assistant which implements {@link IContentAssistantExtension2} and {@link IContentAssistantExtension4}
	 */
	public ContentAssistantFacade(IContentAssistant contentAssistant) {
		Assert.isLegal(contentAssistant instanceof IContentAssistantExtension2 && contentAssistant instanceof IContentAssistantExtension4);
		fContentAssistant= contentAssistant;
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
	 * @throws IllegalArgumentException if the command is not supported by this
	 *             content assistant
	 * @throws IllegalStateException if called when the content assistant is
	 *             uninstalled
	 */
	public IHandler getHandler(String commandId) {
		if (fContentAssistant == null)
			throw new IllegalStateException();
		return ((IContentAssistantExtension4)fContentAssistant).getHandler(commandId);
	}

	/**
	 * Adds a completion listener that will be informed before proposals are
	 * computed.
	 *
	 * @param listener the listener
	 * @throws IllegalStateException if called when the content assistant is
	 *             uninstalled
	 */
	public void addCompletionListener(ICompletionListener listener) {
		if (fContentAssistant == null)
			throw new IllegalStateException();
		((IContentAssistantExtension2)fContentAssistant).addCompletionListener(listener);
	}

	/**
	 * Removes a completion listener.
	 *
	 * @param listener the listener to remove
	 * @throws IllegalStateException if called when the content assistant is
	 *             uninstalled
	 */
	public void removeCompletionListener(ICompletionListener listener) {
		((IContentAssistantExtension2)fContentAssistant).removeCompletionListener(listener);
	}

}
