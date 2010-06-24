package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.edit.domain.EditingDomain;

public class TrimmedWindowEditor extends WindowEditor {
	private IListProperty TRIMMED_WINDOW__TRIM_BARS = EMFProperties.list(BasicPackageImpl.Literals.TRIMMED_WINDOW__TRIM_BARS);
	
	public TrimmedWindowEditor(EditingDomain editingDomain, ModelEditor editor, IProject project) {
		super(editingDomain,editor, project);
	}

	public IObservableList getChildList(Object element) {
		IObservableList list = super.getChildList(element);
		
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_TRIMMED_WINDOW_TRIMS, TRIMMED_WINDOW__TRIM_BARS, element, "TrimBars") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		return list;
	}
	
	@Override
	public String getLabel(Object element) {
		return "Trimmed Window";
	}
}