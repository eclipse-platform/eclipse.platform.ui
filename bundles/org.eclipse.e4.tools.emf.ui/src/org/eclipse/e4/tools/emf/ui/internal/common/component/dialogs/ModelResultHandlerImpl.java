package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.ModelResultHandler;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public class ModelResultHandlerImpl implements ModelResultHandler {
	private boolean canceled = false;
	private final IObservableList<Object> list;
	private final Filter filter;
	private final AbstractComponentEditor<?> editor;
	private final Resource resource;

	public ModelResultHandlerImpl(IObservableList<Object> list, Filter filter, AbstractComponentEditor<?> editor,
			Resource resource) {
		this.list = list;
		this.filter = filter;
		this.editor = editor;
		this.resource = resource;
	}

	public void cancel() {
		canceled = true;
	}

	@Override
	public void result(EObject data) {
		if (!canceled) {
			if (!resource.getURI().equals(data.eResource().getURI())) {
				if (data instanceof MApplicationElement) {
					final String elementId = ((MApplicationElement) data).getElementId();
					if (elementId == null) {
						list.add(data);
						return;
					}

					if (elementId.trim().length() > 0) {
						if (filter.elementIdPattern.matcher(elementId).matches()) {
							list.add(data);
							return;
						}
					}

					final String label = editor.getDetailLabel(data);
					if (label != null && label.trim().length() > 0) {
						if (filter.elementIdPattern.matcher(label).matches()) {
							list.add(data);
							return;
						}
					}
				}
			}
		}
	}
}