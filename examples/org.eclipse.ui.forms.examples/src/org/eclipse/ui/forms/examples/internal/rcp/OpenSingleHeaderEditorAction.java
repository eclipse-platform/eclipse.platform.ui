package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.examples.internal.OpenFormEditorAction;

public class OpenSingleHeaderEditorAction extends OpenFormEditorAction {
	public void run(IAction action) {
		openEditor(new SimpleFormEditorInput("Single Header Editor"),
				"org.eclipse.ui.forms.examples.single-header-editor");
	}
}
