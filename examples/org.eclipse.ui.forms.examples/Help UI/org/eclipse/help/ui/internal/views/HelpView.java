/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.util.Hashtable;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
    	toolkit.setBackground(toolkit.getColors().createColor("bg", 245, 250, 255));
		reusableHelpPart.createControl(parent, toolkit);
		reusableHelpPart
				.setDefaultContextHelpText("Click on any workbench part to show its context help.");
		reusableHelpPart.showPage(IHelpViewConstants.CONTEXT_HELP_PAGE);
	}

	public void dispose() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IPartService service = window.getPartService();
		service.removePartListener(this);
		if (reusableHelpPart != null) {
			reusableHelpPart.dispose();
			reusableHelpPart = null;
		}
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		super.dispose();
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		this.memento = memento;
		init(site);
		reusableHelpPart = new ReusableHelpPart(site.getWorkbenchWindow());
		reusableHelpPart.init(site.getActionBars().getToolBarManager());
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IPartService service = window.getPartService();
		service.addPartListener(this);
	}

	/*
	 * (non-Javadoc) Method declared on IViewPart.
	 */
	public void saveState(IMemento memento) {
	}

	private void handlePartActivation(IWorkbenchPartReference ref) {
		if (reusableHelpPart == null)
			return;
		if (!reusableHelpPart.isMonitoringContextHelp())
			return;
		IWorkbenchPart part = ref.getPart(false);
		if (part.equals(this))
			return;
		Display display = part.getSite().getShell().getDisplay();
		Control c = display.getFocusControl();
		if (c != null && c.isVisible() && !c.isDisposed()) {
			reusableHelpPart.update(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		handlePartActivation(partRef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (reusableHelpPart != null)
			reusableHelpPart.setFocus();
	}
}