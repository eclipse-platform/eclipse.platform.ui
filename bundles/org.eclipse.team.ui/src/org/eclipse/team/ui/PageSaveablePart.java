/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public abstract class PageSaveablePart extends SaveablePartAdapter implements IContentChangeListener{
	
	private CompareConfiguration cc;
	Shell shell;
	
//	 Tracking of dirty state
	private boolean fDirty= false;
	private ArrayList fDirtyViewers= new ArrayList();
	private IPropertyChangeListener fDirtyStateListener;
	
	//	 SWT controls
	private CompareViewerSwitchingPane fContentPane;
	private CompareViewerPane fEditionPane;
	private CompareViewerSwitchingPane fStructuredComparePane;
	private Control control;
	
	// Configuration options
	private boolean showContentPanes = true;
	
	public PageSaveablePart(Shell shell, CompareConfiguration cc){
		this.shell = shell;
		this.cc = cc;
		
		fDirtyStateListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String propertyName= e.getProperty();
				if (CompareEditorInput.DIRTY_STATE.equals(propertyName)) {
					boolean changed= false;
					Object newValue= e.getNewValue();
					if (newValue instanceof Boolean)
						changed= ((Boolean)newValue).booleanValue();
					setDirty(e.getSource(), changed);
				}			
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return fDirty || fDirtyViewers.size() > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent2) {
		Composite parent = new Composite(parent2, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		parent.setLayout(layout);
		parent.setLayoutData(data);
		
		shell = parent2.getShell();
		
		Splitter vsplitter = new Splitter(parent, SWT.VERTICAL);
		vsplitter.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
		// we need two panes: the left for the elements, the right one for the structured diff
		Splitter hsplitter = new Splitter(vsplitter, SWT.HORIZONTAL);
		fEditionPane = new CompareViewerPane(hsplitter, SWT.BORDER | SWT.FLAT);
		fStructuredComparePane = new CompareViewerSwitchingPane(hsplitter, SWT.BORDER | SWT.FLAT, true) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				if (input instanceof ICompareInput)
					return findStructureViewer(this, oldViewer, (ICompareInput)input);
				return null;
			}
		};
		fStructuredComparePane.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				feedInput2(e.getSelection());
			}
		});
		fEditionPane.setText(TeamUIMessages.ParticipantPageSaveablePart_0); 
		fContentPane = new CompareViewerSwitchingPane(vsplitter, SWT.BORDER | SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				if (!(input instanceof ICompareInput))
					return null;
				Viewer newViewer= findContentViewer(this, oldViewer, (ICompareInput)input);
				boolean isNewViewer= newViewer != oldViewer;
				if (isNewViewer && newViewer instanceof IPropertyChangeNotifier) {
					final IPropertyChangeNotifier dsp= (IPropertyChangeNotifier) newViewer;
					dsp.addPropertyChangeListener(fDirtyStateListener);
					Control c= newViewer.getControl();
					c.addDisposeListener(
						new DisposeListener() {
							public void widgetDisposed(DisposeEvent e) {
								dsp.removePropertyChangeListener(fDirtyStateListener);
							}
						}
					);
					hookContentChangeListener((ICompareInput)input);
				}	
				return newViewer;
			}
		};
		vsplitter.setWeights(new int[]{30, 70});
		
		control = parent;
		
		if(! showContentPanes) {
			hsplitter.setMaximizedControl(fEditionPane);
		}

	}
	// TODO need to figure out save lifecycle
	protected void setDirty(boolean dirty) {
		boolean confirmSave= true;
		Object o= cc.getProperty(CompareEditor.CONFIRM_SAVE_PROPERTY);
		if (o instanceof Boolean)
			confirmSave= ((Boolean)o).booleanValue();

		if (!confirmSave) {
			fDirty= dirty;
			if (!fDirty)
				fDirtyViewers.clear();
		}
	}
	
	private void setDirty(Object source, boolean dirty) {
		Assert.isNotNull(source);
		if (dirty)
			fDirtyViewers.add(source);
		else
			fDirtyViewers.remove(source);
	}	
	
	/*
	 * Feeds input from the participant page into the content and structured viewers.
	 * TODO can we hide this
	 */
	protected void setInput(Object input) {
		CompareViewerPane pane = getContentPane();
		if (pane != null && !pane.isDisposed())
			getContentPane().setInput(input);
		if (fStructuredComparePane != null)
			fStructuredComparePane.setInput(input);
	}
	
	/*
	 * Feeds selection from structure viewer to content viewer.
	 */
	private void feedInput2(ISelection sel) {
		ICompareInput input = getCompareInput(sel);
		prepareCompareInput(input);
		if (input != null)
			fContentPane.setInput(input);
	}
	
	// TODO can we hide this usign a compare adpater?
	/* package */protected void prepareCompareInput(final ICompareInput input) {
		// TODO Merge operations would need to do this check as well
		// TODO Where should this check live
		// TODO What about the left vs. right side changes (perhaps a buffer that wraps three buffers)
		if (input == null)
			return;
		IProgressService manager = PlatformUI.getWorkbench().getProgressService();
		try {
			// TODO: we need a better progress story here
			manager.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			        prepareInput(input, getCompareConfiguration(), monitor);
				}
			});
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
	protected abstract void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws InvocationTargetException;

	//	 TODO can we hide this usign a compare adpater?
	protected void hookContentChangeListener(ICompareInput node) {
		// TODO: there is no unhook which may lead to a leak
		ITypedElement left = node.getLeft();
		if(left instanceof IContentChangeNotifier) {
			((IContentChangeNotifier)left).addContentChangeListener(this);
		}
		ITypedElement right = node.getRight();
		if(right instanceof IContentChangeNotifier) {
			((IContentChangeNotifier)right).addContentChangeListener(this);
		}
	}
	
	abstract public String getTitle();

	abstract public Image getTitleImage();
	
	protected Shell getShell() {
		return shell;
	}

