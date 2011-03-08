/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.swt.util;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingCopies;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Process contexts in the model, feeding them into the command service.
 */
public class BindingProcessingAddon {

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker broker;

	@Inject
	private ContextManager contextManager;

	@Inject
	private BindingTableManager bindingTables;

	@Inject
	private ECommandService commandService;

	@Inject
	private EBindingService bindingService;

	private EventHandler additionHandler;

	private EventHandler contextHandler;

	@PostConstruct
	public void init() {
		defineBindingTables();
		cleanTables();
		activateContexts(application);
		registerModelListeners();
	}

	private void activateContexts(Object me) {
		if (me instanceof MBindings) {
			MContext contextModel = (MContext) me;
			MBindings container = (MBindings) me;
			List<MBindingContext> bindingContexts = container
					.getBindingContexts();
			IEclipseContext context = contextModel.getContext();
			if (context != null && !bindingContexts.isEmpty()) {
				EContextService cs = context.get(EContextService.class);
				for (MBindingContext element : bindingContexts) {
					cs.activateContext(element.getElementId());
				}
			}
		}
		if (me instanceof MElementContainer) {
			List<MUIElement> children = ((MElementContainer) me).getChildren();
			Iterator<MUIElement> i = children.iterator();
			while (i.hasNext()) {
				MUIElement e = i.next();
				activateContexts(e);
			}
		}
	}

	// goes through the entire active bindings list and replaces SYSTEM
	// bindings with any USER bindings that were persisted
	private void cleanTables() {
		ArrayList<Binding> dirtyBindings = new ArrayList<Binding>();
		Binding dirtyBinding;

		Binding[] userBindings = BindingCopies.getUserDefinedBindings();
		Binding curr;

		// go through all USER bindings and check if there is an "equal" SYSTEM
		// binding
		for (int i = 0; i < userBindings.length; i++) {
			curr = userBindings[i];

			// they should all be USER bindings, but double check anyway
			if (curr.getType() == Binding.USER) {
				dirtyBinding = checkDirty(curr);

				// if the SYSTEM binding is marked as dirty, then throw it in a
				// list and we'll remove it later
				if (dirtyBinding != null) {
					dirtyBindings.add(dirtyBinding);
				}
			}
		}

		//System.out.println("@@@ dirty bindings -> " + dirtyBindings.size());

		// go through the list of bindings that are marked as dirty (if any) and
		// remove them from the BindingTableManager
		for (int i = 0; i < dirtyBindings.size(); i++) {
			dirtyBinding = dirtyBindings.get(i);
			bindingTables.getTable(dirtyBinding.getContextId()).removeBinding(
					dirtyBinding);
		}
	}

	private Binding checkDirty(Binding b) {
		Collection<Binding> activeBindings = bindingTables.getActiveBindings();
		Iterator<Binding> iter = activeBindings.iterator();
		Binding curr;
		Binding dirtyBinding = null;

		while (iter.hasNext() && dirtyBinding == null) {
			curr = iter.next();

			// make sure we're only comparing SYSTEM bindings so that we don't
			// remove the wrong ones, and make sure the bindings we're comparing
			// actually have a command
			if (curr.getType() == Binding.SYSTEM
					&& curr.getParameterizedCommand() != null
					&& b.getParameterizedCommand() != null) {
				if (curr.getContextId().equals(b.getContextId())
						&& curr.getParameterizedCommand().equals(
								b.getParameterizedCommand())
						&& curr.getSchemeId().equals(b.getSchemeId())) {

					// mark this binding as dirty, and it will be removed
					dirtyBinding = curr;
				}
			}
		}
		return dirtyBinding;
	}

	private void defineBindingTables() {
		Activator.trace(Policy.DEBUG_CMDS,
				"Initialize binding tables from model", null); //$NON-NLS-1$
		for (MBindingTable bindingTable : application.getBindingTables()) {
			defineBindingTable(bindingTable);
		}
	}

