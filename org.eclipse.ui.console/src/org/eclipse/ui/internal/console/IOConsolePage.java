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
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This class is new and experimental. It will likely be subject to significant change before
 * it is finalized.
 * 
 * @since 3.1
 *
 */
public class IOConsolePage implements IPageBookViewPage, IPropertyChangeListener, IAdaptable {

    private IOConsoleViewer viewer;
    private IOConsole console;
    private IPageSite site;
    private IConsoleView consoleView;
    private boolean autoScroll = true;
    private Map globalActions = new HashMap();
    private ArrayList selectionActions = new ArrayList();
    private ClearOutputAction clearOutputAction;
    private ScrollLockAction scrollLockAction;
    private Menu menu;
    
	// text selection listener
	private ISelectionChangedListener selectionChangedListener =  new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}
	};
	
	private ITextListener textListener = new ITextListener() {
	    public void textChanged(TextEvent event) {
			// update the find replace action if the document length is > 0
			IUpdate findReplace = (IUpdate)globalActions.get(ActionFactory.FIND.getId());
			if (findReplace != null) {
				findReplace.update();
			}
		}
	};
    
    public IOConsolePage(IOConsole console, IConsoleView view) {
        this.console = console;
        this.consoleView = view;
    } 

    public IPageSite getSite() {
        return site;
    }

    public void init(IPageSite site) throws PartInitException {
        this.site = site;
    }

    public void createControl(Composite parent) {
		viewer = new IOConsoleViewer(parent, console.getDocument());
		viewer.setWordWrap(console.getWordWrap());
		viewer.setConsoleWidth(console.getConsoleWidth());
		console.addPropertyChangeListener(this);
		
		MenuManager manager= new MenuManager("#IOConsole", "#IOConsole");  //$NON-NLS-1$//$NON-NLS-2$
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
		
		viewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
		viewer.addTextListener(textListener);
    }
    
    public void dispose() {
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
	
    public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		String property = event.getProperty();
		
		if (source.equals(console) && IOConsole.P_FONT.equals(property)) {
			setFont(console.getFont());	
		} else if (IOConsole.P_FONT_STYLE.equals(property)) {
		    viewer.getTextWidget().redraw();
		} else if (IOConsole.P_STREAM_COLOR.equals(property) && source instanceof IOConsoleOutputStream) {
			IOConsoleOutputStream stream = (IOConsoleOutputStream)source;
			if (stream.getConsole().equals(console)) {
				viewer.getTextWidget().redraw();
			}
		} else if (property.equals(IOConsole.P_INPUT_COLOR)) {
		    viewer.getTextWidget().redraw();
		} else if (source.equals(console) && property.equals(IOConsole.P_TAB_SIZE)) {
			if (viewer != null) {
			    Integer tabSize = (Integer)event.getNewValue();
				viewer.getTextWidget().setTabs(tabSize.intValue());
				viewer.getTextWidget().redraw();
			}
		} else if(source.equals(console) && property.equals(IOConsole.P_WORD_WRAP)) {
		    viewer.setWordWrap(console.getWordWrap());
		} else if (source.equals(console) && property.equals(IOConsole.P_CONSOLE_WIDTH)) {
		    viewer.setConsoleWidth(console.getConsoleWidth()); 
		}
	}

    protected void createActions() {
        IActionBars actionBars= getSite().getActionBars();
        TextViewerAction action= new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.0"), ConsoleMessages.getString("IOConsolePage.1"), ConsoleMessages.getString("IOConsolePage.2"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		
		action= new TextViewerAction(viewer, ITextOperationTarget.CUT);
		action.configureAction("Cut", "Cut", "Cut"); 
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setGlobalAction(actionBars, ActionFactory.CUT.getId(), action);
		
		action= new TextViewerAction(viewer, ITextOperationTarget.COPY);
		action.configureAction("Copy", "Copy", "Copy");
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		
		action= new TextViewerAction(viewer, ITextOperationTarget.PASTE);
		action.configureAction("Paste", "Paste", "Paste");
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setGlobalAction(actionBars, ActionFactory.PASTE.getId(), action);
		
		clearOutputAction = new ClearOutputAction(viewer);
		
		scrollLockAction = new ScrollLockAction(viewer);
		scrollLockAction.setChecked(!autoScroll);
		viewer.setAutoScroll(autoScroll);
		
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
	 * @param menu menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		IDocument doc= (IDocument)viewer.getDocument();
		if (doc == null) {
			return;
		}
	
		menu.add((IAction)globalActions.get(ActionFactory.SELECT_ALL.getId()));						
		menu.add((IAction)globalActions.get(ActionFactory.CUT.getId()));
		menu.add((IAction)globalActions.get(ActionFactory.COPY.getId()));
		menu.add((IAction)globalActions.get(ActionFactory.PASTE.getId()));
		menu.add((IAction)globalActions.get(ActionFactory.SELECT_ALL.getId()));

		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add((IAction)globalActions.get(ActionFactory.FIND.getId()));
		menu.add(clearOutputAction);
		menu.add(scrollLockAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void configureToolBar(IToolBarManager mgr) {
//		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTerminate);
//		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveTerminated);
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

}
