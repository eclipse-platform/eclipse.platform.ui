package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class Session {
	public static final String CURRENT_LOCAL_FOLDER = ".";
	public static final String CURRENT_REMOTE_FOLDER = "";
	public static final String SERVER_SEPARATOR = "/";

	// default file transfer buffer size (in bytes)
	private static int TRANSFER_BUFFER_SIZE = 8192;
	// update progress bar in increments of this size (in bytes)
	//   no incremental progress shown for files smaller than this size
	private static long TRANSFER_PROGRESS_INCREMENT = 32768;

	// the platform's line termination sequence
	private static final byte[] PLATFORM_NEWLINE_BYTES =
		System.getProperty("line.separator").getBytes(); // at least one byte long
	// the server's line termination sequence
	private static final int SERVER_NEWLINE_BYTE = 0x0a; // exactly one byte long
	private static final byte[] SERVER_NEWLINE_BYTES = new byte[] { SERVER_NEWLINE_BYTE };
	// true iff newlines must be converted between platform and server formats
	private static boolean MUST_CONVERT_NEWLINES = PLATFORM_NEWLINE_BYTES.length != 1
		&& PLATFORM_NEWLINE_BYTES[0] != SERVER_NEWLINE_BYTE;
		
	// VCM 1.0 comitted files using CR/LF as a delimiter
	private static final int CARRIAGE_RETURN_BYTE = 0x0d;

	private final CVSRepositoryLocation location;
	private final ICVSFolder localRoot;
	private boolean outputToConsole;
	private Connection connection = null;
	private String validRequests = null;
	private Date modTime = null;
	private boolean noLocalChanges = false;

	// a shared buffer used for file transfers
	private byte[] transferBuffer = null;

	/**
	 * Creates a new CVS session.
	 * 
	 * @param location
	 * @param localRoot represents the current working directory of the client
	 */
	public Session(ICVSRepositoryLocation location, ICVSFolder localRoot) {
		this(location, localRoot, true);
	}
	
	public Session(ICVSRepositoryLocation location, ICVSFolder localRoot, boolean outputToConsole) {
		this.location = (CVSRepositoryLocation) location;
		this.localRoot = localRoot;
		this.outputToConsole = outputToConsole;
	}
	
	public void open(IProgressMonitor monitor) throws CVSException {
		if (connection != null) throw new IllegalStateException();
		connection = location.openConnection(monitor);
		
		// Tell the serves the names of the responses we can handle
		connection.writeLine("Valid-responses " + Command.makeResponseList());

		// Ask for the set of valid requests
		Command.VALID_REQUESTS.execute(this, Command.NO_GLOBAL_OPTIONS, Command.NO_LOCAL_OPTIONS,
			Command.NO_ARGUMENTS, null, monitor);

		// Set the root directory on the server for this connection
		connection.writeLine("Root " + location.getRootDirectory());
	}		
	
	public void close() throws CVSException {
		/// ?
		if (connection == null) throw new IllegalStateException();
		connection.close();
		connection = null;
	}
	
	public boolean isValidRequest(String request) {
		return (validRequests == null) ||
			(validRequests.indexOf(" " + request + " ") != -1);
	}

	/**
	 * Gives you an LocalFolder for a absolute path in
	 * platform dependend style.
	 * 
	 * @throws CVSException on path.indexOf("CVS") != -1
	 * @throws CVSException on internal IOExeption
	 */
	public static ICVSFolder getManagedFolder(File folder) throws CVSException {
		return new LocalFolder(folder);
	}
	public static ICVSFile getManagedFile(File file) throws CVSException {
		return new LocalFile(file);
	}
	
	public static ICVSResource getManagedResource(IResource resource) throws CVSException {
		File file = resource.getLocation().toFile();
		if (resource.getType() == IResource.FILE)
			return getManagedFile(file);
		else
			return getManagedFolder(file);
	}

	/**
	 * Returns the local root folder for this session.
	 * 
	 * @returns the local root folder
	 */
	public ICVSFolder getLocalRoot() {
		return localRoot;
	}

	/**
	 * Receives a line of text minus the newline from the server.
	 */
	public String readLine() throws CVSException {
		return connection.readLine();
	}

	/**
	 * Sends a line of text followed by a newline to the server.
	 * 
	 * @param line the line
	 */
	public void writeLine(String line) throws CVSException {
		connection.writeLine(line);
	}

	/**
	 * Sends an argument to the server.
	 * <p>
	 * e.g. sendArgument("Hello\nWorld\n  Hello World") is sent as
	 * <ul>
	 *   <li>Argument Hello</li>
	 *   <li>Argumentx World</li>
	 *   <li>Argumentx Hello World</li>
	 * </ul></p>
	 * 
	 * @param arg the argument to send
	 */
	public void sendArgument(String arg) throws CVSException {
		connection.write("Argument ");
		int oldPos = 0;
		for (;;) {
			int pos = arg.indexOf('\n', oldPos);
			if (pos == -1) break;
			connection.writeLine(arg.substring(oldPos, pos));
			connection.write("Argumentx ");
			oldPos = pos + 1;
		}
		connection.writeLine(arg.substring(oldPos));
	}

	public void sendCommand(String commandId) throws CVSException {
		connection.writeLine(commandId);
		connection.flush();
	}

	public void sendKopt(String arg) throws CVSException {
		connection.writeLine("Kopt " + arg);
	}

	/**
	 * Sends an Is-modified request to the server without the file contents.
	 * <p>e.g. if a file called "local_file" was modified, sends:
	 * <pre>
	 *   Is-modified local_file \n
	 * </pre></p><p>
	 * This request is an optimized form of the Modified request and may not
	 * be supported by all servers.  Hence, if it is not supported, a Modified
	 * request is sent instead along with the file's contents.  According to
	 * the CVS protocol specification, this request is only safe for use with
	 * some forms of: admin, annotate, diff, editors, log, watch-add, watch-off,
	 * watch-on, watch-remove, and watchers.<br>
	 * It may be possible to use this for: add, export, remove and status.<br>
	 * Do not use with co, ci, history, init, import, release, rdiff, rtag, or update.
	 * </p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param file the file that was modified
	 * @see #sendModified
	 */
	public void sendIsModified(ICVSFile file, boolean isBinary, IProgressMonitor monitor)
		throws CVSException {
		if (isValidRequest("Is-modified")) {
			connection.writeLine("Is-modified " + file.getName());
		} else {
			sendModified(file, isBinary, monitor);
		}
	}

	public void sendStaticDirectory() throws CVSException {
		connection.writeLine("Static-directory");
	}
	
	/**
	 * The Directory request is sent as:
	 * <ul>
	 * 		<li>Directory localdir
	 * 		<li>repository_root/remotedir
	 * </ul>
	 * 
	 * This note is copied from an old version:
	 * [Note: A CVS repository root can end with a trailing slash. The CVS server
	 * expects that the repository root sent contain this extra slash. Including
	 * the foward slash in addition to the absolute remote path makes for a string
	 * containing two consecutive slashes (e.g. /home/cvs/repo//projecta/a.txt).
	 * This is valid in the CVS protocol.]
	 */
	public void sendConstructedDirectory(String local, String remote) throws CVSException {
		// FIXME I do not know wether this method is "ModuleFile-safe"
		connection.writeLine("Directory " + local);
		connection.writeLine(location.getRootDirectory() + "/" + remote);
	}

	/**
	 * The Directory request is sent as:
	 * <ul>
	 * 		<li>Directory localdir
	 * 		<li>repository_root/remotedir
	 * </ul>
	 */
	public void sendDirectory(String local, String remote) throws CVSException {
		if (local.length() == 0) local = Session.CURRENT_LOCAL_FOLDER;
		connection.writeLine("Directory " + local);
		connection.writeLine(remote);
	}
	
	public void sendLocalRootDirectory() throws CVSException {
		sendDirectory(CURRENT_LOCAL_FOLDER, localRoot.getRemoteLocation(localRoot));
	}
	
	public void sendDefaultRootDirectory() throws CVSException {
		sendConstructedDirectory(Session.CURRENT_LOCAL_FOLDER, CURRENT_REMOTE_FOLDER);
	}

	
	public void sendEntry(String entryLine) throws CVSException {
		connection.writeLine("Entry " + entryLine);
	}

	public void sendGlobalOption(String option) throws CVSException {
		connection.writeLine("Global_option " + option);
	}

	/**
	 * Sends an Unchanged request to the server.
	 * <p>e.g. if a file called "local_file" was not modified, sends:
	 * <pre>
	 *   Unchanged local_file \n
	 * </pre></p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param file the file that was not modified
	 */
	public void sendUnchanged(ICVSFile file) throws CVSException {
		connection.writeLine("Unchanged " + file.getName());
	}
	
	public void sendQuestionable(String filename) throws CVSException {
		connection.writeLine("Questionable " + filename);
	}

	public void sendSticky(String tag) throws CVSException {
		connection.writeLine("Sticky " + tag);
	}

	/**
	 * Sends a Modified request to the server along with the file contents.
	 * <p>e.g. if a file called "local_file" was modified, sends:
	 * <pre>
	 *   Modified local_file \n
	 *   file_permissions \n
	 *   file_size \n
	 *   [... file_contents ...]
	 * </pre></p><p>
	 * Under some circumstances, Is-modified may be used in place of this request.<br>
	 * Do not use with history, init, import, rdiff, release, rtag, or update.
	 * </p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param file the file that was modified
	 * @param isBinary if true the file is sent without translating line delimiters
	 * @param monitor the progress monitor
	 * @see #sendIsModified
	 */
	public void sendModified(ICVSFile file, boolean isBinary, IProgressMonitor monitor)
		throws CVSException {
		connection.writeLine("Modified " + file.getName());
		ResourceSyncInfo info = file.getSyncInfo();
		if (info != null && info.getPermissions() != null) {
			connection.writeLine(info.getPermissions());
		} else {
			connection.writeLine(ResourceSyncInfo.DEFAULT_PERMISSIONS);
		}
		sendFile(file, isBinary, monitor);
	}

	/**
	 * Gets the shared file transfer buffer.
	 */
	private byte[] getTransferBuffer() {
		if (transferBuffer == null) transferBuffer = new byte[TRANSFER_BUFFER_SIZE];
		return transferBuffer;
	}
	
	/**
	 * Sends a file to the remote CVS server, possibly translating line delimiters.
	 * <p>
	 * Line termination sequences are automatically converted to linefeeds only
	 * (required by the CVS specification) when sending non-binary files.  This
	 * may alter the actual size and contents of the file that is sent.
	 * </p><p>
	 * Note: Non-binary files must be small enough to fit in available memory.
	 * </p>
	 * @param file the file to be sent
	 * @param isBinary is true if the file should be sent without translation
	 * @param monitor the progress monitor
	 */
	public void sendFile(ICVSFile file, boolean isBinary, IProgressMonitor monitor)
		throws CVSException {
		// update progress monitor
		String title = Policy.bind("LocalFile.sending", new Object[]{ file.getName() });
		monitor.subTask(Policy.bind("LocalFile.transferNoSize", title));
		// obtain an input stream for the file and its size
		long size = file.getSize();
		InputStream in = file.getInputStream();
		OutputStream out = connection.getOutputStream();
		try {
			if (isBinary || PLATFORM_NEWLINE_BYTES.length == 1) {
				writeLine(Long.toString(size));
				if (! isBinary && MUST_CONVERT_NEWLINES) {
					/*** convert newlines on-the-fly ***/
					transferWithProgress(in, out, size,
						PLATFORM_NEWLINE_BYTES[0], SERVER_NEWLINE_BYTES, monitor, title);
				} else {
					/*** perform no conversion ***/
					transferWithProgress(in, out, size, 0, null, monitor, title);
				}
			} else {
				// implies file is text, and we must convert newlines since size of platform newline
				// sequence is not 1, but the server's is
				/*** convert newlines in memory, since file size may change ***/
				Assert.isTrue(size < Integer.MAX_VALUE);
				int fsize = (int) size;
				byte[] fileContents;
				if (fsize <= TRANSFER_BUFFER_SIZE) fileContents = getTransferBuffer();
				else fileContents = new byte[fsize];
				// translate the file from non-LF delimiters in memory and
				// compute its reduced size
				try {
					// read exactly _size_ bytes
					try {
						for (int pos = 0, read; pos < fsize; pos += read) {
							Policy.checkCanceled(monitor);
							read = in.read(fileContents, pos, fsize - pos);
							if (read == -1) {
								// file ended prematurely
								throw new IOException("Read finished prematurely");
							}
						}
					} finally {
						in.close(); // remember to close the source file
						in = null;
					}
				} catch (IOException e) {
					throw CVSException.wrapException(e);
				}
				// convert platform line termination sequences
				// conservative since it leaves any partial sequences alone (like stray CR's)
				// assumes no prefix of a sequence
				int cur = 0, match = 0;
				for (int pos = 0; pos < fsize; ++pos) {
					byte b = fileContents[pos];
					if (PLATFORM_NEWLINE_BYTES[match] == b) {
						if (match == PLATFORM_NEWLINE_BYTES.length - 1) {
							b = SERVER_NEWLINE_BYTE;
							cur -= match;
							match = 0;
						} else match += 1;
					} else {
						match = 0;
					}
					fileContents[cur++] = b;
				}
				// send file
				writeLine(Integer.toString(cur));
				in = new ByteArrayInputStream(fileContents, 0, cur);
				transferWithProgress(in, out, cur, 0, null, monitor, title);
			}
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				throw CVSException.wrapException(e);
			}
		}
	}

	/**
	 * Receives a file from the remote CVS server, possibly translating line delimiters.
	 * <p>
	 * Line termination sequences are automatically converted to platform format
	 * only when receiving non-binary files.  This may alter the actual size and
	 * contents of the file that is received.
	 * </p><p>
	 * Translation is performed on-the-fly, so the file need not fit in available memory.
	 * </p>
	 * @param file the file to be received
	 * @param isBinary is true if the file should be received without translation
	 * @param monitor the progress monitor
	 */
	public void receiveFile(ICVSFile file, boolean isBinary, IProgressMonitor monitor)
	throws CVSException {
		// update progress monitor
		String title = Policy.bind("LocalFile.receiving", new Object[]{ file.getName() });
		monitor.subTask(Policy.bind("LocalFile.transferNoSize", title));
		// get the file size from the server
		long size;
		try {
			size = Long.parseLong(readLine(), 10);
		} catch (NumberFormatException e) {
			throw new CVSException("Malformed file transmission received", e);
		}
		// obtain an output stream for the file
		OutputStream out = file.getOutputStream();
		try {
			transferWithProgress(connection.getInputStream(), out, size, SERVER_NEWLINE_BYTE,
				isBinary ? null : PLATFORM_NEWLINE_BYTES, monitor, title);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				throw CVSException.wrapException(e);
			}
		}
	}
	
	/**
	 * Transfers a file to or from the remove CVS server, possibly expanding line delimiters.
	 * <p>
	 * Line termination sequences are only converted upon request by specifying an
	 * array containing the expected sequence of bytes representing an outbound newline,
	 * and a single byte representing an inbound newline.  If null is passed for the
	 * former, the file is assumed to have binary contents, hence no translation is
	 * performed.
	 * </p><p>
	 * Translation is performed on-the-fly, so the file need not fit in available memory.
	 * </p>
	 * @param in the input stream
	 * @param out the output stream
	 * @param size the source file size
	 * @param newlineIn the single byte for a received newline, ignored if binary
	 * @param newlineOut the sequence of bytes for sent newline, or null if binary
	 * @param monitor the progress monitor
	 * @param title the name of the file being received (as shown in the monitor)
	 */
	private void transferWithProgress(InputStream in, OutputStream out,
		long size, int newlineIn, byte[] newlineOut, IProgressMonitor monitor, String title)
		throws CVSException {
		long nextProgressThresh = TRANSFER_PROGRESS_INCREMENT;
		Long ksize = new Long(size / 1024);
		try {
			byte[] buffer = getTransferBuffer();
			final int wfirst, wlast;
			if (newlineOut != null) {
				wfirst = buffer.length / 2;
				wlast = buffer.length - newlineOut.length - 1; // reserve space for newline & stray CR
			} else {
				wfirst = buffer.length;
				wlast = wfirst;
			}
			int wpos = wfirst;
			// read exactly _size_ bytes
			boolean fixCRLF = (newlineIn == SERVER_NEWLINE_BYTE);
			boolean seenCR = false; // only true if fixCRLF and last byte was a CR
			for (long totalRead = 0; totalRead < size;) {
				Policy.checkCanceled(monitor);
				int read = in.read(buffer, 0, (int) Math.min(wfirst, size - totalRead));
				if (read == -1) {
					// file ended prematurely
					throw new IOException("Read finished prematurely");
				}
				totalRead += read;
				if (newlineOut == null) {
					// dump binary data
					out.write(buffer, 0, read);
				} else {
					// filter newline sequences in memory from first half of buffer into second half
					// then dump to output stream
					for (int p = 0; p < read; ++p) {
						final byte b = buffer[p];
						if (b == CARRIAGE_RETURN_BYTE && fixCRLF) {
							seenCR = true;
						} else {
							if (b == newlineIn) {
								// if fixCRLF we ignore previous CR (if there was one)
								// replace newlineIn with newlineOut
								for (int x = 0; x < newlineOut.length; ++x) buffer[wpos++] = newlineOut[x];
							} else {
								if (seenCR) buffer[wpos++] = CARRIAGE_RETURN_BYTE; // preserve stray CR's
								buffer[wpos++] = b;
							}
							seenCR = false;
						}
						if (wpos >= wlast) {
							// flush output buffer
							out.write(buffer, wfirst, wpos - wfirst);
							wpos = wfirst;
						}
					}
				}
				// update progress monitor
				if (totalRead > nextProgressThresh) {
					monitor.subTask(Policy.bind("LocalFile.transfer",
							new Object[] { title, new Long(totalRead / 1024), ksize}));
					monitor.worked(read);
					nextProgressThresh = totalRead + TRANSFER_PROGRESS_INCREMENT;
				}
			}
			// flush pending buffered output
			if (seenCR) buffer[wpos++] = CARRIAGE_RETURN_BYTE; // preserve stray CR's
			if (wpos != wfirst) out.write(buffer, wfirst, wpos - wfirst);
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}	 
	
	public ICVSRepositoryLocation getCVSRepositoryLocation() {
		return location;
	}

	void setModTime(Date modTime) {
		this.modTime = modTime;
	}
	
	Date getModTime() {
		return modTime;
	}
	
	boolean isNoLocalChanges() {
		return noLocalChanges;
	}
	
	boolean isOutputToConsole() {
		return outputToConsole;
	}
	
	void setNoLocalChanges(boolean noLocalChanges) {
		this.noLocalChanges = noLocalChanges;
	}
	
	void setValidRequests(String validRequests) {
		this.validRequests = " " + validRequests + " ";
	}
}