//	 TODO can we hide
	protected CompareViewerPane getEditionPane() {
		return fEditionPane;
	}
//	 TODO can we hide
	protected CompareViewerSwitchingPane getStructuredComparePane() {
		return fStructuredComparePane;
	}
//	 TODO can we hide
	protected CompareViewerSwitchingPane getContentPane() {
		return fContentPane;
	}
	
	/*
	 * Find a viewer that can provide a structure view for the given compare input.
	 * Return <code>null</code> if a suitable viewer could not be found.
	 * @param parent the parent composite for the viewer
	 * @param oldViewer the viewer that is currently a child of the parent
	 * @param input the compare input to be viewed
	 * @return a viewer capable of displaying a structure view of the input or
	 * <code>null</code> if such a viewer is not available.
	 */
	private Viewer findStructureViewer(Composite parent, Viewer oldViewer, ICompareInput input) {
		return CompareUI.findStructureViewer(oldViewer, input, parent, cc);
	}
	
	/*
	 * Find a viewer that can provide a content compare view for the given compare input.
	 * Return <code>null</code> if a suitable viewer could not be found.
	 * @param parent the parent composite for the viewer
	 * @param oldViewer the viewer that is currently a child of the parent
	 * @param input the compare input to be viewed
	 * @return a viewer capable of displaying a content compare view of the input or
	 * <code>null</code> if such a viewer is not available.
	 */
	private Viewer findContentViewer(Composite parent, Viewer oldViewer, ICompareInput input) {
		return CompareUI.findContentViewer(oldViewer, input, parent, cc);
	}
	
	/*
	 * Return a compare input that represents the selection.
	 * This input is used to feed the structure and content
	 * viewers. By default, a compare input is returned if the selection is
	 * of size 1 and the selected element implements <code>ICompareInput</code>
	 * @param selection the selection
	 * @return a compare input representing the selection
	 * TODO can we hide
	 */
	protected ICompareInput getCompareInput(ISelection selection) {
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o = ss.getFirstElement();
				if(o instanceof ICompareInput) {
					return (ICompareInput)o;
				}
			}
		}
		return null;
	}

    /**
     * Set whether the file contents panes should be shown. If they are not,
     * only the resource tree will be shown.
     * 
     * @param showContentPanes whether to show contents pane
     */
	public void setShowContentPanes(boolean showContentPanes) {
		this.showContentPanes = showContentPanes;
	}

	/**
	 * Returns the primary control for this part.
	 * 
	 * @return the primary control for this part.
	 */
	public Control getControl() {
		return control;
	}

	protected CompareConfiguration getCompareConfiguration() {
		return cc;
	}

}
