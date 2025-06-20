/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.markers;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.AbstractInformationControl;
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

	private final LinkedHashMap<IMarker, Composite> composites = new LinkedHashMap<>();

	public MarkerInformationControl(Shell parentShell, boolean showAffordanceString) {
		super(parentShell, showAffordanceString ? EditorsUI.getTooltipAffordanceString() : null);
		create();
	}

	private Collection<IMarker> markers;
	private Composite parent;

	@Override
	public boolean hasContents() {
		return this.markers != null && !this.markers.isEmpty();
	}

	@Override
	protected void createContent(Composite parentComposite) {
		parentComposite.setLayout(new RowLayout(SWT.VERTICAL));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parentComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		this.parent = parentComposite;
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
		this.composites.values().forEach(Composite::dispose);
		this.markers = (List<IMarker>)input;
		for (IMarker marker : this.markers) {
			Composite markerComposite = new Composite(parent, SWT.NONE);
			this.composites.put(marker, markerComposite);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.verticalSpacing = 0;
			gridLayout.horizontalSpacing = 0;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			markerComposite.setLayout(gridLayout);
			Composite markerLine = new Composite(markerComposite, SWT.NONE);
			RowLayout rowLayout = new RowLayout();
			rowLayout.marginTop = 0;
			rowLayout.marginBottom = 0;
			rowLayout.marginLeft = 0;
			rowLayout.marginRight = 0;
			markerLine.setLayout(rowLayout);
			IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry().getResolutions(marker);
			if(resolutions.length > 0) {
				Label markerImage = new Label(markerLine, SWT.NONE);
				markerImage.setImage(getImage(marker));
			}
			Label markerLabel = new Label(markerLine, SWT.NONE);
			String markerText = marker.getAttribute(IMarker.MESSAGE, "missing message"); //$NON-NLS-1$
			markerText = markerText.replace("&", "&&"); // Disable mnemonics //$NON-NLS-1$ //$NON-NLS-2$
			markerLabel.setText(markerText);
			for (IMarkerResolution resolution : resolutions) {
				Composite resolutionComposite = new Composite(markerComposite, SWT.NONE);
				GridData layoutData = new GridData();
				layoutData.horizontalIndent = 10;
				resolutionComposite.setLayoutData(layoutData);
				resolutionComposite.setLayout(new RowLayout());
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
				String resolutionText = resolution.getLabel().replace("&", "&&"); // Disable mnemonics //$NON-NLS-1$ //$NON-NLS-2$
				resolutionLink.setText("<A>" + resolutionText + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
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
		parent.layout(true);
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new MarkerHoverControlCreator(false);
	}

	@Override
	public Point computeSizeHint() {
		if (getShell().getChildren().length > 0) {
			// Do not pack the shell/control if it has no children, as it will size to 2,2
			getShell().pack();
		}
		return getShell().getSize();
	}

}