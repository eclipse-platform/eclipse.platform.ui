/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingCopies;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
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
	private MApplication application;

	@Inject
	private EBindingService bindingService;

	@Inject
	private ECommandService commandService;

	@Inject
	private BindingManager manager;

	@Inject
	private CommandManager cmdManager;

	@Inject
	private BindingTableManager bindingTableManager;

	@Inject
	@Optional
	private KeyBindingDispatcher dispatcher;

	private Map<String, MBindingContext> bindingContexts = new HashMap<String, MBindingContext>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		for (Runnable r : bindingsToRemove) {
			r.run();
		}
		for (MBindingTable table : tablesToRemove) {
			application.getBindingTables().remove(table);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#addBindingManagerListener(org.eclipse
	 * .jface.bindings.IBindingManagerListener)
	 */
	public void addBindingManagerListener(IBindingManagerListener listener) {
		manager.addBindingManagerListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#removeBindingManagerListener(org.
	 * eclipse.jface.bindings.IBindingManagerListener)
	 */
	public void removeBindingManagerListener(IBindingManagerListener listener) {
		manager.removeBindingManagerListener(listener);
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
		TriggerSequence seq = bindingService.getBestSequenceFor(command);
		return seq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#getBestActiveBindingFor(java.lang
	 * .String)
	 */
	public TriggerSequence getBestActiveBindingFor(String commandId) {
		ParameterizedCommand cmd = commandService.createCommand(commandId, null);
		return bindingService.getBestSequenceFor(cmd);
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

	private ArrayList<MBindingTable> tablesToRemove = new ArrayList<MBindingTable>();
	private ArrayList<Runnable> bindingsToRemove = new ArrayList<Runnable>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#readRegistryAndPreferences(org.eclipse
	 * .ui.commands.ICommandService)
	 */
	public void readRegistryAndPreferences(ICommandService commandService) {


		// TODO: shouldn't be using BindingPersistence here, but if we don't,
		// then the keys pref page will crash when loading!
		BindingPersistence bindingPersistence = new BindingPersistence(manager, cmdManager);
		bindingPersistence.reRead();

		Collection<Binding> bindings = bindingTableManager.getActiveBindings();
		manager.setBindings(bindings.toArray(new Binding[bindings.size()]));

		// BindingPersistence reader = new BindingPersistence(manager,
		// commandService);
		// reader.reRead();
		// Iterator i =
		// manager.getActiveBindingsDisregardingContextFlat().iterator();
		// while (i.hasNext()) {
		// Binding binding = (Binding) i.next();
		// addBinding(binding);
		// }

		bindingPersistence.dispose();
	}

	private MCommand findCommand(String id) {
		for (MCommand cmd : application.getCommands()) {
			if (id.equals(cmd.getElementId())) {
				return cmd;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#savePreferences(org.eclipse.jface
	 * .bindings.Scheme, org.eclipse.jface.bindings.Binding[])
	 */
	public void savePreferences(Scheme activeScheme, Binding[] bindings) throws IOException {

		Collection<Binding> activeBindings = bindingTableManager.getActiveBindings();
		Binding[] activeBindingsArray = activeBindings.toArray(new Binding[activeBindings.size()]);

		Binding b;

		Collection<Binding> activeUserBindings = new ArrayList<Binding>();

		boolean madeChanges = false; // flag to actually save the prefs

		// go through the list of active bindings (from the BTM) and DEACTIVATE
		// any bindings that are not included in the list of bindings passed
		// from the new keys pref page
		for (int i = 0; i < activeBindingsArray.length; i++) {
			b = getEqualBinding(bindings, activeBindingsArray[i]);

			// if we didn't find the binding, then it wasn't included in the
			// list passed from the new keys pref page, so this binding needs to
			// be deactivated
			if (b == null) {
				bindingService.deactivateBinding(activeBindingsArray[i]);

				// make sure we keep this binding manager consistent with the
				// BTM since the keys pref page will read off this
				manager.removeBinding(activeBindingsArray[i]);

				// if we're deactivating a SYSTEM binding, then keep it in the
				// list of bindings to write to xml, but make sure it gets
				// flagged as disabled
				if (activeBindingsArray[i].getType() == Binding.SYSTEM) {
					// inactiveSystemBindings.add(activeBindingsArray[i]);
					BindingCopies.addInactiveSysBinding(activeBindingsArray[i]);
				}

				// flag that changes were made to the bindings so that we need
				// to save
				madeChanges = true;
			}
		}

		// go though the list of bindings passed from the new keys pref page and
		// ACTIVATE the NEW bindings added
		for (int i = 0; i < bindings.length; i++) {
			b = getEqualBinding(activeBindingsArray, bindings[i]);

			// if we didn't find the binding, that means it's a new one, so
			// let's activate it (as long as the command isn't null)
			if (b == null && bindings[i].getParameterizedCommand() != null) {

				bindingService.activateBinding(bindings[i]);

				// make sure we keep this binding manager consistent with the
				// BTM since the keys pref page will read off this
				manager.addBinding(bindings[i]);

				// flag that changes were made to the bindings, so we need
				// to persist them
				madeChanges = true;

				// Since the binding persistence only writes out active USER
				// bindings and deactivated SYSTEM bindings, there's no point in
				// sending the entire bindings list and searching USER bindings.
				if (bindings[i].getType() == Binding.USER) {
					activeUserBindings.add(bindings[i]);
				} else {
					BindingCopies.removeInactiveSysBinding(bindings[i]);
				}

				// since we've activated a new binding, we need to see if it's
				// replacing one of the old bindings (in which case the old
				// binding needs to be deactivated)
				// ... but not right now
			}
			// else if the binding was already activated AND it's a USER
			// binding, then we'll need to persist it as well
			else if (bindings[i].getType() == Binding.USER) {
				activeUserBindings.add(bindings[i]);
				madeChanges = true;
			}
		}

		// if we made any changes, then they need to be persisted
		if (madeChanges) {
			// BindingPersistence.write(activeScheme, bindings);
			BindingPersistence.write(activeScheme,
					activeUserBindings.toArray(new Binding[activeUserBindings.size()]),
					BindingCopies.getInactiveSysBindings());
			// TODO: these numbers don't make sense... dig a litter deeper
		}

	}

	private Binding getEqualBinding(Binding[] bindings, Binding target) {
		Binding theBinding = null;

		for (int i = 0; i < bindings.length && theBinding == null; i++) {
			if (bindings[i].equals(target)) {
				theBinding = bindings[i];
			}
		}
		return theBinding;
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

	public MBindingContext getBindingContext(String id) {
		// cache
		MBindingContext result = bindingContexts.get(id);
		if (result == null) {
			// search
			result = searchContexts(id, application.getRootContext());
			if (result == null) {
				// create
				result = MCommandsFactory.INSTANCE.createBindingContext();
				result.setElementId(id);
				result.setName("Auto::" + id); //$NON-NLS-1$
				application.getRootContext().add(result);
			}
			if (result != null) {
				bindingContexts.put(id, result);
			}
		}
		return result;
	}

	/**
	 * @param id
	 * @param rootContext
	 * @return
	 */
	private MBindingContext searchContexts(String id, List<MBindingContext> rootContext) {
		for (MBindingContext context : rootContext) {
			if (context.getElementId().equals(id)) {
				return context;
			}
			MBindingContext result = searchContexts(id, context.getChildren());
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * TODO Promote this method to API.
	 * <p>
	 * Adds a single new binding to the existing array of bindings. If the array
	 * is currently <code>null</code>, then a new array is created and this
	 * binding is added to it. This method does not detect duplicates.
	 * </p>
	 * <p>
	 * This method completes in amortized <code>O(1)</code>.
	 * </p>
	 * 
	 * @param binding
	 *            The binding to be added; must not be <code>null</code>.
	 */
	public final void addBinding(final Binding binding) {
		MBindingTable table = null;
		for (MBindingTable bt : application.getBindingTables()) {
			if (bt.getBindingContext().getElementId().equals(binding.getContextId())) {
				table = bt;
				break;
			}
		}
		if (table == null) {
			table = CommandsFactoryImpl.eINSTANCE.createBindingTable();
			tablesToRemove.add(table);
			table.setBindingContext(getBindingContext(binding.getContextId()));
			table.setElementId(binding.getContextId());
			application.getBindingTables().add(table);
		}
		final MKeyBinding keyBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
		ParameterizedCommand parmCmd = binding.getParameterizedCommand();

		MCommand cmd = findCommand(parmCmd.getId());
		if (cmd == null) {
			return;
		}
		keyBinding.setCommand(cmd);
		keyBinding.setKeySequence(binding.getTriggerSequence().format());
		for (Object obj : parmCmd.getParameterMap().entrySet()) {
			Map.Entry entry = (Map.Entry) obj;
			MParameter p = CommandsFactoryImpl.eINSTANCE.createParameter();
			p.setElementId((String) entry.getKey());
			p.setName((String) entry.getKey());
			p.setValue((String) entry.getValue());
			keyBinding.getParameters().add(p);
		}
		table.getBindings().add(keyBinding);
		if (!tablesToRemove.contains(table)) {
			final MBindingTable theTable = table;
			bindingsToRemove.add(new Runnable() {
				public void run() {
					theTable.getBindings().remove(keyBinding);
				}
			});
		}
	}

	/**
	 * Remove the specific binding by identity. Does nothing if the binding is
	 * not in the manager.
	 * 
	 * @param binding
	 *            The binding to be removed; must not be <code>null</code>.
	 */
	public final void removeBinding(final Binding binding) {
		MBindingTable table = null;
		for (MBindingTable bt : application.getBindingTables()) {
			if (bt.getBindingContext().getElementId().equals(binding.getContextId())) {
				table = bt;
				break;
			}
		}
		if (table == null) {
			return;
		}
		final MKeyBinding keyBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
		ParameterizedCommand parmCmd = binding.getParameterizedCommand();

		MCommand cmd = findCommand(parmCmd.getId());
		if (cmd == null) {
			return;
		}
		keyBinding.setCommand(cmd);
		keyBinding.setKeySequence(binding.getTriggerSequence().format());
		for (Object obj : parmCmd.getParameterMap().entrySet()) {
			Map.Entry entry = (Map.Entry) obj;
			MParameter p = CommandsFactoryImpl.eINSTANCE.createParameter();
			p.setElementId((String) entry.getKey());
			p.setName((String) entry.getKey());
			p.setValue((String) entry.getValue());
			keyBinding.getParameters().add(p);
		}
		table.getBindings().remove(keyBinding);
		// if we need to be clean:
		manager.removeBinding(binding);
	}

	public BindingManager getBindingManager() {
		return manager;
	}

}
