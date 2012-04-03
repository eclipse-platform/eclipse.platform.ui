/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris.Dennis@invidi.com - http://bugs.eclipse.org/bugs/show_bug.cgi?id=29027
 *     Michel Ishizuka (cqw10305@nifty.com) - http://bugs.eclipse.org/bugs/show_bug.cgi?id=68963
 *     Genady Beryozkin, me@genady.org - https://bugs.eclipse.org/bugs/show_bug.cgi?id=11668
 *     Benjamin Muskalla <b.muskalla@gmx.net> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=41573
 *     Stephan Wahlbrink <stephan.wahlbrink@walware.de> - Wrong operations mode/feedback for text drag over/drop in text editors - https://bugs.eclipse.org/bugs/show_bug.cgi?id=206043
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *     Nick Sandonato <nsandona@us.ibm.com> - [implementation] AbstractTextEditor does not prompt when out of sync in MultiPageEditorPart - http://bugs.eclipse.org/337719
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IMarkRegionTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.ITextViewerExtension7;
import org.eclipse.jface.text.ITextViewerExtension8;
import org.eclipse.jface.text.ITextViewerExtension8.EnrichMode;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.hyperlink.HyperlinkManager;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension3;
import org.eclipse.jface.text.source.ISourceViewerExtension4;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerExtension;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.VerticalRuler;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.CommandNotMappedException;
import org.eclipse.ui.actions.ContributedAction;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.dnd.IDragAndDropService;
import org.eclipse.ui.internal.texteditor.EditPosition;
import org.eclipse.ui.internal.texteditor.FocusedInformationPresenter;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.rulers.StringSetSerializer;
import org.eclipse.ui.operations.LinearUndoViolationUserApprover;
import org.eclipse.ui.operations.NonLocalUndoUserApprover;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.ui.texteditor.rulers.RulerColumnPreferenceAdapter;
import org.eclipse.ui.texteditor.rulers.RulerColumnRegistry;


/**
 * Abstract base implementation of a text editor.
 * <p>
 * Subclasses are responsible for configuring the editor appropriately. The standard text editor,
 * <code>TextEditor</code>, is one such example.
 * </p>
 * <p>
 * If a subclass calls {@linkplain #setEditorContextMenuId(String) setEditorContextMenuId} the
 * argument is used as the id under which the editor's context menu is registered for extensions. If
 * no id is set, the context menu is registered under <b>[editor_id].EditorContext</b> whereby
 * [editor_id] is replaced with the editor's part id. If the editor is instructed to run in version
 * 1.0 context menu registration compatibility mode, the latter form of the registration even
 * happens if a context menu id has been set via {@linkplain #setEditorContextMenuId(String)
 * setEditorContextMenuId}. If no id is set while in compatibility mode, the menu is registered
 * under {@link #DEFAULT_EDITOR_CONTEXT_MENU_ID}.
 * </p>
 * <p>
 * If a subclass calls {@linkplain #setRulerContextMenuId(String) setRulerContextMenuId} the
 * argument is used as the id under which the ruler's context menu is registered for extensions. If
 * no id is set, the context menu is registered under <b>[editor_id].RulerContext</b> whereby
 * [editor_id] is replaced with the editor's part id. If the editor is instructed to run in version
 * 1.0 context menu registration compatibility mode, the latter form of the registration even
 * happens if a context menu id has been set via {@linkplain #setRulerContextMenuId(String)
 * setRulerContextMenuId}. If no id is set while in compatibility mode, the menu is registered under
 * {@link #DEFAULT_RULER_CONTEXT_MENU_ID}.
 * </p>
 * <p>
 * As of 3.5, contributers can contribute editor and ruler context menu actions to all subclasses of
 * this class by using {@link #COMMON_EDITOR_CONTEXT_MENU_ID} and
 * {@link #COMMON_RULER_CONTEXT_MENU_ID}.
 * </p>
 */
public abstract class AbstractTextEditor extends EditorPart implements ITextEditor, IReusableEditor, ITextEditorExtension, ITextEditorExtension2, ITextEditorExtension3, ITextEditorExtension4, ITextEditorExtension5, INavigationLocationProvider, ISaveablesSource, IPersistableEditor {

	/**
	 * Tag used in xml configuration files to specify editor action contributions.
	 * Current value: <code>editorContribution</code>
	 * @since 2.0
	 */
	private static final String TAG_CONTRIBUTION_TYPE= "editorContribution"; //$NON-NLS-1$

	/**
	 * Tag used in the {@link IMemento} when saving and restoring the editor's selection offset.
	 * 
	 * @see #saveState(IMemento)
	 * @see #restoreState(IMemento)
	 * @see #doRestoreState(IMemento)
	 * @since 3.3
	 */
	protected static final String TAG_SELECTION_OFFSET= "selectionOffset"; //$NON-NLS-1$

	/**
	 * Tag used in the {@link IMemento} when saving and restoring the editor's selection length.
	 * 
	 * @see #saveState(IMemento)
	 * @see #restoreState(IMemento)
	 * @see #doRestoreState(IMemento)
	 * @since 3.3
	 */
	protected static final String TAG_SELECTION_LENGTH= "selectionLength"; //$NON-NLS-1$

	/**
	 * Tag used in the {@link IMemento} when saving and restoring the editor's top pixel value.
	 * 
	 * @see #saveState(IMemento)
	 * @see #restoreState(IMemento)
	 * @see #doRestoreState(IMemento)
	 * @since 3.6
	 */
	protected static final String TAG_SELECTION_TOP_PIXEL= "selectionTopPixel"; //$NON-NLS-1$

	/**
	 * Tag used in the {@link IMemento} when saving and restoring the editor's horizontal pixel
	 * value.
	 * 
	 * @see #saveState(IMemento)
	 * @see #restoreState(IMemento)
	 * @see #doRestoreState(IMemento)
	 * @since 3.6
	 */
	protected static final String TAG_SELECTION_HORIZONTAL_PIXEL= "selectionHorizontalPixel"; //$NON-NLS-1$


	/**
	 * The caret width for the wide (double) caret.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=21715.
	 * Value: {@value}
	 * @since 3.0
	 */
	private static final int WIDE_CARET_WIDTH= 2;

	/**
	 * The caret width for the narrow (single) caret.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=21715.
	 * Value: {@value}
	 * @since 3.0
	 */
	private static final int SINGLE_CARET_WIDTH= 1;

	/**
	 * The symbolic name of the block seletion mode font.
	 * 
	 * @since 3.5
	 */
	private static final String BLOCK_SELECTION_MODE_FONT= "org.eclipse.ui.workbench.texteditor.blockSelectionModeFont"; //$NON-NLS-1$

	/**
	 * The text input listener.
	 *
	 * @see ITextInputListener
	 * @since 2.1
	 */
	private static class TextInputListener implements ITextInputListener {
		/** Indicates whether the editor input changed during the process of state validation. */
		public boolean inputChanged;

