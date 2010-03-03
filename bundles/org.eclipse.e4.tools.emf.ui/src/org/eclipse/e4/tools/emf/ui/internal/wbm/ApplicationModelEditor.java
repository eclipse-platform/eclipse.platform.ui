package org.eclipse.e4.tools.emf.ui.internal.wbm;

import javax.inject.Inject;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.swt.widgets.Composite;

public class ApplicationModelEditor extends ModelEditor {
	@Inject
	public ApplicationModelEditor(Composite composite,
			IModelResource modelProvider) {
		super(composite, modelProvider);
	}

}
