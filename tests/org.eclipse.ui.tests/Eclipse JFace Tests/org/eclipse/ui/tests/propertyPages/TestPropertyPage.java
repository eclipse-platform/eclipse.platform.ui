package org.eclipse.ui.tests.propertyPages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class TestPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public TestPropertyPage() {
		//Create a new instance of the receiver
	}

	protected Control createContents(Composite parent) {
		Label label = new Label(parent,SWT.NONE);
		label.setText("Test page");
		return label;
		
	}

}
