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
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IProject;

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
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
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
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryLabelProvider;

/**
 * Control which is capable of displaying refactoring histories.
 * 
 * @since 3.2
 */
public class RefactoringHistoryControl extends Composite implements IRefactoringHistoryControl {

	/** The comment caption key */
	private static final String COMMENT_CAPTION= "commentCaption"; //$NON-NLS-1$

	/** The project caption key */
	private static final String PROJECT_CAPTION= "projectCaption"; //$NON-NLS-1$

	/** The workspace caption key */
	private static final String WORKSPACE_CAPTION= "workspaceCaption"; //$NON-NLS-1$

	/** The caption image */
	private Image fCaptionImage= null;

	/** The comment pane */
	private CompareViewerSwitchingPane fCommentPane= null;

	/** The content provider to use, or <code>null</code> */
	private RefactoringHistoryContentProvider fContentProvider= null;

	/** The history pane */
	private CompareViewerPane fHistoryPane= null;

	/** The history viewer */
	private TreeViewer fHistoryViewer= null;

	/** The label provider to use */
	private RefactoringHistoryLabelProvider fLabelProvider= null;

	/** The message, or <code>null</code> */
	private String fMessage= null;

	/** The message label */
	private Label fMessageLabel= null;

	/** The project, or <code>null</code> */
	private IProject fProject= null;

	/** The resource bundle to use */
	protected final ResourceBundle fResourceBundle;

	/** The splitter control */
	private Splitter fSplitterControl= null;

	/**
	 * Creates a new refactoring history control.
	 * 
	 * @param parent
	 *            the parent control
	 * @param bundle
	 *            the resource bundle to use
	 */
	public RefactoringHistoryControl(final Composite parent, final ResourceBundle bundle) {
		super(parent, SWT.NONE);
		fResourceBundle= bundle;
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
		Assert.isNotNull(fContentProvider);
		Assert.isNotNull(fLabelProvider);
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
		handleCaptionChanged();
		fHistoryPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		fHistoryViewer= createHistoryViewer(fHistoryPane);
		fHistoryViewer.setAutoExpandLevel(2);
		fHistoryViewer.setContentProvider(fContentProvider);
		fHistoryViewer.setLabelProvider(fLabelProvider);
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
					final SourceViewer extended= new SourceViewer(fCommentPane, null, SWT.NULL);
					extended.setDocument(new Document(comment));
					setText(fResourceBundle.getString(COMMENT_CAPTION));
					return extended;
				}
				return null;
			}
		};
		fCommentPane.setText(fResourceBundle.getString(COMMENT_CAPTION));
		fMessageLabel= new Label(fSplitterControl, SWT.LEFT | SWT.WRAP | SWT.HORIZONTAL);
		handleMessageChanged();
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
	 * Handles the caption changed event.
	 */
	protected void handleCaptionChanged() {
		String text= null;
		if (fProject != null)
			text= MessageFormat.format(fResourceBundle.getString(PROJECT_CAPTION), new String[] { fProject.getName()});
		else
			text= fResourceBundle.getString(WORKSPACE_CAPTION);
		fHistoryPane.setText(text);
	}

	/**
	 * Handles the message changed event.
	 */
	protected void handleMessageChanged() {
		if (fMessage != null && !"".equals(fMessage)) { //$NON-NLS-1$
			fMessageLabel.setText(fMessage);
			fSplitterControl.setWeights(new int[] { 70, 15, 15});
		} else
			fSplitterControl.setWeights(new int[] { 75, 25, 0});
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
						fCommentPane.setText(fResourceBundle.getString(COMMENT_CAPTION));
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
		fCommentPane.setInput(null);
		fCommentPane.setText(fResourceBundle.getString(COMMENT_CAPTION));
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setContentProvider(final RefactoringHistoryContentProvider provider) {
		Assert.isNotNull(provider);
		fContentProvider= provider;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setDisplayTime(final boolean display) {
		fContentProvider.setDisplayTime(display);
		fLabelProvider.setDisplayTime(display);
		fHistoryViewer.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setLabelProvider(final RefactoringHistoryLabelProvider provider) {
		Assert.isNotNull(provider);
		fLabelProvider= provider;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setMessage(final String message) {
		fMessage= message;
		if (fSplitterControl != null && fMessageLabel != null)
			handleMessageChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setProject(final IProject project) {
		fProject= project;
		if (fHistoryPane != null)
			handleCaptionChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRefactoringHistory(final RefactoringHistory history) {
		fHistoryViewer.setInput(history);
	}
}