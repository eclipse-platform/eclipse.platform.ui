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

package org.eclipse.ui.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.internal.console.ConsoleMessages;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.internal.console.FollowHyperlinkAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A page for a text console.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.1
 */
public class TextConsolePage implements IPageBookViewPage, IPropertyChangeListener, IAdaptable {
    private IPageSite fSite;
    private TextConsole fConsole;
    private IConsoleView fConsoleView;
    private TextConsoleViewer fViewer;
    private Menu fMenu;
    protected Map fGlobalActions = new HashMap();
    protected ArrayList fSelectionActions = new ArrayList();
    protected ClearOutputAction fClearOutputAction;
    
    
    
	// text selection listener, used to update selection dependant actions on selection changes
	private ISelectionChangedListener selectionChangedListener =  new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}
	};
    
	// updates the find replace action if the document length is > 0
	private ITextListener textListener = new ITextListener() {
	    public void textChanged(TextEvent event) {
			IUpdate findReplace = (IUpdate)fGlobalActions.get(ActionFactory.FIND.getId());
			if (findReplace != null) {
				findReplace.update();
			}
		}
	};
	
	public TextConsolePage(TextConsole console, IConsoleView view) {
	    fConsole = console;
	    fConsoleView = view;
	}
	
	protected TextConsoleViewer createViewer(Composite parent, TextConsole console) {
	    return new TextConsoleViewer(parent, console);
	}
    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPageBookViewPage#getSite()
     */
    public IPageSite getSite() {
        return fSite;
    }
    	
    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
     */
    public void init(IPageSite pageSite) throws PartInitException {
        fSite = pageSite;
    }

    protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction((String)iterator.next());		
		}
	}

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        fViewer = createViewer(parent, fConsole);
		fViewer.setConsoleWidth(fConsole.getConsoleWidth());
		fViewer.setTabWidth(fConsole.getTabWidth());
		fConsole.addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		
		MenuManager manager= new MenuManager("#TextConsole", "#TextConsole");  //$NON-NLS-1$//$NON-NLS-2$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		fMenu = manager.createContextMenu(getControl());
		getControl().setMenu(fMenu);
		
		createActions();
		configureToolBar(getSite().getActionBars().getToolBarManager());
		
		getSite().registerContextMenu(ConsolePlugin.getUniqueIdentifier() + ".TextConsole", manager, fViewer); //$NON-NLS-1$
		getSite().setSelectionProvider(fViewer);
		
		fViewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
		fViewer.addTextListener(textListener);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose() {
        fConsole.removePropertyChangeListener(this);
        JFaceResources.getFontRegistry().removeListener(this);
        
        if (fMenu != null && !fMenu.isDisposed()) {
            fMenu.dispose();
        }
        fClearOutputAction = null;
        fSelectionActions.clear();
        fGlobalActions.clear();
        
        fViewer.getSelectionProvider().removeSelectionChangedListener(selectionChangedListener);
        fViewer.removeTextListener(textListener);
    }


    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#getControl()
     */
    public Control getControl() {
        return fViewer != null ? fViewer.getControl() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
     */
    public void setActionBars(IActionBars actionBars) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#setFocus()
     */
    public void setFocus() {
        fViewer.getTextWidget().setFocus();
    }

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
    public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		String property = event.getProperty();
		
		if (source.equals(fConsole) && IConsoleConstants.P_FONT.equals(property)) {
			fViewer.setFont(fConsole.getFont());	
		} else if (IConsoleConstants.P_FONT_STYLE.equals(property)) {
		    fViewer.getTextWidget().redraw();
		} else if (property.equals(IConsoleConstants.P_STREAM_COLOR)) {
		    fViewer.getTextWidget().redraw();
		} else if (source.equals(fConsole) && property.equals(IConsoleConstants.P_TAB_SIZE)) {
		    Integer tabSize = (Integer)event.getNewValue();
		    fViewer.setTabWidth(tabSize.intValue());
		} else if (source.equals(fConsole) && property.equals(IConsoleConstants.P_CONSOLE_WIDTH)) {
		    fViewer.setConsoleWidth(fConsole.getConsoleWidth()); 
		}
	}

    protected void createActions() {
        IActionBars actionBars= getSite().getActionBars();
        TextViewerAction action= new TextViewerAction(fViewer, ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.0"), ConsoleMessages.getString("IOConsolePage.1"), ConsoleMessages.getString("IOConsolePage.2"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		
		action= new TextViewerAction(fViewer, ITextOperationTarget.CUT);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.3"), ConsoleMessages.getString("IOConsolePage.4"), ConsoleMessages.getString("IOConsolePage.5"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setGlobalAction(actionBars, ActionFactory.CUT.getId(), action);
		
		action= new TextViewerAction(fViewer, ITextOperationTarget.COPY);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.6"), ConsoleMessages.getString("IOConsolePage.7"), ConsoleMessages.getString("IOConsolePage.8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		
		action= new TextViewerAction(fViewer, ITextOperationTarget.PASTE);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.9"), ConsoleMessages.getString("IOConsolePage.10"), ConsoleMessages.getString("IOConsolePage.11")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setGlobalAction(actionBars, ActionFactory.PASTE.getId(), action);
		
		fClearOutputAction = new ClearOutputAction(fConsole);
		
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.ui.internal.console.ConsoleMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.FIND.getId(), new FindReplaceAction(bundle, "find_replace_action.", fConsoleView)); //$NON-NLS-1$

		fSelectionActions.add(ActionFactory.CUT.getId());
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.PASTE.getId());
		fSelectionActions.add(ActionFactory.FIND.getId());
		
		actionBars.updateActionBars();
    }
    
    protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
        fGlobalActions.put(actionID, action);  
        actionBars.setGlobalActionHandler(actionID, action);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return fViewer.getFindReplaceTarget();
		}
		if (Widget.class.equals(required)) {
			return fViewer.getTextWidget();
		}
		return null;
    }
    
    /**
	 * Returns the view this page is contained in
	 * 
	 * @return the view this page is contained in
	 */
	protected IConsoleView getConsoleView() {
		return fConsoleView;
	}
	
	/**
	 * Returns the console this page is displaying
	 * 
	 * @return the console this page is displaying
	 */
	protected IConsole getConsole() {
		return fConsole;
	}
	
	protected void updateAction(String actionId) {
		IAction action= (IAction)fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}	

    
	/**
	 * Fill the context menu
	 * 
	 * @param menuManager menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menuManager) {
		IDocument doc= fViewer.getDocument();
		if (doc == null) {
			return;
		}
	 

		menuManager.add((IAction)fGlobalActions.get(ActionFactory.CUT.getId()));
		menuManager.add((IAction)fGlobalActions.get(ActionFactory.COPY.getId()));
		menuManager.add((IAction)fGlobalActions.get(ActionFactory.PASTE.getId()));
		menuManager.add((IAction)fGlobalActions.get(ActionFactory.SELECT_ALL.getId()));
		
		menuManager.add(new Separator("FIND")); //$NON-NLS-1$
		menuManager.add((IAction)fGlobalActions.get(ActionFactory.FIND.getId()));
		menuManager.add(new FollowHyperlinkAction(fViewer));
		menuManager.add(fClearOutputAction);
		
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		// TODO: hack to let participants contribute to context menu
		((ConsoleView)getConsoleView()).menuAboutToShow(menuManager);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fClearOutputAction);
	}


    /**
     * @return Returns the viewer.
     */
    public TextConsoleViewer getViewer() {
        return fViewer;
    }
    /**
     * @param viewer The viewer to set.
     */
    public void setViewer(TextConsoleViewer viewer) {
        this.fViewer = viewer;
    }
}
