package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class InstallForm extends FeatureSelectionForm {
	public InstallForm(UpdateFormPage page) {
		super(page, IUpdateModes.INSTALL);
	}
	
public void initialize(Object modelObject) {
	setTitle("Install New Features");
	super.initialize(modelObject);
}

}

