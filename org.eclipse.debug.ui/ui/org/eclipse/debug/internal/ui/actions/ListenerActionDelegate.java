package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class ListenerActionDelegate extends ControlActionDelegate implements IDebugEventListener, IPartListener {

	/**
	 * The window associated with this action delegate
	 */
	private IWorkbenchWindow fWindow;

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(getView())) {
			dispose();
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}
	
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		if (getView() != null) {
			getView().getViewSite().getPage().removePartListener(this);
		}
	}
	
	/**
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(final DebugEvent event) {
		if (getPage() == null || getAction() == null) {
			return;
		}
		Object element= event.getSource();
		if (element == null) {
			return;
		}
		if (getPage().getWorkbenchWindow().getShell().isDisposed()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				if (getPage().getWorkbenchWindow().getShell().isDisposed()) {
					return;
				}
				doHandleDebugEvent(event);
			}
		};
		
		getPage().getWorkbenchWindow().getShell().getDisplay().asyncExec(r);
	}
	
	protected IWorkbenchPage getPage() {
		if (getWindow() != null) {
			return getWindow().getActivePage();
		} else {
			return DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		}
	}
	
	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}

	protected void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}
	
	protected abstract void doHandleDebugEvent(DebugEvent event);

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		super.init(window);
		DebugPlugin.getDefault().addDebugEventListener(this);
		setWindow(window);
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().addDebugEventListener(this);
		setWindow(view.getViewSite().getWorkbenchWindow());
		getPage().addPartListener(this);
	}
}
