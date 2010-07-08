package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.ModelResultHandler;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class FindImportElementDialog extends TitleAreaDialog {
	private EObject element;
	private AbstractComponentEditor editor;
	private TableViewer viewer;

	public FindImportElementDialog(Shell parentShell, AbstractComponentEditor editor, EObject element) {
		super(parentShell);
		this.element = element;
		this.editor = editor;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		final Image titleImage = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/import_wiz.png"));
		setTitleImage(titleImage);
		getShell().addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				titleImage.dispose();
			}
		});

		getShell().setText("Find Import Elements");
		setTitle("Find Import Elements");
		setMessage("Search for an elements whose ID you'd like to import");

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText("Search");

		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		searchText.setLayoutData(gd);

		l = new Label(container, SWT.PUSH);

		viewer = new TableViewer(container);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				EObject o = (EObject) cell.getElement();
				cell.setImage(editor.getImage(o, searchText.getDisplay()));

				StyledString styledString = new StyledString(editor.getLabel(o), null);
				String detailLabel = editor.getDetailLabel(o);
				if (detailLabel != null) {
					styledString.append(" - " + detailLabel, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				styledString.append(" - " + o.eResource().getURI(), StyledString.COUNTER_STYLER); //$NON-NLS-1$
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setText(styledString.getString());
			}
		});
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		final WritableList list = new WritableList();
		viewer.setInput(list);

		final ClassContributionCollector collector = getCollector();

		searchText.addModifyListener(new ModifyListener() {
			private ModelResultHandlerImpl currentResultHandler;

			public void modifyText(ModifyEvent e) {
				if (currentResultHandler != null) {
					currentResultHandler.cancled = true;
				}
				list.clear();
				currentResultHandler = new ModelResultHandlerImpl(list);
				Filter filter = new Filter(element, searchText.getText());
				collector.findModelElements(filter, currentResultHandler);
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Clear Cache");
		button.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false, 2, 1));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				collector.clearModelCache();
			}
		});

		return comp;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			MApplicationElement el = (MApplicationElement) s.getFirstElement();
			if (el.getElementId() != null && el.getElementId().trim().length() > 0) {
				Command cmd = SetCommand.create(editor.getEditingDomain(), element, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, el.getElementId());
				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
				}
			}
			super.okPressed();
		}
	}

	private ClassContributionCollector getCollector() {
		Bundle bundle = FrameworkUtil.getBundle(FindImportElementDialog.class);
		BundleContext context = bundle.getBundleContext();
		ServiceReference ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if (ref != null) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}

	private static class ModelResultHandlerImpl implements ModelResultHandler {
		private boolean cancled = false;
		private IObservableList list;

		public ModelResultHandlerImpl(IObservableList list) {
			this.list = list;
		}

		public void result(EObject data) {
			if (!cancled) {
				list.add(data);
			}
		}

	}
}