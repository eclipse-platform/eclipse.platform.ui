package org.eclipse.jface.text.reconciler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Listener;

import org.eclipse.core.runtime.IProgressMonitor;

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
 * Abstract implementation of <code>IReconciler</code>. The reconciler
 * listens to input document changes as well as changes of
 * the input document of the text viewer it is installed on. Depending on 
 * its configuration it manages the received change notifications in a 
 * queue folding neighboring or overlapping changes together. After the
 * configured period of time, it processes one dirty region after the other.
 * A reconciler is started using its <code>install</code> method. It is the
 * clients responsibility to stop a reconciler using its <code>uninstall</code>
 * method. Unstopped reconcilers do not free their resources.<p>
 * It is subclass responsibility to specify how dirty regions are processed.
 *
 * @see IReconciler
 * @see IDocumentListener
 * @see ITextInputListener
 * @see DirtyRegion
 */
abstract public class AbstractReconciler implements IReconciler {

	
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
            
            // http://bugs.eclipse.org/bugs/show_bug.cgi?id=19525
            reconcilerReset();
            
		}
		
		/**
		 * The periodic activity. Wait until there is something in the
		 * queue managing the changes applied to the text viewer. Remove the
		 * first change from the queue and process it.
		 */
		public void run() {
			
			synchronized (fDirtyRegionQueue) {
				try {
					fDirtyRegionQueue.wait(fDelay);
				} catch (InterruptedException x) {
				}
			}
			
			initialProcess();
			
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
				
				if (fProgressMonitor != null)
					fProgressMonitor.setCanceled(false);
					
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
			
			if (fProgressMonitor != null && fThread.isActive())
				fProgressMonitor.setCanceled(true);
				
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
			reconcilerDocumentChanged(fDocument);
				
			fDocument.addDocumentListener(this);
			
			forceReconciling();
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
	/** The progress monitor used by this reconciler */
	private IProgressMonitor fProgressMonitor;

	/** The text viewer's document */
	private IDocument fDocument;
	/** The text viewer */
	private ITextViewer fViewer;
	
	
	/**
	 * Processes a dirty region. If the dirty region is <code>null</code> the whole
	 * document is consider being dirty. The dirty region is partitioned by the
	 * document and each partition is handed over to a reconciling strategy registered
	 * for the partition's content type.
	 *
	 * @param dirtyRegion the dirty region to be processed
	 */
	abstract protected void process(DirtyRegion dirtyRegion);
	
	/**
	 * Hook called when the document whose contents should be reconciled
	 * has been changed, i.e., the input document of the text viewer this
	 * reconciler is installed on. Usually, subclasses use this hook to 
	 * inform all their reconciling strategies about the change.
	 * 
	 * @param newDocument the new reconciler document
	 */
	abstract protected void reconcilerDocumentChanged(IDocument newDocument);
	
	
	/**
	 * Creates a new reconciler with the following configuration: it is
	 * an incremental reconciler which kicks in every 500 ms. There are no
	 * predefined reconciling strategies.
	 */ 
	protected AbstractReconciler() {
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
	 * Sets the progress monitor of this reconciler.
	 * 
	 * @param monitor the monitor to be used
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}
	
	/**
	 * Returns whether any of the reconciling strategies is interested in
	 * detailed dirty region information.
	 * 
	 * @return whether this reconciler is incremental
	 * 
	 * @see IReconcilingStrategy 
	 */
	protected boolean isIncrementalReconciler() {
		return fIsIncrementalReconciler;
	}
	
	/**
	 * Returns the input document of the text viewer this reconciler is installed on.
	 * 
	 * @return the reconciler document
	 */
	protected IDocument getDocument() {
		return fDocument;
	}
	
	/**
	 * Returns the text viewer this reconciler is installed on.
	 * 
	 * @return the text viewer this reconciler is installed on
	 */
	protected ITextViewer getTextViewer() {
		return fViewer;
	}
	
	/**
	 * Returns the progress monitor of this reconciler.
	 * 
	 * @return the progress monitor of this reconciler
	 */
	protected IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
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
	}
	
	/*
	 * @see IReconciler#uninstall
	 */
	public void uninstall() {
		if (fListener != null) {
			
			fViewer.removeTextInputListener(fListener);
			if (fDocument != null) fDocument.removeDocumentListener(fListener);
			fListener= null;
			
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=19135
			BackgroundThread bt= fThread;
			fThread= null;
			bt.cancel();
		}
	}
		
	/**
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
	 * This method is called on startup of the background activity. It is called only
	 * once during the life time of the reconciler. Clients may reimplement this method.
	 */
	protected void initialProcess() {
	}
	
	/**
	 * Forces the reconciler to reconcile the structure of the whole document.
	 * Clients may extend this method.
	 */
	protected void forceReconciling() {
		
		if (fIsIncrementalReconciler) {
			DocumentEvent e= new DocumentEvent(fDocument, 0, 0, fDocument.get());
			createDirtyRegion(e);
		}
		
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=19135
		if (fThread == null)
			return;
			
		if (!fThread.isAlive())
			fThread.start();
		else
			fThread.reset();
	}
    
    /**
     * Hook that is called after the reconciler thread has been reset.
     */
    protected void reconcilerReset() {
    }
}
