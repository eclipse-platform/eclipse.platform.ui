package org.eclipse.ui.texteditor;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;




/**
 * Abstract base implementation of a text editor.
 * <p>
 * Subclasses are responsible for configuring the editor appropriately.
 * The standard text editor, <code>TextEditor</code>, is one such example.
 * </p>
 *
 * @see org.eclipse.ui.editors.text.TextEditor
 */
public abstract class AbstractTextEditor extends EditorPart implements ITextEditor {
	
	/**
	 * Internal element state listener.
	 */
	class ElementStateListener implements IElementStateListener {
		
		private ITextSelection fRememberedSelection;
		
		/*
		 * @see IElementStateListener#elementDirtyStateChanged
		 */
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			if (element != null && element.equals(getEditorInput())) {
				firePropertyChange(PROP_DIRTY);
				if (!isDirty && fSourceViewer != null)
					fSourceViewer.resetPlugins();
			}
		}
		
		/*
		 * @see IElementStateListener#elementContentAboutToBeReplaced
		 */
		public void elementContentAboutToBeReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				resetHighlightRange();
				ISelectionProvider sp= getSelectionProvider();
				fRememberedSelection= (sp == null ? null : (ITextSelection) sp.getSelection());
			}
		}
		
		/*
		 * @see IElementStateListener#elementContentReplaced
		 */
		public void elementContentReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				firePropertyChange(PROP_DIRTY);
				restoreSelection(fRememberedSelection);
				if (fSourceViewer != null)
					fSourceViewer.resetPlugins();
			}
		}
		
		/*
		 * @see IElementStateListener#elementDeleted
		 */
		public void elementDeleted(Object deletedElement) {
			if (deletedElement != null && deletedElement.equals(getEditorInput()))
				close(false);		
		}
		
		/*
		 * @see IElementStateListener#elementMoved
		 */
		public void elementMoved(Object originalElement, Object movedElement) {
			if (originalElement != null && 
					originalElement.equals(getEditorInput()) &&
					(movedElement == null || movedElement instanceof IEditorInput)) {
				ITextSelection s= (ITextSelection) getSelectionProvider().getSelection();
				setInput((IEditorInput) movedElement);
				restoreSelection(s);
			}
		}
		
		/**
		 * Restores the given selection in the editor.
		 * 
		 * @param selection the selection to be restored
		 */
		private void restoreSelection(final ITextSelection selection) {
			
			if (selection == null || fSourceViewer == null)
				return;
				
			IDocument document= fSourceViewer.getDocument();
			int offset= selection.getOffset();
			int length= selection.getLength();
			if (offset + length <= document.getLength())
				selectAndReveal(offset, length);
		} 
	};
	
	/**
	 * Internal text listener.
	 */
	class TextListener implements ITextListener {
		
		private Runnable fRunnable= new Runnable() {
			public void run() {
				updateContentDependentActions();
			}
		};
		
		private Display fDisplay;
		
		/**
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			
			if (fDisplay == null)
				fDisplay= getSite().getShell().getDisplay();
				
			fDisplay.asyncExec(fRunnable);
		}
	};
	
	/**
	 * Internal property change listener.
	 */
	class PropertyChangeListener implements IPropertyChangeListener {
		/**
		 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			handlePreferenceStoreChanged(event);
		}
	};
	
	
	/** Key used to look up font preference */
	public final static String PREFERENCE_FONT= "AbstractTextEditor.Font";
	/** Menu id for the editor context menu. */
	public final static String DEFAULT_EDITOR_CONTEXT_MENU_ID= "#EditorContext";
	/** Menu id for the ruler context menu. */
	public final static String DEFAULT_RULER_CONTEXT_MENU_ID= "#RulerContext";
	/** The width of the vertical ruler */
	protected final static int VERTICAL_RULER_WIDTH= 12;
	
	
	/** The editor's internal document provider */
	private IDocumentProvider fInternalDocumentProvider;
	/** The editor's external document provider */
	private IDocumentProvider fExternalDocumentProvider;
	/** The editor's resource bundle */
	private ResourceBundle fResourceBundle;
	/** The prefix used for looking up keys in the resource bundle */
	private String fResourceKeyPrefix;
	/** The editor's preference store */
	private IPreferenceStore fPreferenceStore;
	/** The editor's range indicator */
	private Annotation fRangeIndicator;
	/** The editor's source viewer configuration */
	private SourceViewerConfiguration fConfiguration;
	/** The editor's source viewer */
	private ISourceViewer fSourceViewer;
	/** The editor's font */
	private Font fFont;
	/** The editor's vertical ruler */
	private IVerticalRuler fVerticalRuler;
	/** The editor's context menu id */
	private String fEditorContextMenuId;
	/** The ruler's context menu id */
	private String fRulerContextMenuId;
	/** The editor's presentation mode */
	private boolean fShowHighlightRangeOnly;
	/** The actions registered with the editor */	
	private Map fActions= new HashMap(10);
	/** The actions marked as selection dependent */
	private List fSelectionActions= new ArrayList(5);
	/** The actions marked as content dependent */
	private List fContentActions= new ArrayList(5);
	/** Context menu listener */
	private IMenuListener fMenuListener;
	/** Vertical ruler mouse listener */
	private MouseListener fMouseListener;
	/** Selection changed listener */
	private ISelectionChangedListener fSelectionChangedListener;
	/** Title image to be disposed */
	private Image fTitleImage;
	/** The text context menu to be disposed */
	private Menu fTextContextMenu;
	/** The ruler context menu to be disposed */
	private Menu fRulerContextMenu;
	/** The editor's element state listener */
	private IElementStateListener fElementStateListener= new ElementStateListener();
	/** The editor's text listener */
	private ITextListener fTextListener= new TextListener();
	/** The editor's property change listener */
	private IPropertyChangeListener fPropertyChangeListener= new PropertyChangeListener();
	
	/**
	 * Creates a new text editor. It initializes the editor and ruler context
	 * menu id with the predefined names. If not explicitly set, this
	 * editor uses a <code>SourceViewerConfiguration</code> to configure its
	 * source viewer. This viewer does not have a range indicator installed.
	 */
	protected AbstractTextEditor() {
		super();
		fEditorContextMenuId= DEFAULT_EDITOR_CONTEXT_MENU_ID;
		fRulerContextMenuId= DEFAULT_RULER_CONTEXT_MENU_ID;
	}
	/**
	 * Convenience method to add the action installed under the given action id
	 * to the given menu.
	 */
	protected final void addAction(IMenuManager menu, String actionId) {
		IAction action= getAction(actionId);
		if (action != null) {
			if (action instanceof IUpdate)
				((IUpdate) action).update();
			menu.add(action);
		}
	}
	/**
	 * Convenience method to add the action installed under the given action id
	 * to the specified group of the menu.
	 */
	protected final void addAction(IMenuManager menu, String group, String actionId) {
	 	IAction action= getAction(actionId);
	 	if (action != null) {
	 		if (action instanceof IUpdate)
	 			((IUpdate) action).update();
	 			
	 		IMenuManager subMenu= menu.findMenuUsingPath(group);
	 		if (subMenu != null)
	 			subMenu.add(action);
	 		else
	 			menu.appendToGroup(group, action);
	 	}
	}
	/**
	 * Convenience method to add a new group after the specified group.
	 */
	protected final void addGroup(IMenuManager menu, String existingGroup, String newGroup) {
 		IMenuManager subMenu= menu.findMenuUsingPath(existingGroup);
 		if (subMenu != null)
 			subMenu.add(new Separator(newGroup));
 		else
 			menu.appendToGroup(existingGroup, new Separator(newGroup));
	}
	/**
	 * Adjusts the highlight range so that at least the specified range 
	 * is highlighted. <p>
	 * Subclasses may re-implement this method.
	 *
	 * @param offset the offset of the range which at least should be highlighted
	 * @param length the length of the range which at least should be highlighted 
	 */
	protected void adjustHighlightRange(int offset, int length) {
		if (fSourceViewer == null)
			return;
		
		if (!fSourceViewer.overlapsWithVisibleRegion(offset, length))
			fSourceViewer.resetVisibleRegion();
	}
	/*
	 * @see ITextEditor#close
	 */
	public void close(final boolean save) {
		
		Display display= getSite().getShell().getDisplay();
		
		display.syncExec(new Runnable() {
			public void run() {
				getSite().getPage().closeEditor(AbstractTextEditor.this, save);
			}
		});
	}
	/**
	 * Creates this editor's standard actions and connects them with the global
	 * workbench actions.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	protected void createActions() {
		
		setAction(ITextEditorActionConstants.UNDO, new TextOperationAction(getResourceBundle(), "Undo.", this, ITextOperationTarget.UNDO));
		setAction(ITextEditorActionConstants.REDO, new TextOperationAction(getResourceBundle(), "Redo.", this, ITextOperationTarget.REDO));
		setAction(ITextEditorActionConstants.CUT, new TextOperationAction(getResourceBundle(), "Cut.", this, ITextOperationTarget.CUT));
		setAction(ITextEditorActionConstants.COPY, new TextOperationAction(getResourceBundle(), "Copy.", this, ITextOperationTarget.COPY));
		setAction(ITextEditorActionConstants.PASTE, new TextOperationAction(getResourceBundle(), "Paste.", this, ITextOperationTarget.PASTE));
		setAction(ITextEditorActionConstants.DELETE, new TextOperationAction(getResourceBundle(), "Delete.", this, ITextOperationTarget.DELETE));
		setAction(ITextEditorActionConstants.SELECT_ALL, new TextOperationAction(getResourceBundle(), "SelectAll.", this, ITextOperationTarget.SELECT_ALL));
		setAction(ITextEditorActionConstants.SHIFT_RIGHT, new TextOperationAction(getResourceBundle(), "ShiftRight.", this, ITextOperationTarget.SHIFT_RIGHT));
		setAction(ITextEditorActionConstants.SHIFT_LEFT, new TextOperationAction(getResourceBundle(), "ShiftLeft.", this, ITextOperationTarget.SHIFT_LEFT));
		setAction(ITextEditorActionConstants.FIND, new FindReplaceAction(getResourceBundle(), "FindReplace.", getSite().getWorkbenchWindow()));
		setAction(ITextEditorActionConstants.BOOKMARK, new AddMarkerAction(getResourceBundle(), "AddBookmark.", this, IMarker.BOOKMARK, true));
		setAction(ITextEditorActionConstants.ADD_TASK, new AddMarkerAction(getResourceBundle(), "AddTask.", this, IMarker.TASK, true));
		setAction(ITextEditorActionConstants.SAVE, new SaveAction(getResourceBundle(), "Save.", this));
		setAction(ITextEditorActionConstants.REVERT_TO_SAVED, new RevertToSavedAction(getResourceBundle(), "Revert.", this));
		setAction(ITextEditorActionConstants.GOTO_LINE, new GotoLineAction(getResourceBundle(), "GotoLine.", this));
		
		setAction(ITextEditorActionConstants.RULER_MANAGE_BOOKMARKS, new MarkerRulerAction(getResourceBundle(), "ManageBookmarks.", fVerticalRuler, this, IMarker.BOOKMARK, true));
		setAction(ITextEditorActionConstants.RULER_MANAGE_TASKS, new MarkerRulerAction(getResourceBundle(), "ManageTasks.", fVerticalRuler, this, IMarker.TASK, true));
		setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, getAction(ITextEditorActionConstants.RULER_MANAGE_BOOKMARKS));
		
		markAsContentDependentAction(ITextEditorActionConstants.UNDO, true);
		markAsContentDependentAction(ITextEditorActionConstants.REDO, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND, true);
		
		markAsSelectionDependentAction(ITextEditorActionConstants.CUT, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.COPY, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.PASTE, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.DELETE, true);
	}
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IWorkbenchPart</code> method creates the vertical ruler and
	 * source viewer. Subclasses may extend.
	 */
	public void createPartControl(Composite parent) {
				
		fVerticalRuler= createVerticalRuler();
		
		int styles= SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
		fSourceViewer= createSourceViewer(parent, fVerticalRuler, styles);
		
		if (fConfiguration == null)
			fConfiguration= new SourceViewerConfiguration();
		fSourceViewer.configure(fConfiguration);
		
		if (fRangeIndicator != null)
			fSourceViewer.setRangeIndicator(fRangeIndicator);
		
		fSourceViewer.addTextListener(fTextListener);
		getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
				
		StyledText styledText= fSourceViewer.getTextWidget();
		initializeWidgetFont(styledText);
		
		MenuManager manager= new MenuManager(fEditorContextMenuId, fEditorContextMenuId);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(getContextMenuListener());
		fTextContextMenu= manager.createContextMenu(styledText);
		styledText.setMenu(fTextContextMenu);
		getSite().registerContextMenu(fEditorContextMenuId, manager, getSelectionProvider());
		
		Control ruler= fVerticalRuler.getControl();
		manager= new MenuManager(fRulerContextMenuId, fRulerContextMenuId);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(getContextMenuListener());		
		fRulerContextMenu= manager.createContextMenu(ruler);
		ruler.setMenu(fRulerContextMenu);
		ruler.addMouseListener(getRulerMouseListener());
		getSite().registerContextMenu(fRulerContextMenuId, manager, getSelectionProvider());
		
		createActions();
		
		getSite().setSelectionProvider(getSelectionProvider());
		
		initializeSourceViewer(getEditorInput());
	}
	/**
	 * Creates the source viewer to be used by this editor.
	 * Subclasses may re-implement this method.
	 *
	 * @param parent the parent control
	 * @param ruler the vertical ruler
	 * @param styles style bits
	 * @return the source viewer
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		return new SourceViewer(parent, ruler, styles);
	}
	/**
	 * Creates the vertical ruler to be used by this editor.
	 * Subclasses may re-implement this method.
	 *
	 * @return the vertical ruler
	 */
	protected IVerticalRuler createVerticalRuler() {
		return new VerticalRuler(VERTICAL_RULER_WIDTH);
	}
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IWorkbenchPart</code> method may be extended by subclasses.
	 * Subclasses must call <code>super.dispose()</code>.
	 */
	public void dispose() {
		
		if (fTitleImage != null) {
			fTitleImage.dispose();
			fTitleImage= null;
		}
		
		if (fFont != null) {
			fFont.dispose();
			fFont= null;
		}
		
		if (fPropertyChangeListener != null) {
			if (fPreferenceStore != null) {
				fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
				fPreferenceStore= null;
			}
			fPropertyChangeListener= null;
		}
		
		IDocumentProvider provider= getDocumentProvider();
		if (provider != null) {
			
			IEditorInput input= getEditorInput();
			if (input != null)
				provider.disconnect(input);
			
			if (fElementStateListener != null) {
				provider.removeElementStateListener(fElementStateListener);
				fElementStateListener= null;
			}
			
			fInternalDocumentProvider= null;
			fExternalDocumentProvider= null;
		}
		
		if (fSourceViewer != null) {
			if (fTextListener != null) {
				fSourceViewer.removeTextListener(fTextListener);
				fTextListener= null;
			}
			fSourceViewer= null;
		}
		
		if (fTextContextMenu != null) {
			fTextContextMenu.dispose();
			fTextContextMenu= null;
		}
		
		if (fRulerContextMenu != null) {
			fRulerContextMenu.dispose();
			fRulerContextMenu= null;
		}
		
		super.dispose();
	}
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>ITextEditor</code> method may be extended by subclasses.
	 */
	public void doRevertToSaved() {
		
		IDocumentProvider p= getDocumentProvider();
		if (p == null)
			return;
			
		try {
			
			p.resetDocument(getEditorInput());
			
			IAnnotationModel model= p.getAnnotationModel(getEditorInput());
			if (model instanceof AbstractMarkerAnnotationModel) {
				AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) model;
				markerModel.resetMarkers();
			}
						
			firePropertyChange(PROP_DIRTY);
			
		} catch (CoreException x) {
			String title= getResourceString("Error.revert.title");
			String msg= getResourceString("Error.revert.message");
			Shell shell= getSite().getShell();
			ErrorDialog.openError(shell, title, msg, x.getStatus());
		}
	}
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method may be extended by subclasses.
	 */
	public void doSave(IProgressMonitor progressMonitor) {
		
		IDocumentProvider p= getDocumentProvider();
		if (p == null)
			return;
		
		WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				getDocumentProvider().saveDocument(monitor, getEditorInput(), getDocumentProvider().getDocument(getEditorInput()));
			}
		};
		
		try {
			
			p.aboutToChange(getEditorInput());
			operation.run(progressMonitor);
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			
			Shell shell= getSite().getShell();
			String title= getResourceString("Error.save.title");
			String msg= getResourceString("Error.save.message");
			
			Throwable t= x.getTargetException();
			if (t instanceof CoreException) {
				CoreException cx= (CoreException) t;
				ErrorDialog.openError(shell, title, msg, cx.getStatus());
			} else {
				MessageDialog.openError(shell, title, msg + t.getMessage());
			}
			
		} finally {
			p.changed(getEditorInput());
		}
	}
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method does nothing. Subclasses may reimplement.
	 */
	public void doSaveAs() {
	}
	/**
	 * Internal <code>setInput</code> method.
	 *
	 * @param input the input to be set
	 * @exception CoreException if input cannot be connected to the document provider
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		
		if (input == null)
			
			close(isSaveOnCloseNeeded());
		
		else {
			
			IEditorInput oldInput= getEditorInput();
			if (oldInput != null)
				getDocumentProvider().disconnect(oldInput);
			
				
			super.setInput(input);
			
			updateDocumentProvider(input);
			
			IDocumentProvider provider= getDocumentProvider();
			if (provider == null) {
				IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, getResourceString("Error.no_provider"), null);
				throw new CoreException(s);
			}
			
			provider.connect(input);
			
			initializeTitle(input);
			if (fSourceViewer != null)
				initializeSourceViewer(input);
		}
	}
	/**
	 * Sets up this editor's context menu before it is made visible.
	 * <p>
	 * Subclasses may extend to add other actions.
	 * </p>
	 *
	 * @param menu the menu
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		
		if (isEditable()) {
			menu.add(new Separator(ITextEditorActionConstants.GROUP_UNDO));
			addAction(menu, ITextEditorActionConstants.UNDO);
			addAction(menu, ITextEditorActionConstants.REDO);
			addAction(menu, ITextEditorActionConstants.REVERT_TO_SAVED);			
		}
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_COPY));
		if (isEditable()) {
			addAction(menu, ITextEditorActionConstants.CUT);
			addAction(menu, ITextEditorActionConstants.COPY);
			addAction(menu, ITextEditorActionConstants.PASTE);
			addAction(menu, ITextEditorActionConstants.SELECT_ALL);
		} else {
			addAction(menu, ITextEditorActionConstants.COPY);
			addAction(menu, ITextEditorActionConstants.SELECT_ALL);
		}
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		addAction(menu, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.SHIFT_LEFT);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_FIND));
		addAction(menu, ITextEditorActionConstants.FIND);
		addAction(menu, ITextEditorActionConstants.GOTO_LINE);
		
		String label= getResourceString("AddMenu.label", "A&dd");
		MenuManager submenu= new MenuManager(label, ITextEditorActionConstants.GROUP_ADD);
		addAction(submenu, ITextEditorActionConstants.BOOKMARK);
		addAction(submenu, ITextEditorActionConstants.ADD_TASK);
		menu.add(submenu);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_SAVE));
		if (isEditable())
			addAction(menu, ITextEditorActionConstants.SAVE);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_REST));
		menu.add(new Separator(ITextEditorActionConstants.MB_ADDITIONS));
	}
	/*
	 * @see EditorPart#firePropertyChange
	 */
	protected void firePropertyChange(int property) {
		super.firePropertyChange(property);
	}
	/*
	 * @see ITextEditor#getAction
	 */
	public IAction getAction(String actionID) {
		Assert.isNotNull(actionID);
		return (IAction) fActions.get(actionID);
	}
	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required))
			return (fSourceViewer == null ? null : fSourceViewer.getFindReplaceTarget());
		if (ITextOperationTarget.class.equals(required))
			return (fSourceViewer == null ? null : fSourceViewer.getTextOperationTarget());
		return super.getAdapter(required);
	}
	/**
	 * Creates and returns the listener on this editor's context menus.
	 *
	 * @return the menu listener
	 */
	protected final IMenuListener getContextMenuListener() {
		if (fMenuListener == null) {
			fMenuListener= new IMenuListener() {
				
				public void menuAboutToShow(IMenuManager menu) {
					String id= menu.getId();
					if (getRulerContextMenuId().equals(id)) {
						setFocus();
						rulerContextMenuAboutToShow(menu);
					} else if (getEditorContextMenuId().equals(id)) {
						setFocus();
						editorContextMenuAboutToShow(menu);
					}
				}
			};
		}
		return fMenuListener;
	}
	/*
	 * @see ITextEditor#getDocumentProvider
	 */
	public IDocumentProvider getDocumentProvider() {
		if (fInternalDocumentProvider != null)
			return fInternalDocumentProvider;
		return fExternalDocumentProvider;
	}
	/** 
	 * Returns the editor's context menu id.
	 *
	 * @return the editor's context menu id
	 */
	protected final String getEditorContextMenuId() {
		return fEditorContextMenuId;
	}
	/*
	 * @see ITextEditor#getHighlightRange
	 */
	public IRegion getHighlightRange() {
		if (fSourceViewer == null)
			return null;
			
		if (fShowHighlightRangeOnly)
			return fSourceViewer.getVisibleRegion();
			
		return fSourceViewer.getRangeIndication();
	}
	/**
	 * Returns this editor's preference store.
	 * 
	 * @return this editor's preference store
	 */
	protected final IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}
	/** 
	 * Returns the editor's range indicator. 
	 *
	 * @return the editor's range indicator
	 */
	protected final Annotation getRangeIndicator() {
		return fRangeIndicator;
	}
	/** 
	 * Returns the editor's resource bundle.
	 *
	 * @return the editor's resource bundle
	 */
	private ResourceBundle getResourceBundle() {
		if (fResourceBundle == null)
			fResourceBundle= ResourceBundle.getBundle("org.eclipse.ui.texteditor.AbstractTextEditorResources");
		return fResourceBundle;
	}
	/**
	 * Convenience method for safely accessing resources.
	 */
	private String getResourceString(String key) {
		return getResourceString(key, "");
	}
	/**
	 * Convenience method for safely accessing resources.
	 */
	private String getResourceString(String key, String dfltValue) {
		try {
			if (getResourceBundle() != null && key != null)
				return getResourceBundle().getString(key);
		} catch (MissingResourceException x) {
		}
		return dfltValue;
	}
	/** 
	 * Returns the ruler's context menu id.
	 *
	 * @return the ruler's context menu id
	 */
	protected final String getRulerContextMenuId() {
		return fRulerContextMenuId;
	}
	/**
	 * Creates and returns the listener on this editor's vertical ruler.
	 *
	 * @return the mouse listener
	 */
	protected final MouseListener getRulerMouseListener() {
		if (fMouseListener == null) {
			fMouseListener= new MouseListener() {
				
				public void mouseDown(MouseEvent e) {}
				
				public void mouseUp(MouseEvent e) {}
				
				public void mouseDoubleClick(MouseEvent e) {
					IAction action= getAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK);
					if (action != null) {
						if (action instanceof IUpdate)
							((IUpdate) action).update();
						action.run();
					}
				}
			};
		}
		return fMouseListener;
	}
	/**
	 * Returns this editor's the selection changed listener to be installed
	 * on the editor's source viewer.
	 *
	 * @return the listener
	 */
	protected final ISelectionChangedListener getSelectionChangedListener() {
		if (fSelectionChangedListener == null) {
			fSelectionChangedListener= new ISelectionChangedListener() {
				
				private Runnable fRunnable= new Runnable() {
					public void run() {
						updateSelectionDependentActions();
					}
				};
				
				private Display fDisplay;
				
				public void selectionChanged(SelectionChangedEvent event) {
					if (fDisplay == null)
						fDisplay= getSite().getShell().getDisplay();
						
					fDisplay.asyncExec(fRunnable);	
				}
			};
		}
		
		return fSelectionChangedListener;
	}
	/*
	 * @see ITextEditor#getSelectionProvider
	 */
	public ISelectionProvider getSelectionProvider() {
		return (fSourceViewer != null ? fSourceViewer.getSelectionProvider() : null);
	}
	/** 
	 * Returns the editor's source viewer.
	 *
	 * @return the editor's source viewer
	 */
	protected final ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}
	/** 
	 * Returns the editor's source viewer configuration.
	 *
	 * @return the editor's source viewer configuration
	 */
	protected final SourceViewerConfiguration getSourceViewerConfiguration() {
		return fConfiguration;
	}
	/** 
	 * Returns the editor's vertical ruler.
	 * 
	 * @return the editor's vertical ruler
	 */
	protected final IVerticalRuler getVerticalRuler() {
		return fVerticalRuler;
	}
	/**
	 * If the editor can be saved all marker ranges have been changed according to
	 * the text manipulations. However, those changes are not yet propagated to the
	 * marker manager. Thus, when opening a marker, the marker's position in the editor
	 * must be determined as it might differ from the position stated in the marker.
	 * @see EditorPart#gotoMarker
	 */
	public void gotoMarker(IMarker marker) {
		
		if (fSourceViewer == null)
			return;
		
		int start= MarkerUtilities.getCharStart(marker);
		int end= MarkerUtilities.getCharEnd(marker);
		
		if (start < 0 || end < 0) {
			
			// there is only a line number
			int line= MarkerUtilities.getLineNumber(marker);
			if (line > -1) {
				
				// marker line numbers are 1-based
				-- line;
				
				try {
					
					IDocument document= getDocumentProvider().getDocument(getEditorInput());
					selectAndReveal(document.getLineOffset(line), document.getLineLength(line));
				
				} catch (BadLocationException x) {
					// marker refers to invalid text position -> do nothing
				}
			}
			
		} else {
		
			// look up the current range of the marker when the document has been edited
			IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
			if (model instanceof AbstractMarkerAnnotationModel) {
				
				AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) model;
				Position pos= markerModel.getMarkerPosition(marker);
				if (pos == null || pos.isDeleted()) {
					// do nothing if position has been deleted
					return;
				}
				
				start= pos.getOffset();
				end= pos.getOffset() + pos.getLength();
			}
			
			IDocument document= getDocumentProvider().getDocument(getEditorInput());
			int length= document.getLength();
			if (end - 1 < length && start < length)
				selectAndReveal(start, end - start);
		}
	}
	/**
	 * Handles a property change event describing a change
	 * of the editor's preference store and updates the preference
	 * related editor properties.
	 * 
	 * @param event the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		if (fSourceViewer == null)
			return;
			
		if (PREFERENCE_FONT.equals(event.getProperty()))
			initializeWidgetFont(fSourceViewer.getTextWidget());
	}
	/*
	 * @see IEditorPart#init
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		setSite(site);
		
		try {
			doSetInput(input);
		} catch (CoreException x) {
			throw new PartInitException(x.getMessage());
		}
	}
	/**
	 * Initializes the editor's source viewer based on the given editor input.
	 *
	 * @param input the editor input to be used to initialize the source viewer
	 */
	private void initializeSourceViewer(IEditorInput input) {
		
		IAnnotationModel model= getDocumentProvider().getAnnotationModel(input);
		IDocument document= getDocumentProvider().getDocument(input);
		
		if (document != null) {
			fSourceViewer.setDocument(document, model);
			fSourceViewer.setEditable(isEditable());
			fSourceViewer.showAnnotations(model != null);			
		}
	}
	/**
	 * Initializes the editor's title based on the given editor input.
	 *
	 * @param input the editor input to be used
	 */
	private void initializeTitle(IEditorInput input) {
		
		Image oldImage= fTitleImage;
		fTitleImage= null;
		String title= "";
		
		if (input != null) {
			ImageDescriptor imageDesc= input.getImageDescriptor();
			fTitleImage= imageDesc != null ? imageDesc.createImage() : null;
			title= input.getName();
		}
		
		setTitleImage(fTitleImage);
		setTitle(title);
		
		firePropertyChange(PROP_DIRTY);
		
		if (oldImage != null && !oldImage.isDisposed())
			oldImage.dispose();
	}
	/*
	 * Initializes the given widget's font.
	 * 
	 * @param styledText the widget to be initialized
	 */
	private void initializeWidgetFont(StyledText styledText) {
		
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			
			FontData data= null;
			
			if (store.contains(PREFERENCE_FONT) && !store.isDefault(PREFERENCE_FONT))
				data= PreferenceConverter.getFontData(store, PREFERENCE_FONT);
			else
				data= PreferenceConverter.getDefaultFontData(store, PREFERENCE_FONT);
			
			if (data != null) {
				
				Font font= new Font(styledText.getDisplay(), data);
				styledText.setFont(font);
				
				if (fFont != null)
					fFont.dispose();
					
				fFont= font;
				return;
			}
		}
		
		// if all the preferences failed
		styledText.setFont(JFaceResources.getTextFont());
	}
	/*
	 * @see IEditorPart#isDirty
	 */
	public boolean isDirty() {
		IDocumentProvider p= getDocumentProvider();
		return p == null ? false : p.canSaveDocument(getEditorInput());
	}
	/*
	 * @see ITextEditor#isEditable
	 */
	public boolean isEditable() {
		return true;
	}
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method returns <code>false</code>. Subclasses
	 * may override.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	/*
	 * @see IEditorPart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		IDocumentProvider p= getDocumentProvider();
		return p == null ? false : p.mustSaveDocument(getEditorInput());
	}
	/**
	 * Marks or unmarks the given action to be updated on content changes.
	 *
	 * @param actionId the action id
	 * @param mark <code>true</code> if the action is content dependent
	 */
	public void markAsContentDependentAction(String actionId, boolean mark) {
		Assert.isNotNull(actionId);
		if (mark) {
			if (!fContentActions.contains(actionId))
				fContentActions.add(actionId);
		} else
			fContentActions.remove(actionId);
	}
	/**
	 * Marks or unmarks the given action to be updated on text selection changes.
	 *
	 * @param actionId the action id
	 * @param mark <code>true</code> if the action is selection dependent
	 */
	public void markAsSelectionDependentAction(String actionId, boolean mark) {
		Assert.isNotNull(actionId);
		if (mark) {
			if (!fSelectionActions.contains(actionId))
				fSelectionActions.add(actionId);
		} else
			fSelectionActions.remove(actionId);
	}
	/*
	 * @see ITextEditor#resetHighlightRange	 
	 */
	public void resetHighlightRange() {
		if (fSourceViewer == null)
			return;
		
		if (fShowHighlightRangeOnly)
			fSourceViewer.resetVisibleRegion();
		else
			fSourceViewer.removeRangeIndication();
	}
	/**
	 * Sets up the ruler context menu before it is made visible.
	 * <p>
	 * Subclasses may extend to add other actions.
	 * </p>
	 *
	 * @param menu the menu
	 */
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		addAction(menu, ITextEditorActionConstants.RULER_MANAGE_BOOKMARKS);
		addAction(menu, ITextEditorActionConstants.RULER_MANAGE_TASKS);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_REST));
		menu.add(new Separator(ITextEditorActionConstants.MB_ADDITIONS));
	}
	/*
	 * @see ITextEditor#selectAndReveal
	 */
	public void selectAndReveal(int start, int length) {
		if (fSourceViewer == null)
			return;
		
		adjustHighlightRange(start, length);
			
		fSourceViewer.revealRange(start, length);
		fSourceViewer.setSelectedRange(start, length);
	}
	/*
	 * @see ITextEditor#setAction
	 */
	public void setAction(String actionID, IAction action) {
		Assert.isNotNull(actionID);
		if (action == null)
			fActions.remove(actionID);
		else
			fActions.put(actionID, action);
	}
	/**
	 * Sets this editor's document provider. This method must be 
	 * called before the editor's control is created.
	 *
	 * @param provider the document provider
	 */
	protected void setDocumentProvider(IDocumentProvider provider) {
		Assert.isNotNull(provider);
		fInternalDocumentProvider= provider;
	}
	/**
	 * Sets this editor's context menu id.
	 *
	 * @param contextMenuId the content menu id
	 */
	protected void setEditorContextMenuId(String contextMenuId) {
		Assert.isNotNull(contextMenuId);
		fEditorContextMenuId= contextMenuId;
	}
	/*
	 * @see IDesktopPart#setFocus()
	 */
	public void setFocus() {
		if (fSourceViewer != null)
			fSourceViewer.getTextWidget().setFocus();
	}
	/*
	 * @see ITextEditor#setHighlightRange
	 */
	public void setHighlightRange(int start, int length, boolean moveCursor) {
		if (fSourceViewer == null)
			return;
			
		if (fShowHighlightRangeOnly && moveCursor)
			fSourceViewer.setVisibleRegion(start, length);
		else
			fSourceViewer.setRangeIndication(start, length, moveCursor);
	}
	/*
	 * @see EditorPart#setInput
	 */
	public final void setInput(IEditorInput input) {
		
		try {
			
			doSetInput(input);
				
		} catch (CoreException x) {
			String title= getResourceString("Error.setinput.title");
			String msg= getResourceString("Error.setinput.message");
			Shell shell= getSite().getShell();
			ErrorDialog.openError(shell, title, msg, x.getStatus());
		}				
	}
	/**
	 * Sets this editor's preference store. This method must be
	 * called before the editor's control is created.
	 * 
	 * @param store the new preference store
	 */
	protected void setPreferenceStore(IPreferenceStore store) {
		if (fPreferenceStore != null)
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
			
		fPreferenceStore= store;
		
		if (fPreferenceStore != null)
			fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);
	}
	/**
	 * Sets the annotation which this editor uses to represent the highlight
	 * range if the editor is configured to show the entire document. If the
	 * range indicator is not set, this editor uses a <code>DefaultRangeIndicator</code>.
	 *
	 * @param rangeIndicator the annotation
	 */
	protected void setRangeIndicator(Annotation rangeIndicator) {
		Assert.isNotNull(rangeIndicator);
		fRangeIndicator= rangeIndicator;
	}
	/**
	 * Sets the ruler's context menu id.
	 *
	 * @param contextMenuId the content menu id
	 */
	protected void setRulerContextMenuId(String contextMenuId) {
		Assert.isNotNull(contextMenuId);
		fRulerContextMenuId= contextMenuId;
	}
	/**
	 * Sets this editor's source viewer configuration used to configure its
	 * internal source viewer. This method must be called before the editor's
	 * control is created. If not, this editor uses a <code>SourceViewerConfiguration</code>.
	 *
	 * @param configuration the source viewer configuration object
	 */
	protected void setSourceViewerConfiguration(SourceViewerConfiguration configuration) {
		Assert.isNotNull(configuration);
		fConfiguration= configuration;
	}
	/*
	 * @see ITextEditor#showHighlightRangeOnly
	 */
	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
		fShowHighlightRangeOnly= showHighlightRangeOnly;
	}
	/*
	 * @see ITextEditor#showsHighlightRangeOnly
	 */
	public boolean showsHighlightRangeOnly() {
		return fShowHighlightRangeOnly;
	}
	/**
	 * Updates the specified action by calling <code>IUpdate.update</code>
	 * if applicable.
	 *
	 * @param actionId the action id
	 */
	private void updateAction(String actionId) {
		Assert.isNotNull(actionId);
		IAction action= (IAction) fActions.get(actionId);
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}
	/**
	 * Updates all content dependent actions.
	 */
	protected void updateContentDependentActions() {
		Iterator e= fContentActions.iterator();
		while (e.hasNext())
			updateAction((String) e.next());
	}
	/**
	 * If there is no implicit document provider set, the external one is
	 * re-initialized based on the given editor input.
	 *
	 * @param input the editor input.
	 */
	private void updateDocumentProvider(IEditorInput input) {
		if (getDocumentProvider() != null)
			getDocumentProvider().removeElementStateListener(fElementStateListener);
			
		if (fInternalDocumentProvider == null)
			fExternalDocumentProvider= DocumentProviderRegistry.getDefault().getDocumentProvider(input);
			
		if (getDocumentProvider() != null)
			getDocumentProvider().addElementStateListener(fElementStateListener);
	}
	/**
	 * Updates all selection dependent actions.
	 */
	protected void updateSelectionDependentActions() {
		Iterator e= fSelectionActions.iterator();
		while (e.hasNext())
			updateAction((String) e.next());
	}
}