		/* Detectors for editor input changes during the process of state validation. */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {}
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) { inputChanged= true; }
	}

	/**
	 * Internal element state listener.
	 */
	class ElementStateListener implements IElementStateListener, IElementStateListenerExtension {

			/**
			 * Internal <code>VerifyListener</code> for performing the state validation of the
			 * editor input in case of the first attempted manipulation via typing on the keyboard.
			 * @since 2.0
			 */
			class Validator implements VerifyListener {
				/*
				 * @see VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
				 */
				public void verifyText(VerifyEvent e) {
					IDocument document= getDocumentProvider().getDocument(getEditorInput());
					final boolean[] documentChanged= new boolean[1];
					IDocumentListener listener= new IDocumentListener() {
						public void documentAboutToBeChanged(DocumentEvent event) {
						}
						public void documentChanged(DocumentEvent event) {
							documentChanged[0]= true;
						}
					};
					try {
						if (document != null)
							document.addDocumentListener(listener);
						if (! validateEditorInputState() || documentChanged[0])
							e.doit= false;
					} finally {
						if (document != null)
							document.removeDocumentListener(listener);
					}
				}
			}

		/**
		 * The listener's validator.
		 * @since 2.0
		 */
		private Validator fValidator;
		/**
		 * The display used for posting runnable into the UI thread.
		 * @since 3.0
		 */
		private Display fDisplay;

		/*
		 * @see IElementStateListenerExtension#elementStateValidationChanged(Object, boolean)
		 * @since 2.0
		 */
		public void elementStateValidationChanged(final Object element, final boolean isStateValidated) {
			if (element != null && element.equals(getEditorInput())) {
				Runnable r= new Runnable() {
					public void run() {
						enableSanityChecking(true);
						if (isStateValidated) {
							if (fValidator != null) {
								ISourceViewer viewer= fSourceViewer;
								if (viewer != null) {
									StyledText textWidget= viewer.getTextWidget();
									if (textWidget != null && !textWidget.isDisposed())
										textWidget.removeVerifyListener(fValidator);
									fValidator= null;
								}
							}
							enableStateValidation(false);
						} else if (!isStateValidated && fValidator == null) {
							ISourceViewer viewer= fSourceViewer;
							if (viewer != null) {
								StyledText textWidget= viewer.getTextWidget();
								if (textWidget != null && !textWidget.isDisposed()) {
									fValidator= new Validator();
									enableStateValidation(true);
									textWidget.addVerifyListener(fValidator);
								}
							}
						}
					}
				};
				execute(r, false);
			}
		}


		/*
		 * @see IElementStateListener#elementDirtyStateChanged(Object, boolean)
		 */
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			if (element != null && element.equals(getEditorInput())) {
				Runnable r= new Runnable() {
					public void run() {
						enableSanityChecking(true);
						firePropertyChange(PROP_DIRTY);
					}
				};
				execute(r, false);
			}
		}

		/*
		 * @see IElementStateListener#elementContentAboutToBeReplaced(Object)
		 */
		public void elementContentAboutToBeReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				Runnable r= new Runnable() {
					public void run() {
						enableSanityChecking(true);
						rememberSelection();
						resetHighlightRange();
					}
				};
				execute(r, false);
			}
		}

		/*
		 * @see IElementStateListener#elementContentReplaced(Object)
		 */
		public void elementContentReplaced(Object element) {
			if (element != null && element.equals(getEditorInput())) {
				Runnable r= new Runnable() {
					public void run() {
						enableSanityChecking(true);
						firePropertyChange(PROP_DIRTY);
						restoreSelection();
						handleElementContentReplaced();
					}
				};
				execute(r, false);
			}
		}

		/*
		 * @see IElementStateListener#elementDeleted(Object)
		 */
		public void elementDeleted(Object deletedElement) {
			if (deletedElement != null && deletedElement.equals(getEditorInput())) {
				Runnable r= new Runnable() {
					public void run() {
						enableSanityChecking(true);
						close(false);
					}
				};
				execute(r, false);
			}
		}

		/*
		 * @see IElementStateListener#elementMoved(Object, Object)
		 */
		public void elementMoved(final Object originalElement, final Object movedElement) {
			if (originalElement != null && originalElement.equals(getEditorInput())) {
				final boolean doValidationAsync= Display.getCurrent() != null;
				Runnable r= new Runnable() {
					public void run() {
						enableSanityChecking(true);

						if (fSourceViewer == null)
							return;

						if (!canHandleMove((IEditorInput) originalElement, (IEditorInput) movedElement)) {
							close(true);
							return;
						}

						if (movedElement == null || movedElement instanceof IEditorInput) {
							rememberSelection();

							final IDocumentProvider d= getDocumentProvider();
							final String previousContent;
							IDocumentUndoManager previousUndoManager=null;
							IDocument changed= null;
							boolean wasDirty= isDirty();
							changed= d.getDocument(getEditorInput());
							if (changed != null) {
								if (wasDirty)
									previousContent= changed.get();
								else
									previousContent= null;

								previousUndoManager= DocumentUndoManagerRegistry.getDocumentUndoManager(changed);
								if (previousUndoManager != null)
									previousUndoManager.connect(this);
							}
							else
								previousContent= null;

							setInput((IEditorInput) movedElement);

							// The undo manager needs to be replaced with one for the new document.
							// Transfer the undo history and then disconnect from the old undo manager.
							if (previousUndoManager != null) {
								IDocument newDocument= getDocumentProvider().getDocument(movedElement);
								if (newDocument != null) {
									IDocumentUndoManager newUndoManager= DocumentUndoManagerRegistry.getDocumentUndoManager(newDocument);
									if (newUndoManager != null)
										newUndoManager.transferUndoHistory(previousUndoManager);
								}
								previousUndoManager.disconnect(this);
							}

							if (wasDirty && changed != null) {
								Runnable r2= new Runnable() {
									public void run() {
										validateState(getEditorInput());
										d.getDocument(getEditorInput()).set(previousContent);
										updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);
										restoreSelection();
									}
								};
								execute(r2, doValidationAsync);
							} else
								restoreSelection();

						}
					}
				};
				execute(r, false);
			}
		}

		/*
		 * @see IElementStateListenerExtension#elementStateChanging(Object)
		 * @since 2.0
		 */
		public void elementStateChanging(Object element) {
			if (element != null && element.equals(getEditorInput()))
				enableSanityChecking(false);
		}

		/*
		 * @see IElementStateListenerExtension#elementStateChangeFailed(Object)
		 * @since 2.0
		 */
		public void elementStateChangeFailed(Object element) {
			if (element != null && element.equals(getEditorInput()))
				enableSanityChecking(true);
		}

		/**
		 * Executes the given runnable in the UI thread.
		 * <p>
		 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=76765 for details
		 * about why the parameter <code>postAsync</code> has been
		 * introduced in the course of 3.1.
		 *
		 * @param runnable runnable to be executed
		 * @param postAsync <code>true</code> if the runnable must be posted asynchronous, <code>false</code> otherwise
		 * @since 3.0
		 */
		private void execute(Runnable runnable, boolean postAsync) {
			if (postAsync || Display.getCurrent() == null) {
				if (fDisplay == null)
					fDisplay= getSite().getShell().getDisplay();
				fDisplay.asyncExec(runnable);
			} else
				runnable.run();
		}
	}

	/**
	 * Internal text listener for updating all content dependent
	 * actions. The updating is done asynchronously.
	 */
	class TextListener implements ITextListener, ITextInputListener {

		/** The posted updater code. */
		private Runnable fRunnable= new Runnable() {
			public void run() {
				fIsRunnablePosted= false;

				if (fSourceViewer != null) {
					updateContentDependentActions();

					// remember the last edit position
					if (isDirty() && fUpdateLastEditPosition) {
						fUpdateLastEditPosition= false;
						ISelection sel= getSelectionProvider().getSelection();
						IEditorInput input= getEditorInput();
						IDocument document= getDocumentProvider().getDocument(input);

						if (fLocalLastEditPosition != null) {
							document.removePosition(fLocalLastEditPosition);
							fLocalLastEditPosition= null;
						}

						if (sel instanceof ITextSelection && !sel.isEmpty()) {
							ITextSelection s= (ITextSelection) sel;
							fLocalLastEditPosition= new Position(s.getOffset(), s.getLength());
							try {
								document.addPosition(fLocalLastEditPosition);
							} catch (BadLocationException ex) {
								fLocalLastEditPosition= null;
							}
						}
						TextEditorPlugin.getDefault().setLastEditPosition(new EditPosition(input, getEditorSite().getId(), fLocalLastEditPosition));
					}
				}
			}
		};

		/** Display used for posting the updater code. */
		private Display fDisplay;
		/**
		 * The editor's last edit position
		 * @since 3.0
		 */
		private Position fLocalLastEditPosition;
		/**
		 * Has the runnable been posted?
		 * @since 3.0
		 */
		private boolean fIsRunnablePosted= false;
		/**
		 * Should the last edit position be updated?
		 * @since 3.0
		 */
		private boolean fUpdateLastEditPosition= false;

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {

			/*
			 * Also works for text events which do not base on a DocumentEvent.
			 * This way, if the visible document of the viewer changes, all content
			 * dependent actions are updated as well.
			 */

			if (fDisplay == null)
				fDisplay= getSite().getShell().getDisplay();

			if (event.getDocumentEvent() != null)
				fUpdateLastEditPosition= true;

			if (!fIsRunnablePosted) {
				fIsRunnablePosted= true;
				fDisplay.asyncExec(fRunnable);
			}
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput != null && fLocalLastEditPosition != null) {
				oldInput.removePosition(fLocalLastEditPosition);
				fLocalLastEditPosition= null;
			}
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		}
	}

	/**
	 * Internal property change listener for handling changes in the editor's preferences.
	 */
	class PropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			handlePreferenceStoreChanged(event);
		}
	}

	/**
	 * Internal property change listener for handling workbench font changes.
	 * @since 2.1
	 */
	class FontPropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (fSourceViewer == null)
				return;

			String property= event.getProperty();

			// IMPORTANT: Do not call isBlockSelectionModeEnabled() before checking the property!

			if (BLOCK_SELECTION_MODE_FONT.equals(property) && isBlockSelectionModeEnabled()) {
				Font blockFont= JFaceResources.getFont(BLOCK_SELECTION_MODE_FONT);
				setFont(fSourceViewer, blockFont);
				disposeFont();
				updateCaret();
				return;
			}
			if (getFontPropertyPreferenceKey().equals(property) && !isBlockSelectionModeEnabled()) {
				initializeViewerFont(fSourceViewer);
				updateCaret();
				return;
			}
		}
	}

	/**
	 * Internal key verify listener for triggering action activation codes.
	 */
	class ActivationCodeTrigger implements VerifyKeyListener {

		/** Indicates whether this trigger has been installed. */
		private boolean fIsInstalled= false;
		/**
		 * The key binding service to use.
		 * @since 2.0
		 */
		private IKeyBindingService fKeyBindingService;

		/*
		 * @see VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
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

						if (!action.isEnabled() && action instanceof IReadOnlyDependent) {
							IReadOnlyDependent dependent= (IReadOnlyDependent) action;
							boolean writable= dependent.isEnabled(true);
							if (writable) {
								event.doit= false;
								return;
							}
						} else if (action.isEnabled()) {
							event.doit= false;
							action.run();
							return;
						}
					}
				}
			}
		}

		/**
		 * Installs this trigger on the editor's text widget.
		 * @since 2.0
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
				fIsInstalled= true;
			}
		}

		/**
		 * Uninstalls this trigger from the editor's text widget.
		 * @since 2.0
		 */
		public void uninstall() {
			if (fIsInstalled) {

				if (fSourceViewer instanceof ITextViewerExtension) {
					ITextViewerExtension e= (ITextViewerExtension) fSourceViewer;
					e.removeVerifyKeyListener(this);
				} else if (fSourceViewer != null) {
					StyledText text= fSourceViewer.getTextWidget();
					if (text != null && !text.isDisposed())
						text.removeVerifyKeyListener(fActivationCodeTrigger);
				}

				fIsInstalled= false;
				fKeyBindingService= null;
			}
		}

		/**
		 * Registers the given action for key activation.
		 * @param action the action to be registered
		 * @since 2.0
		 */
		public void registerActionForKeyActivation(IAction action) {
			if (fIsInstalled && action.getActionDefinitionId() != null)
				fKeyBindingService.registerAction(action);
		}

		/**
		 * The given action is no longer available for key activation
		 * @param action the action to be unregistered
		 * @since 2.0
		 */
		public void unregisterActionFromKeyActivation(IAction action) {
			if (fIsInstalled && action.getActionDefinitionId() != null)
				fKeyBindingService.unregisterAction(action);
		}

		/**
		 * Sets the key binding scopes for this editor.
		 * @param keyBindingScopes the key binding scopes
		 * @since 2.1
		 */
		public void setScopes(String[] keyBindingScopes) {
			if (keyBindingScopes != null && keyBindingScopes.length > 0)
				fKeyBindingService.setScopes(keyBindingScopes);
		}
	}

	/**
	 * Representation of action activation codes.
	 */
	static class ActionActivationCode {

		/** The action id. */
		public String fActionId;
		/** The character. */
		public char fCharacter;
		/** The key code. */
		public int fKeyCode= -1;
		/** The state mask. */
		public int fStateMask= SWT.DEFAULT;

		/**
		 * Creates a new action activation code for the given action id.
		 * @param actionId the action id
		 */
		public ActionActivationCode(String actionId) {
			fActionId= actionId;
		}

		/**
		 * Returns <code>true</code> if this activation code matches the given verify event.
		 * @param event the event to test for matching
		 * @return whether this activation code matches <code>event</code>
		 */
		public boolean matches(VerifyEvent event) {
			return (event.character == fCharacter &&
						(fKeyCode == -1 || event.keyCode == fKeyCode) &&
						(fStateMask == SWT.DEFAULT || event.stateMask == fStateMask));
		}
	}

	/**
	 * Internal part and shell activation listener for triggering state validation.
	 * @since 2.0
	 */
	class ActivationListener implements IPartListener, IWindowListener {

		/** Cache of the active workbench part. */
		private IWorkbenchPart fActivePart;
		/** Indicates whether activation handling is currently be done. */
		private boolean fIsHandlingActivation= false;
		/**
		 * The part service.
		 * @since 3.1
		 */
		private IPartService fPartService;

		/**
		 * Creates this activation listener.
		 *
		 * @param partService the part service on which to add the part listener
		 * @since 3.1
		 */
		public ActivationListener(IPartService partService) {
			fPartService= partService;
			fPartService.addPartListener(this);
			PlatformUI.getWorkbench().addWindowListener(this);
		}

		/**
		 * Disposes this activation listener.
		 *
		 * @since 3.1
		 */
		public void dispose() {
			fPartService.removePartListener(this);
			PlatformUI.getWorkbench().removeWindowListener(this);
			fPartService= null;
		}

		/*
		 * @see IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
			fActivePart= part;
			handleActivation();
		}

		/*
		 * @see IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		/*
		 * @see IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
		}

		/*
		 * @see IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {
			fActivePart= null;
		}

		/*
		 * @see IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {
			// Restore the saved state if any
			if ((part == AbstractTextEditor.this || part.getAdapter(AbstractTextEditor.class) == AbstractTextEditor.this) && fMementoToRestore != null && containsSavedState(fMementoToRestore)) {
				doRestoreState(fMementoToRestore);
				fMementoToRestore= null;
			}
		}

		/**
		 * Handles the activation triggering a element state check in the editor.
		 */
		private void handleActivation() {
			if (fIsHandlingActivation)
				return;

			if (fActivePart == AbstractTextEditor.this || fActivePart != null && fActivePart.getAdapter(AbstractTextEditor.class) == AbstractTextEditor.this) {
				fIsHandlingActivation= true;
				try {
					safelySanityCheckState(getEditorInput());
				} finally {
					fIsHandlingActivation= false;
					fHasBeenActivated= true;
				}
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowActivated(IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow()) {
				/*
				 * Workaround for problem described in
				 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11731
				 * Will be removed when SWT has solved the problem.
				 */
				window.getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						handleActivation();
					}
				});
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowDeactivated(IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowClosed(IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowOpened(IWorkbenchWindow window) {
		}
	}

	/**
	 * Internal interface for a cursor listener. I.e. aggregation
	 * of mouse and key listener.
	 * @since 2.0
	 */
	interface ICursorListener extends MouseListener, KeyListener {
	}

	/**
	 * Maps an action definition id to an StyledText action.
	 * @since 2.0
	 */
	protected static final class IdMapEntry {

		/** The action id. */
		private String fActionId;
		/** The StyledText action. */
		private int fAction;

		/**
		 * Creates a new mapping.
		 * @param actionId the action id
		 * @param action the StyledText action
		 */
		public IdMapEntry(String actionId, int action) {
			fActionId= actionId;
			fAction= action;
		}

		/**
		 * Returns the action id.
		 * @return the action id
		 */
		public String getActionId() {
			return fActionId;
		}

		/**
		 * Returns the action.
		 * @return the action
		 */
		public int getAction() {
			return fAction;
		}
	}

	/**
	 * Internal action to scroll the editor's viewer by a specified number of lines.
	 * @since 2.0
	 */
	class ScrollLinesAction extends Action {

		/** Number of lines to scroll. */
		private int fScrollIncrement;

		/**
		 * Creates a new scroll action that scroll the given number of lines. If the
		 * increment is &lt; 0, it's scrolling up, if &gt; 0 it's scrolling down.
		 * @param scrollIncrement the number of lines to scroll
		 */
		public ScrollLinesAction(int scrollIncrement) {
			fScrollIncrement= scrollIncrement;
		}

		/*
		 * @see IAction#run()
		 */
		public void run() {
			if (fSourceViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
				StyledText textWidget= fSourceViewer.getTextWidget();
				int topIndex= textWidget.getTopIndex();
				int newTopIndex= Math.max(0, topIndex + fScrollIncrement);
				fSourceViewer.setTopIndex(extension.widgetLine2ModelLine(newTopIndex));
			} else {
				int topIndex= fSourceViewer.getTopIndex();
				int newTopIndex= Math.max(0, topIndex + fScrollIncrement);
				fSourceViewer.setTopIndex(newTopIndex);
			}
		}
	}

	/**
	 * Action to toggle the insert mode. The action is checked if smart mode is
	 * turned on.
	 *
	 * @since 2.1
	 */
	class ToggleInsertModeAction extends ResourceAction {

		public ToggleInsertModeAction(ResourceBundle bundle, String prefix) {
			super(bundle, prefix, IAction.AS_CHECK_BOX);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			switchToNextInsertMode();
		}

		/*
		 * @see org.eclipse.jface.action.IAction#isChecked()
		 * @since 3.0
		 */
		public boolean isChecked() {
			return fInsertMode == SMART_INSERT;
		}
	}

	/**
	 * Action to toggle the overwrite mode.
	 *
	 * @since 3.0
	 */
	class ToggleOverwriteModeAction extends ResourceAction {

		public ToggleOverwriteModeAction(ResourceBundle bundle, String prefix) {
			super(bundle, prefix);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			toggleOverwriteMode();
		}
	}

	/**
	 * This action implements smart end.
	 * Instead of going to the end of a line it does the following:
	 * - if smart home/end is enabled and the caret is before the line's last non-whitespace and then the caret is moved directly after it
	 * - if the caret is after last non-whitespace the caret is moved at the end of the line
	 * - if the caret is at the end of the line the caret is moved directly after the line's last non-whitespace character
	 * @since 2.1 (in 3.3 the access modifier changed from package visibility to protected)
	 */
	protected class LineEndAction extends TextNavigationAction {

		/** boolean flag which tells if the text up to the line end should be selected. */
		private boolean fDoSelect;

		/**
		 * Create a new line end action.
		 *
		 * @param textWidget the styled text widget
		 * @param doSelect a boolean flag which tells if the text up to the line end should be selected
		 */
		public LineEndAction(StyledText textWidget, boolean doSelect) {
			super(textWidget, ST.LINE_END);
			fDoSelect= doSelect;
		}

		/**
		 * Computes the offset of the line end position.
		 *
		 * @param document the document where to compute the line end position
		 * @param line the line to determine the end position of
		 * @param length the length of the line
		 * @param offset the caret position in the document
		 * @return the offset of the line end
		 * @since 3.4 protected, was added in 3.3 as private method
		 */
		protected int getLineEndPosition(final IDocument document, final String line, final int length, final int offset) {
			int index= length - 1;
			while (index > -1 && Character.isWhitespace(line.charAt(index)))
				index--;
			index++;

			LinkedModeModel model= LinkedModeModel.getModel(document, offset);
			if (model != null) {
				LinkedPosition linkedPosition= model.findPosition(new LinkedPosition(document, offset, 0));
				if (linkedPosition != null) {
					int linkedPositionEnd= linkedPosition.getOffset() + linkedPosition.getLength();
					int lineOffset;
					try {
						lineOffset= document.getLineInformationOfOffset(offset).getOffset();
						if (offset != linkedPositionEnd && linkedPositionEnd - lineOffset < index)
							index= linkedPositionEnd - lineOffset;
					} catch (BadLocationException e) {
						//should not happen
					}
				}
			}
			return index;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			boolean isSmartHomeEndEnabled= false;
			IPreferenceStore store= getPreferenceStore();
			if (store != null)
				isSmartHomeEndEnabled= store.getBoolean(AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END);

			StyledText st= fSourceViewer.getTextWidget();
			if (st == null || st.isDisposed())
				return;
			int caretOffset= st.getCaretOffset();
			int lineNumber= st.getLineAtOffset(caretOffset);
			int lineOffset= st.getOffsetAtLine(lineNumber);

			int lineLength;
			int caretOffsetInDocument;
			final IDocument document= fSourceViewer.getDocument();

			try {
				caretOffsetInDocument= widgetOffset2ModelOffset(fSourceViewer, caretOffset);
				lineLength= document.getLineInformationOfOffset(caretOffsetInDocument).getLength();
			} catch (BadLocationException ex) {
				return;
			}
			int lineEndOffset= lineOffset + lineLength;

			int delta= lineEndOffset - st.getCharCount();
			if (delta > 0) {
				lineEndOffset -= delta;
				lineLength -= delta;
			}

			String line= ""; //$NON-NLS-1$
			if (lineLength > 0)
				line= st.getText(lineOffset, lineEndOffset - 1);

			// Remember current selection
			Point oldSelection= st.getSelection();

			// The new caret position
			int newCaretOffset= -1;

			if (isSmartHomeEndEnabled) {
				// Compute the line end offset
				int i= getLineEndPosition(document, line, lineLength, caretOffsetInDocument);

				if (caretOffset - lineOffset == i)
					// to end of line
					newCaretOffset= lineEndOffset;
				else
					// to end of text
					newCaretOffset= lineOffset + i;

			} else {

				if (caretOffset < lineEndOffset)
					// to end of line
					newCaretOffset= lineEndOffset;

			}

			if (newCaretOffset == -1)
				newCaretOffset= caretOffset;
			else
				st.setCaretOffset(newCaretOffset);

			st.setCaretOffset(newCaretOffset);
			if (fDoSelect) {
				if (caretOffset < oldSelection.y)
					st.setSelection(oldSelection.y, newCaretOffset);
				else
					st.setSelection(oldSelection.x, newCaretOffset);
			} else
				st.setSelection(newCaretOffset);

			fireSelectionChanged(oldSelection);
		}
	}

	/**
	 * This action implements smart home.
	 * Instead of going to the start of a line it does the following:
	 * - if smart home/end is enabled and the caret is after the line's first non-whitespace then the caret is moved directly before it
	 * - if the caret is before the line's first non-whitespace the caret is moved to the beginning of the line
	 * - if the caret is at the beginning of the line the caret is moved directly before the line's first non-whitespace character
	 * @since 2.1
	 */
	protected class LineStartAction extends TextNavigationAction {

		/** boolean flag which tells if the text up to the beginning of the line should be selected. */
		private final boolean fDoSelect;

		/**
		 * Creates a new line start action.
		 *
		 * @param textWidget the styled text widget
		 * @param doSelect a boolean flag which tells if the text up to the beginning of the line should be selected
		 */
		public LineStartAction(final StyledText textWidget, final boolean doSelect) {
			super(textWidget, ST.LINE_START);
			fDoSelect= doSelect;
		}

		/**
		 * Computes the offset of the line start position.
		 *
		 * @param document the document where to compute the line start position
		 * @param line the line to determine the start position of
		 * @param length the length of the line
		 * @param offset the caret position in the document
		 * @return the offset of the line start
		 * @since 3.0
		 */
		protected int getLineStartPosition(final IDocument document, final String line, final int length, final int offset) {
			int index= 0;
			while (index < length && Character.isWhitespace(line.charAt(index)))
				index++;

			LinkedModeModel model= LinkedModeModel.getModel(document, offset);
			if (model != null) {
				LinkedPosition linkedPosition= model.findPosition(new LinkedPosition(document, offset, 0));
				if (linkedPosition != null) {
					int linkedPositionOffset= linkedPosition.getOffset();
					int lineOffset;
					try {
						lineOffset= document.getLineInformationOfOffset(offset).getOffset();
						if (offset != linkedPositionOffset && index < linkedPositionOffset - lineOffset)
							index= linkedPositionOffset - lineOffset;
					} catch (BadLocationException e) {
						//should not happen
					}
				}
			}
			return index;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			boolean isSmartHomeEndEnabled= false;
			IPreferenceStore store= getPreferenceStore();
			if (store != null)
				isSmartHomeEndEnabled= store.getBoolean(AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END);

			StyledText st= fSourceViewer.getTextWidget();
			if (st == null || st.isDisposed())
				return;

			int caretOffset= st.getCaretOffset();
			int lineNumber= st.getLineAtOffset(caretOffset);
			int lineOffset= st.getOffsetAtLine(lineNumber);

			int lineLength;
			int caretOffsetInDocument;
			final IDocument document= fSourceViewer.getDocument();

			try {
				caretOffsetInDocument= widgetOffset2ModelOffset(fSourceViewer, caretOffset);
				lineLength= document.getLineInformationOfOffset(caretOffsetInDocument).getLength();
			} catch (BadLocationException ex) {
				return;
			}

			String line= ""; //$NON-NLS-1$
			if (lineLength > 0) {
				int end= lineOffset + lineLength - 1;
				end= Math.min(end, st.getCharCount() -1);
				line= st.getText(lineOffset, end);
			}

			// Remember current selection
			Point oldSelection= st.getSelection();

			// The new caret position
			int newCaretOffset= -1;

			if (isSmartHomeEndEnabled) {

				// Compute the line start offset
				int index= getLineStartPosition(document, line, lineLength, caretOffsetInDocument);

				if (caretOffset - lineOffset == index)
					// to beginning of line
					newCaretOffset= lineOffset;
				else
					// to beginning of text
					newCaretOffset= lineOffset + index;

			} else {

				if (caretOffset > lineOffset)
					// to beginning of line
					newCaretOffset= lineOffset;
			}

			if (newCaretOffset == -1)
				newCaretOffset= caretOffset;
			else
				st.setCaretOffset(newCaretOffset);

			if (fDoSelect) {
				if (caretOffset < oldSelection.y)
					st.setSelection(oldSelection.y, newCaretOffset);
				else
					st.setSelection(oldSelection.x, newCaretOffset);
			} else
				st.setSelection(newCaretOffset);

			fireSelectionChanged(oldSelection);
		}

	}

	/**
	 * Internal action to show the editor's ruler context menu (accessibility).
	 * @since 2.0
	 */
	class ShowRulerContextMenuAction extends Action {
		/*
		 * @see IAction#run()
		 */
		public void run() {
			if (fSourceViewer == null)
				return;

			StyledText text= fSourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			Point location= text.getLocationAtOffset(text.getCaretOffset());
			location.x= 0;

			if (fVerticalRuler instanceof IVerticalRulerExtension)
				((IVerticalRulerExtension) fVerticalRuler).setLocationOfLastMouseButtonActivity(location.x, location.y);

			location= text.toDisplay(location);
			fRulerContextMenu.setLocation(location.x, location.y);
			fRulerContextMenu.setVisible(true);
		}
	}

	/**
	 * Editor specific selection provider which wraps the source viewer's selection provider.
	 *
	 * @since 3.4 protected, was added in 2.1 as private class
	 */
	protected class SelectionProvider implements IPostSelectionProvider, ISelectionValidator {

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			if (fSourceViewer != null)
				fSourceViewer.getSelectionProvider().addSelectionChangedListener(listener);
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			return doGetSelection();
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			if (fSourceViewer != null)
				fSourceViewer.getSelectionProvider().removeSelectionChangedListener(listener);
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
		 */
		public void setSelection(ISelection selection) {
			doSetSelection(selection);
		}

		/*
		 * @see org.eclipse.jface.text.IPostSelectionProvider#addPostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 * @since 3.0
		 */
		public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
			if (fSourceViewer != null) {
				if (fSourceViewer.getSelectionProvider() instanceof IPostSelectionProvider)  {
					IPostSelectionProvider provider= (IPostSelectionProvider) fSourceViewer.getSelectionProvider();
					provider.addPostSelectionChangedListener(listener);
				}
			}
		}

		/*
		 * @see org.eclipse.jface.text.IPostSelectionProvider#removePostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 * @since 3.0
		 */
		public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
			if (fSourceViewer != null)  {
				if (fSourceViewer.getSelectionProvider() instanceof IPostSelectionProvider)  {
					IPostSelectionProvider provider= (IPostSelectionProvider) fSourceViewer.getSelectionProvider();
					provider.removePostSelectionChangedListener(listener);
				}
			}
		}

		/*
		 * @see org.eclipse.jface.text.IPostSelectionValidator#isValid()
		 * @since 3.0
		 */
		public boolean isValid(ISelection postSelection) {
			return fSelectionListener != null && fSelectionListener.isValid(postSelection);
		}
	}

	/**
	 * Internal implementation class for a change listener.
	 * @since 3.0
	 */
	protected abstract class AbstractSelectionChangedListener implements ISelectionChangedListener  {

		/**
		 * Installs this selection changed listener with the given selection provider. If the
		 * selection provider is a post selection provider, post selection changed events are the
		 * preferred choice, otherwise normal selection changed events are requested.
		 *
		 * @param selectionProvider the selection provider
		 */
		public void install(ISelectionProvider selectionProvider) {
			if (selectionProvider == null)
				return;

			if (selectionProvider instanceof IPostSelectionProvider)  {
				IPostSelectionProvider provider= (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else  {
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		/**
		 * Removes this selection changed listener from the given selection provider.
		 *
		 * @param selectionProvider the selection provider
		 */
		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider == null)
				return;

			if (selectionProvider instanceof IPostSelectionProvider)  {
				IPostSelectionProvider provider= (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			} else  {
				selectionProvider.removeSelectionChangedListener(this);
			}
		}
	}

	/**
	 * This selection listener allows the SelectionProvider to implement {@link ISelectionValidator}.
	 *
	 * @since 3.0
	 */
	private class SelectionListener extends AbstractSelectionChangedListener implements IDocumentListener {

		private IDocument fDocument;
		private final Object INVALID_SELECTION= new Object();
		private Object fPostSelection= INVALID_SELECTION;

		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public synchronized void selectionChanged(SelectionChangedEvent event) {
			fPostSelection= event.getSelection();
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 * @since 3.0
		 */
		public synchronized void documentAboutToBeChanged(DocumentEvent event) {
			fPostSelection= INVALID_SELECTION;
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 * @since 3.0
		 */
		public void documentChanged(DocumentEvent event) {
		}

		public synchronized boolean isValid(ISelection selection) {
			return fPostSelection != INVALID_SELECTION && fPostSelection == selection;
		}

		public void setDocument(IDocument document) {
			if (fDocument != null)
				fDocument.removeDocumentListener(this);

			fDocument= document;
			if (fDocument != null)
				fDocument.addDocumentListener(this);
		}

		/*
		 * @see org.eclipse.ui.texteditor.AbstractTextEditor.AbstractSelectionChangedListener#install(org.eclipse.jface.viewers.ISelectionProvider)
		 * @since 3.0
		 */
		public void install(ISelectionProvider selectionProvider) {
			super.install(selectionProvider);

			if (selectionProvider != null)
				selectionProvider.addSelectionChangedListener(this);
		}

		/*
		 * @see org.eclipse.ui.texteditor.AbstractTextEditor.AbstractSelectionChangedListener#uninstall(org.eclipse.jface.viewers.ISelectionProvider)
		 * @since 3.0
		 */
		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider != null)
				selectionProvider.removeSelectionChangedListener(this);

			if (fDocument != null) {
				fDocument.removeDocumentListener(this);
				fDocument= null;
			}
			super.uninstall(selectionProvider);
		}
	}


	/**
	 * Implements the ruler column support of for the given editor.
	 * <p>
	 * This is currently only used to support vertical ruler columns.
	 * </p>
	 *
	 * @since 3.3
	 */
	protected static class ColumnSupport implements IColumnSupport {
		private final AbstractTextEditor fEditor;
		private final RulerColumnRegistry fRegistry;
		private final List fColumns;

		/**
		 * Creates a new column support for the given editor. Only the editor itself should normally
		 * create such an instance.
		 *
		 * @param editor the editor
		 * @param registry the contribution registry to refer to
		 */
		public ColumnSupport(AbstractTextEditor editor, RulerColumnRegistry registry) {
			Assert.isLegal(editor != null);
			Assert.isLegal(registry != null);
			fEditor= editor;
			fRegistry= registry;
			fColumns= new ArrayList();
		}

		/*
		 * @see org.eclipse.ui.texteditor.IColumnSupport#setColumnVisible(java.lang.String, boolean)
		 */
		public final void setColumnVisible(RulerColumnDescriptor descriptor, boolean visible) {
			Assert.isLegal(descriptor != null);

			final CompositeRuler ruler= getRuler();
			if (ruler == null)
				return;

			if (!isColumnSupported(descriptor))
				visible= false;

			if (isColumnVisible(descriptor)) {
				if (!visible)
					removeColumn(ruler, descriptor);
			} else {
				if (visible)
					addColumn(ruler, descriptor);
			}
		}

		private void addColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor) {

			final int idx= computeIndex(ruler, descriptor);

			SafeRunnable runnable= new SafeRunnable() {
				public void run() throws Exception {
					IContributedRulerColumn column= descriptor.createColumn(fEditor);
					fColumns.add(column);
					initializeColumn(column);
					ruler.addDecorator(idx, column);
				}
			};
			SafeRunner.run(runnable);
		}

		/**
		 * Hook to let subclasses initialize a newly created column.
		 * <p>
		 * Subclasses may extend this method.</p>
		 *
		 * @param column the created column
		 */
		protected void initializeColumn(IContributedRulerColumn column) {
		}

		private void removeColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor) {
			removeColumn(ruler, getVisibleColumn(ruler, descriptor));
		}

		private void removeColumn(final CompositeRuler ruler, final IContributedRulerColumn rulerColumn) {
			if (rulerColumn != null) {
				SafeRunnable runnable= new SafeRunnable() {
					public void run() throws Exception {
						if (ruler != null)
							ruler.removeDecorator(rulerColumn);
						rulerColumn.columnRemoved();
					}
				};
				SafeRunner.run(runnable);
			}
		}

		/**
		 * Returns the currently visible column matching <code>id</code>, <code>null</code> if
		 * none.
		 *
		 * @param ruler the composite ruler to scan
		 * @param descriptor the descriptor of the column of interest
		 * @return the matching column or <code>null</code>
		 */
		private IContributedRulerColumn getVisibleColumn(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
			for (Iterator it= ruler.getDecoratorIterator(); it.hasNext();) {
				IVerticalRulerColumn column= (IVerticalRulerColumn)it.next();
				if (column instanceof IContributedRulerColumn) {
					IContributedRulerColumn rulerColumn= (IContributedRulerColumn)column;
					RulerColumnDescriptor rcd= rulerColumn.getDescriptor();
					if (descriptor.equals(rcd))
						return rulerColumn;
				}
			}
			return null;
		}

		/**
		 * Computes the insertion index for a column contribution into the currently visible columns.
		 *
		 * @param ruler the composite ruler into which to insert the column
		 * @param descriptor the descriptor to compute the index for
		 * @return the insertion index for a new column
		 */
		private int computeIndex(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
			int index= 0;
			List all= fRegistry.getColumnDescriptors();
			int newPos= all.indexOf(descriptor);
			for (Iterator it= ruler.getDecoratorIterator(); it.hasNext();) {
				IVerticalRulerColumn column= (IVerticalRulerColumn) it.next();
				if (column instanceof IContributedRulerColumn) {
					RulerColumnDescriptor rcd= ((IContributedRulerColumn)column).getDescriptor();
					if (rcd != null && all.indexOf(rcd) > newPos)
						break;
				} else if ("org.eclipse.jface.text.source.projection.ProjectionRulerColumn".equals(column.getClass().getName())) { //$NON-NLS-1$
					// projection column is always the rightmost column
					break;
				}
				index++;
			}
			return index;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IColumnSupport#isColumnVisible(java.lang.String)
		 */
		public final boolean isColumnVisible(RulerColumnDescriptor descriptor) {
			Assert.isLegal(descriptor != null);
			CompositeRuler ruler= getRuler();
			return ruler != null && getVisibleColumn(ruler, descriptor) != null;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IColumnSupport#isColumnSupported(java.lang.String)
		 */
		public final boolean isColumnSupported(RulerColumnDescriptor descriptor) {
			Assert.isLegal(descriptor != null);
			if (getRuler() == null)
				return false;

			return descriptor.matchesEditor(fEditor);
		}

		/**
		 * Returns the editor's vertical ruler, if it is a {@link CompositeRuler}, <code>null</code>
		 * otherwise.
		 *
		 * @return the editor's {@link CompositeRuler} or <code>null</code>
		 */
		private CompositeRuler getRuler() {
			Object ruler= fEditor.getAdapter(IVerticalRulerInfo.class);
			if (ruler instanceof CompositeRuler)
				return (CompositeRuler) ruler;
			return null;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Subclasses may extend this method.</p>
		 *
		 */
		public void dispose() {
			for (Iterator iter= new ArrayList(fColumns).iterator(); iter.hasNext();)
				removeColumn(getRuler(), (IContributedRulerColumn)iter.next());
			fColumns.clear();
		}
	}



	/**
	 * This action behaves in two different ways: If there is no current text
	 * hover, the javadoc is displayed using information presenter. If there is
	 * a current text hover, it is converted into a information presenter in
	 * order to make it sticky.
	 *
	 * @since 3.3
	 */
	private final class InformationDispatchAction extends TextEditorAction {

		/** The wrapped text operation action. */
		private final TextOperationAction fTextOperationAction;

		/**
		 * Creates a dispatch action.
		 *
		 * @param resourceBundle the resource bundle
		 * @param prefix the prefix
		 * @param textOperationAction the text operation action
		 */
		public InformationDispatchAction(ResourceBundle resourceBundle, String prefix, final TextOperationAction textOperationAction) {
			super(resourceBundle, prefix, AbstractTextEditor.this);
			if (textOperationAction == null)
				throw new IllegalArgumentException();
			fTextOperationAction= textOperationAction;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {

			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null) {
				if (fTextOperationAction.isEnabled())
					fTextOperationAction.run();
				return;
			}

			if (sourceViewer instanceof ITextViewerExtension4)  {
				ITextViewerExtension4 extension4= (ITextViewerExtension4) sourceViewer;
				if (extension4.moveFocusToWidgetToken())
					return;
			}

			if (sourceViewer instanceof ITextViewerExtension2) {
				// does a text hover exist?
				ITextHover textHover= ((ITextViewerExtension2) sourceViewer).getCurrentTextHover();
				if (textHover != null && makeTextHoverFocusable(sourceViewer, textHover))
					return;
			}

			if (sourceViewer instanceof ISourceViewerExtension3) {
				// does an annotation hover exist?
				IAnnotationHover annotationHover= ((ISourceViewerExtension3) sourceViewer).getCurrentAnnotationHover();
				if (annotationHover != null && makeAnnotationHoverFocusable(annotationHover))
					return;
			}

			// otherwise, just run the action
			if (fTextOperationAction.isEnabled())
				fTextOperationAction.run();
		}

		/**
		 * Tries to make a text hover focusable (or "sticky").
		 *
		 * @param sourceViewer the source viewer to display the hover over
		 * @param textHover the hover to make focusable
		 * @return <code>true</code> if successful, <code>false</code> otherwise
		 */
		private boolean makeTextHoverFocusable(ISourceViewer sourceViewer, ITextHover textHover) {
			Point hoverEventLocation= ((ITextViewerExtension2) sourceViewer).getHoverEventLocation();
			int offset= computeOffsetAtLocation(sourceViewer, hoverEventLocation.x, hoverEventLocation.y);
			if (offset == -1)
				return false;

			try {
				IRegion hoverRegion= textHover.getHoverRegion(sourceViewer, offset);
				if (hoverRegion == null)
					return false;

				String hoverInfo= textHover.getHoverInfo(sourceViewer, hoverRegion);

				IInformationControlCreator controlCreator= null;
				if (textHover instanceof IInformationProviderExtension2) // this is conceptually wrong, but left here for backwards compatibility
					controlCreator= ((IInformationProviderExtension2)textHover).getInformationPresenterControlCreator();

				IInformationProvider informationProvider= new FocusedInformationPresenter.InformationProvider(hoverRegion, hoverInfo, controlCreator);

				FocusedInformationPresenter informationPresenter= getInformationPresenter();
				informationPresenter.setOffset(offset);
				informationPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_BOTTOM);
				informationPresenter.setMargins(6, 6); // default values from AbstractInformationControlManager
				String contentType= TextUtilities.getContentType(sourceViewer.getDocument(), getSourceViewerConfiguration().getConfiguredDocumentPartitioning(getSourceViewer()), offset, true);
				informationPresenter.setInformationProvider(informationProvider, contentType);
				informationPresenter.showInformation();

				return true;

			} catch (BadLocationException e) {
				return false;
			}
		}

		/**
		 * Tries to make an annotation hover focusable (or "sticky").
		 *
		 * @param annotationHover the hover to make focusable
		 * @return <code>true</code> if successful, <code>false</code> otherwise
		 */
		private boolean makeAnnotationHoverFocusable(IAnnotationHover annotationHover) {
			IVerticalRulerInfo info= getVerticalRuler();
			int line= info.getLineOfLastMouseButtonActivity();
			if (line == -1)
				return false;

			return getInformationPresenter().openFocusedAnnotationHover(annotationHover, line);
        }
		
		/**
		 * Returns the information presenter (creates it if necessary).
		 * 
		 * @return the information presenter
		 * @since 3.6
		 */
		private FocusedInformationPresenter getInformationPresenter() {
			if (fInformationPresenter == null) {
				fInformationPresenter= new FocusedInformationPresenter(getSourceViewer(), getSourceViewerConfiguration());
			}
			return fInformationPresenter;
		}

		// modified version from TextViewer
		private int computeOffsetAtLocation(ITextViewer textViewer, int x, int y) {

			StyledText styledText= textViewer.getTextWidget();
			IDocument document= textViewer.getDocument();

			if (document == null)
				return -1;

			try {
				int widgetOffset= styledText.getOffsetAtLocation(new Point(x, y));
				Point p= styledText.getLocationAtOffset(widgetOffset);
				if (p.x > x)
					widgetOffset--;

				if (textViewer instanceof ITextViewerExtension5) {
					ITextViewerExtension5 extension= (ITextViewerExtension5) textViewer;
					return extension.widgetOffset2ModelOffset(widgetOffset);
				}
				IRegion visibleRegion= textViewer.getVisibleRegion();
				return widgetOffset + visibleRegion.getOffset();
			} catch (IllegalArgumentException e) {
				return -1;
			}
		}
	}

	/**
	 * Key used to look up font preference.
	 * Value: <code>"org.eclipse.jface.textfont"</code>
	 *
	 * @deprecated As of 2.1, replaced by {@link JFaceResources#TEXT_FONT}
	 */
	public static final String PREFERENCE_FONT= JFaceResources.TEXT_FONT;
	/**
	 * Key used to look up foreground color preference.
	 * Value: <code>AbstractTextEditor.Color.Foreground</code>
	 * @since 2.0
	 */
	public static final String PREFERENCE_COLOR_FOREGROUND= "AbstractTextEditor.Color.Foreground"; //$NON-NLS-1$
	/**
	 * Key used to look up background color preference.
	 * Value: <code>AbstractTextEditor.Color.Background</code>
	 * @since 2.0
	 */
	public static final String PREFERENCE_COLOR_BACKGROUND= "AbstractTextEditor.Color.Background"; //$NON-NLS-1$
	/**
	 * Key used to look up foreground color system default preference.
	 * Value: <code>AbstractTextEditor.Color.Foreground.SystemDefault</code>
	 * @since 2.0
	 */
	public static final String PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT= "AbstractTextEditor.Color.Foreground.SystemDefault"; //$NON-NLS-1$
	/**
	 * Key used to look up background color system default preference.
	 * Value: <code>AbstractTextEditor.Color.Background.SystemDefault</code>
	 * @since 2.0
	 */
	public static final String PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT= "AbstractTextEditor.Color.Background.SystemDefault"; //$NON-NLS-1$
	/**
	 * Key used to look up selection foreground color preference.
	 * Value: <code>AbstractTextEditor.Color.SelectionForeground</code>
	 * @since 3.0
	 */
	public static final String PREFERENCE_COLOR_SELECTION_FOREGROUND= "AbstractTextEditor.Color.SelectionForeground"; //$NON-NLS-1$
	/**
	 * Key used to look up selection background color preference.
	 * Value: <code>AbstractTextEditor.Color.SelectionBackground</code>
	 * @since 3.0
	 */
	public static final String PREFERENCE_COLOR_SELECTION_BACKGROUND= "AbstractTextEditor.Color.SelectionBackground"; //$NON-NLS-1$
	/**
	 * Key used to look up selection foreground color system default preference.
	 * Value: <code>AbstractTextEditor.Color.SelectionForeground.SystemDefault</code>
	 * @since 3.0
	 */
	public static final String PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT= "AbstractTextEditor.Color.SelectionForeground.SystemDefault"; //$NON-NLS-1$
	/**
	 * Key used to look up selection background color system default preference.
	 * Value: <code>AbstractTextEditor.Color.SelectionBackground.SystemDefault</code>
	 * @since 3.0
	 */
	public static final String PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT= "AbstractTextEditor.Color.SelectionBackground.SystemDefault"; //$NON-NLS-1$
	/**
	 * Key used to look up find scope background color preference.
	 * Value: <code>AbstractTextEditor.Color.FindScope</code>
	 * @since 2.0
	 */
	public static final String PREFERENCE_COLOR_FIND_SCOPE= "AbstractTextEditor.Color.FindScope"; //$NON-NLS-1$
	/**
	 * Key used to look up smart home/end preference.
	 * Value: <code>AbstractTextEditor.Navigation.SmartHomeEnd</code>
	 * @since 2.1
	 */
	public static final String PREFERENCE_NAVIGATION_SMART_HOME_END= "AbstractTextEditor.Navigation.SmartHomeEnd"; //$NON-NLS-1$
	/**
	 * Key used to look up the custom caret preference.
	 * Value: {@value}
	 * @since 3.0
	 */
	public static final String PREFERENCE_USE_CUSTOM_CARETS= "AbstractTextEditor.Accessibility.UseCustomCarets"; //$NON-NLS-1$
	/**
	 * Key used to look up the caret width preference.
	 * Value: {@value}
	 * @since 3.0
	 */
	public static final String PREFERENCE_WIDE_CARET= "AbstractTextEditor.Accessibility.WideCaret"; //$NON-NLS-1$
	/**
	 * A named preference that controls if hyperlinks are turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	public static final String PREFERENCE_HYPERLINKS_ENABLED= "hyperlinksEnabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls the key modifier for hyperlinks.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	public static final String PREFERENCE_HYPERLINK_KEY_MODIFIER= "hyperlinkKeyModifier"; //$NON-NLS-1$
	/**
	 * A named preference that controls the key modifier mask for hyperlinks.
	 * The value is only used if the value of <code>PREFERENCE_HYPERLINK_KEY_MODIFIER</code>
	 * cannot be resolved to valid SWT modifier bits.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 *
	 * @see #PREFERENCE_HYPERLINK_KEY_MODIFIER
	 * @since 3.1
	 */
	public static final String PREFERENCE_HYPERLINK_KEY_MODIFIER_MASK= "hyperlinkKeyModifierMask"; //$NON-NLS-1$
	/**
	 * A named preference that controls the visible ruler column contributions.
	 * <p>
	 * Value is of type <code>String</code> and should be read using a {@link RulerColumnPreferenceAdapter}.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String PREFERENCE_RULER_CONTRIBUTIONS= "rulerContributions"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls the display of whitespace characters.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * <p>
	 * The following preferences can be used for fine-grained configuration when enabled.
	 * <ul>
	 * <li>{@link #PREFERENCE_SHOW_LEADING_SPACES}</li>
	 * <li>{@link #PREFERENCE_SHOW_ENCLOSED_SPACES}</li>
	 * <li>{@link #PREFERENCE_SHOW_TRAILING_SPACES}</li>
	 * <li>{@link #PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES}</li>
	 * <li>{@link #PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES}</li>
	 * <li>{@link #PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES}</li>
	 * <li>{@link #PREFERENCE_SHOW_LEADING_TABS}</li>
	 * <li>{@link #PREFERENCE_SHOW_ENCLOSED_TABS}</li>
	 * <li>{@link #PREFERENCE_SHOW_TRAILING_TABS}</li>
	 * <li>{@link #PREFERENCE_SHOW_CARRIAGE_RETURN}</li>
	 * <li>{@link #PREFERENCE_SHOW_LINE_FEED}</li>
	 * <li>{@link #PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE}</li>
	 * </ul>
	 * </p>
	 * 
	 * @since 3.3
	 */
	public static final String PREFERENCE_SHOW_WHITESPACE_CHARACTERS= "showWhitespaceCharacters"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of leading Space characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_LEADING_SPACES= "showLeadingSpaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of enclosed Space characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_ENCLOSED_SPACES= "showEnclosedSpaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of trailing Space characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_TRAILING_SPACES= "showTrailingSpaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of leading Ideographic Space characters. The
	 * value is used only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES= "showLeadingIdeographicSpaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of enclosed Ideographic Space characters. The
	 * value is used only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES= "showEnclosedIdeographicSpaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of trailing Ideographic Space characters. The
	 * value is used only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES= "showTrailingIdeographicSpaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of leading Tab characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_LEADING_TABS= "showLeadingTabs"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of enclosed Tab characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_ENCLOSED_TABS= "showEnclosedTabs"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of trailing Tab characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_TRAILING_TABS= "showTrailingTabs"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of Carriage Return characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_CARRIAGE_RETURN= "showCarriageReturn"; //$NON-NLS-1$

	/**
	 * A named preference that controls the display of Line Feed characters. The value is used only
	 * if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_SHOW_LINE_FEED= "showLineFeed"; //$NON-NLS-1$

	/**
	 * A named preference that controls the alpha value of whitespace characters. The value is used
	 * only if the value of {@link #PREFERENCE_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE= "whitespaceCharacterAlphaValue"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether text drag and drop is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String PREFERENCE_TEXT_DRAG_AND_DROP_ENABLED= "textDragAndDropEnabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls if hovers should automatically be closed
	 * when the mouse is moved into them, or when they should be enriched.
	 * <p>
	 * Value is of type <code>Integer</code> and maps to the following
	 * {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode}:
	 * </p>
	 * <ul>
	 * <li>-1: <code>null</code> (don't allow moving the mouse into a hover),</li>
	 * <li>0: {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode#AFTER_DELAY},</li>
	 * <li>1: {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode#IMMEDIATELY},</li>
	 * <li>2: {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode#ON_CLICK}.</li>
	 * </ul>
	 *
	 * @since 3.4
	 */
	public static final String PREFERENCE_HOVER_ENRICH_MODE= "hoverReplaceMode"; //$NON-NLS-1$

	/** Menu id for the editor context menu. */
	public static final String DEFAULT_EDITOR_CONTEXT_MENU_ID= "#EditorContext"; //$NON-NLS-1$
	/** Menu id for the ruler context menu. */
	public static final String DEFAULT_RULER_CONTEXT_MENU_ID= "#RulerContext"; //$NON-NLS-1$

	/**
	 * Menu id used to contribute to the editor context menu of all textual editors.
	 * 
	 * @since 3.5
	 */
	public static final String COMMON_EDITOR_CONTEXT_MENU_ID= "#AbstractTextEditorContext"; //$NON-NLS-1$

	/**
	 * Menu id used to contribute to the ruler context menu of all textual editors.
	 * 
	 * @since 3.5
	 */
	public static final String COMMON_RULER_CONTEXT_MENU_ID= "#AbstractTextEditorRulerContext"; //$NON-NLS-1$

	/** The width of the vertical ruler. */
	protected static final int VERTICAL_RULER_WIDTH= 12;

	/**
	 * The complete mapping between action definition IDs used by eclipse and StyledText actions.
	 *
	 * @since 2.0
	 */
	protected static final IdMapEntry[] ACTION_MAP= new IdMapEntry[] {
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
		new IdMapEntry(IWorkbenchCommandConstants.EDIT_CUT, ST.CUT),
		new IdMapEntry(IWorkbenchCommandConstants.EDIT_COPY, ST.COPY),
		new IdMapEntry(IWorkbenchCommandConstants.EDIT_PASTE, ST.PASTE),
		new IdMapEntry(ITextEditorActionDefinitionIds.DELETE_PREVIOUS, ST.DELETE_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.DELETE_NEXT, ST.DELETE_NEXT),
		new IdMapEntry(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, ST.DELETE_WORD_PREVIOUS),
		new IdMapEntry(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, ST.DELETE_WORD_NEXT),
		// miscellaneous
		new IdMapEntry(ITextEditorActionDefinitionIds.TOGGLE_OVERWRITE, ST.TOGGLE_OVERWRITE)
	};

	private final String fReadOnlyLabel= EditorMessages.Editor_statusline_state_readonly_label;
	private final String fWritableLabel= EditorMessages.Editor_statusline_state_writable_label;
	private final String fInsertModeLabel= EditorMessages.Editor_statusline_mode_insert_label;
	private final String fOverwriteModeLabel= EditorMessages.Editor_statusline_mode_overwrite_label;
	private final String fSmartInsertModeLabel= EditorMessages.Editor_statusline_mode_smartinsert_label;

	/** The error message shown in the status line in case of failed information look up. */
	protected final String fErrorLabel= EditorMessages.Editor_statusline_error_label;

	/**
	 * Data structure for the position label value.
	 */
	private static class PositionLabelValue {

		public int fValue;

		public String toString() {
			return String.valueOf(fValue);
		}
	}
	/** The pattern used to show the position label in the status line. */
	private final String fPositionLabelPattern= EditorMessages.Editor_statusline_position_pattern;
	/** The position label value of the current line. */
	private final PositionLabelValue fLineLabel= new PositionLabelValue();
	/** The position label value of the current column. */
	private final PositionLabelValue fColumnLabel= new PositionLabelValue();
	/** The arguments for the position label pattern. */
	private final Object[] fPositionLabelPatternArguments= new Object[] { fLineLabel, fColumnLabel };
	/**
	 * The column support of this editor.
	 * @since 3.3
	 */
	private IColumnSupport fColumnSupport;

	/** The editor's explicit document provider. */
	private IDocumentProvider fExplicitDocumentProvider;
	/** The editor's preference store. */
	private IPreferenceStore fPreferenceStore;
	/** The editor's range indicator. */
	private Annotation fRangeIndicator;
	/** The editor's source viewer configuration. */
	private SourceViewerConfiguration fConfiguration;
	/** The editor's source viewer. */
	private ISourceViewer fSourceViewer;
	/**
	 * The editor's selection provider.
	 * @since 2.1
	 */
	private SelectionProvider fSelectionProvider= new SelectionProvider();
	/**
	 * The editor's selection listener.
	 * @since 3.0
	 */
	private SelectionListener fSelectionListener;
	/** The editor's font. */
	private Font fFont;	/**
	 * The editor's foreground color.
	 * @since 2.0
	 */
	private Color fForegroundColor;
	/**
	 * The editor's background color.
	 * @since 2.0
	 */
	private Color fBackgroundColor;
	/**
	 * The editor's selection foreground color.
	 * @since 3.0
	 */
	private Color fSelectionForegroundColor;
	/**
	 * The editor's selection background color.
	 * @since 3.0
	 */
	private Color fSelectionBackgroundColor;
	/**
	 * The find scope's highlight color.
	 * @since 2.0
	 */
	private Color fFindScopeHighlightColor;

	/**
	 * The editor's status line.
	 * @since 2.1
	 */
	private IEditorStatusLine fEditorStatusLine;
	/** The editor's vertical ruler. */
	private IVerticalRuler fVerticalRuler;
	/** The editor's context menu id. */
	private String fEditorContextMenuId;
	/** The ruler's context menu id. */
	private String fRulerContextMenuId;
	/** The editor's help context id. */
	private String fHelpContextId;
	/** The editor's presentation mode. */
	private boolean fShowHighlightRangeOnly;
	/** The actions registered with the editor. */
	private Map fActions= new HashMap(10);
	/** The actions marked as selection dependent. */
	private List fSelectionActions= new ArrayList(5);
	/** The actions marked as content dependent. */
	private List fContentActions= new ArrayList(5);
	/**
	 * The actions marked as property dependent.
	 * @since 2.0
	 */
	private List fPropertyActions= new ArrayList(5);
	/**
	 * The actions marked as state dependent.
	 * @since 2.0
	 */
	private List fStateActions= new ArrayList(5);
	/** The editor's action activation codes. */
	private List fActivationCodes= new ArrayList(2);
	/** The verify key listener for activation code triggering. */
	private ActivationCodeTrigger fActivationCodeTrigger= new ActivationCodeTrigger();
	/** Context menu listener. */
	private IMenuListener fMenuListener;
	/** Vertical ruler mouse listener. */
	private MouseListener fMouseListener;
	/** Selection changed listener. */
	private ISelectionChangedListener fSelectionChangedListener;
	/** Title image to be disposed. */
	private Image fTitleImage;
	/** The text context menu to be disposed. */
	private Menu fTextContextMenu;
	/** The ruler context menu to be disposed. */
	private Menu fRulerContextMenu;
	/** The editor's element state listener. */
	private IElementStateListener fElementStateListener= new ElementStateListener();
	/**
	 * The editor's text input listener.
	 * @since 2.1
	 */
	private TextInputListener fTextInputListener= new TextInputListener();
	/** The editor's text listener. */
	private TextListener fTextListener= new TextListener();
	/** The editor's property change listener. */
	private IPropertyChangeListener fPropertyChangeListener= new PropertyChangeListener();
	/**
	 * The editor's font properties change listener.
	 * @since 2.1
	 */
	private IPropertyChangeListener fFontPropertyChangeListener= new FontPropertyChangeListener();

	/**
	 * The editor's activation listener.
	 * @since 2.0
	 */
	private ActivationListener fActivationListener;
	/**
	 * The map of the editor's status fields.
	 * @since 2.0
	 */
	private Map fStatusFields;
	/**
	 * The editor's cursor listener.
	 * @since 2.0
	 */
	private ICursorListener fCursorListener;
	/**
	 * The editor's remembered text selection.
	 * @since 2.0
	 */
	private ISelection fRememberedSelection;
	/**
	 * Indicates whether the editor runs in 1.0 context menu registration compatibility mode.
	 * @since 2.0
	 */
	private boolean fCompatibilityMode= true;
	/**
	 * The number of re-entrances into error correction code while saving.
	 * @since 2.0
	 */
	private int fErrorCorrectionOnSave;
	/**
	 * The delete line target.
	 * @since 2.1
	 */
	private IDeleteLineTarget fDeleteLineTarget;
	/**
	 * The incremental find target.
	 * @since 2.0
	 */
	private IncrementalFindTarget fIncrementalFindTarget;
	/**
	 * The mark region target.
	 * @since 2.0
	 */
	private IMarkRegionTarget fMarkRegionTarget;
	/**
	 * Cached modification stamp of the editor's input.
	 * @since 2.0
	 */
	private long fModificationStamp= -1;
	/**
	 * Ruler context menu listeners.
	 * @since 2.0
	 */
	private List fRulerContextMenuListeners= new ArrayList();
	/**
	 * Indicates whether sanity checking in enabled.
	 * @since 2.0
	 */
	private boolean fIsSanityCheckEnabled= true;
	/**
	 * The find replace target.
	 * @since 2.1
	 */
	private FindReplaceTarget fFindReplaceTarget;
	/**
	 * Indicates whether state validation is enabled.
	 * @since 2.1
	 */
	private boolean fIsStateValidationEnabled= true;
	/**
	 * The key binding scopes of this editor.
	 * @since 2.1
	 */
	private String[] fKeyBindingScopes;
	/**
	 * Whether the overwrite mode can be turned on.
	 * @since 3.0
	 */
	private boolean fIsOverwriteModeEnabled= true;
	/**
	 * Whether the overwrite mode is currently on.
	 * @since 3.0
	 */
	private boolean fIsOverwriting= false;
	/**
	 * The editor's insert mode.
	 * @since 3.0
	 */
	private InsertMode fInsertMode= SMART_INSERT;
	/**
	 * The sequence of legal editor insert modes.
	 * @since 3.0
	 */
	private List fLegalInsertModes= null;
	/**
	 * The non-default caret.
	 * @since 3.0
	 */
	private Caret fNonDefaultCaret;
	/**
	 * The image used in non-default caret.
	 * @since 3.0
	 */
	private Image fNonDefaultCaretImage;
	/**
	 * The styled text's initial caret.
	 * @since 3.0
	 */
	private Caret fInitialCaret;
	/**
	 * The operation approver used to warn on undoing of non-local operations.
	 * @since 3.1
	 */
	private IOperationApprover fNonLocalOperationApprover;
	/**
	 * The operation approver used to warn of linear undo violations.
	 * @since 3.1
	 */
	private IOperationApprover fLinearUndoViolationApprover;
	/**
	 * This editor's memento holding data for restoring it after restart.
	 * @since 3.3
	 */
	private IMemento fMementoToRestore;
	/**
	 * This editor's savable.
	 * @since 3.3
	 */
	private TextEditorSavable fSavable;
	/**
	 * Tells whether text drag and drop has been installed on the control.
	 * @since 3.3
	 */
	private boolean fIsTextDragAndDropInstalled= false;
	/**
	 * Helper token to decide whether drag and
	 * drop happens inside the same editor.
	 * @since 3.3
	 */
	private Object fTextDragAndDropToken;
	/**
	 * The information presenter, may be <code>null</code>.
	 * @since 3.3
	 */
	private FocusedInformationPresenter fInformationPresenter;

	/**
	 * Tells whether this editor has been activated at least once.
	 * @since 3.3.2
	 */
	private boolean fHasBeenActivated= false;
	/**
	 * Key binding support for the quick assist assistant.
	 * @since 3.4
	 */
	private KeyBindingSupportForAssistant fKeyBindingSupportForQuickAssistant;

	/**
	 * Key binding support for the quick assist assistant.
	 * @since 3.5
	 */
	private KeyBindingSupportForAssistant fKeyBindingSupportForContentAssistant;

	/**
	 * The save action.
	 * @since 3.6.1
	 */
	private IWorkbenchAction fSaveAction;


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
	 * @see ITextEditor#getDocumentProvider()
	 */
	public IDocumentProvider getDocumentProvider() {
		return fExplicitDocumentProvider;
	}

	/**
	 * Returns the editor's range indicator. May return <code>null</code> if no
	 * range indicator is installed.
	 *
	 * @return the editor's range indicator which may be <code>null</code>
	 */
	protected final Annotation getRangeIndicator() {
		return fRangeIndicator;
	}

	/**
	 * Returns the editor's source viewer configuration. May return <code>null</code>
	 * before the editor's part has been created and after disposal.
	 *
	 * @return the editor's source viewer configuration which may be <code>null</code>
	 */
	protected final SourceViewerConfiguration getSourceViewerConfiguration() {
		return fConfiguration;
	}

	/**
	 * Returns the editor's source viewer. May return <code>null</code> before
	 * the editor's part has been created and after disposal.
	 *
	 * @return the editor's source viewer which may be <code>null</code>
	 */
	protected final ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}

	/**
	 * Returns the editor's vertical ruler. May return <code>null</code> before
	 * the editor's part has been created and after disposal.
	 *
	 * @return the editor's vertical ruler which may be <code>null</code>
	 */
	protected final IVerticalRuler getVerticalRuler() {
		return fVerticalRuler;
	}

	/**
	 * Returns the editor's context menu id. May return <code>null</code> before
	 * the editor's part has been created.
	 *
	 * @return the editor's context menu id which may be <code>null</code>
	 */
	protected final String getEditorContextMenuId() {
		return fEditorContextMenuId;
	}

	/**
	 * Returns the ruler's context menu id. May return <code>null</code> before
	 * the editor's part has been created.
	 *
	 * @return the ruler's context menu id which may be <code>null</code>
	 */
	protected final String getRulerContextMenuId() {
		return fRulerContextMenuId;
	}

	/**
	 * Returns the editor's help context id or <code>null</code> if none has
	 * been set.
	 *
	 * @return the editor's help context id which may be <code>null</code>
	 */
	protected final String getHelpContextId() {
		return fHelpContextId;
	}

	/**
	 * Returns this editor's preference store or <code>null</code> if none has
	 * been set.
	 *
	 * @return this editor's preference store which may be <code>null</code>
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
		fExplicitDocumentProvider= provider;
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
	 * range indicator is not set, this editor will not show a range indication.
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
	 * @since 2.0
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
	 * Sets the key binding scopes for this editor.
	 *
	 * @param scopes a non-empty array of key binding scope identifiers
	 * @since 2.1
	 */
	protected void setKeyBindingScopes(String[] scopes) {
		Assert.isTrue(scopes != null && scopes.length > 0);
		fKeyBindingScopes= scopes;
	}

	/**
	 * Sets this editor's preference store. This method must be
	 * called before the editor's control is created.
	 *
	 * @param store the preference store or <code>null</code> to remove the
	 * 		  preference store
	 */
	protected void setPreferenceStore(IPreferenceStore store) {
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
			fPreferenceStore.removePropertyChangeListener(fFontPropertyChangeListener);
		}

		fPreferenceStore= store;

		if (fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);
			fPreferenceStore.addPropertyChangeListener(fFontPropertyChangeListener);
		}
	}

	/*
	 * @see ITextEditor#isEditable()
	 */
	public boolean isEditable() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			return extension.isModifiable(getEditorInput());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns <code>null</code> after disposal.
	 * </p>
	 *
	 * @return the selection provider or <code>null</code> if the editor has
	 *         been disposed
	 */
	public ISelectionProvider getSelectionProvider() {
		return fSelectionProvider;
	}

	/**
	 * Remembers the current selection of this editor. This method is called when, e.g.,
	 * the content of the editor is about to be reverted to the saved state. This method
	 * remembers the selection in a semantic format, i.e., in a format which allows to
	 * restore the selection even if the originally selected text is no longer part of the
	 * editor's content.
	 * <p>
	 * Subclasses should implement this method including all necessary state. This
	 * default implementation remembers the textual range only and is thus purely
	 * syntactic.</p>
	 *
	 * @see #restoreSelection()
	 * @since 2.0
	 */
	protected void rememberSelection() {
		fRememberedSelection= doGetSelection();
	}

	/**
	 * Returns the current selection.
	 * @return ISelection
	 * @since 2.1
	 */
	protected ISelection doGetSelection() {
		ISelectionProvider sp= null;
		if (fSourceViewer != null)
			sp= fSourceViewer.getSelectionProvider();
		return (sp == null ? null : sp.getSelection());
	}

	/**
	 * Restores a selection previously remembered by <code>rememberSelection</code>.
	 * Subclasses may reimplement this method and thereby semantically adapt the
	 * remembered selection. This default implementation just selects the
	 * remembered textual range.
	 *
	 * @see #rememberSelection()
	 * @since 2.0
	 */
	protected void restoreSelection() {
		if (fRememberedSelection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)fRememberedSelection;
			if (isValidSelection(textSelection.getOffset(), textSelection.getLength()))
				doSetSelection(fRememberedSelection);
		}
		fRememberedSelection= null;
	}

	/**
	 * Tells whether the given selection is valid.
	 *
	 * @param offset the offset of the selection
	 * @param length the length of the selection
	 * @return <code>true</code> if the selection is valid
	 * @since 2.1
	 */
	private boolean isValidSelection(int offset, int length) {
		IDocumentProvider provider= getDocumentProvider();
		if (provider != null) {
			IDocument document= provider.getDocument(getEditorInput());
			if (document != null) {
				int end= offset + length;
				int documentLength= document.getLength();
				return 0 <= offset  && offset <= documentLength && 0 <= end && end <= documentLength && length >= 0;
			}
		}
		return false;
	}

	/**
	 * Sets the given selection.
	 *
	 * @param selection the selection
	 * @since 2.1
	 */
	protected void doSetSelection(ISelection selection) {
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) selection;
			selectAndReveal(textSelection.getOffset(), textSelection.getLength());
		}
	}

	/**
	 * Creates the listener on this editor's context menus.
	 *
	 * @return the created menu listener
	 * @since 3.4
	 */
	protected IMenuListener createContextMenuListener() {
		return new IMenuListener() {
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

	/**
	 * Creates and returns the listener on this editor's context menus.
	 *
	 * @return the menu listener
	 */
	protected final IMenuListener getContextMenuListener() {
		if (fMenuListener == null)
			fMenuListener= createContextMenuListener();
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
				private final int fDoubleClickTime= getSite().getShell().getDisplay().getDoubleClickTime();
				private long fMouseUpDelta= 0;

				private void triggerAction(String actionID, MouseEvent e) {
					IAction action= getAction(actionID);
					if (action != null) {
						if (action instanceof IUpdate)
							((IUpdate) action).update();
						if (action.isEnabled()) {
							Event event= new Event();
							event.type= fDoubleClicked ? SWT.MouseDoubleClick : SWT.MouseUp;
							event.display= e.display;
							event.widget= e.widget;
							event.time= e.time;
							event.data= e.data;
							event.x= e.x;
							event.y= e.y;
							event.button= e.button;
							event.stateMask= e.stateMask;
							event.count= e.count;
							action.runWithEvent(event);
						}
					}
				}

				public void mouseUp(final MouseEvent e) {
					setFocus();
					final int delay= fMouseUpDelta == 0 ? 0 : fDoubleClickTime - (int)(System.currentTimeMillis() - fMouseUpDelta);
					if (1 != e.button)
						return;

					Runnable runnable= new Runnable() {
						public void run() {
							if (!fDoubleClicked)
								triggerAction(ITextEditorActionConstants.RULER_CLICK, e);
						}
					};
					if (delay <= 0)
						runnable.run();
					else
						e.widget.getDisplay().timerExec(delay, runnable);
				}

				public void mouseDoubleClick(MouseEvent e) {
					if (1 == e.button) {
						fDoubleClicked= true;
						triggerAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, e);
					}
				}

				public void mouseDown(MouseEvent e) {
					fMouseUpDelta= System.currentTimeMillis();
					fDoubleClicked= false;
					if (fRulerContextMenu != null && !fRulerContextMenu.isDisposed()) {
						Display display= fRulerContextMenu.getDisplay();
						Point location= display.getCursorLocation();
						fRulerContextMenu.setLocation(location.x, location.y);
					}
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
						if (fSourceViewer != null && fSourceViewer.getDocument() != null) {
							updateSelectionDependentActions();
						}
					}
				};

				private Display fDisplay;

				public void selectionChanged(SelectionChangedEvent event) {
					if (fDisplay == null)
						fDisplay= getSite().getShell().getDisplay();
					fDisplay.asyncExec(fRunnable);
					handleCursorPositionChanged();
				}
			};
		}

		return fSelectionChangedListener;
	}

	/**
	 * Returns this editor's "cursor" listener to be installed on the editor's
	 * source viewer. This listener is listening to key and mouse button events.
	 * It triggers the updating of the status line by calling
	 * <code>handleCursorPositionChanged()</code>.
	 *
	 * @return the listener
	 * @since 2.0
	 */
	protected final ICursorListener getCursorListener() {
		if (fCursorListener == null) {
			fCursorListener= new ICursorListener() {

				public void keyPressed(KeyEvent e) {
					handleCursorPositionChanged();
				}

				public void keyReleased(KeyEvent e) {
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

	/**
	 * Implements the <code>init</code> method of <code>IEditorPart</code>.
	 * Subclasses replacing <code>init</code> may choose to call this method in
	 * their implementation.
	 *
	 * @param window the workbench window
	 * @param site the editor's site
	 * @param input the editor input for the editor being created
	 * @throws PartInitException if {@link #doSetInput(IEditorInput)} fails or gets canceled
	 *
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 * @since 2.1
	 */
	protected final void internalInit(IWorkbenchWindow window, final IEditorSite site, final IEditorInput input) throws PartInitException {

		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {

					if (getDocumentProvider() instanceof IDocumentProviderExtension2) {
						IDocumentProviderExtension2 extension= (IDocumentProviderExtension2) getDocumentProvider();
						extension.setProgressMonitor(monitor);
					}

					doSetInput(input);

				} catch (CoreException x) {
					throw new InvocationTargetException(x);
				} finally {
					if (getDocumentProvider() instanceof IDocumentProviderExtension2) {
						IDocumentProviderExtension2 extension= (IDocumentProviderExtension2) getDocumentProvider();
						extension.setProgressMonitor(null);
					}
				}
			}
		};

		try {
//			When using the progress service always a modal dialog pops up. The site should be asked for a runnable context
//			which could be the workbench window or the progress service, depending on what the site represents.
//			getSite().getWorkbenchWindow().getWorkbench().getProgressService().run(false, true, runnable);

			getSite().getWorkbenchWindow().run(false, true, runnable);

		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			Throwable t= x.getTargetException();
			if (t instanceof CoreException) {
                /*
                /* XXX: Remove unpacking of CoreException once the following bug is
                 *		fixed: https://bugs.eclipse.org/bugs/show_bug.cgi?id=81640
                 */
                CoreException e= (CoreException)t;
                IStatus status= e.getStatus();
                if (status.getException() != null)
                    throw new PartInitException(status);
               	throw new PartInitException(new Status(status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), t));
            }
			throw new PartInitException(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, EditorMessages.Editor_error_init, t));
		}
	}

	/*
	 * @see IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {

		setSite(site);

		internalInit(site.getWorkbenchWindow(), site, input);
		fActivationListener= new ActivationListener(site.getWorkbenchWindow().getPartService());
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
	 * Adds enabled ruler contributions to the vertical ruler.
	 * <p>
	 * Clients may extend or replace.</p>
	 *
	 * @param ruler the composite ruler to add contributions to
	 * @since 3.3
	 */
	protected void updateContributedRulerColumns(CompositeRuler ruler) {
		IColumnSupport support= (IColumnSupport)getAdapter(IColumnSupport.class);
		if (support == null)
			return;

		RulerColumnPreferenceAdapter adapter= null;
		if (fPreferenceStore != null)
			adapter= new RulerColumnPreferenceAdapter(getPreferenceStore(), PREFERENCE_RULER_CONTRIBUTIONS);

		RulerColumnRegistry registry= RulerColumnRegistry.getDefault();
		List descriptors= registry.getColumnDescriptors();
		for (Iterator it= descriptors.iterator(); it.hasNext();) {
			final RulerColumnDescriptor descriptor= (RulerColumnDescriptor) it.next();
			support.setColumnVisible(descriptor, adapter == null || adapter.isEnabled(descriptor));
		}
	}

	/**
	 * Creates the column support to be used by this editor to manage the
	 * contributed ruler columns.
	 * Subclasses may re-implement this method using the {@link ColumnSupport},
	 * e.g. by returning <code>new ColumnSupport(this, RulerColumnRegistry.getDefault());</code>.
	 * <p>
	 * <strong>Note:</strong> If you override this method to provide column support you will
	 * also need to override {@link #createVerticalRuler()} to return a {@link CompositeRuler}.</p>
	 * <p>
	 * Out of the box this class does not install this support and hence this
	 * implementation always returns <code>null</code>.</p>
	 *
	 * @return the column support or <code>null</code> if none
	 * @since 3.3
	 */
	protected IColumnSupport createColumnSupport() {
		return null;
	}

	/**
	 * Creates the source viewer to be used by this editor.
	 * Subclasses may re-implement this method.
	 *
	 * @param parent the parent control
	 * @param ruler the vertical ruler
	 * @param styles style bits, <code>SWT.WRAP</code> is currently not supported
	 * @return the source viewer
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		return new SourceViewer(parent, ruler, styles);
	}

	/**
	 * Initializes the drag and drop support for the given viewer based on
	 * provided editor adapter for drop target listeners.
	 *
	 * @param viewer the viewer
	 * @since 3.0
	 */
	protected void initializeDragAndDrop(ISourceViewer viewer) {
		IDragAndDropService dndService= (IDragAndDropService)getSite().getService(IDragAndDropService.class);
		if (dndService == null)
			return;

		ITextEditorDropTargetListener listener= (ITextEditorDropTargetListener) getAdapter(ITextEditorDropTargetListener.class);

		if (listener == null) {
			Object object= Platform.getAdapterManager().loadAdapter(this, "org.eclipse.ui.texteditor.ITextEditorDropTargetListener"); //$NON-NLS-1$
			if (object instanceof ITextEditorDropTargetListener)
				listener= (ITextEditorDropTargetListener)object;
		}

		if (listener != null)
			dndService.addMergedDropTarget(viewer.getTextWidget(), DND.DROP_MOVE | DND.DROP_COPY, listener.getTransfers(), listener);

		IPreferenceStore store= getPreferenceStore();
		if (store != null && store.getBoolean(PREFERENCE_TEXT_DRAG_AND_DROP_ENABLED))
			installTextDragAndDrop(viewer);

	}

	/**
	 * The <code>AbstractTextEditor</code> implementation of this
	 * <code>IWorkbenchPart</code> method creates the vertical ruler and
	 * source viewer.
	 * <p>
	 * Subclasses may extend this method. Besides extending this method, the
	 * behavior of <code>createPartControl</code> may be customized by
	 * calling, extending or replacing the following methods: <br>
	 * Subclasses may supply customized implementations for some members using
	 * the following methods before <code>createPartControl</code> is invoked:
	 * <ul>
	 * <li>
	 * {@linkplain #setSourceViewerConfiguration(SourceViewerConfiguration) setSourceViewerConfiguration}
	 * to supply a custom source viewer configuration,</li>
	 * <li>{@linkplain #setRangeIndicator(Annotation) setRangeIndicator} to
	 * provide a range indicator,</li>
	 * <li>{@linkplain #setHelpContextId(String) setHelpContextId} to provide a
	 * help context id,</li>
	 * <li>{@linkplain #setEditorContextMenuId(String) setEditorContextMenuId}
	 * to set a custom context menu id,</li>
	 * <li>{@linkplain #setRulerContextMenuId(String) setRulerContextMenuId} to
	 * set a custom ruler context menu id.</li>
	 * </ul>
	 * <br>
	 * Subclasses may replace the following methods called from within
	 * <code>createPartControl</code>:
	 * <ul>
	 * <li>{@linkplain #createVerticalRuler() createVerticalRuler} to supply a
	 * custom vertical ruler,</li>
	 * <li>{@linkplain #createSourceViewer(Composite, IVerticalRuler, int) createSourceViewer}
	 * to supply a custom source viewer,</li>
	 * <li>{@linkplain #getSelectionProvider() getSelectionProvider} to supply
	 * a custom selection provider.</li>
	 * </ul>
	 * <br>
	 * Subclasses may extend the following methods called from within
	 * <code>createPartControl</code>:
	 * <ul>
	 * <li>
	 * {@linkplain #initializeViewerColors(ISourceViewer) initializeViewerColors}
	 * to customize the viewer color scheme (may also be replaced),</li>
	 * <li>
	 * {@linkplain #initializeDragAndDrop(ISourceViewer) initializeDragAndDrop}
	 * to customize drag and drop (may also be replaced),</li>
	 * <li>{@linkplain #createNavigationActions() createNavigationActions} to
	 * add navigation actions,</li>
	 * <li>{@linkplain #createActions() createActions} to add text editor
	 * actions.</li>
	 * </ul>
	 * </p>
	 *
	 * @param parent the parent composite
	 */
	public void createPartControl(Composite parent) {

		fVerticalRuler= createVerticalRuler();

		int styles= SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
		fSourceViewer= createSourceViewer(parent, fVerticalRuler, styles);

		if (fConfiguration == null)
			fConfiguration= new SourceViewerConfiguration();
		fSourceViewer.configure(fConfiguration);

		if (fSourceViewer instanceof ISourceViewerExtension4)
			fKeyBindingSupportForContentAssistant= new KeyBindingSupportForAssistant(((ISourceViewerExtension4)fSourceViewer));

		if (fSourceViewer instanceof ISourceViewerExtension3) {
			IQuickAssistAssistant assistant= ((ISourceViewerExtension3)fSourceViewer).getQuickAssistAssistant();
			if (assistant != null)
				fKeyBindingSupportForQuickAssistant= new KeyBindingSupportForAssistant(assistant);
		}

		if (fRangeIndicator != null)
			fSourceViewer.setRangeIndicator(fRangeIndicator);

		fSourceViewer.addTextListener(fTextListener);
		fSourceViewer.addTextInputListener(fTextListener);
		getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());

		initializeViewerFont(fSourceViewer);
		initializeViewerColors(fSourceViewer);
		initializeFindScopeColor(fSourceViewer);
		initializeDragAndDrop(fSourceViewer);

		StyledText styledText= fSourceViewer.getTextWidget();

		/* gestures commented out until proper solution (i.e. preference page) can be found
		 * for bug # 28417:
		 *
		final Map gestureMap= new HashMap();

		gestureMap.put("E", IWorkbenchCommandConstants.NAVIGATE_FORWARDHISTORY); //$NON-NLS-1$
		gestureMap.put("N", IWorkbenchCommandConstants.FILE_SAVE); //$NON-NLS-1$
		gestureMap.put("NW", IWorkbenchCommandConstants.FILE_SAVEALL); //$NON-NLS-1$
		gestureMap.put("S", IWorkbenchCommandConstants.FILE_CLOSE); //$NON-NLS-1$
		gestureMap.put("SW", IWorkbenchCommandConstants.FILE_CLOSEALL); //$NON-NLS-1$
		gestureMap.put("W", IWorkbenchCommandConstants.NAVIGATE_BACKWARDHISTORY); //$NON-NLS-1$
		gestureMap.put("EN", IWorkbenchCommandConstants.EDIT_COPY); //$NON-NLS-1$
		gestureMap.put("ES", IWorkbenchCommandConstants.EDIT_PASTE); //$NON-NLS-1$
		gestureMap.put("EW", IWorkbenchCommandConstants.EDIT_CUT); //$NON-NLS-1$

		Capture capture= Capture.create();
		capture.setControl(styledText);

		capture.addCaptureListener(new CaptureListener() {
			public void gesture(Gesture gesture) {
				if (gesture.getPen() == 3) {
					String actionId= (String) gestureMap.get(Util.recognize(gesture.getPoints(), 20));

					if (actionId != null) {
						IKeyBindingService keyBindingService= getEditorSite().getKeyBindingService();

						if (keyBindingService instanceof KeyBindingService) {
							IAction action= ((KeyBindingService) keyBindingService).getAction(actionId);

							if (action != null) {
								if (action instanceof IUpdate)
									((IUpdate) action).update();

								if (action.isEnabled())
									action.run();
							}
						}

						return;
					}

					fTextContextMenu.setVisible(true);
				}
			};
		});
		*/

		styledText.addMouseListener(getCursorListener());
		styledText.addKeyListener(getCursorListener());

		// Disable orientation switching until we fully support it.
		styledText.addListener(SWT.OrientationChange, new Listener() {
			public void handleEvent(Event event) {
				event.doit= false;
			}
		});

		if (getHelpContextId() != null)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(styledText, getHelpContextId());


		String id= fEditorContextMenuId != null ?  fEditorContextMenuId : DEFAULT_EDITOR_CONTEXT_MENU_ID;

		MenuManager manager= new MenuManager(id, id);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(getContextMenuListener());
		fTextContextMenu= manager.createContextMenu(styledText);

		// comment this line if using gestures, above.
		styledText.setMenu(fTextContextMenu);

		if (fEditorContextMenuId != null)
			getEditorSite().registerContextMenu(fEditorContextMenuId, manager, getSelectionProvider(), isEditorInputIncludedInContextMenu());
		else if (fCompatibilityMode)
			getEditorSite().registerContextMenu(DEFAULT_EDITOR_CONTEXT_MENU_ID, manager, getSelectionProvider(), isEditorInputIncludedInContextMenu());

		if ((fEditorContextMenuId != null && fCompatibilityMode) || fEditorContextMenuId  == null) {
			String partId= getEditorSite().getId();
			if (partId != null)
				getEditorSite().registerContextMenu(partId + ".EditorContext", manager, getSelectionProvider(), isEditorInputIncludedInContextMenu()); //$NON-NLS-1$
		}

		getEditorSite().registerContextMenu(COMMON_EDITOR_CONTEXT_MENU_ID, manager, getSelectionProvider(), false);

		if (fEditorContextMenuId == null)
			fEditorContextMenuId= DEFAULT_EDITOR_CONTEXT_MENU_ID;


		id= fRulerContextMenuId != null ? fRulerContextMenuId : DEFAULT_RULER_CONTEXT_MENU_ID;
		manager= new MenuManager(id, id);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(getContextMenuListener());

		Control rulerControl= fVerticalRuler.getControl();
		fRulerContextMenu= manager.createContextMenu(rulerControl);
		rulerControl.setMenu(fRulerContextMenu);
		rulerControl.addMouseListener(getRulerMouseListener());

		if (fRulerContextMenuId != null)
			getEditorSite().registerContextMenu(fRulerContextMenuId, manager, getSelectionProvider(), false);
		else if (fCompatibilityMode)
			getEditorSite().registerContextMenu(DEFAULT_RULER_CONTEXT_MENU_ID, manager, getSelectionProvider(), false);

		if ((fRulerContextMenuId != null && fCompatibilityMode) || fRulerContextMenuId  == null) {
			String partId= getSite().getId();
			if (partId != null)
				getEditorSite().registerContextMenu(partId + ".RulerContext", manager, getSelectionProvider(), false); //$NON-NLS-1$
		}

		getEditorSite().registerContextMenu(COMMON_RULER_CONTEXT_MENU_ID, manager, getSelectionProvider(), false);

		if (fRulerContextMenuId == null)
			fRulerContextMenuId= DEFAULT_RULER_CONTEXT_MENU_ID;

		getSite().setSelectionProvider(getSelectionProvider());

		fSelectionListener= new SelectionListener();
		fSelectionListener.install(getSelectionProvider());
		fSelectionListener.setDocument(getDocumentProvider().getDocument(getEditorInput()));

		initializeActivationCodeTrigger();

		createNavigationActions();
		createAccessibilityActions();
		createActions();

		initializeSourceViewer(getEditorInput());

		/* since 3.2 - undo redo actions should be created after
		 * the source viewer is initialized, so that the undo manager
		 * can obtain its undo context from its document.
		 */
		createUndoRedoActions();

		JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);

		IVerticalRuler ruler= getVerticalRuler();
		if (ruler instanceof CompositeRuler)
			updateContributedRulerColumns((CompositeRuler) ruler);
	}

	/**
	 * Installs text drag and drop on the given source viewer.
	 *
	 * @param viewer the viewer
	 * @since 3.3
	 */
	protected void installTextDragAndDrop(final ISourceViewer viewer) {
		if (viewer == null || fIsTextDragAndDropInstalled)
			return;

		final IDragAndDropService dndService= (IDragAndDropService)getSite().getService(IDragAndDropService.class);
		if (dndService == null)
			return;

		final StyledText st= viewer.getTextWidget();

		// Install drag source
		final ISelectionProvider selectionProvider= viewer.getSelectionProvider();
		final DragSource source= new DragSource(st, DND.DROP_COPY | DND.DROP_MOVE);
		source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		source.addDragListener(new DragSourceAdapter() {
			String fSelectedText;
			Point fSelection;
			public void dragStart(DragSourceEvent event) {
				fTextDragAndDropToken= null;
				try {
					fSelection= st.getSelection();
					event.doit= isLocationSelected(new Point(event.x, event.y));

					ISelection selection= selectionProvider.getSelection();
					if (selection instanceof ITextSelection)
						fSelectedText= ((ITextSelection)selection).getText();
					else // fallback to widget
						fSelectedText= st.getSelectionText();
				} catch (IllegalArgumentException ex) {
					event.doit= false;
				}
			}

			private boolean isLocationSelected(Point point) {
				// FIXME: https://bugs.eclipse.org/bugs/show_bug.cgi?id=260922
				if (isBlockSelectionModeEnabled())
					return false;

				int offset= st.getOffsetAtLocation(point);
				Point p= st.getLocationAtOffset(offset);
				if (p.x > point.x)
					offset--;
				return offset >= fSelection.x && offset < fSelection.y;
			}

			public void dragSetData(DragSourceEvent event) {
				event.data= fSelectedText;
				fTextDragAndDropToken= this; // Can be any non-null object
			}

			public void dragFinished(DragSourceEvent event) {
				try {
					if (event.detail == DND.DROP_MOVE && validateEditorInputState()) {
						Point newSelection= st.getSelection();
						int length= fSelection.y - fSelection.x;
						int delta= 0;
						if (newSelection.x < fSelection.x)
							delta= length;
						st.replaceTextRange(fSelection.x + delta, length, ""); //$NON-NLS-1$

						if (fTextDragAndDropToken == null) {
							// Move in same editor - end compound change
							IRewriteTarget target= (IRewriteTarget)getAdapter(IRewriteTarget.class);
							if (target != null)
								target.endCompoundChange();
						}

					}
				} finally {
					fTextDragAndDropToken= null;
				}
			}
		});

		// Install drag target
		DropTargetListener dropTargetListener= new DropTargetAdapter() {

			private Point fSelection;

			public void dragEnter(DropTargetEvent event) {
				fTextDragAndDropToken= null;
				fSelection= st.getSelection();
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_MOVE) != 0) {
						event.detail= DND.DROP_MOVE;
					} else if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail= DND.DROP_COPY;
					} else {
						event.detail= DND.DROP_NONE;
					}
				}
			}

			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_MOVE) != 0) {
						event.detail= DND.DROP_MOVE;
					} else if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail= DND.DROP_COPY;
					} else {
						event.detail= DND.DROP_NONE;
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				event.feedback |= DND.FEEDBACK_SCROLL;
			}

			public void drop(DropTargetEvent event) {
				try {
					if (fTextDragAndDropToken != null && event.detail == DND.DROP_MOVE) {
						// Move in same editor
						int caretOffset= st.getCaretOffset();
						if (fSelection.x <= caretOffset && caretOffset <= fSelection.y) {
							event.detail= DND.DROP_NONE;
							return;
						}

						// Start compound change
						IRewriteTarget target= (IRewriteTarget)getAdapter(IRewriteTarget.class);
						if (target != null)
							target.beginCompoundChange();
					}

					if (!validateEditorInputState()) {
						event.detail= DND.DROP_NONE;
						return;
					}

					String text= (String)event.data;
					if (isBlockSelectionModeEnabled()) {
						// FIXME fix block selection and DND
//						if (fTextDNDColumnSelection != null && fTextDragAndDropToken != null && event.detail == DND.DROP_MOVE) {
//							// DND_MOVE within same editor - remove origin before inserting
//							Rectangle newSelection= st.getColumnSelection();
//							st.replaceColumnSelection(fTextDNDColumnSelection, ""); //$NON-NLS-1$
//							st.replaceColumnSelection(newSelection, text);
//							st.setColumnSelection(newSelection.x, newSelection.y, newSelection.x + fTextDNDColumnSelection.width - fTextDNDColumnSelection.x, newSelection.y + fTextDNDColumnSelection.height - fTextDNDColumnSelection.y);
//						} else {
//							Point newSelection= st.getSelection();
//							st.insert(text);
//							IDocument document= getDocumentProvider().getDocument(getEditorInput());
//							int startLine= st.getLineAtOffset(newSelection.x);
//							int startColumn= newSelection.x - st.getOffsetAtLine(startLine);
//							int endLine= startLine + document.computeNumberOfLines(text);
//							int endColumn= startColumn + TextUtilities.indexOf(document.getLegalLineDelimiters(), text, 0)[0];
//							st.setColumnSelection(startColumn, startLine, endColumn, endLine);
//						}
					} else {
						Point newSelection= st.getSelection();
						try {
							int modelOffset= widgetOffset2ModelOffset(viewer, newSelection.x);
							viewer.getDocument().replace(modelOffset, 0, text);
						} catch (BadLocationException e) {
							return;
						}
						st.setSelectionRange(newSelection.x, text.length());
					}
				} finally {
					fTextDragAndDropToken= null;
				}
			}
		};
		dndService.addMergedDropTarget(st, DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] {TextTransfer.getInstance()}, dropTargetListener);

		fIsTextDragAndDropInstalled= true;
	}

	/**
	 * Uninstalls text drag and drop from the given source viewer.
	 *
	 * @param viewer the viewer
	 * @since 3.3
	 */
	protected void uninstallTextDragAndDrop(ISourceViewer viewer) {
		if (viewer == null || !fIsTextDragAndDropInstalled)
			return;

		final IDragAndDropService dndService= (IDragAndDropService)getSite().getService(IDragAndDropService.class);
		if (dndService == null)
			return;

		StyledText st= viewer.getTextWidget();
		dndService.removeMergedDropTarget(st);

		DragSource dragSource= (DragSource)st.getData(DND.DRAG_SOURCE_KEY);
		if (dragSource != null) {
			dragSource.dispose();
			st.setData(DND.DRAG_SOURCE_KEY, null);
		}

		fIsTextDragAndDropInstalled= false;
	}

	/**
     * Tells whether the editor input should be included when adding object
     * contributions to this editor's context menu.
     * <p>
     * This implementation always returns <code>true</code>.
     * </p>
     *
	 * @return <code>true</code> if the editor input should be considered
     * @since 3.2
	 */
	protected boolean isEditorInputIncludedInContextMenu() {
		return true;
	}

	/**
	 * Initializes the activation code trigger.
	 *
	 * @since 2.1
	 */
	private void initializeActivationCodeTrigger() {
		fActivationCodeTrigger.install();
		fActivationCodeTrigger.setScopes(fKeyBindingScopes);
	}

	/**
	 * Initializes the given viewer's font.
	 *
	 * @param viewer the viewer
	 * @since 2.0
	 */
	private void initializeViewerFont(ISourceViewer viewer) {

		boolean isSharedFont= true;
		Font font= null;
		String symbolicFontName= getSymbolicFontName();

		if (symbolicFontName != null)
			font= JFaceResources.getFont(symbolicFontName);
		else if (fPreferenceStore != null) {
			// Backward compatibility
			if (fPreferenceStore.contains(JFaceResources.TEXT_FONT) && !fPreferenceStore.isDefault(JFaceResources.TEXT_FONT)) {
				FontData data= PreferenceConverter.getFontData(fPreferenceStore, JFaceResources.TEXT_FONT);

				if (data != null) {
					isSharedFont= false;
					font= new Font(viewer.getTextWidget().getDisplay(), data);
				}
			}
		}
		if (font == null)
			font= JFaceResources.getTextFont();

		if (!font.equals(fSourceViewer.getTextWidget().getFont())) {
			setFont(viewer, font);

			disposeFont();
			if (!isSharedFont)
				fFont= font;
		} else if (!isSharedFont) {
			font.dispose();
		}
	}

	/**
	 * Disposes of the non-shared font.
	 * 
	 * @since 3.5
	 */
	private void disposeFont() {
		if (fFont != null) {
			fFont.dispose();
			fFont= null;
		}
	}

	/**
	 * Sets the font for the given viewer sustaining selection and scroll position.
	 *
	 * @param sourceViewer the source viewer
	 * @param font the font
	 * @since 2.0
	 */
	private void setFont(ISourceViewer sourceViewer, Font font) {
		if (sourceViewer.getDocument() != null) {

			ISelectionProvider provider= sourceViewer.getSelectionProvider();
			ISelection selection= provider.getSelection();
			int topIndex= sourceViewer.getTopIndex();

			StyledText styledText= sourceViewer.getTextWidget();
			Control parent= styledText;
			if (sourceViewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= (ITextViewerExtension) sourceViewer;
				parent= extension.getControl();
			}

			parent.setRedraw(false);

			styledText.setFont(font);

			if (fVerticalRuler instanceof IVerticalRulerExtension) {
				IVerticalRulerExtension e= (IVerticalRulerExtension) fVerticalRuler;
				e.setFont(font);
			}

			provider.setSelection(selection);
			sourceViewer.setTopIndex(topIndex);

			if (parent instanceof Composite) {
				Composite composite= (Composite) parent;
				composite.layout(true);
			}

			parent.setRedraw(true);


		} else {

			StyledText styledText= sourceViewer.getTextWidget();
			styledText.setFont(font);

			if (fVerticalRuler instanceof IVerticalRulerExtension) {
				IVerticalRulerExtension e= (IVerticalRulerExtension) fVerticalRuler;
				e.setFont(font);
			}
		}
	}

	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 *
	 * @param store the store to read from
	 * @param key the key used for the lookup in the preference store
	 * @param display the display used create the color
	 * @return the created color according to the specification in the preference store
	 * @since 2.0
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
	 * Initializes the fore- and background colors of the given viewer for both
	 * normal and selected text.
	 *
	 * @param viewer the viewer to be initialized
	 * @since 2.0
	 */
	protected void initializeViewerColors(ISourceViewer viewer) {

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

			// ----------- selection foreground color --------------------
			color= store.getBoolean(PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, PREFERENCE_COLOR_SELECTION_FOREGROUND, styledText.getDisplay());
			styledText.setSelectionForeground(color);

			if (fSelectionForegroundColor != null)
				fSelectionForegroundColor.dispose();

			fSelectionForegroundColor= color;

			// ---------- selection background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, PREFERENCE_COLOR_SELECTION_BACKGROUND, styledText.getDisplay());
			styledText.setSelectionBackground(color);

			if (fSelectionBackgroundColor != null)
				fSelectionBackgroundColor.dispose();

			fSelectionBackgroundColor= color;
		}
	}

	/**
	 * Initializes the background color used for highlighting the document ranges
	 * defining search scopes.
	 *
	 * @param viewer the viewer to initialize
	 * @since 2.0
	 */
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

		IDocumentProvider documentProvider= getDocumentProvider();
		IAnnotationModel model= documentProvider.getAnnotationModel(input);
		IDocument document= documentProvider.getDocument(input);

		if (document != null) {
			fSourceViewer.setDocument(document, model);
			fSourceViewer.setEditable(isEditable());
			fSourceViewer.showAnnotations(model != null);
		}

		if (fElementStateListener instanceof IElementStateListenerExtension) {
			boolean isStateValidated= false;
			if (documentProvider instanceof IDocumentProviderExtension)
				isStateValidated= ((IDocumentProviderExtension)documentProvider).isStateValidated(input);

			IElementStateListenerExtension extension= (IElementStateListenerExtension) fElementStateListener;
			extension.elementStateValidationChanged(input, isStateValidated);
		}

		if (fInitialCaret == null)
			fInitialCaret= fSourceViewer.getTextWidget().getCaret();

		if (fIsOverwriting)
			fSourceViewer.getTextWidget().invokeAction(ST.TOGGLE_OVERWRITE);
		handleInsertModeChanged();

		if (isTabsToSpacesConversionEnabled())
			installTabsToSpacesConverter();

		if (fSourceViewer instanceof ITextViewerExtension8) {
			IPreferenceStore store= getPreferenceStore();
			EnrichMode mode= store != null ? convertEnrichModePreference(store.getInt(PREFERENCE_HOVER_ENRICH_MODE)) : EnrichMode.AFTER_DELAY;
			((ITextViewerExtension8)fSourceViewer).setHoverEnrichMode(mode);
		}
	}

	/**
	 * Converts the {link #PREFERENCE_HOVER_ENRICH_MODE} preference value to
	 * {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode}.
	 *
	 * @param mode the preference value
	 * @return the enrich mode, can be <code>null</code>
	 * @since 3.4
	 */
	private EnrichMode convertEnrichModePreference(int mode) {
		switch (mode) {
			case -1:
				return null;
			case 0:
				return EnrichMode.AFTER_DELAY;
			case 1:
				return EnrichMode.IMMEDIATELY;
			case 2:
				return EnrichMode.ON_CLICK;
			default:
				Assert.isLegal(false);
			return null;
		}
	}

	/**
	 * Initializes the editor's title based on the given editor input.
	 * <p>
	 * <strong>Note:</strong> We use the editor's image instead of the image from the
	 * editor input to distinguish situations where the same editor input is
	 * opened in different kinds of editors.
	 * </p>
	 *
	 * @param input the editor input to be used
	 */
	private void initializeTitle(IEditorInput input) {

		Image oldImage= fTitleImage;
		fTitleImage= null;
		String title= ""; //$NON-NLS-1$

		if (input != null) {
			IEditorRegistry editorRegistry= PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor editorDesc= editorRegistry.findEditor(getSite().getId());
			ImageDescriptor imageDesc= editorDesc != null ? editorDesc.getImageDescriptor() : null;

			fTitleImage= imageDesc != null ? imageDesc.createImage() : null;
			title= input.getName();
		}

		setTitleImage(fTitleImage);
		setPartName(title);

		firePropertyChange(PROP_DIRTY);

		if (oldImage != null && !oldImage.isDisposed())
			oldImage.dispose();
	}

	/**
	 * Hook method for setting the document provider for the given input.
	 * This default implementation does nothing. Clients may
	 * reimplement.
	 *
	 * @param input the input of this editor.
	 * @since 3.0
	 */
	protected void setDocumentProvider(IEditorInput input) {
	}

	/**
	 * If there is no explicit document provider set, the implicit one is
	 * re-initialized based on the given editor input.
	 *
	 * @param input the editor input.
	 */
	private void updateDocumentProvider(IEditorInput input) {

		IProgressMonitor rememberedProgressMonitor= null;

		IDocumentProvider provider= getDocumentProvider();
		if (provider != null) {
			provider.removeElementStateListener(fElementStateListener);
			if (provider instanceof IDocumentProviderExtension2) {
				IDocumentProviderExtension2 extension= (IDocumentProviderExtension2) provider;
				rememberedProgressMonitor= extension.getProgressMonitor();
				extension.setProgressMonitor(null);
			}
		}

		setDocumentProvider(input);

		provider= getDocumentProvider();
		if (provider != null) {
			provider.addElementStateListener(fElementStateListener);
			if (provider instanceof IDocumentProviderExtension2) {
				IDocumentProviderExtension2 extension= (IDocumentProviderExtension2) provider;
				extension.setProgressMonitor(rememberedProgressMonitor);
			}
		}
	}

	/**
	 * Called directly from <code>setInput</code> and from within a workspace
	 * runnable from <code>init</code>, this method does the actual setting
	 * of the editor input. Closes the editor if <code>input</code> is
	 * <code>null</code>. Disconnects from any previous editor input and its
	 * document provider and connects to the new one.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @param input the input to be set
	 * @exception CoreException if input cannot be connected to the document
	 *            provider
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		ISaveablesLifecycleListener listener= (ISaveablesLifecycleListener)getSite().getService(ISaveablesLifecycleListener.class);
		if (listener == null)
			fSavable= null;

		if (input == null) {
			close(isSaveOnCloseNeeded());

			if (fSavable != null) {
				listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this,	SaveablesLifecycleEvent.POST_CLOSE,	getSaveables(), false));
				fSavable.disconnectEditor();
				fSavable= null;
			}

		} else {
			boolean mustSendLifeCycleEvent= false;
			if (fSavable != null) {
				listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this,	SaveablesLifecycleEvent.POST_CLOSE,	getSaveables(), false));
				fSavable.disconnectEditor();
				fSavable= null;
				mustSendLifeCycleEvent= true;
			}

			IEditorInput oldInput= getEditorInput();
			if (oldInput != null)
				getDocumentProvider().disconnect(oldInput);

			super.setInput(input);

			updateDocumentProvider(input);

			IDocumentProvider provider= getDocumentProvider();
			if (provider == null) {
				IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, EditorMessages.Editor_error_no_provider, null);
				throw new CoreException(s);
			}

			provider.connect(input);

			initializeTitle(input);

			if (fSourceViewer != null) {
				initializeSourceViewer(input);

				// Reset the undo context for the undo and redo action handlers
				IAction undoAction= getAction(ITextEditorActionConstants.UNDO);
				IAction redoAction= getAction(ITextEditorActionConstants.REDO);
				boolean areOperationActionHandlersInstalled= undoAction instanceof OperationHistoryActionHandler && redoAction instanceof OperationHistoryActionHandler;
				IUndoContext undoContext= getUndoContext();
				if (undoContext != null && areOperationActionHandlersInstalled) {
					((OperationHistoryActionHandler)undoAction).setContext(undoContext);
					((OperationHistoryActionHandler)redoAction).setContext(undoContext);
				} else {
					createUndoRedoActions();
				}
			}

			if (fIsOverwriting)
				toggleOverwriteMode();
			setInsertMode((InsertMode) getLegalInsertModes().get(0));
			updateCaret();

			updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);

			if (fSelectionListener != null)
				fSelectionListener.setDocument(getDocumentProvider().getDocument(input));

			IVerticalRuler ruler= getVerticalRuler();
			if (ruler instanceof CompositeRuler)
				updateContributedRulerColumns((CompositeRuler) ruler);

			// Send savable life-cycle if needed.
			if (mustSendLifeCycleEvent && listener != null)
				listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this,	SaveablesLifecycleEvent.POST_OPEN, getSaveables(), false));

		}

	}

	/**
	 * Returns this editor's viewer's undo manager undo context.
	 *
	 * @return the undo context or <code>null</code> if not available
	 * @since 3.1
	 */
	private IUndoContext getUndoContext() {
		if (fSourceViewer instanceof ITextViewerExtension6) {
			IUndoManager undoManager= ((ITextViewerExtension6)fSourceViewer).getUndoManager();
			if (undoManager instanceof IUndoManagerExtension)
				return ((IUndoManagerExtension)undoManager).getUndoContext();
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.part.EditorPart#setInputWithNotify(org.eclipse.ui.IEditorInput)
	 * @since 3.2
	 */
	protected final void setInputWithNotify(IEditorInput input) {
		try {

			doSetInput(input);

			/*
			 * The following bugs explain why we fire this property change:
			 * 	https://bugs.eclipse.org/bugs/show_bug.cgi?id=90283
			 * 	https://bugs.eclipse.org/bugs/show_bug.cgi?id=92049
			 * 	https://bugs.eclipse.org/bugs/show_bug.cgi?id=92286
			 */
			firePropertyChange(IEditorPart.PROP_INPUT);

		} catch (CoreException x) {
			String title= EditorMessages.Editor_error_setinput_title;
			String msg= EditorMessages.Editor_error_setinput_message;
			Shell shell= getSite().getShell();
			ErrorDialog.openError(shell, title, msg, x.getStatus());
		}
	}

	/*
	 * @see EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	public final void setInput(IEditorInput input) {
		setInputWithNotify(input);
	}

	/*
	 * @see ITextEditor#close
	 */
	public void close(final boolean save) {

		enableSanityChecking(false);

		Display display= getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (fSourceViewer != null)
					getSite().getPage().closeEditor(AbstractTextEditor.this, save);
			}
		});
	}

	/**
	 * The <code>AbstractTextEditor</code> implementation of this
	 * <code>IWorkbenchPart</code> method may be extended by subclasses.
	 * Subclasses must call <code>super.dispose()</code>.
	 * <p>
	 * Note that many methods may return <code>null</code> after the editor is
	 * disposed.
	 * </p>
	 */
	public void dispose() {

		if (fActivationListener != null) {
			fActivationListener.dispose();
			fActivationListener= null;
		}

		if (fTitleImage != null) {
			fTitleImage.dispose();
			fTitleImage= null;
		}

		disposeFont();

		disposeNonDefaultCaret();
		fInitialCaret= null;

		if (fForegroundColor != null) {
			fForegroundColor.dispose();
			fForegroundColor= null;
		}

		if (fBackgroundColor != null) {
			fBackgroundColor.dispose();
			fBackgroundColor= null;
		}

		if (fSelectionForegroundColor != null) {
			fSelectionForegroundColor.dispose();
			fSelectionForegroundColor= null;
		}

		if (fSelectionBackgroundColor != null) {
			fSelectionBackgroundColor.dispose();
			fSelectionBackgroundColor= null;
		}

		if (fFindScopeHighlightColor != null) {
			fFindScopeHighlightColor.dispose();
			fFindScopeHighlightColor= null;
		}

		if (fFontPropertyChangeListener != null) {
			JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
			if (fPreferenceStore != null)
				fPreferenceStore.removePropertyChangeListener(fFontPropertyChangeListener);
			fFontPropertyChangeListener= null;
		}

		if (fPropertyChangeListener != null) {
			if (fPreferenceStore != null) {
				fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
				fPreferenceStore= null;
			}
			fPropertyChangeListener= null;
		}

		if (fActivationCodeTrigger != null) {
			fActivationCodeTrigger.uninstall();
			fActivationCodeTrigger= null;
		}

		if (fSelectionListener != null)  {
			fSelectionListener.uninstall(getSelectionProvider());
			fSelectionListener= null;
		}

		if (fSavable != null) {
			fSavable.disconnectEditor();
			fSavable= null;
		}

		disposeDocumentProvider();

		if (fSourceViewer != null) {

			if (fTextListener != null) {
				fSourceViewer.removeTextListener(fTextListener);
				fSourceViewer.removeTextInputListener(fTextListener);
				fTextListener= null;
			}

			uninstallTabsToSpacesConverter();

			fTextInputListener= null;
			fSelectionProvider= null;
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
			registerUndoRedoAction(ITextEditorActionConstants.UNDO, null);
			registerUndoRedoAction(ITextEditorActionConstants.REDO, null);
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

		if (fStateActions != null) {
			fStateActions.clear();
			fStateActions= null;
		}

		if (fActivationCodes != null) {
			fActivationCodes.clear();
			fActivationCodes= null;
		}

		if (fEditorStatusLine != null)
			fEditorStatusLine= null;

		if (fConfiguration != null)
			fConfiguration= null;

		if (fColumnSupport != null) {
			fColumnSupport.dispose();
			fColumnSupport= null;
		}

		if (fVerticalRuler != null)
			fVerticalRuler= null;

		IOperationHistory history= OperationHistoryFactory.getOperationHistory();
		if (history != null) {
			if (fNonLocalOperationApprover != null)
				history.removeOperationApprover(fNonLocalOperationApprover);
			if (fLinearUndoViolationApprover != null)
				history.removeOperationApprover(fLinearUndoViolationApprover);
		}
		fNonLocalOperationApprover= null;
		fLinearUndoViolationApprover= null;

		if (fKeyBindingSupportForContentAssistant != null) {
			fKeyBindingSupportForContentAssistant.dispose();
			fKeyBindingSupportForContentAssistant= null;
		}

		if (fKeyBindingSupportForQuickAssistant != null) {
			fKeyBindingSupportForQuickAssistant.dispose();
			fKeyBindingSupportForQuickAssistant= null;
		}
		
		if (fInformationPresenter != null) {
			fInformationPresenter.uninstall();
			fInformationPresenter= null;
		}

		if (fSaveAction != null) {
			fSaveAction.dispose();
			fSaveAction= null;
		}

		super.dispose();
	}

	/**
	 * Disposes of the connection with the document provider. Subclasses
	 * may extend.
	 *
	 * @since 3.0
	 */
	protected void disposeDocumentProvider() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider != null) {

			IEditorInput input= getEditorInput();
			if (input != null)
				provider.disconnect(input);

			if (fElementStateListener != null) {
				provider.removeElementStateListener(fElementStateListener);
				fElementStateListener= null;
			}

		}
		fExplicitDocumentProvider= null;
	}

	/**
	 * Determines whether the given preference change affects the editor's
	 * presentation. This implementation always returns <code>false</code>.
	 * May be reimplemented by subclasses.
	 *
	 * @param event the event which should be investigated
	 * @return <code>true</code> if the event describes a preference change affecting the editor's presentation
	 * @since 2.0
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return false;
	}

	/**
	 * Returns the symbolic font name for this editor as defined in XML.
	 *
	 * @return a String with the symbolic font name or <code>null</code> if
	 *         none is defined
	 * @since 2.1
	 */
	private String getSymbolicFontName() {
		if (getConfigurationElement() != null)
			return getConfigurationElement().getAttribute("symbolicFontName"); //$NON-NLS-1$
		return null;
	}

	/**
	 * Returns the property preference key for the editor font.
	 * <p>
	 * If the editor is defined with a <code>symbolicFontName </code> then this name is returned and
	 * the font is looked up in the JFace resource registry. Otherwise,
	 * {@link JFaceResources#TEXT_FONT} is returned and the font is looked up in this editor's
	 * preference store.
	 * </p>
	 * 
	 * @return a String with the key
	 * @since 2.1
	 */
	protected final String getFontPropertyPreferenceKey() {
		String symbolicFontName= getSymbolicFontName();
		if (symbolicFontName != null)
			return symbolicFontName;
		return JFaceResources.TEXT_FONT;
	}

	/**
	 * Handles a property change event describing a change of the editor's
	 * preference store and updates the preference related editor properties.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @param event the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {

		if (fSourceViewer == null)
			return;

		String property= event.getProperty();

		if (getFontPropertyPreferenceKey().equals(property))
			// There is a separate handler for font preference changes
			return;

		if (PREFERENCE_COLOR_FOREGROUND.equals(property) || PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
				PREFERENCE_COLOR_BACKGROUND.equals(property) ||	PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property) ||
				PREFERENCE_COLOR_SELECTION_FOREGROUND.equals(property) || PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
				PREFERENCE_COLOR_SELECTION_BACKGROUND.equals(property) ||	PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT.equals(property))
		{
			initializeViewerColors(fSourceViewer);
		} else if (PREFERENCE_COLOR_FIND_SCOPE.equals(property)) {
			initializeFindScopeColor(fSourceViewer);
		} else if (PREFERENCE_USE_CUSTOM_CARETS.equals(property)) {
			updateCaret();
		} else if (PREFERENCE_WIDE_CARET.equals(property)) {
			updateCaret();
		}

		if (affectsTextPresentation(event))
			fSourceViewer.invalidateTextPresentation();

		if (PREFERENCE_HYPERLINKS_ENABLED.equals(property)) {
			if (fSourceViewer instanceof ITextViewerExtension6) {
				IHyperlinkDetector[] detectors= getSourceViewerConfiguration().getHyperlinkDetectors(fSourceViewer);
				int stateMask= getSourceViewerConfiguration().getHyperlinkStateMask(fSourceViewer);
				ITextViewerExtension6 textViewer6= (ITextViewerExtension6)fSourceViewer;
				textViewer6.setHyperlinkDetectors(detectors, stateMask);
			}
			return;
		}

		if (PREFERENCE_HYPERLINK_KEY_MODIFIER.equals(property)) {
			if (fSourceViewer instanceof ITextViewerExtension6) {
				ITextViewerExtension6 textViewer6= (ITextViewerExtension6)fSourceViewer;
				IHyperlinkDetector[] detectors= getSourceViewerConfiguration().getHyperlinkDetectors(fSourceViewer);
				int stateMask= getSourceViewerConfiguration().getHyperlinkStateMask(fSourceViewer);
				textViewer6.setHyperlinkDetectors(detectors, stateMask);
			}
			return;
		}

		if (PREFERENCE_RULER_CONTRIBUTIONS.equals(property)) {
			String[] difference= StringSetSerializer.getDifference((String) event.getOldValue(), (String) event.getNewValue());
			IColumnSupport support= (IColumnSupport) getAdapter(IColumnSupport.class);
			for (int i= 0; i < difference.length; i++) {
				RulerColumnDescriptor desc= RulerColumnRegistry.getDefault().getColumnDescriptor(difference[i]);
				if (desc != null &&  support.isColumnSupported(desc)) {
					boolean newState= !support.isColumnVisible(desc);
					support.setColumnVisible(desc, newState);
				}
			}
			return;
		}

		if (PREFERENCE_SHOW_WHITESPACE_CHARACTERS.equals(property) ||
				PREFERENCE_SHOW_LEADING_SPACES.equals(property) ||
				PREFERENCE_SHOW_ENCLOSED_SPACES.equals(property) ||
				PREFERENCE_SHOW_TRAILING_SPACES.equals(property) ||
				PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES.equals(property) ||
				PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES.equals(property) ||
				PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES.equals(property) ||
				PREFERENCE_SHOW_LEADING_TABS.equals(property) ||
				PREFERENCE_SHOW_ENCLOSED_TABS.equals(property) ||
				PREFERENCE_SHOW_TRAILING_TABS.equals(property) ||
				PREFERENCE_SHOW_CARRIAGE_RETURN.equals(property) ||
				PREFERENCE_SHOW_LINE_FEED.equals(property) ||
				PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE.equals(property)) {
			IAction action= getAction(ITextEditorActionConstants.SHOW_WHITESPACE_CHARACTERS);
			if (action instanceof IUpdate)
				((IUpdate)action).update();
			return;
		}

		if (PREFERENCE_TEXT_DRAG_AND_DROP_ENABLED.equals(property)) {
			IPreferenceStore store= getPreferenceStore();
			if (store != null && store.getBoolean(PREFERENCE_TEXT_DRAG_AND_DROP_ENABLED))
				installTextDragAndDrop(getSourceViewer());
			else
				uninstallTextDragAndDrop(getSourceViewer());
			return;
		}

		if (PREFERENCE_HOVER_ENRICH_MODE.equals(property)) {
			if (fSourceViewer instanceof ITextViewerExtension8) {
				IPreferenceStore store= getPreferenceStore();
				if (store != null) {
					((ITextViewerExtension8)fSourceViewer).setHoverEnrichMode(convertEnrichModePreference(store.getInt(PREFERENCE_HOVER_ENRICH_MODE)));
				}
			}
			return;
		}

	}

	/**
	 * Returns the progress monitor related to this editor. It should not be
	 * necessary to extend this method.
	 *
	 * @return the progress monitor related to this editor
	 * @since 2.1
	 */
	protected IProgressMonitor getProgressMonitor() {

		IProgressMonitor pm= null;

		IStatusLineManager manager= getStatusLineManager();
		if (manager != null)
			pm= manager.getProgressMonitor();

		return pm != null ? pm : new NullProgressMonitor();
	}

	/**
	 * Handles an external change of the editor's input element. Subclasses may
	 * extend.
	 */
	protected void handleEditorInputChanged() {

		String title;
		String msg;
		Shell shell= getSite().getShell();

		final IDocumentProvider provider= getDocumentProvider();
		if (provider == null) {
			// fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=15066
			close(false);
			return;
		}

		final IEditorInput input= getEditorInput();
		final String inputName= input.getToolTipText();

		if (provider.isDeleted(input)) {

			if (isSaveAsAllowed()) {

				title= EditorMessages.Editor_error_activated_deleted_save_title;
				msg= NLSUtility.format(EditorMessages.Editor_error_activated_deleted_save_message, inputName);

				String[] buttons= {
						EditorMessages.Editor_error_activated_deleted_save_button_save,
						EditorMessages.Editor_error_activated_deleted_save_button_close,
				};

				MessageDialog dialog= new MessageDialog(shell, title, null, msg, MessageDialog.QUESTION, buttons, 0);

				if (dialog.open() == 0) {
					IProgressMonitor pm= getProgressMonitor();
					performSaveAs(pm);
					if (pm.isCanceled())
						handleEditorInputChanged();
				} else {
					close(false);
				}

			} else {

				title= EditorMessages.Editor_error_activated_deleted_close_title;
				msg= NLSUtility.format(EditorMessages.Editor_error_activated_deleted_close_message, inputName);
				if (MessageDialog.openConfirm(shell, title, msg))
					close(false);
			}

		} else if (fHasBeenActivated) {

			title= EditorMessages.Editor_error_activated_outofsync_title;
			msg= NLSUtility.format(EditorMessages.Editor_error_activated_outofsync_message, inputName);

			if (MessageDialog.openQuestion(shell, title, msg)) {


				try {
					if (provider instanceof IDocumentProviderExtension) {
						IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
						extension.synchronize(input);
					} else {
						doSetInput(input);
					}
				} catch (CoreException x) {
					IStatus status= x.getStatus();
					if (status == null || status.getSeverity() != IStatus.CANCEL) {
						title= EditorMessages.Editor_error_refresh_outofsync_title;
						msg= NLSUtility.format(EditorMessages.Editor_error_refresh_outofsync_message, inputName);
						ErrorDialog.openError(shell, title, msg, x.getStatus());
					}
				}
			} else if (!isDirty()) {
				// Trigger dummy change to dirty the editor, for details see https://bugs.eclipse.org/344101 .
				try {
					IDocument document= provider.getDocument(input);
					if (document != null)
						document.replace(0, 0, ""); //$NON-NLS-1$
				} catch (BadLocationException e) {
					// Ignore as this can't happen
				}
			}
		}
	}

	/**
	 * The <code>AbstractTextEditor</code> implementation of this
	 * <code>IEditorPart</code> method calls <code>performSaveAs</code>.
	 * Subclasses may reimplement.
	 */
	public void doSaveAs() {
		/*
		 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in the editors.
		 * Changed Behavior to make sure that if called inside a regular save (because
		 * of deletion of input element) there is a way to report back to the caller.
		 */
		performSaveAs(getProgressMonitor());
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
	 *
	 * @param progressMonitor the progress monitor for communicating result state or <code>null</code>
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
				String title= EditorMessages.Editor_error_save_deleted_title;
				String msg= EditorMessages.Editor_error_save_deleted_message;
				MessageDialog.openError(shell, title, msg);
			}

		} else {
			updateState(getEditorInput());
			validateState(getEditorInput());
			performSave(false, progressMonitor);
		}
	}

	/**
	 * Enables/disables sanity checking.
	 * @param enable <code>true</code> if sanity checking should be enabled, <code>false</code> otherwise
	 * @since 2.0
	 */
	protected void enableSanityChecking(boolean enable) {
		synchronized (this) {
			fIsSanityCheckEnabled= enable;
		}
	}

	/**
	 * Checks the state of the given editor input if sanity checking is enabled.
	 * @param input the editor input whose state is to be checked
	 * @since 2.0
	 */
	protected void safelySanityCheckState(IEditorInput input) {
		boolean enabled= false;

		synchronized (this) {
			enabled= fIsSanityCheckEnabled;
		}

		if (enabled)
			sanityCheckState(input);
	}

	/**
	 * Checks the state of the given editor input.
	 * @param input the editor input whose state is to be checked
	 * @since 2.0
	 */
	protected void sanityCheckState(IEditorInput input) {

		IDocumentProvider p= getDocumentProvider();
		if (p == null)
			return;

		if (p instanceof IDocumentProviderExtension3)  {

			IDocumentProviderExtension3 p3= (IDocumentProviderExtension3) p;

			long stamp= p.getModificationStamp(input);
			if (stamp != fModificationStamp) {
				fModificationStamp= stamp;
				if (!p3.isSynchronized(input))
					handleEditorInputChanged();
			}

		} else  {

			if (fModificationStamp == -1)
				fModificationStamp= p.getSynchronizationStamp(input);

			long stamp= p.getModificationStamp(input);
			if (stamp != fModificationStamp) {
				fModificationStamp= stamp;
				if (stamp != p.getSynchronizationStamp(input))
					handleEditorInputChanged();
			}
		}

		updateState(getEditorInput());
		updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE);
	}

	/**
	 * Enables/disables state validation.
	 * @param enable <code>true</code> if state validation should be enabled, <code>false</code> otherwise
	 * @since 2.1
	 */
	protected void enableStateValidation(boolean enable) {
		synchronized (this) {
			fIsStateValidationEnabled= enable;
		}
	}

	/**
	 * Validates the state of the given editor input. The predominate intent
	 * of this method is to take any action probably necessary to ensure that
	 * the input can persistently be changed.
	 *
	 * @param input the input to be validated
	 * @since 2.0
	 */
	protected void validateState(IEditorInput input) {

		IDocumentProvider provider= getDocumentProvider();
		if (! (provider instanceof IDocumentProviderExtension))
			return;

		IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;

		try {

			extension.validateState(input, getSite().getShell());

		} catch (CoreException x) {
			IStatus status= x.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL) {
				Bundle bundle= Platform.getBundle(PlatformUI.PLUGIN_ID);
				ILog log= Platform.getLog(bundle);
				log.log(x.getStatus());

				Shell shell= getSite().getShell();
				String title= EditorMessages.Editor_error_validateEdit_title;
				String msg= EditorMessages.Editor_error_validateEdit_message;
				ErrorDialog.openError(shell, title, msg, x.getStatus());
			}
			return;
		}

		if (fSourceViewer != null)
			fSourceViewer.setEditable(isEditable());

		updateStateDependentActions();
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension2#validateEditorInputState()
	 * @since 2.1
	 */
	public boolean validateEditorInputState() {

		boolean enabled= false;

		synchronized (this) {
			enabled= fIsStateValidationEnabled;
		}

		if (enabled) {

			ISourceViewer viewer= fSourceViewer;
			if (viewer == null)
				return false;

			fTextInputListener.inputChanged= false;
			viewer.addTextInputListener(fTextInputListener);

			try {
				final IEditorInput input= getEditorInput();
				BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {
					/*
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						validateState(input);
					}
				});
				sanityCheckState(input);
				return !isEditorInputReadOnly() && !fTextInputListener.inputChanged;

			} finally {
				viewer.removeTextInputListener(fTextInputListener);
			}

		}

		return !isEditorInputReadOnly();
	}

	/**
	 * Updates the state of the given editor input such as read-only flag.
	 *
	 * @param input the input to be validated
	 * @since 2.0
	 */
	protected void updateState(IEditorInput input) {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			try {

				boolean wasReadOnly= isEditorInputReadOnly();
				extension.updateStateCache(input);

				if (fSourceViewer != null)
					fSourceViewer.setEditable(isEditable());

				if (wasReadOnly != isEditorInputReadOnly())
					updateStateDependentActions();

			} catch (CoreException x) {
				Bundle bundle= Platform.getBundle(PlatformUI.PLUGIN_ID);
				ILog log= Platform.getLog(bundle);
				log.log(x.getStatus());
			}
		}
	}

	/**
	 * Performs the save and handles errors appropriately.
	 *
	 * @param overwrite indicates whether or not overwriting is allowed
	 * @param progressMonitor the monitor in which to run the operation
	 * @since 3.0
	 */
	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {

		IDocumentProvider provider= getDocumentProvider();
		if (provider == null)
			return;

		try {

			provider.aboutToChange(getEditorInput());
			IEditorInput input= getEditorInput();
			provider.saveDocument(progressMonitor, input, getDocumentProvider().getDocument(input), overwrite);
			editorSaved();

		} catch (CoreException x) {
			IStatus status= x.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL)
				handleExceptionOnSave(x, progressMonitor);
		} finally {
			provider.changed(getEditorInput());
		}
	}

	/**
	 * Handles the given exception. If the exception reports an out-of-sync
	 * situation, this is reported to the user. Otherwise, the exception
	 * is generically reported.
	 *
	 * @param exception the exception to handle
	 * @param progressMonitor the progress monitor
	 */
	protected void handleExceptionOnSave(CoreException exception, IProgressMonitor progressMonitor) {

		try {
			++ fErrorCorrectionOnSave;

			boolean isSynchronized= false;
			IDocumentProvider p= getDocumentProvider();

			if (p instanceof IDocumentProviderExtension3)  {
				IDocumentProviderExtension3 p3= (IDocumentProviderExtension3) p;
				isSynchronized= p3.isSynchronized(getEditorInput());
			} else  {
				long modifiedStamp= p.getModificationStamp(getEditorInput());
				long synchStamp= p.getSynchronizationStamp(getEditorInput());
				isSynchronized= (modifiedStamp == synchStamp);
			}

			if (isNotSynchronizedException(exception) && fErrorCorrectionOnSave == 1 && !isSynchronized) {
				String title= EditorMessages.Editor_error_save_outofsync_title;
				String msg= NLSUtility.format(EditorMessages.Editor_error_save_outofsync_message, getEditorInput().getToolTipText());

				if (MessageDialog.openQuestion(getSite().getShell(), title, msg))
					performSave(true, progressMonitor);
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
				String title= EditorMessages.Editor_error_save_title;
				String msg= EditorMessages.Editor_error_save_message;
				openSaveErrorDialog(title, msg, exception);

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
	 * Presents an error dialog to the user when a problem
	 * happens during save.
	 * <p>
	 * Subclasses can decide to override the given title and message.
	 * </p>
	 *
	 * @param title	the dialog title
	 * @param message the message to display
	 * @param exception the exception to handle
	 * @since 3.3
	 */
	protected void openSaveErrorDialog(String title, String message, CoreException  exception) {
		ErrorDialog.openError(getSite().getShell(), title, message, exception.getStatus());
	}

	/**
	 * Tells whether the given core exception is exactly the
	 * exception which is thrown for a non-synchronized element.
	 *
	 * @param ex the core exception
	 * @return <code>true</code> iff the given core exception is exactly the
	 *			exception which is thrown for a non-synchronized element
	 * @since 3.1
	 */
	private boolean isNotSynchronizedException(CoreException ex) {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension5)
			return ((IDocumentProviderExtension5)provider).isNotSynchronizedException(getEditorInput(), ex);
		return false;
	}

	/**
	 * The <code>AbstractTextEditor</code> implementation of this
	 * <code>IEditorPart</code> method returns <code>false</code>.
	 * Subclasses may override.
	 *
	 * @return <code>false</code>
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * @see EditorPart#isDirty()
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

		performRevert();
	}

	/**
	 * Performs revert and handles errors appropriately.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @since 3.0
	 */
	protected void performRevert() {

		IDocumentProvider provider= getDocumentProvider();
		if (provider == null)
			return;

		try {

			provider.aboutToChange(getEditorInput());
			provider.resetDocument(getEditorInput());
			editorSaved();

		} catch (CoreException x) {
			IStatus status= x.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL ) {
				Shell shell= getSite().getShell();
				String title= EditorMessages.Editor_error_revert_title;
				String msg= EditorMessages.Editor_error_revert_message;
				ErrorDialog.openError(shell, title, msg, x.getStatus());
			}
		} finally {
			provider.changed(getEditorInput());
		}
	}

	/**
	 * Performs any additional action necessary to perform after the input
	 * document's content has been replaced.
	 * <p>
	 * Clients may extended this method.
	 *
	 * @since 3.0
	 */
	protected void handleElementContentReplaced() {
	}

	/*
	 * @see ITextEditor#setAction(String, IAction)
	 */
	public void setAction(String actionID, IAction action) {
		Assert.isNotNull(actionID);
		if (action == null) {
			action= (IAction) fActions.remove(actionID);
			if (action != null)
				fActivationCodeTrigger.unregisterActionFromKeyActivation(action);
		} else {
			if (action.getId() == null)
				action.setId(actionID); // make sure the action ID has been set
			fActions.put(actionID, action);
			fActivationCodeTrigger.registerActionForKeyActivation(action);
		}
	}

	/**
	 * Sets this editor's actions into activated (default) or deactived state.
	 * <p>
	 * XXX: This is called by the Java editor for its breadcrumb feature. We
	 * don't want to make this risky method API because the Java editor
	 * breadcrumb might become a Platform UI feature during 3.5 and hence we can
	 * then delete this workaround.
	 * </p>
	 *
	 * @param state <code>true</code> if activated
	 * @since 3.4
	 */
	private void setActionActivation(boolean state) {
		if (state) {
			fActivationCodeTrigger.install();
			Iterator iter= fActions.values().iterator();
			while (iter.hasNext()) {
				IAction action= (IAction)iter.next();
				if (action != null)
					fActivationCodeTrigger.registerActionForKeyActivation(action);
			}
			getEditorSite().getActionBarContributor().setActiveEditor(this);
		} else {
			getEditorSite().getActionBarContributor().setActiveEditor(null);
			Iterator iter= fActions.values().iterator();
			while (iter.hasNext()) {
				IAction action= (IAction)iter.next();
				if (action != null)
					fActivationCodeTrigger.unregisterActionFromKeyActivation(action);
			}
			fActivationCodeTrigger.uninstall();
		}
	}

	private static final boolean HACK_TO_SUPPRESS_UNUSUED_WARNING= false;
	{
		if (HACK_TO_SUPPRESS_UNUSUED_WARNING)
			setActionActivation(true);
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
	 * @return the registered activation code or <code>null</code> if no code has been installed
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
	 * @see ITextEditor#getAction(String)
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

	/**
	 * Returns the action with the given action id that has been contributed via XML to this editor.
	 * The lookup honors the dependencies of plug-ins.
	 *
	 * @param actionID the action id to look up
	 * @return the action that has been contributed
	 * @since 2.0
	 */
	private IAction findContributedAction(String actionID) {
		List actions= new ArrayList();
		IConfigurationElement[] elements= Platform.getExtensionRegistry().getConfigurationElementsFor(PlatformUI.PLUGIN_ID, "editorActions"); //$NON-NLS-1$
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
		int actionSize= actions.size();
		if (actionSize > 0) {
			IConfigurationElement element;
			if (actionSize > 1) {
				IConfigurationElement[] actionArray= (IConfigurationElement[])actions.toArray(new IConfigurationElement[actionSize]);
				ConfigurationElementSorter sorter= new ConfigurationElementSorter() {
					/*
					 * @see org.eclipse.ui.texteditor.ConfigurationElementSorter#getConfigurationElement(java.lang.Object)
					 */
					public IConfigurationElement getConfigurationElement(Object object) {
						return (IConfigurationElement)object;
					}
				};
				sorter.sort(actionArray);
				element= actionArray[0];
			} else
				element= (IConfigurationElement)actions.get(0);

			try {
				return new ContributedAction(getSite(), element);
			} catch (CommandNotMappedException e) {
				// out of luck, no command action mapping
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
	 * @since 2.0
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
	 * Marks or unmarks the given action to be updated on state changes.
	 *
	 * @param actionId the action id
	 * @param mark <code>true</code> if the action is state dependent
	 * @since 2.0
	 */
	public void markAsStateDependentAction(String actionId, boolean mark) {
		Assert.isNotNull(actionId);
		if (mark) {
			if (!fStateActions.contains(actionId))
				fStateActions.add(actionId);
		} else
			fStateActions.remove(actionId);
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
	 * @since 2.0
	 */
	protected void updatePropertyDependentActions() {
		if (fPropertyActions != null) {
			Iterator e= fPropertyActions.iterator();
			while (e.hasNext())
				updateAction((String) e.next());
		}
	}

	/**
	 * Updates all state dependent actions.
	 * @since 2.0
	 */
	protected void updateStateDependentActions() {
		if (fStateActions != null) {
			Iterator e= fStateActions.iterator();
			while (e.hasNext())
				updateAction((String) e.next());
		}
	}

	/**
	 * Creates action entries for all SWT StyledText actions as defined in
	 * <code>org.eclipse.swt.custom.ST</code>. Overwrites and
	 * extends the list of these actions afterwards.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 * @since 2.0
	 */
	protected void createNavigationActions() {

		IAction action;

		StyledText textWidget= fSourceViewer.getTextWidget();
		for (int i= 0; i < ACTION_MAP.length; i++) {
			IdMapEntry entry= ACTION_MAP[i];
			action= new TextNavigationAction(textWidget, entry.getAction());
			action.setActionDefinitionId(entry.getActionId());
			setAction(entry.getActionId(), action);
		}

		action= new ToggleOverwriteModeAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ToggleOverwriteMode."); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_OVERWRITE);
		setAction(ITextEditorActionDefinitionIds.TOGGLE_OVERWRITE, action);
		textWidget.setKeyBinding(SWT.INSERT, SWT.NULL);

		action=  new ScrollLinesAction(-1);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SCROLL_LINE_UP);
		setAction(ITextEditorActionDefinitionIds.SCROLL_LINE_UP, action);

		action= new ScrollLinesAction(1);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN);
		setAction(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN, action);

		action= new LineEndAction(textWidget, false);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_END);
		setAction(ITextEditorActionDefinitionIds.LINE_END, action);

		action= new LineStartAction(textWidget, false);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_START);
		setAction(ITextEditorActionDefinitionIds.LINE_START, action);

		action= new LineEndAction(textWidget, true);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_END);
		setAction(ITextEditorActionDefinitionIds.SELECT_LINE_END, action);

		action= new LineStartAction(textWidget, true);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_START);
		setAction(ITextEditorActionDefinitionIds.SELECT_LINE_START, action);

		// to accommodate https://bugs.eclipse.org/bugs/show_bug.cgi?id=51516
		// nullify handling of DELETE key by StyledText
		textWidget.setKeyBinding(SWT.DEL, SWT.NULL);
	}

	/**
	 * Creates this editor's accessibility actions.
	 * @since 2.0
	 */
	private void createAccessibilityActions() {
		IAction action= new ShowRulerContextMenuAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_RULER_CONTEXT_MENU);
		setAction(ITextEditorActionDefinitionIds.SHOW_RULER_CONTEXT_MENU, action);
	}

	/**
	 * Creates this editor's undo/redo actions.
	 * <p>
	 * Subclasses may override or extend.</p>
	 *
	 * @since 3.1
	 */
	protected void createUndoRedoActions() {
		IUndoContext undoContext= getUndoContext();
		if (undoContext != null) {
			// Use actions provided by global undo/redo

			// Create the undo action
			OperationHistoryActionHandler undoAction= new UndoActionHandler(getEditorSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(undoAction, IAbstractTextEditorHelpContextIds.UNDO_ACTION);
			undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
			registerUndoRedoAction(ITextEditorActionConstants.UNDO, undoAction);

			// Create the redo action.
			OperationHistoryActionHandler redoAction= new RedoActionHandler(getEditorSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(redoAction, IAbstractTextEditorHelpContextIds.REDO_ACTION);
			redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
			registerUndoRedoAction(ITextEditorActionConstants.REDO, redoAction);

			// Install operation approvers
			IOperationHistory history= OperationHistoryFactory.getOperationHistory();

			// The first approver will prompt when operations affecting outside elements are to be undone or redone.
			if (fNonLocalOperationApprover != null)
				history.removeOperationApprover(fNonLocalOperationApprover);
			fNonLocalOperationApprover= getUndoRedoOperationApprover(undoContext);
			history.addOperationApprover(fNonLocalOperationApprover);

			// The second approver will prompt from this editor when an undo is attempted on an operation
			// and it is not the most recent operation in the editor.
			if (fLinearUndoViolationApprover != null)
				history.removeOperationApprover(fLinearUndoViolationApprover);
			fLinearUndoViolationApprover= new LinearUndoViolationUserApprover(undoContext, this);
			history.addOperationApprover(fLinearUndoViolationApprover);

		} else {
			// Use text operation actions (pre 3.1 style)

			ResourceAction action;

			if (getAction(ITextEditorActionConstants.UNDO) == null) {
				action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Undo.", this, ITextOperationTarget.UNDO); //$NON-NLS-1$
				action.setHelpContextId(IAbstractTextEditorHelpContextIds.UNDO_ACTION);
				action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
				setAction(ITextEditorActionConstants.UNDO, action);
			}

			if (getAction(ITextEditorActionConstants.REDO) == null) {
				action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Redo.", this, ITextOperationTarget.REDO); //$NON-NLS-1$
				action.setHelpContextId(IAbstractTextEditorHelpContextIds.REDO_ACTION);
				action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
				setAction(ITextEditorActionConstants.REDO, action);
			}
		}
	}

	/**
	 * Registers the given undo/redo action under the given ID and ensures that previously installed
	 * actions get disposed. It also takes care of re-registering the new action with the global
	 * action handler.
	 * 
	 * @param actionId the action id under which to register the action
	 * @param action the action to register or <code>null</code> to dispose them
	 * @since 3.1
	 */
	private void registerUndoRedoAction(String actionId, OperationHistoryActionHandler action) {
		IAction oldAction= getAction(actionId);
		if (oldAction instanceof OperationHistoryActionHandler)
			((OperationHistoryActionHandler)oldAction).dispose();

		if (action == null)
			return;

		setAction(actionId, action);

		IActionBars actionBars= getEditorSite().getActionBars();
		if (actionBars != null)
			actionBars.setGlobalActionHandler(actionId, action);
	}

	/**
	 * Return an {@link IOperationApprover} appropriate for approving the undo and
	 * redo of operations that have the specified undo context.
	 * <p>
	 * Subclasses may override.
	 * </p>
	 * @param undoContext	the IUndoContext of operations that should be examined
	 * 						by the operation approver
	 * @return	the <code>IOperationApprover</code> appropriate for approving undo
	 * 			and redo operations inside this editor, or <code>null</code> if no
	 * 			approval is needed
	 * @since 3.1
	 */
	protected IOperationApprover getUndoRedoOperationApprover(IUndoContext undoContext) {
		return new NonLocalUndoUserApprover(undoContext, this, new Object [] { getEditorInput() }, Object.class);
	}

	/**
	 * Creates this editor's standard actions and connects them with the global
	 * workbench actions.
	 * <p>
	 * Subclasses may extend.</p>
	 */
	protected void createActions() {

		ResourceAction action;

		setAction(IWorkbenchCommandConstants.EDIT_CUT, null);
		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Cut.", this, ITextOperationTarget.CUT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
		setAction(ITextEditorActionConstants.CUT, action);

		setAction(IWorkbenchCommandConstants.EDIT_COPY, null);
		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Copy.", this, ITextOperationTarget.COPY, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		setAction(ITextEditorActionConstants.COPY, action);

		setAction(IWorkbenchCommandConstants.EDIT_PASTE, null);
		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Paste.", this, ITextOperationTarget.PASTE); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.PASTE_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		setAction(ITextEditorActionConstants.PASTE, action);

		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Delete.", this, ITextOperationTarget.DELETE); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.DELETE_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
		setAction(ITextEditorActionConstants.DELETE, action);

		action= new DeleteLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.DeleteLine.", this, DeleteLineAction.WHOLE, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.DELETE_LINE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_LINE);
		setAction(ITextEditorActionConstants.DELETE_LINE, action);

		action= new DeleteLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.CutLine.", this, DeleteLineAction.WHOLE, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_LINE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CUT_LINE);
		setAction(ITextEditorActionConstants.CUT_LINE, action);

		action= new DeleteLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.DeleteLineToBeginning.", this, DeleteLineAction.TO_BEGINNING, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.DELETE_LINE_TO_BEGINNING_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_LINE_TO_BEGINNING);
		setAction(ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING, action);

		action= new DeleteLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.CutLineToBeginning.", this, DeleteLineAction.TO_BEGINNING, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_LINE_TO_BEGINNING_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CUT_LINE_TO_BEGINNING);
		setAction(ITextEditorActionConstants.CUT_LINE_TO_BEGINNING, action);

		action= new DeleteLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.DeleteLineToEnd.", this, DeleteLineAction.TO_END, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.DELETE_LINE_TO_END_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_LINE_TO_END);
		setAction(ITextEditorActionConstants.DELETE_LINE_TO_END, action);

		action= new DeleteLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.CutLineToEnd.", this, DeleteLineAction.TO_END, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_LINE_TO_END_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CUT_LINE_TO_END);
		setAction(ITextEditorActionConstants.CUT_LINE_TO_END, action);

		action= new JoinLinesAction(EditorMessages.getBundleForConstructedKeys(), "Editor.JoinLines.", this, " "); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.JOIN_LINES_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.JOIN_LINES);
		setAction(ITextEditorActionConstants.JOIN_LINES, action);

		action= new MarkAction(EditorMessages.getBundleForConstructedKeys(), "Editor.SetMark.", this, MarkAction.SET_MARK); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SET_MARK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SET_MARK);
		setAction(ITextEditorActionConstants.SET_MARK, action);

		action= new MarkAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ClearMark.", this, MarkAction.CLEAR_MARK); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CLEAR_MARK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CLEAR_MARK);
		setAction(ITextEditorActionConstants.CLEAR_MARK, action);

		action= new MarkAction(EditorMessages.getBundleForConstructedKeys(), "Editor.SwapMark.", this, MarkAction.SWAP_MARK); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SWAP_MARK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SWAP_MARK);
		setAction(ITextEditorActionConstants.SWAP_MARK, action);

		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.SelectAll.", this, ITextOperationTarget.SELECT_ALL, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SELECT_ALL_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		setAction(ITextEditorActionConstants.SELECT_ALL, action);

		action= new ShiftAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ShiftRight.", this, ITextOperationTarget.SHIFT_RIGHT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SHIFT_RIGHT_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHIFT_RIGHT);
		setAction(ITextEditorActionConstants.SHIFT_RIGHT, action);

		action= new ShiftAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ShiftRight.", this, ITextOperationTarget.SHIFT_RIGHT) { //$NON-NLS-1$
			public void update() {
				updateForTab();
			}
		};
		setAction(ITextEditorActionConstants.SHIFT_RIGHT_TAB, action);

		action= new ShiftAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ShiftLeft.", this, ITextOperationTarget.SHIFT_LEFT); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SHIFT_LEFT_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHIFT_LEFT);
		setAction(ITextEditorActionConstants.SHIFT_LEFT, action);

		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Print.", this, ITextOperationTarget.PRINT, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.PRINT_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.FILE_PRINT);
		setAction(ITextEditorActionConstants.PRINT, action);

		action= new FindReplaceAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindReplace.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		setAction(ITextEditorActionConstants.FIND, action);

		action= new FindNextAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindNext.", this, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_NEXT_ACTION);
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_NEXT);
		setAction(ITextEditorActionConstants.FIND_NEXT, action);

		action= new FindNextAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindPrevious.", this, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_PREVIOUS_ACTION);
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_PREVIOUS);
		setAction(ITextEditorActionConstants.FIND_PREVIOUS, action);

		action= new IncrementalFindAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindIncremental.", this, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_INCREMENTAL_ACTION);
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_INCREMENTAL);
		setAction(ITextEditorActionConstants.FIND_INCREMENTAL, action);

		action= new IncrementalFindAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindIncrementalReverse.", this, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_INCREMENTAL_REVERSE_ACTION);
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_INCREMENTAL_REVERSE);
		setAction(ITextEditorActionConstants.FIND_INCREMENTAL_REVERSE, action);

		fSaveAction= ActionFactory.SAVE.create(getSite().getWorkbenchWindow());
		setAction(ITextEditorActionConstants.SAVE, fSaveAction);

		action= new RevertToSavedAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Revert.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.REVERT_TO_SAVED_ACTION);
		action.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REVERT);
		setAction(ITextEditorActionConstants.REVERT_TO_SAVED, action);

		action= new GotoLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.GotoLine.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.GOTO_LINE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_GOTO);
		setAction(ITextEditorActionConstants.GOTO_LINE, action);

		action= new MoveLinesAction(EditorMessages.getBundleForConstructedKeys(), "Editor.MoveLinesUp.", this, getSourceViewer(), true, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.MOVE_LINES_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.MOVE_LINES_UP);
		setAction(ITextEditorActionConstants.MOVE_LINE_UP, action);

		action= new MoveLinesAction(EditorMessages.getBundleForConstructedKeys(), "Editor.MoveLinesDown.", this, getSourceViewer(), false, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.MOVE_LINES_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.MOVE_LINES_DOWN);
		setAction(ITextEditorActionConstants.MOVE_LINE_DOWN, action);

		action= new MoveLinesAction(EditorMessages.getBundleForConstructedKeys(), "Editor.CopyLineUp.", this, getSourceViewer(), true, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_LINES_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.COPY_LINES_UP);
		setAction(ITextEditorActionConstants.COPY_LINE_UP, action);

		action= new MoveLinesAction(EditorMessages.getBundleForConstructedKeys(), "Editor.CopyLineDown.", this, getSourceViewer(), false, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_LINES_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.COPY_LINES_DOWN);
		setAction(ITextEditorActionConstants.COPY_LINE_DOWN, action);

		action= new CaseAction(EditorMessages.getBundleForConstructedKeys(), "Editor.UpperCase.", this, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.UPPER_CASE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.UPPER_CASE);
		setAction(ITextEditorActionConstants.UPPER_CASE, action);

		action= new CaseAction(EditorMessages.getBundleForConstructedKeys(), "Editor.LowerCase.", this, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.LOWER_CASE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LOWER_CASE);
		setAction(ITextEditorActionConstants.LOWER_CASE, action);

		action= new InsertLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.SmartEnter.", this, false); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SMART_ENTER_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SMART_ENTER);
		setAction(ITextEditorActionConstants.SMART_ENTER, action);

		action= new InsertLineAction(EditorMessages.getBundleForConstructedKeys(), "Editor.SmartEnterInverse.", this, true); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SMART_ENTER_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SMART_ENTER_INVERSE);
		setAction(ITextEditorActionConstants.SMART_ENTER_INVERSE, action);

		action= new ToggleInsertModeAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ToggleInsertMode."); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.TOGGLE_INSERT_MODE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_INSERT_MODE);
		setAction(ITextEditorActionConstants.TOGGLE_INSERT_MODE, action);

		action= new HippieCompleteAction(EditorMessages.getBundleForConstructedKeys(), "Editor.HippieCompletion.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.HIPPIE_COMPLETION_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.HIPPIE_COMPLETION);
		setAction(ITextEditorActionConstants.HIPPIE_COMPLETION, action);

		action= new ContentAssistAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ContentAssistProposal.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONTENT_ASSIST_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction(ITextEditorActionConstants.CONTENT_ASSIST, action);
		markAsStateDependentAction(ITextEditorActionConstants.CONTENT_ASSIST, true);
		
		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ContentAssistContextInformation.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONTENT_ASSIST_CONTEXT_INFORMATION_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction(ITextEditorActionConstants.CONTENT_ASSIST_CONTEXT_INFORMATION, action);
		markAsStateDependentAction(ITextEditorActionConstants.CONTENT_ASSIST_CONTEXT_INFORMATION, true);

		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.QuickAssist.", this, ISourceViewer.QUICK_ASSIST); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.QUICK_ASSIST_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.QUICK_ASSIST);
		setAction(ITextEditorActionConstants.QUICK_ASSIST, action);
		markAsStateDependentAction(ITextEditorActionConstants.QUICK_ASSIST, true);

		action= new GotoAnnotationAction(this, true);
		setAction(ITextEditorActionConstants.NEXT, action);
		action= new GotoAnnotationAction(this, false);
		setAction(ITextEditorActionConstants.PREVIOUS, action);

		action= new RecenterAction(EditorMessages.getBundleForConstructedKeys(), "Editor.Recenter.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.RECENTER_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.RECENTER);
		setAction(ITextEditorActionConstants.RECENTER, action);

		action= new ShowWhitespaceCharactersAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ShowWhitespaceCharacters.", this, getPreferenceStore()); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SHOW_WHITESPACE_CHARACTERS_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_WHITESPACE_CHARACTERS);
		setAction(ITextEditorActionConstants.SHOW_WHITESPACE_CHARACTERS, action);

		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ShowInformation.", this, ISourceViewer.INFORMATION, true); //$NON-NLS-1$
		action= new InformationDispatchAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ShowInformation.", (TextOperationAction) action); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.SHOW_INFORMATION_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
		setAction(ITextEditorActionConstants.SHOW_INFORMATION, action);

		action= new BlockSelectionModeToggleAction(EditorMessages.getBundleForConstructedKeys(), "Editor.ToggleBlockSelectionMode.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.BLOCK_SELECTION_MODE_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.BLOCK_SELECTION_MODE);
		setAction(ITextEditorActionConstants.BLOCK_SELECTION_MODE, action);

		action= new TextOperationAction(EditorMessages.getBundleForConstructedKeys(), "Editor.OpenHyperlink.", this, HyperlinkManager.OPEN_HYPERLINK, true); //$NON-NLS-1$;
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.OPEN_HYPERLINK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.OPEN_HYPERLINK);
		setAction(ITextEditorActionConstants.OPEN_HYPERLINK, action);

		PropertyDialogAction openProperties= new PropertyDialogAction(
				new IShellProvider() {
					public Shell getShell() {
						return getSite().getShell();
					}
				},
				new ISelectionProvider() {
					public void addSelectionChangedListener(ISelectionChangedListener listener) {
					}
					public ISelection getSelection() {
						return new StructuredSelection(getEditorInput());
					}
					public void removeSelectionChangedListener(ISelectionChangedListener listener) {
					}
					public void setSelection(ISelection selection) {
					}
				});
		openProperties.setActionDefinitionId(IWorkbenchCommandConstants.FILE_PROPERTIES);
		setAction(ITextEditorActionConstants.PROPERTIES, openProperties);

		markAsContentDependentAction(ITextEditorActionConstants.UNDO, true);
		markAsContentDependentAction(ITextEditorActionConstants.REDO, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_NEXT, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_PREVIOUS, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_INCREMENTAL, true);
		markAsContentDependentAction(ITextEditorActionConstants.FIND_INCREMENTAL_REVERSE, true);

		markAsSelectionDependentAction(ITextEditorActionConstants.CUT, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.COPY, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.PASTE, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.DELETE, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.SHIFT_RIGHT, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.SHIFT_RIGHT_TAB, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.UPPER_CASE, true);
		markAsSelectionDependentAction(ITextEditorActionConstants.LOWER_CASE, true);

		markAsPropertyDependentAction(ITextEditorActionConstants.UNDO, true);
		markAsPropertyDependentAction(ITextEditorActionConstants.REDO, true);
		markAsPropertyDependentAction(ITextEditorActionConstants.REVERT_TO_SAVED, true);
		markAsPropertyDependentAction(ITextEditorActionConstants.SAVE, true);

		markAsStateDependentAction(ITextEditorActionConstants.UNDO, true);
		markAsStateDependentAction(ITextEditorActionConstants.REDO, true);
		markAsStateDependentAction(ITextEditorActionConstants.CUT, true);
		markAsStateDependentAction(ITextEditorActionConstants.PASTE, true);
		markAsStateDependentAction(ITextEditorActionConstants.DELETE, true);
		markAsStateDependentAction(ITextEditorActionConstants.SHIFT_RIGHT, true);
		markAsStateDependentAction(ITextEditorActionConstants.SHIFT_RIGHT_TAB, true);
		markAsStateDependentAction(ITextEditorActionConstants.SHIFT_LEFT, true);
		markAsStateDependentAction(ITextEditorActionConstants.FIND, true);
		markAsStateDependentAction(ITextEditorActionConstants.DELETE_LINE, true);
		markAsStateDependentAction(ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING, true);
		markAsStateDependentAction(ITextEditorActionConstants.DELETE_LINE_TO_END, true);
		markAsStateDependentAction(ITextEditorActionConstants.MOVE_LINE_UP, true);
		markAsStateDependentAction(ITextEditorActionConstants.MOVE_LINE_DOWN, true);
		markAsStateDependentAction(ITextEditorActionConstants.CUT_LINE, true);
		markAsStateDependentAction(ITextEditorActionConstants.CUT_LINE_TO_BEGINNING, true);
		markAsStateDependentAction(ITextEditorActionConstants.CUT_LINE_TO_END, true);

		setActionActivationCode(ITextEditorActionConstants.SHIFT_RIGHT_TAB,'\t', -1, SWT.NONE);
		setActionActivationCode(ITextEditorActionConstants.SHIFT_LEFT, '\t', -1, SWT.SHIFT);
	}

	/**
	 * Convenience method to add the action installed under the given action id to the given menu.
	 * @param menu the menu to add the action to
	 * @param actionId the id of the action to be added
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
	 * Convenience method to add the action installed under the given action id to the specified group of the menu.
	 * @param menu the menu to add the action to
	 * @param group the group in the menu
	 * @param actionId the id of the action to add
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
	 * @param menu the menu to add the new group to
	 * @param existingGroup the group after which to insert the new group
	 * @param newGroup the new group
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
	 * Subclasses may extend to add other actions.</p>
	 *
	 * @param menu the menu
	 */
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {

		menu.add(new Separator(ITextEditorActionConstants.GROUP_REST));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		for (Iterator i= fRulerContextMenuListeners.iterator(); i.hasNext();)
			((IMenuListener) i.next()).menuAboutToShow(menu);

		addAction(menu, ITextEditorActionConstants.RULER_MANAGE_BOOKMARKS);
		addAction(menu, ITextEditorActionConstants.RULER_MANAGE_TASKS);
	}

	/**
	 * Sets up this editor's context menu before it is made visible.
	 * <p>
	 * Subclasses may extend to add other actions.</p>
	 *
	 * @param menu the menu
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {

		menu.add(new Separator(ITextEditorActionConstants.GROUP_UNDO));
		menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_SAVE));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_COPY));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_PRINT));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_FIND));
		menu.add(new Separator(IWorkbenchActionConstants.GROUP_ADD));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_REST));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		if (isEditable()) {
			addAction(menu, ITextEditorActionConstants.GROUP_UNDO, ITextEditorActionConstants.UNDO);
			addAction(menu, ITextEditorActionConstants.GROUP_UNDO, ITextEditorActionConstants.REVERT_TO_SAVED);
			addAction(menu, ITextEditorActionConstants.GROUP_SAVE, ITextEditorActionConstants.SAVE);
			addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.CUT);
			addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.COPY);
			addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.PASTE);
			IAction action= getAction(ITextEditorActionConstants.QUICK_ASSIST);
			if (action != null && action.isEnabled())
				addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.QUICK_ASSIST);
		} else {
			addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.COPY);
		}
	}

	/**
	 * Returns the status line manager of this editor.
	 *
	 * @return the status line manager of this editor
	 * @since 2.0, protected since 3.3
	 */
	protected IStatusLineManager getStatusLineManager() {
		return getEditorSite().getActionBars().getStatusLineManager();
	}

	/*
	 * @see IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {

		if (IEditorStatusLine.class.equals(required)) {
			if (fEditorStatusLine == null) {
				IStatusLineManager statusLineManager= getStatusLineManager();
				ISelectionProvider selectionProvider= getSelectionProvider();
				if (statusLineManager != null && selectionProvider != null)
					fEditorStatusLine= new EditorStatusLine(statusLineManager, selectionProvider);
			}
			return fEditorStatusLine;
		}

		if (IVerticalRulerInfo.class.equals(required)) {
			if (fVerticalRuler != null)
				return fVerticalRuler;
		}

		if (IMarkRegionTarget.class.equals(required)) {
			if (fMarkRegionTarget == null) {
				IStatusLineManager manager= getStatusLineManager();
				if (manager != null)
					fMarkRegionTarget= (fSourceViewer == null ? null : new MarkRegionTarget(fSourceViewer, manager));
			}
			return fMarkRegionTarget;
		}

		if (IDeleteLineTarget.class.equals(required)) {
			if (fDeleteLineTarget == null) {
				fDeleteLineTarget= new TextViewerDeleteLineTarget(fSourceViewer);
			}
			return fDeleteLineTarget;
		}

		if (IncrementalFindTarget.class.equals(required)) {
			if (fIncrementalFindTarget == null) {
				IStatusLineManager manager= getStatusLineManager();
				if (manager != null)
					fIncrementalFindTarget= (fSourceViewer == null ? null : new IncrementalFindTarget(fSourceViewer, manager));
			}
			return fIncrementalFindTarget;
		}

		if (IFindReplaceTarget.class.equals(required)) {
			if (fFindReplaceTarget == null) {
				IFindReplaceTarget target= (fSourceViewer == null ? null : fSourceViewer.getFindReplaceTarget());
				if (target != null) {
					fFindReplaceTarget= new FindReplaceTarget(this, target);
					if (fFindScopeHighlightColor != null)
						fFindReplaceTarget.setScopeHighlightColor(fFindScopeHighlightColor);
				}
			}
			return fFindReplaceTarget;
		}

		if (ITextOperationTarget.class.equals(required))
			return (fSourceViewer == null ? null : fSourceViewer.getTextOperationTarget());

		if (IRewriteTarget.class.equals(required)) {
			if (fSourceViewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= (ITextViewerExtension) fSourceViewer;
				return extension.getRewriteTarget();
			}
			return null;
		}

		if (Control.class.equals(required))
			return fSourceViewer != null ? fSourceViewer.getTextWidget() : null;

		if (IColumnSupport.class.equals(required)) {
			if (fColumnSupport == null)
				fColumnSupport= createColumnSupport();
			return fColumnSupport;
		}

		return super.getAdapter(required);
	}

	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (fSourceViewer != null && fSourceViewer.getTextWidget() != null)
			fSourceViewer.getTextWidget().setFocus();
	}

	/*
	 * @see ITextEditor#showsHighlightRangeOnly()
	 */
	public boolean showsHighlightRangeOnly() {
		return fShowHighlightRangeOnly;
	}

	/*
	 * @see ITextEditor#showHighlightRangeOnly(boolean)
	 */
	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
		fShowHighlightRangeOnly= showHighlightRangeOnly;
	}

	/*
	 * @see ITextEditor#setHighlightRange(int, int, boolean)
	 */
	public void setHighlightRange(int offset, int length, boolean moveCursor) {
		if (fSourceViewer == null)
			return;

		if (fShowHighlightRangeOnly) {
			if (moveCursor)
				fSourceViewer.setVisibleRegion(offset, length);
		} else {
			IRegion rangeIndication= fSourceViewer.getRangeIndication();
			if (rangeIndication == null || offset != rangeIndication.getOffset() || length != rangeIndication.getLength())
				fSourceViewer.setRangeIndication(offset, length, moveCursor);
		}
	}

	/*
	 * @see ITextEditor#getHighlightRange()
	 */
	public IRegion getHighlightRange() {
		if (fSourceViewer == null)
			return null;

		if (fShowHighlightRangeOnly)
			return getCoverage(fSourceViewer);

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
	 * is highlighted.
	 * <p>
	 * Subclasses may re-implement this method.</p>
	 *
	 * @param offset the offset of the range which at least should be highlighted
	 * @param length the length of the range which at least should be highlighted
	 */
	protected void adjustHighlightRange(int offset, int length) {
		if (fSourceViewer == null)
			return;

		if (fSourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
			extension.exposeModelRange(new Region(offset, length));
		} else if (!isVisible(fSourceViewer, offset, length)) {
			fSourceViewer.resetVisibleRegion();
		}
	}

	/*
	 * @see ITextEditor#selectAndReveal(int, int)
	 */
	public void selectAndReveal(int start, int length) {
		selectAndReveal(start, length, start, length);
	}

	/**
	 * Selects and reveals the specified ranges in this text editor.
	 *
	 * @param selectionStart the offset of the selection
	 * @param selectionLength the length of the selection
	 * @param revealStart the offset of the revealed range
	 * @param revealLength the length of the revealed range
	 * @since 3.0
	 */
	protected void selectAndReveal(int selectionStart, int selectionLength, int revealStart, int revealLength) {
		if (fSourceViewer == null)
			return;

		ISelection selection= getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) selection;
			if (textSelection.getOffset() != 0 || textSelection.getLength() != 0)
				markInNavigationHistory();
		}

		StyledText widget= fSourceViewer.getTextWidget();
		widget.setRedraw(false);
		{
			adjustHighlightRange(revealStart, revealLength);
			fSourceViewer.revealRange(revealStart, revealLength);

			fSourceViewer.setSelectedRange(selectionStart, selectionLength);

			markInNavigationHistory();
		}
		widget.setRedraw(true);
	}

	/*
	 * @see org.eclipse.ui.INavigationLocationProvider#createNavigationLocation()
	 * @since 2.1
	 */
	public INavigationLocation createEmptyNavigationLocation() {
		return new TextSelectionNavigationLocation(this, false);
	}

	/*
	 * @see org.eclipse.ui.INavigationLocationProvider#createNavigationLocation()
	 */
	public INavigationLocation createNavigationLocation() {
		return new TextSelectionNavigationLocation(this, true);
	}

	/**
	 * Writes a check mark of the given situation into the navigation history.
	 * @since 2.1
	 */
	protected void markInNavigationHistory() {
		getSite().getPage().getNavigationHistory().markLocation(this);
	}

	/**
	 * Hook which gets called when the editor has been saved.
	 * Subclasses may extend.
	 * @since 2.1
	 */
	protected void editorSaved() {
		INavigationLocation[] locations= getSite().getPage().getNavigationHistory().getLocations();
		IEditorInput input= getEditorInput();
		for (int i= 0; i < locations.length; i++) {
			if (locations[i] instanceof TextSelectionNavigationLocation) {
				if(input.equals(locations[i].getInput())) {
					TextSelectionNavigationLocation location= (TextSelectionNavigationLocation) locations[i];
					location.partSaved(this);
				}
			}
		}
	}

	/*
	 * @see WorkbenchPart#firePropertyChange(int)
	 */
	protected void firePropertyChange(int property) {
		super.firePropertyChange(property);
		updatePropertyDependentActions();
	}

	/*
	 * @see ITextEditorExtension#setStatusField(IStatusField, String)
	 * @since 2.0
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

		if (fIncrementalFindTarget != null && ITextEditorActionConstants.STATUS_CATEGORY_FIND_FIELD.equals(category))
			fIncrementalFindTarget.setStatusField(field);
	}

	/**
	 * Returns the current status field for the given status category.
	 *
	 * @param category the status category
	 * @return the current status field for the given status category
	 * @since 2.0
	 */
	protected IStatusField getStatusField(String category) {
		if (category != null && fStatusFields != null)
			return (IStatusField) fStatusFields.get(category);
		return null;
	}

	/**
	 * Returns whether this editor is in overwrite or insert mode.
	 *
	 * @return <code>true</code> if in insert mode, <code>false</code> for overwrite mode
	 * @since 2.0
	 */
	protected boolean isInInsertMode() {
		return !fIsOverwriting;
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension3#getInsertMode()
	 * @since 3.0
	 */
	public InsertMode getInsertMode() {
		return fInsertMode;
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension3#setInsertMode(org.eclipse.ui.texteditor.ITextEditorExtension3.InsertMode)
	 * @since 3.0
	 */
	public void setInsertMode(InsertMode newMode) {
		List legalModes= getLegalInsertModes();
		if (!legalModes.contains(newMode))
			throw new IllegalArgumentException();

		fInsertMode= newMode;

		handleInsertModeChanged();
	}

	/**
	 * Returns the set of legal insert modes. If insert modes are configured all defined insert modes
	 * are legal.
	 *
	 * @return the set of legal insert modes
	 * @since 3.0
	 */
	protected List getLegalInsertModes() {
		if (fLegalInsertModes == null) {
			fLegalInsertModes= new ArrayList();
			fLegalInsertModes.add(SMART_INSERT);
			fLegalInsertModes.add(INSERT);
		}
		return fLegalInsertModes;
	}

	private void switchToNextInsertMode() {

		InsertMode mode= getInsertMode();
		List legalModes= getLegalInsertModes();

		int i= 0;
		while (i < legalModes.size()) {
			if (legalModes.get(i) == mode) break;
			++ i;
		}

		i= (i + 1) % legalModes.size();
		InsertMode newMode= (InsertMode) legalModes.get(i);
		setInsertMode(newMode);
	}

	private void toggleOverwriteMode() {
		if (fIsOverwriteModeEnabled) {
			fIsOverwriting= !fIsOverwriting;
			fSourceViewer.getTextWidget().invokeAction(ST.TOGGLE_OVERWRITE);
			handleInsertModeChanged();
		}
	}

	/**
	 * Configures the given insert mode as legal or illegal. This call is ignored if the set of legal
	 * input modes would be empty after the call.
	 *
	 * @param mode the insert mode to be configured
	 * @param legal <code>true</code> if the given mode is legal, <code>false</code> otherwise
	 * @since 3.0
	 */
	protected void configureInsertMode(InsertMode mode, boolean legal) {
		List legalModes= getLegalInsertModes();
		if (legal) {
			if (!legalModes.contains(mode))
				legalModes.add(mode);
		} else if (legalModes.size() > 1) {
			if (getInsertMode() == mode)
				switchToNextInsertMode();
			legalModes.remove(mode);
		}
	}

	/**
	 * Sets the overwrite mode enablement.
	 *
	 * @param enable <code>true</code> to enable new overwrite mode,
	 *        <code>false</code> to disable
	 * @since 3.0
	 */
	protected void enableOverwriteMode(boolean enable) {
		if (fIsOverwriting && !enable)
			toggleOverwriteMode();
		fIsOverwriteModeEnabled= enable;
	}

	private Caret createOverwriteCaret(StyledText styledText) {
		Caret caret= new Caret(styledText, SWT.NULL);
		GC gc= new GC(styledText);
		// XXX: this overwrite box is not proportional-font aware
		// take 'a' as a medium sized character
		Point charSize= gc.stringExtent("a"); //$NON-NLS-1$

		// XXX: Filed request to get a caret with auto-height: https://bugs.eclipse.org/bugs/show_bug.cgi?id=118612
		caret.setSize(charSize.x, styledText.getLineHeight());
		caret.setFont(styledText.getFont());

		gc.dispose();

		return caret;
	}

	private Caret createInsertCaret(StyledText styledText) {
		Caret caret= new Caret(styledText, SWT.NULL);

		// XXX: Filed request to get a caret with auto-height: https://bugs.eclipse.org/bugs/show_bug.cgi?id=118612
		caret.setSize(getCaretWidthPreference(), styledText.getLineHeight());
		caret.setFont(styledText.getFont());

		return caret;
	}

	private Image createRawInsertModeCaretImage(StyledText styledText) {

		PaletteData caretPalette= new PaletteData(new RGB[] {new RGB (0,0,0), new RGB (255,255,255)});
		int width= getCaretWidthPreference();
		int widthOffset= width - 1;

		// XXX: Filed request to get a caret with auto-height: https://bugs.eclipse.org/bugs/show_bug.cgi?id=118612
		ImageData imageData= new ImageData(4 + widthOffset, styledText.getLineHeight(), 1, caretPalette);

		Display display= styledText.getDisplay();
		Image bracketImage= new Image(display, imageData);
		GC gc= new GC (bracketImage);
		gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.setLineWidth(0); // NOTE: 0 means width is 1 but with optimized performance
		int height= imageData.height / 3;
		// gap between two bars of one third of the height
		// draw boxes using lines as drawing a line of a certain width produces
		// rounded corners.
		for (int i= 0; i < width ; i++) {
			gc.drawLine(i, 0, i, height - 1);
			gc.drawLine(i, imageData.height - height, i, imageData.height - 1);
		}

		gc.dispose();

		return bracketImage;
	}

	private Caret createRawInsertModeCaret(StyledText styledText) {
		// don't draw special raw caret if no smart mode is enabled
		if (!getLegalInsertModes().contains(SMART_INSERT))
			return createInsertCaret(styledText);

		Caret caret= new Caret(styledText, SWT.NULL);
		Image image= createRawInsertModeCaretImage(styledText);
		if (image != null)
			caret.setImage(image);
		else {
			// XXX: Filed request to get a caret with auto-height: https://bugs.eclipse.org/bugs/show_bug.cgi?id=118612
			caret.setSize(getCaretWidthPreference(), styledText.getLineHeight());
		}

		caret.setFont(styledText.getFont());

		return caret;
	}

	private int getCaretWidthPreference() {
		if (getPreferenceStore() != null && getPreferenceStore().getBoolean(PREFERENCE_WIDE_CARET))
			return WIDE_CARET_WIDTH;

		return SINGLE_CARET_WIDTH;
	}

	private void updateCaret() {

		if (fSourceViewer == null)
			return;

		StyledText styledText= fSourceViewer.getTextWidget();

		InsertMode mode= getInsertMode();

		styledText.setCaret(null);
		disposeNonDefaultCaret();

		if (getPreferenceStore() == null || !getPreferenceStore().getBoolean(PREFERENCE_USE_CUSTOM_CARETS))
			Assert.isTrue(fNonDefaultCaret == null);
		else if (fIsOverwriting)
			fNonDefaultCaret= createOverwriteCaret(styledText);
		else if (SMART_INSERT == mode)
			fNonDefaultCaret= createInsertCaret(styledText);
		else if (INSERT == mode)
			fNonDefaultCaret= createRawInsertModeCaret(styledText);

		if (fNonDefaultCaret != null) {
			styledText.setCaret(fNonDefaultCaret);
			fNonDefaultCaretImage= fNonDefaultCaret.getImage();
		} else if (fInitialCaret != styledText.getCaret())
		    styledText.setCaret(fInitialCaret);
	}

	private void disposeNonDefaultCaret() {
		if (fNonDefaultCaretImage != null) {
			fNonDefaultCaretImage.dispose();
			fNonDefaultCaretImage= null;
		}

		if (fNonDefaultCaret != null) {
			fNonDefaultCaret.dispose();
			fNonDefaultCaret= null;
		}
	}

	/**
	 * Handles a change of the editor's insert mode.
	 * Subclasses may extend.
	 *
	 * @since 2.0
	 */
	protected void handleInsertModeChanged() {
		updateInsertModeAction();
		updateCaret();
		updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE);
	}

	private void updateInsertModeAction() {

		// this may be called before the part is fully initialized (see configureInsertMode)
		// drop out in this case.
		if (getSite() == null)
			return;

		IAction action= getAction(ITextEditorActionConstants.TOGGLE_INSERT_MODE);
		if (action != null) {
			action.setEnabled(!fIsOverwriting);
			action.setChecked(fInsertMode == SMART_INSERT);
		}
	}

	/**
	 * Handles a potential change of the cursor position.
	 * Subclasses may extend.
	 *
	 * @since 2.0
	 */
	protected void handleCursorPositionChanged() {
		updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
	}

	/**
	 * Updates the status fields for the given category.
	 *
	 * @param category the category
	 * @since 2.0
	 */
	protected void updateStatusField(String category) {

		if (category == null)
			return;

		IStatusField field= getStatusField(category);
		if (field != null) {

			String text= null;

			if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION.equals(category))
				text= getCursorPosition();
			else if (ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE.equals(category))
				text= isEditorInputReadOnly() ? fReadOnlyLabel : fWritableLabel;
			else if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE.equals(category)) {
				InsertMode mode= getInsertMode();
				if (fIsOverwriting)
					text= fOverwriteModeLabel;
				else if (INSERT == mode)
					text= fInsertModeLabel;
				else if (SMART_INSERT == mode)
					text= fSmartInsertModeLabel;
			}

			field.setText(text == null ? fErrorLabel : text);
		}
	}

	/**
	 * Updates all status fields.
	 *
	 * @since 2.0
	 */
	protected void updateStatusFields() {
		if (fStatusFields != null) {
			Iterator e= fStatusFields.keySet().iterator();
			while (e.hasNext())
				updateStatusField((String) e.next());
		}
	}

	/**
	 * Returns a description of the cursor position.
	 *
	 * @return a description of the cursor position
	 * @since 2.0
	 */
	protected String getCursorPosition() {

		if (fSourceViewer == null)
			return fErrorLabel;

		StyledText styledText= fSourceViewer.getTextWidget();
		int caret= widgetOffset2ModelOffset(fSourceViewer, styledText.getCaretOffset());
		IDocument document= fSourceViewer.getDocument();

		if (document == null)
			return fErrorLabel;

		try {

			int line= document.getLineOfOffset(caret);

			int lineOffset= document.getLineOffset(line);
			int tabWidth= styledText.getTabs();
			int column= 0;
			for (int i= lineOffset; i < caret; i++)
				if ('\t' == document.getChar(i))
					column += tabWidth - (tabWidth == 0 ? 0 : column % tabWidth);
				else
					column++;

			fLineLabel.fValue= line + 1;
			fColumnLabel.fValue= column + 1;
			return NLSUtility.format(fPositionLabelPattern, fPositionLabelPatternArguments);

		} catch (BadLocationException x) {
			return fErrorLabel;
		}
	}

	/*
	 * @see ITextEditorExtension#isEditorInputReadOnly()
	 * @since 2.0
	 */
	public boolean isEditorInputReadOnly() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			return extension.isReadOnly(getEditorInput());
		}
		return true;
	}

	/*
	 * @see ITextEditorExtension2#isEditorInputModifiable()
	 * @since 2.1
	 */
	public boolean isEditorInputModifiable() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			return extension.isModifiable(getEditorInput());
		}
		return true;
	}

	/*
	 * @see ITextEditorExtension#addRulerContextMenuListener(IMenuListener)
	 * @since 2.0
	 */
	public void addRulerContextMenuListener(IMenuListener listener) {
		fRulerContextMenuListeners.add(listener);
	}

	/*
	 * @see ITextEditorExtension#removeRulerContextMenuListener(IMenuListener)
	 * @since 2.0
	 */
	public void removeRulerContextMenuListener(IMenuListener listener) {
		fRulerContextMenuListeners.remove(listener);
	}

	/**
	 * Returns whether this editor can handle the move of the original element
	 * so that it ends up being the moved element. By default this method
	 * returns <code>true</code>. Subclasses may reimplement.
	 *
	 * @param originalElement the original element
	 * @param movedElement the moved element
	 * @return whether this editor can handle the move of the original element
	 *         so that it ends up being the moved element
	 * @since 2.0
	 */
	protected boolean canHandleMove(IEditorInput originalElement, IEditorInput movedElement) {
		return true;
	}

	/**
	 * Returns the offset of the given source viewer's document that corresponds
	 * to the given widget offset or <code>-1</code> if there is no such offset.
	 *
	 * @param viewer the source viewer
	 * @param widgetOffset the widget offset
	 * @return the corresponding offset in the source viewer's document or <code>-1</code>
	 * @since 2.1
	 */
	protected static final int widgetOffset2ModelOffset(ISourceViewer viewer, int widgetOffset) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.widgetOffset2ModelOffset(widgetOffset);
		}
		return widgetOffset + viewer.getVisibleRegion().getOffset();
	}

	/**
	 * Returns the offset of the given source viewer's text widget that corresponds
	 * to the given model offset or <code>-1</code> if there is no such offset.
	 *
	 * @param viewer the source viewer
	 * @param modelOffset the model offset
	 * @return the corresponding offset in the source viewer's text widget or <code>-1</code>
	 * @since 3.0
	 */
	protected static final int modelOffset2WidgetOffset(ISourceViewer viewer, int modelOffset) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.modelOffset2WidgetOffset(modelOffset);
		}
		return modelOffset - viewer.getVisibleRegion().getOffset();
	}

	/**
	 * Returns the minimal region of the given source viewer's document that completely
	 * comprises everything that is visible in the viewer's widget.
	 *
	 * @param viewer the viewer go return the coverage for
	 * @return the minimal region of the source viewer's document comprising the contents of the viewer's widget
	 * @since 2.1
	 */
	protected static final IRegion getCoverage(ISourceViewer viewer) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.getModelCoverage();
		}
		return viewer.getVisibleRegion();
	}

	/**
	 * Tells whether the given region is visible in the given source viewer.
	 *
	 * @param viewer the source viewer
	 * @param offset the offset of the region
	 * @param length the length of the region
	 * @return <code>true</code> if visible
	 * @since 2.1
	 */
	protected static final boolean isVisible(ISourceViewer viewer, int offset, int length) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion overlap= extension.modelRange2WidgetRange(new Region(offset, length));
			return overlap != null;
		}
		return viewer.overlapsWithVisibleRegion(offset, length);
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension3#showChangeInformation(boolean)
	 * @since 3.0
	 */
	public void showChangeInformation(boolean show) {
		// do nothing
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension3#isChangeInformationShowing()
	 * @since 3.0
	 */
	public boolean isChangeInformationShowing() {
		return false;
	}

	/**
	 * Sets the given message as error message to this editor's status line.
	 *
	 * @param message message to be set
	 * @since 3.2
	 */
	protected void setStatusLineErrorMessage(String message) {
		IEditorStatusLine statusLine= (IEditorStatusLine)getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(true, message, null);
	}

	/**
	 * Sets the given message as message to this editor's status line.
	 *
	 * @param message message to be set
	 * @since 3.2
	 */
	protected void setStatusLineMessage(String message) {
		IEditorStatusLine statusLine= (IEditorStatusLine)getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(false, message, null);
	}

	/**
	 * Jumps to the next annotation according to the given direction.
	 *
	 * @param forward <code>true</code> if search direction is forward, <code>false</code> if backward
	 * @return the selected annotation or <code>null</code> if none
	 * @see #isNavigationTarget(Annotation)
	 * @see #findAnnotation(int, int, boolean, Position)
	 * @since 3.2
	 */
	public Annotation gotoAnnotation(boolean forward) {
		ITextSelection selection= (ITextSelection) getSelectionProvider().getSelection();
		Position position= new Position(0, 0);
		Annotation annotation= findAnnotation(selection.getOffset(), selection.getLength(), forward, position);
		setStatusLineErrorMessage(null);
		setStatusLineMessage(null);

		if (annotation != null) {
			selectAndReveal(position.getOffset(), position.getLength());
			setStatusLineMessage(annotation.getText());
		}
		return annotation;
	}

	/**
	 * Returns the annotation closest to the given range respecting the given
	 * direction. If an annotation is found, the annotations current position
	 * is copied into the provided annotation position.
	 *
	 * @param offset the region offset
	 * @param length the region length
	 * @param forward <code>true</code> for forwards, <code>false</code> for backward
	 * @param annotationPosition the position of the found annotation
	 * @return the found annotation
	 * @since 3.2
	 */
	protected Annotation findAnnotation(final int offset, final int length, boolean forward, Position annotationPosition) {

		Annotation nextAnnotation= null;
		Position nextAnnotationPosition= null;
		Annotation containingAnnotation= null;
		Position containingAnnotationPosition= null;
		boolean currentAnnotation= false;

		IDocument document= getDocumentProvider().getDocument(getEditorInput());
		int endOfDocument= document.getLength();
		int distance= Integer.MAX_VALUE;

		IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			if (!isNavigationTarget(a))
				continue;

			Position p= model.getPosition(a);
			if (p == null)
				continue;

			if (forward && p.offset == offset || !forward && p.offset + p.getLength() == offset + length) {// || p.includes(offset)) {
				if (containingAnnotation == null || (forward && p.length >= containingAnnotationPosition.length || !forward && p.length >= containingAnnotationPosition.length)) {
					containingAnnotation= a;
					containingAnnotationPosition= p;
					currentAnnotation= p.length == length;
				}
			} else {
				int currentDistance= 0;

				if (forward) {
					currentDistance= p.getOffset() - offset;
					if (currentDistance < 0)
						currentDistance= endOfDocument + currentDistance;

					if (currentDistance < distance || currentDistance == distance && p.length < nextAnnotationPosition.length) {
						distance= currentDistance;
						nextAnnotation= a;
						nextAnnotationPosition= p;
					}
				} else {
					currentDistance= offset + length - (p.getOffset() + p.length);
					if (currentDistance < 0)
						currentDistance= endOfDocument + currentDistance;

					if (currentDistance < distance || currentDistance == distance && p.length < nextAnnotationPosition.length) {
						distance= currentDistance;
						nextAnnotation= a;
						nextAnnotationPosition= p;
					}
				}
			}
		}
		if (containingAnnotationPosition != null && (!currentAnnotation || nextAnnotation == null)) {
			annotationPosition.setOffset(containingAnnotationPosition.getOffset());
			annotationPosition.setLength(containingAnnotationPosition.getLength());
			return containingAnnotation;
		}
		if (nextAnnotationPosition != null) {
			annotationPosition.setOffset(nextAnnotationPosition.getOffset());
			annotationPosition.setLength(nextAnnotationPosition.getLength());
		}

		return nextAnnotation;
	}

	/**
	 * Returns whether the given annotation is configured as a target for the
	 * "Go to Next/Previous Annotation" actions.
	 * <p>
	 * Per default every annotation is a target.
	 * </p>
	 *
	 * @param annotation the annotation
	 * @return <code>true</code> if this is a target, <code>false</code> otherwise
	 * @since 3.2
	 */
	protected boolean isNavigationTarget(Annotation annotation) {
		return true;
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension4#showRevisionInformation(org.eclipse.jface.text.revisions.RevisionInformation, java.lang.String)
	 * @since 3.2
	 */
	public void showRevisionInformation(RevisionInformation info, String quickDiffProviderId) {
		// no implementation
	}

	/*
	 * @see org.eclipse.ui.IEditorPersistable#restoreState(org.eclipse.ui.IMemento)
	 * @since 3.3
	 */
	public void restoreState(IMemento memento) {
		fMementoToRestore= memento;
	}

	/*
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 * @since 3.3
	 */
	public void saveState(IMemento memento) {
		ISelection selection= doGetSelection();
		if (selection instanceof ITextSelection) {
			memento.putInteger(TAG_SELECTION_OFFSET, ((ITextSelection)selection).getOffset());
			memento.putInteger(TAG_SELECTION_LENGTH, ((ITextSelection)selection).getLength());
		}
		final StyledText textWidget= fSourceViewer.getTextWidget();
		memento.putInteger(TAG_SELECTION_TOP_PIXEL, textWidget.getTopPixel());
		memento.putInteger(TAG_SELECTION_HORIZONTAL_PIXEL, textWidget.getHorizontalPixel());
	}

	/**
	 * Returns whether the given memento contains saved state
	 * <p>
	 * Subclasses may extend or override this method.</p>
	 *
	 * @param memento the saved state of this editor
	 * @return <code>true</code> if the given memento contains saved state
	 * @since 3.3
	 */
	protected boolean containsSavedState(IMemento memento) {
		return memento.getInteger(TAG_SELECTION_OFFSET) != null && memento.getInteger(TAG_SELECTION_LENGTH) != null;
	}

	/**
	 * Restores this editor's state using the given memento.
	 * <p>
	 * Subclasses may extend or override this method.</p>
	 *
	 * @param memento the saved state of this editor
	 * @since 3.3
	 */
	protected void doRestoreState(IMemento memento) {
		Integer offset= memento.getInteger(TAG_SELECTION_OFFSET);
		if (offset == null)
			return;

		Integer length= memento.getInteger(TAG_SELECTION_LENGTH);
		if (length == null)
			return;

		doSetSelection(new TextSelection(offset.intValue(), length.intValue()));

		final StyledText textWidget= fSourceViewer.getTextWidget();

		Integer topPixel= memento.getInteger(TAG_SELECTION_TOP_PIXEL);
		if (topPixel != null)
			textWidget.setTopPixel(topPixel.intValue());

		Integer horizontalPixel= memento.getInteger(TAG_SELECTION_HORIZONTAL_PIXEL);
		if (horizontalPixel != null)
			textWidget.setHorizontalPixel(horizontalPixel.intValue());
	}

	/*
	 * @see org.eclipse.ui.ISaveablesSource#getSaveables()
	 * @since 3.3
	 */
	public Saveable[] getSaveables() {
		if (fSavable == null)
			fSavable= new TextEditorSavable(this);

		return new Saveable[] { fSavable };
	}

	/*
	 * @see org.eclipse.ui.ISaveablesSource#getActiveSaveables()
	 * @since 3.3
	 */
	public Saveable[] getActiveSaveables() {
		return getSaveables();
	}

	/**
	 * This text editor's savable.
	 *
	 * @since 3.3
	 */
	protected static class TextEditorSavable extends Saveable {

		/** The cached editor. */
		private ITextEditor fTextEditor;
		/** The cached editor input. */
		private IEditorInput fEditorInput;
		/** The cached document. */
		private IDocument fDocument;

		/**
		 * Creates a new savable for this text editor.
		 *
		 * @param textEditor the text editor
		 */
		public TextEditorSavable(ITextEditor textEditor) {
			Assert.isLegal(textEditor != null);
			fTextEditor= textEditor;
			fEditorInput= fTextEditor.getEditorInput();
			Assert.isLegal(fEditorInput != null);
		}

		/**
		 * Disconnects the editor from this savable.
		 */
		public void disconnectEditor() {
			getAdapter(IDocument.class); // make sure the document is cached
			fTextEditor= null;
		}

		/*
		 * @see org.eclipse.ui.Saveable#getName()
		 */
		public String getName() {
			return fEditorInput.getName();
		}

		/*
		 * @see org.eclipse.ui.Saveable#getToolTipText()
		 */
		public String getToolTipText() {
			return fEditorInput.getToolTipText();
		}

		/*
		 * @see org.eclipse.ui.Saveable#getImageDescriptor()
		 */
		public ImageDescriptor getImageDescriptor() {
			return fEditorInput.getImageDescriptor();
		}

		/*
		 * @see org.eclipse.ui.Saveable#doSave(org.eclipse.core.runtime.IProgressMonitor)
		 * @since 3.3
		 */
		public void doSave(IProgressMonitor monitor) throws CoreException {
			fTextEditor.doSave(monitor);
		}

		public boolean isDirty() {
			return fTextEditor.isDirty();
		}

		/*
		 * @see org.eclipse.ui.Saveable#supportsBackgroundSave()
		 */
		public boolean supportsBackgroundSave() {
			return false;
		}

		/*
		 * @see org.eclipse.ui.Saveable#hashCode()
		 */
		public int hashCode() {
			Object document= getAdapter(IDocument.class);
			if (document == null)
				return 0;
			return document.hashCode();
		}

		/*
		 * @see org.eclipse.ui.Saveable#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (!(obj instanceof Saveable))
				return false;

			Object thisDocument= getAdapter(IDocument.class);
			Object otherDocument= ((Saveable)obj).getAdapter(IDocument.class);

			if (thisDocument == null && otherDocument == null)
				return true;

			return thisDocument != null && thisDocument.equals(otherDocument);
		}

		/**
		 * Explicit comment needed to suppress wrong warning caused by
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4848177
		 *
		 * @see org.eclipse.ui.Saveable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter == IDocument.class) {
				if (fDocument == null) {
					IDocumentProvider documentProvider= fTextEditor.getDocumentProvider();
					if (documentProvider != null)
						fDocument= documentProvider.getDocument(fEditorInput);
				}
				return fDocument;
			}
			return super.getAdapter(adapter);
		}
	}

	//---- Tabs to spaces conversion support ------------------

	/**
	 * Installs a tabs to spaces converter.
	 *
	 * <p>Subclasses may extend or override this method.</p>
	 *
	 * @since 3.3
	 */
	protected void installTabsToSpacesConverter() {
		SourceViewerConfiguration config= getSourceViewerConfiguration();
		if (config != null && fSourceViewer instanceof ITextViewerExtension7) {
			int tabWidth= config.getTabWidth(fSourceViewer);
			TabsToSpacesConverter tabToSpacesConverter= new TabsToSpacesConverter();
			tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
			tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);
			((ITextViewerExtension7)fSourceViewer).setTabsToSpacesConverter(tabToSpacesConverter);
			updateIndentPrefixes();
		}
	}

	/**
	 * Installs a tabs to spaces converter.
	 *
	 * <p>Subclasses may extend or override this method.</p>
	 *
	 * @since 3.3
	 */
	protected void uninstallTabsToSpacesConverter() {
		if (fSourceViewer instanceof ITextViewerExtension7) {
			((ITextViewerExtension7)fSourceViewer).setTabsToSpacesConverter(null);
			if (fSourceViewer.getTextWidget() != null)
				updateIndentPrefixes();
		}
	}

	/**
	 * Tells whether tabs should be converted to
	 * spaces while editing inside this editor.
	 *
	 * <p>Subclasses may override this method.</p>
	 *
	 * @return <code>true</code> if tabs should be converted to spaces
	 * @since 3.3
	 */
	protected boolean isTabsToSpacesConversionEnabled() {
		return false;
	}

	/**
	 * Updates the source viewer's indent prefixes with
	 * the values provided by the source viewer configuration.
	 *
	 * @since 3.3
	 */
	protected final void updateIndentPrefixes() {
		SourceViewerConfiguration configuration= getSourceViewerConfiguration();
		String[] types= configuration.getConfiguredContentTypes(fSourceViewer);
		for (int i= 0; i < types.length; i++) {
			String[] prefixes= configuration.getIndentPrefixes(fSourceViewer, types[i]);
			if (prefixes != null && prefixes.length > 0)
				fSourceViewer.setIndentPrefixes(prefixes, types[i]);
		}
	}

	/**
	 * Tells whether selection mode is supported.
	 * <p>
	 * By default block selection mode is supported. Subclasses may override this method to disable
	 * it.
	 * </p>
	 * 
	 * @return <code>true</code> if block selection mode is supported, <code>false</code> otherwise
	 * @since 3.5
	 */
	protected boolean isBlockSelectionModeSupported() {
		return true;
	}

	/**
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension5#isBlockSelectionModeEnabled()
	 * @since 3.5
	 */
	public final boolean isBlockSelectionModeEnabled() {
		ISourceViewer viewer= getSourceViewer();
		if (viewer != null) {
			StyledText styledText= viewer.getTextWidget();
			if (styledText != null)
				return styledText.getBlockSelection();
		}
		return false;
	}

	/**
	 * @see org.eclipse.ui.texteditor.ITextEditorExtension5#setBlockSelectionMode(boolean)
	 * @since 3.5
	 */
	public void setBlockSelectionMode(boolean enable) {
		if (!isBlockSelectionModeSupported())
			return;

		ISourceViewer viewer= getSourceViewer();
		if (viewer != null) {
			StyledText styledText= viewer.getTextWidget();
			if (styledText != null) {
				/*
				 * Font switching. block selection mode needs a monospace font.
				 *  - set the font _before enabling_ block selection mode in order to maintain the
				 * selection
				 * - revert the font _after disabling_ block selection mode in order to maintain the
				 * selection
				 */
				if (enable) {
					Font blockFont= JFaceResources.getFont(BLOCK_SELECTION_MODE_FONT);
					Font normalFont= styledText.getFont();
					if (!blockFont.equals(normalFont) && !normalFont.getFontData()[0].equals(blockFont.getFontData()[0])) {
						setFont(viewer, blockFont);
						disposeFont();
						updateCaret();
					}
				}

				styledText.setBlockSelection(enable);
				
				if (!enable) {
					initializeViewerFont(viewer);
					updateCaret();
				}
			}
		}
	}
}
