/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.commands.KeySupport;
import org.eclipse.ui.internal.commands.Manager;
import org.eclipse.ui.internal.commands.Sequence;
import org.eclipse.ui.internal.commands.SequenceMachine;
import org.eclipse.ui.internal.commands.Stroke;
import org.eclipse.ui.internal.registry.IActionSet;

public final class WWinKeyBindingService {

	private static class KeyModeContributionItem extends ContributionItem {

		private int fixedWidth = -1;
		private String text;
		private CLabel label;
	
		KeyModeContributionItem(String id) {
			super(id);
		}
	
		void setText(String msg) {
			text = msg;
			
			if (label != null && !label.isDisposed())
				label.setText(text);
			
			if (text == null || text.length() < 1) {
				if (isVisible()) {
					setVisible(false);
					getParent().update(true);
				}
			} else {
				if (!isVisible()) {
					setVisible(true);
					getParent().update(true);
				}
			}
		}
	
		public void fill(Composite parent) {
			label = new CLabel(parent, SWT.SHADOW_IN);
			StatusLineLayoutData data = new StatusLineLayoutData();
			
			if (fixedWidth < 0) {
				GC gc = new GC(parent);
				gc.setFont(parent.getFont());
				fixedWidth = gc.getFontMetrics().getAverageCharWidth() * 40;
				gc.dispose();
			}
			
			data.widthHint = fixedWidth;
			label.setLayoutData(data);
		
			if (text != null)
				label.setText(text);
		}
	}

	private final KeyModeContributionItem keyModeContributionItem = new KeyModeContributionItem("KeyModeContribution"); //$NON-NLS-1$

	private final IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			update(part);
		}
			
		public void partBroughtToTop(IWorkbenchPart part) {
		}
			
		public void partClosed(IWorkbenchPart part) {
		}
			
		public void partDeactivated(IWorkbenchPart part) {
			clear();
		}
			
		public void partOpened(IWorkbenchPart part) {
		}
	};
		
	private final ShellListener shellListener = new ShellAdapter() {
		public void shellDeactivated(ShellEvent e) {
			clear();
		}
	};

	private final VerifyListener verifyListener = new VerifyListener() {
		public void verifyText(VerifyEvent event) {
			event.doit = false;
			clear();
		}
	};

	private AcceleratorMenu acceleratorMenu;
	private SortedMap actionSetsCommandIdToActionMap = new TreeMap();
	private KeyBindingService activeKeyBindingService;
	private SortedMap globalActionsCommandIdToActionMap = new TreeMap();
	private WorkbenchWindow workbenchWindow;
	
	WWinKeyBindingService(WorkbenchWindow workbenchWindow) {
		this.workbenchWindow = workbenchWindow;
		workbenchWindow.getStatusLineManager().add(keyModeContributionItem);		
		workbenchWindow.getPartService().addPartListener(partListener);
		final WorkbenchWindow finalWorkbenchWindow = this.workbenchWindow; 
		
		this.workbenchWindow.addPageListener(new IPageListener() {			
			public void pageActivated(IWorkbenchPage page) {
			}
			
			public void pageClosed(IWorkbenchPage page) {
			}
			
			public void pageOpened(IWorkbenchPage page) {
				page.addPartListener(partListener);
				update(page.getActivePart());
				finalWorkbenchWindow.getShell().removeShellListener(shellListener);				
				finalWorkbenchWindow.getShell().addShellListener(shellListener);				
			}
		});		
	}

	public void clear() {		
		Manager.getInstance().getKeyMachine().setMode(Sequence.create());
		keyModeContributionItem.setText(""); //$NON-NLS-1$	
		updateAccelerators();
		MenuManager menuManager = workbenchWindow.getMenuManager();
		menuManager.update(IAction.TEXT);		
	}

	IAction getAction(String command) {
		IAction action = null;
		
		if (activeKeyBindingService != null)
			action = (IAction) activeKeyBindingService.getAction(command);
    	
		if (action == null) {
			action = (IAction) actionSetsCommandIdToActionMap.get(command);
		
			if (action == null)
				action = (IAction) globalActionsCommandIdToActionMap.get(command);
		}
    	    		
		return action;
	}

	void pressed(Stroke stroke, Event event) { 
		Manager manager = Manager.getInstance();
		SequenceMachine keyMachine = manager.getKeyMachine();				
		List strokes = new ArrayList(keyMachine.getMode().getStrokes());
		strokes.add(stroke);
		Sequence childMode = Sequence.create(strokes);		
		Map sequenceMapForMode = keyMachine.getSequenceMapForMode();				
		keyMachine.setMode(childMode);
		Map childSequenceMapForMode = keyMachine.getSequenceMapForMode();

		if (childSequenceMapForMode.isEmpty()) {
			clear();
			String command = (String) sequenceMapForMode.get(childMode);

			if (command != null && activeKeyBindingService != null) {
				IAction action = getAction(command);
			
				if (action != null && action.isEnabled())
					action.runWithEvent(event);
			}
		}
		else {
			keyModeContributionItem.setText(KeySupport.formatSequence(childMode, true));
			updateAccelerators();
			MenuManager menuManager = workbenchWindow.getMenuManager();
			menuManager.update(IAction.TEXT);			
		}
	}

	void registerActionSets(IActionSet[] actionSets) {
		actionSetsCommandIdToActionMap.clear();
		
		for (int i = 0; i < actionSets.length; i++) {
			if (actionSets[i] instanceof PluginActionSet) {
				PluginActionSet pluginActionSet = (PluginActionSet) actionSets[i];
				IAction[] pluginActions = pluginActionSet.getPluginActions();
				
				for (int j = 0; j < pluginActions.length; j++) {
					IAction pluginAction = (IAction) pluginActions[j];
					String command = pluginAction.getActionDefinitionId();
					
					if (command != null)
						actionSetsCommandIdToActionMap.put(command, pluginAction);
				}
			}
		}
	}

	void registerGlobalAction(IAction globalAction) {
		String command = globalAction.getActionDefinitionId();

		if (command != null)		
			globalActionsCommandIdToActionMap.put(command, globalAction);
	}

	public void update(IWorkbenchPart workbenchPart) {
		String[] scopes = new String[] { "" }; //$NON-NLS-1$

		if (workbenchPart != null) {
			activeKeyBindingService = (KeyBindingService) workbenchPart.getSite().getKeyBindingService();
		
			if (activeKeyBindingService != null)
				scopes = activeKeyBindingService.getScopes();
		}

		try {
			Manager.getInstance().getKeyMachine().setScopes(scopes);
		} catch (IllegalArgumentException eIllegalArgument) {
			System.err.println(eIllegalArgument);
		}

		clear();
	}

	private void updateAccelerators() {
		SequenceMachine keyMachine = Manager.getInstance().getKeyMachine();      		
		Sequence mode = keyMachine.getMode();
		List strokes = mode.getStrokes();
		int size = strokes.size();		
		Map sequenceMapForMode = keyMachine.getSequenceMapForMode();
		SortedSet strokeSetForMode = new TreeSet();
		Iterator iterator = sequenceMapForMode.entrySet().iterator();

		if (activeKeyBindingService != null) {					
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
			acceleratorMenu = new AcceleratorMenu(parent);
		}

		acceleratorMenu.setAccelerators(accelerators);		
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
			}
		});

		if (size == 0)
			acceleratorMenu.removeVerifyListener(verifyListener);
		else
			acceleratorMenu.addVerifyListener(verifyListener);
	}
}
