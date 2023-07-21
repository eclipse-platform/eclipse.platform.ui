package org.eclipse.ui.tests.e4;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class E4Part {

	@PostConstruct
	public void createPart(Composite parent) {
		new Label(parent, SWT.NONE).setText("TEST");
	}
}
