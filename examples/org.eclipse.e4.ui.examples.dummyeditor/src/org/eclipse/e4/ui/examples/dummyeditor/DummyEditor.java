package org.eclipse.e4.ui.examples.dummyeditor;

import jakarta.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class DummyEditor {

	@PostConstruct
	public void postConstruct(Composite parent) {
		Text label = new Text(parent, SWT.ITALIC);
		label.setText("Dummy Editor Content...");

	}
}
