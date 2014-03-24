/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.progress.UIJob;

import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.Splitter;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.history.IRefactoringHistoryControl;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryContentProvider;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryLabelProvider;

/**
 * Control which is capable of displaying refactoring histories.
 *
 * @since 3.2
 */
public class RefactoringHistoryControl extends Composite implements IRefactoringHistoryControl {

	/** Checkbox tree viewer for the refactoring history */
	protected final class RefactoringHistoryTreeViewer extends CheckboxTreeViewer {

		/**
		 * Creates a new refactoring history tree viewer.
		 *
		 * @param parent
		 *            the parent control
		 * @param style
		 *            the style
		 */
		public RefactoringHistoryTreeViewer(final Composite parent, final int style) {
			super(parent, style);
			addCheckStateListener(new ICheckStateListener() {

				/**
				 * {@inheritDoc}
				 */
				public final void checkStateChanged(final CheckStateChangedEvent event) {
					reconcileCheckState(event.getElement(), event.getChecked());
					handleCheckStateChanged();
				}
			});
			addTreeListener(new ITreeViewerListener() {

				/**
				 * {@inheritDoc}
				 */
				public final void treeCollapsed(final TreeExpansionEvent event) {
					// Do nothing
				}

				/**
				 * {@inheritDoc}
				 */
				public final void treeExpanded(final TreeExpansionEvent event) {
					BusyIndicator.showWhile(getDisplay(), new Runnable() {

						public final void run() {
							final Object element= event.getElement();
							if (getGrayed(element)) {
								final RefactoringHistory history= RefactoringHistoryControl.this.getInput();
								if (history != null)
									reconcileCheckState(history);
							} else if (getChecked(element)) {
								setSubTreeGrayed(element, false);
								setSubtreeChecked(element, true);
							}
						}
					});
				}
			});
		}

		/**
		 * Returns the children of the specified element.
		 *
		 * @param element
		 *            the element
		 * @return the children of the element
		 */
		private Object[] getChildren(final Object element) {
			return ((RefactoringHistoryContentProvider) getContentProvider()).getChildren(element);
		}

		/**
		 * Reconciles the check state of the specified element and dependencies.
		 *
		 * @param element
		 *            the changed element
		 */
		private void reconcileCheckState(final Object element) {
			final Object[] children= getChildren(element);
			for (int index= 0; index < children.length; index++) {
				reconcileCheckState(children[index]);
			}
			int checkCount= 0;
			final Collection collection= getCoveredDescriptors(element);
			for (final Iterator iterator= collection.iterator(); iterator.hasNext();) {
				final RefactoringDescriptorProxy proxy= (RefactoringDescriptorProxy) iterator.next();
				if (fCheckedDescriptors.contains(proxy))
					checkCount++;
			}
			setElementChecked(element, checkCount > 0);
			setElementGrayed(element, checkCount != 0 && checkCount != collection.size());
		}

		/**
		 * Reconciles the check state of the specified element and dependencies.
		 *
		 * @param element
		 *            the changed element
		 * @param checked
		 *            <code>true</code> if the element is checked,
		 *            <code>false</code> otherwise
		 */
		private void reconcileCheckState(final Object element, final boolean checked) {
			if (element instanceof RefactoringHistoryEntry) {
				final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) element;
				final RefactoringDescriptorProxy proxy= entry.getDescriptor();
				if (checked)
					fCheckedDescriptors.add(proxy);
				else
					fCheckedDescriptors.remove(proxy);
			} else if (element instanceof RefactoringHistoryNode) {
				final Collection collection= getCoveredDescriptors(element);
				if (checked)
					fCheckedDescriptors.addAll(collection);
				else
					fCheckedDescriptors.removeAll(collection);
			}
			RefactoringHistoryControl.this.reconcileCheckState();
		}

		/**
		 * Determines whether the specified element is checked.
		 *
		 * @param element
		 *            the element
		 * @param checked
		 *            <code>true</code> to render it checked,
		 *            <code>false</code> otherwise
		 */
		private void setElementChecked(final Object element, final boolean checked) {
			final Widget widget= RefactoringHistoryTreeViewer.this.findItem(element);
			if (widget instanceof TreeItem) {
				final TreeItem item= (TreeItem) widget;
				item.setChecked(checked);
			}
		}

