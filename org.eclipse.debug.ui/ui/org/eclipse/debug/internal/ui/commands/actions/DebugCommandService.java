/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Updates commands for a window. Coalesces update requests by command type.
 * 
 * @since 3.3
 */
public class DebugCommandService implements IDebugContextListener {
	
	/**
	 * Maps command types to actions to update
	 */
	private Map fCommandUpdates = new HashMap();
	
	/**
	 * Window this service is for.
	 */
	private IWorkbenchWindow fWindow = null;
	
	/**
	 * The context service for this command service.
	 */
	private IDebugContextService fContextService = null;
	
	/**
	 * Service per window
	 */
	private static Map fgServices = new HashMap();
		
	/**
	 * Returns the service for a window.
	 * 
	 * @param window the window
	 * @return service
	 */
	public synchronized static DebugCommandService getService(IWorkbenchWindow window) {
		DebugCommandService service = (DebugCommandService) fgServices.get(window);
		if (service == null) {
			service = new DebugCommandService(window);
			fgServices.put(window, service);
		}
		return service;
	}
	
	public DebugCommandService(IWorkbenchWindow window) {
		fWindow = window;
		fContextService = DebugUITools.getDebugContextManager().getContextService(window);
		fContextService.addPostDebugContextListener(this);
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
		
			public void windowOpened(IWorkbenchWindow w) {
			}
		
			public void windowDeactivated(IWorkbenchWindow w) {
			}
		
			public void windowClosed(IWorkbenchWindow w) {
				if (fWindow == w) {
					dispose();
				}
			}
		
			public void windowActivated(IWorkbenchWindow w) {
			}
		
		});
	}
	
	private void dispose() {
		fContextService.removeDebugContextListener(this);
		fgServices.remove(fWindow);
		fCommandUpdates.clear();
		fWindow = null;
	}
	
	/**
	 * Updates the given command type after the next context change.
	 * 
	 * @param commandType the command class
	 * @param action the action to add to the update list
	 */
	public void postUpdateCommand(Class commandType, IEnabledTarget action) {
		synchronized (fCommandUpdates) {
			Job.getJobManager().cancel(commandType);
			List actions = (List) fCommandUpdates.get(commandType);
			if (actions == null) {
				actions = new ArrayList();
				fCommandUpdates.put(commandType, actions);
			}
			actions.add(action);					
		}
	}
	
	/**
	 * Updates the given command type based on the active context.
	 * 
	 * @param commandType the command class
	 * @param action the action to update
	 */
	public void updateCommand(Class commandType, IEnabledTarget action) {
		ISelection context = fContextService.getActiveContext();
		if (context instanceof IStructuredSelection && !context.isEmpty()) {
			Object[] elements = ((IStructuredSelection)context).toArray();
			updateCommand(commandType, elements, new IEnabledTarget[]{action});
		} else {
			action.setEnabled(false);
		}
	}	
	
	private void postUpdate(ISelection context) {
		Map commands = null;
		synchronized (fCommandUpdates) {
			commands = fCommandUpdates;
			fCommandUpdates = new HashMap(commands.size());
		}
		if (context instanceof IStructuredSelection && !context.isEmpty()) {
			Object[] elements = ((IStructuredSelection)context).toArray();
			Iterator iterator = commands.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry entry = (Entry) iterator.next();
				Class commandType = (Class)entry.getKey();
				List actions = (List) entry.getValue();
				updateCommand(commandType, elements, (IEnabledTarget[]) actions.toArray(new IEnabledTarget[actions.size()]));
			}
		} else {
			Iterator iterator = commands.values().iterator();
			while (iterator.hasNext()) {
				List actionList = (List) iterator.next();
				Iterator actions = actionList.iterator();
				while (actions.hasNext()) {
					((IEnabledTarget)actions.next()).setEnabled(false);
				}
			}
		}
		commands.clear();		
	}
	
	/**
	 * Updates the given command type for the specified elements.
	 * @param handlerType the handle type class 
	 * @param elements elements to update for
	 * @param actions the actions to update
	 */
	private void updateCommand(Class handlerType, Object[] elements, IEnabledTarget[] actions) {
		if (elements.length == 1) {
			// usual case - one element
			Object element = elements[0];
			IDebugCommandHandler handler = getHandler(element, handlerType);
			if (handler != null) {
				UpdateActionsRequest request = new UpdateActionsRequest(elements, actions);
				handler.canExecute(request);
				return;
			}
		} else {
			Map map = collate(elements, handlerType);
			if (map != null) {
				ActionsUpdater updater = new ActionsUpdater(actions, map.size());
				Iterator entries = map.entrySet().iterator();
				while (entries.hasNext()) {
					Entry entry = (Entry) entries.next();
					IDebugCommandHandler handler = (IDebugCommandHandler) entry.getKey();
					List list = (List) entry.getValue();
					UpdateHandlerRequest request = new UpdateHandlerRequest(list.toArray(), updater);
					handler.canExecute(request);
				}
				return;
			}
		}
		// ABORT - no command processors
		for (int i = 0; i < actions.length; i++) {
			actions[i].setEnabled(false);
		}
	}
	
	/**
	 * Updates the given command type for the specified elements.
	 * @param handlerType the handler type class 
	 * @param elements elements to update for
	 * @param participant the participant
	 * @return if the command stays enabled while the command executes
	 */
	public boolean executeCommand(Class handlerType, Object[] elements, ICommandParticipant participant) {
		if (elements.length == 1) {
			// usual case - one element
			Object element = elements[0];
			IDebugCommandHandler handler = getHandler(element, handlerType);
			if (handler != null) {
				ExecuteActionRequest request = new ExecuteActionRequest(elements);
				request.setCommandParticipant(participant);
				return handler.execute(request);
			}
		} else {
			Map map = collate(elements, handlerType);
			if (map != null) {
				boolean enabled = true;
				Iterator entries = map.entrySet().iterator();
				while (entries.hasNext()) {
					Entry entry = (Entry) entries.next();
					IDebugCommandHandler handler = (IDebugCommandHandler) entry.getKey();
					List list = (List) entry.getValue();
					ExecuteActionRequest request = new ExecuteActionRequest(list.toArray());
					request.setCommandParticipant(participant);
					// specifically use & so handler is executed
					enabled = enabled & handler.execute(request);
				}
				return enabled;
			}
		}
		// ABORT - no command processors
		return false;
	}	

	public void debugContextChanged(DebugContextEvent event) {
		postUpdate(event.getContext());
	}	
	
	/**
	 * Returns a map of command handlers to associated elements, or <code>null</code> if 
	 * one is missing.
	 * 
	 * @param elements the elements
	 * @param handlerType the handler type class
	 * @return map of command handlers to associated elements or <code>null</code>
	 */
	private Map collate(Object[] elements, Class handlerType) {
		Map map = new HashMap();
 		for (int i = 0; i < elements.length; i++) {
 			Object element = elements[i];
 			IDebugCommandHandler handler = getHandler(element, handlerType);
			if (handler == null) {
				return null;
			} else {
				List list = (List) map.get(handler);
				if (list == null) {
					list = new ArrayList();
					map.put(handler, list);
	 				}
				list.add(element);
	 			}
	 		}
		return map;
	}
	
	private IDebugCommandHandler getHandler(Object element, Class handlerType) {
		return (IDebugCommandHandler)DebugPlugin.getAdapter(element, handlerType);
	}

}
