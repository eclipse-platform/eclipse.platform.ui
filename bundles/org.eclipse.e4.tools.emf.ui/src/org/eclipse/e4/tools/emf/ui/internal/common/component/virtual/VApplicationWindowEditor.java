package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;

public class VApplicationWindowEditor extends VWindowEditor {

	@Inject
	public VApplicationWindowEditor() {
		super(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	}

}