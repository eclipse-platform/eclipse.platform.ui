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
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.AcceleratorMenu;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.commands.CommandManager;

public class ContextAndHandlerManager implements IContextResolver {

	private final StatusLineContributionItem modeContributionItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

	private final ICommandManagerListener commandManagerListener = new ICommandManagerListener() {
		public void commandManagerChanged(ICommandManagerEvent commandManagerEvent) {
			update();
		}
	};

	private final IContextManagerListener contextManagerListener = new IContextManagerListener() {
		public void contextManagerChanged(IContextManagerEvent contextManagerEvent) {
			update();
		}
	};
	
	private final IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage workbenchPage) {
			update();		
		}
			
		public void pageClosed(IWorkbenchPage workbenchPage) {
			update();		
		}
			
		public void pageOpened(IWorkbenchPage workbenchPage) {
			update();		
		}
	};

	private final IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart workbenchPart) {
			update();
		}
			
		public void partBroughtToTop(IWorkbenchPart workbenchPart) {
		}
			
		public void partClosed(IWorkbenchPart workbenchPart) {
			update();
		}
			
		public void partDeactivated(IWorkbenchPart workbenchPart) {
			update();
		}
			
		public void partOpened(IWorkbenchPart workbenchPart) {
			update();
		}
	};

	private final ShellListener shellListener = new ShellAdapter() {
		public void shellActivated(ShellEvent shellEvent) {
			update();
		}

		public void shellDeactivated(ShellEvent shellEvent) {
			update();
		}
	};

	private final VerifyListener verifyListener = new VerifyListener() {
		public void verifyText(VerifyEvent verifyEvent) {
			verifyEvent.doit = false;
			clear();
		}
	};

	private AcceleratorMenu acceleratorMenu;		
	private WorkbenchWindow workbenchWindow;
	private Map contextsByCommand;
	private ICommandManager commandManager;
	private IContextManager contextManager;

	public ContextAndHandlerManager(WorkbenchWindow workbenchWindow) {
		super();
		this.workbenchWindow = workbenchWindow;	
		reset();
		IWorkbench workbench = workbenchWindow.getWorkbench();
		commandManager = ((Workbench) workbench).getCommandManager(); // TODO temporary cast
		commandManager.addCommandManagerListener(commandManagerListener);				
		contextManager = ((Workbench) workbench).getContextManager(); // TODO temporary cast
		contextManager.addContextManagerListener(contextManagerListener);		
		workbenchWindow.getStatusLineManager().add(modeContributionItem);							
		this.workbenchWindow.addPageListener(pageListener);		
		this.workbenchWindow.getPartService().addPartListener(partListener);		
		Shell shell = workbenchWindow.getShell();
		
		if (shell != null)
			shell.addShellListener(shellListener);

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
			org.eclipse.ui.commands.IAction action = (org.eclipse.ui.commands.IAction) ((CommandManager) commandManager).getActionsById().get((String) sequenceMapForMode.get(childMode));
			
			if (action != null && action.isEnabled())
				try {			
					action.execute(event);
				} catch (Exception e) {
					// TODO
				}
		}
		else {
			modeContributionItem.setText(KeySupport.formatSequence(childMode, true));
			update();	
		}
	}

	public void update() {
		List contexts = new ArrayList(contextManager.getActiveContextIds());
		// TODO: contexts should be sorted somehow to resolve conflicts
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
				org.eclipse.ui.commands.IAction action = (org.eclipse.ui.commands.IAction) ((CommandManager) commandManager).getActionsById().get(command);
				
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
			Set contextIds = (Set) contextsByCommand.get(commandId);
		
			if (contextIds != null) {
				List activeContextIds = contextManager.getActiveContextIds();			
				Iterator iterator = contextIds.iterator();
				
				while (iterator.hasNext()) {
					String contextId = (String) iterator.next();
					
					if (activeContextIds.contains(contextId))
						return true;
				}

				return false;				
			}
		}

		return true;			
	}
	
	void reset() {
		AbstractRegistry coreRegistry = CoreRegistry.getInstance();
		AbstractMutableRegistry preferenceRegistry = PreferenceRegistry.getInstance();
			
		try {
			coreRegistry.load();
		} catch (IOException eIO) {
		}
	
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}		

		List contextBindings = new ArrayList();
		contextBindings.addAll(coreRegistry.getContextBindings());
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
