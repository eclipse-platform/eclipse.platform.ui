/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.Iterator;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.ChangeRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineChangeHover;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.texteditor.quickdiff.QuickDiff;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

/**
 * An intermediate editor comprising functionality not present in the leaner <code>AbstractTextEditor</code>,
 * but used in many heavy weight (and especially source editing) editors, such as line numbers, 
 * change ruler, overview ruler, print margins, current line highlighting, etc.
 * 
 * @since 3.0
 */
public abstract class ExtendedTextEditor extends StatusTextEditor {
	/**
	 * Preference key for showing the line number ruler.
	 */
	private final static String LINE_NUMBER_RULER= ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
	/**
	 * Preference key for the foreground color of the line numbers.
	 */
	private final static String LINE_NUMBER_COLOR= ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
	/**
	 * Preference key for showing the overview ruler.
	 */
	private final static String OVERVIEW_RULER= ExtendedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
	/**
	 * Preference key for highlighting current line.
	 */
	private final static String CURRENT_LINE= ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
	/**
	 * Preference key for highlight color of current line.
	 */
	private final static String CURRENT_LINE_COLOR= ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/**
	 * Preference key for showing print marging ruler.
	 */
	private final static String PRINT_MARGIN= ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN;
	/**
	 * Preference key for print margin ruler color.
	 */
	private final static String PRINT_MARGIN_COLOR= ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/**
	 * Preference key for print margin ruler column.
	 **/
	private final static String PRINT_MARGIN_COLUMN= ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;
	/**
	 * Preference key for unknown annotation indication in overview ruler.
	 * @since 2.1
	 **/
	private final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER= ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;
	/**
	 * Preference key for unknown annotation indication.
	 **/
	private final static String UNKNOWN_INDICATION= ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION;
	/**
	 * Preference key for unknown annotation color.
	 **/
	private final static String UNKNOWN_INDICATION_COLOR= ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;
	/**
	 * The annotation preferences.
	 */
	private MarkerAnnotationPreferences fAnnotationPreferences;
	

	/** 
	 * The overview ruler of this editor.
	 * 
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons and will be made <code>private</code> soon. Use 
	 * {@link #getOverviewRuler()} instead.</p>
	 */
	protected IOverviewRuler fOverviewRuler;
	
	/**
	 * Helper for accessing annotation from the perspective of this editor.
	 * 
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons and will be made <code>private</code> soon. Use 
	 * {@link #getAnnotationAccess()} instead.</p>
	 */
	protected IAnnotationAccess fAnnotationAccess;
	
	/**
	 * Helper for managing the decoration support of this editor's viewer.
	 * 
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons and will be made <code>private</code> soon. Use 
	 * {@link #getSourceViewerDecorationSupport(ISourceViewer)} instead.</p>
	 */
	protected SourceViewerDecorationSupport fSourceViewerDecorationSupport;
	
	/**
	 * The line number column.
	 * 
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons and will be made <code>private</code> soon. Use 
	 * {@link AbstractTextEditor#getVerticalRuler()} to access the vertical bar instead.</p>
	 */
	protected LineNumberRulerColumn fLineNumberRulerColumn;
	
	/**
	 * The change ruler column.
	 */
	private IChangeRulerColumn fChangeRulerColumn;
	/**
	 * Whether quick diff information is displayed, either on a change ruler or the line number ruler.
	 */
	private boolean fIsChangeInformationShown;
	
	/**
	 * Creates a new text editor.
	 */
	public ExtendedTextEditor() {
		super();
		initializeKeyBindingScopes();
		initializeEditor();
	}
	
