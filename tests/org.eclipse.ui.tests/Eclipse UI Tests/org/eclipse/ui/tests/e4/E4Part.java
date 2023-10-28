package org.eclipse.ui.tests.e4;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import jakarta.annotation.PostConstruct;


public class E4Part {

	@PostConstruct
	public void createPart(Composite parent) {
		new Label(parent, SWT.NONE).setText("TEST");
	}
}
