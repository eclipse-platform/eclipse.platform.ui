package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

/*
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.EClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class VSnippetsEditor extends AbstractComponentEditor<MSnippetContainer> {

	/** Define the classes available to create snippets */
	public static final EClass[] SNIPPET_CHILDREN = new EClass[] { AdvancedPackageImpl.Literals.AREA, BasicPackageImpl.Literals.PART,
			BasicPackageImpl.Literals.PART_SASH_CONTAINER, BasicPackageImpl.Literals.PART_STACK,
			BasicPackageImpl.Literals.COMPOSITE_PART, AdvancedPackageImpl.Literals.PERSPECTIVE,
			AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, MenuPackageImpl.Literals.TRIM_CONTRIBUTION,
			BasicPackageImpl.Literals.TRIMMED_WINDOW, BasicPackageImpl.Literals.WINDOW,
			BasicPackageImpl.Literals.TRIM_BAR };

	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private final List<Action> actions = new ArrayList<>();
	private final EStructuralFeature targetFeature;

	public VSnippetsEditor() {
		super();
		targetFeature = UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS;
	}

	@PostConstruct
	void init() {

		actions.add(new Action(Messages.VWindowControlEditor_AddArea,
				createImageDescriptor(ResourceProvider.IMG_Area_vertical)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.AREA);
			}
		});


		actions
		.add(new Action(Messages.VWindowControlEditor_AddPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddCompositePart,
				createImageDescriptor(ResourceProvider.IMG_PartSashContainer)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.COMPOSITE_PART);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPartSashContainer,
				createImageDescriptor(ResourceProvider.IMG_PartSashContainer_vertical)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPartStack,
				createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_STACK);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPerspectiveStack,
				createImageDescriptor(ResourceProvider.IMG_PerspectiveStack)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK);
			}
		});
		actions.add(new Action(Messages.PerspectiveStackEditor_AddPerspective,
				createImageDescriptor(ResourceProvider.IMG_Perspective)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.PERSPECTIVE);
			}
		});

		actions.add(new Action(Messages.VTrimContributionsEditor_AddTrimContribution,
				createImageDescriptor(ResourceProvider.IMG_TrimContribution)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.TRIM_CONTRIBUTION);
			}
		});

		actions.add(new Action(Messages.VWindowEditor_AddTrimmedWindow,
				createImageDescriptor(ResourceProvider.IMG_Window)) {
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
		actions.add(new Action(Messages.VWindowTrimEditor_AddTrim,
				createImageDescriptor(ResourceProvider.IMG_WindowTrim)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.TRIM_BAR);
			}
		});

		actions.sort((o1, o2) -> o1.getText().compareTo(o2.getText()));

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
		@SuppressWarnings("unchecked")
		final VirtualEntry<MSnippetContainer, ?> o = (VirtualEntry<MSnippetContainer, ?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MSnippetContainer> master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final AbstractPickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_GROUP),
				this, targetFeature) {
			@Override
			protected void addPressed() {
				final EClass eClass = (EClass) getSelection().getFirstElement();
				handleAdd(eClass);
			}

			@Override
			protected List<?> getContainerChildren(Object container) {
				return ((MApplication) container).getSnippets();
			}
		};
		pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		viewer = pickList.getList();

		pickList.setLabelProvider(new EClassLabelProvider(getEditor()));
		pickList.setInput(SNIPPET_CHILDREN);
		pickList.setSelection(new StructuredSelection(AdvancedPackageImpl.Literals.PERSPECTIVE));

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAdd(EClass eClass) {
		final EObject handler = EcoreUtil.create(eClass);
		setElementId(handler);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), targetFeature, handler);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}
}