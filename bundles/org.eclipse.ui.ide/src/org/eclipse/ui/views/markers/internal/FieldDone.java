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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * FieldDone is the field for the done marker in tasks.
 *
 */
public class FieldDone extends AbstractField {

	static final String DESCRIPTION_IMAGE_PATH = "obj16/header_complete.gif"; //$NON-NLS-1$

	static final String COMPLETE_IMAGE_PATH = "obj16/complete_tsk.gif"; //$NON-NLS-1$

	static final String INCOMPLETE_IMAGE_PATH = "obj16/incomplete_tsk.gif"; //$NON-NLS-1$

	private String description = MarkerMessages.completion_description;


	/**
	 * Create a new instance of the receiver
	 *
	 */
	public FieldDone() {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return getImage(DESCRIPTION_IMAGE_PATH);
	}
	
	/**
	 * Get the image at path.
	 * @param path
	 * @return Image
	 */
	private Image getImage(String path){
		return JFaceResources.getResources().createImageWithDefault(
				IDEWorkbenchPlugin
						.getIDEImageDescriptor(path));
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return getImage(DESCRIPTION_IMAGE_PATH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		if (obj == null || !(obj instanceof TaskMarker)) {
			return null;
		}
		TaskMarker marker = (TaskMarker) obj;
		int done = marker.getDone();
		if (done == -1) {
			return null;
		}
		if (done == 1) {
			return getImage(COMPLETE_IMAGE_PATH);
		}
		return getImage(INCOMPLETE_IMAGE_PATH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
	 *      java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof TaskMarker)
				|| !(obj2 instanceof TaskMarker)) {
			return 0;
		}
		TaskMarker marker1 = (TaskMarker) obj1;
		TaskMarker marker2 = (TaskMarker) obj2;
		int value1 = marker1.getDone();
		int value2 = marker2.getDone();
		return value1 - value2;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		return TableComparator.ASCENDING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getPreferredWidth()
	 */
	public int getPreferredWidth() {
		return 40;
	}

}