		/**
		 * Determines whether the specified element is grayed.
		 *
		 * @param element
		 *            the element
		 * @param grayed
		 *            <code>true</code> to render it grayed,
		 *            <code>false</code> otherwise
		 */
		private void setElementGrayed(final Object element, final boolean grayed) {
			final Widget widget= RefactoringHistoryTreeViewer.this.findItem(element);
			if (widget instanceof TreeItem) {
				final TreeItem item= (TreeItem) widget;
				item.setGrayed(grayed);
			}
		}

		/**
		 * Determines whether the subtree of the specified element is rendered
		 * grayed.
		 *
		 * @param element
		 *            the element specifying the subtree
		 * @param grayed
		 *            <code>true</code> to render the subtree grayed,
		 *            <code>false</code> otherwise
		 */
		private void setSubTreeGrayed(final Object element, final boolean grayed) {
			setElementGrayed(element, grayed);
			final Object[] children= getChildren(element);
			for (int index= 0; index < children.length; index++) {
				setSubTreeGrayed(children[index], grayed);
			}
		}
	}

	/**
	 * The checked refactoring descriptors (element type:
	 * <code>RefactoringDescriptorProxy</code>)
	 */
	private final Set fCheckedDescriptors= new HashSet();

	/** The refactoring history control configuration to use */
	protected final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The detail field */
	private Text fDetailField= null;

	/** The detail label */
	private Label fDetailLabel= null;

	/** The history pane */
	protected CompareViewerPane fHistoryPane= null;

	/** The history viewer */
	protected TreeViewer fHistoryViewer= null;

	/**
	 * The selected refactoring descriptors (element type:
	 * <code>RefactoringDescriptorProxy</code>)
	 */
	private final Set fSelectedDescriptors= new HashSet();

	/** The selection label, or <code>null</code> */
	private Label fSelectionLabel= null;

	/** The splitter control */
	private Splitter fSplitterControl= null;

	/**
	 * Creates a new refactoring history control.
	 *
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public RefactoringHistoryControl(final Composite parent, final RefactoringHistoryControlConfiguration configuration) {
		super(parent, SWT.NONE);
		Assert.isNotNull(configuration);
		fControlConfiguration= configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addCheckStateListener(final ICheckStateListener listener) {
		Assert.isNotNull(listener);
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer) {
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			viewer.addCheckStateListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addSelectionChangedListener(final ISelectionChangedListener listener) {
		Assert.isNotNull(listener);
		if (fHistoryViewer != null)
			fHistoryViewer.addSelectionChangedListener(listener);
	}

	/**
	 * Creates the button bar at the bottom of the component.
	 *
	 * @param parent
	 *            the parent composite
	 */
	protected void createBottomButtonBar(final Composite parent) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl() {
		RefactoringCore.getHistoryService().connect();
		GridLayout layout= new GridLayout(getContainerColumns(), false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		fSplitterControl= new Splitter(this, SWT.VERTICAL);
		fSplitterControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		createRightButtonBar(this);
		final Composite leftPane= new Composite(fSplitterControl, SWT.NONE);
		layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 2;
		layout.verticalSpacing= 2;
		leftPane.setLayout(layout);
		fHistoryPane= new CompareViewerPane(leftPane, SWT.BORDER | SWT.FLAT);
		fHistoryPane.setText(getHistoryPaneText());
		fHistoryPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		fHistoryViewer= createHistoryViewer(fHistoryPane);
		if (!fControlConfiguration.isTimeDisplayed())
			fHistoryViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		fHistoryViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		fHistoryViewer.setUseHashlookup(true);
		fHistoryViewer.setContentProvider(getContentProvider());
		fHistoryViewer.setLabelProvider(getLabelProvider());
		fHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public final void selectionChanged(final SelectionChangedEvent event) {
				final ISelection selection= event.getSelection();
				if (selection instanceof IStructuredSelection)
					handleSelectionChanged((IStructuredSelection) selection);
			}
		});
		fHistoryPane.setContent(fHistoryViewer.getControl());
		createToolBar(fHistoryPane);
		createDetailPane(fSplitterControl);
		final MouseAdapter listener= new MouseAdapter() {

			public void mouseDoubleClick(final MouseEvent event) {
				final Control content= fHistoryPane.getContent();
				if (content != null && content.getBounds().contains(event.x, event.y))
					return;
				final Control control= fHistoryPane.getParent().getParent();
				if (control instanceof Splitter)
					((Splitter) control).setMaximizedControl(fHistoryPane.getParent());
			}
		};
		addMouseListener(listener);
		fHistoryPane.getTopLeft().addMouseListener(listener);
		fSplitterControl.setWeights(new int[] { 65, 35});
		createBottomButtonBar(this);
		Dialog.applyDialogFont(this);
	}

	/**
	 * Creates the detail label of the control
	 *
	 * @param parent
	 *            the parent composite
	 */
	protected void createDetailLabel(final Composite parent) {
		Assert.isNotNull(parent);
		fDetailLabel= new Label(parent, SWT.HORIZONTAL | SWT.LEFT | SWT.WRAP);
		fDetailLabel.setText(RefactoringUIMessages.RefactoringHistoryControl_comment_viewer_label);
		final GridData data= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalSpan= 1;
		fDetailLabel.setLayoutData(data);
	}

	/**
	 * Creates the detail pane of the control.
	 *
	 * @param parent
	 *            the parent composite
	 * @return the detail pane
	 */
	protected Composite createDetailPane(final Composite parent) {
		Assert.isNotNull(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout(getDetailColumns(), true);
		layout.horizontalSpacing= 0;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		createDetailLabel(composite);
		createSelectionLabel(composite);
		fDetailField= new Text(composite, SWT.BORDER | SWT.FLAT | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		fDetailField.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		fDetailField.setText(fControlConfiguration.getCommentCaption());
		final GridData data= new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.horizontalSpan= getDetailColumns();
		fDetailField.setLayoutData(data);
		return composite;
	}

	/**
	 * Creates the history viewer of the control.
	 *
	 * @param parent
	 *            the parent composite
	 * @return the history viewer
	 */
	protected TreeViewer createHistoryViewer(final Composite parent) {
		Assert.isNotNull(parent);
		if (fControlConfiguration.isCheckableViewer())
			return new RefactoringHistoryTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		else
			return new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	}

	/**
	 * Creates the button bar at the right of the component.
	 *
	 * @param parent
	 *            the parent composite
	 */
	protected void createRightButtonBar(final Composite parent) {
		// Do nothing
	}

	/**
	 * Creates the selection label of the control
	 *
	 * @param parent
	 *            the parent composite
	 */
	protected void createSelectionLabel(final Composite parent) {
		Assert.isNotNull(parent);
		fSelectionLabel= new Label(parent, SWT.HORIZONTAL | SWT.RIGHT | SWT.WRAP);
		final GridData data= new GridData(GridData.HORIZONTAL_ALIGN_END);
		data.horizontalSpan= 1;
		fSelectionLabel.setLayoutData(data);
	}

	/**
	 * Creates the toolbar of the control.
	 *
	 * @param parent
	 *            the parent control
	 */
	protected void createToolBar(final ViewForm parent) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public final void dispose() {
		RefactoringCore.getHistoryService().disconnect();
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptorProxy[] getCheckedDescriptors() {
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer)
			return (RefactoringDescriptorProxy[]) fCheckedDescriptors.toArray(new RefactoringDescriptorProxy[fCheckedDescriptors.size()]);
		return getSelectedDescriptors();
	}

	/**
	 * Returns the number of columns to use for the container layout.
	 *
	 * @return the number of columns
	 */
	protected int getContainerColumns() {
		return 2;
	}

	/**
	 * Returns the content provider to use.
	 *
	 * @return the content provider
	 */
	protected RefactoringHistoryContentProvider getContentProvider() {
		return fControlConfiguration.getContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public final Control getControl() {
		return this;
	}

	/**
	 * Returns the refactoring descriptors covered by the specified node.
	 *
	 * @param element
	 *            the refactoring history element
	 * @return the collection of covered descriptors
	 */
	private Collection getCoveredDescriptors(final Object element) {
		Assert.isNotNull(element);
		final Set set= new HashSet();
		getCoveredDescriptors(element, set);
		return set;
	}

	/**
	 * Computes the refactoring descriptors covered by the specified node.
	 *
	 * @param element
	 *            the refactoring history element
	 * @param set
	 *            the set of refactoring descriptor proxies
	 */
	private void getCoveredDescriptors(final Object element, final Set set) {
		Assert.isNotNull(element);
		Assert.isNotNull(set);
		final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
		if (provider != null) {
			if (element instanceof RefactoringHistoryEntry) {
				final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) element;
				final RefactoringDescriptorProxy proxy= entry.getDescriptor();
				set.add(proxy);
			} else {
				final Object[] children= provider.getChildren(element);
				for (int index= 0; index < children.length; index++) {
					final Object child= children[index];
					if (child instanceof RefactoringHistoryNode)
						getCoveredDescriptors(child, set);
				}
			}
		}
	}

	/**
	 * Returns the number of columns to use for the detail pane layout.
	 *
	 * @return the number of columns
	 */
	protected int getDetailColumns() {
		return 2;
	}

	/**
	 * Returns the text to be displayed in the history pane.
	 *
	 * @return the text in the history pane
	 */
	private String getHistoryPaneText() {
		String text= null;
		final IProject project= fControlConfiguration.getProject();
		if (project != null)
			text= Messages.format(fControlConfiguration.getProjectPattern(), BasicElementLabels.getResourceName(project));
		else
			text= fControlConfiguration.getWorkspaceCaption();
		return text;
	}

	/**
	 * Returns the input of the refactoring history control.
	 *
	 * @return the input, or <code>null</code>
	 */
	public final RefactoringHistory getInput() {
		return (RefactoringHistory) fHistoryViewer.getInput();
	}

	/**
	 * Returns the label provider to use.
	 *
	 * @return the label provider
	 */
	protected RefactoringHistoryLabelProvider getLabelProvider() {
		return fControlConfiguration.getLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptorProxy[] getSelectedDescriptors() {
		return (RefactoringDescriptorProxy[]) fSelectedDescriptors.toArray(new RefactoringDescriptorProxy[fSelectedDescriptors.size()]);
	}

	/**
	 * Handles the check state changed event.
	 */
	protected void handleCheckStateChanged() {
		if (fSelectionLabel != null) {
			final RefactoringHistory history= getInput();
			if (history != null) {
				final int total= history.getDescriptors().length;
				final int checked= fCheckedDescriptors.size();
				if (fSelectionLabel.isEnabled()) {
					if (total > 0 && fControlConfiguration.isCheckableViewer())
						fSelectionLabel.setText(Messages.format(RefactoringUIMessages.RefactoringHistoryControl_selection_pattern, new String[] { String.valueOf(checked), String.valueOf(total)}));
					else
						fSelectionLabel.setText(RefactoringUIMessages.RefactoringHistoryControl_no_selection);
				} else
					fSelectionLabel.setText(""); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Handles the selection changed event.
	 *
	 * @param selection
	 *            the new selection
	 */
	protected void handleSelectionChanged(final IStructuredSelection selection) {
		Assert.isNotNull(selection);
		fSelectedDescriptors.clear();
		final Object[] elements= selection.toArray();
		for (int index= 0; index < elements.length; index++) {
			final Object element= elements[index];
			if (element instanceof RefactoringHistoryEntry) {
				final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) element;
				final RefactoringDescriptorProxy proxy= entry.getDescriptor();
				fSelectedDescriptors.add(proxy);
			} else if (element instanceof RefactoringHistoryNode) {
				final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
				fSelectedDescriptors.addAll(getCoveredDescriptors(node));
			}
		}
		if (elements.length == 1 && elements[0] instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) elements[0];
			final RefactoringDescriptorProxy proxy= entry.getDescriptor();
			final Job job= new UIJob(RefactoringUIMessages.RefactoringHistoryControl_resolving_information) {

				public final IStatus runInUIThread(final IProgressMonitor monitor) {
					final RefactoringDescriptor descriptor= proxy.requestDescriptor(monitor);
					if (descriptor != null) {
						String comment= descriptor.getComment();
						if ("".equals(comment)) //$NON-NLS-1$
							comment= RefactoringUIMessages.RefactoringHistoryControl_no_comment;
						fDetailField.setText(comment);
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		} else
			fDetailField.setText(fControlConfiguration.getCommentCaption());
	}

	/**
	 * Reconciles the check state of the control.
	 */
	public void reconcileCheckState() {
		final RefactoringHistory history= getInput();
		if (history != null && fHistoryViewer instanceof RefactoringHistoryTreeViewer)
			((RefactoringHistoryTreeViewer) fHistoryViewer).reconcileCheckState(history);
	}

	/**
	 * Reconciles the selection state of the control.
	 */
	public void reconcileSelectionState() {
		final RefactoringHistoryNode[] nodes= new RefactoringHistoryNode[fSelectedDescriptors.size()];
		int index= 0;
		for (final Iterator iterator= fSelectedDescriptors.iterator(); iterator.hasNext(); index++) {
			final RefactoringDescriptorProxy descriptor= (RefactoringDescriptorProxy) iterator.next();
			nodes[index]= new RefactoringHistoryEntry(null, descriptor);
			fHistoryViewer.expandToLevel(nodes[index], AbstractTreeViewer.ALL_LEVELS);
		}
		fHistoryViewer.setSelection(new StructuredSelection(nodes), true);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeCheckStateListener(final ICheckStateListener listener) {
		Assert.isNotNull(listener);
		Assert.isNotNull(listener);
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer) {
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			viewer.removeCheckStateListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		Assert.isNotNull(listener);
		if (fHistoryViewer != null)
			fHistoryViewer.removeSelectionChangedListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setCheckedDescriptors(final RefactoringDescriptorProxy[] descriptors) {
		Assert.isNotNull(descriptors);
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer) {
			fCheckedDescriptors.clear();
			fCheckedDescriptors.addAll(Arrays.asList(descriptors));
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			final RefactoringHistory history= RefactoringHistoryControl.this.getInput();
			if (history != null)
				viewer.reconcileCheckState(history);
			handleCheckStateChanged();
		} else
			setSelectedDescriptors(descriptors);
	}

	/**
	 * Sets the expanded state of the viewer.
	 */
	protected void setExpandedState() {
		final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
		if (provider != null) {
			handleCheckStateChanged();
			final Object[] roots= provider.getRootElements();
			if (roots != null) {
				for (int index= 0; index < roots.length; index++) {
					if (!(roots[index] instanceof RefactoringHistoryEntry)) {
						fHistoryViewer.setExpandedState(roots[index], true);
						return;
					}
				}
			}
		}
	}

	/**
	 * Sets the enablement of the detail pane.
	 */
	protected void setHistoryControlEnablement() {
		boolean enable= false;
		final RefactoringHistory history= (RefactoringHistory) fHistoryViewer.getInput();
		if (history != null) {
			final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
			if (proxies.length > 0)
				enable= true;
		}
		if (fDetailField != null)
			fDetailField.setEnabled(enable);
		if (fDetailLabel != null)
			fDetailLabel.setEnabled(enable);
		if (fHistoryPane != null)
			fHistoryPane.setEnabled(enable);
		if (fSelectionLabel != null)
			fSelectionLabel.setEnabled(enable);
		if (enable) {
			fDetailField.setText(fControlConfiguration.getCommentCaption());
			if (fSelectionLabel != null)
				fSelectionLabel.setText(RefactoringUIMessages.RefactoringHistoryControl_no_selection);
		} else {
			fDetailField.setText(""); //$NON-NLS-1$
			if (fSelectionLabel != null)
				fSelectionLabel.setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		fHistoryViewer.setInput(history);
		fSelectedDescriptors.clear();
		fCheckedDescriptors.clear();
		if (history != null) {
			final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
			if (provider != null) {
				provider.inputChanged(fHistoryViewer, null, history);
				setHistoryControlEnablement();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setSelectedDescriptors(final RefactoringDescriptorProxy[] descriptors) {
		Assert.isNotNull(descriptors);
		if (fHistoryViewer != null) {
			fSelectedDescriptors.clear();
			fSelectedDescriptors.addAll(Arrays.asList(descriptors));
			final RefactoringHistoryNode[] nodes= new RefactoringHistoryNode[descriptors.length];
			for (int index= 0; index < descriptors.length; index++)
				nodes[index]= new RefactoringHistoryEntry(null, descriptors[index]);
			fHistoryViewer.setSelection(new StructuredSelection(nodes));
		}
	}
}