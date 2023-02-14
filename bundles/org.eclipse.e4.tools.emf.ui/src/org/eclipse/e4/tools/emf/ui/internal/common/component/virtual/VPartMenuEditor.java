package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;

public class VPartMenuEditor extends VMenuEditor<MPart> {
	@Inject
	public VPartMenuEditor() {
		super(BasicPackageImpl.Literals.PART__MENUS);
	}

}
