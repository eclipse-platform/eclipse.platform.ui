/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.views;

import java.util.Hashtable;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.examples.wizards.ReusableHelpPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HelpView extends ViewPart implements IPartListener2 {
	private FormToolkit toolkit;
	private IMemento memento;
	private ReusableHelpPart reusableHelpPart;
	private Hashtable pageRecs;
	
	/**
	 * 
	 */
	public HelpView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		reusableHelpPart.createControl(parent, toolkit);
	}

	public void dispose() {
		if (toolkit!=null) {
			toolkit.dispose();
			toolkit = null;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow();
        IPartService service = window.getPartService();
        service.removePartListener(this);		
		super.dispose();
	}
    public void init(IViewSite site, IMemento memento) throws PartInitException {
    	this.memento = memento;
       	init(site);
       	reusableHelpPart = new ReusableHelpPart(site.getWorkbenchWindow());
       	reusableHelpPart.init(memento);
		IWorkbenchWindow window = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow();
        IPartService service = window.getPartService();
        service.addPartListener(this);       	
    }
    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void saveState(IMemento memento) {
    	if (reusableHelpPart!=null)
    		reusableHelpPart.saveState(memento);
    }
    
    private void handlePartActivation(IWorkbenchPartReference ref) {
        if (reusableHelpPart==null)
            return;
        if (!reusableHelpPart.isMonitoringContextHelp())
        	return;
        IWorkbenchPart part = ref.getPart(false);
        if (part.equals(this)) return;
        Display display = part.getSite().getShell().getDisplay();
        Control c = display.getFocusControl();
        if (c != null && c.isVisible() && !c.isDisposed()) {
        	reusableHelpPart.update(c);
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		handlePartActivation(partRef);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (reusableHelpPart!=null)
			reusableHelpPart.setFocus();
	}
}