	/**
	 * Initializes this editor.
	 */
	protected void initializeEditor() {
		fAnnotationPreferences= new MarkerAnnotationPreferences();
		setRangeIndicator(new DefaultRangeIndicator());
		setPreferenceStore(TextEditorPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Initializes the key binding scopes of this editor.
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope" });  //$NON-NLS-1$
	}
	
	/*
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (fSourceViewerDecorationSupport != null) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport= null;
		}
		
		fAnnotationAccess= null;
		fAnnotationPreferences= null;
		
		super.dispose();
	}

	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		ISourceViewer viewer= new SourceViewer(parent, ruler, getOverviewRuler(), isPrefOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		
		return viewer;
	}

	protected ISharedTextColors getSharedColors() {
		ISharedTextColors sharedColors= TextEditorPlugin.getDefault().getSharedTextColors();
		return sharedColors;
	}

	protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors) {
		IOverviewRuler ruler= new OverviewRuler(getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors);
		Iterator e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference preference= (AnnotationPreference) e.next();
			if (preference.contributesToHeader())
				ruler.addHeaderAnnotationType(preference.getAnnotationType());
		}
		return ruler;
	}

	/**
	 * Creates the annotation access for this editor.
	 * 
	 * @return the created annotation access
	 */
	protected IAnnotationAccess createAnnotationAccess() {
		return new DefaultMarkerAnnotationAccess(fAnnotationPreferences);
	}

	/**
	 * Configures the decoration support for this editor's the source viewer. Subclasses may override this
	 * method, but should call their superclass' implementation at some point.
	 */
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {

		Iterator e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext())
			support.setAnnotationPreference((AnnotationPreference) e.next());
		support.setAnnotationPainterPreferenceKeys(DefaultMarkerAnnotationAccess.UNKNOWN, UNKNOWN_INDICATION_COLOR, UNKNOWN_INDICATION, UNKNOWN_INDICATION_IN_OVERVIEW_RULER, 0);
		
		support.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
		support.setMarginPainterPreferenceKeys(PRINT_MARGIN, PRINT_MARGIN_COLOR, PRINT_MARGIN_COLUMN);
		support.setSymbolicFontName(getFontPropertyPreferenceKey());
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (fSourceViewerDecorationSupport != null)
			fSourceViewerDecorationSupport.install(getPreferenceStore());
		
		if (isPrefQuickDiffAlwaysOn())
			showChangeInformation(true);
	}
	
	/**
	 * Tells whether the overview ruler is visible.
	 */
	protected boolean isPrefOverviewRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(OVERVIEW_RULER) : false;
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension3#showChangeInformation(boolean)
	 */
	public void showChangeInformation(boolean show) {
		if (show == fIsChangeInformationShown)
			return;
		
		if (fIsChangeInformationShown) {
			uninstallChangeRulerModel();
			showChangeRuler(false); // hide change ruler if its displayed - if the line number ruler is showing, only the colors get removed by deinstalling the model
		} else {
			ensureChangeInfoCanBeDisplayed();
			installChangeRulerModel();
		}
		
		fIsChangeInformationShown= show;
	}

	/**
	 * Installs the differ annotation model with the current quick diff display. 
	 */
	private void installChangeRulerModel() {
		IChangeRulerColumn column= getChangeColumn();
		if (column != null)
			column.setModel(getOrCreateDiffer());
	}

	/**
	 * Uninstalls the differ annotation model from the current quick diff display.
	 */
	private void uninstallChangeRulerModel() {
		IChangeRulerColumn column= getChangeColumn();
		if (column != null)
			column.setModel(null);
	}

