package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.FileDateFormat;
import org.eclipse.team.internal.ccvs.core.util.FileUtil;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Implements the IManagedFile interface on top of an 
 * instance of the ICVSFile interface
 * 
 * @see IManagedFile
 */
public class LocalFile extends LocalResource implements ICVSFile {
	
	/**
	 *
	 */
	public LocalFile(File file) {
		super(file);
	}

	/**
	 * @see IManagedFile#getSize()
	 */
	public long getSize() {
		return ioResource.length();
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
		
		title = Policy.bind("LocalFile.receiving", 
							new Object[] {ioResource.getName()});
		
		try {		

			out = new FileOutputStream(ioResource);
			
			if (binary) {
				// System.out.println("BinaryReciving: " + getName() + "(" + size + ")");
				transferWithProgress(in,out,size,monitor,title);
			} else {
				// System.out.println("TextReciving: " + getName() + "(" + size + ")");
				transferText(in,out,size,monitor,title,false);
			}
			
			out.close();
			
			if (readOnly) {
				ioResource.setReadOnly();
			}
			
		} catch (IOException e) {
			throw CVSException.wrapException(e);
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
		title = Policy.bind("LocalFile.sending",
							new Object[]{ioResource.getName()});
		
		try {			
			in = new FileInputStream(ioResource);
			
			if (binary) {
				
				// Send the size to the server
				out.write(("" + getSize()).getBytes());
				out.write(SERVER_NEWLINE.getBytes());
				transferWithProgress(in,out,size,monitor,title);
				
				// System.out.println("BinarySending: " + getName() + "(" + size + ")");
				
			} else {
				
				// In this case the size has to be computed.
				// Therefore we do send the size in transferText
				transferText(in,out,getSize(),monitor,title,true);

				// System.out.println("TextSending: " + getName() + "(" + size + ")");
			}
			
			in.close();
			
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}	
	}	
	/**
	 * @see IManagedFile#getTimeStamp()
	 */
	public String getTimeStamp() throws CVSFileNotFoundException {
						
		FileDateFormat df = new FileDateFormat();
		
		return df.formatMill(ioResource.lastModified());
	}
 
	/**
	 * @see IManagedFile#setTimeStamp(long)
	 */
	public void setTimeStamp(String date) throws CVSException {
		
		long millSec;
		Calendar calendar;
		FileDateFormat df = new FileDateFormat();
		
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
		
		ioResource.setLastModified(millSec);
	}

	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
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
					"LocalFile.transfer",
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
			
		byte[] BUFFER = new byte[4096];			

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
							"LocalFile.transfer",
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
	 * @see IManagedFile#isDirty()
	 */
	public boolean isDirty() throws CVSException {
		if (!exists() || !isManaged()) {
			return true;
		} else {
			ResourceSyncInfo info = getSyncInfo();
			return !getTimeStamp().equals(info.getTimeStamp());
		}
	}

	/**
	 * @see ICVSResource#accept(ICVSResourceVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/**
	 * @see IManagedFile#moveTo(IManagedFile)
	 */
	public void moveTo(ICVSFile mFile) throws CVSException {
		
		LocalFile file;
		try {
			file = (LocalFile)mFile;
		} catch(ClassCastException e) {
			throw CVSException.wrapException(e);
		}
				
		boolean success;
		
		success = ioResource.renameTo(file.getFile());
		
		if (!success) {
			throw new CVSException("Move from " + ioResource + " to " + file + " was not possible");
		}
	}

	public File getFile() {
		return ioResource;
	}
	
	/**
	 * @see IManagedFile#getContent()
	 */
	public String[] getContent() throws CVSException {
		return FileUtil.readLines(ioResource);
	}
	
	/**
	 * @see ICVSResource#getRemoteLocation()
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		return getParent().getRemoteLocation(stopSearching) + SEPARATOR + getName();
	}	
}

