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

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
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
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

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
	private ICommandManager commandManager;
	private IContextManager contextManager;

	public ContextAndHandlerManager(WorkbenchWindow workbenchWindow) {
		super();
		this.workbenchWindow = workbenchWindow;	
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
		CommandManager.getInstance().setMode(KeySequence.getInstance());
		modeContributionItem.setText(""); //$NON-NLS-1$	
		update();
	}

	// TODO remove event parameter
	private void pressed(int accelerator, Event event) { 
		KeyStroke keyStroke = org.eclipse.ui.keys.KeySupport.convertFromSWT(accelerator);		
		CommandManager commandManager = CommandManager.getInstance();				
		List keyStrokes = new ArrayList(commandManager.getMode().getKeyStrokes());
		keyStrokes.add(keyStroke);
		KeySequence childMode = KeySequence.getInstance(keyStrokes);		
		Map matchesByKeySequenceForMode = commandManager.getMatchesByKeySequenceForMode();				
		commandManager.setMode(childMode);
		Map childMatchesByKeySequenceForMode = commandManager.getMatchesByKeySequenceForMode();

		if (childMatchesByKeySequenceForMode.isEmpty()) {
			clear();
			Match match = (Match) matchesByKeySequenceForMode.get(childMode);
			
			if (match != null) {			
				org.eclipse.ui.commands.IAction action = (org.eclipse.ui.commands.IAction) commandManager.getActionsById().get(match.getCommandId());
				
				if (action != null && action.isEnabled())
					try {			
						action.execute(event);
					} catch (Exception e) {
						// TODO
					}
			}
		}
		else {
			modeContributionItem.setText(childMode.format());
			update();	
		}
	}

	public boolean inContext(String commandId) {
		/* TODO
		if (commandId != null) {
			ICommand command = commandManager.getCommand(commandId);

			if (command != null) {
				return command.isDefined() && command.isActive();
			}
		}
		*/		

		return true;			
	}

	public void update() {
		List activeContextIds = new ArrayList(contextManager.getActiveContextIds());				
		activeContextIds.add(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);	// TODO remove	
		CommandManager commandManager = CommandManager.getInstance();
		commandManager.setActiveContextIds(activeContextIds);
		KeySequence mode = commandManager.getMode();
		List keyStrokes = mode.getKeyStrokes();
		int size = keyStrokes.size();	
		Map matchesByKeySequenceForMode = commandManager.getMatchesByKeySequenceForMode();		
		SortedSet keyStrokeSetForMode = new TreeSet();
		Iterator iterator = matchesByKeySequenceForMode.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();
			Match match = (Match) entry.getValue();		

			if (match != null && keySequence.isChildOf(mode, false)) {
				// TODO uncomment?
				//org.eclipse.ui.commands.IAction action = (org.eclipse.ui.commands.IAction) commandManager.getActionsById().get(match.getCommandId());
				
				//if (action != null)
					keyStrokeSetForMode.add(keySequence.getKeyStrokes().get(size));	
			}
		}
		
		iterator = keyStrokeSetForMode.iterator();
		int[] accelerators = new int[keyStrokeSetForMode.size()];
		int i = 0;
			   	
		while (iterator.hasNext())
			accelerators[i++] = org.eclipse.ui.keys.KeySupport.convertToSWT((KeyStroke) iterator.next());
		
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
					pressed(selectionEvent.detail, event);
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
}
