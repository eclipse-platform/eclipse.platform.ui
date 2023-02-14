package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;

public class VApplicationWindowEditor extends VWindowEditor<MApplication> {

	@Inject
	public VApplicationWindowEditor() {
		super(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	}

}