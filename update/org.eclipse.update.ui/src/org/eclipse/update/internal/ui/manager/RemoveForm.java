package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class RemoveForm extends FeatureSelectionForm {
	public RemoveForm(UpdateFormPage page) {
		super(page, IUpdateModes.REMOVE);
	}
	
public void initialize(Object modelObject) {
	setTitle("Uninstall Features");
	super.initialize(modelObject);
}

}

