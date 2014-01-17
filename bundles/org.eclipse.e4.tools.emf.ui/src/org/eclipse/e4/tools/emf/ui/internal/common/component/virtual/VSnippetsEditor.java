package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VSnippetsEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private List<Action> actions = new ArrayList<Action>();
	private EStructuralFeature targetFeature;

	public VSnippetsEditor() {
		super();
		this.targetFeature = UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS;
	}

	@PostConstruct
	void init() {

		actions.add(new Action(Messages.VWindowControlEditor_AddArea, createImageDescriptor(ResourceProvider.IMG_Area_vertical)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.AREA);
			}
		});

		actions.add(new Action(Messages.VWindowEditor_AddDialog, createImageDescriptor(ResourceProvider.IMG_Dialog)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.DIALOG);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPartSashContainer, createImageDescriptor(ResourceProvider.IMG_PartSashContainer_vertical)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPartStack, createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_STACK);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPerspectiveStack, createImageDescriptor(ResourceProvider.IMG_PerspectiveStack)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK);
			}
		});
		actions.add(new Action(Messages.PerspectiveStackEditor_AddPerspective, createImageDescriptor(ResourceProvider.IMG_Perspective)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.PERSPECTIVE);
			}
		});

		actions.add(new Action(Messages.VTrimContributionsEditor_AddTrimContribution, createImageDescriptor(ResourceProvider.IMG_TrimContribution)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.TRIM_ELEMENT);
			}
		});

		actions.add(new Action(Messages.VWindowEditor_AddTrimmedWindow, createImageDescriptor(ResourceProvider.IMG_Window)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.TRIMMED_WINDOW);
			}
		});
		actions.add(new Action(Messages.VWindowEditor_AddWindow, createImageDescriptor(ResourceProvider.IMG_Window)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.WINDOW);
			}
		});
		actions.add(new Action(Messages.VWindowTrimEditor_AddWindowTrim, createImageDescriptor(ResourceProvider.IMG_WindowTrim)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.TRIM_BAR);
			}
		});
		actions.add(new Action(Messages.VWindowEditor_AddWizardDialog, createImageDescriptor(ResourceProvider.IMG_WizardDialog)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.WIZARD_DIALOG);
			}
		});

		Collections.sort(actions, new Comparator<Action>() {
			@Override
			public int compare(Action o1, Action o2) {
				return o1.getText().compareTo(o2.getText());
			}
		});

	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VWindowControlEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VWindowControlEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		VirtualEntry<?> o = (VirtualEntry<?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		viewer = new TableViewer(parent);
		ObservableListContentProvider cp = new ObservableListContentProvider();
		viewer.setContentProvider(cp);
		viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);

		Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);

		Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);

		b.setText(Messages.ModelTooling_Common_Up);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						EObject container = (EObject) getMaster().getValue();
						List<Object> l = (List<Object>) container.eGet(targetFeature);
						int idx = l.indexOf(obj) - 1;
						if (idx >= 0) {
							if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(), idx, targetFeature)) {
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Down);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_down));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						EObject container = (EObject) getMaster().getValue();
						List<Object> l = (List<Object>) container.eGet(targetFeature);
						int idx = l.indexOf(obj) + 1;
						if (idx < l.size()) {
							if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(), idx, targetFeature)) {
								viewer.setSelection(new StructuredSelection(obj));
							}
						}
					}
				}
			}
		});

		final ComboViewer childrenDropDown = new ComboViewer(buttonComp);
		childrenDropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		childrenDropDown.setContentProvider(new ArrayContentProvider());
		childrenDropDown.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				EClass eclass = (EClass) element;
				return eclass.getName();
			}
		});
		childrenDropDown.setInput(new EClass[] { BasicPackageImpl.Literals.TRIMMED_WINDOW, BasicPackageImpl.Literals.WINDOW, AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, AdvancedPackageImpl.Literals.PERSPECTIVE, AdvancedPackageImpl.Literals.AREA, BasicPackageImpl.Literals.PART_SASH_CONTAINER, BasicPackageImpl.Literals.PART_STACK, BasicPackageImpl.Literals.PART, BasicPackageImpl.Literals.INPUT_PART, BasicPackageImpl.Literals.TRIM_BAR, BasicPackageImpl.Literals.TRIM_ELEMENT, });
		childrenDropDown.setSelection(new StructuredSelection(AdvancedPackageImpl.Literals.PERSPECTIVE));

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EClass eClass = (EClass) ((IStructuredSelection) childrenDropDown.getSelection()).getFirstElement();
				handleAdd(eClass);
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Remove);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					List<?> windows = ((IStructuredSelection) viewer.getSelection()).toList();
					MElementContainer<?> container = (MElementContainer<?>) getMaster().getValue();
					Command cmd = RemoveCommand.create(getEditingDomain(), container, targetFeature, windows);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						if (container.getChildren().size() > 0) {
							viewer.setSelection(new StructuredSelection(container.getChildren().get(0)));
						}
					}
				}
			}
		});

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAdd(EClass eClass) {
		EObject handler = EcoreUtil.create(eClass);
		setElementId(handler);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), targetFeature, handler);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}
}