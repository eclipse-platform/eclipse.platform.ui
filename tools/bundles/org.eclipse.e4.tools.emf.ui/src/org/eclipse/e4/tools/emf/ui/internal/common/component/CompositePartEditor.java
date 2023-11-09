package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.FeatureClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.imp.ModelImportWizard;
import org.eclipse.e4.tools.emf.ui.internal.imp.RegistryUtil;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class CompositePartEditor extends AbstractPartEditor<MCompositePart> {
	private final List<Action> actions = new ArrayList<>();
	private final List<Action> actionsImport = new ArrayList<>();

	@Inject
	private Shell shell;

	@PostConstruct
	protected void init() {
		actions.add(new Action(Messages.CompositePartEditor_AddPartSashContainer,
				createImageDescriptor(ResourceProvider.IMG_PartSashContainer)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});
		actions.add(new Action(Messages.CompositePartEditor_AddPartStack,
				createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART_STACK);
			}
		});
		actions.add(new Action(Messages.CompositePartEditor_AddPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART);
			}
		});

		actions.add(new Action(Messages.CompositePartEditor_AddArea, createImageDescriptor(ResourceProvider.IMG_Area)) {
			@Override
			public void run() {
				handleAddChild(AdvancedPackageImpl.Literals.AREA);
			}
		});
		actions.add(new Action(Messages.CompositePartEditor_AddPlaceholder,
				createImageDescriptor(ResourceProvider.IMG_Placeholder)) {
			@Override
			public void run() {
				handleAddChild(AdvancedPackageImpl.Literals.PLACEHOLDER);
			}
		});
		for (final FeatureClass c : getEditor().getFeatureClasses(BasicPackageImpl.Literals.PART_STACK,
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN)) {
			final EClass ec = c.eClass;
			actions.add(new Action(c.label, createImageDescriptor(c.iconId)) {
				@Override
				public void run() {
					handleAddChild(ec);
				}
			});
		}

		// --- Import Actions ---
		actionsImport.add(new Action("Views", createImageDescriptor(ResourceProvider.IMG_Part)) { //$NON-NLS-1$
			@Override
			public void run() {
				handleImportChild(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_VIEW);
			}
		});
		actionsImport.add(new Action("Editors", createImageDescriptor(ResourceProvider.IMG_Part)) { //$NON-NLS-1$
			@Override
			public void run() {
				handleImportChild(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_EDITOR);
			}
		});

	}

	@Override
	public Image getImage(Object element) {
		final boolean horizontal = ((MCompositePart) element).isHorizontal();

		return horizontal ? getImage(element, ResourceProvider.IMG_PartSashContainer)
				: getImage(element, ResourceProvider.IMG_PartSashContainer_vertical);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.CompositePartEditor_Label;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.GENERIC_TILE__HORIZONTAL),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	@Override
	public String getDescription(Object element) {
		return Messages.CompositePartEditor_Description;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		Composite composite = super.doGetEditor(parent, object);
		getMaster().setValue((MCompositePart) object);
		return composite;
	}

	@Override
	protected void createSubformElements(Composite parent, EMFDataBindingContext context,
			IObservableValue<MCompositePart> master) {

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CompositePartEditor_Orientation);
			l.setLayoutData(new GridData());

			final ComboViewer viewer = new ComboViewer(parent);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			viewer.getControl().setLayoutData(gd);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((Boolean) element).booleanValue() ? Messages.CompositePartEditor_Horizontal
							: Messages.CompositePartEditor_Vertical;
				}
			});
			viewer.setInput(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
			final IViewerValueProperty<ComboViewer, Boolean> vProp = ViewerProperties.singleSelection(Boolean.class);
			context.bindValue(vProp.observe(viewer),
					E4Properties.horizontal(getEditingDomain()).observeDetail(getMaster()));
		}

		ControlFactory.createSelectedElement(parent, this, context, Messages.CompositePartEditor_SelectedElement);

		final Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.CompositePartEditor_Controls);
		l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		final Composite buttonCompTop = new Composite(parent, SWT.NONE);
		final GridData span2 = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1);
		buttonCompTop.setLayoutData(span2);
		buttonCompTop.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		final ComboViewer childrenDropDown = new ComboViewer(buttonCompTop);
		childrenDropDown.setLabelProvider(new FeatureClassLabelProvider(getEditor()));
		childrenDropDown.setContentProvider(ArrayContentProvider.getInstance());

		final List<FeatureClass> eClassList = new ArrayList<>();
		eClassList.add(new FeatureClass("PartSashContainer", BasicPackageImpl.Literals.PART_SASH_CONTAINER)); //$NON-NLS-1$
		eClassList.add(new FeatureClass("PartStack", BasicPackageImpl.Literals.PART_STACK)); //$NON-NLS-1$
		eClassList.add(new FeatureClass("Part", BasicPackageImpl.Literals.PART)); //$NON-NLS-1$
		eClassList.add(new FeatureClass("Area", AdvancedPackageImpl.Literals.AREA)); //$NON-NLS-1$
		eClassList.add(new FeatureClass("Placeholder", AdvancedPackageImpl.Literals.PLACEHOLDER)); //$NON-NLS-1$
		eClassList.addAll(getEditor().getFeatureClasses(BasicPackageImpl.Literals.COMPOSITE_PART,
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN));
		childrenDropDown.setInput(eClassList);
		childrenDropDown.setSelection(new StructuredSelection(eClassList.get(0)));

		Button b = new Button(buttonCompTop, SWT.PUSH | SWT.FLAT);
		b.setText(org.eclipse.e4.tools.emf.ui.internal.Messages.ModelTooling_Common_AddEllipsis);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!childrenDropDown.getSelection().isEmpty()) {
					final EClass eClass = ((FeatureClass) ((IStructuredSelection) childrenDropDown.getSelection())
							.getFirstElement()).eClass;
					handleAddChild(eClass);
				}
			}
		});

		new Label(parent, SWT.NONE);

		final TableViewer viewer = new TableViewer(parent);
		final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ObservableListContentProvider<>());

		final FontDescriptor italicFontDescriptor = FontDescriptor.createFrom(viewer.getControl().getFont())
				.setStyle(SWT.ITALIC);
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ComponentLabelProvider(getEditor(), Messages, italicFontDescriptor)));

		viewer.setInput(E4Properties.<MPartSashContainerElement>children().observeDetail(getMaster()));

		viewer.addOpenListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof EObject && getEditor() != null) {
					EObject selected = (EObject) selection.getFirstElement();
					getEditor().gotoEObject(ModelEditor.TAB_FORM, selected);
				}
			}
		});

		new Label(parent, SWT.NONE);

		final Composite buttonCompBot = new Composite(parent, SWT.NONE);
		buttonCompBot.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false, 2, 1));
		buttonCompBot.setLayout(new FillLayout());

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(org.eclipse.e4.tools.emf.ui.internal.Messages.ModelTooling_Common_Up);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						final Object obj = s.getFirstElement();
						final int idx = getMaster().getValue().getChildren().indexOf(obj) - 1;
						if (idx >= 0) {
							if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(),
									idx)) {
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(org.eclipse.e4.tools.emf.ui.internal.Messages.ModelTooling_Common_Down);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_down));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						final Object obj = s.getFirstElement();
						final int idx = getMaster().getValue().getChildren().indexOf(obj) + 1;
						if (idx < getMaster().getValue().getChildren().size()) {
							if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(),
									idx)) {
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(org.eclipse.e4.tools.emf.ui.internal.Messages.ModelTooling_Common_Remove);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					final List<?> elements = ((IStructuredSelection) viewer.getSelection()).toList();

					final Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(),
							UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, elements);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addChildListEntries(MPart part, IObservableList<Object> list) {
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_CONTROLS, E4Properties.<MPartSashContainerElement>children(),
				(MElementContainer<MPartSashContainerElement>) part, Messages.PartEditor_Controls));
	}

	protected void handleAddChild(EClass eClass) {
		final EObject eObject = EcoreUtil.create(eClass);
		addToModel(eObject);
	}

	protected void handleImportChild(EClass eClass, String hint) {

		if (eClass == BasicPackageImpl.Literals.PART) {
			final ModelImportWizard wizard = new ModelImportWizard(MPart.class, this, hint, resourcePool);
			final WizardDialog wizardDialog = new WizardDialog(shell, wizard);
			if (wizardDialog.open() == Window.OK) {
				final MPart[] parts = (MPart[]) wizard.getElements(MPart.class);
				for (final MPart part : parts) {
					addToModel((EObject) part);
				}
			}
		}
	}

	private void addToModel(EObject eObject) {
		setElementId(eObject);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	@Override
	public List<Action> getActionsImport(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActionsImport(element));
		l.addAll(actionsImport);
		return l;
	}
}