	/**
	 * @param bindingTable
	 */
	private void defineBindingTable(MBindingTable bindingTable) {
		final Context bindingContext = contextManager.getContext(bindingTable
				.getBindingContext().getElementId());
		BindingTable table = bindingTables.getTable(bindingTable
				.getBindingContext().getElementId());
		if (table == null) {
			table = new BindingTable(bindingContext);
			bindingTables.addTable(table);
		}
		for (MKeyBinding binding : bindingTable.getBindings()) {
			defineBinding(table, bindingContext, binding);
		}
	}

	/**
	 * @param bindingTable
	 * @param binding
	 */
	private void defineBinding(BindingTable bindingTable,
			Context bindingContext, MKeyBinding binding) {
		Binding keyBinding = createBinding(bindingContext,
				binding.getCommand(), binding.getParameters(),
				binding.getKeySequence(), binding);
		if (keyBinding != null) {
			// if (keyBinding.getType() == Binding.USER)
			bindingTable.addBinding(keyBinding);
		}
	}

	private Binding createBinding(Context bindingContext, MCommand cmdModel,
			List<MParameter> modelParms, String keySequence, MKeyBinding binding) {
		if (cmdModel == null) {
			Activator.log(IStatus.ERROR, "binding with no command: " + binding); //$NON-NLS-1$
			return null;
		}
		Map<String, Object> parameters = null;
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<String, Object>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = commandService.createCommand(
				cmdModel.getElementId(), parameters);
		TriggerSequence sequence = null;
		sequence = bindingService.createSequence(keySequence);
		Binding keyBinding = null;
		if (cmd == null || sequence == null) {
			System.err.println("Failed to handle binding: " + binding); //$NON-NLS-1$
		} else {
			try {
				int bindingType = Binding.SYSTEM;

				// go thru the copied list of USER defined bindings to see if
				// this particular binding being created matches any of them
				if (BindingCopies.isUserBinding(sequence, cmd,
						"org.eclipse.ui.defaultAcceleratorConfiguration",
						bindingContext.getId())) {
					bindingType = Binding.USER;
				}

				// TODO: NEED TO CHANGE THIS!!!
				keyBinding = bindingService
						.createBinding(
								sequence,
								cmd,
								"org.eclipse.ui.defaultAcceleratorConfiguration", bindingContext.getId(), null, null, bindingType); //$NON-NLS-1$
			} catch (IllegalArgumentException e) {
				Activator.trace(Policy.DEBUG_MENUS,
						"failed to create: " + binding, e); //$NON-NLS-1$
				return null;
			}

		}
		return keyBinding;
	}

	private void updateBinding(MKeyBinding binding, boolean add) {
		Object parentObj = ((EObject) binding).eContainer();
		if (!(parentObj instanceof MBindingTable)) {
			return;
		}
		MBindingTable bt = (MBindingTable) parentObj;
		final Context bindingContext = contextManager.getContext(bt
				.getBindingContext().getElementId());
		BindingTable table = bindingTables.getTable(bindingContext.getId());
		if (table == null) {
			Activator.log(IStatus.ERROR, "Trying to create \'" + binding //$NON-NLS-1$
					+ "\' without binding table " + bindingContext.getId()); //$NON-NLS-1$
			return;
		}
		Binding keyBinding = createBinding(bindingContext,
				binding.getCommand(), binding.getParameters(),
				binding.getKeySequence(), binding);
		if (keyBinding != null) {
			if (add) {
				table.addBinding(keyBinding);
			} else {
				table.removeBinding(keyBinding);
			}
		}
	}

	@PreDestroy
	public void dispose() {
		unregsiterModelListeners();
	}

