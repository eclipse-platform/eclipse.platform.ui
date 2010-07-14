/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.revisions.IRevisionRulerColumn;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension.RenderingMode;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDifferExtension;
import org.eclipse.jface.text.source.ILineDifferExtension2;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.quickdiff.QuickDiff;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

import org.eclipse.ui.editors.text.EditorsUI;

/**
 * The line number ruler contribution. Encapsulates a {@link LineNumberChangeRulerColumn} as a
 * contribution to the <code>rulerColumns</code> extension point. Instead of instantiating the
 * delegate itself, it calls <code>createLineNumberRulerColumn()</code> in
 * {@link AbstractDecoratedTextEditor} via {@link ICompatibilityForwarder} to maintain compatibility
 * with previous releases.
 *
 * @since 3.3
 */
public class LineNumberColumn extends AbstractContributedRulerColumn implements IVerticalRulerInfo, IVerticalRulerInfoExtension {
	/**
	 * Forwarder for preference checks and ruler creation. Needed to maintain the forwarded APIs in
	 * {@link AbstractDecoratedTextEditor}.
	 */
	public static interface ICompatibilityForwarder {
		IVerticalRulerColumn createLineNumberRulerColumn();
		boolean isQuickDiffEnabled();
		boolean isLineNumberRulerVisible();
	}

	/** The contribution id of the line number / change ruler. */
	public static final String ID= "org.eclipse.ui.editors.columns.linenumbers"; //$NON-NLS-1$

	private static final String FG_COLOR_KEY= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
	private static final String BG_COLOR_KEY= AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;
	private static final String USE_DEFAULT_BG_KEY= AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
	private final static String LINE_NUMBER_KEY= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
	private final static String REVISION_ASK_BEFORE_QUICKDIFF_SWITCH_KEY= AbstractDecoratedTextEditorPreferenceConstants.REVISION_ASK_BEFORE_QUICKDIFF_SWITCH;

	/**
	 * The delegate and implemenation of the ruler.
	 */
	private IVerticalRulerColumn fDelegate;
	/**
	 * The annotation preferences.
	 */
	private final MarkerAnnotationPreferences fAnnotationPreferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();

	/**
	 * Preference dispatcher that registers a single listener so we don't have to manage every
	 * single preference listener.
	 */
	private PropertyEventDispatcher fDispatcher;
	private ISourceViewer fViewer;
	private ICompatibilityForwarder fForwarder;

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler, org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		Assert.isTrue(fDelegate != null);
		ITextViewer viewer= parentRuler.getTextViewer();
		Assert.isLegal(viewer instanceof ISourceViewer);
		fViewer= (ISourceViewer) viewer;
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
		if (getQuickDiffPreference())
			fDelegate.setModel(model);
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
	 * @see org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn#columnRemoved()
	 */
	public void columnRemoved() {
		if (fDispatcher != null) {
			fDispatcher.dispose();
			fDispatcher= null;
		}
	}

	private IPreferenceStore getPreferenceStore() {
		return EditorsUI.getPreferenceStore();
	}

