package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.internal.ccvs.core.util.FileDateFormat;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Implements the IManagedFile interface on top of an 
 * instance of the ICVSFile interface
 * 
 * @see IManagedFile
 */
class ManagedFile extends ManagedResource implements IManagedFile {
	
	ICVSFile cvsFile;
	private static final byte[] BUFFER = new byte[4096];

	/**
	 * Constructor for ManagedFile
	 */
	ManagedFile(ICVSFile cvsFile) {
		super();
		this.cvsFile = cvsFile;
	}

	/**
	 * @see IManagedFile#getSize()
	 */
	public long getSize() {
		return cvsFile.getSize();
	}

	/**
	 * @see IManagedFile#getFileInfo()
	 */
	public FileProperties getFileInfo() throws CVSException {
	
		return getInternalParent().getFileInfo(this);
		
	}

	/**
	 * @see IManagedFile#setFileInfo(FileProperties)
	 */
	public void setFileInfo(FileProperties fileInfo) throws CVSException {
		
		if (!(fileInfo == null || fileInfo.getName().equals(cvsFile.getName()))) {
			throw new CVSException("Try to set fileInfo where fileInfo.getName() != file.getName()");
		}
		
		getInternalParent().setFileInfo(this,fileInfo);
		
	}

	/**
	 * @see IManagedFile#reciveFrom(OutputStream, IProgressMonitor)
	 */
	public void receiveFrom(InputStream in, 
							 IProgressMonitor monitor, 
							 long size, 
							 boolean binary,
							 boolean readOnly)
							 
		throws CVSException {
		
		OutputStream out;
		String title;
		
		title = Policy.bind("ManagedFile.receiving", 
							new Object[] {cvsFile.getName()});
		
		try {		

			out = cvsFile.getOutputStream();
			
			if (binary) {
				// System.out.println("BinaryReciving: " + getName() + "(" + size + ")");
				transferWithProgress(in,out,size,monitor,title);
			} else {
				// System.out.println("TextReciving: " + getName() + "(" + size + ")");
				transferText(in,out,size,monitor,title,false);
			}
			
			out.close();
			
			if (readOnly) {
				cvsFile.setReadOnly();
			}
			
		} catch (IOException e) {
			throw wrapException(e);
		}
	}

	/**
	 * @see IManagedFile#sendTo(InputStream, IProgressMonitor, long)
	 */
	public void sendTo(
		OutputStream out,
		IProgressMonitor monitor,
		boolean binary)
		throws CVSException {
		
		InputStream in;
		String title;
		long size = getSize();
		title = Policy.bind("ManagedFile.sending",
							new Object[]{cvsFile.getName()});
		
		try {			
			in = cvsFile.getInputStream();
			
			if (binary) {
				
				// Send the size to the server
				out.write(("" + cvsFile.getSize()).getBytes());
				out.write(SERVER_NEWLINE.getBytes());
				transferWithProgress(in,out,size,monitor,title);
				
				// System.out.println("BinarySending: " + getName() + "(" + size + ")");
				
			} else {
				
				// In this case the size has to be computed.
				// Therefore we do send the size in transferText
				transferText(in,out,cvsFile.getSize(),monitor,title,true);

				// System.out.println("TextSending: " + getName() + "(" + size + ")");
			}
			
			in.close();
			
		} catch (IOException e) {
			throw wrapException(e);
		}	
	}	
	/**
	 * @see IManagedFile#getTimeStamp()
	 */
	public String getTimeStamp() throws CVSFileNotFoundException {
		
		exceptionIfNotExists();		
		
		FileDateFormat df = new FileDateFormat();
		
		return df.formatMill(cvsFile.getTimeStamp());
	}
 
	/**
	 * @see IManagedFile#setTimeStamp(long)
	 */
	public void setTimeStamp(String date) throws CVSException {
		
		long millSec;
		Calendar calendar;
		FileDateFormat df = new FileDateFormat();
		
		exceptionIfNotExists();
		
		if (date==null) {
			// get the current time
			calendar = Calendar.getInstance();
			millSec = calendar.getTime().getTime();
		} else {
			try {
				millSec = df.parseMill(date);
			} catch (ParseException e) {
				throw new CVSException(0,0,"Format of the Date for a TimeStamp not parseable",e);
			}
		}
		
		cvsFile.setTimeStamp(millSec);
	}

