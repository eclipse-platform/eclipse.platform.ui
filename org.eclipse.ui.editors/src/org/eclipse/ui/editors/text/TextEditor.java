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

package org.eclipse.ui.editors.text;


import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AddTaskAction;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ConvertLineDelimitersAction;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.StatusTextEditor;

import org.eclipse.ui.internal.editors.quickdiff.DocumentLineDiffer;
import org.eclipse.ui.internal.editors.quickdiff.ReferenceProviderDescriptor;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;



/**
 * The standard text editor for file resources (<code>IFile</code>).
 * <p>
 * This editor has id <code>"org.eclipse.ui.DefaultTextEditor"</code>.
 * The editor's context menu has id <code>#TextEditorContext</code>.
 * The editor's ruler context menu has id <code>#TextRulerContext</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when the default 
 * editor is needed for a workbench window.
 * </p>
 */
public class TextEditor extends StatusTextEditor {
	
	/**
	 * Preference key for showing the line number ruler.
	 * @since 2.1
	 */
	private final static String LINE_NUMBER_RULER= TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
	/**
	 * Preference key for the foreground color of the line numbers.
	 * @since 2.1
	 */
	private final static String LINE_NUMBER_COLOR= TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
	/**
	 * Preference key for showing the overview ruler.
	 * @since 2.1
	 */
	private final static String OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
	/**
	 * Preference key for unknown annotation indication in overview ruler.
	 * @since 2.1
	 **/
	private final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;
	/**
	 * Preference key for unknown annotation indication.
	 * @since 2.1
	 **/
	private final static String UNKNOWN_INDICATION= TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION;
	/**
	 * Preference key for unknown annotation color.
	 * @since 2.1
	 **/
	private final static String UNKNOWN_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;
	/**
	 * Preference key for highlighting current line.
	 * @since 2.1
	 */
	private final static String CURRENT_LINE= TextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
	/**
	 * Preference key for highlight color of current line.
	 * @since 2.1
	 */
	private final static String CURRENT_LINE_COLOR= TextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/**
	 * Preference key for showing print marging ruler.
	 * @since 2.1
	 */
	private final static String PRINT_MARGIN= TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN;
	/**
	 * Preference key for print margin ruler color.
	 * @since 2.1
	 */
	private final static String PRINT_MARGIN_COLOR= TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/**
	 * Preference key for print margin ruler column.
	 * @since 2.1
	 **/
	private final static String PRINT_MARGIN_COLUMN= TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;

	/** 
	 * The overview ruler of this editor.
	 * @since 2.1
	 */
	protected IOverviewRuler fOverviewRuler;
	/**
	 * Helper for accessing annotation from the perspective of this editor.
	 * @since 2.1
	 */
	protected IAnnotationAccess fAnnotationAccess;
	/**
	 * Helper for managing the decoration support of this editor's viewer.
	 * @since 2.1
	 */
	protected SourceViewerDecorationSupport fSourceViewerDecorationSupport;
	/**
	 * The line number column.
	 * @since 2.1
	 */
	protected LineNumberRulerColumn fLineNumberRulerColumn;
	/**
	 * The change ruler column.
	 * @since 3.0
	 */
	private IChangeRulerColumn fChangeRulerColumn;
	/**
	 * The encoding support for the editor.
	 * @since 2.0
	 */
	protected DefaultEncodingSupport fEncodingSupport;
	/**
	 * The annotation preferences.
	 * @since 2.1
	 */
	private MarkerAnnotationPreferences fAnnotationPreferences;
	/**
	 * Whether quick diff information is displayed, either on a change ruler or the line number ruler.
	 * @since 3.0
	 */
	private boolean fIsChangeInformationShown;
	
	
	/**
	 * Creates a new text editor.
	 */
	public TextEditor() {
		super();
		initializeKeyBindingScopes();
		setSourceViewerConfiguration(new TextSourceViewerConfiguration());
		initializeEditor();
		fAnnotationPreferences= new MarkerAnnotationPreferences();
	}
	
	/**
	 * Initializes this editor.
	 */
	protected void initializeEditor() {
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#TextEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#TextRulerContext"); //$NON-NLS-1$
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}

