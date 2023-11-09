package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;

import jakarta.inject.Inject;

public class VPartMenuEditor extends VMenuEditor<MPart> {
	@Inject
	public VPartMenuEditor() {
		super(BasicPackageImpl.Literals.PART__MENUS);
	}

}
