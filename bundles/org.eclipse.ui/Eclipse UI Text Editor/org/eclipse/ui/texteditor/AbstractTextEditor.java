package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.internal.plugins.ConfigurationElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginPrerequisite;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.VerticalRuler;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.EditorPluginAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;




/**
 * Abstract base implementation of a text editor.
 * <p>
 * Subclasses are responsible for configuring the editor appropriately.
 * The standard text editor, <code>TextEditor</code>, is one such example.
 * </p>
 * <p>
 * If a subclass calls <code>setEditorContextMenuId</code> the arguments is
 * used as the id under which the editor's context menu is registered for extensions.
 * If no id is set, the context menu is registered under <b>[editor_id].EditorContext</b>
 * whereby [editor_id] is replaced with the editor's part id.  If the editor is instructed to
 * run in version 1.0 context menu registration compatibility mode, the latter form of the
 * registration even happens if a context menu id has been set via <code>setEditorContextMenuId</code>.
 * If no id is set while in compatibility mode, the menu is registered under 
 * <code>DEFAULT_EDITOR_CONTEXT_MENU_ID</code>.
 * </p>
 * <p>
 * If a subclass calls <code>setRulerContextMenuId</code> the arguments is
 * used as the id under which the ruler's context menu is registered for extensions.
 * If no id is set, the context menu is registered under <b>[editor_id].RulerContext</b>
 * whereby [editor_id] is replaced with the editor's part id.  If the editor is instructed to
 * run in version 1.0 context menu registration compatibility mode, the latter form of the
 * registration even happens if a context menu id has been set via <code>setRulerContextMenuId</code>.
 * If no id is set while in compatibility mode, the menu is registered under
 * <code>DEFAULT_RULER_CONTEXT_MENU_ID</code>.
 * </p>
 *
 * @see org.eclipse.ui.editors.text.TextEditor
 */
public abstract class AbstractTextEditor extends EditorPart implements ITextEditor, IReusableEditor, ITextEditorExtension {

	private static final String TAG_CONTRIBUTION_TYPE= "editorContribution"; //$NON-NLS-1$

	/**
	 * Internal element state listener.
	 */
	class ElementStateListener implements IElementStateListener, IElementStateListenerExtension {
		
			class Validator implements VerifyListener {
				
				private boolean fInputChanged;
				private ITextInputListener fInputListener= new ITextInputListener() {
					public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {}
					public void inputDocumentChanged(IDocument oldInput, IDocument newInput) { fInputChanged= true; }
				};
				
				public void verifyText(VerifyEvent e) {
					
					ISourceViewer viewer= getSourceViewer();
					fInputChanged= false;
					viewer.addTextInputListener(fInputListener);
					try {
						validateState(getEditorInput());
						sanityCheckState(getEditorInput());
						if (isEditorInputReadOnly() || fInputChanged)
							e.doit= false;
					} finally {
						viewer.removeTextInputListener(fInputListener);
					}
				}
			};
		
		private Validator fValidator;
		
		/*
		 * @see IElementStateListenerExtension#elementStateValidationChanged(Object, boolean)
		 */
		public void elementStateValidationChanged(Object element, boolean isStateValidated) {
			if (isStateValidated && fValidator != null) {
				ISourceViewer viewer= getSourceViewer();
				if (viewer != null) {
					StyledText textWidget= viewer.getTextWidget();
					if (textWidget != null && !textWidget.isDisposed())
						textWidget.removeVerifyListener(fValidator);
					fValidator= null;
				}
			} else if (!isStateValidated && fValidator == null) {
				ISourceViewer viewer= getSourceViewer();
				if (viewer != null) {
					StyledText textWidget= viewer.getTextWidget();
					if (textWidget != null && !textWidget.isDisposed()) {
						fValidator= new Validator();
						textWidget.addVerifyListener(fValidator);
					}
				}
			}
		}
		
		/*
		 * @see IElementStateListener#elementDirtyStateChanged
		 */
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			if (element != null && element.equals(getEditorInput()))
				firePropertyChange(PROP_DIRTY);
		}
		