	private void registerModelListeners() {
		additionHandler = new EventHandler() {
			public void handleEvent(Event event) {
				Object elementObj = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (elementObj instanceof MApplication) {
					Object newObj = event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					if (UIEvents.EventTypes.ADD.equals(event
							.getProperty(UIEvents.EventTags.TYPE))
							&& newObj instanceof MBindingTable) {
						MBindingTable bt = (MBindingTable) newObj;
						final Context bindingContext = contextManager
								.getContext(bt.getBindingContext()
										.getElementId());
						final BindingTable table = new BindingTable(
								bindingContext);
						bindingTables.addTable(table);
						List<MKeyBinding> bindings = bt.getBindings();
						for (MKeyBinding binding : bindings) {
							Binding keyBinding = createBinding(bindingContext,
									binding.getCommand(),
									binding.getParameters(),
									binding.getKeySequence(), binding);
							if (keyBinding != null) {
								table.addBinding(keyBinding);
							}
						}
					}
				} else if (elementObj instanceof MBindingTable) {
					Object newObj = event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					Object oldObj = event
							.getProperty(UIEvents.EventTags.OLD_VALUE);
					if (UIEvents.EventTypes.ADD.equals(event
							.getProperty(UIEvents.EventTags.TYPE))
							&& newObj instanceof MKeyBinding) {
						MKeyBinding binding = (MKeyBinding) newObj;
						updateBinding(binding, true);
					} else if (UIEvents.EventTypes.REMOVE.equals(event
							.getProperty(UIEvents.EventTags.TYPE))
							&& oldObj instanceof MKeyBinding) {
						MKeyBinding binding = (MKeyBinding) oldObj;
						updateBinding(binding, false);
					}
				} else if (elementObj instanceof MKeyBinding) {
					MKeyBinding binding = (MKeyBinding) elementObj;
					String attrName = (String) event
							.getProperty(UIEvents.EventTags.ATTNAME);
					if (UIEvents.EventTypes.SET.equals(event
							.getProperty(UIEvents.EventTags.TYPE))) {
						Object oldObj = event
								.getProperty(UIEvents.EventTags.OLD_VALUE);
						if (UIEvents.KeyBinding.COMMAND.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setCommand((MCommand) oldObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						} else if (UIEvents.KeySequence.KEYSEQUENCE
								.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setKeySequence((String) oldObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						}
					} else if (UIEvents.KeyBinding.PARAMETERS.equals(attrName)) {
						if (UIEvents.EventTypes.ADD.equals(event
								.getProperty(UIEvents.EventTags.TYPE))) {
							Object newObj = event
									.getProperty(UIEvents.EventTags.NEW_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.getParameters().remove(newObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						} else if (UIEvents.EventTypes.REMOVE.equals(event
								.getProperty(UIEvents.EventTags.TYPE))) {
							Object oldObj = event
									.getProperty(UIEvents.EventTags.OLD_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.getParameters().add((MParameter) oldObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						}
					}
				}
			}
		};
		broker.subscribe(UIEvents.buildTopic(
				UIEvents.BindingTableContainer.TOPIC,
				UIEvents.BindingTableContainer.BINDINGTABLES), additionHandler);
		broker.subscribe(UIEvents.buildTopic(UIEvents.BindingTable.TOPIC,
				UIEvents.BindingTable.BINDINGS), additionHandler);
		broker.subscribe(UIEvents.buildTopic(UIEvents.KeyBinding.TOPIC,
				UIEvents.KeyBinding.COMMAND), additionHandler);
		broker.subscribe(UIEvents.buildTopic(UIEvents.KeyBinding.TOPIC,
				UIEvents.KeyBinding.PARAMETERS), additionHandler);
		broker.subscribe(UIEvents.buildTopic(UIEvents.KeySequence.TOPIC,
				UIEvents.KeySequence.KEYSEQUENCE), additionHandler);

		contextHandler = new EventHandler() {
			public void handleEvent(Event event) {
				Object elementObj = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (UIEvents.EventTypes.SET.equals(event
						.getProperty(UIEvents.EventTags.TYPE))
						&& newObj instanceof IEclipseContext) {
					activateContexts(elementObj);
				}
			}
		};
		broker.subscribe(UIEvents.buildTopic(UIEvents.Context.TOPIC,
				UIEvents.Context.CONTEXT), contextHandler);
	}

	private void unregsiterModelListeners() {
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(contextHandler);
	}
}
