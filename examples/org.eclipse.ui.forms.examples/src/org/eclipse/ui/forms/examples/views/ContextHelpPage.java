/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.views;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.examples.wizards.ContextHelpPart;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContextHelpPage implements IHelpViewPage, IPartListener2 {
	public static final String ID = "context-help";
	private ContextHelpPart part;
	private HelpView view;
	
	public ContextHelpPage() {
	}

	public void init(HelpView view, IMemento memento) {
        this.view = view;
		IViewSite site = view.getViewSite();
		part = new ContextHelpPart(site.getWorkbenchWindow());
        IWorkbenchWindow window = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow();
        IPartService service = window.getPartService();
        service.addPartListener(this);
	}
	
	public void saveState(IMemento memento) {
	}

	public void addToActionBars(IActionBars bars) {
	}
	public String getId() {
		return ID;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	public void createControl(Composite parent, FormToolkit toolkit) {
		part.setDefaultText("Click anywhere in the workbench to see a description of the selected part.");		
		part.createControl(parent, toolkit);
		part.getForm().setText("Context Help");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#dispose()
	 */
	public void dispose() {
		if (part!=null) {
			part.dispose();
			part = null;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#getControl()
	 */
	public Control getControl() {
		if (part!=null)
			return part.getControl();
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#setFocus()
	 */
	public void setFocus() {
		if (part!=null)
			part.setFocus();
	}
	private void handlePartActivation(IWorkbenchPartReference ref, boolean active) {
        String text = null;
        if (ref != null && active) {
           IWorkbenchPart refPart = ref.getPart(false);
           if (refPart.equals(view))
              return;
	       Display display = ref.getPage().getWorkbenchWindow().getShell().getDisplay();
	       Control c = display.getFocusControl();
	       if (c != null && c.isVisible() && !c.isDisposed()) {
	          part.update(c);
	          return;
	       }
        }
        part.update(null);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
        if (view.getCurrentPage()==this)
        	handlePartActivation(partRef, true);
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
        //handlePartActivation(partRef, false);		
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
}