	/**
	 * Initializes the key binding scopes of this editor.
	 * 
	 * @since 2.1
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope" });  //$NON-NLS-1$
	}
	
	/*
	 * @see IWorkbenchPart#dispose()
	 * @since 2.0
	 */
	public void dispose() {
		if (fEncodingSupport != null) {
				fEncodingSupport.dispose();
				fEncodingSupport= null;
		}

		if (fSourceViewerDecorationSupport != null) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport= null;
		}
		
		fAnnotationPreferences= null;
		fAnnotationAccess= null;
		
		super.dispose();
	}

	/*
	 * @see AbstractTextEditor#doSaveAs()
	 * @since 2.1
	 */
	public void doSaveAs() {
		if (askIfNonWorkbenchEncodingIsOk())
			super.doSaveAs();
	}
	
	/*
	 * @see AbstractTextEditor#doSave(IProgressMonitor)
	 * @since 2.1
	 */
	public void doSave(IProgressMonitor monitor){
		if (askIfNonWorkbenchEncodingIsOk())
			super.doSave(monitor);
		else
			monitor.setCanceled(true);
	}

	/**
	 * Installs the encoding support on the given text editor.
	 * <p> 
 	 * Subclasses may override to install their own encoding
 	 * support or to disable the default encoding support.
 	 * </p>
	 * @since 2.1
	 */
	protected void installEncodingSupport() {
		fEncodingSupport= new DefaultEncodingSupport();
		fEncodingSupport.initialize(this);
	}

	/**
	 * Asks the user if it is ok to store in non-workbench encoding.
	 * 
	 * @return <true> if the user wants to continue or if no encoding support has been installed
	 * @since 2.1
	 */
	private boolean askIfNonWorkbenchEncodingIsOk() {
		
		if (fEncodingSupport == null)
			return true;
		
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IStorageDocumentProvider) {
			IEditorInput input= getEditorInput();
			IStorageDocumentProvider storageProvider= (IStorageDocumentProvider)provider;
			String encoding= storageProvider.getEncoding(input);
			String defaultEncoding= storageProvider.getDefaultEncoding();
			if (encoding != null && !encoding.equals(defaultEncoding)) {
				Shell shell= getSite().getShell();
				String title= TextEditorMessages.getString("Editor.warning.save.nonWorkbenchEncoding.title"); //$NON-NLS-1$
				String msg;
				if (input != null)
					msg= MessageFormat.format(TextEditorMessages.getString("Editor.warning.save.nonWorkbenchEncoding.message1"), new String[] {input.getName(), encoding});//$NON-NLS-1$
				else
					msg= MessageFormat.format(TextEditorMessages.getString("Editor.warning.save.nonWorkbenchEncoding.message2"), new String[] {encoding});//$NON-NLS-1$
				return MessageDialog.openQuestion(shell, title, msg);
			}
		}
		return true;
	}

	/**
	 * The <code>TextEditor</code> implementation of this  <code>AbstractTextEditor</code> 
	 * method asks the user for the workspace path of a file resource and saves the document there.
	 * 
	 * @param progressMonitor the progress monitor to be used
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell= getSite().getShell();
		IEditorInput input = getEditorInput();
		
		SaveAsDialog dialog= new SaveAsDialog(shell);
		
		IFile original= (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
		if (original != null)
			dialog.setOriginalFile(original);
		
		dialog.create();
			
		IDocumentProvider provider= getDocumentProvider();
		if (provider == null) {
			// editor has programatically been  closed while the dialog was open
			return;
		}
		
		if (provider.isDeleted(input) && original != null) {
			String message= MessageFormat.format(TextEditorMessages.getString("Editor.warning.save.delete"), new Object[] { original.getName() }); //$NON-NLS-1$
			dialog.setErrorMessage(null);
			dialog.setMessage(message, IMessageProvider.WARNING);
		}
		
		if (dialog.open() == Dialog.CANCEL) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IPath filePath= dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(filePath);
		final IEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				getDocumentProvider().saveDocument(monitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
			}
		};
		
		boolean success= false;
		try {
			
			provider.aboutToChange(newInput);
			new ProgressMonitorDialog(shell).run(false, true, op);
			success= true;
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			
			Throwable targetException= x.getTargetException();
			
			String title= TextEditorMessages.getString("Editor.error.save.title"); //$NON-NLS-1$
			String msg= MessageFormat.format(TextEditorMessages.getString("Editor.error.save.message"), new Object[] { targetException.getMessage() }); //$NON-NLS-1$
			
			if (targetException instanceof CoreException) {
				CoreException coreException= (CoreException) targetException;
				IStatus status= coreException.getStatus();
				if (status != null) {
					switch (status.getSeverity()) {
						case IStatus.INFO:
							MessageDialog.openInformation(shell, title, msg);
							break;
						case IStatus.WARNING:
							MessageDialog.openWarning(shell, title, msg);
							break;
						default:
							MessageDialog.openError(shell, title, msg);
					}
				} else {
				  	 MessageDialog.openError(shell, title, msg);
				}
			}
						
		} finally {
			provider.changed(newInput);
			if (success)
				setInput(newInput);
		}
		
		if (progressMonitor != null)
			progressMonitor.setCanceled(!success);
	}
	
	/*
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/*
	 * @see AbstractTextEditor#createActions()
	 * @since 2.0
	 */
	protected void createActions() {
		super.createActions();
		
		ResourceAction action= new AddTaskAction(TextEditorMessages.getResourceBundle(), "Editor.AddTask.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.ADD_TASK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.ADD_TASK);
		setAction(ITextEditorActionConstants.ADD_TASK, action);

		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToWindows.", this, "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_WINDOWS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_WINDOWS);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS, action);

		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToUNIX.", this, "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_UNIX);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_UNIX);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX, action);
		
		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToMac.", this, "\r"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_MAC);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_MAC);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC, action);
		
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=17709
		markAsStateDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS, true);
		markAsStateDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX, true);
		markAsStateDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC, true);

		installEncodingSupport();
	}
	
	/*
	 * @see StatusTextEditor#getStatusHeader(IStatus)
	 * @since 2.0
	 */
	protected String getStatusHeader(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusHeader(status);
			if (message != null)
				return message;
		}
		return super.getStatusHeader(status);
	}
	
	/*
	 * @see StatusTextEditor#getStatusBanner(IStatus)
	 * @since 2.0
	 */
	protected String getStatusBanner(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusBanner(status);
			if (message != null)
				return message;
		}
		return super.getStatusBanner(status);
	}
	
	/*
	 * @see StatusTextEditor#getStatusMessage(IStatus)
	 * @since 2.0
	 */
	protected String getStatusMessage(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusMessage(status);
			if (message != null)
				return message;
		}
		return super.getStatusMessage(status);
	}
	
	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 * @since 2.0
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}
	
	/*
	 * @see IAdaptable#getAdapter(java.lang.Class)
	 * @since 2.0
	 */
	public Object getAdapter(Class adapter) {
		if (IEncodingSupport.class.equals(adapter))
			return fEncodingSupport;
		return super.getAdapter(adapter);
	}
	
	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 * @since 2.0
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updatePropertyDependentActions()
	 * @since 2.0
	 */
	protected void updatePropertyDependentActions() {
		super.updatePropertyDependentActions();
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler, int)
	 * @since 2.1
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		fAnnotationAccess= createAnnotationAccess();
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
		
		fOverviewRuler= new OverviewRuler(fAnnotationAccess, VERTICAL_RULER_WIDTH, sharedColors);
		Iterator e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference preference= (AnnotationPreference) e.next();
			if (preference.contributesToHeader())
				fOverviewRuler.addHeaderAnnotationType(preference.getAnnotationType());
		}
		
		ISourceViewer sourceViewer= new SourceViewer(parent, ruler, fOverviewRuler, isOverviewRulerVisible(), styles);
		fSourceViewerDecorationSupport= new SourceViewerDecorationSupport(sourceViewer, fOverviewRuler, fAnnotationAccess, sharedColors);
		configureSourceViewerDecorationSupport();
		
		return sourceViewer;
	}

	/**
	 * Creates the annotation access for this editor.
	 * 
	 * @return the created annotation access
	 * @since 2.1
	 */
	protected IAnnotationAccess createAnnotationAccess() {
		return new DefaultMarkerAnnotationAccess(fAnnotationPreferences);
	}

	/**
	 * Configures the decoration support for this editor's the source viewer.
	 * 
	 * @since 2.1
	 */
	protected void configureSourceViewerDecorationSupport() {

		Iterator e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext())
			fSourceViewerDecorationSupport.setAnnotationPreference((AnnotationPreference) e.next());
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(DefaultMarkerAnnotationAccess.UNKNOWN, UNKNOWN_INDICATION_COLOR, UNKNOWN_INDICATION, UNKNOWN_INDICATION_IN_OVERVIEW_RULER, 0);
		
		fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
		fSourceViewerDecorationSupport.setMarginPainterPreferenceKeys(PRINT_MARGIN, PRINT_MARGIN_COLOR, PRINT_MARGIN_COLUMN);
		fSourceViewerDecorationSupport.setSymbolicFontName(getFontPropertyPreferenceKey());
	}

	/**
	 * @since 2.1
	 */
	private void showOverviewRuler() {
		if (getSourceViewer() instanceof ISourceViewerExtension) {
			((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(true);
			fSourceViewerDecorationSupport.updateOverviewDecorations();
		}
	}

	/**
	 * Hides the overview ruler.
	 * 
	 * @since 2.1
	 */
	private void hideOverviewRuler() {
		if (getSourceViewer() instanceof ISourceViewerExtension) {
			fSourceViewerDecorationSupport.hideAnnotationOverview();
			((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(false);
		}
	}

	/**
	 * Tells whether the overview ruler is visible.
	 * 
	 * @since 2.1
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
			showChangeRuler(false); // hide change ruler if its displayed - if the line number ruler is showing, only the colors get removed by deinstalling the model
		} else {
			ensureChangeInfoCanBeDisplayed(); // can be replaced w/ showChangeRuler(false) once the old line number ruler is gone
			installChangeRulerModel();
		}
		
		fIsChangeInformationShown= show;
	}

	/**
	 * Installs the differ annotation model with the current quick diff display. 
	 * @since R3.0
	 */
	private void installChangeRulerModel() {
		IChangeRulerColumn column= getChangeColumn();
		if (column != null)
			column.setModel(getOrCreateDiffer());
	}

	/**
	 * Uninstalls the differ annotation model from the current quick diff display.
	 * 
	 * @since R3.0
	 */
	private void uninstallChangeRulerModel() {
		IChangeRulerColumn column= getChangeColumn();
		if (column != null)
			column.setModel(null);
	}

	/**
	 * Ensures that either the line number display is a <code>LineNumberChangeRuler</code> or
	 * a separate change ruler gets displayed.
	 * 
	 * @since R3.0
	 */
	private void ensureChangeInfoCanBeDisplayed() {
		if (isLineNumberRulerVisible()) {
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
	 * Creates a new <code>DocumentLineDiffer</code> and installs it with <code>model</code>.
	 * The default reference provider is installed with the newly created differ.
	 * 
	 * @param model the annotation model of the current document.
	 * @return a new <code>DocumentLineDiffer</code> instance.
	 * @since R3.0
	 */
	private DocumentLineDiffer createDiffer(IAnnotationModelExtension model) {
		DocumentLineDiffer differ;
		differ= new DocumentLineDiffer();
		IQuickDiffProviderImplementation provider= getDefaultReferenceProvider();
		if (provider != null)
			differ.setReferenceProvider(provider);
		model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, differ);
		return differ;
	}
	
	/**
	 * Returns the default quick diff reference provider. It is determined by first trying to 
	 * enable the preferred provider as specified by the preferences; if this is unsuccessful, the
	 * default provider as specified by the extension point mechanism is installed. If that fails
	 * as well, <code>null</code> is returned.
	 * 
	 * @return the default reference provider
	 * @since R3.0
	 */
	private IQuickDiffProviderImplementation getDefaultReferenceProvider() {
		String defaultID= getPreferenceStore().getString(TextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
		EditorsPlugin editorPlugin= EditorsPlugin.getDefault();
		ReferenceProviderDescriptor[] descs= editorPlugin.getExtensions();
		IQuickDiffProviderImplementation provider= null;
		// try to fetch preferred provider; load if needed
		for (int i= 0; i < descs.length; i++) {
			if (descs[i].getId().equals(defaultID)) {
				provider= descs[i].createProvider();
				if (provider != null) {
					provider.setActiveEditor(this);
					if (provider.isEnabled())
						break;
					provider.dispose();
					provider= null;
				}
			}
		}
		// if not found, get default provider as specified by the extension point
		if (provider == null) {
			ReferenceProviderDescriptor defaultDescriptor= editorPlugin.getDefaultProvider();
			if (defaultDescriptor != null) {
				provider= defaultDescriptor.createProvider();
				if (provider != null) {
					provider.setActiveEditor(this);
					if (!provider.isEnabled()) {
						provider.dispose();
						provider= null;
					}
				}
			}
		}
		return provider;
	}

	/**
	 * Returns the annotation model associated with the document displayed in the 
	 * viewer if it is an <code>IAnnotationModelExtension</code>, or <code>null</code>.
	 * 
	 * @return the displayed document's annotation model if it is an <code>IAnnotationModelExtension</code>, or <code>null</code> 
	 * @since R3.0
	 */
	private IAnnotationModelExtension getModel() {
		ISourceViewer viewer= getSourceViewer();
		if (viewer == null)
			return null;
		IAnnotationModel m= viewer.getAnnotationModel();
		if (m instanceof IAnnotationModelExtension)
			return (IAnnotationModelExtension) m;
		else
			return null;
	}

	/**
	 * Extracts the line differ from the displayed document's annotation model. If none can be found,
	 * a new differ is created and attached to the annotation model.
	 * 
	 * @return the linediffer, or <code>null</code> if none could be found or created.
	 * @since R3.0
	 */
	private DocumentLineDiffer getOrCreateDiffer() {
		IAnnotationModelExtension model= getModel();
		if (model == null)
			return null;

		DocumentLineDiffer differ= (DocumentLineDiffer)model.getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
		if (differ == null)
			differ= createDiffer(model);
		return differ;
	}

	/**
	 * Returns the <code>IChangeRulerColumn</code> of this editor, or <code>null</code> if there is none. Either
	 * the line number bar or a separate change ruler column can be returned.
	 * 
	 * @return an instance of <code>IChangeRulerColumn</code> or <code>null</code>.
	 * @since R3.0
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
	 * @since R3.0
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
	 * 
	 * @since 2.1
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
	 * 
	 * @since 2.1
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
	 * visible according to the preference store settings.
	 * 
	 * @return <code>true</code> if the line numbers should be visible
	 * @since 2.1
	 */
	private boolean isLineNumberRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(LINE_NUMBER_RULER) : false;
	}

	/**
	 * Returns whether quick diff info should be visible upon opening an editor 
	 * according to the preference store settings.
	 * 
	 * @return <code>true</code> if the line numbers should be visible
	 * @since R3.0
	 */
	private boolean isQuickDiffAlwaysOn() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(TextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON);
	}
	
	/**
	 * Initializes the given line number ruler column from the preference store.
	 * 
	 * @param rulerColumn the ruler column to be initialized
	 * @since 2.1
	 */
	protected void initializeLineNumberRulerColumn(LineNumberRulerColumn rulerColumn) {
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
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
			rulerColumn.setBackground(sharedColors.getColor(rgb));			
			rulerColumn.redraw();
		}
	}
	
	/**
	 * Initializes the given change ruler column from the preference store.
	 * 
	 * @param changeColumn the ruler column to be initialized
	 * @since R3.0
	 */
	private void initializeChangeRulerColumn(IChangeRulerColumn changeColumn) {
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
		IPreferenceStore store= getPreferenceStore();
	
		if (store != null) {
			RGB rgb= null;

			ISourceViewer v= getSourceViewer();
			if (v != null && v.getAnnotationModel() != null) {
				changeColumn.setModel(v.getAnnotationModel());
			}
			
			rgb= null;
			// change color
			if (!store.getBoolean(TextEditorPreferenceConstants.QUICK_DIFF_CHANGED_COLOR)) {
				if (store.contains(TextEditorPreferenceConstants.QUICK_DIFF_CHANGED_COLOR)) {
					if (store.isDefault(TextEditorPreferenceConstants.QUICK_DIFF_CHANGED_COLOR))
						rgb= PreferenceConverter.getDefaultColor(store, TextEditorPreferenceConstants.QUICK_DIFF_CHANGED_COLOR);
					else
						rgb= PreferenceConverter.getColor(store, TextEditorPreferenceConstants.QUICK_DIFF_CHANGED_COLOR);
				}
			}
			changeColumn.setChangedColor(sharedColors.getColor(rgb));
				
			rgb= null;
			// addition color
			if (!store.getBoolean(TextEditorPreferenceConstants.QUICK_DIFF_ADDED_COLOR)) {
				if (store.contains(TextEditorPreferenceConstants.QUICK_DIFF_ADDED_COLOR)) {
					if (store.isDefault(TextEditorPreferenceConstants.QUICK_DIFF_ADDED_COLOR))
						rgb= PreferenceConverter.getDefaultColor(store, TextEditorPreferenceConstants.QUICK_DIFF_ADDED_COLOR);
					else
						rgb= PreferenceConverter.getColor(store, TextEditorPreferenceConstants.QUICK_DIFF_ADDED_COLOR);
				}
			}
			changeColumn.setAddedColor(sharedColors.getColor(rgb));
				
			rgb= null;
			// deletion indicator color
			if (!store.getBoolean(TextEditorPreferenceConstants.QUICK_DIFF_DELETED_COLOR)) {
				if (store.contains(TextEditorPreferenceConstants.QUICK_DIFF_DELETED_COLOR)) {
					if (store.isDefault(TextEditorPreferenceConstants.QUICK_DIFF_DELETED_COLOR))
						rgb= PreferenceConverter.getDefaultColor(store, TextEditorPreferenceConstants.QUICK_DIFF_DELETED_COLOR);
					else
						rgb= PreferenceConverter.getColor(store, TextEditorPreferenceConstants.QUICK_DIFF_DELETED_COLOR);
				}
			}
			changeColumn.setDeletedColor(sharedColors.getColor(rgb));
		}
	
		changeColumn.redraw();
	}

	/**
	 * Creates a new line number ruler column that is appropriately initialized.
	 * 
	 * @since 2.1
	 */
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		if (isQuickDiffAlwaysOn() || isChangeInformationShowing()) {
			LineNumberChangeRulerColumn column= new LineNumberChangeRulerColumn();
			column.setHover(new LineChangeHover());
			initializeChangeRulerColumn(column);
			fLineNumberRulerColumn= column;
		} else {
			fLineNumberRulerColumn= new LineNumberRulerColumn();
		}
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		return fLineNumberRulerColumn;
	}
	
	/**
	 * Creates a new change ruler column for quick diff display independent of the 
	 * line number ruler column
	 * 
	 * @return a new change ruler column
	 * @since R3.0
	 */
	protected IChangeRulerColumn createChangeRulerColumn() {
		IChangeRulerColumn column= new ChangeRulerColumn();
		column.setHover(new LineChangeHover());
		fChangeRulerColumn= column;
		initializeChangeRulerColumn(fChangeRulerColumn);
		return fChangeRulerColumn;
	}
	
	/*
	 * @see AbstractTextEditor#createVerticalRuler()
	 * @since 2.1
	 */
	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH));
		
		if (isLineNumberRulerVisible())
			ruler.addDecorator(1, createLineNumberRulerColumn());
		else if (isQuickDiffAlwaysOn())
			ruler.addDecorator(1, createChangeRulerColumn());
			
		return ruler;
	}
	
	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 * @since 2.1
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
			
			if (LINE_NUMBER_RULER.equals(property)) {
				if (isLineNumberRulerVisible())
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
			
			if (TextEditorPreferenceConstants.QUICK_DIFF_CHANGED_COLOR.equals(property)
				||	TextEditorPreferenceConstants.QUICK_DIFF_ADDED_COLOR.equals(property)
				||	TextEditorPreferenceConstants.QUICK_DIFF_DELETED_COLOR.equals(property)) {
						
				if (fLineNumberRulerColumn instanceof IChangeRulerColumn)
					initializeChangeRulerColumn((IChangeRulerColumn) fLineNumberRulerColumn);
				else if (fChangeRulerColumn != null)
					initializeChangeRulerColumn(fChangeRulerColumn);
			}
							
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}
	
	/*
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @since 2.1
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (fSourceViewerDecorationSupport != null)
			fSourceViewerDecorationSupport.install(getPreferenceStore());
		
		if (isQuickDiffAlwaysOn())
			showChangeInformation(true);
	}

}
