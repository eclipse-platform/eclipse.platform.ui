/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
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
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
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
	private CommandManager commandManager;

	@Inject
	private BindingManager manager;

	@Inject
	private BindingTableManager tableManager;

	@Inject
	@Optional
	private KeyBindingDispatcher dispatcher;

	private BindingPersistence bp;

	private Map<String, MBindingContext> bindingContexts = new HashMap<>();

	private String[] activeSchemeIds;

	/**
	 * Key assist dialog for workbench key bindings, lazily created and cached
	 */
	private GlobalKeyAssistDialog keyAssistDialog;

	private IEclipseContext context;

	@PostConstruct
	void init() {
		final Scheme activeScheme = manager.getActiveScheme();
		if (activeScheme != null) {
			activeSchemeIds = getSchemeIds(activeScheme.getId());
			tableManager.setActiveSchemes(activeSchemeIds);
		}
		// Initialize BindingPersistence, its needed to install
		// a preferences change listener. See bug 266604.
		bp = new BindingPersistence(manager, commandManager) {
			@Override
			public void reRead() {
				super.reRead();
				// after having read the registry and preferences, persist
				// and update the model
				persistToModel(manager.getActiveScheme());
			}
		};
	}

	@Override
	public void dispose() {
		if (bp != null) {
			bp.dispose();
		}
	}

	@Inject
	public void setContext(IEclipseContext context) {
		this.context = context;
	}

	@Override
	public void addBindingManagerListener(IBindingManagerListener listener) {
		manager.addBindingManagerListener(listener);
	}

	@Override
	public void removeBindingManagerListener(IBindingManagerListener listener) {
		manager.removeBindingManagerListener(listener);
	}

	@Override
	public TriggerSequence[] getActiveBindingsFor(ParameterizedCommand parameterizedCommand) {
		Collection<TriggerSequence> seq = bindingService.getSequencesFor(parameterizedCommand);
		return seq.toArray(new TriggerSequence[seq.size()]);
	}

	@Override
	public TriggerSequence[] getActiveBindingsFor(String commandId) {
		return getActiveBindingsFor(commandService.createCommand(commandId, null));
	}

	@Override
	public Scheme getActiveScheme() {
		return manager.getActiveScheme();
	}

	@Override
	public TriggerSequence getBestActiveBindingFor(ParameterizedCommand command) {
		TriggerSequence seq = bindingService.getBestSequenceFor(command);
		return seq;
	}

	@Override
	public TriggerSequence getBestActiveBindingFor(String commandId) {
		ParameterizedCommand cmd = commandService.createCommand(commandId, null);
		return bindingService.getBestSequenceFor(cmd);
	}

	@Override
	public String getBestActiveBindingFormattedFor(String commandId) {
		TriggerSequence sequence = bindingService.getBestSequenceFor(commandService.createCommand(
				commandId, null));
		return sequence == null ? null : sequence.format();
	}

	@Override
	public Binding[] getBindings() {
		return manager.getBindings();
	}

	@Override
	public TriggerSequence getBuffer() {
		if (dispatcher == null) {
			return KeySequence.getInstance();
		}
		return dispatcher.getBuffer();
	}

	@Override
	public String getDefaultSchemeId() {
		return BindingPersistence.getDefaultSchemeId();
	}

	@Override
	public Scheme[] getDefinedSchemes() {
		return manager.getDefinedSchemes();
	}

	@Override
	public String getLocale() {
		return manager.getLocale();
	}

	@Override
	public Map getPartialMatches(TriggerSequence trigger) {
		final Collection<Binding> partialMatches = bindingService.getPartialMatches(trigger);
		final Map<TriggerSequence, Binding> result = new HashMap<>(
				partialMatches.size());

		for (Binding binding : partialMatches) {
			result.put(binding.getTriggerSequence(), binding);
		}

		return result;
	}

	@Override
	public Binding getPerfectMatch(TriggerSequence trigger) {
		return bindingService.getPerfectMatch(trigger);
	}

	@Override
	public String getPlatform() {
		return Util.getWS();
	}

	@Override
	public Scheme getScheme(String schemeId) {
		return manager.getScheme(schemeId);
	}

	@Override
	public boolean isKeyFilterEnabled() {
		return dispatcher == null ? false : dispatcher.getKeyDownFilter().isEnabled();
	}

	@Override
	public boolean isPartialMatch(TriggerSequence trigger) {
		return bindingService.isPartialMatch(trigger);
	}

	@Override
	public boolean isPerfectMatch(TriggerSequence trigger) {
		return bindingService.isPerfectMatch(trigger);
	}

	@Override
	public void openKeyAssistDialog() {
		if (keyAssistDialog == null) {
			Display.getCurrent();
			keyAssistDialog = new GlobalKeyAssistDialog(context, dispatcher);
		}
		if (keyAssistDialog.getShell() == null) {
			keyAssistDialog.setParentShell(Display.getCurrent().getActiveShell());
		}
		keyAssistDialog.open();
	}

	@Override
	public void readRegistryAndPreferences(ICommandService commandService) {
		bp.read();
	}

	private void saveLegacyPreferences(Scheme activeScheme, Binding[] bindings) throws IOException {
		BindingPersistence.write(activeScheme, bindings);
		try {
			manager.setActiveScheme(activeScheme);
		} catch (final NotDefinedException e) {
			WorkbenchPlugin.log("The active scheme is not currently defined.", //$NON-NLS-1$
					WorkbenchPlugin.getStatus(e));
		}
		manager.setBindings(bindings);
	}

	@Override
	public void savePreferences(Scheme activeScheme, Binding[] bindings) throws IOException {
		saveLegacyPreferences(activeScheme, bindings);
		persistToModel(activeScheme);
	}

	private void persistToModel(Scheme activeScheme) {
		// save the active scheme to the model
		writeSchemeToModel(activeScheme);
		activeSchemeIds = getSchemeIds(activeScheme.getId());
		tableManager.setActiveSchemes(activeSchemeIds);

		// weeds out any of the deleted system bindings using the binding
		// manager
		HashSet<Binding> activeBindings = new HashSet<>(
				manager.getActiveBindingsDisregardingContextFlat());

		// get all of the (active) model bindings that point to the actual runtime
		// bindings
		HashMap<Binding, MKeyBinding> bindingToKey = new HashMap<>();
		for (MBindingTable table : application.getBindingTables()) {
			for (MKeyBinding modelBinding : table.getBindings()) {
				final Object obj = modelBinding.getTransientData().get(
						EBindingService.MODEL_TO_BINDING_KEY);
				if (obj instanceof Binding) {
					bindingToKey.put((Binding) obj, modelBinding);
				}
			}
		}

		// go through each of the (active) bindings in the model to see if there are any
		// bindings that we should remove
		final HashSet<Binding> deleted = new HashSet<>(bindingToKey.keySet());
		deleted.removeAll(activeBindings);
		for (Binding binding : deleted) {
			if (binding.getType() == Binding.USER) {
				removeBinding(binding);
			} else {
				final MKeyBinding model = bindingToKey.get(binding);
				if (!model.getTags().contains(EBindingService.DELETED_BINDING_TAG)) {
					model.getTags().add(EBindingService.DELETED_BINDING_TAG);
				}
			}
		}

		// go through each of the active bindings (from the binding manager) to
		// see if there are any bindings that we should add to the runtime
		for (Binding binding : activeBindings) {
			final MKeyBinding model = bindingToKey.get(binding);
			MKeyBinding toAddModel = model;
			Binding toAddBinding = binding;

			// if we've switched schemes then we need to check to see if we
			// should override any of the old bindings
			final Binding conflict = findPotentialConflict(binding);

			if (conflict != null && conflict.getContextId().equals(binding.getContextId())) {
				final int rc = compareTo(conflict, binding);
				if (rc < 0) {
					// we need to delete the existing binding
					final MKeyBinding conflictModel = bindingToKey.get(conflict);
					if (conflict.getType() == Binding.USER) {
						removeBinding(conflict);
					} else if (conflictModel != null) {
						if (!conflictModel.getTags().contains(EBindingService.DELETED_BINDING_TAG)) {
							conflictModel.getTags().add(EBindingService.DELETED_BINDING_TAG);
						}
					}
				} else if (rc > 0) {
					// the existing binding is correct
					// we need to delete the new binding
					if (binding.getType() == Binding.USER) {
						removeBinding(binding);
					} else if (model != null) {
						if (!model.getTags().contains(EBindingService.DELETED_BINDING_TAG)) {
							model.getTags().add(EBindingService.DELETED_BINDING_TAG);
						}
					}
					// make sure we don't re-add them
					toAddModel = null;
					toAddBinding = null;
				}
			}
			if (toAddModel != null) {
				if (toAddModel.getTags().contains(EBindingService.DELETED_BINDING_TAG)) {
					toAddModel.getTags().remove(EBindingService.DELETED_BINDING_TAG);
				}
			} else if (toAddBinding != null) {
				addBinding(toAddBinding);
			}

		}
	}

	private Binding findPotentialConflict(Binding binding) {
		BindingTable table = tableManager.getTable(binding.getContextId());
		if (table != null) {
			Binding perfectMatch = table.getPerfectMatch(binding.getTriggerSequence());
			if (perfectMatch != null) {
				return perfectMatch;
			}
		}
		return bindingService.getPerfectMatch(binding.getTriggerSequence());
	}

	private final String[] getSchemeIds(String schemeId) {
		final List<String> strings = new ArrayList<>();
		while (schemeId != null) {
			strings.add(schemeId);
			try {
				schemeId = getScheme(schemeId).getParentId();
			} catch (final NotDefinedException e) {
				return new String[0];
			}
		}

		return strings.toArray(new String[strings.size()]);
	}

	/*
	 * Copied from
	 * org.eclipse.jface.bindings.BindingManager.compareSchemes(String, String)
	 *
	 * Returns an in based on scheme 1 < scheme 2
	 */
	private final int compareSchemes(final String schemeId1, final String schemeId2) {
		if (activeSchemeIds == null) {
			return 0;
		}
		if (!schemeId2.equals(schemeId1)) {
			for (int i = 0; i < activeSchemeIds.length; i++) {
				final String schemePointer = activeSchemeIds[i];
				if (schemeId2.equals(schemePointer)) {
					return 1;
				} else if (schemeId1.equals(schemePointer)) {
					return -1;
				}
			}
		}
		return 0;
	}

	/**
	 * Compare 2 bindings, taking into account Scheme and type.
	 *
	 * @param current
	 *            the existing binding
	 * @param addition
	 *            the incoming binding
	 * @return an int indicating current > addition
	 */
	private int compareTo(Binding current, Binding addition) {
		final Scheme s1 = manager.getScheme(current.getSchemeId());
		final Scheme s2 = manager.getScheme(addition.getSchemeId());
		if (!s1.equals(s2)) {
			int rc = compareSchemes(s1.getId(), s2.getId());
			if (rc != 0) {
				// this is because the compare is inverted
				return rc > 0 ? -1 : 1;
			}
		}
		return current.getType() - addition.getType();
	}


	private void writeSchemeToModel(Scheme activeScheme) {
		List<String> tags = application.getTags();
		boolean found = false;
		// replace the old scheme id
		Iterator<String> i = tags.iterator();
		while (i.hasNext() && !found) {
			String tag = i.next();
			if (tag.startsWith(EBindingService.ACTIVE_SCHEME_TAG)) {
				i.remove();
				found = true;
			}
		}
		tags.add(EBindingService.ACTIVE_SCHEME_TAG + ":" + activeScheme.getId()); //$NON-NLS-1$
	}

	@Override
	public void setKeyFilterEnabled(boolean enabled) {
		if (dispatcher != null) {
			dispatcher.getKeyDownFilter().setEnabled(enabled);
		}
	}

	@Override
	public Collection<Binding> getConflictsFor(TriggerSequence sequence) {
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
		MBindingTable table = getMTable(binding.getContextId());
		createORupdateMKeyBinding(application, table, binding);
	}

	/**
	 * @param contextId
	 * @return
	 */
	private MBindingTable getMTable(String contextId) {
		for (MBindingTable bt : application.getBindingTables()) {
			if (bt.getBindingContext().getElementId().equals(contextId)) {
				return bt;
			}
		}
		// create a new table if we couldn't find one
		MBindingTable table = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		table.setBindingContext(getBindingContext(contextId));
		table.setElementId(contextId);
		application.getBindingTables().add(table);
		return table;

	}

	static private boolean isSameBinding(MKeyBinding existingBinding, MCommand cmd, Binding binding) {
		// see org.eclipse.jface.bindings.Binding#equals(final Object object)
		if (!cmd.equals(existingBinding.getCommand()))
			return false;
		String existingKeySequence = existingBinding.getKeySequence();
		if (existingKeySequence == null)
			return false;
		try {
			final KeySequence existingSequence = KeySequence.getInstance(existingKeySequence);
			if (!existingSequence.equals(binding.getTriggerSequence()))
				return false;
		} catch (ParseException e) {
			return false;
		}

		// tags to look for:
		final List<String> modelTags = existingBinding.getTags();

		String schemeId = binding.getSchemeId();
		if (schemeId != null && !schemeId.equals(BindingPersistence.getDefaultSchemeId())) {
			if (!modelTags.contains(EBindingService.SCHEME_ID_ATTR_TAG + ":" + schemeId)) //$NON-NLS-1$
				return false;
		}
		String locale = binding.getLocale();
		if (locale != null) {
			if (!modelTags.contains(EBindingService.LOCALE_ATTR_TAG + ":" + locale)) //$NON-NLS-1$
				return false;
		}
		String platform = binding.getPlatform();
		if (platform != null) {
			if (!modelTags.contains(EBindingService.PLATFORM_ATTR_TAG + ":" + platform)) //$NON-NLS-1$
				return false;
		}
		if (binding.getType() == Binding.USER) {
			if (!modelTags.contains(EBindingService.TYPE_ATTR_TAG + ":user")) //$NON-NLS-1$
				return false;
		}
		return true;
	}

	// TBD the "update" procedure should not typically be run.
	// Add some sort of timestamp on the source files and update
	// only when it changes
	// TBD placement: this should be in the "3.x bridge" code
	static public MKeyBinding createORupdateMKeyBinding(MApplication application,
			MBindingTable table,
			Binding binding) {
		boolean addToTable = false;

		ParameterizedCommand parmCmd = binding.getParameterizedCommand();

		String id = parmCmd.getId();
		MCommand cmd = null;
		for (MCommand appCommand : application.getCommands()) {
			if (id.equals(appCommand.getElementId())) {
				cmd = appCommand;
				break;
			}
		}
		if (cmd == null) {
			return null;
		}

		MKeyBinding keyBinding = null;
		for (MKeyBinding existingBinding : table.getBindings()) {
			Binding b = (Binding) existingBinding.getTransientData().get(
					EBindingService.MODEL_TO_BINDING_KEY);
			if (binding.equals(b)) {
				keyBinding = existingBinding;
				break;
			}
			if (isSameBinding(existingBinding, cmd, binding)) {
				keyBinding = existingBinding;
				break;
			}
		}

		if (keyBinding == null) {
			addToTable = true;
			keyBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
			keyBinding.setCommand(cmd);
			keyBinding.setKeySequence(binding.getTriggerSequence().toString());

			for (Object obj : parmCmd.getParameterMap().entrySet()) {
				@SuppressWarnings({ "unchecked" })
				Map.Entry<String, String> entry = (Map.Entry<String, String>) obj;

				String paramID = entry.getKey();
				if (paramID == null)
					continue;
				List<MParameter> bindingParams = keyBinding.getParameters();
				MParameter p = null;
				for (MParameter param : bindingParams) {
					if (paramID.equals(param.getElementId())) {
						p = param;
						break;
					}
				}
				if (p == null) {
					p = CommandsFactoryImpl.eINSTANCE.createParameter();
					p.setElementId(entry.getKey());
					keyBinding.getParameters().add(p);
				}
				p.setName(entry.getKey());
				p.setValue(entry.getValue());
			}

			List<String> tags = keyBinding.getTags();
			// just add the 'schemeId' tag if the binding is for anything other
			// than
			// the default scheme
			if (binding.getSchemeId() != null
					&& !binding.getSchemeId().equals(BindingPersistence.getDefaultSchemeId())) {
				tags.add(EBindingService.SCHEME_ID_ATTR_TAG + ":" + binding.getSchemeId()); //$NON-NLS-1$
			}
			if (binding.getLocale() != null) {
				tags.add(EBindingService.LOCALE_ATTR_TAG + ":" + binding.getLocale()); //$NON-NLS-1$
			}
			if (binding.getPlatform() != null) {
				tags.add(EBindingService.PLATFORM_ATTR_TAG + ":" + binding.getPlatform()); //$NON-NLS-1$
			}
			// just add the 'type' tag if it's a user binding
			if (binding.getType() == Binding.USER) {
				tags.add(EBindingService.TYPE_ATTR_TAG + ":user"); //$NON-NLS-1$
			}
		}

		keyBinding.getTransientData().put(EBindingService.MODEL_TO_BINDING_KEY, binding);
		if (addToTable) {
			table.getBindings().add(keyBinding);
		}
		return keyBinding;
	}

	private MKeyBinding findMKeyBinding(MBindingTable table, Binding binding) {
		List<MKeyBinding> mBindings = table.getBindings();

		String bindingSchemeId = binding.getSchemeId() == null ? IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID
				: binding.getSchemeId();

		if (binding.getParameterizedCommand() != null) {
			String commandId = binding.getParameterizedCommand().getId();

			for (MKeyBinding curr : mBindings) {
				Binding transientBinding = (Binding) curr.getTransientData().get(
						EBindingService.MODEL_TO_BINDING_KEY);
				if (transientBinding != null) {
					if (binding.equals(transientBinding)) {
						return curr;
					}
					continue;
				}
				// check equality
				if (curr.getKeySequence().equals(binding.getTriggerSequence().toString())
						&& curr.getCommand() != null
						&& curr.getCommand().getElementId().equals(commandId)) {

					String schemeId = IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID;
					List<String> tags = curr.getTags();
					// grab the scheme id from the tags
					for (String tag : tags) {
						if (tag.startsWith(EBindingService.SCHEME_ID_ATTR_TAG)) {
							schemeId = tag.substring(9);
							break;
						}
					}
					// if the scheme ids are the same, then we found the
					// MKeyBinding
					if (schemeId.equals(bindingSchemeId)) {
						return curr;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Remove the specific binding by identity. Does nothing if the binding is
	 * not in the manager.
	 *
	 * @param binding
	 *            The binding to be removed; must not be <code>null</code>.
	 */
	public final void removeBinding(final Binding binding) {
		MKeyBinding mKeyBinding;
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

		// if we're removing a user binding, just remove it from the model and
		// the listeners will take care of removing the binding from the runtime
		// system
		if (binding.getType() == Binding.USER) {
			mKeyBinding = this.findMKeyBinding(table, binding);
			if (mKeyBinding != null) {
				table.getBindings().remove(mKeyBinding);
			}
		}
		// if we're removing a system binding, then find the model binding, add
		// a 'deleted' tag, and explicitly remove the binding from the runtime
		// system
		else {
			mKeyBinding = this.findMKeyBinding(table, binding);
			if (mKeyBinding != null) {
				mKeyBinding.getTags().add(EBindingService.DELETED_BINDING_TAG);
			}
		}
	}

	public BindingManager getBindingManager() {
		return manager;
	}

	public Collection<Binding> getActiveBindings() {
		return bindingService.getActiveBindings();
	}

	public WorkbenchKeyboard getKeyboard() {
		return new WorkbenchKeyboard(dispatcher);
	}
}
