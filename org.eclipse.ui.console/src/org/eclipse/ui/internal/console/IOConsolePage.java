package org.eclipse.ui.internal.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

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
    
    private boolean isLocked = false;
    private Map globalActions = new HashMap();
    private ClearOutputAction clearOutputAction;
    private ScrollLockAction scrollLockAction;
    
    public IOConsolePage(IOConsole console) {
        this.console = console;
    } 

    public IPageSite getSite() {
        return site;
    }

    public void init(IPageSite site) throws PartInitException {
        this.site = site;
    }

    public void createControl(Composite parent) {
		viewer = new IOConsoleViewer(parent, console.getDocument());
		viewer.setDocument(console.getDocument());
		viewer.setWordWrap(console.getWordWrap());
		console.addPropertyChangeListener(this);
		createActions();
		configureToolBar(getSite().getActionBars().getToolBarManager());
    }
    
    public void dispose() {
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
		}
	}

    protected void createActions() {
        IActionBars actionBars= getSite().getActionBars();
        TextViewerAction action= new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.getString("IOConsolePage.0"), ConsoleMessages.getString("IOConsolePage.1"), ConsoleMessages.getString("IOConsolePage.2"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		
		clearOutputAction = new ClearOutputAction(viewer);
		
		scrollLockAction = new ScrollLockAction(viewer);
		scrollLockAction.setChecked(isLocked);
		viewer.setAutoScroll(!isLocked);
		
		actionBars.updateActionBars();
    }
    
    protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
        globalActions.put(actionID, action);  
        actionBars.setGlobalActionHandler(actionID, action);
    }

	protected void contextMenuAboutToShow(IMenuManager menu) {
		menu.add((IAction)globalActions.get(ActionFactory.SELECT_ALL.getId()));
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
        if (Widget.class.equals(required)) {
			return viewer.getTextWidget();
		}
		return null;
    }

}
