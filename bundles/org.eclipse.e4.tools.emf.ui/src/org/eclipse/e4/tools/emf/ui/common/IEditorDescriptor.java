package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.emf.ecore.EClass;

public interface IEditorDescriptor {
	public EClass getEClass();
	public Class<?> getEditorClass();
}
