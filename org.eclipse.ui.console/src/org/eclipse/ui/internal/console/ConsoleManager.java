/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
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
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

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
    
    private List fConsoleViews = new ArrayList();
    
    private boolean fWarnQueued = false;
    
    private RepaintJob fRepaintJob = new RepaintJob();
    
    private class RepaintJob extends WorkbenchJob {
        private Set list = new HashSet();

        public RepaintJob() {
            super("schedule redraw() of viewers"); //$NON-NLS-1$
            setSystem(true);
        }
        
        void addConsole(IConsole console) {
        	synchronized (list) {
        		list.add(console);
			}
        }
        
        public IStatus runInUIThread(IProgressMonitor monitor) {
            synchronized (list) {
                if (list.isEmpty()) {
                    return Status.OK_STATUS;
                }
                
                IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
                for (int i = 0; i < workbenchWindows.length; i++) {
                    IWorkbenchWindow window = workbenchWindows[i];
                    if (window != null) {
                        IWorkbenchPage page = window.getActivePage();
                        if (page != null) {
                            IViewPart part = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
                            if (part != null && part instanceof IConsoleView) {
                                ConsoleView view = (ConsoleView) part;
                                if (list.contains(view.getConsole())) {
                                    Control control = view.getCurrentPage().getControl();
                                    if (!control.isDisposed()) {
                                        control.redraw();
                                    }
                                }
                            }

                        }
                    }
                }
                list.clear();
            }
            return Status.OK_STATUS;
        }
    }
    
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
			IStatus status = new Status(IStatus.ERROR, ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.INTERNAL_ERROR, ConsoleMessages.ConsoleManager_0, exception); 
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
                SafeRunner.run(this);
			}	
			fChanged = null;
			fListener = null;			
		}
	}	
		
	public void registerConsoleView(ConsoleView view) {
	    synchronized (fConsoleViews) {
	        fConsoleViews.add(view);
	    }
	}
    public void unregisterConsoleView(ConsoleView view) {
        synchronized (fConsoleViews) {
            fConsoleViews.remove(view);
        }
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#addConsoleListener(org.eclipse.ui.console.IConsoleListener)
	 */
	public void addConsoleListener(IConsoleListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList();
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
	public void addConsoles(IConsole[] consoles) {
		List added = new ArrayList(consoles.length);
		synchronized (fConsoles) {
			for (int i = 0; i < consoles.length; i++) {
			    IConsole console = consoles[i];
			    if(console instanceof TextConsole) {
			        TextConsole ioconsole = (TextConsole)console;
			        createPatternMatchListeners(ioconsole);
			    }
				if (!fConsoles.contains(console)) {
					fConsoles.add(console);
					added.add(console);
				}
			}			
		}
		if (!added.isEmpty()) {
			fireUpdate((IConsole[])added.toArray(new IConsole[added.size()]), ADDED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#removeConsoles(org.eclipse.ui.console.IConsole[])
	 */
	public void removeConsoles(IConsole[] consoles) {
		List removed = new ArrayList(consoles.length);
		synchronized (fConsoles) {
			for (int i = 0; i < consoles.length; i++) {
				IConsole console = consoles[i];
				if (fConsoles.remove(console)) {
					removed.add(console);
				}
			}
		}
		if (!removed.isEmpty()) {
			fireUpdate((IConsole[])removed.toArray(new IConsole[removed.size()]), REMOVED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleManager#getConsoles()
	 */
	public IConsole[] getConsoles() {
		synchronized (fConsoles) {
			return (IConsole[])fConsoles.toArray(new IConsole[fConsoles.size()]);	
		}
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
	
	
	private class ShowConsoleViewJob extends WorkbenchJob {
		private IConsole console; 
		
		ShowConsoleViewJob() {
			super("Show Console View"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.SHORT);
		}
		
		void setConsole(IConsole console) {
			this.console = console;
		}
		
		public IStatus runInUIThread(IProgressMonitor monitor) {
			boolean consoleFound = false;
            IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null && console != null) {
                IWorkbenchPage page= window.getActivePage();
                if (page != null) {
                    synchronized (fConsoleViews) {
                        for (Iterator iter = fConsoleViews.iterator(); iter.hasNext();) {
                            ConsoleView consoleView = (ConsoleView) iter.next();
                            if (consoleView.getSite().getPage().equals(page)) {
	                            boolean consoleVisible = page.isPartVisible(consoleView);
	                            if (consoleVisible) {
	                                consoleFound = true;
	                                boolean bringToTop = shouldBringToTop(console, consoleView);
	                                if (bringToTop) {
	                                    page.bringToTop(consoleView);
	                                }
	                                consoleView.display(console);
	                            }
                            }
                        }
                    }
                    
                    if (!consoleFound) {
                        try {
                            IConsoleView consoleView = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_CREATE);
                            boolean bringToTop = shouldBringToTop(console, consoleView);
                            if (bringToTop) {
                                page.bringToTop(consoleView);
                            }
                            consoleView.display(console);        
                        } catch (PartInitException pie) {
                            ConsolePlugin.log(pie);
                        }
                    }
                }
            }
            console = null;
			return Status.OK_STATUS;
		}		
	}
	
	private ShowConsoleViewJob showJob = new ShowConsoleViewJob();
	/**
	 * @see IConsoleManager#showConsoleView(IConsole)
	 */
	public void showConsoleView(final IConsole console) {
		showJob.setConsole(console);
		showJob.schedule(100);
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
			Job job = new UIJob(ConsolePlugin.getStandardDisplay(), ConsoleMessages.ConsoleManager_consoleContentChangeJob) {
				public IStatus runInUIThread(IProgressMonitor monitor) {
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
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleManager#getPatternMatchListenerDelegates(org.eclipse.ui.console.IConsole)
     */
    public IPatternMatchListener[] createPatternMatchListeners(IConsole console) {
    		if (fPatternMatchListeners == null) {
    		    fPatternMatchListeners = new ArrayList();
    			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.EXTENSION_POINT_CONSOLE_PATTERN_MATCH_LISTENERS);
    			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
    			for (int i = 0; i < elements.length; i++) {
    				IConfigurationElement config = elements[i];
    				PatternMatchListenerExtension extension = new PatternMatchListenerExtension(config);
    				fPatternMatchListeners.add(extension); 
    			}
    		}
    		ArrayList list = new ArrayList();
    		for(Iterator i = fPatternMatchListeners.iterator(); i.hasNext(); ) {
    		    PatternMatchListenerExtension extension = (PatternMatchListenerExtension) i.next();
                try {
                    if (extension.getEnablementExpression() == null) {
                        i.remove();
                        continue;
                    }
    		    
    		        if (console instanceof TextConsole && extension.isEnabledFor(console)) {
                        TextConsole textConsole = (TextConsole) console;
    		            PatternMatchListener patternMatchListener = new PatternMatchListener(extension);
                        try {
                            textConsole.addPatternMatchListener(patternMatchListener);
                            list.add(patternMatchListener);
                        } catch (PatternSyntaxException e) {
                            ConsolePlugin.log(e);
                            i.remove();
                        }
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
    
    
    public void refresh(final IConsole console) {
        fRepaintJob.addConsole(console);
        fRepaintJob.schedule(50); 
    }

}
