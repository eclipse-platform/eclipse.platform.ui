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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.TextConsole;

/**
 * The singleton console manager.
 * 
 * @since 3.0
 */
public class ConsoleManager implements IConsoleManager {
	
	/**
	 * Console listeners
	 */
	private ListenerList fListeners = null;
	
	/**
	 * List of registered consoles
	 */
	private List fConsoles = new ArrayList(10); 

	
	// change notification constants
	private final static int ADDED = 1;
	private final static int REMOVED = 2;

    private List fPatternMatchListeners;

    private List fPageParticipants;

    private List fConsoleFactoryExtensions;
	
    private IWindowListener fWindowListener;
    
    private boolean fWarnQueued = false;
    
	/**
	 * Notifies a console listener of additions or removals
	 */
	class ConsoleNotifier implements ISafeRunnable {
		
		private IConsoleListener fListener;
		private int fType;
		private IConsole[] fChanged;
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.INTERNAL_ERROR, ConsoleMessages.getString("ConsoleManager.0"), exception); //$NON-NLS-1$
			ConsolePlugin.log(status);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.consolesAdded(fChanged);
					break;
				case REMOVED:
					fListener.consolesRemoved(fChanged);
					break;
			}
		}

		/**
		 * Notifies the given listener of the adds/removes
		 * 
		 * @param consoles the consoles that changed
		 * @param update the type of change
		 */
		public void notify(IConsole[] consoles, int update) {
			if (fListeners == null) {
				return;
			}
			fChanged = consoles;
			fType = update;
			Object[] copiedListeners= fListeners.getListeners();
			for (int i= 0; i < copiedListeners.length; i++) {
				fListener = (IConsoleListener)copiedListeners[i];
				Platform.run(this);
			}	
			fChanged = null;
			fListener = null;			
		}
	}	
	
	
	public ConsoleManager() {
	    fWindowListener = new IWindowListener() {
            public void windowActivated(IWorkbenchWindow window) {
            }
            public void windowDeactivated(IWorkbenchWindow window) {   
            }
            public void windowOpened(IWorkbenchWindow window) {   
            }
            public void windowClosed(IWorkbenchWindow window) {
                removeConsoles((IConsole[]) fConsoles.toArray(new IConsole[0]));
            }
	    };
	    
	    ConsolePlugin.getDefault().getWorkbench().addWindowListener(fWindowListener);
	}
	
    /* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#addConsoleListener(org.eclipse.ui.console.IConsoleListener)
	 */
	public void addConsoleListener(IConsoleListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList(5);
		}
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#removeConsoleListener(org.eclipse.ui.console.IConsoleListener)
	 */
	public void removeConsoleListener(IConsoleListener listener) {
		if (fListeners != null) {
			fListeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#addConsoles(org.eclipse.ui.console.IConsole[])
	 */
	public synchronized void addConsoles(IConsole[] consoles) {
		List added = new ArrayList(consoles.length);
		for (int i = 0; i < consoles.length; i++) {
		    IConsole console = consoles[i];
		    if(console instanceof TextConsole) {
		        TextConsole ioconsole = (TextConsole)console;
		        IPatternMatchListener[] matchListeners = getPatternMatchListeners(ioconsole);
		        for (int j = 0; j < matchListeners.length; j++) {
		            ioconsole.addPatternMatchListener(matchListeners[j]);
		        }
		    }
			if (!fConsoles.contains(console)) {
				fConsoles.add(console);
				added.add(console);
			}
		}
		if (!added.isEmpty()) {
			fireUpdate((IConsole[])added.toArray(new IConsole[added.size()]), ADDED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#removeConsoles(org.eclipse.ui.console.IConsole[])
	 */
	public synchronized void removeConsoles(IConsole[] consoles) {
		List removed = new ArrayList(consoles.length);
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			if (fConsoles.remove(console)) {
				removed.add(console);
			}
		}
		if (!removed.isEmpty()) {
			fireUpdate((IConsole[])removed.toArray(new IConsole[removed.size()]), REMOVED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#getConsoles()
	 */
	public synchronized IConsole[] getConsoles() {
		return (IConsole[])fConsoles.toArray(new IConsole[fConsoles.size()]);
	}

	/**
	 * Fires notification.
	 * 
	 * @param consoles consoles added/removed
	 * @param type ADD or REMOVE
	 */
	private void fireUpdate(IConsole[] consoles, int type) {
		new ConsoleNotifier().notify(consoles, type);
	}
	
	/**
	 * @see IConsoleManager#showConsoleView(IConsole)
	 */
	public void showConsoleView(final IConsole console) {
	    ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
	        public void run() {
	            IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	            if (window != null) {
	                IWorkbenchPage page= window.getActivePage();
	                if (page != null) {
                        IViewReference[] viewReferences = page.getViewReferences();
                        for (int i = 0; i < viewReferences.length; i++) {
                            IViewReference viewRef = viewReferences[i];
                            if (viewRef == null) {
                                continue;
                            }
                            if(IConsoleConstants.ID_CONSOLE_VIEW.equals(viewRef.getId())) {
                                IWorkbenchPart part = viewRef.getPart(false);
                                IConsoleView consoleView = null;
                                if (part == null) {
                                    try {
                                        consoleView = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_CREATE);
                                    } catch (PartInitException pie) {
                                        ConsolePlugin.log(pie);
                                    }
                                } else if (!(part instanceof IConsoleView)) {
                                    continue;
                                } else {
                                    consoleView = (IConsoleView) part;
                                }
                                boolean bringToTop = shouldBringToTop(console, consoleView);
                                if (bringToTop) {
                                    page.bringToTop(consoleView);
                                }
                                consoleView.display(console);        
                            }
                        }
	                }
                }
            }
	    });
	}	
	
	/**
	 * Returns whether the given console view should be brought to the top.
	 * The view should not be brought to the top if the view is pinned on
	 * a console other than the given console.
	 */
	private boolean shouldBringToTop(IConsole console, IViewPart consoleView) {
		boolean bringToTop= true;
		if (consoleView instanceof IConsoleView) {
			IConsoleView cView= (IConsoleView)consoleView;
			if (cView.isPinned()) {
				IConsole pinnedConsole= cView.getConsole();
				bringToTop = console.equals(pinnedConsole);
			}
		}
		return bringToTop;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#warnOfContentChange(org.eclipse.ui.console.IConsole)
	 */
	public void warnOfContentChange(final IConsole console) {
		if (!fWarnQueued) {
			fWarnQueued = true;
			ConsolePlugin.getStandardDisplay().asyncExec(new Runnable(){
				public void run() {
					IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page= window.getActivePage();
						if (page != null) {
							IConsoleView consoleView= (IConsoleView)page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
							if (consoleView != null) {
								consoleView.warnOfContentChange(console);
							}
						} 
					}	
					fWarnQueued = false;
				}			
			});
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleManager#getPatternMatchListenerDelegates(org.eclipse.ui.console.IConsole)
     */
    public IPatternMatchListener[] getPatternMatchListeners(IConsole console) {
    		if (fPatternMatchListeners == null) {
    		    fPatternMatchListeners = new ArrayList();
    			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.EXTENSION_POINT_CONSOLE_PATTERN_MATCH_LISTENERS);
    			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
    			for (int i = 0; i < elements.length; i++) {
    				IConfigurationElement config = elements[i];
    				PatternMatchListenerExtension extension = new PatternMatchListenerExtension(config);
    				fPatternMatchListeners.add(extension); //$NON-NLS-1$
    			}
    		}
    		ArrayList list = new ArrayList();
    		for(Iterator i = fPatternMatchListeners.iterator(); i.hasNext(); ) {
    		    PatternMatchListenerExtension extension = (PatternMatchListenerExtension) i.next();
    		    try {
    		        if (extension.isEnabledFor(console)) {
    		            list.add(new PatternMatchListener(extension));
    		        }
    		    } catch (CoreException e) {
    		        ConsolePlugin.log(e);
    		    }
    		}
        return (PatternMatchListener[])list.toArray(new PatternMatchListener[0]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleManager#getPageParticipants(org.eclipse.ui.console.IConsole)
     */
    public IConsolePageParticipant[] getPageParticipants(IConsole console) {
        if(fPageParticipants == null) {
            fPageParticipants = new ArrayList();
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.EXTENSION_POINT_CONSOLE_PAGE_PARTICIPANTS);
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for(int i = 0; i < elements.length; i++) {
                IConfigurationElement config = elements[i];
                ConsolePageParticipantExtension extension = new ConsolePageParticipantExtension(config);
                fPageParticipants.add(extension);
            }
        }
        ArrayList list = new ArrayList();
        for(Iterator i = fPageParticipants.iterator(); i.hasNext(); ) {
            ConsolePageParticipantExtension extension = (ConsolePageParticipantExtension) i.next();
            try {
                if (extension.isEnabledFor(console)) {
                    list.add(extension.createDelegate());
                }
            } catch (CoreException e) {
                ConsolePlugin.log(e);
            }
        }
        return (IConsolePageParticipant[]) list.toArray(new IConsolePageParticipant[0]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleManager#getConsoleFactories()
     */
    public ConsoleFactoryExtension[] getConsoleFactoryExtensions() {
        if (fConsoleFactoryExtensions == null) {
            fConsoleFactoryExtensions = new ArrayList();
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.EXTENSION_POINT_CONSOLE_FACTORIES);
            IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < configurationElements.length; i++) {
                fConsoleFactoryExtensions.add(new ConsoleFactoryExtension(configurationElements[i]));
            }
        }
        return (ConsoleFactoryExtension[]) fConsoleFactoryExtensions.toArray(new ConsoleFactoryExtension[0]);
    }

}
