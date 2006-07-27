package org.eclipse.ui.internal.texteditor;

import java.util.Iterator;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.rulers.RulerColumn;

public class AnnotationColumn extends RulerColumn {
	/** The width of the vertical ruler. */
	private final static int VERTICAL_RULER_WIDTH= 12;
	
	private final AnnotationRulerColumn fDelegate= new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, new DefaultMarkerAnnotationAccess());
	private final MarkerAnnotationPreferences fAnnotationPreferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
	private PropertyEventDispatcher fDispatcher;

	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		initialize();
		Control control= fDelegate.createControl(parentRuler, parentControl);
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		return control;
	}

	public Control getControl() {
		return fDelegate.getControl();
	}

	public int getWidth() {
		return fDelegate.getWidth();
	}

	public int hashCode() {
		return fDelegate.hashCode();
	}

	public void redraw() {
		fDelegate.redraw();
	}

	public void setFont(Font font) {
		fDelegate.setFont(font);
	}

	public void setModel(IAnnotationModel model) {
		fDelegate.setModel(model);
	}

	private void dispose() {
		if (fDispatcher != null) {
			fDispatcher.dispose();
			fDispatcher= null;
		}
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 */
	private void initialize() {
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			// initial set up
			for (Iterator iter2= fAnnotationPreferences.getAnnotationPreferences().iterator(); iter2.hasNext();) {
				AnnotationPreference preference= (AnnotationPreference)iter2.next();
				String key= preference.getVerticalRulerPreferenceKey();
				boolean showAnnotation= true;
				if (key != null && store.contains(key))
					showAnnotation= store.getBoolean(key);
				if (showAnnotation)
					fDelegate.addAnnotationType(preference.getAnnotationType());
			}
			fDelegate.addAnnotationType(Annotation.TYPE_UNKNOWN);
			
			// link to preference store
			IPropertyChangeListener pcl= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					String property= event.getProperty();
					AnnotationPreference annotationPreference= getVerticalRulerAnnotationPreference(property);
					if (annotationPreference != null && event.getNewValue() instanceof Boolean) {
						Object type= annotationPreference.getAnnotationType();
						if (((Boolean)event.getNewValue()).booleanValue())
							fDelegate.addAnnotationType(type);
						else
							fDelegate.removeAnnotationType(type);
						fDelegate.redraw();
					}
				}
			};
			store.addPropertyChangeListener(pcl);
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
}
