package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * This class is new and experimental. It will likely be subject to significant change before
 * it is finalized.
 * 
 * @since 3.1
 *
 */
public class IOConsolePartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension {
	
	private IDocument document;
	private ArrayList partitions;
	private ArrayList pendingPartitions;
	private IOConsolePartition lastPartition;
	private DocumentUpdaterJob updateJob;
	private IOConsoleInputStream inputStream;
	private boolean updateInProgress;
	private ArrayList inputPartitions;
	private String[] lld;
	private int highWaterMark = -1;
	private int lowWaterMark = -1;
	private ArrayList updatePartitions;
    private int firstOffset;
	
	public IOConsolePartitioner(IOConsoleInputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public IDocument getDocument() {
		return document;
	}
	
	public void connect(IDocument document) {
		this.document = document;
		document.setDocumentPartitioner(this);
		lld = document.getLegalLineDelimiters();
		partitions = new ArrayList();
		pendingPartitions = new ArrayList();
		inputPartitions = new ArrayList();
		updateJob = new DocumentUpdaterJob();
		updateJob.setSystem(true);
	}
	
	
	public void setWaterMarks(int low, int high) {
		lowWaterMark = low;
		highWaterMark = high;
		ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				checkBufferSize();
			}
		});
	}
	
	public void disconnect() {
		document = null;
		partitions = null;
	}
	
	public void documentAboutToBeChanged(DocumentEvent event) {
	}
	
	public boolean documentChanged(DocumentEvent event) {
		return documentChanged2(event) != null;
	}
	
	public String[] getLegalContentTypes() {
		return new String[] { IOConsolePartition.OUTPUT_PARTITION_TYPE, IOConsolePartition.INPUT_PARTITION_TYPE };
	}
	
	public String getContentType(int offset) {
		return getPartition(offset).getType();
	}
	
	public ITypedRegion[] computePartitioning(int offset, int length) {
		int end = length == 0 ? offset : offset + length - 1;
		List list = new ArrayList();
		
		for (int i = 0; i < partitions.size(); i++) {
			ITypedRegion partition = (IOConsolePartition) partitions.get(i);
			int partitionStart = partition.getOffset();
			int partitionEnd = partitionStart + partition.getLength() - 1;
			
			if (offset >= partitionStart && offset <= partitionEnd) {
				list.add(partition);
			} else if (end >= partitionStart && end <= partitionEnd) {
				list.add(partition);
			} else if (partitionStart > end) {
				break; // don't bother testing more.
			}
		}
		
		return (IOConsolePartition[]) list.toArray(new IOConsolePartition[list.size()]);
	}
	
	public ITypedRegion getPartition(int offset) {
		for (int i = 0; i < partitions.size(); i++) {
			ITypedRegion partition = (ITypedRegion) partitions.get(i);
			int start = partition.getOffset();
			int end = start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			} 
		}
		
		if (lastPartition == null)  {
			synchronized(partitions) {
				lastPartition = new IOConsolePartition(inputStream, ""); //$NON-NLS-1$
				lastPartition.setOffset(offset);
				partitions.add(lastPartition);
				inputPartitions.add(lastPartition);
			}
		}
		return lastPartition;
	}
	
	private void checkBufferSize() {
		if (lastPartition == null || highWaterMark == -1) {
			return;
		}
		
		int lines = document.getNumberOfLines();
		if (lines > highWaterMark) {
			
			synchronized(partitions) {
				try {
					int lineOffset = document.getLineOffset(lowWaterMark);
					
					IOConsolePartition partition = (IOConsolePartition) getPartition(lineOffset);
					int offset = partition.getOffset();
					setUpdateInProgress(true);
					document.replace(0, offset, ""); //$NON-NLS-1$
					setUpdateInProgress(false);
					
					//remove partitions and reset Partition offsets
					int index = partitions.indexOf(partition);
					partitions.remove(partitions.subList(0, index));
					
					offset = 0;
					for (Iterator i = partitions.iterator(); i.hasNext(); ) {
						IOConsolePartition p = (IOConsolePartition) i.next();
						p.setOffset(offset);
						offset += p.getLength();
					}
				} catch (BadLocationException e) {
				}  
			}
			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension#documentChanged2(org.eclipse.jface.text.DocumentEvent)
	 */
	public IRegion documentChanged2(DocumentEvent event) {
		if (document.getLength() == 0) { //document cleared
			partitions.clear();
			inputPartitions.clear();
			pendingPartitions.clear();     
			lastPartition = null;
			return new Region(0, 0);
		}
		
		
		if (updateInProgress) {
			synchronized(partitions) {
			    for (Iterator i = updatePartitions.iterator(); i.hasNext(); ) {
			        PendingPartition pp = (PendingPartition) i.next();
			        IOConsolePartition partition = new IOConsolePartition(pp.stream, pp.text);
			        partition.setOffset(firstOffset);
			        firstOffset += partition.getLength();
			        lastPartition = partition;
			        partitions.add(partition);
			    }
			}
		} else {// user input.
			int amountDeleted = event.getLength() - event.getText().length();
			
			if (amountDeleted > 0) {
				int offset = event.fOffset;    
				IOConsolePartition partition = (IOConsolePartition) getPartition(offset);
				if(partition == lastPartition) {
					partition.delete(event.fOffset-partition.getOffset(), amountDeleted);
				} 
				//else {
				//make new partition on end with content of inputPartititions. Delete from that
				
				// deletion can be improved to be more console like. ie Should be able to delete until 'Enter' is pressed even if other output has been appending to
				// the console. ie we should behave like a console :-)
				// requires changes to IOConsoleViewer.handleVerifyEvent(...)
				//                        lastPartition = new IOConsolePartition(inputStream, ""); //$NON-NLS-1$
				//                        lastPartition.setOffset(document.getLength());
				//                        int index = inputPartitions.indexOf(partition);
				//                        for(int i = 0; i< index; i++) {
				//                            IOConsolePartition p = (IOConsolePartition) inputPartitions.get(i);
				//                            p.setReadOnly(true);
				//                            lastPartition.append(p.getString());
				//                        }
				//                        offset = event.fOffset - partition.getOffset() + document.getLength() + lastPartition.getLength();
				//                        for (int i = 0; i<inputPartitions.size(); i++) {
				//                            IOConsolePartition p = (IOConsolePartition) inputPartitions.get(i);
				//                            lastPartition.append(p.getString());
				//                        }
				//                        inputPartitions.clear();
				//                        inputPartitions.add(lastPartition);
				//                        
				//                        lastPartition.delete(offset, amountDeleted);
				//                        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
				//                            public void run() {
				//                                try {
				//                                    setUpdateInProgress(true);
				//                                    document.replace(document.getLength(), 0, lastPartition.getString());
				//                                    setUpdateInProgress(false);
				//                                } catch (BadLocationException e) {
				//                                }
				//                            }
				//                        });
				//}
			}
			
			synchronized(partitions) {
				if (lastPartition == null || lastPartition.isReadOnly()) {
					lastPartition = new IOConsolePartition(inputStream, event.fText); //$NON-NLS-1$
					lastPartition.setOffset(event.fOffset);
					partitions.add(lastPartition);
					inputPartitions.add(lastPartition);
				} else {
					lastPartition.append(event.fText);
				}
				
				for (int i = 0; i < lld.length; i++) {
					String ld = lld[i];
					if (event.fText.endsWith(ld)) {
						StringBuffer input = new StringBuffer();
						for (Iterator it = inputPartitions.iterator(); it.hasNext(); ) {
							IOConsolePartition partition = (IOConsolePartition) it.next();
							input.append(partition.getString());
							partition.setReadOnly(true);
						}
						inputStream.appendData(input.toString());
						inputPartitions.clear();
						break;
					}
				}
			}
		}   
		
		checkBufferSize();
		return new Region(event.fOffset, event.fText.length());
	}
	
	private void setUpdateInProgress(boolean b) {
		updateInProgress = b;
	}
		
	public void streamAppended(IOConsoleOutputStream stream, String s) {
		synchronized(pendingPartitions) {
			PendingPartition last = (PendingPartition) (pendingPartitions.size() > 0 ? pendingPartitions.get(pendingPartitions.size()-1) : null);
			if (last != null && last.stream == stream) {
				last.append(s);
			} else {
				pendingPartitions.add(new PendingPartition(stream, s));
				if (!updateJob.scheduled) {
				    updateJob.scheduled = true;
				    updateJob.schedule(50);
				}
			}
		}
	}
	
	private class PendingPartition {
		String text;
		IOConsoleOutputStream stream;
		
		PendingPartition(IOConsoleOutputStream stream, String text) {
			this.stream = stream;
			this.text=text;
		}
		
		void append(String moreText) {
			text += moreText;
		}
	}
	
	private class DocumentUpdaterJob extends Job {
		
		private boolean scheduled = false;

        DocumentUpdaterJob() {
			super("IOConsole Updater"); //$NON-NLS-1$
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			Display display = ConsolePlugin.getStandardDisplay();
			
			ArrayList pendingCopy = new ArrayList();
			StringBuffer buffer = null;
			while (display != null && pendingPartitions.size() > 0) {
				synchronized(pendingPartitions) {
					pendingCopy.addAll(pendingPartitions);
					pendingPartitions.clear();
				}
				
				buffer = new StringBuffer();
				for (Iterator i = pendingCopy.iterator(); i.hasNext(); ) {
				    PendingPartition pp = (PendingPartition) i.next();
				    buffer.append(pp.text);
				}
			}
			
			final ArrayList finalCopy = pendingCopy;
			final String toAppend = buffer.toString();		
			scheduled = false;
			
			display.asyncExec(new Runnable() {
			    public void run() {    
			        setUpdateInProgress(true);
			        updatePartitions = finalCopy;
			        firstOffset = document.getLength();
			        try {
			            document.replace(firstOffset, 0, toAppend.toString());
			        } catch (BadLocationException e) {
			        }
			        updatePartitions = null;
			        setUpdateInProgress(false);
			    }
			});
			
			return Status.OK_STATUS;
		}        
		
        /* (non-Javadoc)
         * @see org.eclipse.core.internal.jobs.InternalJob#shouldSchedule()
         */
        public boolean shouldSchedule() {
            return scheduled;
        }
	}
	
}