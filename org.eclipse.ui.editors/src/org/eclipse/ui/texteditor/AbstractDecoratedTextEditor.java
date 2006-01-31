/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.Iterator;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IUndoContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.revisions.IRevisionRulerColumn;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.ChangeRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDifferExtension;
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

import org.eclipse.ui.editors.text.DefaultEncodingSupport;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.IEncodingSupport;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.internal.editors.quickdiff.CompositeRevertAction;
import org.eclipse.ui.internal.editors.quickdiff.RestoreAction;
import org.eclipse.ui.internal.editors.quickdiff.RevertBlockAction;
import org.eclipse.ui.internal.editors.quickdiff.RevertLineAction;
import org.eclipse.ui.internal.editors.quickdiff.RevertSelectionAction;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.TextChangeHover;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.operations.NonLocalUndoUserApprover;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.quickdiff.QuickDiff;
import org.eclipse.ui.views.markers.MarkerViewUtil;

/**
 * An intermediate editor comprising functionality not present in the leaner <code>AbstractTextEditor</code>,
 * but used in many heavy weight (and especially source editing) editors, such as line numbers,
 * change ruler, overview ruler, print margins, current line highlighting, etc.
 *
 * @since 3.0
 */
public abstract class AbstractDecoratedTextEditor extends StatusTextEditor {

	/**
	 * Preference key for showing the line number ruler.
	 */
	private final static String LINE_NUMBER_RULER= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
	/**
	 * Preference key for the foreground color of the line numbers.
	 */
	private final static String LINE_NUMBER_COLOR= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
	/**
	 * Preference key for showing the overview ruler.
	 */
	private final static String OVERVIEW_RULER= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
	/**
	 * Preference key for highlighting current line.
	 */
	private final static String CURRENT_LINE= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
	/**
	 * Preference key for highlight color of current line.
	 */
	private final static String CURRENT_LINE_COLOR= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/**
	 * Preference key for showing print margin ruler.
	 */
	private final static String PRINT_MARGIN= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN;
	/**
	 * Preference key for print margin ruler color.
	 */
	private final static String PRINT_MARGIN_COLOR= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/**
	 * Preference key for print margin ruler column.
	 */
	private final static String PRINT_MARGIN_COLUMN= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;
	/**
	 * Preference key to get whether the overwrite mode is disabled.
	 * @since 3.1
	 */
	private final static String DISABLE_OVERWRITE_MODE= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE;
	/**
	 * Preference key to get whether the overwrite mode is disabled.
	 * @since 3.2
	 */
	private final static String REVISION_ASK_BEFORE_QUICKDIFF_SWITCH= AbstractDecoratedTextEditorPreferenceConstants.REVISION_ASK_BEFORE_QUICKDIFF_SWITCH;

	/**
	 * Adapter class for <code>IGotoMarker</code>.
	 */
	private class GotoMarkerAdapter implements IGotoMarker {
		public void gotoMarker(IMarker marker) {
			AbstractDecoratedTextEditor.this.gotoMarker(marker);
		}
	}

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
	 * The annotation ruler column used in the vertical ruler.
	 */
	private AnnotationRulerColumn fAnnotationRulerColumn;
	/**
	 * The editor's implicit document provider.
	 */
	private IDocumentProvider fImplicitDocumentProvider;
	/**
	 * The editor's goto marker adapter.
	 */
	private Object fGotoMarkerAdapter= new GotoMarkerAdapter();
	/**
	 * Indicates whether this editor is updating views that show markers.
	 * @see #updateMarkerViews(Annotation)
	 * @since 3.2
	 */
	protected boolean fIsUpdatingMarkerViews= false;
	/**
	 * Whether quick diff information is displayed, either on a change ruler or the line number
	 * ruler.
	 * @since 3.2
	 */
	private boolean fIsRevisionInformationShown;

	/**
	 * Creates a new text editor.
	 */
	public AbstractDecoratedTextEditor() {
		super();
		fAnnotationPreferences= new MarkerAnnotationPreferences();
		setRangeIndicator(new DefaultRangeIndicator());
		initializeKeyBindingScopes();
		initializeEditor();
	}

	/**
	 * Initializes this editor. Subclasses may re-implement. If sub-classes do
	 * not change the contract, this method should not be extended, i.e. do not
	 * call <code>super.initializeEditor()</code> in order to avoid the
	 * temporary creation of objects that are immediately overwritten by
	 * subclasses.
	 */
	protected void initializeEditor() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
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
		fAnnotationRulerColumn= null;

		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {

		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());

		ISourceViewer viewer= new SourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	protected ISharedTextColors getSharedColors() {
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
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
		return new DefaultMarkerAnnotationAccess();
	}

	/**
	 * Configures the decoration support for this editor's source viewer. Subclasses may override this
	 * method, but should call their superclass' implementation at some point.
	 *
	 * @param support the decoration support to configure
	 */
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {

		Iterator e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext())
			support.setAnnotationPreference((AnnotationPreference) e.next());

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

		if (!isOverwriteModeEnabled())
			enableOverwriteMode(false);
		
