package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;


/**
 * Creates a new task marker. Uses the Workbench's task properties dialog.
 */
public class AddTaskAction extends AddMarkerAction {
	
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
	public AddTaskAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor, IMarker.TASK, false);
	}
	
	/*
	 * @see IAction#run()
	 */
	public void run() {
		
		IResource resource= getResource();
		if (resource == null)
			return;
		Map attributes= getInitialAttributes();

		TaskPropertiesDialog dialog = new TaskPropertiesDialog(getTextEditor().getSite().getShell());
		dialog.setResource(resource);
		dialog.setInitialAttributes(attributes);
		dialog.open();
	}
}