		/*
		 * @see IElementStateListener#elementContentAboutToBeReplaced
		 */
		public void elementContentAboutToBeReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				rememberSelection();
				resetHighlightRange();
			}
		}
		
		/*
		 * @see IElementStateListener#elementContentReplaced
		 */
		public void elementContentReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				firePropertyChange(PROP_DIRTY);
				restoreSelection();
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
						
				rememberSelection();
									
				IDocumentProvider d= getDocumentProvider();
				IDocument changed= null;
				if (isDirty())
					changed= d.getDocument(getEditorInput());
					
				setInput((IEditorInput) movedElement);
				
				if (changed != null) {
					d.getDocument(getEditorInput()).set(changed.get());
					validateState(getEditorInput());
					updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);
				}					
					
				restoreSelection();
			}
		}
	};
	
	/**
	 * Internal text listener.
	 */
	class TextListener implements ITextListener {
		
		private Runnable fRunnable= new Runnable() {
			public void run() {
				if (fSourceViewer != null) {
					// check whether editor has not been disposed yet
					updateContentDependentActions();
				}
			}
		};
		
		private Display fDisplay;
		
		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			
			if (fDisplay == null)
				fDisplay= getSite().getShell().getDisplay();
				
			fDisplay.asyncExec(fRunnable);
		}
	};

	static class ConfigurationElementComparator implements Comparator {
		
		/*
		 * @see Comparator#compare(Object, Object)
		 */
		public int compare(Object object0, Object object1) {

			IConfigurationElement element0= (IConfigurationElement)object0;
			IConfigurationElement element1= (IConfigurationElement)object1;	
			
			if (dependsOn(element0, element1))
				return -1;
				
			if (dependsOn(element1, element0))
				return +1;
			
			return 0;
		}

		private static boolean dependsOn(IConfigurationElement element0, IConfigurationElement element1) {
			IPluginDescriptor descriptor0= element0.getDeclaringExtension().getDeclaringPluginDescriptor();
			IPluginDescriptor descriptor1= element1.getDeclaringExtension().getDeclaringPluginDescriptor();
			
			return dependsOn(descriptor0, descriptor1);
		}
		
		private static boolean dependsOn(IPluginDescriptor descriptor0, IPluginDescriptor descriptor1) {

			IPluginRegistry registry= Platform.getPluginRegistry();
			IPluginPrerequisite[] prerequisites= descriptor0.getPluginPrerequisites();

			for (int i= 0; i < prerequisites.length; i++) {
				IPluginPrerequisite prerequisite= prerequisites[i];
				String id= prerequisite.getUniqueIdentifier();			
				IPluginDescriptor descriptor= registry.getPluginDescriptor(id);
				
				if (descriptor != null && (descriptor.equals(descriptor1) || dependsOn(descriptor, descriptor1)))
					return true;
			}
			
			return false;
		}
	}
	
	/**
	 * Internal property change listener.
	 */
	class PropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			handlePreferenceStoreChanged(event);
		}
	};
	
	/**
	 * Internal key verify listener for triggering action activation codes.
	 */
	class ActivationCodeTrigger implements VerifyKeyListener {
		
		private boolean fIsInstalled= false;
		private IKeyBindingService fKeyBindingService;
		
		/*
		 * @see VerifyKeyListener#verifyKey(VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {
							
			ActionActivationCode code= null;
			int size= fActivationCodes.size();
			for (int i= 0; i < size; i++) {
				code= (ActionActivationCode) fActivationCodes.get(i);
				if (code.matches(event)) {
					IAction action= getAction(code.fActionId);
					if (action != null) {
						
						if (action instanceof IUpdate)
							((IUpdate) action).update();
						
						if (action.isEnabled()) {
							event.doit= false;
							action.run();
							return;
						}
					}
				}
			}
			if (fKeyBindingService.processKey(event))
				event.doit= false;
		}
		
		/**
		 * Installs this trigger on the editor's text widget.
		 */
		public void install() {
			if (!fIsInstalled) {
				
				if (fSourceViewer instanceof ITextViewerExtension) {
					ITextViewerExtension e= (ITextViewerExtension) fSourceViewer;
					e.prependVerifyKeyListener(this);
				} else {
					StyledText text= fSourceViewer.getTextWidget();
					text.addVerifyKeyListener(this);
				}
				
				fKeyBindingService= getEditorSite().getKeyBindingService();
				fKeyBindingService.enable(true);
				fIsInstalled= true;
			}
		}
		
		/**
		 * Uninstalls this trigger from the editor's text widget.
		 */
		public void uninstall() {
			if (fIsInstalled) {
				
				if (fSourceViewer instanceof ITextViewerExtension) {
					ITextViewerExtension e= (ITextViewerExtension) fSourceViewer;
					e.removeVerifyKeyListener(this);
				} else {
					StyledText text= fSourceViewer.getTextWidget();
					text.removeVerifyKeyListener(fActivationCodeTrigger);
				}
				
				fIsInstalled= false;
				fKeyBindingService= null;
			}
		}
		
		/**
		 * Registers the given action for key activation.
		 */
		public void registerActionForKeyActivation(IAction action) {
			if (action.getActionDefinitionId() != null)
				fKeyBindingService.registerAction(action);
		}
		
		/**
		 * The given action is no longer available for key activation
		 */
		public void unregisterActionFromKeyActivation(IAction action) {
			// No such action available on the service
		}
	};
	
	/**
	 * Representation of action activation codes.
	 */
	class ActionActivationCode {
		
		public String fActionId;
		public char fCharacter;
		public int fKeyCode;
		public int fStateMask;
		
		public ActionActivationCode(String actionId) {
			fActionId= actionId;
		}
		
		public boolean matches(VerifyEvent event) {
			return (event.character == fCharacter &&
						event.keyCode == fKeyCode &&
						event.stateMask == fStateMask);
		}		
	};
	
	/**
	 * Internal part and shell activation listener
	 */
	class ActivationListener extends ShellAdapter implements IPartListener {
		
		private IWorkbenchPart fActivePart;
		private boolean fIsHandlingActivation= false;
		
		/*
		 * @see IPartListener#partActivated(IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
			fActivePart= part;
			handleActivation();
		}
	
		/*
		 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {
		}
	
		/*
		 * @see IPartListener#partClosed(IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
		}
	
		/*
		 * @see IPartListener#partDeactivated(IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {
			fActivePart= null;
		}
	
		/*
		 * @see IPartListener#partOpened(IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {
		}
	
		/*
		 * @see ShellListener#shellActivated(ShellEvent)
		 */
		public void shellActivated(ShellEvent e) {
			handleActivation();
		}
		
		private void handleActivation() {
			if (fIsHandlingActivation)
				return;
				
			if (fActivePart == AbstractTextEditor.this) {
				fIsHandlingActivation= true;
				try {
					sanityCheckState(getEditorInput());
				} finally {
					fIsHandlingActivation= false;
				}
			}
		}
	};
	
	/**
	 * Internal interface for a cursor listener. I.e. aggregation 
	 * of mouse and key listener.
	 */
	interface ICursorListener extends MouseListener, KeyListener {
	};
	
	/**
	 * Maps an action definition id to an StyledText action.
	 */
	static class IdMapEntry {
		
		private String fActionId;
		private int fAction;
		
		public IdMapEntry(String actionId, int action) {
			fActionId= actionId;
			fAction= action;
		}
		
		public String getActionId() {
			return fActionId;
		}
		
		public int getAction() {
			return fAction;
		}
	};
	
	/**
	 * Internal action for scroll the editor's viewer by a specified number of lines.
	 */
	class ScrollLinesAction extends Action {
		
		private int fScrollIncrement;
		
		public ScrollLinesAction(int scrollIncrement) {
			fScrollIncrement= scrollIncrement;
		}
		
		/*
		 * @see IAction#run()
		 */
		public void run() {
			ISourceViewer viewer= getSourceViewer();
			int topIndex= viewer.getTopIndex();
			int newTopIndex= Math.max(0, topIndex + fScrollIncrement);
			viewer.setTopIndex(newTopIndex);
		}
	};
	
	
	
	/** Key used to look up font preference */
	public final static String PREFERENCE_FONT= JFaceResources.TEXT_FONT;
	/** Key used to look up foreground color preference */
	public final static String PREFERENCE_COLOR_FOREGROUND= "AbstractTextEditor.Color.Foreground"; //$NON-NLS-1$
	/** Key used to look up background color preference */
	public final static String PREFERENCE_COLOR_BACKGROUND= "AbstractTextEditor.Color.Background"; //$NON-NLS-1$	
	/** Key used to look up foreground color system default preference */
	public final static String PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT= "AbstractTextEditor.Color.Foreground.SystemDefault"; //$NON-NLS-1$
	/** Key used to look up background color system default preference */
	public final static String PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT= "AbstractTextEditor.Color.Background.SystemDefault"; //$NON-NLS-1$	
	/** Key used to look up find scope background color preference */
	public final static String PREFERENCE_COLOR_FIND_SCOPE= "AbstractTextEditor.Color.FindScope"; //$NON-NLS-1$	
	
	/** Menu id for the editor context menu. */
	public final static String DEFAULT_EDITOR_CONTEXT_MENU_ID= "#EditorContext"; //$NON-NLS-1$
	/** Menu id for the ruler context menu. */
	public final static String DEFAULT_RULER_CONTEXT_MENU_ID= "#RulerContext"; //$NON-NLS-1$
	
	/** The width of the vertical ruler */
	protected final static int VERTICAL_RULER_WIDTH= 12;
	
	/** The complete mapping between action definition ids used by eclipse and StyledText actions. */
	protected final static IdMapEntry[] ACTION_MAP= new IdMapEntry[] {
		// navigation
		new IdMapEntry(ITextEditorActionDefinitionIds.LINE_UP, ST.LINE_UP),
		new IdMapEntry(ITextEditorActionDefinitionIds.LINE_DOWN, ST.LINE_DOWN),
		new IdMapEntry(ITextEditorActionDefinitionIds.LINE_START, ST.LINE_START),
		new IdMapEntry(ITextEditorActionDefinitionIds.LINE_END, ST.LINE_END),
		new IdMapEntry(ITextEditorActionDefinitionIds.COLUMN_PREVIOUS, ST.COLUMN_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.COLUMN_NEXT, ST.COLUMN_NEXT),
		new IdMapEntry(ITextEditorActionDefinitionIds.PAGE_UP, ST.PAGE_UP),
		new IdMapEntry(ITextEditorActionDefinitionIds.PAGE_DOWN, ST.PAGE_DOWN),
		new IdMapEntry(ITextEditorActionDefinitionIds.WORD_PREVIOUS, ST.WORD_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.WORD_NEXT, ST.WORD_NEXT),
		new IdMapEntry(ITextEditorActionDefinitionIds.TEXT_START, ST.TEXT_START),
		new IdMapEntry(ITextEditorActionDefinitionIds.TEXT_END, ST.TEXT_END),
		new IdMapEntry(ITextEditorActionDefinitionIds.WINDOW_START, ST.WINDOW_START),
		new IdMapEntry(ITextEditorActionDefinitionIds.WINDOW_END, ST.WINDOW_END),
		// selection
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_LINE_UP, ST.SELECT_LINE_UP),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_LINE_DOWN, ST.SELECT_LINE_DOWN),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_LINE_START, ST.SELECT_LINE_START),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_LINE_END, ST.SELECT_LINE_END),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_COLUMN_PREVIOUS, ST.SELECT_COLUMN_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_COLUMN_NEXT, ST.SELECT_COLUMN_NEXT),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_PAGE_UP, ST.SELECT_PAGE_UP),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_PAGE_DOWN, ST.SELECT_PAGE_DOWN),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, ST.SELECT_WORD_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT,  ST.SELECT_WORD_NEXT),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_TEXT_START, ST.SELECT_TEXT_START),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_TEXT_END, ST.SELECT_TEXT_END),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_WINDOW_START, ST.SELECT_WINDOW_START),
		new IdMapEntry(ITextEditorActionDefinitionIds.SELECT_WINDOW_END, ST.SELECT_WINDOW_END),
		// modification
		new IdMapEntry(ITextEditorActionDefinitionIds.CUT, ST.CUT),
		new IdMapEntry(ITextEditorActionDefinitionIds.COPY, ST.COPY),
		new IdMapEntry(ITextEditorActionDefinitionIds.PASTE, ST.PASTE),
		new IdMapEntry(ITextEditorActionDefinitionIds.DELETE_PREVIOUS, ST.DELETE_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.DELETE_NEXT, ST.DELETE_NEXT),
		// miscellaneous
		new IdMapEntry(ITextEditorActionDefinitionIds.TOGGLE_OVERWRITE, ST.TOGGLE_OVERWRITE)
	};
	
	
	/* Status line labels */
	private final String fReadOnlyLabel = EditorMessages.getString("Editor.statusline.state.readonly.label"); //$NON-NLS-1$
	private final String fWritableLabel = EditorMessages.getString("Editor.statusline.state.writable.label"); //$NON-NLS-1$
	private final String fInsertModeLabel = EditorMessages.getString("Editor.statusline.mode.insert.label"); //$NON-NLS-1$
	private final String fOverwriteModeLabel = EditorMessages.getString("Editor.statusline.mode.overwrite.label"); //$NON-NLS-1$
	
	private static class PositionLabelValue {
		
		public int fValue;
		
		public String toString() {
			return String.valueOf(fValue);
		}
	};
	
	private final String fPositionLabelError= EditorMessages.getString("Editor.statusline.position.error.label"); //$NON-NLS-1$
	private final String fPositionLabelPattern= EditorMessages.getString("Editor.statusline.position.pattern"); //$NON-NLS-1$
	private final PositionLabelValue fLineLabel= new PositionLabelValue();
	private final PositionLabelValue fColumnLabel= new PositionLabelValue();
	private final Object[] fPositionLabelPatternArguments= new Object[] { fLineLabel, fColumnLabel };

	
	
	
	
	/** The editor's internal document provider */
	private IDocumentProvider fInternalDocumentProvider;
	/** The editor's external document provider */
	private IDocumentProvider fExternalDocumentProvider;
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
	/** The editor's foreground color */
	private Color fForegroundColor;
	/** The editor's background color */
	private Color fBackgroundColor;
	/** The find scope's highlight color */
	private Color fFindScopeHighlightColor;
	/** The editor's vertical ruler */
	private IVerticalRuler fVerticalRuler;
	/** The editor's context menu id */
	private String fEditorContextMenuId;
	/** The ruler's context menu id */
	private String fRulerContextMenuId;
	/** The editor's help context id */
	private String fHelpContextId;
	/** The editor's presentation mode */
	private boolean fShowHighlightRangeOnly;
	/** The actions registered with the editor */	
	private Map fActions= new HashMap(10);
	/** The actions marked as selection dependent */
	private List fSelectionActions= new ArrayList(5);
	/** The actions marked as content dependent */
	private List fContentActions= new ArrayList(5);
	/** The actions marked as property dependent */
	private List fPropertyActions= new ArrayList(5);
	/** The editor's action activation codes */
	private List fActivationCodes= new ArrayList(2);
	/** The verify key listener for activation code triggering */
	private ActivationCodeTrigger fActivationCodeTrigger= new ActivationCodeTrigger();
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
	/** The editor's activation listener */
	private ActivationListener fActivationListener= new ActivationListener();
	/** The map of the editor's status fields */
	private Map fStatusFields;
	/** The editor's cursor listener */
	private ICursorListener fCursorListener;
	/** The editor's insert mode */
	private boolean fOverwriting= false;
	/** The editor's remembered text selection */
	private ITextSelection fRememberedSelection;
	/** Indicates whether the editor runs in 1.0 context menu registration compatibility mode */
	private boolean fCompatibilityMode= true;
	/** The number of reentrances into error correction code while saving */
	private int fErrorCorrectionOnSave;
	/** The incremental find target */
	private IncrementalFindTarget fIncrementalFindTarget;
	/** Cached modification stamp of the editor's input */
	private long fModificationStamp= -1;
	

	
	
	
	/**
	 * Creates a new text editor. If not explicitly set, this editor uses
	 * a <code>SourceViewerConfiguration</code> to configure its
	 * source viewer. This viewer does not have a range indicator installed,
	 * nor any menu id set. By default, the created editor runs in 1.0 context
	 * menu registration compatibility mode.
	 */
	protected AbstractTextEditor() {
		super();
		fEditorContextMenuId= null;
		fRulerContextMenuId= null;
		fHelpContextId= null;
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
	 * Returns the editor's range indicator. 
	 *
	 * @return the editor's range indicator
	 */
	protected final Annotation getRangeIndicator() {
		return fRangeIndicator;
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
	 * Returns the editor's source viewer.
	 *
	 * @return the editor's source viewer
	 */
	protected final ISourceViewer getSourceViewer() {
		return fSourceViewer;
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
	 * Returns the editor's context menu id.
	 *
	 * @return the editor's context menu id
	 */
	protected final String getEditorContextMenuId() {
		return fEditorContextMenuId;
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
	 * Returns the editor's help context id.
	 *
	 * @return the editor's help context id
	 */
	protected final String getHelpContextId() {
		return fHelpContextId;
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
	 * Sets this editor's context menu id.
	 *
	 * @param contextMenuId the context menu id
	 */
	protected void setEditorContextMenuId(String contextMenuId) {
		Assert.isNotNull(contextMenuId);
		fEditorContextMenuId= contextMenuId;
	}
	
	/**
	 * Sets the ruler's context menu id.
	 *
	 * @param contextMenuId the context menu id
	 */
	protected void setRulerContextMenuId(String contextMenuId) {
		Assert.isNotNull(contextMenuId);
		fRulerContextMenuId= contextMenuId;
	}
	
	/**
	 * Sets the context menu registration 1.0 compatibility mode. (See class
	 * description for more details.)
	 * 
	 * @param compatible <code>true</code> if compatibility mode is enabled
	 */
	protected final void setCompatibilityMode(boolean compatible) {
		fCompatibilityMode= compatible;
	}
	
	/**
	 * Sets the editor's help context id.
	 *
	 * @param helpContextId the help context id
	 */
	protected void setHelpContextId(String helpContextId) {
		Assert.isNotNull(helpContextId);
		fHelpContextId= helpContextId;
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
		
	/*
	 * @see ITextEditor#isEditable
	 */
	public boolean isEditable() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			return extension.isModifiable(getEditorInput());
		}
		return false;
	}
	
	/*
	 * @see ITextEditor#getSelectionProvider
	 */
	public ISelectionProvider getSelectionProvider() {
		return (fSourceViewer != null ? fSourceViewer.getSelectionProvider() : null);
	}
	
	/**
	 * Remembers the current selection of this editor. This method is called when, e.g., 
	 * the content of the editor is about to be reverted to the saved state. This method
	 * remembers the selection in a semantic format, i.e., in a format which allows to
	 * restore the selection even if the originally selected text is no longer part of the
	 * editor's content.<p>
	 * Subclasses should implement this method including all necessary state. This
	 * default implementation remembers the textual range only and is thus purely
	 * syntactic.
	 * 
	 * @see #restoreSelection
	 */
	protected void rememberSelection() {
		ISelectionProvider sp= getSelectionProvider();
		fRememberedSelection= (sp == null ? null : (ITextSelection) sp.getSelection());
	}
	
	/**
	 * Restores a selection previously remembered by <code>rememberSelection</code>.
	 * Subclasses may reimplement this method and thereby semantically adapt the
	 * remembered selection. This default implementation just selects the
	 * remembered textual range. 
	 * 
	 * @see #rememberSelection
	 */
	protected void restoreSelection() {
		if (getSourceViewer() != null && fRememberedSelection != null)
			selectAndReveal(fRememberedSelection.getOffset(), fRememberedSelection.getLength());
		fRememberedSelection= null;
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
	
	/**
	 * Creates and returns the listener on this editor's vertical ruler.
	 *
	 * @return the mouse listener
	 */
	protected final MouseListener getRulerMouseListener() {
		if (fMouseListener == null) {
			fMouseListener= new MouseListener() {
				
				private boolean fDoubleClicked= false;
				
				private void triggerAction(String actionID) {
					IAction action= getAction(actionID);
					if (action != null) {
						if (action instanceof IUpdate)
							((IUpdate) action).update();
						if (action.isEnabled())
							action.run();
					}
				}
				
				public void mouseUp(MouseEvent e) {
					setFocus();
					if (1 == e.button && !fDoubleClicked)
						triggerAction(ITextEditorActionConstants.RULER_CLICK);
					fDoubleClicked= false;
				}
				
				public void mouseDoubleClick(MouseEvent e) {
					if (1 == e.button) {
						fDoubleClicked= true;
						triggerAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK);
					}
				}
				
				public void mouseDown(MouseEvent e) {
				}
			};
		}
		return fMouseListener;
	}

	/**
	 * Returns this editor's selection changed listener to be installed
	 * on the editor's source viewer.
	 *
	 * @return the listener
	 */
	protected final ISelectionChangedListener getSelectionChangedListener() {
		if (fSelectionChangedListener == null) {
			fSelectionChangedListener= new ISelectionChangedListener() {
				
				private Runnable fRunnable= new Runnable() {
					public void run() {
						// check whether editor has not been disposed yet
						if (fSourceViewer != null) {
							updateSelectionDependentActions();
							handleCursorPositionChanged();
						}
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
	
	/**
	 * Returns this editor's "cursor" listener to be installed on the editor's
	 * source viewer. This listener is listening to key and mouse button events.
	 * 
	 * @return the listener
	 */
	protected final ICursorListener getCursorListener() {
		if (fCursorListener == null) {
			fCursorListener= new ICursorListener() {
				
				public void keyPressed(KeyEvent e) {
					if (e.keyCode != 0) {
						StyledText styledText= (StyledText) e.widget;
						int action = styledText.getKeyBinding(e.keyCode | e.stateMask);
						if (ST.TOGGLE_OVERWRITE == action) {
							fOverwriting= !fOverwriting;
							handleInsertModeChanged();
						}
					}
				}
				
				public void keyReleased(KeyEvent e) {
					handleCursorPositionChanged();
				}
				
				public void mouseDoubleClick(MouseEvent e) {
				}
				
				public void mouseDown(MouseEvent e) {
				}
				
				public void mouseUp(MouseEvent e) {
					handleCursorPositionChanged();
				}
			};
		}
		return fCursorListener;
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
		
		IWorkbenchWindow window= getSite().getWorkbenchWindow();
		window.getPartService().addPartListener(fActivationListener);
		window.getShell().addShellListener(fActivationListener);
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
				
		initializeViewerFont(fSourceViewer);
		initializeViewerColors(fSourceViewer);
		initializeFindScopeColor(fSourceViewer);
		
		StyledText styledText= fSourceViewer.getTextWidget();
		styledText.addMouseListener(getCursorListener());
		styledText.addKeyListener(getCursorListener());
		
		if (getHelpContextId() != null)
			WorkbenchHelp.setHelp(styledText, getHelpContextId());
			
		
		String id= fEditorContextMenuId != null ?  fEditorContextMenuId : DEFAULT_EDITOR_CONTEXT_MENU_ID;
		
		MenuManager manager= new MenuManager(id, id);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(getContextMenuListener());
		fTextContextMenu= manager.createContextMenu(styledText);
		styledText.setMenu(fTextContextMenu);
		
		if (fEditorContextMenuId != null)
			getSite().registerContextMenu(fEditorContextMenuId, manager, getSelectionProvider());
		else if (fCompatibilityMode)
			getSite().registerContextMenu(DEFAULT_EDITOR_CONTEXT_MENU_ID, manager, getSelectionProvider());
			
		if ((fEditorContextMenuId != null && fCompatibilityMode) || fEditorContextMenuId  == null) {
			String partId= getSite().getId();
			if (partId != null)
				getSite().registerContextMenu(partId + ".EditorContext", manager, getSelectionProvider()); //$NON-NLS-1$
		}
		
		if (fEditorContextMenuId == null)
			fEditorContextMenuId= DEFAULT_EDITOR_CONTEXT_MENU_ID;
		
		
		Control ruler= fVerticalRuler.getControl();
		id= fRulerContextMenuId != null ? fRulerContextMenuId : DEFAULT_RULER_CONTEXT_MENU_ID;
		manager= new MenuManager(id, id);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(getContextMenuListener());		
		fRulerContextMenu= manager.createContextMenu(ruler);
		ruler.setMenu(fRulerContextMenu);
		ruler.addMouseListener(getRulerMouseListener());
		
		if (fRulerContextMenuId != null)
			getSite().registerContextMenu(fRulerContextMenuId, manager, getSelectionProvider());
		else if (fCompatibilityMode)
			getSite().registerContextMenu(DEFAULT_RULER_CONTEXT_MENU_ID, manager, getSelectionProvider());
			
		if ((fRulerContextMenuId != null && fCompatibilityMode) || fRulerContextMenuId  == null) {
			String partId= getSite().getId();
			if (partId != null)
				getSite().registerContextMenu(partId + ".RulerContext", manager, getSelectionProvider()); //$NON-NLS-1$
		}
		
		if (fRulerContextMenuId == null)
			fRulerContextMenuId= DEFAULT_RULER_CONTEXT_MENU_ID;
			
		getSite().setSelectionProvider(getSelectionProvider());
		
		fActivationCodeTrigger.install();
		createNavigationActions();
		createActions();
		
		initializeSourceViewer(getEditorInput());
	}
	
	/**
	 * Initializes the given widget's font.
	 * 
	 * @param styledText the widget to be initialized
	 */
	private void initializeViewerFont(ISourceViewer viewer) {
		
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			
			FontData data= null;
			
			if (store.contains(PREFERENCE_FONT) && !store.isDefault(PREFERENCE_FONT))
				data= PreferenceConverter.getFontData(store, PREFERENCE_FONT);
			else
				data= PreferenceConverter.getDefaultFontData(store, PREFERENCE_FONT);
			
			if (data != null) {
				
				Font font= new Font(viewer.getTextWidget().getDisplay(), data);
				setFont(viewer, font);
				
				if (fFont != null)
					fFont.dispose();
					
				fFont= font;
				return;
			}
		}
		
		// if all the preferences failed
		setFont(viewer, JFaceResources.getTextFont());
	}
	
	/**
	 * Sets the font for the given viewer sustaining selection and scroll position.
	 * 
	 * @param sourceViewer the source viewer
	 * @param font the font
	 */
	private void setFont(ISourceViewer sourceViewer, Font font) {
		if (sourceViewer.getDocument() != null) {
		
			Point selection= sourceViewer.getSelectedRange();
			int topIndex= sourceViewer.getTopIndex();
			
			StyledText styledText= sourceViewer.getTextWidget();
			styledText.setRedraw(false);
			
			styledText.setFont(font);
			sourceViewer.setSelectedRange(selection.x , selection.y);
			sourceViewer.setTopIndex(topIndex);
			
			styledText.setRedraw(true);
		} else {
			StyledText styledText= sourceViewer.getTextWidget();
			styledText.setFont(font);
		}	
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
	
		RGB rgb= null;		
		
		if (store.contains(key)) {
			
			if (store.isDefault(key))
				rgb= PreferenceConverter.getDefaultColor(store, key);
			else
				rgb= PreferenceConverter.getColor(store, key);
		
			if (rgb != null)
				return new Color(display, rgb);
		}
		
		return null;
	}
	
	/**
	 * Initializes the given viewer's colors.
	 * 
	 * @param viewer the viewer to be initialized
	 */
	private void initializeViewerColors(ISourceViewer viewer) {
		
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			
			StyledText styledText= viewer.getTextWidget();
			
			// ----------- foreground color --------------------
			Color color= store.getBoolean(PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);
				
			if (fForegroundColor != null)
				fForegroundColor.dispose();
			
			fForegroundColor= color;
			
			// ---------- background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);
				
			if (fBackgroundColor != null)
				fBackgroundColor.dispose();
				
			fBackgroundColor= color;
		}
	}

	private void initializeFindScopeColor(ISourceViewer viewer) {

		IPreferenceStore store= getPreferenceStore();
		if (store != null) {

			StyledText styledText= viewer.getTextWidget();
			
			Color color= createColor(store, PREFERENCE_COLOR_FIND_SCOPE, styledText.getDisplay());	

			IFindReplaceTarget target= viewer.getFindReplaceTarget();
			if (target != null && target instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) target).setScopeHighlightColor(color);
			
			if (fFindScopeHighlightColor != null)
				fFindScopeHighlightColor.dispose();

			fFindScopeHighlightColor= color;				
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
		
		if (fElementStateListener instanceof IElementStateListenerExtension) {
			IElementStateListenerExtension extension= (IElementStateListenerExtension) fElementStateListener;
			extension.elementStateValidationChanged(input, false);
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
		String title= ""; //$NON-NLS-1$
		
		if (input != null) {
			IEditorRegistry editorRegistry = getEditorSite().getPage().getWorkbenchWindow().getWorkbench().getEditorRegistry();
			IEditorDescriptor editorDesc= editorRegistry.findEditor(getSite().getId());			
			ImageDescriptor imageDesc= editorDesc != null ? editorDesc.getImageDescriptor() : null;

			fTitleImage= imageDesc != null ? imageDesc.createImage() : null;
			title= input.getName();
		}
		
		setTitleImage(fTitleImage);
		setTitle(title);
		
		firePropertyChange(PROP_DIRTY);
		
		if (oldImage != null && !oldImage.isDisposed())
			oldImage.dispose();
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
				IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, EditorMessages.getString("Editor.error.no_provider"), null); //$NON-NLS-1$
				throw new CoreException(s);
			}
			
			provider.connect(input);
			
			initializeTitle(input);
			if (fSourceViewer != null)
				initializeSourceViewer(input);
				
			updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);
		}
	}
	
	/*
	 * @see EditorPart#setInput
	 */
	public final void setInput(IEditorInput input) {
		
		try {
			
			doSetInput(input);
				
		} catch (CoreException x) {
			String title= EditorMessages.getString("Editor.error.setinput.title"); //$NON-NLS-1$
			String msg= EditorMessages.getString("Editor.error.setinput.message"); //$NON-NLS-1$
			Shell shell= getSite().getShell();
			ErrorDialog.openError(shell, title, msg, x.getStatus());
		}				
	}
	
	/*
	 * @see ITextEditor#close
	 */
	public void close(final boolean save) {
		
		Display display= getSite().getShell().getDisplay();
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (fSourceViewer != null) {
					// check whether editor has not been disposed yet
					getSite().getPage().closeEditor(AbstractTextEditor.this, save);
				}
			}
		});
	}
	
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IWorkbenchPart</code> method may be extended by subclasses.
	 * Subclasses must call <code>super.dispose()</code>.
	 */
	public void dispose() {
		
		if (fActivationListener != null) {
			IWorkbenchWindow window= getSite().getWorkbenchWindow();
			window.getPartService().removePartListener(fActivationListener);
			Shell shell= window.getShell();
			if (shell != null && !shell.isDisposed())
				shell.removeShellListener(fActivationListener);
			fActivationListener= null;
		}
		
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
		
		if (fActions != null) {
			fActions.clear();
			fActions= null;
		}
		
		if (fSelectionActions != null) {
			fSelectionActions.clear();
			fSelectionActions= null;
		}
		
		if (fContentActions != null) {
			fContentActions.clear();
			fContentActions= null;
		}
		
		if (fPropertyActions != null) {
			fPropertyActions.clear();
			fPropertyActions= null;
		}
		
		if (fActivationCodes != null) {
			fActivationCodeTrigger= null;
			fActivationCodes.clear();
			fActivationCodes= null;
		}
		
		super.setInput(null);		
		
		super.dispose();
	}
	
	/**
	 * Determines whether the given preference change affects the editor's
	 * presentation. This implementation always returns <code>false</code>.
	 * May be reimplemented by subclasses.
	 * 
	 * @param event the event which should be investigated
	 * @return whether the event describes a preference change affecting the editor's
	 * 			presentation
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return false;
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
			
		String property= event.getProperty();
		
		if (PREFERENCE_FONT.equals(property)) {
			initializeViewerFont(fSourceViewer);

		} else if (PREFERENCE_COLOR_FOREGROUND.equals(property) || PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
			PREFERENCE_COLOR_BACKGROUND.equals(property) ||	PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property))
		{
			initializeViewerColors(fSourceViewer);
		} else if (PREFERENCE_COLOR_FIND_SCOPE.equals(property)) {
			initializeFindScopeColor(fSourceViewer);
		}
			
		if (affectsTextPresentation(event))
			fSourceViewer.invalidateTextPresentation();
	}
	
	/**
	 * Handles an external change of the editor's input element.
	 */
	protected void handleEditorInputChanged() {
		
		String title;
		String msg;
		Shell shell= getSite().getShell();
		
		if (getDocumentProvider().isDeleted(getEditorInput())) {
			
			if (isSaveAsAllowed()) {
			
				title= EditorMessages.getString("Editor.error.activated.deleted.save.title"); //$NON-NLS-1$
				msg= EditorMessages.getString("Editor.error.activated.deleted.save.message"); //$NON-NLS-1$
				
				String[] buttons= {
					EditorMessages.getString("Editor.error.activated.deleted.save.button.save"), //$NON-NLS-1$
					EditorMessages.getString("Editor.error.activated.deleted.save.button.close"), //$NON-NLS-1$
				};
					
				MessageDialog dialog= new MessageDialog(shell, title, null, msg, MessageDialog.QUESTION, buttons, 0);
				
				if (dialog.open() == 0) {
					NullProgressMonitor pm= new NullProgressMonitor();
					performSaveAs(pm);
					if (pm.isCanceled())
						handleEditorInputChanged();
				} else {
					close(false);
				}
				
			} else {
				
				title= EditorMessages.getString("Editor.error.activated.deleted.close.title"); //$NON-NLS-1$
				msg= EditorMessages.getString("Editor.error.activated.deleted.close.message"); //$NON-NLS-1$
				MessageDialog.openConfirm(shell, title, msg);
			}
			
		} else {
			
			title= EditorMessages.getString("Editor.error.activated.outofsync.title"); //$NON-NLS-1$
			msg= EditorMessages.getString("Editor.error.activated.outofsync.message"); //$NON-NLS-1$
				
			if (MessageDialog.openQuestion(shell, title, msg)) {
				try {
					doSetInput(getEditorInput());
				} catch (CoreException x) {
					title= EditorMessages.getString("Editor.error.refresh.outofsync.title"); //$NON-NLS-1$
					msg= EditorMessages.getString("Editor.error.refresh.outofsync.message"); //$NON-NLS-1$
					ErrorDialog.openError(shell, title, msg, x.getStatus());
				}
			} else {
				markEditorAsDirty();
			}
		}
	}

	/**
	 * Marks this editor and its editor input as dirty.
	 */
	private void markEditorAsDirty() {
		
		if (isDirty())
			return;
			
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			
			provider.removeElementStateListener(fElementStateListener);
			try {
				
				IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
				extension.setCanSaveDocument(getEditorInput());
				firePropertyChange(PROP_DIRTY);
				
			} finally {
				provider.addElementStateListener(fElementStateListener);
			}
			
		}
	}
			
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method does nothing. Subclasses may reimplement.
	 */
	public void doSaveAs() {
		/*
		 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in the editors.
		 * Changed Behavior to make sure that if called inside a regular save (because
		 * of deletion of input element) there is a way to report back to the caller.
		 */
		performSaveAs(new NullProgressMonitor());
	}
	
	/**
	 * Performs a save as and reports the result state back to the 
	 * given progress monitor. This default implementation does nothing.
	 * Subclasses may reimplement.
	 * 
	 * @param progressMonitor the progress monitor for communicating result state or <code>null</code>
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
	}
		
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method may be extended by subclasses.
	 */
	public void doSave(IProgressMonitor progressMonitor) {
		
		IDocumentProvider p= getDocumentProvider();
		if (p == null)
			return;
			
		if (p.isDeleted(getEditorInput())) {
			
			if (isSaveAsAllowed()) {
				
				/*
				 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in the editors.
				 * Changed Behavior to make sure that if called inside a regular save (because
				 * of deletion of input element) there is a way to report back to the caller.
				 */
				performSaveAs(progressMonitor);
			
			} else {
				
				Shell shell= getSite().getShell();
				String title= EditorMessages.getString("Editor.error.save.deleted.title"); //$NON-NLS-1$
				String msg= EditorMessages.getString("Editor.error.save.deleted.message"); //$NON-NLS-1$
				MessageDialog.openError(shell, title, msg);
			}
			
		} else {	
		
			performSaveOperation(createSaveOperation(false), progressMonitor);
		}
	}
	
	/**
	 * Checks the state of the editor input.
	 */
	protected void sanityCheckState(IEditorInput input) {
		
		IDocumentProvider p= getDocumentProvider();
		
		if (fModificationStamp == -1) 
			fModificationStamp= p.getSynchronizationStamp(input);
			
		long stamp= p.getModificationStamp(input);
		if (stamp != fModificationStamp) {
			fModificationStamp= stamp;
			if (stamp != p.getSynchronizationStamp(input))
				handleEditorInputChanged();
		} 
		
		updateState(getEditorInput());
		updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);
	}
	
	/**
	 * Validates the state of the given editor input. The predominate intent
	 * of this method is to take any action propably necessary to ensure that
	 * the input can persistently be changed.
	 * 
	 * @param input the input to be validated
	 */
	protected void validateState(IEditorInput input) {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			try {
				
				extension.validateState(input, getSite().getShell());
				if (fSourceViewer != null)
					fSourceViewer.setEditable(isEditable());
				
			} catch (CoreException x) {
				ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();		
				log.log(x.getStatus());
			}
		}
	}
	
	/**
	 * Updates the state of the given editor input such as Read-only flag etc.
	 * 
	 * @param input the input to be validated
	 */
	protected void updateState(IEditorInput input) {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			try {
				
				extension.updateStateCache(input);
				if (fSourceViewer != null)
					fSourceViewer.setEditable(isEditable());
				
			} catch (CoreException x) {
				ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();		
				log.log(x.getStatus());
			}
		}
	}
	
	/**
	 * Creates a workspace modify operation which saves the content of the editor
	 * to the editor's input element. <code>overwrite</code> indicates whether
	 * the editor input element may be overwritten if necessary. Clients may
	 * reimplement this method.
	 * 
	 * @param overwrite indicates whether or not overwrititng is allowed
	 * @return the save operation
	 */
	protected WorkspaceModifyOperation createSaveOperation(final boolean overwrite) {
		return new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				IEditorInput input= getEditorInput();
				getDocumentProvider().saveDocument(monitor, input, getDocumentProvider().getDocument(input), overwrite);
			}
		};
	}
	
	/**
	 * Performs the given save operation and handles errors appropriatly.
	 * 
	 * @param operation the operation to be performed
	 * @param progressMonitor the monitor in which to run the operation
	 */
	protected void performSaveOperation(WorkspaceModifyOperation operation, IProgressMonitor progressMonitor) {
		
		IDocumentProvider provider= getDocumentProvider();
		
		try {
		
			provider.aboutToChange(getEditorInput());
			operation.run(progressMonitor);
		
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			
			Throwable t= x.getTargetException();
			if (t instanceof CoreException)
				handleExceptionOnSave((CoreException) t, progressMonitor);
			else {
				Shell shell= getSite().getShell();
				String title= EditorMessages.getString("Editor.error.save.title"); //$NON-NLS-1$
				String msg= EditorMessages.getString("Editor.error.save.message"); //$NON-NLS-1$
				MessageDialog.openError(shell, title, msg + t.getMessage());
			}
		
		} finally {
			provider.changed(getEditorInput());
		}
	}
	
	/**
	 * Handles the given exception. If the exception reports a out-of-sync
	 * situation, this is reported to the user. Otherwise, the exception
	 * is generically reported.
	 * 
	 * @param exception the exception to handle
	 * @param progressMonitor the progress monitor
	 */
	protected void handleExceptionOnSave(CoreException exception, IProgressMonitor progressMonitor) {
		
		try {
			++ fErrorCorrectionOnSave;
			
			Shell shell= getSite().getShell();
			
			IDocumentProvider p= getDocumentProvider();
			long modifiedStamp= p.getModificationStamp(getEditorInput());
			long synchStamp= p.getSynchronizationStamp(getEditorInput());
			
			if (fErrorCorrectionOnSave == 1 && modifiedStamp != synchStamp) {
				
				String title= EditorMessages.getString("Editor.error.save.outofsync.title"); //$NON-NLS-1$
				String msg= EditorMessages.getString("Editor.error.save.outofsync.message"); //$NON-NLS-1$
				
				if (MessageDialog.openQuestion(shell, title, msg))
					performSaveOperation(createSaveOperation(true), progressMonitor);
				else {
					/*
					 * 1GEUPKR: ITPJUI:ALL - Loosing work with simultaneous edits
					 * Set progress monitor to canceled in order to report back 
					 * to enclosing operations. 
					 */
					if (progressMonitor != null)
						progressMonitor.setCanceled(true);
				}
			} else {
				
				String title= EditorMessages.getString("Editor.error.save.title"); //$NON-NLS-1$
				String msg= EditorMessages.getString("Editor.error.save.message"); //$NON-NLS-1$
				ErrorDialog.openError(shell, title, msg, exception.getStatus());
				
				/*
				 * 1GEUPKR: ITPJUI:ALL - Loosing work with simultaneous edits
				 * Set progress monitor to canceled in order to report back 
				 * to enclosing operations. 
				 */
				if (progressMonitor != null)
					progressMonitor.setCanceled(true);
			}
			
		} finally {
			-- fErrorCorrectionOnSave;
		}
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
	
	/*
	 * @see IEditorPart#isDirty
	 */
	public boolean isDirty() {
		IDocumentProvider p= getDocumentProvider();
		return p == null ? false : p.canSaveDocument(getEditorInput());
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
			String title= EditorMessages.getString("Editor.error.revert.title"); //$NON-NLS-1$
			String msg= EditorMessages.getString("Editor.error.revert.message"); //$NON-NLS-1$
			Shell shell= getSite().getShell();
			ErrorDialog.openError(shell, title, msg, x.getStatus());
		}
	}
	
	/*
	 * @see ITextEditor#setAction
	 */
	public void setAction(String actionID, IAction action) {
		Assert.isNotNull(actionID);
		if (action == null) {
			action= (IAction) fActions.remove(actionID);
			if (action != null)
				fActivationCodeTrigger.unregisterActionFromKeyActivation(action);
		} else {
			fActions.put(actionID, action);
			fActivationCodeTrigger.registerActionForKeyActivation(action);
		}
	}
	
	/*
	 * @see ITextEditor#setActionActivationCode(String, char, int, int)
	 */
	public void setActionActivationCode(String actionID, char activationCharacter, int activationKeyCode, int activationStateMask) {
		
		Assert.isNotNull(actionID);
		
		ActionActivationCode found= findActionActivationCode(actionID);
		if (found == null) {
			found= new ActionActivationCode(actionID);
			fActivationCodes.add(found);
		}
		
		found.fCharacter= activationCharacter;
		found.fKeyCode= activationKeyCode;
		found.fStateMask= activationStateMask;
	}
	
	/**
	 * Returns the activation code registered for the specified action.
	 * 
	 * @param actionID the action id
	 * @return the registered activation code or <code>null</code> if no
	 * 			code has been installed
	 */
	private ActionActivationCode findActionActivationCode(String actionID) {
		int size= fActivationCodes.size();
		for (int i= 0; i < size; i++) {
			ActionActivationCode code= (ActionActivationCode) fActivationCodes.get(i);
			if (actionID.equals(code.fActionId))
				return code;
		}
		return null;
	}
	
	/*
	 * @see ITextEditor#removeActionActivationCode(String)
	 */
	public void removeActionActivationCode(String actionID) {
		Assert.isNotNull(actionID);
		ActionActivationCode code= findActionActivationCode(actionID);
		if (code != null)
			fActivationCodes.remove(code);
	}
	
	/*
	 * @see ITextEditor#getAction
	 */
	public IAction getAction(String actionID) {
		Assert.isNotNull(actionID);
		IAction action= (IAction) fActions.get(actionID);
		
		if (action == null) {
			action= findContributedAction(actionID);
			if (action != null)
				setAction(actionID, action);
		}
		
		return action;
	}
	
	private IAction findContributedAction(String actionID) {
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, "editorActions"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();			

			List actions= new ArrayList();
			for (int i= 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];				
				if (TAG_CONTRIBUTION_TYPE.equals(element.getName())) {
					if (!getSite().getId().equals(element.getAttribute("targetID"))) //$NON-NLS-1$
						continue;

					IConfigurationElement[] children= element.getChildren("action"); //$NON-NLS-1$
					for (int j= 0; j < children.length; j++) {
						IConfigurationElement child= children[j];
						if (actionID.equals(child.getAttribute("actionID"))) //$NON-NLS-1$
							actions.add(child);
					}
				}
			}
			Collections.sort(actions, new ConfigurationElementComparator());

			if (actions.size() != 0) {
				IConfigurationElement element= (IConfigurationElement) actions.get(0);
				return new EditorPluginAction(element, "class", this); //$NON-NLS-1$
			}
		}
		
		return null;
	}
	
	/**
	 * Updates the specified action by calling <code>IUpdate.update</code>
	 * if applicable.
	 *
	 * @param actionId the action id
	 */
	private void updateAction(String actionId) {
		Assert.isNotNull(actionId);
		if (fActions != null) {
			IAction action= (IAction) fActions.get(actionId);
			if (action instanceof IUpdate)
				((IUpdate) action).update();
		}
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
	 * Marks or unmarks the given action to be updated on property changes.
	 *
	 * @param actionId the action id
	 * @param mark <code>true</code> if the action is property dependent
	 */
	public void markAsPropertyDependentAction(String actionId, boolean mark) {
		Assert.isNotNull(actionId);
		if (mark) {
			if (!fPropertyActions.contains(actionId))
				fPropertyActions.add(actionId);
		} else
			fPropertyActions.remove(actionId);
	}
	
	/**
	 * Updates all selection dependent actions.
	 */
	protected void updateSelectionDependentActions() {
		if (fSelectionActions != null) {
			Iterator e= fSelectionActions.iterator();
			while (e.hasNext())
				updateAction((String) e.next());
		}
	}
	
	/**
	 * Updates all content dependent actions.
	 */
	protected void updateContentDependentActions() {
		if (fContentActions != null) {
			Iterator e= fContentActions.iterator();
			while (e.hasNext())
				updateAction((String) e.next());
		}
	}
	
	/**
	 * Updates all property dependent actions.
	 */
	protected void updatePropertyDependentActions() {
		if (fPropertyActions != null) {
			Iterator e= fPropertyActions.iterator();
			while (e.hasNext())
				updateAction((String) e.next());
		}
	}
	
	/**
	 * Creates this editor's standard navigation actions.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	protected void createNavigationActions() {
		
		IAction action;
		
		StyledText textWidget= getSourceViewer().getTextWidget();
		for (int i= 0; i < ACTION_MAP.length; i++) {
			IdMapEntry entry= (IdMapEntry) ACTION_MAP[i];
			action= new TextNavigationAction(textWidget, entry.getAction());
			action.setActionDefinitionId(entry.getActionId());
			setAction(entry.getActionId(), action);
		}
		
		action=  new ScrollLinesAction(-1);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SCROLL_LINE_UP);
		setAction(ITextEditorActionDefinitionIds.SCROLL_LINE_UP, action);
		
		action= new ScrollLinesAction(1);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN);
		setAction(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN, action);
	}
	
	/**
	 * Creates this editor's standard actions and connects them with the global
	 * workbench actions.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	protected void createActions() {
		
		ResourceAction action;
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Undo.", this, ITextOperationTarget.UNDO); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.UNDO_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.UNDO);
		setAction(ITextEditorActionConstants.UNDO, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Redo.", this, ITextOperationTarget.REDO); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.REDO_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.REDO);
		setAction(ITextEditorActionConstants.REDO, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Cut.", this, ITextOperationTarget.CUT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CUT);
		setAction(ITextEditorActionConstants.CUT, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Copy.", this, ITextOperationTarget.COPY); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.COPY);
		setAction(ITextEditorActionConstants.COPY, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Paste.", this, ITextOperationTarget.PASTE); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.PASTE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.PASTE);
		setAction(ITextEditorActionConstants.PASTE, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Delete.", this, ITextOperationTarget.DELETE); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.DELETE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE);
		setAction(ITextEditorActionConstants.DELETE, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.SelectAll.", this, ITextOperationTarget.SELECT_ALL); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SELECT_ALL_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_ALL);
		setAction(ITextEditorActionConstants.SELECT_ALL, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.ShiftRight.", this, ITextOperationTarget.SHIFT_RIGHT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SHIFT_RIGHT_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHIFT_RIGHT);
		setAction(ITextEditorActionConstants.SHIFT_RIGHT, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.ShiftLeft.", this, ITextOperationTarget.SHIFT_LEFT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SHIFT_LEFT_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHIFT_LEFT);
		setAction(ITextEditorActionConstants.SHIFT_LEFT, action);
		
		action= new TextOperationAction(EditorMessages.getResourceBundle(), "Editor.Print.", this, ITextOperationTarget.PRINT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.PRINT_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.PRINT);
		setAction(ITextEditorActionConstants.PRINT, action);
		
		action= new FindReplaceAction(EditorMessages.getResourceBundle(), "Editor.FindReplace.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.FIND_REPLACE);
		setAction(ITextEditorActionConstants.FIND, action);

		action= new FindNextAction(EditorMessages.getResourceBundle(), "Editor.FindNext.", this, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.FIND_NEXT);
		setAction(ITextEditorActionConstants.FIND_NEXT, action);

		action= new FindNextAction(EditorMessages.getResourceBundle(), "Editor.FindPrevious.", this, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.FIND_PREVIOUS);
		setAction(ITextEditorActionConstants.FIND_PREVIOUS, action);

		action= new IncrementalFindAction(EditorMessages.getResourceBundle(), "Editor.FindIncremental.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.FIND_INCREMENTAL);
		setAction(ITextEditorActionConstants.FIND_INCREMENTAL, action);
		
		action= new AddMarkerAction(EditorMessages.getResourceBundle(), "Editor.AddBookmark.", this, IMarker.BOOKMARK, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.BOOKMARK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.ADD_BOOKMARK);
		setAction(ITextEditorActionConstants.BOOKMARK, action);
		
		action= new AddMarkerAction(EditorMessages.getResourceBundle(), "Editor.AddTask.", this, IMarker.TASK, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.ADD_TASK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.ADD_TASK);
		setAction(ITextEditorActionConstants.ADD_TASK, action);
		
		action= new SaveAction(EditorMessages.getResourceBundle(), "Editor.Save.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SAVE_ACTION);
		// action.setActionDefinitionId(ITextEditorActionDefinitionIds.SAVE);
		setAction(ITextEditorActionConstants.SAVE, action);
		
		action= new RevertToSavedAction(EditorMessages.getResourceBundle(), "Editor.Revert.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.REVERT_TO_SAVED_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.REVERT_TO_SAVED);
		setAction(ITextEditorActionConstants.REVERT_TO_SAVED, action);
		
		action= new GotoLineAction(EditorMessages.getResourceBundle(), "Editor.GotoLine.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.GOTO_LINE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_GOTO);
		setAction(ITextEditorActionConstants.GOTO_LINE, action);
		
		markAsContentDependentAction(ITextEditorActionConstants.UNDO, true);
		markAsContentDependentAction(ITextEditorActionConstants.REDO, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_NEXT, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_PREVIOUS, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_INCREMENTAL, true);
		
		markAsSelectionDependentAction(ITextEditorActionConstants.CUT, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.COPY, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.PASTE, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.DELETE, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.SHIFT_RIGHT, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.SHIFT_LEFT, true);
		
		markAsPropertyDependentAction(ITextEditorActionConstants.REVERT_TO_SAVED, true);
		
		setActionActivationCode(ITextEditorActionConstants.SHIFT_RIGHT,'\t', 0, 0);
		setActionActivationCode(ITextEditorActionConstants.SHIFT_LEFT, '\t', 0, SWT.SHIFT);
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
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_PRINT));
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		addAction(menu, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.SHIFT_LEFT);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_FIND));
		addAction(menu, ITextEditorActionConstants.FIND);
		addAction(menu, ITextEditorActionConstants.GOTO_LINE);
		
		String label= EditorMessages.getString("Editor.AddMenu.label"); //$NON-NLS-1$
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
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		
		if (IVerticalRulerInfo.class.equals(required))
			return fVerticalRuler;
		
		if (IncrementalFindTarget.class.equals(required)) {
			if (fIncrementalFindTarget == null) {
				IEditorActionBarContributor contributor= getEditorSite().getActionBarContributor();
				if (contributor instanceof EditorActionBarContributor) {
					IStatusLineManager manager= ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
					fIncrementalFindTarget= (fSourceViewer == null ? null : new IncrementalFindTarget(fSourceViewer, manager));
				}
			}
			return fIncrementalFindTarget;
		}
		
		if (IFindReplaceTarget.class.equals(required)) {
			IFindReplaceTarget target= (fSourceViewer == null ? null : fSourceViewer.getFindReplaceTarget());
			if (target != null && target instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) target).setScopeHighlightColor(fFindScopeHighlightColor);
			return target;
		}
		
		if (ITextOperationTarget.class.equals(required))
			return (fSourceViewer == null ? null : fSourceViewer.getTextOperationTarget());
		
		return super.getAdapter(required);
	}
		
	/*
	 * @see IDesktopPart#setFocus()
	 */
	public void setFocus() {
		if (fSourceViewer != null && fSourceViewer.getTextWidget() != null)
			fSourceViewer.getTextWidget().setFocus();
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
		
	/*
	 * @see ITextEditor#showsHighlightRangeOnly
	 */
	public boolean showsHighlightRangeOnly() {
		return fShowHighlightRangeOnly;
	}
	
	/*
	 * @see ITextEditor#showHighlightRangeOnly
	 */
	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
		fShowHighlightRangeOnly= showHighlightRangeOnly;
	}
	
	/*
	 * @see ITextEditor#setHighlightRange
	 */
	public void setHighlightRange(int start, int length, boolean moveCursor) {
		if (fSourceViewer == null)
			return;
			
		if (fShowHighlightRangeOnly) {
			if (moveCursor) {
				IRegion visibleRegion= fSourceViewer.getVisibleRegion();
				if (start != visibleRegion.getOffset() || length != visibleRegion.getLength())
					fSourceViewer.setVisibleRegion(start, length);
			}
		} else {
			IRegion rangeIndication= fSourceViewer.getRangeIndication();
			if (rangeIndication == null || start != rangeIndication.getOffset() || length != rangeIndication.getLength())
				fSourceViewer.setRangeIndication(start, length, moveCursor);
		}
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
	 * @see ITextEditor#selectAndReveal
	 */
	public void selectAndReveal(int start, int length) {
		if (fSourceViewer == null)
			return;
		
		
		StyledText widget= fSourceViewer.getTextWidget();
		widget.setRedraw(false);
		{
			adjustHighlightRange(start, length);
			
			fSourceViewer.revealRange(start, length);
			fSourceViewer.setSelectedRange(start, length);
		}
		widget.setRedraw(true);
	}
	
	/*
	 * @see EditorPart#firePropertyChange
	 */
	protected void firePropertyChange(int property) {
		super.firePropertyChange(property);
		updatePropertyDependentActions();
	}
	
	/*
	 * @see ITextEditorExtension#setStatusField(IStatusField, String)
	 */
	public void setStatusField(IStatusField field, String category) {
		Assert.isNotNull(category);
		if (field != null) {
			
			if (fStatusFields == null)
				fStatusFields= new HashMap(3);			
			
			fStatusFields.put(category, field);
			updateStatusField(category);
			
		} else if (fStatusFields != null)
			fStatusFields.remove(category);
	}
	
	/**
	 * Returns the current status field for the given status category.
	 * 
	 * @param category the status category
	 * @return the current status field for the given status category.
	 */
	protected IStatusField getStatusField(String category) {
		if (category != null && fStatusFields != null)
			return (IStatusField) fStatusFields.get(category);
		return null;
	}
	
	/**
	 * Returns whether this editor is in overwrite or insert mode.
	 * 
	 * @return <code>true</code> if in insert mode,
	 * 	<code>false</code> for overwrite mode
	 */
	protected boolean isInInsertMode() {
		return !fOverwriting;
	}
	
	/**
	 * Handles a potential change of the cursor position.
	 */
	protected void handleCursorPositionChanged() {
		updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
	}
	
	/**
	 * Handles a change of the editor's insert mode.
	 */
	protected void handleInsertModeChanged() {
		updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE);
	}
	
	/**
	 * Updates the status fields for the given category.
	 * 
	 * @param category
	 */
	protected void updateStatusField(String category) {
		if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION.equals(category)) {
			
			IStatusField field= getStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
			if (field != null)
				field.setText(getCursorPosition());
		
		} else if (ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE.equals(category)) {
			
			IStatusField field= getStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);
			if (field != null)
				field.setText(isEditorInputReadOnly() ? fReadOnlyLabel : fWritableLabel);
		
		} else if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE.equals(category)) {
			
			IStatusField field= getStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE);
			if (field != null)
				field.setText(isInInsertMode() ? fInsertModeLabel : fOverwriteModeLabel);
		}	
	}
	
	/**
	 * Returns a description of the cursor position.
	 * 
	 * @return a description of the cursor position
	 */
	protected String getCursorPosition() {
		StyledText styledText= fSourceViewer.getTextWidget();
		
		int offset= fSourceViewer.getVisibleRegion().getOffset();
		int caret= offset + styledText.getCaretOffset();
		IDocument document= fSourceViewer.getDocument();
		
		try {
			
			int line=document.getLineOfOffset(caret);
			
			int lineOffset= document.getLineOffset(line);
			int occurrences= 0;
			for (int i= lineOffset; i < caret; i++)
				if ('\t' == document.getChar(i))
					++ occurrences;
					
			int tabWidth= styledText.getTabs();
			int column= caret - lineOffset + (tabWidth -1) * occurrences;
			
			fLineLabel.fValue= line + 1;
			fColumnLabel.fValue= column + 1;
			return MessageFormat.format(fPositionLabelPattern, fPositionLabelPatternArguments);
			
		} catch (BadLocationException x) {
			return fPositionLabelError;
		}
	}
	
	/*
	 * @see ITextEditorExtension#isEditorInputReadOnly()
	 */
	public boolean isEditorInputReadOnly() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			return extension.isReadOnly(getEditorInput());
		}
		return true;
	}
}