		if (!isRangeIndicatorEnabled()) {
			getSourceViewer().removeRangeIndication();
			getSourceViewer().setRangeIndicator(null);
		}
	}


	/*
	 * @see org.eclipse.ui.texteditor.StatusTextEditor#createStatusControl(org.eclipse.swt.widgets.Composite, org.eclipse.core.runtime.IStatus)
	 * @since 3.1
	 */
	protected Control createStatusControl(Composite parent, final IStatus status) {
		Object adapter= getAdapter(IEncodingSupport.class);
		DefaultEncodingSupport encodingSupport= null;
		if (adapter instanceof DefaultEncodingSupport)
			encodingSupport= (DefaultEncodingSupport)adapter;

		if (encodingSupport == null || !encodingSupport.isEncodingError(status))
			return super.createStatusControl(parent, status);

		Shell shell= getSite().getShell();
		Display display= shell.getDisplay();
		Color bgColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		Color fgColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);

		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setBackground(bgColor);
		composite.setForeground(fgColor);

		Control control= super.createStatusControl(composite, status);
		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite buttonComposite= new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
		buttonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonComposite.setBackground(bgColor);
		buttonComposite.setForeground(fgColor);

		encodingSupport.createStatusEncodingChangeControl(buttonComposite, status);
//
//		Button button= new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
//		button.setText(action.getText());
//		button.addSelectionListener(new SelectionAdapter() {
//			/*
//			 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
//			 */
//			public void widgetSelected(SelectionEvent e) {
//				action.run();
//			}
//		});
//
//		Label filler= new Label(buttonComposite, SWT.NONE);
//		filler.setLayoutData(new GridData(GridData.FILL_BOTH));
//		filler.setBackground(bgColor);
//
		return composite;
	}

	/**
	 * Tells whether the overview ruler is visible.
	 *
	 * @return whether the overview ruler is visible
	 */
	protected boolean isOverviewRulerVisible() {
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
			showChangeRuler(false); // hide change ruler if its displayed - if the line number ruler is showing, only the colors get removed by de-installing the model
		} else {
			ensureChangeInfoCanBeDisplayed();
			installChangeRulerModel();
		}

		if (getChangeColumn() != null && getChangeColumn().getModel() != null)
			fIsChangeInformationShown= true;
		else
			fIsChangeInformationShown= false;
	}
	
	/**
	 * Shows revision information in this editor.
	 * <p>
	 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
	 * </p>
	 * 
	 * @param info the revision information to display
	 * @param quickDiffProviderId the quick diff provider that matches the source of the revision
	 *        information
	 * @since 3.2
	 */
	public void showRevisionInformation(RevisionInformation info, String quickDiffProviderId) {
		if (!ensureQuickDiffProvider(quickDiffProviderId))
			return;
		
		IRevisionRulerColumn revisionColumn= getRevisionColumn();
		if (revisionColumn == null)
			return;
		
		revisionColumn.setRevisionInformation(info);
		fIsRevisionInformationShown= true;
	}
	
	/**
	 * Hides revision information in this editor.
	 * <p>
	 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
	 * </p>
	 * 
	 * @since 3.2
	 */
	private void hideRevisionInformation() {
		if (fChangeRulerColumn instanceof IRevisionRulerColumn)
			((IRevisionRulerColumn) fChangeRulerColumn).setRevisionInformation(null);
		if (fLineNumberRulerColumn instanceof IRevisionRulerColumn)
			((IRevisionRulerColumn) fLineNumberRulerColumn).setRevisionInformation(null);
		
		fIsRevisionInformationShown= false;
	}
	
	/**
	 * Returns the revision ruler column of this editor, creating one if needed.
	 * 
	 * @return the revision ruler column of this editor
	 * @since 3.2
	 */
	private IRevisionRulerColumn getRevisionColumn() {
		if (fChangeRulerColumn instanceof IRevisionRulerColumn)
			return (IRevisionRulerColumn) fChangeRulerColumn;
		
		if (fLineNumberRulerColumn instanceof IRevisionRulerColumn)
			return (IRevisionRulerColumn) fLineNumberRulerColumn;
		
		return null;
	}

	private boolean ensureQuickDiffProvider(String diffProviderId) {
		ISourceViewer viewer= getSourceViewer();
		if (viewer == null)
			return false;

		if (!fIsChangeInformationShown) {
			ensureChangeInfoCanBeDisplayed();
			installChangeRulerModel();
		}
		
		IAnnotationModel oldDiffer= getDiffer();
		if (oldDiffer == null)
			return false; // quick diff is enabled, but no differ? not working for whatever reason

		QuickDiff util= new QuickDiff();
		if (util.getConfiguredQuickDiffProvider(oldDiffer).equals(diffProviderId)) {
			if (oldDiffer instanceof ILineDifferExtension)
				((ILineDifferExtension) oldDiffer).resume();
			return true;
		}
		
		// quick diff is showing with the wrong provider - ask the user whether he wants to switch
		IPreferenceStore store= EditorsUI.getPreferenceStore();
		if (!store.getString(REVISION_ASK_BEFORE_QUICKDIFF_SWITCH).equals(MessageDialogWithToggle.ALWAYS)) {
			MessageDialogWithToggle toggleDialog= MessageDialogWithToggle.openOkCancelConfirm(
					viewer.getTextWidget().getShell(),
					TextEditorMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_title,
					TextEditorMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_message, 
					TextEditorMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_rememberquestion,
					true,
					store,
					REVISION_ASK_BEFORE_QUICKDIFF_SWITCH);
			if (toggleDialog.getReturnCode() != Window.OK)
				return false;
		}
		
		IAnnotationModel m= viewer.getAnnotationModel();
		if (!(m instanceof IAnnotationModelExtension))
			return false;
		IAnnotationModelExtension model= (IAnnotationModelExtension) m;
		model.removeAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
		
		IAnnotationModel newDiffer= util.createQuickDiffAnnotationModel(this, diffProviderId);

		model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, newDiffer);
		
		IChangeRulerColumn changeColumn= getChangeColumn();
		if (changeColumn != null)
			changeColumn.setModel(m);
		
		return true;
	}

	/**
	 * Installs the differ annotation model with the current quick diff display.
	 */
	private void installChangeRulerModel() {
		IChangeRulerColumn column= getChangeColumn();
		if (column != null) {
			getOrCreateDiffer();
			column.setModel(getSourceViewer().getAnnotationModel());
		}
		IOverviewRuler ruler= getOverviewRuler();
		if (ruler != null) {
			ruler.addAnnotationType("org.eclipse.ui.workbench.texteditor.quickdiffChange"); //$NON-NLS-1$
			ruler.addAnnotationType("org.eclipse.ui.workbench.texteditor.quickdiffAddition"); //$NON-NLS-1$
			ruler.addAnnotationType("org.eclipse.ui.workbench.texteditor.quickdiffDeletion"); //$NON-NLS-1$
			ruler.update();
		}
	}

	/**
	 * Uninstalls the differ annotation model from the current quick diff display.
	 */
	private void uninstallChangeRulerModel() {
		IChangeRulerColumn column= getChangeColumn();
		if (column != null)
			column.setModel(null);
		IOverviewRuler ruler= getOverviewRuler();
		if (ruler != null) {
			ruler.removeAnnotationType("org.eclipse.ui.workbench.texteditor.quickdiffChange"); //$NON-NLS-1$
			ruler.removeAnnotationType("org.eclipse.ui.workbench.texteditor.quickdiffAddition"); //$NON-NLS-1$
			ruler.removeAnnotationType("org.eclipse.ui.workbench.texteditor.quickdiffDeletion"); //$NON-NLS-1$
			ruler.update();
		}
		IAnnotationModel model= getDiffer();
		if (model instanceof ILineDifferExtension)
			((ILineDifferExtension) model).suspend();
	}

	/**
	 * Ensures that either the line number display is a <code>LineNumberChangeRuler</code> or
	 * a separate change ruler gets displayed.
	 */
	private void ensureChangeInfoCanBeDisplayed() {
		if (fLineNumberRulerColumn != null) {
			if (!(fLineNumberRulerColumn instanceof IChangeRulerColumn)) {
				hideLineNumberRuler();
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
	 * @return the line differ, or <code>null</code> if none could be found or created
	 */
	private IAnnotationModel getOrCreateDiffer() {
		IAnnotationModel differ= getDiffer();
		// create diff model if it doesn't
		if (differ == null) {
			IPreferenceStore store= getPreferenceStore();
			if (store != null) {
				String defaultId= store.getString(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
				differ= new QuickDiff().createQuickDiffAnnotationModel(this, defaultId);
				if (differ != null) {
					ISourceViewer viewer= getSourceViewer();
					if (viewer == null)
						return null;

					IAnnotationModel m= viewer.getAnnotationModel();
					IAnnotationModelExtension model;
					if (m instanceof IAnnotationModelExtension)
						model= (IAnnotationModelExtension) m;
					else
						return null;
					model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, differ);
				}
			}
		} else if (differ instanceof ILineDifferExtension && !fIsChangeInformationShown)
			((ILineDifferExtension) differ).resume();

		return differ;
	}

	/**
	 * Extracts the line differ from the displayed document's annotation model. If none can be found,
	 * <code>null</code> is returned.
	 *
	 * @return the line differ, or <code>null</code> if none could be found
	 */
	private IAnnotationModel getDiffer() {
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
		return model.getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
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
	protected boolean isLineNumberRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(LINE_NUMBER_RULER) : false;
	}

	/**
	 * Returns whether the overwrite mode is enabled according to the preference
	 * store settings. Subclasses may override this method to provide a custom
	 * preference setting.
	 *
	 * @return <code>true</code> if overwrite mode is enabled
	 * @since 3.1
	 */
	protected boolean isOverwriteModeEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? !store.getBoolean(DISABLE_OVERWRITE_MODE) : true;
	}

	/**
	 * Returns whether the range indicator is enabled according to the preference
	 * store settings. Subclasses may override this method to provide a custom
	 * preference setting.
	 *
	 * @return <code>true</code> if overwrite mode is enabled
	 * @since 3.1
	 */
	private boolean isRangeIndicatorEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR) : true;
	}
	
	/**
	 * Returns whether quick diff info should be visible upon opening an editor
	 * according to the preference store settings.
	 *
	 * @return <code>true</code> if the line numbers should be visible
	 */
	protected boolean isPrefQuickDiffAlwaysOn() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON) : false;
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
			// background color: same as editor, or system default
			if (!store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
				if (store.contains(PREFERENCE_COLOR_BACKGROUND)) {
					if (store.isDefault(PREFERENCE_COLOR_BACKGROUND))
						rgb= PreferenceConverter.getDefaultColor(store, PREFERENCE_COLOR_BACKGROUND);
					else
						rgb= PreferenceConverter.getColor(store, PREFERENCE_COLOR_BACKGROUND);
				}
			}
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

				if ("org.eclipse.ui.workbench.texteditor.quickdiffChange".equals(pref.getAnnotationType())) { //$NON-NLS-1$
					RGB rgb= getColorPreference(store, pref);
					changeColumn.setChangedColor(sharedColors.getColor(rgb));
				} else if ("org.eclipse.ui.workbench.texteditor.quickdiffAddition".equals(pref.getAnnotationType())) { //$NON-NLS-1$
					RGB rgb= getColorPreference(store, pref);
					changeColumn.setAddedColor(sharedColors.getColor(rgb));
				} else if ("org.eclipse.ui.workbench.texteditor.quickdiffDeletion".equals(pref.getAnnotationType())) { //$NON-NLS-1$
					RGB rgb= getColorPreference(store, pref);
					changeColumn.setDeletedColor(sharedColors.getColor(rgb));
				}
			}

			RGB rgb= null;
			// background color: same as editor, or system default
			if (!store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
				if (store.contains(PREFERENCE_COLOR_BACKGROUND)) {
					if (store.isDefault(PREFERENCE_COLOR_BACKGROUND))
						rgb= PreferenceConverter.getDefaultColor(store, PREFERENCE_COLOR_BACKGROUND);
					else
						rgb= PreferenceConverter.getColor(store, PREFERENCE_COLOR_BACKGROUND);
				}
			}
			changeColumn.setBackground(sharedColors.getColor(rgb));

			if (changeColumn instanceof LineNumberChangeRulerColumn) {
				LineNumberChangeRulerColumn lncrc= (LineNumberChangeRulerColumn) changeColumn;
				lncrc.setDisplayMode(store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE));
			}
		}

		changeColumn.redraw();
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
	 *
	 * @return the created line number column
	 */
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		if (isPrefQuickDiffAlwaysOn()) {
			LineNumberChangeRulerColumn column= new LineNumberChangeRulerColumn(getSharedColors());
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
		return new TextChangeHover();
	}

	/**
	 * Creates a new change ruler column for quick diff display independent of the
	 * line number ruler column
	 *
	 * @return a new change ruler column
	 */
	protected IChangeRulerColumn createChangeRulerColumn() {
		IChangeRulerColumn column= new ChangeRulerColumn(getSharedColors());
		column.setHover(createChangeHover());
		fChangeRulerColumn= column;
		initializeChangeRulerColumn(fChangeRulerColumn);
		return fChangeRulerColumn;
	}

	/**
	 * Returns {@link #createCompositeRuler()}. Subclasses should not override this method, but
	 * rather <code>createCompositeRuler</code> if they want to contribute their own vertical ruler
	 * implementation. If not an instance of {@link CompositeRuler} is returned, the built-in ruler
	 * columns (line numbers, annotations) will not work.
	 *
	 * <p>May become <code>final</code> in the future.</p>
	 *
	 * @see AbstractTextEditor#createVerticalRuler()
	 */
	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler= createCompositeRuler();
		IPreferenceStore store= getPreferenceStore();
		if (ruler != null && store != null) {
			for (Iterator iter=  ruler.getDecoratorIterator(); iter.hasNext();) {
				IVerticalRulerColumn column= (IVerticalRulerColumn)iter.next();
				if (column instanceof AnnotationRulerColumn) {
					fAnnotationRulerColumn= (AnnotationRulerColumn)column;
					for (Iterator iter2= fAnnotationPreferences.getAnnotationPreferences().iterator(); iter2.hasNext();) {
						AnnotationPreference preference= (AnnotationPreference)iter2.next();
						String key= preference.getVerticalRulerPreferenceKey();
						boolean showAnnotation= true;
						if (key != null && store.contains(key))
							showAnnotation= store.getBoolean(key);
						if (showAnnotation)
							fAnnotationRulerColumn.addAnnotationType(preference.getAnnotationType());
					}
					fAnnotationRulerColumn.addAnnotationType(Annotation.TYPE_UNKNOWN);
					break;
				}
			}
		}
		return ruler;
	}

	/**
	 * Creates a composite ruler to be used as the vertical ruler by this editor.
	 * Subclasses may re-implement this method.
	 *
	 * @return the vertical ruler
	 */
	protected CompositeRuler createCompositeRuler() {
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, createAnnotationRulerColumn(ruler));

		if (isLineNumberRulerVisible())
			ruler.addDecorator(1, createLineNumberRulerColumn());
		else if (isPrefQuickDiffAlwaysOn())
			ruler.addDecorator(1, createChangeRulerColumn());

		return ruler;
	}

	/**
	 * Creates the annotation ruler column. Subclasses may re-implement or extend.
	 * 
	 * @param ruler the composite ruler that the column will be added
	 * @return an annotation ruler column
	 * @since 3.2
	 */
	protected IVerticalRulerColumn createAnnotationRulerColumn(CompositeRuler ruler) {
		return new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, getAnnotationAccess());
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
				if (isOverviewRulerVisible())
					showOverviewRuler();
				else
					hideOverviewRuler();
				return;
			}

			if (DISABLE_OVERWRITE_MODE.equals(property)) {
				enableOverwriteMode(isOverwriteModeEnabled());
				return;
			}

			if (LINE_NUMBER_RULER.equals(property)) {
				if (isLineNumberRulerVisible())
					showLineNumberRuler();
				else
					hideLineNumberRuler();
				return;
			}

			if (AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON.equals(property)) {
				showChangeInformation(isPrefQuickDiffAlwaysOn());
			}

			if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
				IPreferenceStore store= getPreferenceStore();
				if (store != null)
					sourceViewer.getTextWidget().setTabs(store.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
				return;
			}

			if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE.equals(property) && sourceViewer instanceof ITextViewerExtension6) {
				IPreferenceStore store= getPreferenceStore();
				if (store != null)
					((ITextViewerExtension6)sourceViewer).getUndoManager().setMaximalUndoLevel(store.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE));
				return;
			}

			if (fLineNumberRulerColumn != null
				&&	(LINE_NUMBER_COLOR.equals(property)
				||	PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)
				||	PREFERENCE_COLOR_BACKGROUND.equals(property))) {

				initializeLineNumberRulerColumn(fLineNumberRulerColumn);
			}

			if (fChangeRulerColumn != null
				&&	(LINE_NUMBER_COLOR.equals(property)
				||	PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)
				||	PREFERENCE_COLOR_BACKGROUND.equals(property))) {

				initializeChangeRulerColumn(fChangeRulerColumn);
			}

			if (fLineNumberRulerColumn instanceof LineNumberChangeRulerColumn
					&& AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE.equals(property)) {
				initializeChangeRulerColumn(getChangeColumn());
			}
			
			if (AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR.equals(property)) {
				if (isRangeIndicatorEnabled()) {
					getSourceViewer().setRangeIndicator(getRangeIndicator());
				} else {
					getSourceViewer().removeRangeIndication();
					getSourceViewer().setRangeIndicator(null);
				}
			}

			AnnotationPreference pref= getAnnotationPreference(property);
			if (pref != null) {
				IChangeRulerColumn column= getChangeColumn();
				if (column != null) {
					Object type= pref.getAnnotationType();
					if (type instanceof String) {
						String annotationType= (String) type;
						if (annotationType.startsWith("org.eclipse.ui.workbench.texteditor.quickdiff")) //$NON-NLS-1$
							initializeChangeRulerColumn(column);
					}
				}
			}

			AnnotationPreference annotationPreference= getVerticalRulerAnnotationPreference(property);
			if (annotationPreference != null && event.getNewValue() instanceof Boolean) {
				Object type= annotationPreference.getAnnotationType();
				if (((Boolean)event.getNewValue()).booleanValue())
					fAnnotationRulerColumn.addAnnotationType(type);
				else
					fAnnotationRulerColumn.removeAnnotationType(type);
				getVerticalRuler().update();
			}

		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/**
	 * Returns the <code>AnnotationPreference</code> corresponding to <code>colorKey</code>.
	 *
	 * @param colorKey the color key.
	 * @return the corresponding <code>AnnotationPreference</code>
	 */
	private AnnotationPreference getAnnotationPreference(String colorKey) {
		for (Iterator iter= fAnnotationPreferences.getAnnotationPreferences().iterator(); iter.hasNext();) {
			AnnotationPreference pref= (AnnotationPreference) iter.next();
			if (colorKey.equals(pref.getColorPreferenceKey()))
				return pref;
		}
		return null;
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

	/**
	 * Shows the overview ruler.
	 */
	protected void showOverviewRuler() {
		if (fOverviewRuler != null) {
			if (getSourceViewer() instanceof ISourceViewerExtension) {
				((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(true);
				fSourceViewerDecorationSupport.updateOverviewDecorations();
			}
		}
	}

	/**
	 * Hides the overview ruler.
	 */
	protected void hideOverviewRuler() {
		if (getSourceViewer() instanceof ISourceViewerExtension) {
			fSourceViewerDecorationSupport.hideAnnotationOverview();
			((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(false);
		}
	}

	/**
	 * Returns the annotation access.
	 *
	 * @return the annotation access
	 */
	protected IAnnotationAccess getAnnotationAccess() {
		if (fAnnotationAccess == null)
			fAnnotationAccess= createAnnotationAccess();
		return fAnnotationAccess;
	}

	/**
	 * Returns the annotation preference lookup.
	 *
	 * @return the annotation preference lookup
	 */
	protected AnnotationPreferenceLookup getAnnotationPreferenceLookup() {
		return EditorsPlugin.getDefault().getAnnotationPreferenceLookup();
	}

	/**
	 * Returns the overview ruler.
	 *
	 * @return the overview ruler
	 */
	protected IOverviewRuler getOverviewRuler() {
		if (fOverviewRuler == null)
			fOverviewRuler= createOverviewRuler(getSharedColors());
		return fOverviewRuler;
	}

	/**
	 * Returns the source viewer decoration support.
	 *
	 * @param viewer the viewer for which to return a decoration support
	 * @return the source viewer decoration support
	 */
	protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(ISourceViewer viewer) {
		if (fSourceViewerDecorationSupport == null) {
			fSourceViewerDecorationSupport= new SourceViewerDecorationSupport(viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors());
			configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
		}
		return fSourceViewerDecorationSupport;
	}

	/**
	 * Returns the annotation preferences.
	 *
	 * @return the annotation preferences
	 */
	protected MarkerAnnotationPreferences getAnnotationPreferences() {
		return fAnnotationPreferences;
	}


	/**
	 * If the editor can be saved all marker ranges have been changed according to
	 * the text manipulations. However, those changes are not yet propagated to the
	 * marker manager. Thus, when opening a marker, the marker's position in the editor
	 * must be determined as it might differ from the position stated in the marker.
	 *
	 * @param marker the marker to go to
	 * @deprecated visibility will be reduced, use <code>getAdapter(IGotoMarker.class) for accessing this method</code>
	 */
	public void gotoMarker(IMarker marker) {
		if (fIsUpdatingMarkerViews)
			return;
		
		if (getSourceViewer() == null)
			return;

		int start= MarkerUtilities.getCharStart(marker);
		int end= MarkerUtilities.getCharEnd(marker);
		
		boolean selectLine= start < 0 || end < 0; 

		// look up the current range of the marker when the document has been edited
		IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {

			AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) model;
			Position pos= markerModel.getMarkerPosition(marker);
			if (pos != null && !pos.isDeleted()) {
				// use position instead of marker values
				start= pos.getOffset();
				end= pos.getOffset() + pos.getLength();
			}

			if (pos != null && pos.isDeleted()) {
				// do nothing if position has been deleted
				return;
			}
		}

		IDocument document= getDocumentProvider().getDocument(getEditorInput());

		if (selectLine) {
			int line;
			try {
				if (start >= 0)
					line= document.getLineOfOffset(start);
				else {
					line= MarkerUtilities.getLineNumber(marker);
					// Marker line numbers are 1-based
					-- line;
				}
				end= start + document.getLineLength(line) - 1;
			} catch (BadLocationException e) {
				return;
			}
		}

		int length= document.getLength();
		if (end - 1 < length && start < length)
			selectAndReveal(start, end - start);
	}

	/*
	 * @see org.eclipse.ui.texteditor.StatusTextEditor#isErrorStatus(org.eclipse.core.runtime.IStatus)
	 */
	protected boolean isErrorStatus(IStatus status) {
		// see bug 42230
		return super.isErrorStatus(status) && status.getCode() != IResourceStatus.READ_ONLY_LOCAL && status.getSeverity() != IStatus.CANCEL;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		ResourceAction action= new AddMarkerAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.AddBookmark.", this, IMarker.BOOKMARK, true); //$NON-NLS-1$
		action.setHelpContextId(ITextEditorHelpContextIds.BOOKMARK_ACTION);
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.ADD_BOOKMARK);
		setAction(IDEActionFactory.BOOKMARK.getId(), action);

		action= new AddTaskAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.AddTask.", this); //$NON-NLS-1$
		action.setHelpContextId(ITextEditorHelpContextIds.ADD_TASK_ACTION);
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.ADD_TASK);
		setAction(IDEActionFactory.ADD_TASK.getId(), action);

		action= new ChangeEncodingAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.ChangeEncodingAction.", this); //$NON-NLS-1$
		action.setHelpContextId(ITextEditorHelpContextIds.CHANGE_ENCODING);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CHANGE_ENCODING);
		setAction(ITextEditorActionConstants.CHANGE_ENCODING, action);
		markAsPropertyDependentAction(ITextEditorActionConstants.CHANGE_ENCODING, true);

		action= new ResourceAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.ToggleLineNumbersAction.", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				toggleLineNumberRuler();
			}
		};
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINENUMBER_TOGGLE);
		setAction(ITextEditorActionConstants.LINENUMBERS_TOGGLE, action);

		action= new ResourceAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.ToggleQuickDiffAction.", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				toggleQuickDiffRuler();
			}
		};
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.QUICKDIFF_TOGGLE);
		setAction(ITextEditorActionConstants.QUICKDIFF_TOGGLE, action);

		action= new RevertLineAction(this, false);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.QUICKDIFF_REVERTLINE);
		setAction(ITextEditorActionConstants.QUICKDIFF_REVERTLINE, action);

		action= new RevertSelectionAction(this, false);
		setAction(ITextEditorActionConstants.QUICKDIFF_REVERTSELECTION, action);

		action= new RevertBlockAction(this, false);
		setAction(ITextEditorActionConstants.QUICKDIFF_REVERTBLOCK, action);

		action= new RestoreAction(this, false);
		setAction(ITextEditorActionConstants.QUICKDIFF_REVERTDELETION, action);

		IAction action2= new CompositeRevertAction(new IAction[] {
		                                       getAction(ITextEditorActionConstants.QUICKDIFF_REVERTSELECTION),
		                                       getAction(ITextEditorActionConstants.QUICKDIFF_REVERTBLOCK),
										       getAction(ITextEditorActionConstants.QUICKDIFF_REVERTDELETION),
										       getAction(ITextEditorActionConstants.QUICKDIFF_REVERTLINE)});
		action2.setActionDefinitionId(ITextEditorActionDefinitionIds.QUICKDIFF_REVERT);
		setAction(ITextEditorActionConstants.QUICKDIFF_REVERT, action2);
		
		action= new ResourceAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.HideRevisionInformationAction.") { //$NON-NLS-1$
			public void run() {
				hideRevisionInformation();
			}
		};
		setAction(ITextEditorActionConstants.REVISION_HIDE_INFO, action);

		final Shell shell;
		if (getSourceViewer() != null)
			shell= getSourceViewer().getTextWidget().getShell();
		else
			shell= null;
		action= new ResourceAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.RulerPreferencesAction.") { //$NON-NLS-1$
			public void run() {
				String[] preferencePages= collectRulerMenuPreferencePages();
				if (preferencePages.length > 0 && (shell == null || !shell.isDisposed()))
					PreferencesUtil.createPreferenceDialogOn(shell, preferencePages[0], preferencePages, null).open();
			}

		};
		setAction(ITextEditorActionConstants.RULER_PREFERENCES, action);

		action= new ResourceAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.ContextPreferencesAction.") { //$NON-NLS-1$
			public void run() {
				String[] preferencePages= collectContextMenuPreferencePages();
				if (preferencePages.length > 0 && (shell == null || !shell.isDisposed()))
					PreferencesUtil.createPreferenceDialogOn(shell, preferencePages[0], preferencePages, null).open();
			}
		};
		setAction(ITextEditorActionConstants.CONTEXT_PREFERENCES, action);
	}

	public Object getAdapter(Class adapter) {
		if (IGotoMarker.class.equals(adapter))
			return fGotoMarkerAdapter;
	
		if (IAnnotationAccess.class.equals(adapter))
			return getAnnotationAccess();
	
		if (adapter == IShowInSource.class) {
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					ISelection selection= null;
					ISelectionProvider selectionProvider= getSelectionProvider();
					if (selectionProvider != null)
						selection= selectionProvider.getSelection();
					return new ShowInContext(getEditorInput(), selection);
				}
			};
		}
	
		return super.getAdapter(adapter);
	
	}

	/*
	 * If there is no explicit document provider set, the implicit one is
	 * re-initialized based on the given editor input.
	 *
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setDocumentProvider(org.eclipse.ui.IEditorInput)
	 */
	protected void setDocumentProvider(IEditorInput input) {
		fImplicitDocumentProvider= DocumentProviderRegistry.getDefault().getDocumentProvider(input);
		IDocumentProvider provider= super.getDocumentProvider();
		if (provider instanceof ForwardingDocumentProvider) {
			ForwardingDocumentProvider forwarder= (ForwardingDocumentProvider) provider;
			forwarder.setParentProvider(fImplicitDocumentProvider);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditor#getDocumentProvider()
	 */
	public IDocumentProvider getDocumentProvider() {
		IDocumentProvider provider= super.getDocumentProvider();
		if (provider == null)
			return fImplicitDocumentProvider;
		return provider;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#disposeDocumentProvider()
	 */
	protected void disposeDocumentProvider() {
		super.disposeDocumentProvider();
		fImplicitDocumentProvider= null;
	}

	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 *
	 * This implementation also updates change information in the quick diff
	 * ruler.
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (fIsChangeInformationShown) {
			if (isPrefQuickDiffAlwaysOn()) {
				// only uninstall the model since we will reuse the change ruler
				uninstallChangeRulerModel();
				fIsChangeInformationShown= false;
			} else
				showChangeInformation(false);
		}
		
		super.doSetInput(input);

		if (isPrefQuickDiffAlwaysOn())
			showChangeInformation(true);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#rulerContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 * @since 3.1
	 */
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		/*
		 * XXX: workaround for reliable menu item ordering.
		 * This can be changed once the action contribution story converges,
		 * see http://dev.eclipse.org/viewcvs/index.cgi/~checkout~/platform-ui-home/R3_1/dynamic_teams/dynamic_teams.html#actionContributions
		 */
		// pre-install menus for contributions and call super
		menu.add(new Separator("debug")); //$NON-NLS-1$
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_RESTORE));
		menu.add(new Separator("add")); //$NON-NLS-1$
		menu.add(new Separator(ITextEditorActionConstants.GROUP_RULERS));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_REST));

		super.rulerContextMenuAboutToShow(menu);

		/* quick diff */
		if (isEditorInputModifiable()) {
			IAction quickdiffAction= getAction(ITextEditorActionConstants.QUICKDIFF_TOGGLE);
			quickdiffAction.setChecked(isChangeInformationShowing());
			menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, quickdiffAction);

			if (isChangeInformationShowing()) {
				TextEditorAction revertLine= new RevertLineAction(this, true);
				TextEditorAction revertSelection= new RevertSelectionAction(this, true);
				TextEditorAction revertBlock= new RevertBlockAction(this, true);
				TextEditorAction revertDeletion= new RestoreAction(this, true);

				revertSelection.update();
				revertBlock.update();
				revertLine.update();
				revertDeletion.update();

				// only add block action if selection action is not enabled
				if (revertSelection.isEnabled())
					menu.appendToGroup(ITextEditorActionConstants.GROUP_RESTORE, revertSelection);
				else if (revertBlock.isEnabled())
					menu.appendToGroup(ITextEditorActionConstants.GROUP_RESTORE, revertBlock);
				if (revertLine.isEnabled())
					menu.appendToGroup(ITextEditorActionConstants.GROUP_RESTORE, revertLine);
				if (revertDeletion.isEnabled())
					menu.appendToGroup(ITextEditorActionConstants.GROUP_RESTORE, revertDeletion);
			}
		}
		
		// revision info
		if (fIsRevisionInformationShown) {
			IAction hideRevisionInfoAction= getAction(ITextEditorActionConstants.REVISION_HIDE_INFO);
			menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, hideRevisionInfoAction);
		}

		IAction lineNumberAction= getAction(ITextEditorActionConstants.LINENUMBERS_TOGGLE);
		lineNumberAction.setChecked(fLineNumberRulerColumn != null);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, lineNumberAction);

		IAction preferencesAction= getAction(ITextEditorActionConstants.RULER_PREFERENCES);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, new Separator(ITextEditorActionConstants.GROUP_SETTINGS));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_SETTINGS, preferencesAction);
	}

	/**
	 * Toggles the line number global preference and shows the line number ruler
	 * accordingly.
	 *
	 * @since 3.1
	 */
	private void toggleLineNumberRuler() {
		boolean newSetting= fLineNumberRulerColumn == null;
		// locally
		if (newSetting)
			showLineNumberRuler();
		else
			hideLineNumberRuler();

		// globally
		IPreferenceStore store= EditorsUI.getPreferenceStore();
		if (store != null) {
			store.setValue(LINE_NUMBER_RULER, newSetting);
		}
	}

	/**
	 * Toggles the quick diff global preference and shows the quick diff ruler
	 * accordingly.
	 *
	 * @since 3.1
	 */
	private void toggleQuickDiffRuler() {
		boolean newSetting= !isChangeInformationShowing();
		// change locally to ensure we get it right even if our setting
		// does not adhere to the global setting
		showChangeInformation(newSetting);

		// change global setting which will inform other interested parties
		// the it is changed
		IPreferenceStore store= EditorsUI.getPreferenceStore();
		if (store != null) {
			store.setValue(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, newSetting);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 * @since 3.1
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);

		IAction preferencesAction= getAction(ITextEditorActionConstants.CONTEXT_PREFERENCES);
		menu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator(ITextEditorActionConstants.GROUP_SETTINGS));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_SETTINGS, preferencesAction);
		
		menu.appendToGroup(ITextEditorActionConstants.GROUP_SAVE, new Separator(ITextEditorActionConstants.GROUP_OPEN));
		MenuManager showInSubMenu= new MenuManager(getShowInMenuLabel());
		showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getEditorSite().getWorkbenchWindow()));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_OPEN, showInSubMenu); 
	}

	/**
	 * Returns the menu label for 'Show In' together
	 * with its key binding string.
	 * 
	 * @return the 'Show In' menu label
	 * @since 3.2
	 */
	private String getShowInMenuLabel() {
		String keyBinding= null;
		
		IBindingService bindingService= (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		if (bindingService != null)
			keyBinding= bindingService.getBestActiveBindingFormattedFor("org.eclipse.ui.navigate.showInQuickMenu"); //$NON-NLS-1$
		
		if (keyBinding == null)
			keyBinding= ""; //$NON-NLS-1$
		
		return NLS.bind(TextEditorMessages.AbstractDecoratedTextEditor_showIn_menu, keyBinding);
	}

	/**
	 * Returns the preference page ids of the preference pages to be shown
	 * when executing the preferences action from the editor context menu.
	 * <p>
	 * Subclasses may extend or replace.
	 * </p>
	 *
	 * @return the preference page ids to show, may be empty
	 * @since 3.1
	 */
	protected String[] collectContextMenuPreferencePages() {
		return new String[] {
			"org.eclipse.ui.preferencePages.GeneralTextEditor", //$NON-NLS-1$
			"org.eclipse.ui.editors.preferencePages.Annotations", //$NON-NLS-1$
			"org.eclipse.ui.editors.preferencePages.QuickDiff", //$NON-NLS-1$
			"org.eclipse.ui.editors.preferencePages.Accessibility", //$NON-NLS-1$
			"org.eclipse.ui.editors.preferencePages.Spelling", //$NON-NLS-1$
		};
	}

	/**
	 * Returns the preference page ids of the preference pages to be shown when
	 * executing the preferences action from the editor ruler context menu.
	 * <p>
	 * The default is to return the same list as
	 * <code>collectContextMenuPreferencePages</code>.
	 * </p>
	 * <p>
	 * Subclasses may extend or replace.
	 * </p>
	 *
	 * @return the preference page ids to show, may be empty
	 * @since 3.1
	 */
	protected String[] collectRulerMenuPreferencePages() {
		return collectContextMenuPreferencePages();
	}
	
	/*
	 * @see AbstractTextEditor#getUndoRedoOperationApprover(IUndoContext)
	 * @since 3.1
	 */
	protected IOperationApprover getUndoRedoOperationApprover(IUndoContext undoContext) {
		IEditorInput input= getEditorInput();
		if (input != null && input.getAdapter(IResource.class) != null)
			return new NonLocalUndoUserApprover(undoContext, this, new Object [] { input }, IResource.class);
		return super.getUndoRedoOperationApprover(undoContext);
	}
	
	/**
	 * Returns whether the given annotation is configured as a target for the
	 * "Go to Next/Previous Annotation" actions.
	 * <p>
	 * The annotation is a target if their annotation type is configured to be
	 * in the Next/Previous tool bar drop down menu and if it is checked.
	 * </p>
	 *
	 * @param annotation the annotation
	 * @return <code>true</code> if this is a target, <code>false</code> otherwise
	 * @since 3.2
	 */
	protected boolean isNavigationTarget(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
//		See bug 41689
//		String key= forward ? preference.getIsGoToNextNavigationTargetKey() : preference.getIsGoToPreviousNavigationTargetKey();
		String key= preference == null ? null : preference.getIsGoToNextNavigationTargetKey();
		return (key != null && getPreferenceStore().getBoolean(key));
	}
	
	
	
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This extended implementation updates views that also show the
	 * select marker annotation.
	 * </p>
	 * @since 3.2
	 */
	public Annotation gotoAnnotation(boolean forward) {
		Annotation annotation= super.gotoAnnotation(forward);
		if (annotation != null)
			updateMarkerViews(annotation);
		return annotation;
	}
	
	/**
	 * Updates visible views that show markers.
	 * <p>
	 * If the given annotation can be associated with a marker then
	 * this method tries select the this marker in views that show
	 * markers.
	 * </p> 
	 * @param annotation
	 * @since 3.2
	 */
	protected void updateMarkerViews(Annotation annotation) {
		IMarker marker= null;
		if (annotation instanceof MarkerAnnotation)
			marker= ((MarkerAnnotation)annotation).getMarker();
		
		if (marker != null) {
			try {
				fIsUpdatingMarkerViews= true;
				IWorkbenchPage page= getSite().getPage();
				MarkerViewUtil.showMarker(page, marker, false);
			} finally {
				fIsUpdatingMarkerViews= false;
			}
		}
	}
}
