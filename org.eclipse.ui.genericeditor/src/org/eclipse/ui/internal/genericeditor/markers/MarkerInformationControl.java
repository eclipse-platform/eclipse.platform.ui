/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.markers;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDE.SharedImages;

public class MarkerInformationControl extends AbstractInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension2 {

	private IInformationControlCreator creator;

	public MarkerInformationControl(Shell parentShell, IInformationControlCreator creator) {
		super(parentShell, EditorsUI.getTooltipAffordanceString());
		this.creator = creator;
		create();
	}

	private Collection<IMarker> markers;
	private Composite parent;

	@Override
	public boolean hasContents() {
		return this.markers != null && !this.markers.isEmpty();
	}
	
	@Override
	protected void createContent(Composite parent) {
		parent.setLayout(new RowLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
		this.parent = parent;
	}

	private static Image getImage(IMarker marker) {
		switch (marker.getAttribute(IMarker.SEVERITY, -1)) {
		case IMarker.SEVERITY_ERROR: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		case IMarker.SEVERITY_WARNING: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		case IMarker.SEVERITY_INFO: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		default:
			break;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setInput(Object input) {
		this.markers = (List<IMarker>)input;
		for (IMarker marker : this.markers) {
			Composite markerComposite = new Composite(parent, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.verticalSpacing = 0;
			markerComposite.setLayout(gridLayout);
			Composite markerLine = new Composite(markerComposite, SWT.NONE);
			markerLine.setLayout(new RowLayout());
			Label markerImage = new Label(markerLine, SWT.NONE);
			markerImage.setImage(getImage(marker));
			Label markerLabel = new Label(markerLine, SWT.NONE);
			markerLabel.setText(marker.getAttribute(IMarker.MESSAGE, "missing message")); //$NON-NLS-1$
			for (IMarkerResolution resolution : IDE.getMarkerHelpRegistry().getResolutions(marker)) {
				Composite resolutionComposite = new Composite(markerComposite, SWT.NONE);
				GridData layoutData = new GridData();
				layoutData.horizontalIndent = 10;
				resolutionComposite.setLayoutData(layoutData);
				RowLayout rowLayout = new RowLayout();
				rowLayout.marginBottom = 0;
				resolutionComposite.setLayout(rowLayout);
				Label resolutionImage = new Label(resolutionComposite, SWT.NONE);
				// TODO: try to retrieve icon from QuickFix command
				Image resolutionPic = null; 
				if (resolution instanceof IMarkerResolution2) {
					resolutionPic = ((IMarkerResolution2) resolution).getImage();
				}
				if (resolutionPic == null) {
					resolutionPic = PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OPEN_MARKER);
				}
				resolutionImage.setImage(resolutionPic);
				Link resolutionLink = new Link(resolutionComposite, SWT.NONE);
				resolutionLink.setText("<A>" + resolution.getLabel() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
				resolutionLink.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Job resolutionJob = new Job("apply resolution - " + resolution.getLabel()) { //$NON-NLS-1$
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								resolution.run(marker);
								return Status.OK_STATUS;
							}
						};
						resolutionJob.setUser(true);
						resolutionJob.setSystem(true);
						resolutionJob.setPriority(Job.INTERACTIVE);
						resolutionJob.schedule();
						getShell().dispose();
					}
				});
			}
		}
		parent.pack(true);
	}
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				return creator.createInformationControl(parent);
			}
		};
	}
	
	@Override
	public Point computeSizeHint() {
		getShell().pack();
		return getShell().getSize();
	}
	
}