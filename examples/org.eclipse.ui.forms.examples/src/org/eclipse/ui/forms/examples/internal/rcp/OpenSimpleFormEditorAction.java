package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.examples.internal.OpenFormEditorAction;
/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenSimpleFormEditorAction extends OpenFormEditorAction {
	public void run(IAction action) {
		openEditor(new SimpleFormEditorInput("Simple Editor"), "org.eclipse.ui.forms.examples.base-editor");
	}
}
