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
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
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

	@Inject
	private BindingManager manager;

	@Inject
	@Optional
	private KeyBindingDispatcher dispatcher;

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
		Collection<TriggerSequence> seq = bindingService.getSequencesFor(parameterizedCommand);
		return seq.toArray(new TriggerSequence[seq.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getActiveBindingsFor(java.lang.String
	 * )
	 */
	public TriggerSequence[] getActiveBindingsFor(String commandId) {
		return getActiveBindingsFor(commandService.createCommand(commandId, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveScheme()
	 */
	public Scheme getActiveScheme() {
		return manager.getActiveScheme();
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
		TriggerSequence sequence = bindingService.getBestSequenceFor(commandService.createCommand(
				commandId, null));
		return sequence == null ? null : sequence.format();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBindings()
	 */
	public Binding[] getBindings() {
		return manager.getBindings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBuffer()
	 */
	public TriggerSequence getBuffer() {
		if (dispatcher == null) {
			return KeySequence.getInstance();
		}
		return dispatcher.getBuffer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getDefaultSchemeId()
	 */
	public String getDefaultSchemeId() {
		return BindingPersistence.getDefaultSchemeId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getDefinedSchemes()
	 */
	public Scheme[] getDefinedSchemes() {
		return manager.getDefinedSchemes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getLocale()
	 */
	public String getLocale() {
		return manager.getLocale();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getPartialMatches(org.eclipse.jface
	 * .bindings.TriggerSequence)
	 */
	public Map getPartialMatches(TriggerSequence trigger) {
		final TriggerSequence[] prefixes = trigger.getPrefixes();
		final int prefixesLength = prefixes.length;
		if (prefixesLength == 0) {
			return Collections.EMPTY_MAP;
		}
		
		Collection<Binding> partialMatches = bindingService.getPartialMatches(trigger);
		Map<TriggerSequence,Object> prefixTable = new HashMap<TriggerSequence, Object>();
		for (Binding binding : partialMatches) {
			for (int i = 0; i < prefixesLength; i++) {
				final TriggerSequence prefix = prefixes[i];
				final Object value = prefixTable.get(prefix);
				if ((prefixTable.containsKey(prefix)) && (value instanceof Map)) {
					((Map) value).put(prefixTable, binding);
				} else {
					final Map map = new HashMap();
					prefixTable.put(prefix, map);
					map.put(prefixTable, binding);
				}
			}
		}
		return prefixTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getPerfectMatch(org.eclipse.jface
	 * .bindings.TriggerSequence)
	 */
	public Binding getPerfectMatch(TriggerSequence trigger) {
		return bindingService.getPerfectMatch(trigger);
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
		return manager.getScheme(schemeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#isKeyFilterEnabled()
	 */
	public boolean isKeyFilterEnabled() {
		return dispatcher == null ? false : dispatcher.getKeyDownFilter().isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#isPartialMatch(org.eclipse.jface.
	 * bindings.TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence trigger) {
		return bindingService.isPartialMatch(trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#isPerfectMatch(org.eclipse.jface.
	 * bindings.TriggerSequence)
	 */
	public boolean isPerfectMatch(TriggerSequence trigger) {
		return bindingService.isPerfectMatch(trigger);
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
		BindingPersistence reader = new BindingPersistence(manager, commandService);
		reader.reRead();
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
		if (dispatcher != null) {
			dispatcher.getKeyDownFilter().setEnabled(enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getConflictsFor(org.eclipse.jface
	 * .bindings.TriggerSequence)
	 */
	public Collection getConflictsFor(TriggerSequence sequence) {
		return bindingService.getConflictsFor(sequence);
	}

}
