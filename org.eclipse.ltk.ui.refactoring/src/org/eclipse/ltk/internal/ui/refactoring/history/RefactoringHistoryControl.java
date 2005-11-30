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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
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

/**
 * Control which is capable of displaying refactoring histories.
 * 
 * @since 3.2
 */
public class RefactoringHistoryControl extends Composite implements IRefactoringHistoryControl {

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
		fHistoryPane.setImage(fCaptionImage);
		String text= null;
		final IProject project= fControlConfiguration.getProject();
		if (project != null)
			text= MessageFormat.format(fControlConfiguration.getProjectPattern(), new String[] { project.getName()});
		else
			text= fControlConfiguration.getWorkspaceCaption();
		fHistoryPane.setText(text);
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
	public final RefactoringDescriptorProxy[] getSelectedDescriptors() {
		final Set set= new HashSet();
		final ISelection selection= fHistoryViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structured= (IStructuredSelection) selection;
			for (final Iterator iterator= structured.iterator(); iterator.hasNext();) {
				final Object element= iterator.next();
				if (element instanceof RefactoringHistoryEntry) {
					final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) element;
					set.add(entry.getDescriptor());
				}

				// TODO: handle parent elements
			}
		}
		return (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]);
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
			final RefactoringHistoryContentProvider provider= fControlConfiguration.getContentProvider();
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