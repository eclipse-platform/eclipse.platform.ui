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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.ui.internal.commands.Machine;
import org.eclipse.ui.internal.commands.Manager;
import org.eclipse.ui.internal.commands.Sequence;
import org.eclipse.ui.internal.commands.Stroke;
import org.eclipse.ui.internal.commands.Util;
import org.eclipse.ui.internal.registry.IActionSet;

public class WWinKeyBindingService {

	private static class KeyModeContributionItem extends ContributionItem {

		private int fixedWidth = -1;
		private String text;
		private CLabel label;
	
		public KeyModeContributionItem(String id) {
			super(id);
		}
	
		public void setText(String msg) {
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

	private final KeyModeContributionItem statusItem = new KeyModeContributionItem("KeyModeContribution"); //$NON-NLS-1$

	private HashMap globalActionDefIdToAction = new HashMap();
	private HashMap actionSetDefIdToAction = new HashMap();
	private IPropertyChangeListener propertyListener;
	private KeyBindingService activeService;
	private WorkbenchWindow window;
	private AcceleratorMenu acceleratorMenu;

	private VerifyListener verifyListener = new VerifyListener() {
		public void verifyText(VerifyEvent event) {
			event.doit = false;
			clear();
		}
	};

	public void clear() {		
		Manager manager = Manager.getInstance();
		Machine keyMachine = manager.getKeyMachine();
		keyMachine.setMode(Sequence.create());
		statusItem.setText(""); //$NON-NLS-1$	
		updateAccelerators();
	}
	
	public void pressed(Stroke stroke, Event event) { 
		Manager manager = Manager.getInstance();
		Machine keyMachine = manager.getKeyMachine();				
		Sequence mode = keyMachine.getMode();					
		List strokes = new ArrayList(keyMachine.getMode().getStrokes());
		strokes.add(stroke);
		Sequence childMode = Sequence.create(strokes);		
		Map sequenceMapForMode = keyMachine.getSequenceMapForMode();				
		keyMachine.setMode(childMode);
		Map childSequenceMapForMode = keyMachine.getSequenceMapForMode();

		if (childSequenceMapForMode.isEmpty()) {
			clear();
			String command = (String) sequenceMapForMode.get(childMode);

			if (command != null && activeService != null) {
				IAction action = activeService.getAction(command);
			
				if (action != null && action.isEnabled())
					action.runWithEvent(event);
			}
		}
		else {
			statusItem.setText(childMode.formatKeySequence());
			updateAccelerators();
		}
	}

	public WWinKeyBindingService(WorkbenchWindow workbenchWindow) {
		this.window = workbenchWindow;
		window.getStatusLineManager().add(statusItem);		
		IWorkbenchPage[] pages = window.getPages();
		
		final IPartListener partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
				update(part, false);
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
		
		final ShellListener shellListener = new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				clear();
			}
		};
		
		// TODO: Just use getPartService to add listener.		
		for (int i = 0; i < pages.length; i++) {
			pages[i].addPartListener(partListener);
		}
		
		window.addPageListener(new IPageListener() {			
			public void pageActivated(IWorkbenchPage page) {
			}
			
			public void pageClosed(IWorkbenchPage page) {
			}
			
			public void pageOpened(IWorkbenchPage page) {
				page.addPartListener(partListener);
				partListener.partActivated(page.getActivePart());
				window.getShell().removeShellListener(shellListener);				
				window.getShell().addShellListener(shellListener);				
			}
		});		
	}

	public void dispose() {
	}

	public void registerGlobalAction(IAction action) {
		globalActionDefIdToAction.put(action.getActionDefinitionId(),action);
	}

	public void registerActionSets(IActionSet sets[]) {
		actionSetDefIdToAction.clear();
		
		for (int i=0; i<sets.length; i++) {
			if (sets[i] instanceof PluginActionSet) {
				PluginActionSet set = (PluginActionSet)sets[i];
				IAction actions[] = set.getPluginActions();
				
				for (int j = 0; j < actions.length; j++) {
					Action action = (Action)actions[j];
					String defId = action.getActionDefinitionId();
					
					if (defId != null) {
						actionSetDefIdToAction.put(action.getActionDefinitionId(),action);
					}
				}
			}
		}
	}

	public HashMap getMapping() {
		// TODO this could be a performance problem.
		HashMap result = (HashMap) globalActionDefIdToAction.clone();
		result.putAll(actionSetDefIdToAction);
		return result;
	}

	public void update(IWorkbenchPart part, boolean force) {
		if (part == null)
			return;
   		
		String[] oldScopeIds = new String[0];
   		
		if (activeService != null)
			oldScopeIds = activeService.getScopeIds();
   			
		activeService = (KeyBindingService) part.getSite().getKeyBindingService();
		clear();

		String[] newScopeIds = new String[0];
  		
		if (activeService != null)
			newScopeIds = activeService.getScopeIds();

		if (force || Util.compare(oldScopeIds, newScopeIds) != 0) {
			Manager manager = Manager.getInstance();
			Machine keyMachine = manager.getKeyMachine();

			// TODO
			if (newScopeIds == null || newScopeIds.length == 0)
				newScopeIds = new String[] { "org.eclipse.ui.globalScope" }; //$NON-NLS-1$
	    	
			try {
				keyMachine.setScopes(newScopeIds);
			} catch (IllegalArgumentException eIllegalArgument) {
				System.err.println(eIllegalArgument);
			}
	    			    	   	
			MenuManager menuManager = window.getMenuManager();
			menuManager.update(IAction.TEXT);
		}
	}

	public void updateAccelerators() {
		Manager manager = Manager.getInstance();
		Machine keyMachine = manager.getKeyMachine();      		
		Sequence mode = keyMachine.getMode();
		List strokes = mode.getStrokes();
		int size = strokes.size();		
		Map sequenceMapForMode = keyMachine.getSequenceMapForMode();
		SortedSet strokeSetForMode = new TreeSet();
		Iterator iterator = sequenceMapForMode.keySet().iterator();

		while (iterator.hasNext()) {
			Sequence sequence = (Sequence) iterator.next();
			
			if (sequence.isChildOf(mode, false))
				strokeSetForMode.add(sequence.getStrokes().get(size));	
		}

	   	iterator = strokeSetForMode.iterator();
	   	int[] accelerators = new int[strokeSetForMode.size()];
		int i = 0;
			   	
	   	while (iterator.hasNext()) {
	   		Stroke stroke = (Stroke) iterator.next();
	   		accelerators[i++] = stroke.getValue();	   		
	   	}

		if (acceleratorMenu == null || acceleratorMenu.isDisposed()) {		
			Menu parent = window.getShell().getMenuBar();
			
			if (parent == null || parent.getItemCount() < 1)
				return;
			
			MenuItem parentItem = parent.getItem(parent.getItemCount() - 1);
			parent = parentItem.getMenu();
			acceleratorMenu = new AcceleratorMenu(parent);
		}
		
		if (acceleratorMenu == null)
			return;
		
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

		if (mode.getStrokes().size() == 0)
			acceleratorMenu.removeVerifyListener(verifyListener);
		else
			acceleratorMenu.addVerifyListener(verifyListener);
	}
}
