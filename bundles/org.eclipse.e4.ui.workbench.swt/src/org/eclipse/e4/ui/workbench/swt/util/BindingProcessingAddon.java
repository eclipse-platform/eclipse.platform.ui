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

import java.util.HashMap;
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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
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

	@PostConstruct
	public void init() {
		defineBindingTables();
		registerModelListeners();
	}

	private void defineBindingTables() {
		Activator.trace(Policy.DEBUG_CMDS, "Initialize binding tables from model", null); //$NON-NLS-1$
		for (MBindingTable bindingTable : application.getBindingTables()) {
			defineBindingTable(bindingTable);
		}
	}

	/**
	 * @param bindingTable
	 */
	private void defineBindingTable(MBindingTable bindingTable) {
		final Context bindingContext = contextManager
				.getContext(bindingTable.getBindingContextId());
		final BindingTable table = new BindingTable(bindingContext);
		bindingTables.addTable(table);
		for (MKeyBinding binding : bindingTable.getBindings()) {
			defineBinding(table, bindingContext, binding);
		}
	}

	/**
	 * @param bindingTable
	 * @param binding
	 */
	private void defineBinding(BindingTable bindingTable, Context bindingContext,
			MKeyBinding binding) {
		Binding keyBinding = createBinding(bindingContext, binding.getCommand(),
				binding.getParameters(), binding.getKeySequence(), binding);
		if (keyBinding != null) {
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
		ParameterizedCommand cmd = commandService
				.createCommand(cmdModel.getElementId(), parameters);
		TriggerSequence sequence = null;
		sequence = bindingService.createSequence(keySequence);
		Binding keyBinding = null;
		if (cmd == null || sequence == null) {
			System.err.println("Failed to handle binding: " + binding); //$NON-NLS-1$
		} else {
			try {
				keyBinding = bindingService.createBinding(sequence, cmd,
						"org.eclipse.ui.defaultAcceleratorConfiguration", bindingContext.getId()); //$NON-NLS-1$
			} catch (IllegalArgumentException e) {
				Activator.trace(Policy.DEBUG_MENUS, "failed to create: " + binding, e); //$NON-NLS-1$
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
		final Context bindingContext = contextManager.getContext(bt.getBindingContextId());
		BindingTable table = bindingTables.getTable(bindingContext.getId());
		if (table == null) {
			Activator.log(IStatus.ERROR, "Trying to create \'" + binding //$NON-NLS-1$
					+ "\' without binding table " + bindingContext.getId()); //$NON-NLS-1$
			return;
		}
		Binding keyBinding = createBinding(bindingContext, binding.getCommand(),
				binding.getParameters(), binding.getKeySequence(), binding);
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
				Object elementObj = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (elementObj instanceof MApplication) {
					Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
					if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))
							&& newObj instanceof MBindingTable) {
						MBindingTable bt = (MBindingTable) newObj;
						final Context bindingContext = contextManager.getContext(bt
								.getBindingContextId());
						final BindingTable table = new BindingTable(bindingContext);
						bindingTables.addTable(table);
						List<MKeyBinding> bindings = bt.getBindings();
						for (MKeyBinding binding : bindings) {
							Binding keyBinding = createBinding(bindingContext,
									binding.getCommand(), binding.getParameters(),
									binding.getKeySequence(), binding);
							if (keyBinding != null) {
								table.addBinding(keyBinding);
							}
						}
					}
				} else if (elementObj instanceof MBindingTable) {
					Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
					Object oldObj = event.getProperty(UIEvents.EventTags.OLD_VALUE);
					if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))
							&& newObj instanceof MKeyBinding) {
						MKeyBinding binding = (MKeyBinding) newObj;
						updateBinding(binding, true);
					} else if (UIEvents.EventTypes.REMOVE.equals(event
							.getProperty(UIEvents.EventTags.TYPE)) && oldObj instanceof MKeyBinding) {
						MKeyBinding binding = (MKeyBinding) oldObj;
						updateBinding(binding, false);
					}
				} else if (elementObj instanceof MKeyBinding) {
					MKeyBinding binding = (MKeyBinding) elementObj;
					String attrName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
					if (UIEvents.EventTypes.SET.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
						Object oldObj = event.getProperty(UIEvents.EventTags.OLD_VALUE);
						if (UIEvents.KeyBinding.COMMAND.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setCommand((MCommand) oldObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						} else if (UIEvents.KeySequence.KEYSEQUENCE.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setKeySequence((String) oldObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						}
					} else if (UIEvents.KeyBinding.PARAMETERS.equals(attrName)) {
						if (UIEvents.EventTypes.ADD.equals(event
								.getProperty(UIEvents.EventTags.TYPE))) {
							Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.getParameters().remove(newObj);
							updateBinding(oldBinding, false);
							updateBinding(binding, true);
						} else if (UIEvents.EventTypes.REMOVE.equals(event
								.getProperty(UIEvents.EventTags.TYPE))) {
							Object oldObj = event.getProperty(UIEvents.EventTags.OLD_VALUE);
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
		broker.subscribe(UIEvents.buildTopic(UIEvents.BindingTableContainer.TOPIC,
				UIEvents.BindingTableContainer.BINDINGTABLES), additionHandler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.BindingTable.TOPIC, UIEvents.BindingTable.BINDINGS),
				additionHandler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.KeyBinding.TOPIC, UIEvents.KeyBinding.COMMAND),
				additionHandler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.KeyBinding.TOPIC, UIEvents.KeyBinding.PARAMETERS),
				additionHandler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.KeySequence.TOPIC, UIEvents.KeySequence.KEYSEQUENCE),
				additionHandler);
	}

	private void unregsiterModelListeners() {
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
	}
}
