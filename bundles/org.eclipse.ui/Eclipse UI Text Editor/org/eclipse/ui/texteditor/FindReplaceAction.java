package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ResourceBundle;import org.eclipse.swt.events.DisposeEvent;import org.eclipse.swt.events.DisposeListener;import org.eclipse.swt.widgets.Shell;import org.eclipse.jface.text.IFindReplaceTarget;import org.eclipse.ui.IPartListener;import org.eclipse.ui.IPartService;import org.eclipse.ui.IWorkbenchPart;import org.eclipse.ui.IWorkbenchPartSite;import org.eclipse.ui.IWorkbenchWindow;


/**
 * An action which opens a Find/Replace dialog. 
 * The dialog while open, tracks the active workbench part 
 * and retargets itself to the active find/replace target.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IFindReplaceTarget
 */
public class FindReplaceAction extends ResourceAction implements IUpdate {
		
	/**
	 * Represents the "global" find/replace dialog. It tracks the active
	 * part and retargets the find/replace dialog accordingly. The find/replace
	 * target is retrieved from the active part using  
	 * <code>getAdapter(IFindReplaceTarget.class)</code>.
	 * <p>
	 * The stub has the same life cycle as the find/replace dialog.
	 */
	class FindReplaceDialogStub implements IPartListener, DisposeListener {
		
		private IWorkbenchPart fPart;
		private IWorkbenchPart fPreviousPart;
		private IFindReplaceTarget fPreviousTarget;
		
		private IWorkbenchWindow fWindow;
		private FindReplaceDialog fDialog;
		
		public FindReplaceDialogStub(IWorkbenchPartSite site) {
			
			fWindow= site.getWorkbenchWindow();
			
			fDialog= new FindReplaceDialog(site.getShell());			
			fDialog.create();
			fDialog.getShell().addDisposeListener(this);
			
			IPartService service= fWindow.getPartService();
			service.addPartListener(this);
			partActivated(service.getActivePart());
		}
		
		public FindReplaceDialog getDialog() {
			return fDialog;
		}
		
		public void partActivated(IWorkbenchPart part) {			
			
			IFindReplaceTarget target= part == null ? null : (IFindReplaceTarget) part.getAdapter(IFindReplaceTarget.class);
			fPreviousPart= fPart;
			fPart= target == null ? null : part;
			
			if (fPreviousTarget != target) {
				fPreviousTarget= target;
				if (fDialog != null) {
					boolean isEditable= false;
					if (fPart instanceof ITextEditorExtension) {
						ITextEditorExtension extension= (ITextEditorExtension) fPart;
						isEditable= !extension.isEditorInputReadOnly();
					}
					fDialog.updateTarget(target, isEditable);
				}
			}
		}
		
		public void partClosed(IWorkbenchPart part) {
			
			if (part == fPreviousPart) {
				fPreviousPart= null;
				fPreviousTarget= null;
			}
			
			if (part == fPart)
				partActivated(null);
		}
		
		public void widgetDisposed(DisposeEvent event) {
			
			if (fgFindReplaceDialogStub == this)
				fgFindReplaceDialogStub= null;
				
			if (fWindow != null) {
				fWindow.getPartService().removePartListener(this);
				fWindow= null;
			}
			fDialog= null;
			fPart= null;
			fPreviousPart= null;
			fPreviousTarget= null;
		}
		
		public void partOpened(IWorkbenchPart part) {}
		public void partDeactivated(IWorkbenchPart part) {}
		public void partBroughtToTop(IWorkbenchPart part) {}		
	};
	
	
	/** Lister for disabling the dialog on editor close */
	private static FindReplaceDialogStub fgFindReplaceDialogStub;
	/** The action's target */
	private IFindReplaceTarget fTarget;
	/** The part the action is bound to */
	private IWorkbenchPart fWorkbenchPart;
	/** The workbench window */
	private IWorkbenchWindow fWorkbenchWindow;
	/** Indicates whether the find/replace target is editable */ 
	private boolean fIsTargetEditable= false;

	/**
	 * Creates a new find/replace action for the given text editor. 
	 * The action configures its visual representation from the given 
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction
	 */
	public FindReplaceAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart) {
		super(bundle, prefix);
		fWorkbenchPart= workbenchPart;
		update();
	}
	
	/**
	 * Creates a new find/replace action for the given text editor. 
	 * The action configures its visual representation from the given 
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param workbenchWindow the workbench window
	 * @see ResourceAction#ResourceAction
	 * 
	 * @deprecated use FindReplaceAction(ResourceBundle, String, IWorkbenchPart) instead
	 */
	public FindReplaceAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		update();
	}
	
	/*
	 *	@see IAction#run
	 */
	public void run() {
		if (fTarget != null) {
			
			if (fgFindReplaceDialogStub != null) {
				Shell shell= fWorkbenchPart.getSite().getShell();
				FindReplaceDialog dialog= fgFindReplaceDialogStub.getDialog();
				if (dialog != null && shell != dialog.getParentShell()) {
					fgFindReplaceDialogStub= null; // here to avoid timing issues
					dialog.close();
				}
			}
			
			if (fgFindReplaceDialogStub == null)
				fgFindReplaceDialogStub= new FindReplaceDialogStub(fWorkbenchPart.getSite());
				
			FindReplaceDialog dialog= fgFindReplaceDialogStub.getDialog();
			dialog.updateTarget(fTarget, fIsTargetEditable);
			dialog.open();
		}
	}
	
	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		
		if (fWorkbenchPart == null && fWorkbenchWindow != null)
			fWorkbenchPart= fWorkbenchWindow.getPartService().getActivePart();
			
		if (fWorkbenchPart instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) fWorkbenchPart;
			fIsTargetEditable= !extension.isEditorInputReadOnly();
		}
		
		if (fWorkbenchPart != null)
			fTarget= (IFindReplaceTarget) fWorkbenchPart.getAdapter(IFindReplaceTarget.class);
		else
			fTarget= null;
			
		setEnabled(fTarget != null && fTarget.canPerformFind());
		
//		if (fgFindReplaceDialogStub != null) {
//			FindReplaceDialog dialog= fgFindReplaceDialogStub.getDialog();
//			dialog.updateTarget(fTarget, fIsTargetEditable);
//		}
	}
}
