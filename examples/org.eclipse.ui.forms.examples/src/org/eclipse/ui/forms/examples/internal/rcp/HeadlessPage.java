package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class HeadlessPage extends FormPage {
	private int count;

	public HeadlessPage(FormEditor editor, int count) {
		super(editor, "page"+count, "Page "+count);
		this.count = count;
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = managedForm.getForm().getBody();
		body.setLayout(new GridLayout());
		toolkit.createLabel(body, "The content of the headless page #"+count);
	}
}