/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.util.Hashtable;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
public class HelpView extends ViewPart implements IPartListener2, ISelectionChangedListener {
	private FormToolkit toolkit;

	private IMemento memento;

	private ReusableHelpPart reusableHelpPart;

	private Hashtable pageRecs;
	private IWorkbenchPart monitoredPart;
	private boolean visible;

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
    	//toolkit.setBackground(toolkit.getColors().createColor("bg", 245, 250, 255));
		reusableHelpPart.createControl(parent, toolkit);
		reusableHelpPart
				.setDefaultContextHelpText("Click on any workbench part to show its context help.");
		reusableHelpPart.showPage(IHelpViewConstants.CONTEXT_HELP_PAGE);
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window==null) return;
		IWorkbenchPage page = window.getActivePage();
		if (page==null) return;
		IWorkbenchPartReference aref = page.getActivePartReference();
		if (aref!=null)
			handlePartActivation(aref);
	}

	public void dispose() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IPartService service = window.getPartService();
		if (monitoredPart!=null)
			uninstallSelectionListener(monitoredPart);
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
		if (isThisPart(ref))
			return;
		IWorkbenchPart part = ref.getPart(false);
		Display display = part.getSite().getShell().getDisplay();
		Control c = display.getFocusControl();
		if (c != null && c.isVisible() && !c.isDisposed()) {
			IContextHelpProvider provider = (IContextHelpProvider)part.getAdapter(IContextHelpProvider.class);
			if (provider!=null) {
				reusableHelpPart.update(provider, c);
				if ((provider.getContextHelpChangeMask() & IContextHelpProvider.SELECTION)!=0) {
					// context help changes with selections
					installSelectionListener(part);
				}
			}
			else
				reusableHelpPart.update(c);
		}
	}
	
	private void installSelectionListener(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider instanceof IPostSelectionProvider)
			((IPostSelectionProvider)provider).addPostSelectionChangedListener(this);
		else
			provider.addSelectionChangedListener(this);
		monitoredPart = part;
		System.out.println("Installing "+part.getSite().getRegisteredName());
	}
	private void uninstallSelectionListener(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider instanceof IPostSelectionProvider)
			((IPostSelectionProvider)provider).removePostSelectionChangedListener(this);
		else
			provider.removeSelectionChangedListener(this);
		monitoredPart = null;
		System.out.println("Uninstalling "+part.getSite().getRegisteredName());
	}
	
	private boolean isThisPart(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		return part.equals(this);
	}
	
	private void updateActivePart() {
		if (reusableHelpPart == null)
			return;
		if (!reusableHelpPart.isMonitoringContextHelp())
			return;
		if (monitoredPart==null)
			return;
		IContextHelpProvider provider = (IContextHelpProvider)monitoredPart.getAdapter(IContextHelpProvider.class);
		Control c = monitoredPart.getSite().getShell().getDisplay().getFocusControl();
		if (c!=null && c.isDisposed()==false && provider!=null) {
			reusableHelpPart.update(provider, c);
		}
	}

	private void handlePartDeactivation(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (monitoredPart!=null && part!=null && part.equals(monitoredPart)) {
			uninstallSelectionListener(part);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(final IWorkbenchPartReference partRef) {
		if (isThisPart(partRef))
			visible = true;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				handlePartActivation(partRef);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible= true;
			selectionChanged(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
		handlePartDeactivation(partRef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
		handlePartDeactivation(partRef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef))
			visible = false;
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
		if (isThisPart(partRef)) {
			visible = true;
			selectionChanged(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible=true;
			selectionChanged(null);
		}
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
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if (!visible) return;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateActivePart();
			}
		});
	}
}