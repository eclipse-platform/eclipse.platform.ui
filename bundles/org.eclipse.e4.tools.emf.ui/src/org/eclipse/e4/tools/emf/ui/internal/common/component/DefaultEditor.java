package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class DefaultEditor extends AbstractComponentEditor {

	@Override
	public Image getImage(Object element, Display display) {
		// no image
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return element.getClass().getInterfaces()[0].getSimpleName() + " " + Messages.Special_UnknownElement; //$NON-NLS-1$
	}

	@Override
	public String getDetailLabel(Object element) {
		return Messages.Special_UnknownElement_Detail;
	}

	@Override
	public String getDescription(Object element) {
		return null;
	}

	@Override
	protected Composite doGetEditor(Composite parent, Object object) {
		return new Composite(parent, SWT.NONE);
	}

	@Override
	public IObservableList getChildList(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
