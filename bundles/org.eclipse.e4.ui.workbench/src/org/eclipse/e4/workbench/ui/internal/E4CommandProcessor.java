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

package org.eclipse.e4.workbench.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MBindingContext;
import org.eclipse.e4.ui.model.application.MBindingTable;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MCommandParameter;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

/**
 *
 */
public class E4CommandProcessor {
	public static void processCommands(IEclipseContext context, List<MCommand> commands) {
		// fill in commands
		Activator.trace(Policy.DEBUG_CMDS, "Initialize commands from model", null); //$NON-NLS-1$
		ECommandService cs = (ECommandService) context.get(ECommandService.class.getName());
		Category cat = cs
				.defineCategory(MApplication.class.getName(), "Application Category", null); //$NON-NLS-1$
		for (MCommand cmd : commands) {
			IParameter[] parms = null;
			String id = cmd.getId();
			String name = cmd.getCommandName();
			EList<MCommandParameter> modelParms = cmd.getParameters();
			if (modelParms != null && !modelParms.isEmpty()) {
				ArrayList<Parameter> parmList = new ArrayList<Parameter>();
				for (MCommandParameter cmdParm : modelParms) {
					parmList.add(new Parameter(cmdParm.getId(), cmdParm.getName(), null, null,
							cmdParm.isOptional()));
				}
				parms = parmList.toArray(new Parameter[parmList.size()]);
			}
			cs.defineCommand(id, name, null, cat, parms);
		}

	}

	public static void processBindings(IEclipseContext context, MBindingContainer bindingContainer) {
		Activator.trace(Policy.DEBUG_CMDS, "Initialize binding tables from model", null); //$NON-NLS-1$
		final BindingTableManager bindingTables;
		try {
			bindingTables = (BindingTableManager) ContextInjectionFactory.make(
					BindingTableManager.class, context);
			context.set(BindingTableManager.class.getName(), bindingTables);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		MBindingContext root = bindingContainer.getRootContext();
		if (root == null) {
			return;
		}
		final ContextManager manager = (ContextManager) context.get(ContextManager.class.getName());
		defineContexts(null, root, manager);
		for (MBindingTable bt : bindingContainer.getBindingTables()) {
			Context c = manager.getContext(bt.getBindingContextId());
			defineBindingTable(context, c, bindingTables, bt);
		}

		((EObject) bindingContainer).eAdapters().add(new EContentAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify
			 * .Notification)
			 */
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				if (notification.isTouch()) {
					return;
				}

				if (notification.getEventType() == Notification.ADD
						&& notification.getNewValue() instanceof MBindingTable) {
					MBindingTable bt = (MBindingTable) notification.getNewValue();
					Context bindingContext = manager.getContext(bt.getBindingContextId());
					BindingTable table = new BindingTable(bindingContext);
					bindingTables.addTable(table);
				}
			}
		});
	}

	private static void defineBindingTable(IEclipseContext context, Context bindingContext,
			BindingTableManager manager, MBindingTable bt) {
		BindingTable table = new BindingTable(bindingContext);
		manager.addTable(table);
		ECommandService cs = (ECommandService) context.get(ECommandService.class.getName());
		EBindingService bs = (EBindingService) context.get(EBindingService.class.getName());
		EList<MKeyBinding> bindings = bt.getBindings();
		for (MKeyBinding binding : bindings) {
			Map<String, Object> parameters = null;
			EList<MParameter> modelParms = binding.getParameters();
			if (modelParms != null && !modelParms.isEmpty()) {
				parameters = new HashMap<String, Object>();
				for (MParameter mParm : modelParms) {
					parameters.put(mParm.getTag(), mParm.getValue());
				}
			}
			ParameterizedCommand cmd = cs.createCommand(binding.getCommand().getId(), parameters);
			TriggerSequence sequence = bs.createSequence(binding.getKeySequence());
			if (cmd == null || sequence == null) {
				System.err.println("Failed to handle binding: " + binding); //$NON-NLS-1$
			} else {
				Binding keyBinding = bs.createBinding(sequence, cmd,
						"org.eclipse.ui.defaultAcceleratorConfiguration", bindingContext.getId()); //$NON-NLS-1$
				table.addBinding(keyBinding);
			}
		}
	}

	private static void defineContexts(MBindingContext parent, MBindingContext current,
			ContextManager manager) {
		Context context = manager.getContext(current.getId());
		if (!context.isDefined()) {
			context.define(current.getName(), current.getDescription(), parent == null ? null
					: parent.getId());
		}
		for (MBindingContext child : current.getChildren()) {
			defineContexts(current, child, manager);
		}
	}
}
