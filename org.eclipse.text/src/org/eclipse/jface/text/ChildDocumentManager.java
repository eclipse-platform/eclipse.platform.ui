package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */




/**
 * Manages a set of child documents for given parent documents. 
 * A child document represents a particular range of the parent 
 * document and is accordingly adapted to changes of the parent document. 
 * Vice versa, the parent document is accordingly adapted to changes of
 * its child documents. The manager does not maintain any particular management
 * structure but utilizes mechanisms given by <code>IDocument</code> such
 * as position categories and position updaters. <p>
 *
 * For internal use only.
 */
public final class ChildDocumentManager implements IDocumentListener {
	
	
	/** 
	 * Name of the position categories used to keep track of the child
	 * documents offset ranges into the parent document.
	 */
	public final static String CHILDDOCUMENTS= "__childdocuments"; //$NON-NLS-1$
	
	
	/**
	 * Positions which are used to mark the child documents offset ranges into
	 * the parent documents. This position uses as bidirectional reference as
	 * it knows the child document as well as the parent document.
	 */
	static class ChildPosition extends Position {
		
		public IDocument fParentDocument;
		public ChildDocument fChildDocument;
		
		public ChildPosition(IDocument parentDocument, int offset, int length) {
			super(offset, length);
			fParentDocument= parentDocument;
		}
		
		/**
		 * Changed to be compatible to the position updater behavior
		 * @see Position#overlapsWith(int, int)
		 */
		public boolean overlapsWith(int offset, int length) {
			boolean append= (offset == this.offset + this.length) && length == 0;
			return append || super.overlapsWith(offset, length);
		}
	};	
	
	
	/**
	 * The position updater used to adapt the positions representing
	 * the child document ranges to changes of the parent document.
	 */
	static class ChildPositionUpdater extends DefaultPositionUpdater {
		
		/**
		 * Creates the position updated.
		 */
		protected ChildPositionUpdater() {
			super(CHILDDOCUMENTS);
		}
		
		/**
		 * Child document ranges cannot be deleted other then by calling
		 * freeChildDocument.
		 */
		protected boolean notDeleted() {
			return true;
		}
		
		/**
		 * If an insertion happens at a child document's start offset, the
		 * position is extended rather than shifted. Also, if something is added 
		 * right behind the end of the position, the position is extended rather
		 * than kept stable.
		 */
		protected void adaptToInsert() {
			
			int myStart= fPosition.offset;
			int myEnd=   fPosition.offset + fPosition.length;
			myEnd= Math.max(myStart, myEnd);
			
			int yoursStart= fOffset;
			int yoursEnd=   fOffset + fReplaceLength -1;
			yoursEnd= Math.max(yoursStart, yoursEnd);
			
			if (myEnd < yoursStart)
				return;
			
			if (myStart <= yoursStart)
				fPosition.length += fReplaceLength;
			else
				fPosition.offset += fReplaceLength;
		}
	};
	
	/**
	 * The child document partitioner uses the parent document to answer all questions.
	 */
	static class ChildPartitioner implements IDocumentPartitioner {
		
		protected ChildDocument fChildDocument;
		protected IDocument fParentDocument;
		
		protected ChildPartitioner() {
		}
		
		/*
		 * @see IDocumentPartitioner#getPartition(int)
		 */
		public ITypedRegion getPartition(int offset) {
			try {
				offset += fChildDocument.getParentDocumentRange().getOffset();
				return fParentDocument.getPartition(offset);
			} catch (BadLocationException x) {
			}
			
			return null;
		}
		
		/*
		 * @see IDocumentPartitioner#computePartitioning(int, int)
		 */
		public ITypedRegion[] computePartitioning(int offset, int length) {
			try {
				offset += fChildDocument.getParentDocumentRange().getOffset();
				return fParentDocument.computePartitioning(offset, length);
			} catch (BadLocationException x) {
			}
			
			return null;
		}
		
		/*
		 * @see IDocumentPartitioner#getContentType(int)
		 */
		public String getContentType(int offset) {
			try {
				offset += fChildDocument.getParentDocumentRange().getOffset();
				return fParentDocument.getContentType(offset);
			} catch (BadLocationException x) {
			}
			
			return null;
		}
		
		/*
		 * @see IDocumentPartitioner#getLegalContentTypes()
		 */
		public String[] getLegalContentTypes() {
			return fParentDocument.getLegalContentTypes();
		}
		
