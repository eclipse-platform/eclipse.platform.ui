/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
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
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.progress.UIJob;

import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.Splitter;

import org.eclipse.ltk.ui.refactoring.history.IRefactoringHistoryControl;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryContentProvider;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

import org.eclipse.osgi.util.NLS;

/**
 * Control which is capable of displaying refactoring histories.
 * 
 * @since 3.2
 */
public class RefactoringHistoryControl extends Composite implements IRefactoringHistoryControl {

	/** Checkbox treeviewer for the refactoring history */
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
					updateCheckState(event.getElement(), event.getChecked());
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
					final Object element= event.getElement();
					if (getChecked(element)) {
						setSubTreeGrayed(element, false);
						setSubtreeChecked(element, true);
					}
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
		 * Returns the parent of the specified element.
		 * 
		 * @param element
		 *            the element
		 * @return the parent of the element
		 */
		private Object getParent(final Object element) {
			return ((RefactoringHistoryContentProvider) getContentProvider()).getParent(element);
		}

		/**
		 * Propagates the checkstate of the specified element and its children.
		 * 
		 * @param element
		 *            the element
		 * @param children
		 *            the children
		 */
		private void propagateCheckState(final Object element, final Object[] children) {
			int checkCount= 0;
			for (int index= 0; index < children.length; index++) {
				if (getChecked(children[index]))
					checkCount++;
			}
			setElementChecked(element, checkCount > 0);
			setElementGrayed(element, checkCount != 0 && checkCount != children.length);
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
		 * checked.
		 * 
		 * @param element
		 *            the element specifying the subtree
		 * @param checked
		 *            <code>true</code> to render the subtree checked,
		 *            <code>false</code> otherwise
		 */
		private void setSubTreeChecked(final Object element, final boolean checked) {
			setElementChecked(element, checked);
			final Object[] children= getChildren(element);
			for (int index= 0; index < children.length; index++) {
				setSubTreeChecked(children[index], checked);
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

		/**
		 * Updates the check state after some refactoring descriptors have been
		 * checked.
		 */
		public void updateCheckState() {
			updateCheckState(getInput());
		}

		/**
		 * Updates the checkstate of the specified element and dependencies.
		 * 
		 * @param element
		 *            the changed element
		 */
		private void updateCheckState(final Object element) {
			final Object[] children= getChildren(element);
			for (int index= 0; index < children.length; index++) {
				updateCheckState(children[index]);
			}
			if (children.length > 0)
				propagateCheckState(element, children);
		}

		/**
		 * Updates the checkstate of the specified element and dependencies.
		 * 
		 * @param element
		 *            the changed element
		 * @param checked
		 *            <code>true</code> if the element is checked,
		 *            <code>false</code> otherwise
		 */
		private void updateCheckState(final Object element, final boolean checked) {
			setSubTreeChecked(element, checked);
			setSubTreeGrayed(element, false);
			Object current= getParent(element);
			while (current != null) {
				final Object[] children= getChildren(current);
				propagateCheckState(current, children);
				current= getParent(current);
			}
		}
	}

	/** The caption image */
	private Image fCaptionImage= null;

	/** The comment pane */
	private CompareViewerSwitchingPane fCommentPane= null;

	/** The refactoring history control configuration to use */
	protected final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The history pane */
	private CompareViewerPane fHistoryPane= null;

	/** The history viewer */
	private TreeViewer fHistoryViewer= null;

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
	 * Creates the button bar at the right of the component.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createButtonBar(final Composite parent) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl() {
		RefactoringCore.getRefactoringHistoryService().connect();
		fCaptionImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_COLL.createImage();
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		setLayout(layout);
		final GridData data= new GridData();
		data.grabExcessHorizontalSpace= true;
		data.grabExcessVerticalSpace= true;
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.FILL;
		setLayoutData(data);
		fSplitterControl= new Splitter(this, SWT.VERTICAL);
		fSplitterControl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
		fSplitterControl.addDisposeListener(new DisposeListener() {

			public final void widgetDisposed(final DisposeEvent event) {
				if (fCaptionImage != null)
					fCaptionImage.dispose();
			}
		});
		createButtonBar(this);
		final Composite leftPane= new Composite(fSplitterControl, SWT.NONE);
		layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 2;
		layout.verticalSpacing= 2;
		leftPane.setLayout(layout);
		fHistoryPane= new CompareViewerPane(leftPane, SWT.BORDER | SWT.FLAT);
		if (fControlConfiguration.isTimeDisplayed())
			fHistoryPane.setImage(fCaptionImage);
		fHistoryPane.setText(getHistoryPaneText());
		fHistoryPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		fHistoryViewer= createHistoryViewer(fHistoryPane);
		if (!fControlConfiguration.isTimeDisplayed())
			fHistoryViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		fHistoryViewer.setUseHashlookup(true);
		fHistoryViewer.setContentProvider(fControlConfiguration.getContentProvider());
		fHistoryViewer.setLabelProvider(fControlConfiguration.getLabelProvider());
		fHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public final void selectionChanged(final SelectionChangedEvent event) {
				final ISelection selection= event.getSelection();
				if (selection instanceof IStructuredSelection)
					handleSelectionChanged((IStructuredSelection) selection);
			}
		});
		fHistoryPane.setContent(fHistoryViewer.getControl());
		fCommentPane= new CompareViewerSwitchingPane(fSplitterControl, SWT.BORDER | SWT.FLAT) {

			protected final Viewer getViewer(final Viewer viewer, final Object input) {
				if (input instanceof String) {
					final String comment= (String) input;
					final TextViewer extended= new TextViewer(fCommentPane, SWT.NULL);
					extended.setDocument(new Document(comment));
					setText(fControlConfiguration.getCommentCaption());
					return extended;
				}
				return null;
			}
		};
		fCommentPane.setText(fControlConfiguration.getCommentCaption());
		fCommentPane.setEnabled(false);
		fSplitterControl.setWeights(new int[] { 80, 20 });

		Dialog.applyDialogFont(this);
	}