	/**
	 * @see IManagedResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
	
	/**
	 * @see IManagedResource#isManaged()
	 */
	public boolean isManaged() throws CVSException {
		return (getInternalParent().getFileInfo(this) != null);
	}
	
	/**
	 * Send/Recive a textFile from/to the server. It does the conversion
	 * of the newlines and sends the filesize to the server (only on a 
	 * send)
	 */
	protected static void transferText(InputStream in,
											OutputStream out,
											long size,
											IProgressMonitor monitor,
											String title,
											boolean toServer) 
											throws IOException {
												
		// If we get a file bigger than 2 GigaByte, this does not
		// work
		Assert.isTrue(size < Integer.MAX_VALUE);

		if (size > 25000) {
			
			monitor.setTaskName(
				Policy.bind(
					"ManagedFile.transfer",
					new Object[]{title,new Long(0),new Long(size/1024)}
				)
			);

		}
		
		byte[] buffer = new byte[(int)size];

		// Get the content from the file
		int num = in.read(buffer);
		int pos = num;
		while ((num != -1) && (size - pos > 0)) {
			Policy.checkCanceled(monitor);
			num = in.read(buffer, pos, ((int)size) - pos);
			pos += num;
		}
		
		// care about newlines
		if (toServer) {
			buffer = Util.replace(buffer,PLATFORM_NEWBYTE,SERVER_NEWBYTE);
			// Send the size to the server
			out.write(("" + buffer.length).getBytes());
			out.write(SERVER_NEWLINE.getBytes());

		} else {
			buffer = Util.replace(buffer,PLATFORM_NEWBYTE,SERVER_NEWBYTE);
			buffer = Util.replace(buffer,SERVER_NEWBYTE,PLATFORM_NEWBYTE);
		}
		
		out.write(buffer);	
	}
		
	/**
	 * Transfer an InputStream to an OutputStream
	 * and update the monitor in between.
	 * 
	 * Used for saving files from server
	 * on disc, etc.
	 */
	protected static void transferWithProgress(
		InputStream in,
		OutputStream out,
		long size,
		IProgressMonitor monitor,
		String title)
		throws IOException {

		// This special transfer utility will show progress to
		// the monitor for files that are bigger than 25K
		boolean progress = size > 25000;
		int read = 0;
		long totalRead = 0;
		long ksize = size / 1024;
		// buffer size is smaller than MAXINT...
		int toRead = (int) Math.min(BUFFER.length, size);
		synchronized (BUFFER) {
			while ((totalRead < size) && (read = in.read(BUFFER, 0, toRead)) != -1) {
				if (progress && totalRead > 0) {
					monitor.subTask(
						Policy.bind(
							"ManagedFile.transfer",
							new Object[] { title, new Long(totalRead / 1024), new Long(ksize)}));
					monitor.worked(read);
				}
				totalRead += read;
				out.write(BUFFER, 0, read);
				toRead = (int) Math.min(BUFFER.length, size - totalRead);
			}
		}
	}
	
	/**
	 * @see ManagedResource#getResource()
	 */
	public ICVSResource getCVSResource() {
		return cvsFile;
	}

	/**
	 * @see IManagedFile#isDirty()
	 */
	public boolean isDirty() throws CVSException {
		
		if (!exists() || !isManaged()) {
			return true;
		}
		
		return !getTimeStamp().equals(getFileInfo().getTimeStamp());
	}

	/**
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(IManagedVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/**
	 * @see IManagedFile#moveTo(IManagedFile)
	 */
	public void moveTo(IManagedFile mFile) throws CVSException, ClassCastException {
		cvsFile.moveTo(((ManagedFile)mFile).cvsFile);
	}

	/**
	 * @see IManagedFile#getContent()
	 */
	public String[] getContent() throws CVSException {
		return cvsFile.getContent();
	}
	
	/**
	 * @see IManagedResource#getRemoteLocation()
	 */
	public String getRemoteLocation(IManagedFolder stopSearching) throws CVSException {
		return getParent().getRemoteLocation(stopSearching) + separator + getName();
	}
	
	/**
	 * @see IManagedResource#unmanage()
	 */
	public void unmanage() throws CVSException {
		setFileInfo(null);
	}
}

