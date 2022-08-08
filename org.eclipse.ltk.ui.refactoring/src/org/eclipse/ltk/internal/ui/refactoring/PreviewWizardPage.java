/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import org.eclipse.compare.CompareUI;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.internal.ui.refactoring.util.ViewerPane;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardPage;

/**
 * Presents the changes made by the refactoring.
 * Consists of a tree of changes and a compare viewer that shows the differences.
 */
public class PreviewWizardPage extends RefactoringWizardPage implements IPreviewWizardPage {

	private static final String PREVIEW_WIZARD_PAGE_HIDE_DERIVED= "PreviewWizardPage.hide.derived"; //$NON-NLS-1$
	protected static final String PREVIOUS_CHANGE_ID= "org.eclipse.ltk.ui.refactoring.previousChange"; //$NON-NLS-1$
	protected static final String NEXT_CHANGE_ID= "org.eclipse.ltk.ui.refactoring.nextChange"; //$NON-NLS-1$

	private static class NullPreviewer implements IChangePreviewViewer {
		private Label fLabel;
		@Override
		public void createControl(Composite parent) {
			fLabel= new Label(parent, SWT.CENTER | SWT.FLAT);
			fLabel.setText(RefactoringUIMessages.PreviewWizardPage_no_preview);
		}
		@Override
		public Control getControl() {
			return fLabel;
		}
		@Override
		public void setInput(ChangePreviewViewerInput input) {
		}
	}