	/**
	 * Creates the history viewer of the dialog.
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
	 * {@inheritDoc}
	 */
	public final void dispose() {
		RefactoringCore.getRefactoringHistoryService().disconnect();
		super.dispose();
	}

	/**
	 * Computes the checked refactoring descriptor proxies of the specified
	 * element.
	 * 
	 * @param viewer
	 *            the refactoring history viewer
	 * @param set
	 *            the set of checked refactoring descriptors
	 * @param element
	 *            the element to compute the descriptors for
	 */
	private void getCheckedDescriptorProxies(final TreeViewer viewer, final Set set, final Object element) {
		if (element instanceof RefactoringHistoryEntry) {
			if (viewer instanceof RefactoringHistoryTreeViewer) {
				final RefactoringHistoryTreeViewer extended= (RefactoringHistoryTreeViewer) viewer;
				if (!extended.getChecked(element))
					return;
			}
			set.add(((RefactoringHistoryEntry) element).getDescriptor());
		} else if (element instanceof RefactoringHistoryNode) {
			if (viewer instanceof RefactoringHistoryTreeViewer) {
				final RefactoringHistoryTreeViewer extended= (RefactoringHistoryTreeViewer) viewer;
				if (!extended.getChecked(element))
					return;
			}
			final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) viewer.getContentProvider();
			if (provider != null) {
				final Object[] elements= provider.getChildren(element);
				for (int index= 0; index < elements.length; index++)
					getCheckedDescriptorProxies(viewer, set, elements[index]);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptorProxy[] getCheckedDescriptors() {
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer) {
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			final Set set= new HashSet();
			final Object[] elements= viewer.getCheckedElements();
			for (int index= 0; index < elements.length; index++)
				getCheckedDescriptorProxies(viewer, set, elements[index]);
			return (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]);
		}
		return getSelectedDescriptors();
	}

	/**
	 * {@inheritDoc}
	 */
	public final Control getControl() {
		return this;
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
			text= Messages.format(fControlConfiguration.getProjectPattern(), new String[] { project.getName() });
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
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptorProxy[] getSelectedDescriptors() {
		final Set set= new HashSet();
		final ISelection selection= fHistoryViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structured= (IStructuredSelection) selection;
			for (final Iterator iterator= structured.iterator(); iterator.hasNext();) {
				final RefactoringHistoryNode node= (RefactoringHistoryNode) iterator.next();
				if (node instanceof RefactoringHistoryEntry) {
					final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) node;
					set.add(entry.getDescriptor());
				}
			}
		}
		return (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]);
	}

	/**
	 * Handles the check state changed event.
	 */
	protected void handleCheckStateChanged() {
		final int total= RefactoringHistoryControl.this.getInput().getDescriptors().length;
		if (total > 0 && fControlConfiguration.isCheckableViewer())
			fHistoryPane.setText(NLS.bind(RefactoringUIMessages.RefactoringHistoryControl_selection_pattern, new String[] { getHistoryPaneText(), String.valueOf(getCheckedDescriptors().length), String.valueOf(total)}));
		else
			fHistoryPane.setText(getHistoryPaneText());
	}

	/**
	 * Handles the selection changed event.
	 * 
	 * @param selection
	 *            the new selection
	 */
	protected void handleSelectionChanged(final IStructuredSelection selection) {
		Assert.isNotNull(selection);
		final Object[] elements= selection.toArray();
		if (elements.length == 1 && elements[0] instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) elements[0];
			final RefactoringDescriptorProxy proxy= entry.getDescriptor();
			final Job job= new UIJob(RefactoringUIMessages.RefactoringHistoryControl_resolving_information) {

				public final IStatus runInUIThread(final IProgressMonitor monitor) {
					final RefactoringDescriptor descriptor= proxy.requestDescriptor(monitor);
					if (descriptor != null) {
						fCommentPane.setInput(descriptor.getComment());
						fCommentPane.setText(fControlConfiguration.getCommentCaption());
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
		fCommentPane.setInput(null);
		fCommentPane.setText(fControlConfiguration.getCommentCaption());
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
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			final RefactoringHistoryNode[] nodes= new RefactoringHistoryNode[descriptors.length];
			for (int index= 0; index < descriptors.length; index++)
				nodes[index]= new RefactoringHistoryEntry(null, descriptors[index]);
			viewer.setCheckedElements(nodes);
			viewer.updateCheckState();
			handleCheckStateChanged();
		} else
			setSelectedDescriptors(descriptors);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		fHistoryViewer.setInput(history);
		if (history != null) {
			final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
			if (provider != null) {
				provider.inputChanged(fHistoryViewer, null, history);
				handleCheckStateChanged();
				final Object[] roots= provider.getRootElements();
				if (roots != null && roots.length > 0) {
					final Object first= roots[0];
					if (first != null)
						fHistoryViewer.setExpandedState(first, true);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setSelectedDescriptors(final RefactoringDescriptorProxy[] descriptors) {
		Assert.isNotNull(descriptors);
		if (fHistoryViewer != null) {
			final RefactoringHistoryNode[] nodes= new RefactoringHistoryNode[descriptors.length];
			for (int index= 0; index < descriptors.length; index++)
				nodes[index]= new RefactoringHistoryEntry(null, descriptors[index]);
			fHistoryViewer.setSelection(new StructuredSelection(nodes));
		}
	}
}