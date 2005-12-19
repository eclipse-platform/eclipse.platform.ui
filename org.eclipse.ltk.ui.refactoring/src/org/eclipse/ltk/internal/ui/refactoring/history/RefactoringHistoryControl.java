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

import java.text.MessageFormat;
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

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
		}

		/**
		 * Finds the widget corresponding to the specified element.
		 * 
		 * @param element
		 *            the element
		 * @return the corresponding widget
		 */
		public Widget findWidget(final Object element) {
			return findItem(element);
		}

		/**
		 * Returns the child items of the specified widget.
		 * 
		 * @param widget
		 *            the widget
		 * @return the child items
		 */
		protected Item[] getChildItems(final Widget widget) {
			if (widget instanceof TreeItem)
				return ((TreeItem) widget).getItems();
			if (widget instanceof Tree)
				return ((Tree) widget).getItems();
			return null;
		}

		/**
		 * Is the specified element to be displayed as grayed out?
		 * 
		 * @param parent
		 *            the parent element to test
		 * @param children
		 *            the child elements
		 * @return <code>true</code> if the element needs to be grayed out,
		 *         <code>false</code> otherwise
		 */
		protected boolean isGrayedElement(final Object parent, final Object[] children) {
			int elements= 0;
			for (int index= 0; index < children.length; index++) {
				if (getGrayed(children[index]) || getChecked(children[index]))
					elements++;
			}
			return !(elements == 0 || elements == children.length);
		}

		/**
		 * Sets the children to gray.
		 * 
		 * @param items
		 *            the tree items
		 * @param grayed
		 *            <code>true</code> to set to gray, <code>false</code>
		 *            otherwise
		 */
		protected void setChildrenGrayed(final Item[] items, final boolean grayed) {
			for (int index= 0; index < items.length; index++) {
				if (items[index] instanceof TreeItem) {
					final TreeItem item= (TreeItem) items[index];
					if (item.getGrayed() != grayed) {
						item.setGrayed(grayed);
						setChildrenGrayed(getChildItems(item), grayed);
					}
				}
			}
		}

		/**
		 * Sets the subtree of the specified element to gray.
		 * 
		 * @param element
		 *            the element
		 * @param grayed
		 *            <code>true</code> to set to gray, <code>false</code>
		 *            otherwise
		 */
		protected void setSubtreeGrayed(final Object element, final boolean grayed) {
			final Widget widget= findWidget(element);
			if (widget instanceof TreeItem) {
				final TreeItem item= (TreeItem) widget;
				if (item.getGrayed() != grayed) {
					item.setGrayed(grayed);
					setChildrenGrayed(getChildItems(item), grayed);
				}
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
	public final void createControl() {
		RefactoringCore.getRefactoringHistoryService().connect();
		fCaptionImage= RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE.createImage();
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
		fHistoryViewer.setContentProvider(fControlConfiguration.getContentProvider());
		fHistoryViewer.setLabelProvider(fControlConfiguration.getLabelProvider());
		fHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public final void selectionChanged(final SelectionChangedEvent event) {
				final ISelection selection= event.getSelection();
				if (selection instanceof IStructuredSelection)
					handleSelectionChanged((IStructuredSelection) selection);
			}
		});
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer) {
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			viewer.addCheckStateListener(new ICheckStateListener() {

				public final void checkStateChanged(final CheckStateChangedEvent event) {
					handleCheckStateChanged(event);
				}
			});
		}
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
		fSplitterControl.setWeights(new int[] { 80, 20});
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
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptorProxy[] getCheckedDescriptors() {
		if (fHistoryViewer instanceof RefactoringHistoryTreeViewer) {
			final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
			final Set set= new HashSet();
			final Object[] elements= viewer.getCheckedElements();
			for (int index= 0; index < elements.length; index++)
				getDescriptorProxies(viewer, set, elements[index]);
			return (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]);
		}
		return getSelectedDescriptors();
	}

	/**
	 * Computes the refactoring descriptor proxies of the specified element.
	 * 
	 * @param viewer
	 *            the refactoring history viewer
	 * @param set
	 *            the set of refactoring descriptors
	 * @param element
	 *            the element to compute the descriptors for
	 */
	private void getDescriptorProxies(final TreeViewer viewer, final Set set, final Object element) {
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
					getDescriptorProxies(viewer, set, elements[index]);
			}
		}
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
			text= MessageFormat.format(fControlConfiguration.getProjectPattern(), new String[] { project.getName()});
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
			for (final Iterator iterator= structured.iterator(); iterator.hasNext();)
				getDescriptorProxies(fHistoryViewer, set, iterator.next());
		}
		return (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]);
	}

	/**
	 * Handles the check state changed event.
	 * 
	 * @param event
	 *            the check state changed event
	 */
	protected void handleCheckStateChanged(final CheckStateChangedEvent event) {
		final RefactoringHistoryTreeViewer viewer= (RefactoringHistoryTreeViewer) fHistoryViewer;
		BusyIndicator.showWhile(getDisplay(), new Runnable() {

			public final void run() {
				final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) viewer.getContentProvider();
				final Object element= event.getElement();
				final boolean checked= event.getChecked();
				viewer.setSubtreeChecked(element, checked);
				viewer.setSubtreeGrayed(element, false);
				Object parent= provider.getParent(element);
				while (parent != null) {
					final boolean grayed= viewer.isGrayedElement(parent, provider.getChildren(parent));
					viewer.setChecked(parent, checked || grayed);
					viewer.setGrayed(parent, grayed);
					parent= provider.getParent(parent);
				}
				final RefactoringDescriptorProxy[] proxies= getCheckedDescriptors();
				final RefactoringDescriptorProxy[] total= getInput().getDescriptors();
				fHistoryPane.setText(NLS.bind(RefactoringUIMessages.RefactoringHistoryControl_selection_pattern, new String[] { getHistoryPaneText(), String.valueOf(proxies.length), String.valueOf(total.length)}));
			}
		});
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
	public void setInput(final RefactoringHistory history) {
		fHistoryViewer.setInput(history);
		if (history != null) {
			final RefactoringHistoryContentProvider provider= (RefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
			if (provider != null) {
				provider.inputChanged(fHistoryViewer, null, history);
				final Object[] roots= provider.getRootElements();
				if (roots != null && roots.length > 0) {
					final Object first= roots[0];
					if (first != null)
						fHistoryViewer.setExpandedState(first, true);
				}
			}
		}
	}
}