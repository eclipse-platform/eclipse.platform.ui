/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		initialize();
		Control control= fDelegate.createControl(parentRuler, parentControl);
		return control;
	}

	@Override
	public Control getControl() {
		return fDelegate.getControl();
	}

	@Override
	public int getWidth() {
		return fDelegate.getWidth();
	}

	@Override
	public void redraw() {
		fDelegate.redraw();
	}

	@Override
	public void setFont(Font font) {
		fDelegate.setFont(font);
	}

	@Override
	public void setModel(IAnnotationModel model) {
		fDelegate.setModel(model);
	}

	@Override
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
			for (Iterator<AnnotationPreference> iter2= fAnnotationPreferences.getAnnotationPreferences().iterator(); iter2.hasNext();) {
				AnnotationPreference preference= iter2.next();
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
				@Override
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

		Iterator<AnnotationPreference> e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= e.next();
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

	@Override
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			((IVerticalRulerInfoExtension) fDelegate).addVerticalRulerListener(listener);
	}

	@Override
	public IAnnotationHover getHover() {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			return ((IVerticalRulerInfoExtension) fDelegate).getHover();
		return null;
	}

	@Override
	public IAnnotationModel getModel() {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			return ((IVerticalRulerInfoExtension) fDelegate).getModel();
		return null;
	}

	@Override
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			((IVerticalRulerInfoExtension) fDelegate).removeVerticalRulerListener(listener);
	}

	@Override
	public int getLineOfLastMouseButtonActivity() {
		if (fDelegate instanceof IVerticalRulerInfo)
			return ((IVerticalRulerInfo)fDelegate).getLineOfLastMouseButtonActivity();
		return -1;
	}

	@Override
	public int toDocumentLineNumber(int y_coordinate) {
		if (fDelegate instanceof IVerticalRulerInfo)
			return ((IVerticalRulerInfo)fDelegate).toDocumentLineNumber(y_coordinate);
		return -1;
	}
}