	/**
	 * Ensures that either the line number display is a <code>LineNumberChangeRuler</code> or
	 * a separate change ruler gets displayed.
	 */
	private void ensureChangeInfoCanBeDisplayed() {
		if (isPrefLineNumberRulerVisible()) {
			if (!(fLineNumberRulerColumn instanceof IChangeRulerColumn)) {
				hideLineNumberRuler();
				// HACK: set state already so a change ruler is created. Not needed once always a change line number bar gets installed
				fIsChangeInformationShown= true;
				showLineNumberRuler();
			}
		} else 
			showChangeRuler(true);
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension3#isChangeInformationShowing()
	 */
	public boolean isChangeInformationShowing() {
		return fIsChangeInformationShown;
	}
	
	/**
	 * Extracts the line differ from the displayed document's annotation model. If none can be found,
	 * a new differ is created and attached to the annotation model.
	 * 
	 * @return the linediffer, or <code>null</code> if none could be found or created.
	 */
	private IAnnotationModel getOrCreateDiffer() {
		// get annotation model extension
		ISourceViewer viewer= getSourceViewer();
		if (viewer == null)
			return null;
			
		IAnnotationModel m= viewer.getAnnotationModel();
		IAnnotationModelExtension model;
		if (m instanceof IAnnotationModelExtension)
			model= (IAnnotationModelExtension) m;
		else
			return null;
		
		// get diff model if it exists already
		IAnnotationModel differ= model.getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
		
		// create diff model if it doesn't
		if (differ == null) {
			String defaultId= getPreferenceStore().getString(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
			differ= new QuickDiff().createQuickDiffAnnotationModel(this, defaultId);
			if (differ != null)
				model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, differ);
		}
		
		return differ;
	}

	/**
	 * Returns the <code>IChangeRulerColumn</code> of this editor, or <code>null</code> if there is none. Either
	 * the line number bar or a separate change ruler column can be returned.
	 * 
	 * @return an instance of <code>IChangeRulerColumn</code> or <code>null</code>.
	 */
	private IChangeRulerColumn getChangeColumn() {
		if (fChangeRulerColumn != null)
			return fChangeRulerColumn;
		else if (fLineNumberRulerColumn instanceof IChangeRulerColumn)
			return (IChangeRulerColumn) fLineNumberRulerColumn;
		else
			return null;
	}

	/**
	 * Sets the display state of the separate change ruler column (not the quick diff display on
	 * the line number ruler column) to <code>show</code>.
	 * 
	 * @param show <code>true</code> if the change ruler column should be shown, <code>false</code> if it should be hidden
	 */
	private void showChangeRuler(boolean show) {
		IVerticalRuler v= getVerticalRuler();
		if (v instanceof CompositeRuler) {
			CompositeRuler c= (CompositeRuler) v;
			if (show && fChangeRulerColumn == null)
				c.addDecorator(1, createChangeRulerColumn());
			else if (!show && fChangeRulerColumn != null) {
				c.removeDecorator(fChangeRulerColumn);
				fChangeRulerColumn= null;
			}
		}
	}

	/**
	 * Shows the line number ruler column.
	 */
	private void showLineNumberRuler() {
		showChangeRuler(false);
		if (fLineNumberRulerColumn == null) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.addDecorator(1, createLineNumberRulerColumn());
			}
		}
	}
	
	/**
	 * Hides the line number ruler column.
	 */
	private void hideLineNumberRuler() {
		if (fLineNumberRulerColumn != null) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.removeDecorator(fLineNumberRulerColumn);
			}
			fLineNumberRulerColumn = null;
		}
		if (fIsChangeInformationShown)
			showChangeRuler(true);
	}
	
	/**
	 * Returns whether the line number ruler column should be 
	 * visible according to the preference store settings. Subclasses may override this
	 * method to provide a custom preference setting.
	 * 
	 * @return <code>true</code> if the line numbers should be visible
	 */
	protected boolean isPrefLineNumberRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(LINE_NUMBER_RULER) : false;
	}

	/**
	 * Returns whether quick diff info should be visible upon opening an editor 
	 * according to the preference store settings.
	 * 
	 * @return <code>true</code> if the line numbers should be visible
	 */
	protected boolean isPrefQuickDiffAlwaysOn() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON);
	}
	
	/**
	 * Initializes the given line number ruler column from the preference store.
	 * 
	 * @param rulerColumn the ruler column to be initialized
	 */
	protected void initializeLineNumberRulerColumn(LineNumberRulerColumn rulerColumn) {
		ISharedTextColors sharedColors= getSharedColors();
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
		
			RGB rgb=  null;
			// foreground color
			if (store.contains(LINE_NUMBER_COLOR)) {
				if (store.isDefault(LINE_NUMBER_COLOR))
					rgb= PreferenceConverter.getDefaultColor(store, LINE_NUMBER_COLOR);
				else
					rgb= PreferenceConverter.getColor(store, LINE_NUMBER_COLOR);
			}
			if (rgb == null)
				rgb= new RGB(0, 0, 0);
			rulerColumn.setForeground(sharedColors.getColor(rgb));
			
			
			rgb= null;
			// background color
			if (!store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
				if (store.contains(PREFERENCE_COLOR_BACKGROUND)) {
					if (store.isDefault(PREFERENCE_COLOR_BACKGROUND))
						rgb= PreferenceConverter.getDefaultColor(store, PREFERENCE_COLOR_BACKGROUND);
					else
						rgb= PreferenceConverter.getColor(store, PREFERENCE_COLOR_BACKGROUND);
				}
			}
			if (rgb == null)
				rgb= new RGB(255, 255, 255);
			rulerColumn.setBackground(sharedColors.getColor(rgb));			
			rulerColumn.redraw();
		}
	}
	
	/**
	 * Initializes the given change ruler column from the preference store.
	 * 
	 * @param changeColumn the ruler column to be initialized
	 */
	private void initializeChangeRulerColumn(IChangeRulerColumn changeColumn) {
		ISharedTextColors sharedColors= getSharedColors();
		IPreferenceStore store= getPreferenceStore();
	
		if (store != null) {
			ISourceViewer v= getSourceViewer();
			if (v != null && v.getAnnotationModel() != null) {
				changeColumn.setModel(v.getAnnotationModel());
			}
			
			Iterator iter= fAnnotationPreferences.getAnnotationPreferences().iterator();
			while (iter.hasNext()) {
				AnnotationPreference pref= (AnnotationPreference) iter.next();
				
				if ("org.eclipse.quickdiff.changeindication".equals(pref.getMarkerType())) { //$NON-NLS-1$
					RGB rgb= getColorPreference(store, pref);
					changeColumn.setChangedColor(sharedColors.getColor(rgb));
				} else if ("org.eclipse.quickdiff.additionindication".equals(pref.getMarkerType())) { //$NON-NLS-1$
					RGB rgb= getColorPreference(store, pref);
					changeColumn.setAddedColor(sharedColors.getColor(rgb));
				} else if ("org.eclipse.quickdiff.deletionindication".equals(pref.getMarkerType())) { //$NON-NLS-1$
					RGB rgb= getColorPreference(store, pref);
					changeColumn.setDeletedColor(sharedColors.getColor(rgb));
				}
			}
			
			if (changeColumn instanceof LineNumberChangeRulerColumn) {
				LineNumberChangeRulerColumn lncrc= (LineNumberChangeRulerColumn) changeColumn;
				lncrc.setDisplayMode(store.getBoolean(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE));
			}
		}
	
		changeColumn.redraw();
	}

	/**
	 * Extracts the color preference for <code>pref</code> from <code>store</code>. If <code>store</code>
	 * indicates that the default value is to be used, or the value stored in the preferences store is <code>null</code>,
	 * the value is taken from the <code>AnnotationPreference</code>'s default color value.
	 * 
	 * <p>The return value is never <code>null</code></p>
	 * 
	 * @param store
	 * @param pref
	 * @return
	 */
	private RGB getColorPreference(IPreferenceStore store, AnnotationPreference pref) {
		RGB rgb= null;
		if (store.contains(pref.getColorPreferenceKey())) {
			if (store.isDefault(pref.getColorPreferenceKey()))
				rgb= pref.getColorPreferenceValue();
			else
				rgb= PreferenceConverter.getColor(store, pref.getColorPreferenceKey());
		}
		if (rgb == null)
			rgb= pref.getColorPreferenceValue();
		return rgb;
	}

	/**
	 * Creates a new line number ruler column that is appropriately initialized.
	 */
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		if (isPrefQuickDiffAlwaysOn() || isChangeInformationShowing()) {
			LineNumberChangeRulerColumn column= new LineNumberChangeRulerColumn();
			column.setHover(createChangeHover());
			initializeChangeRulerColumn(column);
			fLineNumberRulerColumn= column;
		} else {
			fLineNumberRulerColumn= new LineNumberRulerColumn();
		}
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		return fLineNumberRulerColumn;
	}
	
	/**
	 * Creates and returns a <code>LineChangeHover</code> to be used on this editor's change
	 * ruler column. This default implementation returns a plain <code>LineChangeHover</code>.
	 * Subclasses may override.
	 * 
	 * @return the change hover to be used by this editors quick diff display
	 */
	protected LineChangeHover createChangeHover() {
		return new LineChangeHover();
	}

	/**
	 * Creates a new change ruler column for quick diff display independent of the 
	 * line number ruler column
	 * 
	 * @return a new change ruler column
	 */
	protected IChangeRulerColumn createChangeRulerColumn() {
		IChangeRulerColumn column= new ChangeRulerColumn();
		column.setHover(createChangeHover());
		fChangeRulerColumn= column;
		initializeChangeRulerColumn(fChangeRulerColumn);
		return fChangeRulerColumn;
	}
	
	/*
	 * @see AbstractTextEditor#createVerticalRuler()
	 */
	protected final IVerticalRuler createVerticalRuler() {
		return createCompositeRuler();
	}
	
	/**
	 * Creates a composite ruler to be used as the vertical ruler by this editor.
	 * Subclasses may re-implement this method.
	 *
	 * @return the vertical ruler
	 */
	protected CompositeRuler createCompositeRuler() {
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH));
		
		if (isPrefLineNumberRulerVisible())
			ruler.addDecorator(1, createLineNumberRulerColumn());
		else if (isPrefQuickDiffAlwaysOn())
			ruler.addDecorator(1, createChangeRulerColumn());
			
		return ruler;
	}
	
	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		try {			

			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null)
				return;
				
			String property= event.getProperty();	
			
			if (fSourceViewerDecorationSupport != null && fOverviewRuler != null && OVERVIEW_RULER.equals(property))  {
				if (isPrefOverviewRulerVisible())
					showOverviewRuler();
				else
					hideOverviewRuler();
				return;
			}
			
			if (LINE_NUMBER_RULER.equals(property)) {
				if (isPrefLineNumberRulerVisible())
					showLineNumberRuler();
				else
					hideLineNumberRuler();
				return;
			}

			if (fLineNumberRulerColumn != null
				&&	(LINE_NUMBER_COLOR.equals(property) 
				||	PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)
				||	PREFERENCE_COLOR_BACKGROUND.equals(property))) {
					
				initializeLineNumberRulerColumn(fLineNumberRulerColumn);
			}
			
			if (fLineNumberRulerColumn instanceof LineNumberChangeRulerColumn
					&& ExtendedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE.equals(property)) {
				initializeChangeRulerColumn(getChangeColumn());
			}

			AnnotationPreference pref= getAnnotationPreference(property);
			if (pref != null) {
				IChangeRulerColumn column= getChangeColumn();
				if (column != null) {
					if ("org.eclipse.quickdiff.changeindication".equals(pref.getMarkerType()) //$NON-NLS-1$
							|| "org.eclipse.quickdiff.additionindication".equals(pref.getMarkerType()) //$NON-NLS-1$
							|| "org.eclipse.quickdiff.deletionindication".equals(pref.getMarkerType())) { //$NON-NLS-1$
						initializeChangeRulerColumn(column);
					}
				}
			}			
			
							
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	private AnnotationPreference getAnnotationPreference(String colorKey) {
		for (Iterator iter= fAnnotationPreferences.getAnnotationPreferences().iterator(); iter.hasNext();) {
			AnnotationPreference pref= (AnnotationPreference) iter.next();
			if (colorKey.equals(pref.getColorPreferenceKey()))
				return pref;
		}
		return null;
	}

	protected void showOverviewRuler() {
		if (fOverviewRuler != null) {
			if (getSourceViewer() instanceof ISourceViewerExtension) {
				((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(true);
				fSourceViewerDecorationSupport.updateOverviewDecorations();
			}
		}
	}

	protected void hideOverviewRuler() {
		if (getSourceViewer() instanceof ISourceViewerExtension) {
			fSourceViewerDecorationSupport.hideAnnotationOverview();
			((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(false);
		}
	}
	/**
	 * 
	 * 
	 * @return
	 */
	protected IAnnotationAccess getAnnotationAccess() {
		if (fAnnotationAccess == null)
			fAnnotationAccess= createAnnotationAccess();
		return fAnnotationAccess;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected IOverviewRuler getOverviewRuler() {
		if (fOverviewRuler == null)
			fOverviewRuler= createOverviewRuler(getSharedColors());
		return fOverviewRuler;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(ISourceViewer viewer) {
		if (fSourceViewerDecorationSupport == null) {
			fSourceViewerDecorationSupport= new SourceViewerDecorationSupport(viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors());
			configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
		}
		return fSourceViewerDecorationSupport;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected MarkerAnnotationPreferences getAnnotationPreferences() {
		return fAnnotationPreferences;
	}

}
