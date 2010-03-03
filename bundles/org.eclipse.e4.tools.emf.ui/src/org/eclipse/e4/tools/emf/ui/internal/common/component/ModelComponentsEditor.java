package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ModelComponentsEditor extends AbstractComponentEditor {
	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	@Override
	public Image getImage(Display display) {
		if( image == null ) {
			image = new Image(display, getClass().getClassLoader().getResourceAsStream("/icons/application_view_icons.png"));
		}
		return image;
	}

	@Override
	public String getLabel() {
		return "Model Components";
	}

	@Override
	public String getDescription() {
		return "Some bla bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new DataBindingContext();
			composite = createForm(parent);
		}
		master.setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent) {
		parent = new Composite(parent, SWT.NONE);

		return parent;
	}

}
