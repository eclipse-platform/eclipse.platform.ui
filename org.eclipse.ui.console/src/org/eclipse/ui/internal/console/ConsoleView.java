/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Page book console view.
 * 
 * @since 3.0
 */
public class ConsoleView extends PageBookView implements IConsoleView, IConsoleListener, IPropertyChangeListener, IMenuListener {
	
	/**
	 * Whether this console is pinned.
	 */
	private boolean fPinned = false;
	
	/**
	 * Stack of consoles in MRU order
	 */
	private List fStack = new ArrayList();
	
	/**
	 * The console being displayed, or <code>null</code> if none
	 */
	private IConsole fActiveConsole = null;
	
	/**
	 * Map of consoles to dummy console parts (used to close pages)
	 */
	private Map fConsoleToPart;
	
	/**
	 * Map of consoles to array of page participants
	 */
	private Map fConsoleToPageParticipants;
	
	/**
	 * Map of parts to consoles
	 */
	private Map fPartToConsole;
	
	// actions
	private PinConsoleAction fPinAction = null; 
	private ConsoleDropDownAction fDisplayConsoleAction = null;
	
	private OpenConsoleAction fOpenConsoleAction = null;

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
			fStack.remove(fActiveConsole);
			fStack.add(0,fActiveConsole);
			updateTitle();		
			// update console actions
			if (fPinAction != null) {
				fPinAction.update();
			}
		}
	}
	
	/**
	 * Returns a stack of consoles in the view in MRU order.
	 * 
	 * @return a stack of consoles in the view in MRU order
	 */
	protected List getConsoleStack() {
		return fStack;
	}

	/**
	 * Updates the view title based on the active console
	 */
	protected void updateTitle() {
		IConsole console = getConsole();
		if (console == null) {
			setContentDescription(ConsoleMessages.getString("ConsoleView.0")); //$NON-NLS-1$
		} else {
			setContentDescription(console.getName()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
	    IConsole console = (IConsole)fPartToConsole.get(part);
	    
		// dispose page participants
		IConsolePageParticipant[] participants = (IConsolePageParticipant[]) fConsoleToPageParticipants.remove(console);
		for (int i = 0; i < participants.length; i++) {
            IConsolePageParticipant participant = participants[i];
            // TODO: this should be done in a safe runnable
            participant.dispose();
        }

		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();
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
		
		// initialize page participants
		IConsolePageParticipant[] participants = ((ConsoleManager)getConsoleManager()).getPageParticipants(console);
		fConsoleToPageParticipants.put(console, participants);
		for (int i = 0; i < participants.length; i++) {
            IConsolePageParticipant participant = participants[i];
            // TODO: this should be done in a safe runnable
            participant.init(page, console);
        }
		
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
		getConsoleManager().removeConsoleListener(this);
	}

	/**
	 * Returns the console manager.
	 * 
     * @return the console manager
     */
    private IConsoleManager getConsoleManager() {
        return ConsolePlugin.getDefault().getConsoleManager();
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
							// ensure it's still registered since this is done asynchronously
							IConsole[] allConsoles = getConsoleManager().getConsoles();
							for (int j = 0; j < allConsoles.length; j++) {
                                IConsole registered = allConsoles[j];
                                if (registered.equals(console)) {
        							ConsoleWorkbenchPart part = new ConsoleWorkbenchPart(console, getSite());
        							fConsoleToPart.put(console, part);
        							fPartToConsole.put(part, console);
        							partActivated(part);
        							break;
                                }
                            }

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
							fStack.remove(console);
							ConsoleWorkbenchPart part = (ConsoleWorkbenchPart)fConsoleToPart.get(console);
							if (part != null) {
								partClosed(part);
							}
							if (getConsole() == null) {
								IConsole[] available = getConsoleManager().getConsoles();
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
		fConsoleToPageParticipants = new HashMap();
	}
	
	protected void createActions() {
		fPinAction = new PinConsoleAction(this);
		fDisplayConsoleAction = new ConsoleDropDownAction(this);
		fOpenConsoleAction = new OpenConsoleAction();
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IConsoleConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IConsoleConstants.OUTPUT_GROUP));
		mgr.add(new Separator("fixedGroup")); //$NON-NLS-1$
		mgr.add(fPinAction);
		mgr.add(fDisplayConsoleAction);
		mgr.add(fOpenConsoleAction);
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
			fPinned = false; // need this off to change displayed console
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
		super.createPartControl(parent);
		createActions();
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);
		updateForExistingConsoles();
		getViewSite().getActionBars().updateActionBars();
		WorkbenchHelp.setHelp(parent, IConsoleHelpContextIds.CONSOLE_VIEW);
	}
	
	/**
	 * Initialize for existing consoles
	 */
	private void updateForExistingConsoles() {
		IConsoleManager manager = getConsoleManager();
		// create pages for consoles
		IConsole[] consoles = manager.getConsoles();
		consolesAdded(consoles);
		// add as a listener
		manager.addConsoleListener(this);		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleView#warnOfContentChange(org.eclipse.ui.console.IConsole)
	 */
	public void warnOfContentChange(IConsole console) {
		IWorkbenchPart part = (IWorkbenchPart)fConsoleToPart.get(console);
		if (part != null) {
			IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) part.getSite().getAdapter(IWorkbenchSiteProgressService.class);
			if (service != null) {
				service.warnOfContentChange();
			}
		}
	}
	
    public Object getAdapter(Class key) {
        Object adpater = super.getAdapter(key);
        if (adpater == null) {
            IConsole console = getConsole();
            if (console != null) {
                IConsolePageParticipant[] participants = (IConsolePageParticipant[]) fConsoleToPageParticipants.get(console);
                for (int i = 0; i < participants.length; i++) {
                    IConsolePageParticipant participant = participants[i];
                    adpater = participant.getAdapter(key);
                    if (adpater != null) {
                        return adpater;
                    }
                }
            }
        }
        return adpater;
    }

    /* (non-Javadoc)
     * 
     * TODO: This is an undocumented hack to allow page participants to fill a context menu.
     * Not yet API. Page participants that are IMenuListeners will be consulted to add to the
     * given context menu.
     * 
     * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void menuAboutToShow(IMenuManager manager) {
        IConsole console = getConsole();
        if (console != null) {
            IConsolePageParticipant[] participants = (IConsolePageParticipant[]) fConsoleToPageParticipants.get(console);
            for (int i = 0; i < participants.length; i++) {
                IConsolePageParticipant participant = participants[i];
                if (participant instanceof IMenuListener) {
                    // TODO: should be done in a safe runnable
                    ((IMenuListener)participant).menuAboutToShow(manager);
                }
            }
        }
        
    }
    
}