/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/


package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.ITextOperationTarget;

import org.eclipse.ui.IWorkbenchPartSite;



/**
 * Action for shifting code to the right or left by one indentation level.
 * @since 2.0
 */
public final class ShiftAction extends TextEditorAction implements IReadOnlyDependent {
	
	/** The text operation code */
	private int fOperationCode= -1;
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	
	/**
	 * Creates and initializes the action for the given text editor and operation 
	 * code. The action configures its visual representation from the given resource
	 * bundle. The action works by asking the text editor at the time for its 
	 * text operation target adapter (using
	 * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
	 * operation with the given opcode.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or  <code>null</code> if none
	 * @param editor the text editor
	 * @param operationCode the operation code
	 * @see ResourceAction#ResourceAction
	 */
	public ShiftAction(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
		super(bundle, prefix, editor);
		fOperationCode= operationCode;
		update();
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IAction</code> method runs the operation with the current
	 * operation code.
	 */
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null) {
			
			ITextEditor editor= getTextEditor();
			if (editor != null) {
				
				Display display= null;
				
				IWorkbenchPartSite site= editor.getSite();
				Shell shell= site.getShell();
				if (shell != null && !shell.isDisposed()) 
					display= shell.getDisplay();
			
				BusyIndicator.showWhile(display, new Runnable() {
					public void run() {
						fOperationTarget.doOperation(fOperationCode);
					}
				});
			}
		}
	}
	
	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) editor;
			if (extension.isEditorInputReadOnly()) {
				setEnabled(false);
				return;
			}
		}
		
		if (fOperationTarget == null && editor!= null && fOperationCode != -1)
			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
			
		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);
	}
	
	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		fOperationTarget= null;
	}
	
	/*
	 * @see IReadOnlyDependent#isEnabled(boolean)
	 */
	public boolean isEnabled(boolean isWritable) {
		
		if (!isWritable)
			return false;
			
		/*
		 * Note that this implementation still honors the result returned by canDoOperation.
		 * I.e. if the viewer is set to read-only, this method still returns false.
		 * It covers the case in which the viewer is also writable.
		 *  
		 */
		ITextEditor editor= getTextEditor();
		if (fOperationTarget == null && editor!= null && fOperationCode != -1)
			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
			
		return (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
	}
}
