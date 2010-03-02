/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.keys.IBindingService;

/**
 * <p>
 * Provides services related to the binding architecture (e.g., keyboard
 * shortcuts) within the workbench. This service can be used to access the
 * currently active bindings, as well as the current state of the binding
 * architecture.
 * </p>
 * 
 * @since 3.1
 */
public final class BindingService implements IBindingService {

	@Inject
	private EBindingService bindingService;

	@Inject
	private ECommandService commandService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#addBindingManagerListener(org.eclipse
	 * .jface.bindings.IBindingManagerListener)
	 */
	public void addBindingManagerListener(IBindingManagerListener listener) {
		// TODO compat addBindingManagerListener
		E4Util.unsupported("addBindingManagerListener"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#removeBindingManagerListener(org.
	 * eclipse.jface.bindings.IBindingManagerListener)
	 */
	public void removeBindingManagerListener(IBindingManagerListener listener) {
		// TODO compat removeBindingManagerListener
		E4Util.unsupported("removeBindingManagerListener"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getActiveBindingsFor(org.eclipse.
	 * core.commands.ParameterizedCommand)
	 */
	public TriggerSequence[] getActiveBindingsFor(ParameterizedCommand parameterizedCommand) {
		// TODO compat getActiveBindingsFor
		E4Util.unsupported("getActiveBindingsFor"); //$NON-NLS-1$
		return new TriggerSequence[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getActiveBindingsFor(java.lang.String
	 * )
	 */
	public TriggerSequence[] getActiveBindingsFor(String commandId) {
		// TODO compat getActiveBindingsFor
		E4Util.unsupported("getActiveBindingsFor"); //$NON-NLS-1$
		return new TriggerSequence[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveScheme()
	 */
	public Scheme getActiveScheme() {
		// TODO compat getActiveScheme
		E4Util.unsupported("getActiveScheme"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getBestActiveBindingFor(org.eclipse
	 * .core.commands.ParameterizedCommand)
	 */
	public TriggerSequence getBestActiveBindingFor(ParameterizedCommand command) {
		// TODO compat getBestActiveBindingFor
		E4Util.unsupported("getBestActiveBindingFor"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getBestActiveBindingFor(java.lang
	 * .String)
	 */
	public TriggerSequence getBestActiveBindingFor(String commandId) {
		// TODO compat getBestActiveBindingFor
		E4Util.unsupported("getBestActiveBindingFor"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getBestActiveBindingFormattedFor(
	 * java.lang.String)
	 */
	public String getBestActiveBindingFormattedFor(String commandId) {
		Command command = commandService.getCommand(commandId);
		if (command == null) {
			return null;
		}

		org.eclipse.e4.ui.bindings.TriggerSequence sequence = bindingService
				.getBestSequenceFor(new ParameterizedCommand(command, null));
		return sequence == null ? null : sequence.format();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBindings()
	 */
	public Binding[] getBindings() {
		// TODO compat getBindings
		E4Util.unsupported("getBindings"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBuffer()
	 */
	public TriggerSequence getBuffer() {
		// TODO compat getBuffer
		E4Util.unsupported("getBuffer"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getDefaultSchemeId()
	 */
	public String getDefaultSchemeId() {
		// TODO compat getDefaultSchemeId
		E4Util.unsupported("getDefaultSchemeId"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getDefinedSchemes()
	 */
	public Scheme[] getDefinedSchemes() {
		// TODO compat getDefinedSchemes
		E4Util.unsupported("getDefinedSchemes"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getLocale()
	 */
	public String getLocale() {
		// TODO compat getLocale
		E4Util.unsupported("getLocale"); //$NON-NLS-1$
		return Locale.getDefault().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getPartialMatches(org.eclipse.jface
	 * .bindings.TriggerSequence)
	 */
	public Map getPartialMatches(TriggerSequence trigger) {
		// TODO compat getPartialMatches
		E4Util.unsupported("getPartialMatches"); //$NON-NLS-1$
		return Collections.EMPTY_MAP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getPerfectMatch(org.eclipse.jface
	 * .bindings.TriggerSequence)
	 */
	public Binding getPerfectMatch(TriggerSequence trigger) {
		// TODO compat getPerfectMatch
		E4Util.unsupported("getPerfectMatch"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getPlatform()
	 */
	public String getPlatform() {
		return Util.getWS();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getScheme(java.lang.String)
	 */
	public Scheme getScheme(String schemeId) {
		// TODO compat getScheme
		E4Util.unsupported("getScheme"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#isKeyFilterEnabled()
	 */
	public boolean isKeyFilterEnabled() {
		// TODO compat isKeyFilterEnabled
		E4Util.unsupported("isKeyFilterEnabled"); //$NON-NLS-1$
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#isPartialMatch(org.eclipse.jface.
	 * bindings.TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence trigger) {
		// TODO compat isPartialMatch
		E4Util.unsupported("isPartialMatch"); //$NON-NLS-1$
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#isPerfectMatch(org.eclipse.jface.
	 * bindings.TriggerSequence)
	 */
	public boolean isPerfectMatch(TriggerSequence trigger) {
		// TODO compat isPerfectMatch
		E4Util.unsupported("isPerfectMatch"); //$NON-NLS-1$
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#openKeyAssistDialog()
	 */
	public void openKeyAssistDialog() {
		// TODO compat openKeyAssistDialog
		E4Util.unsupported("openKeyAssistDialog"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#readRegistryAndPreferences(org.eclipse
	 * .ui.commands.ICommandService)
	 */
	public void readRegistryAndPreferences(ICommandService commandService) {
		// TODO compat readRegistryAndPreferences
		E4Util.unsupported("BindingService#readRegistryAndPreferences"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#savePreferences(org.eclipse.jface
	 * .bindings.Scheme, org.eclipse.jface.bindings.Binding[])
	 */
	public void savePreferences(Scheme activeScheme, Binding[] bindings) throws IOException {
		// TODO compat savePreferences
		E4Util.unsupported("savePreferences"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#setKeyFilterEnabled(boolean)
	 */
	public void setKeyFilterEnabled(boolean enabled) {
		// TODO compat setKeyFilterEnabled
		E4Util.unsupported("setKeyFilterEnabled"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getConflictsFor(org.eclipse.jface
	 * .bindings.TriggerSequence)
	 */
	public Collection getConflictsFor(TriggerSequence sequence) {
		// TODO compat getConflictsFor
		E4Util.unsupported("getConflictsFor"); //$NON-NLS-1$
		return null;
	}

}