		/*
		 * @see IDocumentPartitioner#documentChanged(DocumentEvent)
		 */
		public boolean documentChanged(DocumentEvent event) {
			// ignore as the parent does this for us
			return false;
		}
		
		/*
		 * @see IDocumentPartitioner#documentAboutToBeChanged(DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			// ignore as the parent does this for us
		}
		
		/*
		 * @see IDocumentPartitioner#disconnect()
		 */
		public void disconnect() {
			fChildDocument= null;
			fParentDocument= null;
		}
		
		/*
		 * @see IDocumentPartitioner#connect(IDocument)
		 */
		public void connect(IDocument childDocument) {
			Assert.isTrue(childDocument instanceof ChildDocument);
			fChildDocument= (ChildDocument) childDocument;
			fParentDocument= fChildDocument.getParentDocument();
		}	
	};
	
	
	
	/** The position updater shared by all documents which have child documents */
	private IPositionUpdater fChildPositionUpdater;
	
	
	
	/**
	 * Returns the child position updater. If necessary, it is dynamically created.
	 *
	 * @return the child position updater
	 */
	protected IPositionUpdater getChildPositionUpdater() {
		if (fChildPositionUpdater == null)
			fChildPositionUpdater= new ChildPositionUpdater();
		return fChildPositionUpdater;
	}
	
	/**
	 * Creates and returns a new child document for the specified range of the given parent document.
	 * The created child document is initialized with a child document partitioner.
	 *
	 * @param parent the parent document
	 * @param offset the offset of the parent document range
	 * @param length the length of the parent document range
	 * @exception BadLocationException if the specified range is invalid in the parent document
	 */
	 public ChildDocument createChildDocument(IDocument parent, int offset, int length) throws BadLocationException {
	 	
		if (!parent.containsPositionCategory(CHILDDOCUMENTS)) {
			parent.addPositionCategory(CHILDDOCUMENTS);
			parent.addPositionUpdater(getChildPositionUpdater());
			parent.addDocumentListener(this);
		}
		
		ChildPosition pos= new ChildPosition(parent, offset, length);
		try {
			parent.addPosition(CHILDDOCUMENTS, pos);
		} catch (BadPositionCategoryException x) {
			// cannot happen
		}
		
		ChildDocument child= new ChildDocument(parent, pos);
		IDocumentPartitioner partitioner= new ChildPartitioner();
		child.setDocumentPartitioner(partitioner);
		partitioner.connect(child);
		
		pos.fChildDocument= child;
		
		return child;
	}
	
	/**
	 * Disconnects the given child document from it's parent document and frees 
	 * all resources which are no longer needed.
	 *
	 * @param childDocument the child document to be freed
	 */
	public void freeChildDocument(ChildDocument childDocument) {
		
		childDocument.getDocumentPartitioner().disconnect();
		
		ChildPosition pos= (ChildPosition) childDocument.getParentDocumentRange();
		IDocument parent= pos.fParentDocument;
		
		try {
			parent.removePosition(CHILDDOCUMENTS, pos);
			Position[] category= parent.getPositions(CHILDDOCUMENTS);
			if (category.length == 0) {
				parent.removeDocumentListener(this);
				parent.removePositionUpdater(getChildPositionUpdater());
				parent.removePositionCategory(CHILDDOCUMENTS);
			}
		} catch (BadPositionCategoryException x) {
			// cannot happen
		}
	}
	
	/**
	 * Informs all child documents of the document which issued this document event.
	 *
	 * @param about indicates whether the change is about to happen or alread happend
	 * @param event the document event which will be processed to inform child documents
	 */
	protected void fireDocumentEvent(boolean about, DocumentEvent event) {
		try {
			
			IDocument parent= event.getDocument();
			Position[] children= parent.getPositions(CHILDDOCUMENTS);
			for (int i= 0; i < children.length; i++) {
				Object o= children[i];
				if (o instanceof ChildPosition) {
					ChildPosition pos= (ChildPosition) o;
					if (about)
						pos.fChildDocument.parentDocumentAboutToBeChanged(event);
					else
						pos.fChildDocument.parentDocumentChanged(event);
				}
			}
		} catch (BadPositionCategoryException x) {
			// cannot happen
		}
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		fireDocumentEvent(false, event);
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		fireDocumentEvent(true, event);
	}
}