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

package org.eclipse.ui.commands.internal;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IActionServiceListener;
import org.eclipse.ui.commands.IContextService;
import org.eclipse.ui.commands.IContextServiceListener;
import org.eclipse.ui.internal.AcceleratorMenu;
import org.eclipse.ui.internal.CoolBarManager;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.commands.ContextBinding;
import org.eclipse.ui.internal.commands.CoreRegistry;
import org.eclipse.ui.internal.commands.KeySupport;
import org.eclipse.ui.internal.commands.LocalRegistry;
import org.eclipse.ui.internal.commands.Manager;
import org.eclipse.ui.internal.commands.PreferenceRegistry;
import org.eclipse.ui.internal.commands.Sequence;
import org.eclipse.ui.internal.commands.SequenceMachine;
import org.eclipse.ui.internal.commands.Stroke;

public class ActionAndContextManager implements IContextResolver {

	private final StatusLineContributionItem modeContributionItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

	private final IActionServiceListener actionServiceListener = new IActionServiceListener() {
		public void actionServiceChanged(IActionService actionService) {
			ActionAndContextManager.this.actionServiceChanged();
		}
	};			
	
	private final IContextServiceListener contextServiceListener = new IContextServiceListener() {
		public void contextServiceChanged(IContextService contextService) {
			ActionAndContextManager.this.contextServiceChanged();
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
	//private IActionService activeWorkbenchPageActionService;
	//private IContextService activeWorkbenchPageContextService;
	private IActionService activeWorkbenchPartActionService;
	private IContextService activeWorkbenchPartContextService;
	private WorkbenchWindow workbenchWindow;
	private IActionService workbenchWindowActionService;
	private IContextService workbenchWindowContextService;
	private Map contextsByCommand;

	public ActionAndContextManager(WorkbenchWindow workbenchWindow) {
		super();
		this.workbenchWindow = workbenchWindow;	
		workbenchWindow.getStatusLineManager().add(modeContributionItem);							
		workbenchWindowActionService = ((WorkbenchWindow) workbenchWindow).getActionService();
		workbenchWindowActionService.addActionServiceListener(actionServiceListener);	
		workbenchWindowContextService = ((WorkbenchWindow) workbenchWindow).getContextService();
		workbenchWindowContextService.addContextServiceListener(contextServiceListener);
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

	private void actionServiceChanged() {
		update();
	}
	
	private void contextServiceChanged() {
		update();
	}	

	private void partEvent() {
		IActionService activeWorkbenchPartActionService = null;
		IContextService activeWorkbenchPartContextService = null;
		IWorkbenchPage activeWorkbenchPage = workbenchWindow.getActivePage();
	 
		if (activeWorkbenchPage != null) {
			IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage.getActivePart();
	
			if (activeWorkbenchPart != null) {
				IWorkbenchPartSite activeWorkbenchPartSite = activeWorkbenchPart.getSite();
			
				if (activeWorkbenchPartSite != null) {
					activeWorkbenchPartActionService = ((PartSite) activeWorkbenchPartSite).getActionService();
					activeWorkbenchPartContextService = ((PartSite) activeWorkbenchPartSite).getContextService();
				}
			}
		}

		boolean updateRequired = false;
				
		if (this.activeWorkbenchPartActionService != activeWorkbenchPartActionService) {
			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.removeActionServiceListener(actionServiceListener);				
			
			this.activeWorkbenchPartActionService = activeWorkbenchPartActionService;
			
			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.addActionServiceListener(actionServiceListener);
				
			updateRequired = true;				
		}
	
		if (this.activeWorkbenchPartContextService != activeWorkbenchPartContextService) {
			if (this.activeWorkbenchPartContextService != null)
				this.activeWorkbenchPartContextService.removeContextServiceListener(contextServiceListener);				
			
			this.activeWorkbenchPartContextService = activeWorkbenchPartContextService;
			
			if (this.activeWorkbenchPartContextService != null)
				this.activeWorkbenchPartContextService.addContextServiceListener(contextServiceListener);
				
			updateRequired = true;				
		}
	
		if (updateRequired)
			update();
	}

	private void shellEvent() {
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
			IAction action = getAction((String) sequenceMapForMode.get(childMode));
			
			if (action != null && action.isEnabled())
				action.runWithEvent(event);
		}
		else {
			modeContributionItem.setText(KeySupport.formatSequence(childMode, true));
			update();	
		}
	}

	private IAction getAction(String command) {
		if (command != null) {
			if (activeWorkbenchPartActionService != null) {
				SortedMap actionMap = activeWorkbenchPartActionService.getActionMap();
				
				if (actionMap != null) {
					Object object = actionMap.get(command);
					
					if (object instanceof IAction)
						return (IAction) object;
				}
			}

			if (workbenchWindowActionService != null) {
				SortedMap actionMap = workbenchWindowActionService.getActionMap();
				
				if (actionMap != null) {
					Object object = actionMap.get(command);
					
					if (object instanceof IAction)
						return (IAction) object;
				}
			}
		}
		
		return null;
	}

	public void update() {
		List contexts = new ArrayList();
		
		if (workbenchWindowContextService != null)
			contexts.addAll(workbenchWindowContextService.getContexts());

		if (activeWorkbenchPartContextService != null)
			contexts.addAll(activeWorkbenchPartContextService.getContexts());

		SequenceMachine keyMachine = Manager.getInstance().getKeyMachine();      		
			
		try {
			// TODO: get rid of this
			if (contexts.size() == 0)
				contexts.add(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
			
			keyMachine.setScopes((String[]) contexts.toArray(new String[contexts.size()]));
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
				IAction action = getAction(command);
				
				if (action != null)
					strokeSetForMode.add(sequence.getStrokes().get(size));	
			}
		}
		
		iterator = strokeSetForMode.iterator();
		int[] accelerators = new int[strokeSetForMode.size()];
		int i = 0;
			   	
		while (iterator.hasNext())
			accelerators[i++] = ((Stroke) iterator.next()).getValue();
		
		if (acceleratorMenu == null || acceleratorMenu.isDisposed()) {		
			Menu parent = workbenchWindow.getShell().getMenuBar();
			
			if (parent == null || parent.getItemCount() < 1)
				return;
			
			MenuItem parentItem = parent.getItem(parent.getItemCount() - 1);
			parent = parentItem.getMenu();
			
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

		/* TODO: coolbars are weird. make this work like it does for menus
		CoolBarManager coolBarManager = ((WorkbenchWindow) workbenchWindow).getCoolBarManager();
		
		if (coolBarManager != null)
			coolBarManager.update(true);
		*/
	}

	public boolean inContext(String commandId) {
		if (commandId != null) {
			Set set = (Set) contextsByCommand.get(commandId);
		
			if (set != null) {
				if (activeWorkbenchPartContextService != null) {
					List contexts = activeWorkbenchPartContextService.getContexts();

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
					List contexts = workbenchWindowContextService.getContexts();
		
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

				return false;				
			}
		}

		return true;			
	}
	
	void reset() {
		CoreRegistry coreRegistry = CoreRegistry.getInstance();
		LocalRegistry localRegistry = LocalRegistry.getInstance();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();
			
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
