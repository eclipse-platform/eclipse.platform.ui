/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.link;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.IDocumentExtension.IReplace;


/**
 * The model for linked mode, umbrellas several
 * {@link LinkedPositionGroup}s. Once installed, the model
 * propagates any changes to a position to all its siblings in the same position
 * group.
 * <p>
 * Setting up a model consists of first adding
 * <code>LinkedPositionGroup</code>s to it, and then installing the
 * model by either calling {@link #forceInstall()} or
 * {@link #tryInstall()}. After installing the model, it becomes
 * <em>sealed</em> and no more groups may be added.
 * </p>
 * <p>
 * If a document change occurs that would modify more than one position
 * group or that would invalidate the disjointness requirement of the positions,
 * the model is torn down and all positions are deleted. The same happens
 * upon calling {@link #exit(int)}.
 * </p>
 * <h4>Nesting</h4>
 * <p>
 * A <code>LinkedModeModel</code> may be nested into another model. This
 * happens when installing a model the positions of which all fit into a
 * single position in a parent model that has previously been installed on
 * the same document(s).
 * </p>
 * <p>
 * Clients may instantiate instances of this class.
 * </p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LinkedModeModel {

	/**
	 * Checks whether there is already a model installed on <code>document</code>.
	 *
	 * @param document the <code>IDocument</code> of interest
	 * @return <code>true</code> if there is an existing model, <code>false</code>
	 *         otherwise
	 */
	public static boolean hasInstalledModel(IDocument document) {
		// if there is a manager, there also is a model
		return LinkedModeManager.hasManager(document);
	}

	/**
	 * Checks whether there is already a linked mode model installed on any of
	 * the <code>documents</code>.
	 *
	 * @param documents the <code>IDocument</code>s of interest
	 * @return <code>true</code> if there is an existing model, <code>false</code>
	 *         otherwise
	 */
	public static boolean hasInstalledModel(IDocument[] documents) {
		// if there is a manager, there also is a model
		return LinkedModeManager.hasManager(documents);
	}

	/**
	 * Cancels any linked mode model on the specified document. If there is no
	 * model, nothing happens.
	 *
	 * @param document the document whose <code>LinkedModeModel</code> should
	 * 		  be canceled
	 */
	public static void closeAllModels(IDocument document) {
		LinkedModeManager.cancelManager(document);
	}

	/**
	 * Returns the model currently active on <code>document</code> at
	 * <code>offset</code>, or <code>null</code> if there is none.
	 *
	 * @param document the document for which the caller asks for a
	 *        model
	 * @param offset the offset into <code>document</code>, as there may be
	 *        several models on a document
	 * @return the model currently active on <code>document</code>, or
	 *         <code>null</code>
	 */
	public static LinkedModeModel getModel(IDocument document, int offset) {
		if (!hasInstalledModel(document))
			return null;

		LinkedModeManager mgr= LinkedModeManager.getLinkedManager(new IDocument[] {document}, false);
		if (mgr != null)
			return mgr.getTopEnvironment();
		return null;
	}

	/**
	 * Encapsulates the edition triggered by a change to a linking position. Can
	 * be applied to a document as a whole.
	 */
	private class Replace implements IReplace {

		/** The edition to apply on a document. */
		private TextEdit fEdit;

		/**
		 * Creates a new instance.
		 *
		 * @param edit the edition to apply to a document.
		 */
		public Replace(TextEdit edit) {
			fEdit= edit;
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentExtension.IReplace#perform(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocumentListener)
		 */
		public void perform(IDocument document, IDocumentListener owner) throws RuntimeException, MalformedTreeException {
			document.removeDocumentListener(owner);
			fIsChanging= true;
			try {
				fEdit.apply(document, TextEdit.UPDATE_REGIONS | TextEdit.CREATE_UNDO);
			} catch (BadLocationException e) {
				/* XXX: perform should really throw a BadLocationException
				 *		see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52950
				 */
				throw new RuntimeException(e);
			} finally {
				document.addDocumentListener(owner);
				fIsChanging= false;
			}
		}

	}

	/**
	 * The document listener triggering the linked updating of positions
	 * managed by this model.
	 */
	private class DocumentListener implements IDocumentListener {

		private boolean fExit= false;

		/**
		 * Checks whether <code>event</code> occurs within any of the positions
		 * managed by this model. If not, the linked mode is left.
		 *
		 * @param event {@inheritDoc}
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			// don't react on changes executed by the parent model
			if (fParentEnvironment != null && fParentEnvironment.isChanging())
				return;

			for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
				LinkedPositionGroup group= (LinkedPositionGroup) it.next();
				if (!group.isLegalEvent(event)) {
					fExit= true;
					return;
				}
			}
		}

		/**
		 * Propagates a change to a linked position to all its sibling positions.
		 *
		 * @param event {@inheritDoc}
		 */
		public void documentChanged(DocumentEvent event) {
			if (fExit) {
				LinkedModeModel.this.exit(ILinkedModeListener.EXTERNAL_MODIFICATION);
				return;
			}
			fExit= false;

			// don't react on changes executed by the parent model
			if (fParentEnvironment != null && fParentEnvironment.isChanging())
				return;

			// collect all results
			Map result= null;
			for (Iterator it= fGroups.iterator(); it.hasNext();) {
				LinkedPositionGroup group= (LinkedPositionGroup) it.next();

				Map map= group.handleEvent(event);
				if (result != null && map != null) {
					// exit if more than one position was changed
					LinkedModeModel.this.exit(ILinkedModeListener.EXTERNAL_MODIFICATION);
					return;
				}
				if (map != null)
					result= map;
			}

			if (result != null) {
				// edit all documents
				for (Iterator it2= result.keySet().iterator(); it2.hasNext(); ) {
					IDocument doc= (IDocument) it2.next();
					TextEdit edit= (TextEdit) result.get(doc);
					Replace replace= new Replace(edit);

					// apply the edition, either as post notification replace
					// on the calling document or directly on any other
					// document
					if (doc == event.getDocument()) {
						if (doc instanceof IDocumentExtension) {
							((IDocumentExtension) doc).registerPostNotificationReplace(this, replace);
						} else {
							// ignore - there is no way we can log from JFace text...
						}
					} else {
						replace.perform(doc, this);
					}
				}
			}
		}

	}

	/** The set of linked position groups. */
	private final List fGroups= new ArrayList();
	/** The set of documents spanned by this group. */
	private final Set fDocuments= new HashSet();
	/** The position updater for linked positions. */
	private final IPositionUpdater fUpdater= new InclusivePositionUpdater(getCategory());
	/** The document listener on the documents affected by this model. */
	private final DocumentListener fDocumentListener= new DocumentListener();
	/** The parent model for a hierarchical set up, or <code>null</code>. */
	private LinkedModeModel fParentEnvironment;
	/**
	 * The position in <code>fParentEnvironment</code> that includes all
	 * positions in this object, or <code>null</code> if there is no parent
	 * model.
	 */
	private LinkedPosition fParentPosition= null;
	/**
	 * A model is sealed once it has children - no more positions can be
	 * added.
	 */
	private boolean fIsSealed= false;
	/** <code>true</code> when this model is changing documents. */
	private boolean fIsChanging= false;
	/** The linked listeners. */
	private final List fListeners= new ArrayList();
	/** Flag telling whether we have exited: */
	private boolean fIsActive= true;
	/**
	 * The sequence of document positions as we are going to iterate through
	 * them.
	 */
	private List fPositionSequence= new ArrayList();

	/**
	 * Whether we are in the process of editing documents (set by <code>Replace</code>,
	 * read by <code>DocumentListener</code>.
	 *
	 * @return <code>true</code> if we are in the process of editing a
	 *         document, <code>false</code> otherwise
	 */
	private boolean isChanging() {
		return fIsChanging || fParentEnvironment != null && fParentEnvironment.isChanging();
	}

	/**
	 * Throws a <code>BadLocationException</code> if <code>group</code>
	 * conflicts with this model's groups.
	 *
	 * @param group the group being checked
	 * @throws BadLocationException if <code>group</code> conflicts with this
	 *         model's groups
	 */
	private void enforceDisjoint(LinkedPositionGroup group) throws BadLocationException {
		for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
			LinkedPositionGroup g= (LinkedPositionGroup) it.next();
			g.enforceDisjoint(group);
		}
	}

	/**
	 * Causes this model to exit. Called either if an illegal document change
	 * is detected, or by the UI.
	 *
	 * @param flags the exit flags as defined in {@link ILinkedModeListener}
	 */
	public void exit(int flags) {
		if (!fIsActive)
			return;

		fIsActive= false;

		for (Iterator it= fDocuments.iterator(); it.hasNext(); ) {
			IDocument doc= (IDocument) it.next();
			try {
				doc.removePositionCategory(getCategory());
			} catch (BadPositionCategoryException e) {
				// won't happen
				Assert.isTrue(false);
			}
			doc.removePositionUpdater(fUpdater);
			doc.removeDocumentListener(fDocumentListener);
		}

		fDocuments.clear();
		fGroups.clear();

		List listeners= new ArrayList(fListeners);
		fListeners.clear();
		for (Iterator it= listeners.iterator(); it.hasNext(); ) {
			ILinkedModeListener listener= (ILinkedModeListener) it.next();
			listener.left(this, flags);
		}


		if (fParentEnvironment != null)
			fParentEnvironment.resume(flags);
	}

	/**
	 * Causes this model to stop forwarding updates. The positions are not
	 * unregistered however, which will only happen when <code>exit</code>
	 * is called, or after the next document change.
	 *
	 * @param flags the exit flags as defined in {@link ILinkedModeListener}
	 * @since 3.1
	 */
	public void stopForwarding(int flags) {
		fDocumentListener.fExit= true;
	}

	/**
	 * Puts <code>document</code> into the set of managed documents. This
	 * involves registering the document listener and adding our position
	 * category.
	 *
	 * @param document the new document
	 */
	private void manageDocument(IDocument document) {
		if (!fDocuments.contains(document)) {
			fDocuments.add(document);
			document.addPositionCategory(getCategory());
			document.addPositionUpdater(fUpdater);
			document.addDocumentListener(fDocumentListener);
		}

	}

	/**
	 * Returns the position category used by this model.
	 *
	 * @return the position category used by this model
	 */
	private String getCategory() {
		return toString();
	}

	/**
	 * Adds a position group to this <code>LinkedModeModel</code>. This
	 * method may not be called if the model has been installed. Also, if
	 * a UI has been set up for this model, it may not pick up groups
	 * added afterwards.
	 * <p>
	 * If the positions in <code>group</code> conflict with any other group in
	 * this model, a <code>BadLocationException</code> is thrown. Also,
	 * if this model is nested inside another one, all positions in all
	 * groups of the child model have to reside within a single position in the
	 * parent model, otherwise a <code>BadLocationException</code> is thrown.
	 * </p>
	 * <p>
	 * If <code>group</code> already exists, nothing happens.
	 * </p>
	 *
	 * @param group the group to be added to this model
	 * @throws BadLocationException if the group conflicts with the other groups
	 *         in this model or violates the nesting requirements.
	 * @throws IllegalStateException if the method is called when the
	 *         model is already sealed
	 */
	public void addGroup(LinkedPositionGroup group) throws BadLocationException {
		if (group == null)
			throw new IllegalArgumentException("group may not be null"); //$NON-NLS-1$
		if (fIsSealed)
			throw new IllegalStateException("model is already installed"); //$NON-NLS-1$
		if (fGroups.contains(group))
			// nothing happens
			return;

		enforceDisjoint(group);
		group.seal();
		fGroups.add(group);
	}

	/**
	 * Creates a new model.
	 * @since 3.1
	 */
	public LinkedModeModel() {
	}

	/**
	 * Installs this model, which includes registering as document
	 * listener on all involved documents and storing global information about
	 * this model. Any conflicting model already present will be
	 * closed.
	 * <p>
	 * If an exception is thrown, the installation failed and
	 * the model is unusable.
	 * </p>
	 *
	 * @throws BadLocationException if some of the positions of this model
	 *         were not valid positions on their respective documents
	 */
	public void forceInstall() throws BadLocationException {
		if (!install(true))
			Assert.isTrue(false);
	}

	/**
	 * Installs this model, which includes registering as document
	 * listener on all involved documents and storing global information about
	 * this model. If there is another model installed on the
	 * document(s) targeted by the receiver that conflicts with it, installation
	 * may fail.
	 * <p>
	 * The return value states whether installation was
	 * successful; if not, the model is not installed and will not work.
	 * </p>
	 *
	 * @return <code>true</code> if installation was successful,
	 *         <code>false</code> otherwise
	 * @throws BadLocationException if some of the positions of this model
	 *         were not valid positions on their respective documents
	 */
	public boolean tryInstall() throws BadLocationException {
		return install(false);
	}

	/**
	 * Installs this model, which includes registering as document
	 * listener on all involved documents and storing global information about
	 * this model. The return value states whether installation was
	 * successful; if not, the model is not installed and will not work.
	 * The return value can only then become <code>false</code> if
	 * <code>force</code> was set to <code>false</code> as well.
	 *
	 * @param force if <code>true</code>, any other model that cannot
	 *        coexist with this one is canceled; if <code>false</code>,
	 *        install will fail when conflicts occur and return false
	 * @return <code>true</code> if installation was successful,
	 *         <code>false</code> otherwise
	 * @throws BadLocationException if some of the positions of this model
	 *         were not valid positions on their respective documents
	 */
	private boolean install(boolean force) throws BadLocationException {
		if (fIsSealed)
			throw new IllegalStateException("model is already installed"); //$NON-NLS-1$
		enforceNotEmpty();

		IDocument[] documents= getDocuments();
		LinkedModeManager manager= LinkedModeManager.getLinkedManager(documents, force);
		// if we force creation, we require a valid manager
		Assert.isTrue(!(force && manager == null));
		if (manager == null)
			return false;

		if (!manager.nestEnvironment(this, force))
			if (force)
				Assert.isTrue(false);
			else
				return false;

		// we set up successfully. After this point, exit has to be called to
		// remove registered listeners...
		fIsSealed= true;
		if (fParentEnvironment != null)
			fParentEnvironment.suspend();

		// register positions
		try {
			for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
	            LinkedPositionGroup group= (LinkedPositionGroup) it.next();
	            group.register(this);
	        }
			return true;
		} catch (BadLocationException e){
			// if we fail to add, make sure to release all listeners again
			exit(ILinkedModeListener.NONE);
			throw e;
		}
	}

	/**
	 * Asserts that there is at least one linked position in this linked mode
	 * model, throws an IllegalStateException otherwise.
	 */
	private void enforceNotEmpty() {
        boolean hasPosition= false;
		for (Iterator it= fGroups.iterator(); it.hasNext(); )
			if (!((LinkedPositionGroup) it.next()).isEmpty()) {
				hasPosition= true;
				break;
			}
		if (!hasPosition)
			throw new IllegalStateException("must specify at least one linked position"); //$NON-NLS-1$

    }

    /**
	 * Collects all the documents that contained positions are set upon.
     * @return the set of documents affected by this model
     */
    private IDocument[] getDocuments() {
    	Set docs= new HashSet();
        for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
            LinkedPositionGroup group= (LinkedPositionGroup) it.next();
            docs.addAll(Arrays.asList(group.getDocuments()));
        }
        return (IDocument[]) docs.toArray(new IDocument[docs.size()]);
    }

    /**
     * Returns whether the receiver can be nested into the given <code>parent</code>
     * model. If yes, the parent model and its position that the receiver
     * fits in are remembered.
     *
     * @param parent the parent model candidate
     * @return <code>true</code> if the receiver can be nested into <code>parent</code>, <code>false</code> otherwise
     */
    boolean canNestInto(LinkedModeModel parent) {
    	for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
			LinkedPositionGroup group= (LinkedPositionGroup) it.next();
			if (!enforceNestability(group, parent)) {
				fParentPosition= null;
				return false;
			}
		}

    	Assert.isNotNull(fParentPosition);
    	fParentEnvironment= parent;
    	return true;
    }

    /**
	 * Called by nested models when a group is added to them. All
	 * positions in all groups of a nested model have to fit inside a
	 * single position in the parent model.
	 *
	 * @param group the group of the nested model to be adopted.
	 * @param model the model to check against
	 * @return <code>false</code> if it failed to enforce nestability
	 */
	private boolean enforceNestability(LinkedPositionGroup group, LinkedModeModel model) {
		Assert.isNotNull(model);
		Assert.isNotNull(group);

		try {
			for (Iterator it= model.fGroups.iterator(); it.hasNext(); ) {
				LinkedPositionGroup pg= (LinkedPositionGroup) it.next();
				LinkedPosition pos;
				pos= pg.adopt(group);
				if (pos != null && fParentPosition != null && fParentPosition != pos)
					return false; // group does not fit into one parent position, which is illegal
				else if (fParentPosition == null && pos != null)
					fParentPosition= pos;
			}
		} catch (BadLocationException e) {
			return false;
		}

		// group must fit into exactly one of the parent's positions
		return fParentPosition != null;
	}

	/**
	 * Returns whether this model is nested.
	 *
	 * <p>
	 * This method is part of the private protocol between
	 * <code>LinkedModeUI</code> and <code>LinkedModeModel</code>.
	 * </p>
	 *
	 * @return <code>true</code> if this model is nested,
	 *         <code>false</code> otherwise
	 */
	public boolean isNested() {
		return fParentEnvironment != null;
	}

	/**
	 * Returns the positions in this model that have a tab stop, in the
	 * order they were added.
	 *
	 * <p>
	 * This method is part of the private protocol between
	 * <code>LinkedModeUI</code> and <code>LinkedModeModel</code>.
	 * </p>
	 *
	 * @return the positions in this model that have a tab stop, in the
	 *         order they were added
	 */
	public List getTabStopSequence() {
		return fPositionSequence;
	}

	/**
	 * Adds <code>listener</code> to the set of listeners that are informed
	 * upon state changes.
	 *
	 * @param listener the new listener
	 */
	public void addLinkingListener(ILinkedModeListener listener) {
		Assert.isNotNull(listener);
		if (!fListeners.contains(listener))
			fListeners.add(listener);
	}

	/**
	 * Removes <code>listener</code> from the set of listeners that are
	 * informed upon state changes.
	 *
	 * @param listener the new listener
	 */
	public void removeLinkingListener(ILinkedModeListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Finds the position in this model that is closest after
	 * <code>toFind</code>. <code>toFind</code> needs not be a position in
	 * this model and serves merely as an offset.
	 *
	 * <p>
	 * This method part of the private protocol between
	 * <code>LinkedModeUI</code> and <code>LinkedModeModel</code>.
	 * </p>
	 *
	 * @param toFind the position to search from
	 * @return the closest position in the same document as <code>toFind</code>
	 *         after the offset of <code>toFind</code>, or <code>null</code>
	 */
	public LinkedPosition findPosition(LinkedPosition toFind) {
		LinkedPosition position= null;
		for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
			LinkedPositionGroup group= (LinkedPositionGroup) it.next();
			position= group.getPosition(toFind);
			if (position != null)
				break;
		}
		return position;
	}

	/**
	 * Registers a <code>LinkedPosition</code> with this model. Called
	 * by <code>PositionGroup</code>.
	 *
	 * @param position the position to register
	 * @throws BadLocationException if the position cannot be added to its
	 *         document
	 */
	void register(LinkedPosition position) throws BadLocationException {
		Assert.isNotNull(position);

		IDocument document= position.getDocument();
		manageDocument(document);
		try {
			document.addPosition(getCategory(), position);
		} catch (BadPositionCategoryException e) {
			// won't happen as the category has been added by manageDocument()
			Assert.isTrue(false);
		}
		int seqNr= position.getSequenceNumber();
		if (seqNr != LinkedPositionGroup.NO_STOP) {
			fPositionSequence.add(position);
		}
	}

	/**
	 * Suspends this model.
	 */
	private void suspend() {
		List l= new ArrayList(fListeners);
		for (Iterator it= l.iterator(); it.hasNext(); ) {
			ILinkedModeListener listener= (ILinkedModeListener) it.next();
			listener.suspend(this);
		}
	}

	/**
	 * Resumes this model. <code>flags</code> can be <code>NONE</code>
	 * or <code>SELECT</code>.
	 *
	 * @param flags <code>NONE</code> or <code>SELECT</code>
	 */
	private void resume(int flags) {
		List l= new ArrayList(fListeners);
		for (Iterator it= l.iterator(); it.hasNext(); ) {
			ILinkedModeListener listener= (ILinkedModeListener) it.next();
			listener.resume(this, flags);
		}
	}

	/**
	 * Returns whether an offset is contained by any position in this
	 * model.
	 *
	 * @param offset the offset to check
	 * @return <code>true</code> if <code>offset</code> is included by any
	 *         position (see {@link LinkedPosition#includes(int)}) in this
	 *         model, <code>false</code> otherwise
	 */
	public boolean anyPositionContains(int offset) {
		for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
			LinkedPositionGroup group= (LinkedPositionGroup) it.next();
			if (group.contains(offset))
				// take the first hit - exclusion is guaranteed by enforcing
				// disjointness when adding positions
				return true;
		}
		return false;
	}

	/**
	 * Returns the linked position group that contains <code>position</code>,
	 * or <code>null</code> if <code>position</code> is not contained in any
	 * group within this model. Group containment is tested by calling
	 * <code>group.contains(position)</code> for every <code>group</code> in
	 * this model.
	 *
	 * <p>
	 * This method part of the private protocol between
	 * <code>LinkedModeUI</code> and <code>LinkedModeModel</code>.
	 * </p>
	 *
	 * @param position the position the group of which is requested
	 * @return the first group in this model for which
	 *         <code>group.contains(position)</code> returns <code>true</code>,
	 *         or <code>null</code> if no group contains <code>position</code>
	 */
	public LinkedPositionGroup getGroupForPosition(Position position) {
		for (Iterator it= fGroups.iterator(); it.hasNext(); ) {
			LinkedPositionGroup group= (LinkedPositionGroup) it.next();
			if (group.contains(position))
				return group;
		}
		return null;
	}
}
