package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ResourceBundle;


/**
 * Action for saving recent changes made in the text editor. The action is
 * initially associated with a text editor via the constructor, but that can be
 * subsequently changed using <code>setEditor</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class SaveAction extends TextEditorAction {
	
	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction
	 */
	public SaveAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/*
	 * @see IAction#run
	 */
	public void run() {
		getTextEditor().getSite().getPage().saveEditor(getTextEditor(), false);
	}
	
	/*
	 * @see TextEditorAction#update
	 */
	public void update() {
		setEnabled(getTextEditor().isDirty());
	}
}
