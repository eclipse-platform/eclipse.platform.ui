package org.eclipse.ui.texteditor;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


import java.util.ResourceBundle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * An action which opens a Find/Replace dialog. While the dialog is open, the
 * action tracks the active workbench part and retargets the dialog and
 * enables/disables the action accordingly.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IFindReplaceTarget
 */
public class FindReplaceAction extends ResourceAction implements IUpdate {
	
	/**
	 * Dispose listener used to track how long the dialog is open.
	 */
	class ShellDisposeListener implements DisposeListener {
		
		/*
		 * @see DisposeListener#widgetDisposed(DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent event) {
			fWorkbenchWindow.getPartService().removePartListener(fPartListener);
			fgFindReplaceDialog= null;
			fShellDisposeListener= null;
		}
	};
	
	/**
	 * Part listener used to track the active part and to retarget the open
	 * dialog on activation changes. The find/replace target is retrieved from the
	 * active part using  <code>getAdapter(IFindReplaceTarget.class)</code>.
	 */
	class PartListener implements IPartListener {
		
		private IWorkbenchPart fActivePart;
		
		public void partActivated(IWorkbenchPart part) {			
			fActivePart= part;
			fTarget= fActivePart == null ? null : (IFindReplaceTarget) fActivePart.getAdapter(IFindReplaceTarget.class);
			update();
			if (fgFindReplaceDialog != null)
				fgFindReplaceDialog.updateTarget(fTarget);
		}
		
		public void partClosed(IWorkbenchPart part) {
			if (part == fActivePart)
				partActivated(null);
		}
		
		public void partOpened(IWorkbenchPart part) {
		}
		
		public void partDeactivated(IWorkbenchPart part) {
		}
		
		public void partBroughtToTop(IWorkbenchPart part) {
		}
	};

	
	/** Dialog shared between all actions */
	private static FindReplaceDialog fgFindReplaceDialog;
	
	private PartListener fPartListener;
	private DisposeListener fShellDisposeListener;
	private IFindReplaceTarget fTarget;
	private IWorkbenchWindow fWorkbenchWindow;
	
	/**
	 * Creates a new action for the given workbench window. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param window the workbench window this action operates on
	 * @see ResourceAction#ResourceAction
	 */
	public FindReplaceAction(ResourceBundle bundle, String prefix, IWorkbenchWindow window) {
		super(bundle, prefix);
		fWorkbenchWindow= window;
		
		fPartListener= new PartListener();
		fWorkbenchWindow.getPartService().addPartListener(fPartListener);
		IWorkbenchPart activePart= fWorkbenchWindow.getPartService().getActivePart(); 
		fPartListener.partActivated(activePart);
	}
	/**
	 * Returns the shared find/replace dialog for the given shell.
	 * If there is already a dialog, but its parent shell is not 
	 * the given shell, the dialog is closed and a new one is created
	 * using the given shell as its parent shell.
	 */
	private static FindReplaceDialog createDialog(Shell shell) {
		
		if (fgFindReplaceDialog != null) {
			Shell s= fgFindReplaceDialog.getParentShell();
			if (s != shell) {
				fgFindReplaceDialog.close();
				fgFindReplaceDialog= null;
			}
		}
		
		if (fgFindReplaceDialog == null) {
			fgFindReplaceDialog= new FindReplaceDialog(shell, "Find/Replace", null);			
			fgFindReplaceDialog.create();
		}
			
		return fgFindReplaceDialog;
	}
	/*
	 *	@see IAction#run
	 */
	public void run() {
		if (fTarget != null && fTarget.canPerformFind()) {
			
			Shell shell= fWorkbenchWindow.getShell();
			
			FindReplaceDialog dialog= createDialog(shell);
			if (fShellDisposeListener == null) {
				fShellDisposeListener= new ShellDisposeListener();
				dialog.getShell().addDisposeListener(fShellDisposeListener);
			}
			
			dialog.setParentShell(shell);
			dialog.updateTarget(fTarget);
			dialog.open();
		}
	}
	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		setEnabled(fTarget != null && fTarget.canPerformFind());
	}
}
