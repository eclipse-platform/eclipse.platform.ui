package org.eclipse.update.internal.ui.manager;
import org.eclipse.swt.widgets.*;
import java.net.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.resource.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;

public class ControlSection extends UpdateSection {
	private int mode;
	private Button button;

public ControlSection(UpdateFormPage page, int mode) {
	super(page);
	this.mode = mode;
	setHeaderPainted(false);
	setAddSeparator(false);
}

public final Composite createClient(
	Composite parent,
	FormWidgetFactory factory) {
	Composite container = factory.createComposite(parent);
	GridLayout layout = new GridLayout();
	container.setLayout(layout);
	button = factory.createButton(container, null, SWT.PUSH);
	switch (mode) {
		case IUpdateModes.INSTALL:
		button.setText("Install");
		break;
		case IUpdateModes.UPDATE:
		button.setText("Update");
		break;
		case IUpdateModes.REMOVE:
		button.setText("Remove");
		break;
	}
	GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
	button.setLayoutData(gd);
	return container;
}

public void initialize(Object model) {
}

}