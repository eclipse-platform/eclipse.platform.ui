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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
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
public class ConsoleView extends AbstractDebugView implements IConsoleView, IConsoleListener, IPropertyListener {
	
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
	 * Map of parts to consoles
	 */
	private Map fPartToConsole;
	
	// actions
	private PinConsoleAction fPinAction = null; 
	private ConsoleDropDownAction fDisplayConsoleAction = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getControl()
	 */
	protected Control getControl() {
		return getPageBook();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#isAvailable()
	 */
	public boolean isAvailable() {
		return getPageBook() != null && !getPageBook().isDisposed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (source instanceof IConsole && propId == IConsole.PROP_NAME) {
			if (source.equals(getConsole())) {
				updateTitle();
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (isPinned()) {
			// if closing the pinned console, un-pin
			IConsole console = (IConsole)fPartToConsole.get(part);
			if (console != null && console.equals(getConsole())) {
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
			setTitle(MessageFormat.format(ConsoleMessages.getString("ConsoleView.1"), new String[]{console.getName()})); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();
		
		IConsole console = (IConsole)fPartToConsole.get(part);
		console.removePropertyListener(this);
				
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
		console.addPropertyListener(this);
		PageRec rec = new PageRec(dummyPart, page);
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
	public void consolesAdded(final IConsole[] consoles) {
		if (isAvailable()) {
			Runnable r = new Runnable() {
				public void run() {
					for (int i = 0; i < consoles.length; i++) {
						if (isAvailable()) {
							IConsole console = consoles[i];
							ConsoleWorkbenchPart part = new ConsoleWorkbenchPart(console, getSite());
							fConsoleToPart.put(console, part);
							fPartToConsole.put(part, console);
							partActivated(part);
						}
					}
				}
			};
			asyncExec(r);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesRemoved(org.eclipse.debug.internal.ui.console.IConsole[])
	 */
	public void consolesRemoved(final IConsole[] consoles) {
		if (isAvailable()) {
			Runnable r = new Runnable() {
				public void run() {
					for (int i = 0; i < consoles.length; i++) {
						if (isAvailable()) {
							IConsole console = consoles[i];
							ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)fConsoleToPart.get(console);
							if (part != null) {
								partClosed(part);
							}
							if (getConsole() == null) {
								IConsole[] available = DebugUIPlugin.getDefault().getConsoleManager().getConsoles();
								if (available.length > 0) {
									display(available[available.length - 1]);
								}
							}
						}
					}
				}
			};
			asyncExec(r);
		}
	}

	/**
	 * Constructs a console view
	 */
	public ConsoleView() {
		super();
		fConsoleToPart = new HashMap();
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
		fDisplayConsoleAction = new ConsoleDropDownAction(this);
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IDebugUIConstants.OUTPUT_GROUP));
		mgr.add(fPinAction);
		mgr.add(fDisplayConsoleAction);
		
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
		if (!isPinned()) {
			ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)fConsoleToPart.get(console);
			if (part != null) {
				partActivated(part);
			}
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
