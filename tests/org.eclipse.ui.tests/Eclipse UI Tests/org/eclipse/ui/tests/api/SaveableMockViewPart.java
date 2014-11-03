/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.internal.DefaultSaveable;

/**
 * Mock view part that implements ISaveablePart.
 * Used for testing hideView and other view lifecycle on saveable views.
 *
 * @since 3.0.1
 */
public class SaveableMockViewPart extends MockViewPart implements
		ISaveablePart, ISaveablesSource {

	public static String ID = "org.eclipse.ui.tests.api.SaveableMockViewPart";

	private boolean isDirty = false;

    private boolean saveAsAllowed = false;

    private boolean saveNeeded = true;

	private boolean adapt;

    @Override
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        final Button dirtyToggle = new Button(parent, SWT.CHECK);
        dirtyToggle.setText("Dirty");
        dirtyToggle.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                setDirty(dirtyToggle.getSelection());
            }
        });
        dirtyToggle.setSelection(isDirty());

        final Button adaptToggle = new Button(parent, SWT.CHECK);
        adaptToggle.setText("Adapt to resource");
        adaptToggle.addSelectionListener(new SelectionAdapter() {
        	@Override
			public void widgetSelected(SelectionEvent e) {
        		setAdapt(adaptToggle.getSelection());
        	}
        });

        final Button saveNeededToggle = new Button(parent, SWT.CHECK);
        saveNeededToggle.setText("Save on close");
        saveNeededToggle.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                setSaveNeeded(saveNeededToggle.getSelection());
            }
        });
        saveNeededToggle.setSelection(saveNeeded);

        final Button saveAsToggle = new Button(parent, SWT.CHECK);
        saveAsToggle.setText("Save as allowed");
        saveAsToggle.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                setSaveAsAllowed(saveAsToggle.getSelection());
            }
        });
        saveAsToggle.setSelection(saveAsAllowed);
    }

	/**
	 * @param selection
	 */
	protected void setAdapt(boolean selection) {
		this.adapt = selection;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		callTrace.add("doSave" );
	}

	@Override
	public void doSaveAs() {
		callTrace.add("doSaveAs" );
	}

	@Override
	public boolean isDirty() {
		callTrace.add("isDirty" );
		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		callTrace.add("isSaveAsAllowed" );
		return saveAsAllowed ;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		callTrace.add("isSaveOnCloseNeeded" );
		return saveNeeded;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
        firePropertyChange(PROP_DIRTY);
	}

    public void setSaveAsAllowed(boolean isSaveAsAllowed) {
        this.saveAsAllowed = isSaveAsAllowed;
    }

    public void setSaveNeeded(boolean isSaveOnCloseNeeded) {
        this.saveNeeded = isSaveOnCloseNeeded;
    }

	@Override
	public Saveable[] getActiveSaveables() {
		// TODO Auto-generated method stub
		return getSaveables();
	}

	@Override
	public Saveable[] getSaveables() {
		Saveable[] result = new Saveable[1];
		result[0] = new DefaultSaveable(this){
			@Override
			public Object getAdapter(Class c) {
				final IFile[] someFile = {null};
				try {
					ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {

						@Override
						public boolean visit(IResource resource) {
							if (someFile[0] != null) {
								return false;
							}
							if (resource.getType() == IResource.FILE) {
								someFile[0] = (IFile) resource;
								return false;
							}
							return true;
						}
					});
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
				if (adapt && someFile[0] != null && c.equals(IFile.class)) {
					return someFile[0];
				}
				return super.getAdapter(c);
			};
		};
		return result ;
	}
}
