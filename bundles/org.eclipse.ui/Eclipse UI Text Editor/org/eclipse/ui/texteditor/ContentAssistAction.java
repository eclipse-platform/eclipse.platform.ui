package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ResourceBundle;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextOperationTargetExtension;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IWorkbenchPartSite;


/**
 * An action which gets a text operation target from its text editor.
 * <p>
 * The action is initially associated with a text editor via the constructor,
 * but can subsequently be changed using <code>setEditor</code>.
 * </p>
 * <p>
 * If this class is used as is, it works by asking the text editor for its
 * text operation target adapter (using <code>getAdapter(ITextOperationTarget.class)</code>. 
 * The action runs this operation with the pre-configured opcode.
 * </p>
 */
public final class ContentAssistAction extends TextEditorAction {
	
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	
	/**
	 * Creates and initializes the action for the given text editor.
	 * The action configures its visual representation from the given resource
	 * bundle. The action works by asking the text editor at the time for its 
	 * text operation target adapter (using
	 * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
	 * operation with the given opcode.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction
	 */
	public ContentAssistAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
		update();
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IAction</code> method runs the operation with the current
	 * operation code.
	 */
	public void run() {
		if (fOperationTarget != null) {
			
			ITextEditor editor= getTextEditor();
			if (editor != null) {
				
				Display display= null;
				
				IWorkbenchPartSite site= editor.getSite();
				Shell shell= site.getShell();
				if (shell != null && !shell.isDisposed()) 
					display= shell.getDisplay();
			
				BusyIndicator.showWhile(display, new Runnable() {
					public void run() {
						fOperationTarget.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
					}
				});
			}
		}
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IUpdate</code> method discovers the operation through the current
	 * editor's <code>ITextOperationTarget</code> adapter, and sets the
	 * enabled state accordingly.
	 */
	public void update() {
		
		ITextEditor editor= getTextEditor();

		if (fOperationTarget == null && editor!= null)
			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
		
		if (fOperationTarget == null) {
			setEnabled(false);
			return;
		}
		
		if (editor instanceof ITextEditorExtension && fOperationTarget instanceof ITextOperationTargetExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) editor;
			ITextOperationTargetExtension targetExtension= (ITextOperationTargetExtension) fOperationTarget;
			boolean isEnabled= !extension.isEditorInputReadOnly();
			targetExtension.enableOperation(ISourceViewer.CONTENTASSIST_PROPOSALS, isEnabled);
		}
		
		setEnabled(fOperationTarget.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS));
	}
	
	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		fOperationTarget= null;
	}
}
