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
package org.eclipse.debug.internal.ui.console;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;

/**
 * Page book console view.
 * 
 * @since 3.0
 */
public class ConsoleView extends AbstractDebugView implements IConsoleView, IConsoleListener, ILabelProviderListener {
	
	/**
	 * Whether this console is pinned.
	 */
	private boolean fPinned = false;
	
	/**
	 * The console being displayed, or <code>null</code> if none
	 */
	private IConsole fActiveConsole = null;
	
	/**
	 * Map of consoles to dummy console parts (used to close pages)
	 */
	private Map fConsoleToPart;
	
	/**
	 * Map of consoles to labelproviders
	 */
	private Map fConsoleToLabelProvider;
	
	/**
	 * Map of parts to consoles
	 */
	private Map fPartToConsole;
	
	// actions
	private PinConsoleAction fPinAction = null; 

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
	 */
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		updateTitle();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (isPinned()) {
			// if closing the pinned console, un-pin
			IConsole console = (IConsole)fPartToConsole.get(part);
			if (console.equals(getConsole())) {
				pin(null);
			}
		}
		super.partClosed(part);
		fPinAction.update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleView#getConsole()
	 */
	public IConsole getConsole() {
		return fActiveConsole;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#showPageRec(org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void showPageRec(PageRec pageRec) {
		if (!isPinned()) {
			super.showPageRec(pageRec);
			fActiveConsole = (IConsole)fPartToConsole.get(pageRec.part);
			updateTitle();		
			// update console actions
			if (fPinAction != null) {
				fPinAction.update();
			}
		}
	}

	/**
	 * Updates the view title based on the active console
	 */
	protected void updateTitle() {
		IConsole console = getConsole();
		if (console == null) {
			setTitle(ConsoleMessages.getString("ConsoleView.0")); //$NON-NLS-1$
		} else {
			// update view title to the active console
			ILabelProvider labelProvider = (ILabelProvider)fConsoleToLabelProvider.get(console);
			String label = labelProvider.getText(console);
			setTitle(MessageFormat.format(ConsoleMessages.getString("ConsoleView.1"), new String[]{label})); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IPageBookViewPage page = (IPageBookViewPage) pageRecord.page;
		page.dispose();
		pageRecord.dispose();
		
		IConsole console = (IConsole)fPartToConsole.get(part);
		// dispose label provider
		ILabelProvider provider = (ILabelProvider)fConsoleToLabelProvider.remove(console);
		provider.removeListener(this);
		provider.dispose();
		
		// empty cross-reference cache
		fPartToConsole.remove(part);
		fConsoleToPart.remove(console);
		
		// update console actions
		fPinAction.update();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart dummyPart) {
		ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)dummyPart;
		IConsole console = part.getConsole();
		IPageBookViewPage page = console.createPage(this);
		initPage(page);
		page.createControl(getPageBook());
		PageRec rec = new PageRec(dummyPart, page);
		
		// create label provider for the console
		ILabelProvider labelProvider = console.createLabelProvider();
		fConsoleToLabelProvider.put(console, labelProvider);
		labelProvider.addListener(this);
		
		return rec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof ConsoleWorkbenchPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugUIPlugin.getDefault().getConsoleManager().removeConsoleListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		page.createControl(getPageBook());
		initPage(page);
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesAdded(org.eclipse.debug.internal.ui.console.IConsole[])
	 */
	public void consolesAdded(IConsole[] consoles) {
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			ConsoleWorkbenchPart part = new ConsoleWorkbenchPart(console, getSite());
			fConsoleToPart.put(console, part);
			fPartToConsole.put(part, console);
			partActivated(part);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesRemoved(org.eclipse.debug.internal.ui.console.IConsole[])
	 */
	public void consolesRemoved(IConsole[] consoles) {
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)fConsoleToPart.get(console);
			if (part != null) {
				partClosed(part);
			}
		}
	}

	/**
	 * Constructs a console view
	 */
	public ConsoleView() {
		super();
		fConsoleToPart = new HashMap();
		fConsoleToLabelProvider = new HashMap();
		fPartToConsole = new HashMap();
	}

	/**
	 * This method is never called for this view.
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		fPinAction = new PinConsoleAction(this);
		// TODO: drop-down action
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.CONSOLE_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IDebugUIConstants.OUTPUT_GROUP));
		mgr.add(fPinAction);
		
		// init for existing consoles
		IConsoleManager manager = DebugUIPlugin.getDefault().getConsoleManager();
		// create pages for consoles
		IConsole[] consoles = manager.getConsoles();
		consolesAdded(consoles);
		// add as a listener
		manager.addConsoleListener(this);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleView#display(org.eclipse.debug.internal.ui.console.IConsole)
	 */
	public void display(IConsole console) {
		ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)fConsoleToPart.get(console);
		if (part != null) {
			partActivated(part);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleView#pin(org.eclipse.debug.internal.ui.console.IConsole)
	 */
	public void pin(IConsole console) {
		if (console == null) {
			fPinned = false;	
		} else {
			display(console);
			fPinned = true;
		}
		if (fPinAction != null) {
			fPinAction.update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleView#isPinned()
	 */
	public boolean isPinned() {
		return fPinned;
	}

}
