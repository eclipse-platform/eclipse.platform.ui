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
package org.eclipse.ui.internal.console;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * Page book console view.
 * 
 * @since 3.0
 */
public class ConsoleView extends PageBookView implements IConsoleView, IConsoleListener, IPropertyChangeListener {
	
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

	private boolean isAvailable() {
		return getPageBook() != null && !getPageBook().isDisposed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		if (source instanceof IConsole && event.getProperty().equals(IBasicPropertyConstants.P_TEXT)) {
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
		console.removePropertyChangeListener(this);
				
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
		console.addPropertyChangeListener(this);
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
		ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(this);
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
	 * @see org.eclipse.ui.console.IConsoleListener#consolesAdded(org.eclipse.ui.console.IConsole[])
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
	 * @see org.eclipse.ui.console.IConsoleListener#consolesRemoved(org.eclipse.ui.console.IConsole[])
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
								IConsole[] available = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
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
	 * Creates a pop-up menu on the given control. The menu
	 * is registered with this view's site, such that other
	 * plug-ins may contribute to the menu.
	 * 
	 * @param menuControl the control with which the pop-up
	 *  menu will be associated with.
	 */
	protected void createContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		// register the context menu such that other plugins may contribute to it
		if (getSite() != null) {
			getSite().registerContextMenu(menuMgr, null);
		}
	}

	protected void createActions() {
		fPinAction = new PinConsoleAction(this);
		fDisplayConsoleAction = new ConsoleDropDownAction(this);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IConsoleConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IConsoleConstants.OUTPUT_GROUP));
		mgr.add(fPinAction);
		mgr.add(fDisplayConsoleAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleView#display(org.eclipse.ui.console.IConsole)
	 */
	public void display(IConsole console) {
		if (!isPinned()) {
			ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)fConsoleToPart.get(console);
			if (part != null) {
				partActivated(part);
			}
		}
	}

	/*/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleView#pin(org.eclipse.ui.console.IConsole)
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
	 * @see org.eclipse.ui.console.IConsoleView#isPinned()
	 */
	public boolean isPinned() {
		return fPinned;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	protected IWorkbenchPart getBootstrapPart() {
		return null;
	}
	
	/**
	 * Registers the given runnable with the display
	 * associated with this view's control, if any.
	 * 
	 * @see org.eclipse.swt.widgets.Display#asyncExec(java.lang.Runnable)
	 */
	public void asyncExec(Runnable r) {
		if (isAvailable()) {
			getPageBook().getDisplay().asyncExec(r);
		}
	}
	
	/**
	 * Creates this view's underlying viewer and actions.
	 * Hooks a pop-up menu to the underlying viewer's control,
	 * as well as a key listener. When the delete key is pressed,
	 * the <code>REMOVE_ACTION</code> is invoked. Hooks help to
	 * this view. Subclasses must implement the following methods
	 * which are called in the following order when a view is
	 * created:<ul>
	 * <li><code>createViewer(Composite)</code> - the context
	 *   menu is hooked to the viewer's control.</li>
	 * <li><code>createActions()</code></li>
	 * <li><code>configureToolBar(IToolBarManager)</code></li>
	 * <li><code>getHelpContextId()</code></li>
	 * </ul>
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		//registerPartListener();
		super.createPartControl(parent);
		createActions();
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);
		updateForExistingConsoles();
		getViewSite().getActionBars().updateActionBars();
//		Viewer viewer = getViewer();
//		if (viewer != null) {
//			createContextMenu(viewer.getControl());
//		}
		WorkbenchHelp.setHelp(parent, IConsoleHelpContextIds.CONSOLE_VIEW);
//		if (viewer != null) {
//			getViewer().getControl().addKeyListener(new KeyAdapter() {
//				public void keyPressed(KeyEvent e) {
//					handleKeyPressed(e);
//				}
//			});
//			if (getViewer() instanceof StructuredViewer) {
//				((StructuredViewer)getViewer()).addDoubleClickListener(this);	
//			}
//		}
		// create the message page
		//setMessagePage(new MessagePage());
		//getMessagePage().createControl(getPageBook());
		//initPage(getMessagePage());
	
//		if (fEarlyMessage != null) { //bug 28127
//			showMessage(fEarlyMessage);
//			fEarlyMessage= null;
//		}
	}
	
	/**
	 * Initialize for existing consoles
	 */
	private void updateForExistingConsoles() {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		// create pages for consoles
		IConsole[] consoles = manager.getConsoles();
		consolesAdded(consoles);
		// add as a listener
		manager.addConsoleListener(this);		
	}	
}