/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import com.ibm.icu.text.Collator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;

import org.eclipse.ltk.internal.ui.refactoring.util.ViewerPane;

import org.eclipse.swt.SWT;
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import org.eclipse.compare.CompareUI;

import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardPage;

/**
 * Presents the changes made by the refactoring.
 * Consists of a tree of changes and a compare viewer that shows the differences. 
 */
public class PreviewWizardPage extends RefactoringWizardPage implements IPreviewWizardPage {

	protected static final String PREVIOUS_CHANGE_ID= "org.eclipse.ltk.ui.refactoring.previousChange"; //$NON-NLS-1$
	protected static final String NEXT_CHANGE_ID= "org.eclipse.ltk.ui.refactoring.nextChange"; //$NON-NLS-1$

	private static class NullPreviewer implements IChangePreviewViewer {
		private Label fLabel;
		public void createControl(Composite parent) {
			fLabel= new Label(parent, SWT.CENTER | SWT.FLAT);
			fLabel.setText(RefactoringUIMessages.PreviewWizardPage_no_preview); 
		}
		public void refresh() {
		}
		public Control getControl() {
			return fLabel;
		}
		public void setInput(ChangePreviewViewerInput input) {
		}
	}
	
	private static class DerivedFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return ! ((PreviewNode) element).hasDerived();
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
		public void run() {
			((ChangeElementTreeViewer) fTreeViewer).revealNext();	
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
		public void run() {
			((ChangeElementTreeViewer) fTreeViewer).revealPrevious();
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

		public void run() {
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

				public void run() {
					setActiveGroupCategory(fGroupCategory);
					fOwner.executed(FilterAction.this);
				}
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

		public void run() {
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

				public final void run() {
					clearGroupCategories();
					fOwner.executed(ShowAllAction.this);
				}
			});
		}
	}
	private class HideDerivedAction extends Action {
		private static final String PREVIEW_WIZARD_PAGE_HIDE_DERIVED= "PreviewWizardPage.hide.derived"; //$NON-NLS-1$
		private final DerivedFilter fDerivedFilter;
		public HideDerivedAction() {
			super(RefactoringUIMessages.PreviewWizardPage_hideDerived_text, IAction.AS_CHECK_BOX);
			fDerivedFilter= new DerivedFilter();
			boolean hideDerived= getRefactoringSettings().getBoolean(PREVIEW_WIZARD_PAGE_HIDE_DERIVED);
			setChecked(hideDerived);
			if (hideDerived) {
				addFilter();
			}
		}
		public void run() {
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public final void run() {
					boolean hideDerived= isChecked();
					getRefactoringSettings().put(PREVIEW_WIZARD_PAGE_HIDE_DERIVED, hideDerived);
					if (hideDerived) {
						addFilter();
					} else {
						removeFilter();
					}
				}
			});
		}
		private void addFilter() {
			fTreeViewer.addFilter(fDerivedFilter);
		}
		private void removeFilter() {
			fTreeViewer.removeFilter(fDerivedFilter);
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
		public void initialize(Collection/*<GroupCategory>*/ groupCategories) {
			List list= new ArrayList(groupCategories);
			Collections.sort(list, new Comparator() {
				private Collator fCollator= Collator.getInstance();
				public final int compare(final Object first, final Object second) {
					final GroupCategory left= (GroupCategory) first;
					final GroupCategory right= (GroupCategory) second;
					return fCollator.compare(left.getName(), right.getName());
				}
			});
			fShowAllAction= new ShowAllAction(this);
			fActiveAction= fShowAllAction;
			fFilterActions= new FilterAction[list.size()];
			int i= 0;
			for (Iterator iter= list.iterator(); iter.hasNext();) {
				fFilterActions[i++]= new FilterAction(this, (GroupCategory)iter.next());
			}
			fHideDerivedAction= new HideDerivedAction();
		}
		public void dispose() {
			if (fMenu != null) {
				fMenu.dispose();
				fMenu= null;				
			}
		}
		public Menu getMenu(Control parent) {
			dispose();
			fMenu= new Menu(parent);
			if (fFilterActions.length != 0) {
				new ActionContributionItem(fShowAllAction).fill(fMenu, -1);
				new MenuItem(fMenu, SWT.SEPARATOR);
				for (int i= 0; i < fFilterActions.length; i++) {
					new ActionContributionItem(fFilterActions[i]).fill(fMenu, -1);
				}
				new MenuItem(fMenu, SWT.SEPARATOR);
			}
			new ActionContributionItem(fHideDerivedAction).fill(fMenu, -1);
			return fMenu;
		}
		public Menu getMenu(Menu parent) {
			return null;
		}
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
		}
	}
	
	protected Change fChange;
	private List/*<GroupCategory>*/ fActiveGroupCategories;
	protected CompositeChange fTreeViewerInputChange;
	private PreviewNode fCurrentSelection;
	private PageBook fPageContainer;
	private Control fStandardPage;
	private Control fNullPage;
	protected Action fFilterDropDownAction;
	protected Action fNextAction;
	protected Action fPreviousAction;
	protected ViewerPane fTreeViewerPane;
	protected CheckboxTreeViewer fTreeViewer;
	private PageBook fPreviewContainer;
	private ChangePreviewViewerDescriptor fCurrentDescriptor;
	private IChangePreviewViewer fCurrentPreviewViewer;
	private IChangePreviewViewer fNullPreviewer;
	
	/**
	 * Creates a new preview wizard page.
	 */
	public PreviewWizardPage() {
		super(PAGE_NAME);
		setDescription(RefactoringUIMessages.PreviewWizardPage_description);
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
		setDescription(RefactoringUIMessages.PreviewWizardPage_description);
	}

	/**
	 * Sets the given change. Setting the change initializes the tree viewer with
	 * the given change.
	 * @param change the new change.
	 */
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
	
	/* (non-JavaDoc)
	 * Method defined in RefactoringWizardPage
	 */
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
	
	/* (non-JavaDoc)
	 * Method defined in IWizardPage
	 */
	public boolean canFlipToNextPage() {
		return false;
	}
	
	/* (Non-JavaDoc)
	 * Method defined in IWizardPage
	 */
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
		fTreeViewerPane.setText(RefactoringUIMessages.PreviewWizardPage_changes); 
		ToolBarManager tbm= fTreeViewerPane.getToolBarManager();
		fNextAction= new NextChange();
		tbm.add(fNextAction);
		fPreviousAction= new PreviousChange();
		tbm.add(fPreviousAction);
		tbm.add(new Separator());
		fFilterDropDownAction= new FilterDropDownAction();
		tbm.add(fFilterDropDownAction);
		
		tbm.update(true);
		
		fTreeViewer= createTreeViewer(fTreeViewerPane);
		fTreeViewer.setContentProvider(createTreeContentProvider());
		fTreeViewer.setLabelProvider(createTreeLabelProvider());
		fTreeViewer.addSelectionChangedListener(createSelectionChangedListener());
		fTreeViewer.addCheckStateListener(createCheckStateListener());
		fTreeViewerPane.setContent(fTreeViewer.getControl());
		setTreeViewerInput();
		
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
	
	/* (Non-JavaDoc)
	 * Method defined in IWizardPage
	 */
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
					PreviewNode element= getFirstNonCompositeChange(provider, treeViewerInput);
					if (element != null) {
						if (refactoringWizard != null && refactoringWizard.internalGetExpandFirstNode(InternalAPI.INSTANCE)) {
							Object[] subElements= provider.getElements(element);
							if (subElements != null && subElements.length > 0) {
								fTreeViewer.expandToLevel(element, 999);
							}
						}
						fTreeViewer.setSelection(new StructuredSelection(element));
					}
				}
			}
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
	
	private PreviewNode getFirstNonCompositeChange(ITreeContentProvider provider, AbstractChangeNode input) {
		PreviewNode focus= input;
		Change change= input.getChange();
		while (change != null && change instanceof CompositeChange) {
			PreviewNode[] children= (PreviewNode[])provider.getElements(focus);
			if (children == null || children.length == 0)
				return null;
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
		}
		fTreeViewer.setInput(input);
	}
	
	private ICheckStateListener createCheckStateListener() {
		return new ICheckStateListener() {
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
		return new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
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
		final Change[] children= change.getChildren();
		for (int index= 0; index < children.length; index++) {
			if (children[index] instanceof CompositeChange) {
				if (hasChanges((CompositeChange) children[index]))
					return true;
			} else
				return true;
		}
		return false;
	}

	//---- manage group categories --------------------------------------------
	
	private Collection/*<GroupCategory>*/ collectGroupCategories() {
		Set/*<GroupCategory>*/ result= new HashSet();
		collectGroupCategories(result, fChange);
		return result;
	}
	
	private void collectGroupCategories(Set/*<GroupCategory>*/ result, Change change) {
		if (change instanceof TextEditBasedChange) {
			TextEditBasedChangeGroup[] groups= ((TextEditBasedChange)change).getChangeGroups();
			for (int i= 0; i < groups.length; i++) {
				result.addAll(groups[i].getGroupCategorySet().asList());
			}
		} else if (change instanceof CompositeChange) {
			Change[] children= ((CompositeChange)change).getChildren();
			for (int i= 0; i < children.length; i++) {
				collectGroupCategories(result, children[i]);
			}
		}
	}
	
	private void setActiveGroupCategory(GroupCategory category) {
		if (fActiveGroupCategories == null) {
			fActiveGroupCategories= new ArrayList(1);
		} else {
			fActiveGroupCategories.clear();
		}
		fActiveGroupCategories.add(category);
		((ChangeElementTreeViewer) fTreeViewer).setGroupCategory(fActiveGroupCategories);
		fTreeViewerPane.setText(Messages.format(
			RefactoringUIMessages.PreviewWizardPage_changes_filtered, 
			category.getName()));
	}
	
	private void clearGroupCategories() {
		fActiveGroupCategories= null;
		((ChangeElementTreeViewer) fTreeViewer).setGroupCategory(null);
		fTreeViewerPane.setText(RefactoringUIMessages.PreviewWizardPage_changes); 
	}

	public Change getChange() {
		return fChange;
	}
}
