package org.eclipse.jface.text.reconciler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Listener;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.util.Assert;



/**
 * Standard implementation of <code>IReconciler</code>. The reconciler
 * listens to input document changes of as well as document changes of
 * the input document of the text viewer it is installed on. Depending on 
 * its configuration it manages the received change notifications in a 
 * queue folding neighboring or overlapping changes together. After the
 * configured period of time, it processes one dirty region after the other.
 * A reconciler is started using its <code>install</code> method. It is the
 * clients responsibility to stop a reconciler using its <code>uninstall</code>
 * method. Unstopped reconcilers do not free their resources.<p>
 * Usually, clients instantiate this class and configure it before using it.
 *
 * @see IReconciler
 * @see IDocumentListener
 * @see ITextInputListener
 * @see DirtyRegion
 */
public class Reconciler implements IReconciler {

	
	/**
	 * Background thread for the periodic reconciling activity.
	 */
	class BackgroundThread extends Thread {
		
		/** Has the reconciler been canceled */
		private boolean fCanceled= false;
		/** Has the reconciler been reset */
		private boolean fReset= false;
		/** Has a change been applied */
		private boolean fIsDirty= false;
		/** Is a reconciling strategy active */
		private boolean fIsActive= false;
		
		/**
		 * Creates a new background thread. The thread 
		 * runs with minimal priority.
		 *
		 * @param name the thread's name
		 */
		public BackgroundThread(String name) {
			super(name);
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}
		
		/**
		 * Returns whether a reconciling strategy is active right now.
		 *
		 * @return <code>true</code> if a activity is active
		 */
		public boolean isActive() {
			return fIsActive;
		}
		
		/**
		 * Cancels the background thread.
		 */
		public void cancel() {
			fCanceled= true;
			synchronized (fDirtyRegionQueue) {
				fDirtyRegionQueue.notifyAll();
			}
		}
		
		/**
		 * Reset the background thread as the text viewer has been changed,
		 */
		public void reset() {
			
			if (fDelay > 0) {
				
				synchronized (this) {
					fIsDirty= true;
					fReset= true;
				}
				
			} else {
				
				synchronized(this) {
					fIsDirty= true;
				}
				
				synchronized (fDirtyRegionQueue) {
					fDirtyRegionQueue.notifyAll();
				}
			}
		}
		
		/**
		 * The periodic activity. Wait until there is something in the
		 * queue managing the changes applied to the text viewer. Remove the
		 * first change from the queue and process it.
		 */
		public void run() {
			while (!fCanceled) {
				
				synchronized (fDirtyRegionQueue) {
					try {
						fDirtyRegionQueue.wait(fDelay);
					} catch (InterruptedException x) {
					}
				}
					
				if (fCanceled)
					break;
					
				if (!fIsDirty)
					continue;
					
				if (fReset) {
					synchronized (this) {
						fReset= false;
					}
					continue;
				}
				
				DirtyRegion r= null;
				synchronized (fDirtyRegionQueue) {
					r= fDirtyRegionQueue.removeNextDirtyRegion();
				}
					
				fIsActive= true;
				
				process(r);
				synchronized (this) {
					fIsDirty= false;
				}
				
				fIsActive= false;
			}
		}
	};
	
	/**
	 * Internal document listener and text input listener.
	 */
	class Listener implements IDocumentListener, ITextInputListener {
		
		/*
		 * @see IDocumentListener#documentAboutToBeChanged
		 */
		public void documentAboutToBeChanged(DocumentEvent e) {
		}
		
		/*
		 * @see IDocumentListener#documentChanged
		 */
		public void documentChanged(DocumentEvent e) {
			if (fIsIncrementalReconciler)
				createDirtyRegion(e);
			fThread.reset();
		}
		
		/*
		 * @see ITextInputListener#inputDocumentAboutToBeChanged
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			
			if (oldInput == fDocument) {
				
				if (fDocument != null)
					fDocument.removeDocumentListener(this);
					
				if (fIsIncrementalReconciler) {
					fDirtyRegionQueue.purgeQueue();
					if (fDocument != null && fDocument.getLength() > 0) {
						DocumentEvent e= new DocumentEvent(fDocument, 0, fDocument.getLength(), null);
						createDirtyRegion(e);
					}
				}
				
				fDocument= null;
			}
		}
		
		/*
		 * @see ITextInputListener#inputDocumentChanged
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			
			if (newInput == null)
				return;
				
			fDocument= newInput;
			setDocumentToReconcilingStrategies(fDocument);
				
			fDocument.addDocumentListener(this);
				
			if (fIsIncrementalReconciler) {
				DocumentEvent e= new DocumentEvent(fDocument, 0, 0, fDocument.get());
				createDirtyRegion(e);
			}
			
			fThread.reset();
		}
	};
	
	/** Queue to manage the changes applied to the text viewer */
	private DirtyRegionQueue fDirtyRegionQueue;
	/** The background thread */
	private BackgroundThread fThread;
	/** Internal document and text input listener */
	private Listener fListener;
	/** The background thread delay */
	private int fDelay= 500;
	/** Are there incremental reconciling strategies? */
	private boolean fIsIncrementalReconciler= true;
	/** The map of reconciling strategies */
	private Map fStrategies;