	private ISharedTextColors getSharedColors() {
		return EditorsUI.getSharedTextColors();
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 */
	private void initialize() {
		final IPreferenceStore store= getPreferenceStore();
		if (store == null)
			return;

		// initial set up
		updateForegroundColor(store, fDelegate);
		updateBackgroundColor(store, fDelegate);

		updateLineNumbersVisibility(fDelegate);
		updateQuickDiffVisibility(fDelegate);
		updateCharacterMode(store, fDelegate);
		updateRevisionRenderingMode(store, fDelegate);
		updateRevisionAuthorVisibility(store, fDelegate);
		updateRevisionIdVisibility(store, fDelegate);

		Map annotationPrefs= getAnnotationPreferenceMap();
		final AnnotationPreference changedPref= (AnnotationPreference) annotationPrefs.get("org.eclipse.ui.workbench.texteditor.quickdiffChange"); //$NON-NLS-1$
		final AnnotationPreference addedPref= (AnnotationPreference) annotationPrefs.get("org.eclipse.ui.workbench.texteditor.quickdiffAddition"); //$NON-NLS-1$
		final AnnotationPreference deletedPref= (AnnotationPreference) annotationPrefs.get("org.eclipse.ui.workbench.texteditor.quickdiffDeletion"); //$NON-NLS-1$
		updateChangedColor(changedPref, store, fDelegate);
		updateAddedColor(addedPref, store, fDelegate);
		updateDeletedColor(deletedPref, store, fDelegate);

		fDelegate.redraw();

		// listen to changes
		fDispatcher= new PropertyEventDispatcher(store);

		fDispatcher.addPropertyChangeListener(FG_COLOR_KEY, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateForegroundColor(store, fDelegate);
				fDelegate.redraw();
			}
		});
		IPropertyChangeListener backgroundHandler= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateBackgroundColor(store, fDelegate);
				fDelegate.redraw();
			}
		};
		fDispatcher.addPropertyChangeListener(BG_COLOR_KEY, backgroundHandler);
		fDispatcher.addPropertyChangeListener(USE_DEFAULT_BG_KEY, backgroundHandler);

		fDispatcher.addPropertyChangeListener(LINE_NUMBER_KEY, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				// only handle quick diff on/off information, but not ruler visibility (handled by AbstractDecoratedTextEditor)
				updateLineNumbersVisibility(fDelegate);
			}
		});

		fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateCharacterMode(store, fDelegate);
			}
		});

		fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_RENDERING_MODE, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateRevisionRenderingMode(store, fDelegate);
			}
		});

		fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_AUTHOR, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateRevisionAuthorVisibility(store, fDelegate);
			}
		});

		fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_REVISION, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateRevisionIdVisibility(store, fDelegate);
			}
		});

		fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateQuickDiffVisibility(fDelegate);
			}
		});

		if (changedPref != null) {
			fDispatcher.addPropertyChangeListener(changedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					updateChangedColor(changedPref, store, fDelegate);
					fDelegate.redraw();
				}
			});
		}
		if (addedPref != null) {
			fDispatcher.addPropertyChangeListener(addedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					updateAddedColor(addedPref, store, fDelegate);
					fDelegate.redraw();
				}
			});
		}
		if (deletedPref != null) {
			fDispatcher.addPropertyChangeListener(deletedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					updateDeletedColor(deletedPref, store, fDelegate);
					fDelegate.redraw();
				}
			});
		}
	}

	private Map getAnnotationPreferenceMap() {
		Map annotationPrefs= new HashMap();
		Iterator iter= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (iter.hasNext()) {
			AnnotationPreference pref= (AnnotationPreference) iter.next();
			Object type= pref.getAnnotationType();
			annotationPrefs.put(type, pref);
		}
		return annotationPrefs;
	}

	private void updateForegroundColor(IPreferenceStore store, IVerticalRulerColumn column) {
		RGB rgb=  getColorFromStore(store, FG_COLOR_KEY);
		if (rgb == null)
			rgb= new RGB(0, 0, 0);
		ISharedTextColors sharedColors= getSharedColors();
		if (column instanceof LineNumberRulerColumn)
			((LineNumberRulerColumn) column).setForeground(sharedColors.getColor(rgb));
	}

	private void updateBackgroundColor(IPreferenceStore store, IVerticalRulerColumn column) {
		// background color: same as editor, or system default
		RGB rgb;
		if (store.getBoolean(USE_DEFAULT_BG_KEY))
			rgb= null;
		else
			rgb= getColorFromStore(store, BG_COLOR_KEY);
		ISharedTextColors sharedColors= getSharedColors();
		if (column instanceof LineNumberRulerColumn)
			((LineNumberRulerColumn) column).setBackground(sharedColors.getColor(rgb));
	}

	private void updateChangedColor(AnnotationPreference pref, IPreferenceStore store, IVerticalRulerColumn column) {
		if (pref != null && column instanceof IChangeRulerColumn) {
			RGB rgb= getColorFromAnnotationPreference(store, pref);
			((IChangeRulerColumn) column).setChangedColor(getSharedColors().getColor(rgb));
		}
	}

	private void updateAddedColor(AnnotationPreference pref, IPreferenceStore store, IVerticalRulerColumn column) {
		if (pref != null && column instanceof IChangeRulerColumn) {
			RGB rgb= getColorFromAnnotationPreference(store, pref);
			((IChangeRulerColumn) column).setAddedColor(getSharedColors().getColor(rgb));
		}
	}

	private void updateDeletedColor(AnnotationPreference pref, IPreferenceStore store, IVerticalRulerColumn column) {
		if (pref != null && column instanceof IChangeRulerColumn) {
			RGB rgb= getColorFromAnnotationPreference(store, pref);
			((IChangeRulerColumn) column).setDeletedColor(getSharedColors().getColor(rgb));
		}
	}

	private void updateCharacterMode(IPreferenceStore store, IVerticalRulerColumn column) {
		if (column instanceof LineNumberChangeRulerColumn) {
			LineNumberChangeRulerColumn lncrc= (LineNumberChangeRulerColumn) column;
			lncrc.setDisplayMode(store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE));
		}
	}

	private void updateLineNumbersVisibility(IVerticalRulerColumn column) {
		if (column instanceof LineNumberChangeRulerColumn)
			((LineNumberChangeRulerColumn) column).showLineNumbers(getLineNumberPreference());
	}

	private void updateRevisionRenderingMode(IPreferenceStore store, IVerticalRulerColumn column) {
		if (column instanceof IRevisionRulerColumnExtension) {
			String option= store.getString(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_RENDERING_MODE);
			RenderingMode[] modes= { IRevisionRulerColumnExtension.AUTHOR, IRevisionRulerColumnExtension.AGE, IRevisionRulerColumnExtension.AUTHOR_SHADED_BY_AGE };
			for (int i= 0; i < modes.length; i++) {
				if (modes[i].name().equals(option)) {
					((IRevisionRulerColumnExtension) column).setRevisionRenderingMode(modes[i]);
					return;
				}
			}
		}
	}

	private void updateRevisionAuthorVisibility(IPreferenceStore store, IVerticalRulerColumn column) {
		if (column instanceof IRevisionRulerColumnExtension) {
			boolean show= store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_AUTHOR);
			((IRevisionRulerColumnExtension) column).showRevisionAuthor(show);
		}
	}

	private void updateRevisionIdVisibility(IPreferenceStore store, IVerticalRulerColumn column) {
		if (column instanceof IRevisionRulerColumnExtension) {
			boolean show= store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_REVISION);
			((IRevisionRulerColumnExtension) column).showRevisionId(show);
		}
	}

	private void updateQuickDiffVisibility(IVerticalRulerColumn column) {
		boolean show= getQuickDiffPreference();
		if (show == isShowingChangeInformation())
			return;

		if (show)
			installChangeRulerModel(column);
		else
			uninstallChangeRulerModel(column);
	}

	/**
	 * Returns whether the line number ruler column should be
	 * visible according to the preference store settings. Subclasses may override this
	 * method to provide a custom preference setting.
	 *
	 * @return <code>true</code> if the line numbers should be visible
	 */
	private boolean getLineNumberPreference() {
		if (fForwarder != null)
			return fForwarder.isLineNumberRulerVisible();
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(LINE_NUMBER_KEY) : false;
	}

	/**
	 * Returns whether quick diff info should be visible upon opening an editor
	 * according to the preference store settings.
	 *
	 * @return <code>true</code> if the line numbers should be visible
	 */
	private boolean getQuickDiffPreference() {
		if (fForwarder != null)
			return fForwarder.isQuickDiffEnabled();
		IPreferenceStore store= getPreferenceStore();
		boolean setting= store != null ? store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON) : false;
		if (!setting)
			return false;

		boolean modifiable;
		ITextEditor editor= getEditor();
		if (editor instanceof ITextEditorExtension2) {
			ITextEditorExtension2 ext= (ITextEditorExtension2) editor;
			modifiable= ext.isEditorInputModifiable();
		} else if (editor instanceof ITextEditorExtension) {
			ITextEditorExtension ext= (ITextEditorExtension) editor;
			modifiable= ext.isEditorInputReadOnly();
		} else if (editor != null) {
			modifiable= editor.isEditable();
		} else {
			modifiable= true;
		}
		return modifiable;
	}

	/**
	 * Extracts the color preference for the given preference from the given store.
	 * If the given store indicates that the default value is to be used, or
	 * the value stored in the preferences store is <code>null</code>,
	 * the value is taken from the <code>AnnotationPreference</code>'s default
	 * color value.
	 * <p>
	 * The return value is
	 * </p>
	 *
	 * @param store the preference store
	 * @param pref the annotation preference
	 * @return the RGB color preference, not <code>null</code>
	 */
	private static RGB getColorFromAnnotationPreference(IPreferenceStore store, AnnotationPreference pref) {
		String key= pref.getColorPreferenceKey();
		RGB rgb= null;
		if (store.contains(key)) {
			if (store.isDefault(key))
				rgb= pref.getColorPreferenceValue();
			else
				rgb= PreferenceConverter.getColor(store, key);
		}
		if (rgb == null)
			rgb= pref.getColorPreferenceValue();
		return rgb;
	}

	private static RGB getColorFromStore(IPreferenceStore store, String key) {
		RGB rgb= null;
		if (store.contains(key)) {
			if (store.isDefault(key))
				rgb= PreferenceConverter.getDefaultColor(store, key);
			else
				rgb= PreferenceConverter.getColor(store, key);
		}
		return rgb;
	}

	/**
	 * Ensures that quick diff information is displayed and the quick diff provider is the one with
	 * the specified id. If a different quick diff provider is in use, the user may be asked whether
	 * he wants to switch.
	 *
	 * @param diffProviderId the quick diff provider id to use
	 * @return <code>true</code> if quick diff could be enabled for the given id,
	 *         <code>false</code> otherwise
	 */
	private boolean ensureQuickDiffProvider(String diffProviderId) {
		if (!isShowingChangeInformation())
			installChangeRulerModel(fDelegate); // FIXME pass provider id

		IAnnotationModel annotationModel= fViewer.getAnnotationModel();
		IAnnotationModel oldDiffer= getDiffer();
		if (oldDiffer == null && annotationModel != null)
			return false; // quick diff is enabled, but no differ? not working for whatever reason

		if (annotationModel == null)
			annotationModel= new AnnotationModel();
		if (!(annotationModel instanceof IAnnotationModelExtension))
			return false;

		QuickDiff util= new QuickDiff();
		Object oldDifferId= util.getConfiguredQuickDiffProvider(oldDiffer);
		if (oldDifferId.equals(diffProviderId)) {
			if (oldDiffer instanceof ILineDifferExtension)
				((ILineDifferExtension) oldDiffer).resume();
			return true;
		}

		// Check whether the desired provider is available at all
		IAnnotationModel newDiffer= util.createQuickDiffAnnotationModel(getEditor(), diffProviderId);
		if (util.getConfiguredQuickDiffProvider(newDiffer).equals(oldDifferId)) {
			if (oldDiffer instanceof ILineDifferExtension)
				((ILineDifferExtension) oldDiffer).resume();
				return true;
		}

		// quick diff is showing with the wrong provider - ask the user whether he wants to switch
		IPreferenceStore store= EditorsUI.getPreferenceStore();
		if (oldDiffer != null && !store.getString(REVISION_ASK_BEFORE_QUICKDIFF_SWITCH_KEY).equals(MessageDialogWithToggle.ALWAYS)) {
			MessageDialogWithToggle toggleDialog= MessageDialogWithToggle.openOkCancelConfirm(
					fViewer.getTextWidget().getShell(),
					RulerMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_title,
					RulerMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_message,
					RulerMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_rememberquestion,
					true,
					store,
					REVISION_ASK_BEFORE_QUICKDIFF_SWITCH_KEY);
			if (toggleDialog.getReturnCode() != Window.OK)
				return false;
		}

		IAnnotationModelExtension modelExtension=(IAnnotationModelExtension) annotationModel;
		modelExtension.removeAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);


		modelExtension.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, newDiffer);

		if (fDelegate instanceof IChangeRulerColumn)
			((IChangeRulerColumn) fDelegate).setModel(annotationModel); // picks up the new model attachment

		return true;
	}

	/**
	 * Installs the differ annotation model with the current quick diff display.
	 *
	 * @param column the column to install the model on
	 */
	private void installChangeRulerModel(IVerticalRulerColumn column) {
		if (column instanceof IChangeRulerColumn) {
			IAnnotationModel model= getAnnotationModelWithDiffer();
			((IChangeRulerColumn) column).setModel(model);
			if (model != null) {
				ISourceViewer viewer= fViewer;
				if (viewer != null && viewer.getAnnotationModel() == null && column.getControl() != null)
					viewer.showAnnotations(true);
				}
		}
	}

	/**
	 * Uninstalls the differ annotation model from the current quick diff display.
	 *
	 * @param column the column to remove the model from
	 */
	private void uninstallChangeRulerModel(IVerticalRulerColumn column) {
		if (column instanceof IChangeRulerColumn)
			((IChangeRulerColumn) column).setModel(null);
		IAnnotationModel model= getDiffer();
		if (model instanceof ILineDifferExtension)
			((ILineDifferExtension) model).suspend();

		ISourceViewer viewer= fViewer;
		if (viewer != null && viewer.getAnnotationModel() == null)
			viewer.showAnnotations(false);
	}

	/**
	 * Returns the annotation model that contains the quick diff annotation model.
	 * <p>
	 * Extracts the line differ from the displayed document's annotation model. If none can be found,
	 * a new differ is created and attached to the annotation model.</p>
	 *
	 * @return the annotation model that contains the line differ, or <code>null</code> if none could be found or created
	 * @see IChangeRulerColumn#QUICK_DIFF_MODEL_ID
	 */
	private IAnnotationModel getAnnotationModelWithDiffer() {
		ISourceViewer viewer= fViewer;
		if (viewer == null)
			return null;

		IAnnotationModel m= viewer.getAnnotationModel();
		IAnnotationModelExtension model= null;
		if (m instanceof IAnnotationModelExtension)
			model= (IAnnotationModelExtension) m;

		IAnnotationModel differ= getDiffer();
		// create diff model if it doesn't
		if (differ == null) {
			IPreferenceStore store= getPreferenceStore();
			if (store != null) {
				String defaultId= store.getString(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
				differ= new QuickDiff().createQuickDiffAnnotationModel(getEditor(), defaultId);
				if (differ != null) {
					if (model == null)
						model= new AnnotationModel();
					model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, differ);
				}
			}
		} else if (differ instanceof ILineDifferExtension2) {
			if (((ILineDifferExtension2) differ).isSuspended())
				((ILineDifferExtension) differ).resume();
		} else if (differ instanceof ILineDifferExtension) {
			((ILineDifferExtension) differ).resume();
		}

		return (IAnnotationModel)model;
	}

	/**
	 * Extracts the line differ from the displayed document's annotation model. If none can be found,
	 * <code>null</code> is returned.
	 *
	 * @return the line differ, or <code>null</code> if none could be found
	 */
	private IAnnotationModel getDiffer() {
		// get annotation model extension
		ISourceViewer viewer= fViewer;
		if (viewer == null)
			return null;

		IAnnotationModel m= viewer.getAnnotationModel();
		if (m == null && fDelegate instanceof IChangeRulerColumn)
			m= ((IChangeRulerColumn)fDelegate).getModel();

		if (!(m instanceof IAnnotationModelExtension))
			return null;

		IAnnotationModelExtension model= (IAnnotationModelExtension)m;

		// get diff model if it exists already
		return model.getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
	}

	/**
	 * Sets the forwarder. Used by {@link AbstractDecoratedTextEditor} to maintain the contract of
	 * its {@link AbstractDecoratedTextEditor#createLineNumberRulerColumn} method.
	 *
	 * @param forwarder the forwarder
	 */
	public void setForwarder(ICompatibilityForwarder forwarder) {
		fForwarder= forwarder;
		fDelegate= forwarder.createLineNumberRulerColumn();
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 *
	 * @param rulerColumn the ruler column to be initialized
	 */
	public void initializeLineNumberRulerColumn(LineNumberRulerColumn rulerColumn) {
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			updateForegroundColor(store, rulerColumn);
			updateBackgroundColor(store, rulerColumn);
			updateLineNumbersVisibility(rulerColumn);
			rulerColumn.redraw();
		}
	}

	/**
	 * Returns <code>true</code> if the ruler is showing line numbers, <code>false</code> if it
	 * is only showing change information.
	 *
	 * @return <code>true</code> if line numbers are shown, <code>false</code> otherwise
	 */
	public boolean isShowingLineNumbers() {
		return fDelegate instanceof LineNumberChangeRulerColumn && ((LineNumberChangeRulerColumn) fDelegate).isShowingLineNumbers();
	}

	/**
	 * Returns <code>true</code> if the ruler is showing change information, <code>false</code>
	 * if it is only showing line numbers.
	 *
	 * @return <code>true</code> if change information is shown, <code>false</code> otherwise
	 */
	public boolean isShowingChangeInformation() {
		return fDelegate instanceof LineNumberChangeRulerColumn && ((LineNumberChangeRulerColumn) fDelegate).isShowingChangeInformation();
	}

	/**
	 * Shows revision information on the receiver.
	 *
	 * @param info the revision information to show
	 * @param quickDiffProviderId the id of the corresponding quick diff provider
	 */
	public void showRevisionInformation(RevisionInformation info, String quickDiffProviderId) {
		if (!ensureQuickDiffProvider(quickDiffProviderId))
			return;

		if (fDelegate instanceof IRevisionRulerColumn)
			((IRevisionRulerColumn) fDelegate).setRevisionInformation(info);
	}

	/**
	 * Hides revision information.
	 */
	public void hideRevisionInformation() {
		if (fDelegate instanceof IRevisionRulerColumn)
			((IRevisionRulerColumn) fDelegate).setRevisionInformation(null);
	}

	/**
	 * Returns <code>true</code> if the ruler is showing revision information, <code>false</code>
	 * if it is only showing line numbers.
	 *
	 * @return <code>true</code> if revision information is shown, <code>false</code> otherwise
	 */
	public boolean isShowingRevisionInformation() {
		if (fDelegate instanceof LineNumberChangeRulerColumn)
			return ((LineNumberChangeRulerColumn) fDelegate).isShowingRevisionInformation();
		return false;
	}

	/**
	 * Returns the selection provider of the revision column, <code>null</code> if none is
	 * available.
	 *
	 * @return the revision selection provider
	 */
	public ISelectionProvider getRevisionSelectionProvider() {
		if (fDelegate instanceof IRevisionRulerColumnExtension)
			return ((IRevisionRulerColumnExtension) fDelegate).getRevisionSelectionProvider();
		return null;
	}
}
