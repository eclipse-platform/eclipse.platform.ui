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
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A page for an IOConsole 
 * 
 * @since 3.1
 *
 */
public class TextConsolePage implements IPageBookViewPage, IPropertyChangeListener, IAdaptable {

    private TextConsoleViewer viewer;
    private TextConsole console;
    private IPageSite site;
    private IConsoleView consoleView;
    private Map globalActions = new HashMap();
    private ArrayList selectionActions = new ArrayList();
    private ClearOutputAction clearOutputAction;
    private ScrollLockAction scrollLockAction;
    private Menu menu;
    private boolean readOnly;
    
	// text selection listener, used to update selection dependant actions on selection changes
	private ISelectionChangedListener selectionChangedListener =  new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}
	};

	// updates the find replace action if the document length is > 0
	private ITextListener textListener = new ITextListener() {
	    public void textChanged(TextEvent event) {
			IUpdate findReplace = (IUpdate)globalActions.get(ActionFactory.FIND.getId());
			if (findReplace != null) {
				findReplace.update();
			}
		}
	};
    
    
    public TextConsolePage(TextConsole console, IConsoleView view) {
        this.console = console;
        this.consoleView = view;
    } 

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPageBookViewPage#getSite()
     */
    public IPageSite getSite() {
        return site;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
     */
    public void init(IPageSite pageSite) throws PartInitException {
        site = pageSite;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
		viewer = new TextConsoleViewer(parent, console);
		viewer.setConsoleWidth(console.getConsoleWidth());
		viewer.setTabWidth(console.getTabWidth());
		console.addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		
		MenuManager manager= new MenuManager("#TextConsole", "#TextConsole");  //$NON-NLS-1$//$NON-NLS-2$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		menu = manager.createContextMenu(getControl());
		getControl().setMenu(menu);
		
		createActions();
		configureToolBar(getSite().getActionBars().getToolBarManager());
		
		getSite().registerContextMenu(ConsolePlugin.getUniqueIdentifier() + ".TextConsole", manager, viewer); //$NON-NLS-1$
		getSite().setSelectionProvider(viewer);
		
		viewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
		viewer.addTextListener(textListener);
		if (readOnly) {
		    viewer.setReadOnly();
		}
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose() {
        console.removePropertyChangeListener(this);
        JFaceResources.getFontRegistry().removeListener(this);
        
        if (menu != null && !menu.isDisposed()) {
            menu.dispose();
        }
        clearOutputAction = null;
        if (scrollLockAction != null) {
            scrollLockAction.dispose();
        }
        selectionActions.clear();
        globalActions.clear();
        
        viewer.getSelectionProvider().removeSelectionChangedListener(selectionChangedListener);
        viewer.removeTextListener(textListener);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#getControl()
     */
    public Control getControl() {
        return viewer != null ? viewer.getControl() : null;
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
        viewer.getTextWidget().setFocus();
    }

	protected void setFont(Font font) {
		viewer.getTextWidget().setFont(font);
	}
	
	public void setAutoScroll(boolean scroll) {
	    if (viewer != null) {
	        viewer.setAutoScroll(scroll);
	    }
	    if (scrollLockAction != null) {
	        scrollLockAction.setChecked(!scroll);
	    }
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
    public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		String property = event.getProperty();
		
		if (source.equals(console) && IConsoleConstants.P_FONT.equals(property)) {
			setFont(console.getFont());	
		} else if (IConsoleConstants.P_FONT_STYLE.equals(property)) {
		    viewer.getTextWidget().redraw();
		} else if (property.equals(IConsoleConstants.P_STREAM_COLOR)) {
		    viewer.getTextWidget().redraw();
		} else if (source.equals(console) && property.equals(IConsoleConstants.P_TAB_SIZE)) {
		    Integer tabSize = (Integer)event.getNewValue();
		    viewer.setTabWidth(tabSize.intValue());
		} else if (source.equals(console) && property.equals(IConsoleConstants.P_CONSOLE_WIDTH)) {
		    viewer.setConsoleWidth(console.getConsoleWidth()); 
		} else if (property.equals(IConsoleConstants.CONSOLE_FONT)) {
		    viewer.setFont(JFaceResources.getFont(IConsoleConstants.CONSOLE_FONT));
		} else if (property.equals(IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE)) {
		    viewer.setReadOnly();
		} else if (property.equals(IConsoleConstants.P_AUTO_SCROLL)) {
		    setAutoScroll(console.getAutoScroll());
		}
	}

    protected void createActions() {
        IActionBars actionBars= getSite().getActionBars();
        TextViewerAction action= new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.0"), ConsoleMessages.getString("IOConsolePage.1"), ConsoleMessages.getString("IOConsolePage.2"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		
		action= new TextViewerAction(viewer, ITextOperationTarget.CUT);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.3"), ConsoleMessages.getString("IOConsolePage.4"), ConsoleMessages.getString("IOConsolePage.5"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setGlobalAction(actionBars, ActionFactory.CUT.getId(), action);
		
		action= new TextViewerAction(viewer, ITextOperationTarget.COPY);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.6"), ConsoleMessages.getString("IOConsolePage.7"), ConsoleMessages.getString("IOConsolePage.8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		
		action= new TextViewerAction(viewer, ITextOperationTarget.PASTE);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.9"), ConsoleMessages.getString("IOConsolePage.10"), ConsoleMessages.getString("IOConsolePage.11")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setGlobalAction(actionBars, ActionFactory.PASTE.getId(), action);
		
		clearOutputAction = new ClearOutputAction(console);
		
		scrollLockAction = new ScrollLockAction(console);
		setAutoScroll(!scrollLockAction.isChecked());
		
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.ui.internal.console.ConsoleMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.FIND.getId(), new FindReplaceAction(bundle, "find_replace_action.", consoleView)); //$NON-NLS-1$

		selectionActions.add(ActionFactory.CUT.getId());
		selectionActions.add(ActionFactory.COPY.getId());
		selectionActions.add(ActionFactory.PASTE.getId());
		selectionActions.add(ActionFactory.FIND.getId());
		
		actionBars.updateActionBars();
    }
    
    protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
        globalActions.put(actionID, action);  
        actionBars.setGlobalActionHandler(actionID, action);
    }

    protected void updateSelectionDependentActions() {
		Iterator iterator= selectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction((String)iterator.next());		
		}
	}	
	
	protected void updateAction(String actionId) {
		IAction action= (IAction)globalActions.get(actionId);
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
		IDocument doc= viewer.getDocument();
		if (doc == null) {
			return;
		}
	 
		if (!viewer.isReadOnly()) {
		    menuManager.add((IAction)globalActions.get(ActionFactory.CUT.getId()));
		}
		menuManager.add((IAction)globalActions.get(ActionFactory.COPY.getId()));
		if (!viewer.isReadOnly()) {
		    menuManager.add((IAction)globalActions.get(ActionFactory.PASTE.getId()));
		}
		menuManager.add((IAction)globalActions.get(ActionFactory.SELECT_ALL.getId()));

		menuManager.add(new Separator("FIND")); //$NON-NLS-1$
		menuManager.add((IAction)globalActions.get(ActionFactory.FIND.getId()));
		menuManager.add(new FollowHyperlinkAction(viewer));
		menuManager.add(clearOutputAction);
		menuManager.add(scrollLockAction);
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		// TODO: hack to let participants contribute to context menu
		((ConsoleView)getConsoleView()).menuAboutToShow(menuManager);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, clearOutputAction);
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, scrollLockAction);
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return viewer.getFindReplaceTarget();
		}
		if (Widget.class.equals(required)) {
			return viewer.getTextWidget();
		}
		return null;
    }

    /**
     * inform the viewer that it's text widget should not be editable.
     */
    public void setReadOnly() {
        readOnly = true;
        if (viewer != null) {
            viewer.setReadOnly();
        }
    }
    
    /**
	 * Returns the view this page is contained in
	 * 
	 * @return the view this page is contained in
	 */
	protected IConsoleView getConsoleView() {
		return consoleView;
	}
	
	/**
	 * Returns the console this page is displaying
	 * 
	 * @return the console this page is displaying
	 */
	protected IConsole getConsole() {
		return console;
	}

}
