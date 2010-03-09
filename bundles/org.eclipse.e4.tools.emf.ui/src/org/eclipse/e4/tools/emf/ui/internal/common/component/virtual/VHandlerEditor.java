package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class VHandlerEditor extends AbstractComponentEditor {
	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;

	public VHandlerEditor(EditingDomain editingDomain) {
		super(editingDomain);
	}

	@Override
	public Image getImage(Object element, Display display) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return "Handlers";
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return "Handlers Bla Bla Bla Bla Bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new EMFDataBindingContext();
			composite = createForm(parent,context, getMaster());
		}
		getMaster().setValue(((VirtualEntry<?>)object).getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue master) {
		parent = new Composite(parent,SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Handlers");
			l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

			TableViewer viewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 300;
			viewer.getControl().setLayoutData(gd);
			viewer.getTable().setHeaderVisible(true);

			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Id");
			column.getColumn().setWidth(150);

			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Command");
			column.getColumn().setWidth(200);
			
			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Class");
			column.getColumn().setWidth(300);
			
			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
			IValueProperty[] props = {
				EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID),
				EMFEditProperties.value(getEditingDomain(), FeaturePath.fromList(MApplicationPackage.Literals.HANDLER__COMMAND, MApplicationPackage.Literals.COMMAND__COMMAND_NAME)),
				EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.CONTRIBUTION__URI)
			};
			
			ViewerSupport.bind(viewer, prop.observeDetail(getMaster()), props);
			
			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL,GridData.END,false,false));
			GridLayout gl = new GridLayout();
			gl.marginLeft=0;
			gl.marginRight=0;
			gl.marginWidth=0;
			gl.marginHeight=0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Up");
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Down");
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Add ...");
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Remove");
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		}


		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
