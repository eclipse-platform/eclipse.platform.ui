/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands.old;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.action.ContextResolver;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContextResolver;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.old.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.old.ICommandHandler;
import org.eclipse.ui.commands.old.ICommandHandlerService;
import org.eclipse.ui.commands.old.ICommandHandlerServiceListener;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.AcceleratorMenu;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.commands.registry.old.ContextBinding;
import org.eclipse.ui.internal.commands.registry.old.CoreRegistry;
import org.eclipse.ui.internal.commands.registry.old.IMutableRegistry;
import org.eclipse.ui.internal.commands.registry.old.IRegistry;
import org.eclipse.ui.internal.commands.registry.old.LocalRegistry;
import org.eclipse.ui.internal.commands.registry.old.PreferenceRegistry;
import org.eclipse.ui.internal.commands.util.old.KeySupport;
import org.eclipse.ui.internal.commands.util.old.Sequence;
import org.eclipse.ui.internal.commands.util.old.Stroke;

public class ContextAndHandlerManager implements IContextResolver {

	private final StatusLineContributionItem modeContributionItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

	private final IContextManagerListener contextManagerListener = new IContextManagerListener() {
		public void contextManagerChanged(IContextManagerEvent contextManagerEvent) {
			update();
		}
	};

	private final ICommandHandlerServiceListener handlerServiceListener = new ICommandHandlerServiceListener() {
		public void handlerServiceChanged(CommandHandlerServiceEvent handlerServiceEvent) {
			update();
		}
	};