	private class NextChange extends Action {
		public NextChange() {
			setId(NEXT_CHANGE_ID);
			setImageDescriptor(CompareUI.DESC_ETOOL_NEXT);
			setDisabledImageDescriptor(CompareUI.DESC_DTOOL_NEXT);
			setHoverImageDescriptor(CompareUI.DESC_CTOOL_NEXT);
			setToolTipText(RefactoringUIMessages.PreviewWizardPage_next_Change);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IRefactoringHelpContextIds.NEXT_CHANGE_ACTION);
		}
		@Override
		public void run() {
			fTreeViewer.revealNext();
		}
	}

	private class PreviousChange extends Action {
		public PreviousChange() {
			setId(PREVIOUS_CHANGE_ID);
			setImageDescriptor(CompareUI.DESC_ETOOL_PREV);
			setDisabledImageDescriptor(CompareUI.DESC_DTOOL_PREV);
			setHoverImageDescriptor(CompareUI.DESC_CTOOL_PREV);
			setToolTipText(RefactoringUIMessages.PreviewWizardPage_previous_Change);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IRefactoringHelpContextIds.PREVIOUS_CHANGE_ACTION);
		}
		@Override
		public void run() {
			fTreeViewer.revealPrevious();
		}
	}

	private class FilterAction extends Action {

		private FilterDropDownAction fOwner;

		private GroupCategory fGroupCategory;

		public FilterAction(FilterDropDownAction owner, GroupCategory category) {
			super(category.getName(), IAction.AS_RADIO_BUTTON);
			setToolTipText(category.getDescription());
			fOwner= owner;
			fGroupCategory= category;
		}

		@Override
		public void run() {
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				setActiveGroupCategory(fGroupCategory);
				fOwner.executed(FilterAction.this);
			});
		}
	}

	private class ShowAllAction extends Action {

		private FilterDropDownAction fOwner;

		public ShowAllAction(FilterDropDownAction owner) {
			super(RefactoringUIMessages.PreviewWizardPage_showAll_text, IAction.AS_RADIO_BUTTON);
			super.setToolTipText(RefactoringUIMessages.PreviewWizardPage_showAll_description);
			fOwner= owner;
			setChecked(true);
		}

		@Override
		public void run() {
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				clearGroupCategories();
				fOwner.executed(ShowAllAction.this);
			});
		}
	}
	private class HideDerivedAction extends Action {
		public HideDerivedAction() {
			super(RefactoringUIMessages.PreviewWizardPage_hideDerived_text, IAction.AS_CHECK_BOX);
			setChecked(fDerivedFilterActive);
		}
		@Override
		public void run() {
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				boolean hideDerived= isChecked();
				getRefactoringSettings().put(PREVIEW_WIZARD_PAGE_HIDE_DERIVED, hideDerived);
				setHideDerived(hideDerived);
			});
		}
	}
	private class FilterDropDownAction extends Action implements IMenuCreator {
		private Menu fMenu;
		private ShowAllAction fShowAllAction;
		private FilterAction[] fFilterActions;
		private Action fActiveAction;
		private HideDerivedAction fHideDerivedAction;

		public FilterDropDownAction() {
			setImageDescriptor(RefactoringPluginImages.DESC_ELCL_FILTER);
			setDisabledImageDescriptor(RefactoringPluginImages.DESC_DLCL_FILTER);
			setText(RefactoringUIMessages.PreviewWizardPage_filterChanges);
			setToolTipText(RefactoringUIMessages.PreviewWizardPage_filterChanges);
			setMenuCreator(this);
		}
		public void initialize(Collection<GroupCategory> groupCategories) {
			List<GroupCategory> list= new ArrayList<>(groupCategories);
			Collections.sort(list, new Comparator<GroupCategory>() {
				private Collator fCollator= Collator.getInstance();
				@Override
				public final int compare(GroupCategory first, GroupCategory second) {
					return fCollator.compare(first.getName(), second.getName());
				}
			});
			fShowAllAction= new ShowAllAction(this);
			fActiveAction= fShowAllAction;
			fFilterActions= new FilterAction[list.size()];
			int i= 0;
			for (GroupCategory groupCategory : list) {
				fFilterActions[i++]= new FilterAction(this, groupCategory);
			}
			fHideDerivedAction= new HideDerivedAction();
		}
		@Override
		public void dispose() {
			if (fMenu != null) {
				fMenu.dispose();
				fMenu= null;
			}
		}
		@Override
		public Menu getMenu(Control parent) {
			dispose();
			fMenu= new Menu(parent);
			if (fFilterActions.length != 0) {
				new ActionContributionItem(fShowAllAction).fill(fMenu, -1);
				for (FilterAction fFilterAction : fFilterActions) {
					new ActionContributionItem(fFilterAction).fill(fMenu, -1);
				}
				new MenuItem(fMenu, SWT.SEPARATOR);
			}
			new ActionContributionItem(fHideDerivedAction).fill(fMenu, -1);
			return fMenu;
		}
		@Override
		public Menu getMenu(Menu parent) {
			return null;
		}
		@Override
		public void runWithEvent(Event event) {
			ToolItem toolItem= (ToolItem) event.widget;
			ToolBar toolBar= toolItem.getParent();
			Menu menu= getMenu(toolBar);
			Rectangle toolItemBounds= toolItem.getBounds();
			Point location= toolBar.toDisplay(toolItemBounds.x, toolItemBounds.y + toolItemBounds.height);
			menu.setLocation(location);
			menu.setVisible(true);
		}
		public void executed(Action action) {
			if (fActiveAction == action)
				return;
			fActiveAction.setChecked(false);
			fActiveAction= action;
			fActiveAction.setChecked(true);
			if (fCurrentSelection != null)
				showPreview(fCurrentSelection);
		}
	}

	protected Change fChange;
	private List<GroupCategory> fActiveGroupCategories;
	private boolean fDerivedFilterActive;
	protected CompositeChange fTreeViewerInputChange;
	private PreviewNode fCurrentSelection;
	private PageBook fPageContainer;
	private Control fStandardPage;
	private Control fNullPage;
	protected Action fFilterDropDownAction;
	protected Action fNextAction;
	protected Action fPreviousAction;
	protected ViewerPane fTreeViewerPane;
	protected ChangeElementTreeViewer fTreeViewer;
	private PageBook fPreviewContainer;
	private ChangePreviewViewerDescriptor fCurrentDescriptor;
	private IChangePreviewViewer fCurrentPreviewViewer;
	private IChangePreviewViewer fNullPreviewer;

	/**
	 * Creates a new preview wizard page.
	 */
	public PreviewWizardPage() {
		this(false);
	}

	/**
	 * Creates a new preview wizard page.
	 *
	 * @param wizard
	 *            <code>true</code> if the page belongs to a conventional
	 *            wizard, <code>false</code> otherwise
	 */
	public PreviewWizardPage(boolean wizard) {
		super(PAGE_NAME, wizard);
		setDescription(RefactoringUIMessages.PreviewWizardPage_description_z);
	}

	/**
	 * Sets the given change. Setting the change initializes the tree viewer with
	 * the given change.
	 * @param change the new change.
	 */
	@Override
	public void setChange(Change change) {
		if (fChange == change)
			return;

		fChange= change;
		if (fChange instanceof CompositeChange) {
			fTreeViewerInputChange= (CompositeChange)fChange;
		} else {
			fTreeViewerInputChange= new CompositeChange("Dummy Change"); //$NON-NLS-1$
			fTreeViewerInputChange.add(fChange);
		}
		setTreeViewerInput();
	}

	/**
	 * Creates the tree viewer to present the hierarchy of changes. Subclasses may override
	 * to create their own custom tree viewer.
	 *
	 * @param parent the tree viewer's parent
	 *
	 * @return the tree viewer to present the hierarchy of changes
	 */
	protected ChangeElementTreeViewer createTreeViewer(Composite parent) {
		return new ChangeElementTreeViewer(parent);
	}

	/**
	 * Creates the content provider used to fill the tree of changes. Subclasses may override
	 * to create their own custom tree content provider.
	 *
	 * @return the tree content provider used to fill the tree of changes
	 */
	protected ITreeContentProvider createTreeContentProvider() {
		return new ChangeElementContentProvider();
	}

	/**
	 * Creates the label provider used to render the tree of changes. Subclasses may override
	 * to create their own custom label provider.
	 *
	 * @return the label provider used to render the tree of changes
	 */
	protected ILabelProvider createTreeLabelProvider() {
		// return new ChangeElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_SMALL_ICONS);
		return new ChangeElementLabelProvider();
	}

	protected ViewerComparator createTreeComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				PreviewNode node1= (PreviewNode) e1;
				PreviewNode node2= (PreviewNode) e2;
				if (node1.hasDerived()) {
					if (node2.hasDerived()) {
						return 0;
					} else {
						return +1;
					}
				} else {
					if (node2.hasDerived()) {
						return -1;
					} else {
						return 0;
					}
				}
			}
		};
	}

	@Override
	protected boolean performFinish() {
		UIPerformChangeOperation operation= new UIPerformChangeOperation(getShell().getDisplay(), fChange, getContainer());
		FinishResult result= getRefactoringWizard().internalPerformFinish(InternalAPI.INSTANCE, operation);
		if (result.isException())
			return true;
		if (result.isInterrupted())
			return false;
		RefactoringStatus fValidationStatus= operation.getValidationStatus();
		if (fValidationStatus != null && fValidationStatus.hasFatalError()) {
			RefactoringWizard wizard= getRefactoringWizard();
			MessageDialog.openError(wizard.getShell(), wizard.getWindowTitle(),
				Messages.format(
					RefactoringUIMessages.RefactoringUI_cannot_execute,
					fValidationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
			return true;
		}
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		fPageContainer= new PageBook(parent, SWT.NONE);
		fStandardPage= createStandardPreviewPage(fPageContainer);
		fNullPage= createNullPage(fPageContainer);
		setControl(fPageContainer);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IRefactoringHelpContextIds.REFACTORING_PREVIEW_WIZARD_PAGE);
	}

	private Composite createStandardPreviewPage(Composite parent) {
		// XXX The composite is needed to limit the width of the SashForm. See http://bugs.eclipse.org/bugs/show_bug.cgi?id=6854
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0; layout.marginWidth= 0;
		result.setLayout(layout);

		SashForm sashForm= new SashForm(result, SWT.VERTICAL);

		fTreeViewerPane= new ViewerPane(sashForm, SWT.BORDER | SWT.FLAT);
		ToolBarManager tbm= fTreeViewerPane.getToolBarManager();
		fNextAction= new NextChange();
		tbm.add(fNextAction);
		fPreviousAction= new PreviousChange();
		tbm.add(fPreviousAction);
		tbm.add(new Separator());
		final IDialogSettings settings= getRefactoringSettings();
		if (settings != null)
			fDerivedFilterActive= settings.getBoolean(PREVIEW_WIZARD_PAGE_HIDE_DERIVED);
		fFilterDropDownAction= new FilterDropDownAction();
		tbm.add(fFilterDropDownAction);
		tbm.update(true);

		final ToolBar toolBar= tbm.getControl();

		// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=375354 :
		toolBar.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			/*
			 * @see org.eclipse.swt.accessibility.AccessibleAdapter#getName(org.eclipse.swt.accessibility.AccessibleEvent)
			 */
			@Override
			public void getName(AccessibleEvent e) {
				if (e.childID != ACC.CHILDID_SELF)
					e.result= toolBar.getItem(e.childID).getToolTipText();
			}
		});

		fTreeViewer= createTreeViewer(fTreeViewerPane);
		fTreeViewer.setContentProvider(createTreeContentProvider());
		fTreeViewer.setLabelProvider(createTreeLabelProvider());
		fTreeViewer.setComparator(createTreeComparator());
		fTreeViewer.addSelectionChangedListener(createSelectionChangedListener());
		fTreeViewer.addCheckStateListener(createCheckStateListener());
		fTreeViewerPane.setContent(fTreeViewer.getControl());
		fTreeViewer.getControl().getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				super.getName(e);
				e.result= fTreeViewerPane.getText() + (e.result != null ? (" " + e.result) : ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		setHideDerived(fDerivedFilterActive);
		setTreeViewerInput();
		updateTreeViewerPaneTitle();

		fPreviewContainer= new PageBook(sashForm, SWT.NONE);
		fNullPreviewer= new NullPreviewer();
		fNullPreviewer.createControl(fPreviewContainer);
		fPreviewContainer.showPage(fNullPreviewer.getControl());
		fCurrentPreviewViewer= fNullPreviewer;
		fCurrentDescriptor= null;

		sashForm.setWeights(new int[]{33, 67});
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		sashForm.setLayoutData(gd);
		Dialog.applyDialogFont(result);
		return result;
	}

	private Control createNullPage(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		result.setLayout(layout);
		Label label= new Label(result, SWT.CENTER);
		label.setText(RefactoringUIMessages.PreviewWizardPage_no_source_code_change);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Dialog.applyDialogFont(result);
		return result;
	}

	@Override
	public void setVisible(boolean visible) {
		fCurrentSelection= null;
		final RefactoringWizard refactoringWizard= getRefactoringWizard();
		if (hasChanges()) {
			fPageContainer.showPage(fStandardPage);
			AbstractChangeNode treeViewerInput= (AbstractChangeNode)fTreeViewer.getInput();
			if (visible && treeViewerInput != null) {
				IStructuredSelection selection= (IStructuredSelection)fTreeViewer.getSelection();
				if (selection.isEmpty()) {
					ITreeContentProvider provider= (ITreeContentProvider)fTreeViewer.getContentProvider();
					ViewerComparator comparator= fTreeViewer.getComparator();
					PreviewNode element= getFirstNonCompositeChange(provider, comparator, treeViewerInput);
					if (element != null) {
						if (refactoringWizard != null && refactoringWizard.internalGetExpandFirstNode(InternalAPI.INSTANCE)) {
							Object[] subElements= provider.getElements(element);
							if (subElements != null && subElements.length > 0) {
								comparator.sort(fTreeViewer, subElements);
								fTreeViewer.expandToLevel(element, 999);
							}
						}
						fTreeViewer.setSelection(new StructuredSelection(element));
					}
				}
			} else if (!visible) // dispose the previewer
				fCurrentPreviewViewer.setInput(new ChangePreviewViewerInput(new NullChange()));
			((FilterDropDownAction) fFilterDropDownAction).initialize(collectGroupCategories());
			super.setVisible(visible);
			fTreeViewer.getControl().setFocus();
		} else {
			fPageContainer.showPage(fNullPage);
			super.setVisible(visible);
		}
		if (refactoringWizard != null)
			refactoringWizard.internalSetPreviewShown(InternalAPI.INSTANCE, visible);
	}

	private PreviewNode getFirstNonCompositeChange(ITreeContentProvider provider, ViewerComparator comparator, AbstractChangeNode input) {
		PreviewNode focus= input;
		Change change= input.getChange();
		while (change instanceof CompositeChange) {
			PreviewNode[] children= (PreviewNode[])provider.getElements(focus);
			if (children == null || children.length == 0)
				return null;
			comparator.sort(fTreeViewer, children);
			focus= children[0];
			change= (focus instanceof AbstractChangeNode)
				? ((AbstractChangeNode)focus).getChange()
				: null;
		}
		return focus;
	}

	protected void setTreeViewerInput() {
		if (fTreeViewer == null)
			return;
		PreviewNode input= null;
		if (fTreeViewerInputChange != null) {
			input= AbstractChangeNode.createNode(null, fTreeViewerInputChange);
			int filenumber= fTreeViewerInputChange.getFilenumber();
			String fullDescription= RefactoringUIMessages.PreviewWizardPage_description_z;
			if (filenumber == 1) {
				fullDescription= RefactoringUIMessages.PreviewWizardPage_description_s;
			} else if (filenumber > 1) {
				fullDescription= MessageFormat.format(RefactoringUIMessages.PreviewWizardPage_description_m, String.valueOf(filenumber));
			}
			setDescription(fullDescription);
		}
		fTreeViewer.setInput(input);
	}

	private ICheckStateListener createCheckStateListener() {
		return new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event){
				PreviewNode element= (PreviewNode)event.getElement();
				if (isChild(fCurrentSelection, element) || isChild(element, fCurrentSelection)) {
					showPreview(fCurrentSelection);
				}
			}
			private boolean isChild(PreviewNode element, PreviewNode child) {
				while (child != null) {
					if (child == element)
						return true;
					child= child.getParent();
				}
				return false;
			}
		};
	}

	private ISelectionChangedListener createSelectionChangedListener() {
		return event -> {
			IStructuredSelection sel= (IStructuredSelection) event.getSelection();
			if (sel.size() == 1) {
				PreviewNode newSelection= (PreviewNode)sel.getFirstElement();
				if (newSelection != fCurrentSelection) {
					fCurrentSelection= newSelection;
					showPreview(newSelection);
				}
			} else {
				showPreview(null);
			}
		};
	}

	private void showPreview(PreviewNode element) {
		try {
			if (element == null) {
				showNullPreviewer();
			} else {
				ChangePreviewViewerDescriptor descriptor= element.getChangePreviewViewerDescriptor();
				if (fCurrentDescriptor != descriptor) {
					IChangePreviewViewer newViewer;
					if (descriptor != null) {
						newViewer= descriptor.createViewer();
						newViewer.createControl(fPreviewContainer);
					} else {
						newViewer= fNullPreviewer;
					}
					fCurrentDescriptor= descriptor;
					element.feedInput(newViewer, fActiveGroupCategories);
					if (fCurrentPreviewViewer != null && fCurrentPreviewViewer != fNullPreviewer)
						fCurrentPreviewViewer.getControl().dispose();
					fCurrentPreviewViewer= newViewer;
					fPreviewContainer.showPage(fCurrentPreviewViewer.getControl());
				} else {
					element.feedInput(fCurrentPreviewViewer, fActiveGroupCategories);
				}
			}
		} catch (CoreException e) {
			showNullPreviewer();
			ExceptionHandler.handle(e, getShell(),
						RefactoringUIMessages.PreviewWizardPage_refactoring,
						RefactoringUIMessages.PreviewWizardPage_Internal_error);
		}
	}

	private void showNullPreviewer() {
		fCurrentDescriptor= null;
		fCurrentPreviewViewer= fNullPreviewer;
		fPreviewContainer.showPage(fCurrentPreviewViewer.getControl());
	}

	/**
	 * Returns <code>true</code> if the preview page will show any changes when
	 * it becomes visible. Otherwise <code>false</code> is returned.
	 *
	 * @return whether the preview has changes or not
	 */
	public boolean hasChanges() {
		if (fChange == null)
			return false;
		if (fChange instanceof CompositeChange)
			return hasChanges((CompositeChange) fChange);
		return true;
	}

	private boolean hasChanges(CompositeChange change) {
		for (Change child : change.getChildren()) {
			if (child instanceof CompositeChange) {
				if (hasChanges((CompositeChange) child)) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	//---- manage group categories --------------------------------------------

	private Collection<GroupCategory> collectGroupCategories() {
		Set<GroupCategory> result= new HashSet<>();
		collectGroupCategories(result, fChange);
		return result;
	}

	private void collectGroupCategories(Set<GroupCategory> result, Change change) {
		if (change instanceof TextEditBasedChange) {
			for (TextEditBasedChangeGroup group : ((TextEditBasedChange)change).getChangeGroups()) {
				result.addAll(group.getGroupCategorySet().asList());
			}
		} else if (change instanceof CompositeChange) {
			for (Change child : ((CompositeChange)change).getChildren()) {
				collectGroupCategories(result, child);
			}
		}
	}

	private void setActiveGroupCategory(GroupCategory category) {
		if (fActiveGroupCategories == null) {
			fActiveGroupCategories= new ArrayList<>(1);
		} else {
			fActiveGroupCategories.clear();
		}
		fActiveGroupCategories.add(category);
		fTreeViewer.setGroupCategory(fActiveGroupCategories);
		updateTreeViewerPaneTitle();
	}

	private void updateTreeViewerPaneTitle() {
		String derivedMessage= null;
		String groupFilterMessage= null;

		if (fDerivedFilterActive) {
			if (fTreeViewer != null && fTreeViewer.getInput() instanceof PreviewNode) {
				if (((PreviewNode) fTreeViewer.getInput()).hasDerived()) {
					derivedMessage= RefactoringUIMessages.PreviewWizardPage_changes_filter_derived;
				}
			}
		}
		if (fActiveGroupCategories != null && fActiveGroupCategories.size() > 0) {
			GroupCategory groupCategory= fActiveGroupCategories.get(0);
			groupFilterMessage= Messages.format(RefactoringUIMessages.PreviewWizardPage_changes_filter_category, groupCategory.getName());
		}

		String title;
		if (groupFilterMessage == null && derivedMessage == null) {
			title= RefactoringUIMessages.PreviewWizardPage_changes;

		} else if (groupFilterMessage != null && derivedMessage != null) {
			title= Messages.format(
					RefactoringUIMessages.PreviewWizardPage_changes_filtered2,
					new Object[] { groupFilterMessage, derivedMessage });

		} else if (groupFilterMessage != null) {
			title= Messages.format(
					RefactoringUIMessages.PreviewWizardPage_changes_filtered,
					groupFilterMessage);
		} else {
			title= Messages.format(
					RefactoringUIMessages.PreviewWizardPage_changes_filtered,
					derivedMessage);
		}

		fTreeViewerPane.setText(title);
	}

	private void clearGroupCategories() {
		fActiveGroupCategories= null;
		fTreeViewer.setGroupCategory(null);
		updateTreeViewerPaneTitle();
	}

	private void setHideDerived(boolean hide) {
		fDerivedFilterActive= hide;
		fTreeViewer.setHideDerived(hide);
		updateTreeViewerPaneTitle();
	}

	@Override
	public Change getChange() {
		return fChange;
	}
}
