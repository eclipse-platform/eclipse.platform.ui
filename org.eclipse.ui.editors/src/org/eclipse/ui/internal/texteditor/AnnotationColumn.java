/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.Iterator;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

import org.eclipse.ui.editors.text.EditorsUI;

/**
 * The annotation ruler contribution. Encapsulates an {@link AnnotationRulerColumn} as a
 * contribution to the <code>rulerColumns</code> extension point. Instead of instantiating the
 * delegate itself, it {@link AbstractDecoratedTextEditor} creates it using its
 * <code>createAnnotationRulerColumn()</code> method and sets it via
 * {@link #setDelegate(IVerticalRulerColumn)}.
 *
 * @since 3.3
 */
public class AnnotationColumn extends AbstractContributedRulerColumn implements IVerticalRulerInfo, IVerticalRulerInfoExtension {
	/** The contribution id of the annotation ruler. */
	public static final String ID= "org.eclipse.ui.editors.columns.annotations"; //$NON-NLS-1$
	/** The width of the vertical ruler. */
	private final static int VERTICAL_RULER_WIDTH= 12;


	private IVerticalRulerColumn fDelegate;
	private final MarkerAnnotationPreferences fAnnotationPreferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
	private IPropertyChangeListener fPropertyListener;

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler, org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		initialize();
		Control control= fDelegate.createControl(parentRuler, parentControl);
		return control;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getControl()
	 */
	public Control getControl() {
		return fDelegate.getControl();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getWidth()
	 */
	public int getWidth() {
		return fDelegate.getWidth();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#redraw()
	 */
	public void redraw() {
		fDelegate.redraw();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font) {
		fDelegate.setFont(font);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		fDelegate.setModel(model);
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn#columnRemoved()
	 */
	public void columnRemoved() {
		if (fPropertyListener != null) {
			IPreferenceStore store= getPreferenceStore();
			if (store != null)
				store.removePropertyChangeListener(fPropertyListener);
			fPropertyListener= null;
		}
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 */
	private void initialize() {
		if (fDelegate == null)
			fDelegate= new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, new DefaultMarkerAnnotationAccess());
		IPreferenceStore store= getPreferenceStore();
		if (store != null && fDelegate instanceof AnnotationRulerColumn) {
			final AnnotationRulerColumn column= (AnnotationRulerColumn) fDelegate;
			// initial set up
			for (Iterator iter2= fAnnotationPreferences.getAnnotationPreferences().iterator(); iter2.hasNext();) {
				AnnotationPreference preference= (AnnotationPreference)iter2.next();
				String key= preference.getVerticalRulerPreferenceKey();
				boolean showAnnotation= true;
				if (key != null && store.contains(key))
					showAnnotation= store.getBoolean(key);
				if (showAnnotation)
					column.addAnnotationType(preference.getAnnotationType());
			}
			column.addAnnotationType(Annotation.TYPE_UNKNOWN);

			// link to preference store
			fPropertyListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					String property= event.getProperty();
					AnnotationPreference annotationPreference= getVerticalRulerAnnotationPreference(property);
					if (annotationPreference != null && property.equals(annotationPreference.getVerticalRulerPreferenceKey())) {
						Object type= annotationPreference.getAnnotationType();
						if (getPreferenceStore().getBoolean(property))
							column.addAnnotationType(type);
						else
							column.removeAnnotationType(type);
						column.redraw();
					}
				}
			};
			store.addPropertyChangeListener(fPropertyListener);
		}
	}

	/**
	 * Returns the annotation preference for which the given
	 * preference matches a vertical ruler preference key.
	 *
	 * @param preferenceKey the preference key string
	 * @return the annotation preference or <code>null</code> if none
	 */
	private AnnotationPreference getVerticalRulerAnnotationPreference(String preferenceKey) {
		if (preferenceKey == null)
			return null;

		Iterator e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			if (info != null && preferenceKey.equals(info.getVerticalRulerPreferenceKey()))
				return info;
		}
		return null;
	}

	private IPreferenceStore getPreferenceStore() {
		return EditorsUI.getPreferenceStore();
	}

	/**
	 * Sets the compatibility delegate. Called by {@link AbstractDecoratedTextEditor}.
	 *
	 * @param column the delegate column implementation
	 */
	public void setDelegate(IVerticalRulerColumn column) {
		Assert.isLegal(fDelegate == null);
		Assert.isLegal(column != null);
		fDelegate= column;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			((IVerticalRulerInfoExtension) fDelegate).addVerticalRulerListener(listener);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 */
	public IAnnotationHover getHover() {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			return ((IVerticalRulerInfoExtension) fDelegate).getHover();
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel() {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			return ((IVerticalRulerInfoExtension) fDelegate).getModel();
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			((IVerticalRulerInfoExtension) fDelegate).removeVerticalRulerListener(listener);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		if (fDelegate instanceof IVerticalRulerInfo)
			return ((IVerticalRulerInfo)fDelegate).getLineOfLastMouseButtonActivity();
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		if (fDelegate instanceof IVerticalRulerInfo)
			return ((IVerticalRulerInfo)fDelegate).toDocumentLineNumber(y_coordinate);
		return -1;
	}
}