	private final IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart workbenchPart) {
			partEvent();
		}
			
		public void partBroughtToTop(IWorkbenchPart workbenchPart) {
		}
			
		public void partClosed(IWorkbenchPart workbenchPart) {
			partEvent();
		}
			
		public void partDeactivated(IWorkbenchPart workbenchPart) {
			partEvent();
		}
			
		public void partOpened(IWorkbenchPart workbenchPart) {
			partEvent();
		}
	};

	private final ShellListener shellListener = new ShellAdapter() {
		public void shellActivated(ShellEvent shellEvent) {
			shellEvent();
		}

		public void shellDeactivated(ShellEvent shellEvent) {
			shellEvent();
		}
	};

	private final VerifyListener verifyListener = new VerifyListener() {
		public void verifyText(VerifyEvent verifyEvent) {
			verifyEvent.doit = false;
			clear();
		}
	};

	private AcceleratorMenu acceleratorMenu;		
	private IContextActivationService activeWorkbenchPartContextService;
	private ICommandHandlerService activeWorkbenchPartHandlerService;
	private WorkbenchWindow workbenchWindow;
	private IContextActivationService workbenchWindowContextService;
	private ICommandHandlerService workbenchWindowHandlerService;
	private Map contextsByCommand;
	private IContextManager contextManager;
	private boolean start = false;

	public ContextAndHandlerManager(WorkbenchWindow workbenchWindow) {
		super();
		this.workbenchWindow = workbenchWindow;	
		IWorkbench workbench = workbenchWindow.getWorkbench();
		contextManager = ((Workbench) workbench).getContextManager(); // TODO temporary cast
		contextManager.addContextManagerListener(contextManagerListener);		
		workbenchWindowHandlerService = ((WorkbenchWindow) workbenchWindow).getHandlerService();
		workbenchWindowHandlerService.addHandlerServiceListener(handlerServiceListener);
		workbenchWindow.getStatusLineManager().add(modeContributionItem);							
		reset();

		this.workbenchWindow.addPageListener(new IPageListener() {			
			public void pageActivated(IWorkbenchPage workbenchPage) {
				workbenchPage.addPartListener(partListener);		
			}
			
			public void pageClosed(IWorkbenchPage workbenchPage) {
				workbenchPage.removePartListener(partListener);			
			}
			
			public void pageOpened(IWorkbenchPage workbenchPage) {
				workbenchPage.addPartListener(partListener);			
			}
		});
		
		workbenchWindow.getPartService().addPartListener(partListener);						
		
		Shell shell = workbenchWindow.getShell();
		
		if (shell != null)
			shell.addShellListener(shellListener);

		partEvent();
	}

	private void partEvent() {
		IContextActivationService activeWorkbenchPartContextService = null;
		ICommandHandlerService activeWorkbenchPartHandlerService = null;
		IWorkbenchPage activeWorkbenchPage = workbenchWindow.getActivePage();
	 
		if (activeWorkbenchPage != null) {
			IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage.getActivePart();
	
			if (activeWorkbenchPart != null) {
				IWorkbenchPartSite activeWorkbenchPartSite = activeWorkbenchPart.getSite();
			
				if (activeWorkbenchPartSite != null) {
					activeWorkbenchPartContextService = ((PartSite) activeWorkbenchPartSite).getContextActivationService();
					activeWorkbenchPartHandlerService = ((PartSite) activeWorkbenchPartSite).getHandlerService();
				}
			}
		}

		if (this.activeWorkbenchPartHandlerService != activeWorkbenchPartHandlerService) {
			if (this.activeWorkbenchPartHandlerService != null)
				this.activeWorkbenchPartHandlerService.removeHandlerServiceListener(handlerServiceListener);				
			
			this.activeWorkbenchPartHandlerService = activeWorkbenchPartHandlerService;
			
			if (this.activeWorkbenchPartHandlerService != null)
				this.activeWorkbenchPartHandlerService.addHandlerServiceListener(handlerServiceListener);
				
			start = true;
			update();				
		}
	}

	private void shellEvent() {
		start = true;
		update();
	}

	private void clear() {		
		Manager.getInstance().getKeyMachine().setMode(Sequence.create());
		modeContributionItem.setText(""); //$NON-NLS-1$	
		update();
	}

	private void pressed(Stroke stroke, Event event) { 
		SequenceMachine keyMachine = Manager.getInstance().getKeyMachine();				
		List strokes = new ArrayList(keyMachine.getMode().getStrokes());
		strokes.add(stroke);
		Sequence childMode = Sequence.create(strokes);		
		Map sequenceMapForMode = keyMachine.getSequenceMapForMode();				
		keyMachine.setMode(childMode);
		Map childSequenceMapForMode = keyMachine.getSequenceMapForMode();

		if (childSequenceMapForMode.isEmpty()) {
			clear();
			ICommandHandler handler = getHandler((String) sequenceMapForMode.get(childMode));
			
			if (handler != null && handler.isEnabled())
				try {			
					handler.execute(event);
				} catch (Exception e) {
					// TODO
				}
		}
		else {
			modeContributionItem.setText(KeySupport.formatSequence(childMode, true));
			update();	
		}
	}

	private ICommandHandler getHandler(String command) {
		if (command != null) {
			if (activeWorkbenchPartHandlerService != null) {
				SortedMap handlerMap = activeWorkbenchPartHandlerService.getHandlerMap();
				
				if (handlerMap != null) {
					Object object = handlerMap.get(command);
					
					if (object instanceof ICommandHandler)
						return (ICommandHandler) object;
				}
			}

			if (workbenchWindowHandlerService != null) {
				SortedMap handlerMap = workbenchWindowHandlerService.getHandlerMap();
				
				if (handlerMap != null) {
					Object object = handlerMap.get(command);
					
					if (object instanceof ICommandHandler)
						return (ICommandHandler) object;
				}
			}
		}
		
		return null;
	}

	public void update() {
		SortedSet activeContextIds = contextManager.getActiveContextIds();
		List contexts = new ArrayList(activeContextIds);
		// TODO: these should be sorted somehow
		SequenceMachine keyMachine = Manager.getInstance().getKeyMachine();      		
			
		try {
			// TODO: get rid of this
			if (contexts.size() == 0)
				contexts.add(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
			
			keyMachine.setContexts((String[]) contexts.toArray(new String[contexts.size()]));
		} catch (IllegalArgumentException eIllegalArgument) {
			System.err.println(eIllegalArgument);
		}

		Sequence mode = keyMachine.getMode();
		List strokes = mode.getStrokes();
		int size = strokes.size();		
		Map sequenceMapForMode = keyMachine.getSequenceMapForMode();
		SortedSet strokeSetForMode = new TreeSet();
		Iterator iterator = sequenceMapForMode.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Sequence sequence = (Sequence) entry.getKey();
			String command = (String) entry.getValue();		

			if (sequence.isChildOf(mode, false)) {
				ICommandHandler handler = getHandler(command);
				
				if (handler != null)
					strokeSetForMode.add(sequence.getStrokes().get(size));	
			}
		}
		
		iterator = strokeSetForMode.iterator();
		int[] accelerators = new int[strokeSetForMode.size()];
		int i = 0;
			   	
		while (iterator.hasNext())
			accelerators[i++] = ((Stroke) iterator.next()).getValue();
		
		if (!start)
			return;
		
		if (acceleratorMenu == null || acceleratorMenu.isDisposed()) {		
			Shell shell = workbenchWindow.getShell();
			
			if (shell == null || shell.isDisposed())
				return;
				
			Menu parent = shell.getMenuBar();
			
			if (parent == null || parent.isDisposed() || parent.getItemCount() < 1)
				return;
			
			MenuItem parentItem = parent.getItem(parent.getItemCount() - 1);
			
			if (parentItem == null || parentItem.isDisposed())
				return;
			
			parent = parentItem.getMenu();

			if (parent == null || parent.isDisposed())
				return;
				
			if (acceleratorMenu != null)
				acceleratorMenu.dispose();
			
			acceleratorMenu = new AcceleratorMenu(parent);
			acceleratorMenu.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent selectionEvent) {
					Event event = new Event();
					event.item = selectionEvent.item;
					event.detail = selectionEvent.detail;
					event.x = selectionEvent.x;
					event.y = selectionEvent.y;
					event.width = selectionEvent.width;
					event.height = selectionEvent.height;
					event.stateMask = selectionEvent.stateMask;
					event.doit = selectionEvent.doit;
					event.data = selectionEvent.data;
					event.display = selectionEvent.display;
					event.time = selectionEvent.time;
					event.widget = selectionEvent.widget;
					pressed(Stroke.create(selectionEvent.detail), event);
					//pressed(Stroke.create(selectionEvent.detail));
				}
			});
		}

		acceleratorMenu.setAccelerators(accelerators);		
	
		if (size == 0)
			acceleratorMenu.removeVerifyListener(verifyListener);
		else
			acceleratorMenu.addVerifyListener(verifyListener);

		ContextResolver.getInstance().setContextResolver(this);
		MenuManager menuManager = ((WorkbenchWindow) workbenchWindow).getMenuManager();
		menuManager.update(IAction.TEXT);

		/* TODO make this work like it does for menus
		CoolBarManager coolBarManager = ((WorkbenchWindow) workbenchWindow).getCoolBarManager();
		
		if (coolBarManager != null)
			coolBarManager.update(true);
		*/
	}

	public boolean inContext(String commandId) {
		if (commandId != null) {
			Set set = (Set) contextsByCommand.get(commandId);
		
			if (set != null) {
				/*
				if (activeWorkbenchPartContextService != null) {
					List contexts = Arrays.asList(activeWorkbenchPartContextService.getActiveContextIds());

					if (contexts != null) {
						// TODO: get rid of this
						contexts = new ArrayList(contexts);
						if (contexts.size() == 0)
							contexts.add(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
			
						Iterator iterator = contexts.iterator();
				
						while (iterator.hasNext()) {
							String context = (String) iterator.next();
					
							if (set.contains(context))
								return true;
						}
					}
				}

				if (workbenchWindowContextService != null) {
					List contexts = Arrays.asList(workbenchWindowContextService.getActiveContextIds());
		
					if (contexts != null) {
						// TODO: get rid of this
						contexts = new ArrayList(contexts);
						if (contexts.size() == 0)
							contexts.add(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);

						Iterator iterator = contexts.iterator();
				
						while (iterator.hasNext()) {
							String context = (String) iterator.next();
					
							if (set.contains(context))
								return true;
						}
					}
				}
				*/

				return false;				
			}
		}

		return true;			
	}
	
	void reset() {
		IRegistry coreRegistry = CoreRegistry.getInstance();
		IMutableRegistry localRegistry = LocalRegistry.getInstance();
		IMutableRegistry preferenceRegistry = PreferenceRegistry.getInstance();
			
		try {
			coreRegistry.load();
		} catch (IOException eIO) {
		}
	
		try {
			localRegistry.load();
		} catch (IOException eIO) {
		}
	
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}		

		List contextBindings = new ArrayList();
		contextBindings.addAll(coreRegistry.getContextBindings());
		contextBindings.addAll(localRegistry.getContextBindings());
		contextBindings.addAll(preferenceRegistry.getContextBindings());	
		contextsByCommand = new TreeMap();
		Iterator iterator = contextBindings.iterator();
		
		while (iterator.hasNext()) {		
			ContextBinding contextBinding = (ContextBinding) iterator.next();
			String command = contextBinding.getCommand();
			String context = contextBinding.getContext();			
			Set set = (Set) contextsByCommand.get(command);
			
			if (set == null) {
				set = new TreeSet();
				contextsByCommand.put(command, set);
			}
			
			set.add(context);
		}
	}	
}