	/** The text viewer's document */
	private IDocument fDocument;
	/** The text viewer */
	private ITextViewer fViewer;
	
	
	/**
	 * Creates a new reconciler with the following configuration: it is
	 * an incremental reconciler which kicks in every 500 ms. There are no
	 * predefined reconciling strategies.
	 */ 
	public Reconciler() {
		super();
	}
		
	/**
	 * Tells the reconciler how long it should collect text changes before
	 * it activates the appropriate reconciling strategies.
	 *
	 * @param delay the duration in milli seconds of a change collection period.
	 */
	public void setDelay(int delay) {
		fDelay= delay;
	}
	
	/**
	 * Tells the reconciler whether any of the available reconciling strategies
	 * is interested in getting detailed dirty region information or just in the
	 * fact the the document has been changed. In the second case, the reconciling 
	 * can not incrementally be pursued.
	 *
	 * @param isIncremental indicates whether this reconciler will be configured with
	 *		incremental reconciling strategies
	 *
	 * @see DirtyRegion
	 * @see IReconcilingStrategy
	 */
	public void setIsIncrementalReconciler(boolean isIncremental) {
		fIsIncrementalReconciler= isIncremental;
	}
	
	/**
	 * Registers a given reconciling strategy for a particular content type.
	 * If there is already a strategy registered for this type, the new strategy 
	 * is registered instead of the old one.
	 *
	 * @param strategy the reconciling strategy to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	public void setReconcilingStrategy(IReconcilingStrategy strategy, String contentType) {
		
		Assert.isNotNull(contentType);
					
		if (fStrategies == null)
			fStrategies= new HashMap();
		
		if (strategy == null)
			fStrategies.remove(contentType);
		else
			fStrategies.put(contentType, strategy);
	}
	
	/*
	 * @see IReconciler#install
	 */
	public void install(ITextViewer textViewer) {
		
		Assert.isNotNull(textViewer);
		
		fViewer= textViewer;
		
		fListener= new Listener();
		fViewer.addTextInputListener(fListener);
		
		fDirtyRegionQueue= new DirtyRegionQueue();
		fThread= new BackgroundThread(getClass().getName());
		fThread.start();
	}
	
	/*
	 * @see IReconciler#uninstall
	 */
	public void uninstall() {
		if (fListener != null) {
			fViewer.removeTextInputListener(fListener);
			fListener= null;
			fThread.cancel();
			fThread= null;
		}
	}
	
	/*
	 * @see IReconciler#getReconcilingStrategy
	 */
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		
		Assert.isNotNull(contentType);
		
		if (fStrategies == null)
			return null;
						
		return (IReconcilingStrategy) fStrategies.get(contentType);
	}
	
		
	/*
	 * Creates a dirty region for a document event and adds it to the queue.
	 *
	 * @param e the document event for which to create a dirty region
	 */
	private void createDirtyRegion(DocumentEvent e) {
		
		if (e.getLength() == 0 && e.getText() != null) {
			// Insert
			fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getText().length(), DirtyRegion.INSERT, e.getText()));
				
		} else if (e.getText() == null || e.getText().length() == 0) {
			// Remove
			fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getLength(), DirtyRegion.REMOVE, null));
				
		} else {
			// Replace (Remove + Insert)
			fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getLength(), DirtyRegion.REMOVE, null));
			fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getText().length(), DirtyRegion.INSERT, e.getText()));
		}
	}
		
	/**
	 * Flushs the dirty-region queue to sync up any background activity.
	 */
	private void flushDirtyRegionQueue() {
		while (fDirtyRegionQueue.getSize() > 0 || fThread.isActive()) {
			synchronized (fDirtyRegionQueue) {
				fDirtyRegionQueue.notifyAll();
			}
			Thread.currentThread().yield();
		}
	}
	
	/**
	 * Processes a dirty region. If the dirty region is <code>null</code> the whole
	 * document is consider being dirty. The dirty region is partitioned by the
	 * document and each partition is handed over to a reconciling strategy registered
	 * for the partition's content type.
	 *
	 * @param dirtyRegion the dirty region to be processed
	 */
	private void process(DirtyRegion dirtyRegion) {
		
		IRegion region= dirtyRegion;
		
		if (region == null)
			region= new Region(0, fDocument.getLength());
			
		ITypedRegion[] regions= null;
		try {
			regions= fDocument.computePartitioning(region.getOffset(), region.getLength());
		} catch (BadLocationException x) {
			regions= new TypedRegion[0];
		}
		
		for (int i= 0; i < regions.length; i++) {
			ITypedRegion r= regions[i];
			IReconcilingStrategy s= getReconcilingStrategy(r.getType());
			if (s == null)
				continue;
				
			if(dirtyRegion != null)
				s.reconcile(dirtyRegion, r);
			else
				s.reconcile(r);
		}
	}
	
	/**
	 * Informs all registed reconciling strategies about the new document
	 * they are supoosed to work on.
	 *
	 * @param document the new document
	 */
	private void setDocumentToReconcilingStrategies(IDocument document) {
		if (fStrategies != null) {
			Iterator e= fStrategies.values().iterator();
			while (e.hasNext()) {
				IReconcilingStrategy strategy= (IReconcilingStrategy) e.next();
				strategy.setDocument(document);
			}
		}
	}	
}
