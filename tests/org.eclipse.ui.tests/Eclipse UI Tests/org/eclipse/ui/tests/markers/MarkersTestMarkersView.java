/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.provisional.views.markers.ExtendedMarkersView;

/**
 * MarkersTestMarkersView is the test suite version of the markers view.
 * 
 * @since 3.4
 * 
 */
public class MarkersTestMarkersView extends ExtendedMarkersView {

	/**
	 * Get the current markers for the receiver.
	 * 
	 * @return
	 */
	public IMarker[] getCurrentMarkers() {
		Method method;
		try {
			method = ExtendedMarkersView.class.getDeclaredMethod("getAllMarkers",
					new Class[0]);
			method.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
			return new IMarker[0];
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return new IMarker[0];
		}
		try {
			return (IMarker[]) method.invoke(this, new Object[0]);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return new IMarker[0];
	}

	/**
	 * Add a listener for the end of the update.
	 * 
	 * @param listener
	 */
	public void addUpdateFinishListener(IJobChangeListener listener) {
		getUpdateJobForListener().addJobChangeListener(listener);

	}

	/**
	 * Return the updateJob.
	 * 
	 * @return
	 */
	private Job getUpdateJobForListener() {
		Field field;
		try {
			field = ExtendedMarkersView.class.getDeclaredField("updateJob");
			field.setAccessible(true);
			return (Job) field.get(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Remove a listener for the end of the update.
	 * 
	 * @param listener
	 */
	public void removeUpdateFinishListener(IJobChangeListener listener) {
		getUpdateJobForListener().addJobChangeListener(listener);

	}

}
