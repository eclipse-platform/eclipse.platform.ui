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

public class FeatureListSection extends UpdateSection {
	private int mode;

public FeatureListSection(UpdateFormPage page, int mode) {
	super(page);
	this.mode = mode;
	if (mode != IUpdateModes.INSTALL) {
		setHeaderText("Installed Features");
	}
	else {
		setHeaderText("Available Features");
	}
}

public final Composite createClient(
	Composite parent,
	FormWidgetFactory factory) {
	Composite container = factory.createComposite(parent);
	GridLayout layout = new GridLayout();
	container.setLayout(layout);
	return container;
}

public void initialize(Object model) {
	inputChanged(null);
}

private void inputChanged(IFeature feature) {
	if (feature==null) {
		return;
	}
}

public void sectionChanged(FormSection source, int type, Object object) {
	if (type == FormSection.SELECTION)
	   inputChanged((IFeature)object);
}

}