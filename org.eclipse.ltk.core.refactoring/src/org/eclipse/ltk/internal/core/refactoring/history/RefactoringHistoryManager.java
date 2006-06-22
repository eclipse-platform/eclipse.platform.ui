/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.IRefactoringSerializationConstants;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.RefactoringSessionReader;
import org.eclipse.ltk.internal.core.refactoring.RefactoringSessionTransformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manager for persistable refactoring histories.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryManager {

	/** The index component delimiter */
	public static final char DELIMITER_COMPONENT= '\t';

	/** The index entry delimiter */
	public static final char DELIMITER_ENTRY= '\n';

	/** The local time zone */
	private static final TimeZone LOCAL_TIME_ZONE= TimeZone.getTimeZone("GMT+00:00"); //$NON-NLS-1$

	/**
	 * Adds the specified index entry to the refactoring history.
	 * 
	 * @param file
	 *            the history index file
	 * @param descriptor
	 *            the refactoring descriptor to add
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while adding the index entry
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private static void addIndexEntry(final IFileStore file, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) throws CoreException, IOException {
		BufferedReader reader= null;
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 5);
			if (file.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
				final StringBuffer temp= new StringBuffer(64);
				temp.append(descriptor.getTimeStamp());
				temp.append(DELIMITER_COMPONENT);
				temp.append(escapeString(descriptor.getDescription()));
				final String value= temp.toString();
				reader= new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(file.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)))), IRefactoringSerializationConstants.OUTPUT_ENCODING));
				final StringBuffer buffer= new StringBuffer();
				boolean inserted= false;
				while (reader.ready()) {
					final String line= reader.readLine();
					if (line != null) {
						buffer.append(line);
						buffer.append(DELIMITER_ENTRY);
						if (!inserted && value.compareTo(line) > 0) {
							buffer.append(value);
							buffer.append(DELIMITER_ENTRY);
							inserted= true;
						}
					}
				}
				monitor.worked(1);
				try {
					reader.close();
					reader= null;
				} catch (IOException exception) {
					// Do nothing
				}
				OutputStream stream= null;
				try {
					file.getParent().mkdir(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
					stream= new BufferedOutputStream(file.openOutputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
					stream.write(buffer.toString().getBytes(IRefactoringSerializationConstants.OUTPUT_ENCODING));
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException exception) {
							// Do nothing
						}
					}
				}
			}
		} finally {
			monitor.done();
			try {
				if (reader != null)
					reader.close();
			} catch (IOException exception) {
				// Do nothing
			}
		}
	}

	/**
	 * Appends the specified index entry to the refactoring history,
	 * 
	 * @param file
	 *            the history index file
	 * @param descriptor
	 *            the refactoring descriptor the description of the refactoring
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while writing the index entry
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private static void appendIndexEntry(final IFileStore file, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) throws CoreException, IOException {
		OutputStream output= null;
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 2);
			file.getParent().mkdir(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			output= new BufferedOutputStream(file.openOutputStream(EFS.APPEND, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
			final StringBuffer buffer= new StringBuffer(256);
			buffer.append(descriptor.getTimeStamp());
			buffer.append(DELIMITER_COMPONENT);
			buffer.append(escapeString(descriptor.getDescription()));
			buffer.append(DELIMITER_ENTRY);
			output.write(buffer.toString().getBytes(IRefactoringSerializationConstants.OUTPUT_ENCODING));
		} finally {
			monitor.done();
			if (output != null) {
				try {
					output.close();
				} catch (IOException exception) {
					// Do nothing
				}
			}
		}
	}

	/**
	 * Checks whether the argument is well-formed.
	 * 
	 * @param argument
	 *            the argument
	 * @throws CoreException
	 */
	private static void checkArgument(final Object argument) throws CoreException {
		if (argument instanceof String) {
			final String string= (String) argument;
			if ("".equals(string)) //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, RefactoringCoreMessages.RefactoringHistoryManager_empty_argument, null));
		} else
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, RefactoringCoreMessages.RefactoringHistoryManager_non_string_argument, null));
	}

	/**
	 * Checks whether the argument map is well-formed.
	 * 
	 * @param arguments
	 *            the argument map
	 * @throws CoreException
	 *             if the check turns out to be unsuccessful
	 */
	public static void checkArgumentMap(final Map arguments) throws CoreException {
		Assert.isNotNull(arguments);
		for (final Iterator iterator= arguments.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry entry= (Map.Entry) iterator.next();
			checkArgument(entry.getKey());
			checkArgument(entry.getValue());
		}
	}

	/**
	 * Escapes the specified string for the history index.
	 * 
	 * @param string
	 *            the string for the history index
	 * @return the escaped string
	 */
	public static String escapeString(final String string) {
		if (string.indexOf(DELIMITER_COMPONENT) < 0) {
			final int length= string.length();
			final StringBuffer buffer= new StringBuffer(length + 4);
			for (int index= 0; index < length; index++) {
				final char character= string.charAt(index);
				if (DELIMITER_COMPONENT == character)
					buffer.append(DELIMITER_COMPONENT);
				buffer.append(character);
			}
			return buffer.toString();
		}
		return string;
	}

	/**
	 * Returns the argument map of the specified descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the argument map, or <code>null</code>
	 */
	public static Map getArgumentMap(final RefactoringDescriptor descriptor) {
		Map arguments= null;
		final RefactoringContribution contribution= RefactoringContributionManager.getInstance().getRefactoringContribution(descriptor.getID());
		if (contribution != null)
			arguments= contribution.retrieveArgumentMap(descriptor);
		else if (descriptor instanceof DefaultRefactoringDescriptor)
			arguments= ((DefaultRefactoringDescriptor) descriptor).getArguments();
		return arguments;
	}

	/**
	 * Reads refactoring descriptor proxies.
	 * 
	 * @param store
	 *            the file store to read
	 * @param project
	 *            the name of the project, or <code>null</code> for the
	 *            workspace
	 * @param collection
	 *            the collection of proxies to fill in
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param flags
	 *            the flags which must be present
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	private static void readRefactoringDescriptorProxies(final IFileStore store, final String project, final Collection collection, final long start, final long end, final int flags, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 22);
			final IFileInfo info= store.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			if (!info.isDirectory() && info.exists() && store.getName().equalsIgnoreCase(RefactoringHistoryService.NAME_INDEX_FILE)) {
				InputStream stream= null;
				try {
					stream= store.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
					final RefactoringDescriptorProxy[] proxies= readRefactoringDescriptorProxies(stream, project, start, end, flags);
					for (int index= 0; index < proxies.length; index++)
						collection.add(proxies[index]);
					monitor.worked(1);
				} catch (IOException exception) {
					throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
				} finally {
					monitor.worked(1);
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException exception) {
							// Do nothing
						}
					}
					monitor.worked(1);
				}
			} else
				monitor.worked(4);
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			final IFileStore[] stores= store.childStores(EFS.NONE, new SubProgressMonitor(monitor, 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			final IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 12);
			try {
				subMonitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, stores.length);
				for (int index= 0; index < stores.length; index++)
					readRefactoringDescriptorProxies(stores[index], project, collection, start, end, flags, new SubProgressMonitor(subMonitor, 1));
			} finally {
				subMonitor.done();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Reads refactoring descriptor proxies from the specified input stream.
	 * 
	 * @param stream
	 *            the input stream where to read from
	 * @param project
	 *            the name of the project, or <code>null</code> for the
	 *            workspace
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param flags
	 *            the flags which must be present
	 * @return An array of refactoring descriptor proxies
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	public static RefactoringDescriptorProxy[] readRefactoringDescriptorProxies(final InputStream stream, final String project, final long start, final long end, final int flags) throws IOException {
		final List list= new ArrayList();
		final BufferedReader reader= new BufferedReader(new InputStreamReader(stream, IRefactoringSerializationConstants.OUTPUT_ENCODING));
		while (reader.ready()) {
			final String line= reader.readLine();
			if (line != null) {
				final int index= line.indexOf(DELIMITER_COMPONENT);
				if (index > 0) {
					try {
						final long stamp= new Long(line.substring(0, index)).longValue();
						if (stamp >= start && stamp <= end)
							list.add(new DefaultRefactoringDescriptorProxy(unescapeString(line.substring(index + 1)), project, stamp));
					} catch (NumberFormatException exception) {
						// Just skip
					}
				}
			}
		}
		return (RefactoringDescriptorProxy[]) list.toArray(new RefactoringDescriptorProxy[list.size()]);
	}

	/**
	 * Reads the specified number of refactoring descriptors from the
	 * refactoring history.
	 * 
	 * @param stream
	 *            the input stream where to read
	 * @param collection
	 *            the list of descriptors read from the history
	 * @param count
	 *            the total number of descriptors to be read
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while reading the descriptors
	 */
	private static void readRefactoringDescriptors(final InputStream stream, final Collection collection, final int count, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 1);
			final RefactoringDescriptor[] results= new RefactoringSessionReader(true).readSession(new InputSource(new BufferedInputStream(stream))).getRefactorings();
			Arrays.sort(results, new Comparator() {

				public final int compare(final Object first, final Object second) {
					final RefactoringDescriptor predecessor= (RefactoringDescriptor) first;
					final RefactoringDescriptor successor= (RefactoringDescriptor) second;
					final long delta= predecessor.getTimeStamp() - successor.getTimeStamp();
					if (delta > 0)
						return 1;
					else if (delta < 0)
						return -1;
					return 0;
				}
			});
			monitor.worked(1);
			final int size= count - collection.size();
			for (int index= 0; index < results.length && index < size; index++)
				collection.add(results[index]);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Reads refactoring descriptor proxies from the specified input stream.
	 * 
	 * @param stream
	 *            the input stream where to read from
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @return An array of refactoring descriptor proxies
	 * @throws CoreException
	 *             if an error occurs while reading the descriptors
	 */
	public static RefactoringDescriptor[] readRefactoringDescriptors(final InputStream stream, final long start, final long end) throws CoreException {
		final List list= new ArrayList();
		readRefactoringDescriptors(stream, list, Integer.MAX_VALUE, new NullProgressMonitor());
		final RefactoringDescriptor[] descriptors= new RefactoringDescriptor[list.size()];
		list.toArray(descriptors);
		return descriptors;
	}

	/**
	 * Removes the specified index entry from the refactoring history.
	 * 
	 * @param file
	 *            the history index file
	 * @param stamp
	 *            the time stamp
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while removing the index entry
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private static void removeIndexEntry(final IFileStore file, final long stamp, final IProgressMonitor monitor) throws CoreException, IOException {
		BufferedReader reader= null;
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 5);
			if (file.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
				final String value= new Long(stamp).toString();
				reader= new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(file.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)))), IRefactoringSerializationConstants.OUTPUT_ENCODING));
				final StringBuffer buffer= new StringBuffer();
				while (reader.ready()) {
					final String line= reader.readLine();
					if (line != null && !line.startsWith(value)) {
						buffer.append(line);
						buffer.append(DELIMITER_ENTRY);
					}
				}
				monitor.worked(1);
				try {
					reader.close();
					reader= null;
				} catch (IOException exception) {
					// Do nothing
				}
				OutputStream stream= null;
				try {
					file.getParent().mkdir(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
					stream= new BufferedOutputStream(file.openOutputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
					stream.write(buffer.toString().getBytes(IRefactoringSerializationConstants.OUTPUT_ENCODING));
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException exception) {
							// Do nothing
						}
					}
				}
			}
		} finally {
			monitor.done();
			try {
				if (reader != null)
					reader.close();
			} catch (IOException exception) {
				// Do nothing
			}
		}
	}

	/**
	 * Removes the refactoring history index tree spanned by the specified file
	 * store.
	 * 
	 * @param store
	 *            the file store spanning the history index tree
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while removing the index tree
	 */
	private static void removeIndexTree(final IFileStore store, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 16);
			final IFileInfo info= store.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			if (info.isDirectory()) {
				if (info.getName().equalsIgnoreCase(RefactoringHistoryService.NAME_HISTORY_FOLDER))
					return;
				final IFileStore[] stores= store.childStores(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
				final IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
				try {
					subMonitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, stores.length);
					for (int index= 0; index < stores.length; index++) {
						final IFileInfo current= stores[index].fetchInfo(EFS.NONE, new SubProgressMonitor(subMonitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
						if (current.isDirectory()) {
							final char[] characters= stores[index].getName().toCharArray();
							for (int offset= 0; offset < characters.length; offset++) {
								if (Character.isDigit(characters[offset]))
									return;
								else
									continue;
							}
						}
					}
				} finally {
					subMonitor.done();
				}
			}
			final IFileStore parent= store.getParent();
			store.delete(0, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			removeIndexTree(parent, new SubProgressMonitor(monitor, 12, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns a path representing the history part for the specified time
	 * stamp.
	 * 
	 * @param stamp
	 *            the time stamp
	 * @return A path representing the folder of the history part
	 */
	public static IPath stampToPath(final long stamp) {
		final Calendar calendar= Calendar.getInstance(LOCAL_TIME_ZONE);
		calendar.setTimeInMillis(stamp);
		IPath path= new Path(String.valueOf(calendar.get(Calendar.YEAR)));
		path= path.append(String.valueOf(calendar.get(Calendar.MONTH) + 1));
		path= path.append(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
		return path;
	}

	/**
	 * Transforms the specified refactoring descriptor into a DOM node.
	 * 
	 * @param descriptor
	 *            the descriptor to transform
	 * @param projects
	 *            <code>true</code> to include project information,
	 *            <code>false</code> otherwise
	 * @return the DOM node representing the refactoring descriptor
	 * @throws CoreException
	 *             if an error occurs while transforming the descriptor
	 */
	private static Object transformDescriptor(final RefactoringDescriptor descriptor, final boolean projects) throws CoreException {
		final RefactoringSessionTransformer transformer= new RefactoringSessionTransformer(projects);
		try {
			transformer.beginSession(null);
			try {
				final String id= descriptor.getID();
				transformer.beginRefactoring(id, descriptor.getTimeStamp(), descriptor.getProject(), descriptor.getDescription(), descriptor.getComment(), descriptor.getFlags());
				final Map arguments= getArgumentMap(descriptor);
				if (arguments != null) {
					checkArgumentMap(arguments);
					for (final Iterator iterator= arguments.entrySet().iterator(); iterator.hasNext();) {
						final Map.Entry entry= (Entry) iterator.next();
						transformer.createArgument((String) entry.getKey(), (String) entry.getValue());
					}
				}
			} finally {
				transformer.endRefactoring();
			}
		} finally {
			transformer.endSession();
		}
		return transformer.getResult();
	}

	/**
	 * Un-escapes the specified string from the history index.
	 * 
	 * @param string
	 *            the string from the history index
	 * @return the un-escaped string
	 */
	public static String unescapeString(final String string) {
		if (string.indexOf(DELIMITER_COMPONENT) < 0) {
			final int length= string.length();
			final StringBuffer buffer= new StringBuffer(length);
			for (int index= 0; index < length; index++) {
				final char character= string.charAt(index);
				if (DELIMITER_COMPONENT == character) {
					if (index < length - 1) {
						final char escape= string.charAt(index + 1);
						if (DELIMITER_COMPONENT == escape)
							continue;
					}
				}
				buffer.append(character);
			}
			return buffer.toString();
		}
		return string;
	}

	/**
	 * Writes refactoring descriptors to the specified output stream.
	 * 
	 * @param stream
	 *            the output stream where to write to
	 * @param descriptors
	 *            the refactoring descriptors to write
	 * @param stamps
	 *            <code>true</code> to write time stamps as well,
	 *            <code>false</code> otherwise
	 * @throws CoreException
	 *             if an error occurs while writing the descriptors
	 */
	public static void writeRefactoringDescriptors(final OutputStream stream, final RefactoringDescriptor[] descriptors, final boolean stamps) throws CoreException {
		writeRefactoringSession(stream, new RefactoringSessionDescriptor(descriptors, IRefactoringSerializationConstants.CURRENT_VERSION, null), stamps);
	}

	/**
	 * Writes refactoring session descriptor to the specified output stream.
	 * 
	 * @param stream
	 *            the output stream where to write to
	 * @param descriptor
	 *            the refactoring session descriptors to write
	 * @param stamps
	 *            <code>true</code> to write time stamps as well,
	 *            <code>false</code> otherwise
	 * @throws CoreException
	 *             if an error occurs while writing the refactoring session
	 *             descriptor
	 */
	public static void writeRefactoringSession(final OutputStream stream, final RefactoringSessionDescriptor descriptor, final boolean stamps) throws CoreException {
		final RefactoringSessionTransformer transformer= new RefactoringSessionTransformer(true);
		final RefactoringDescriptor[] descriptors= descriptor.getRefactorings();
		try {
			transformer.beginSession(descriptor.getComment());
			for (int index= 0; index < descriptors.length; index++) {
				final RefactoringDescriptor current= descriptors[index];
				if (current != null) {
					try {
						long stamp= stamps ? current.getTimeStamp() : -1;
						final String id= current.getID();
						transformer.beginRefactoring(id, stamp, current.getProject(), current.getDescription(), current.getComment(), current.getFlags());
						final Map arguments= getArgumentMap(current);
						if (arguments != null) {
							checkArgumentMap(arguments);
							for (final Iterator iterator= arguments.entrySet().iterator(); iterator.hasNext();) {
								final Map.Entry entry= (Entry) iterator.next();
								transformer.createArgument((String) entry.getKey(), (String) entry.getValue());
							}
						}
					} finally {
						transformer.endRefactoring();
					}
				}
			}
		} finally {
			transformer.endSession();
		}
		final Object result= transformer.getResult();
		if (result instanceof Node) {
			try {
				final Transformer transform= TransformerFactory.newInstance().newTransformer();
				transform.setOutputProperty(OutputKeys.INDENT, IRefactoringSerializationConstants.OUTPUT_INDENT);
				transform.setOutputProperty(OutputKeys.METHOD, IRefactoringSerializationConstants.OUTPUT_METHOD);
				transform.setOutputProperty(OutputKeys.ENCODING, IRefactoringSerializationConstants.OUTPUT_ENCODING);
				transform.transform(new DOMSource((Node) result), new StreamResult(stream));
			} catch (TransformerConfigurationException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getLocalizedMessage(), exception));
			} catch (TransformerFactoryConfigurationError exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getLocalizedMessage(), exception));
			} catch (TransformerException exception) {
				final Throwable throwable= exception.getException();
				if (throwable instanceof IOException)
					throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, throwable.getLocalizedMessage(), throwable));
				RefactoringCorePlugin.log(exception);
			}
		}
	}

	/** The cached session descriptor, or <code>null</code> */
	private RefactoringSessionDescriptor fCachedDescriptor= null;

	/** The cached document, or <code>null</code> */
	private Document fCachedDocument= null;

	/** The cached path, or <code>null</code> */
	private IPath fCachedPath= null;

	/** The cached file store, or <code>null</code> */
	private IFileStore fCachedStore= null;

	/** The history file store */
	private final IFileStore fHistoryStore;

	/**
	 * The non-empty name of the managed project, or <code>null</code> for
	 * the workspace
	 */
	private final String fProjectName;

	/**
	 * Creates a new refactoring history manager.
	 * 
	 * @param store
	 *            the history file store
	 * @param name
	 *            the non-empty name of the managed project, or
	 *            <code>null</code> for the workspace
	 */
	RefactoringHistoryManager(final IFileStore store, final String name) {
		Assert.isNotNull(store);
		Assert.isTrue(name == null || !"".equals(name)); //$NON-NLS-1$
		fHistoryStore= store;
		fProjectName= name;
	}

	/**
	 * Adds the specified refactoring descriptor to the refactoring history.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to add
	 * @param sort
	 *            <code>true</code> if the refactoring descriptor should be
	 *            inserted into the history according to its time stamp,
	 *            <code>false</code> if the descriptor is assumed to be the
	 *            most recent one, and its simply appended
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while adding the descriptor to the history
	 */
	void addRefactoringDescriptor(final RefactoringDescriptor descriptor, boolean sort, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 5);
			final long stamp= descriptor.getTimeStamp();
			if (stamp >= 0) {
				final IPath path= stampToPath(stamp);
				final IFileStore folder= fHistoryStore.getChild(path);
				if (folder != null) {
					final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
					final IFileStore index= folder.getChild(RefactoringHistoryService.NAME_INDEX_FILE);
					if (history != null && index != null) {
						if (history.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
							InputStream input= null;
							try {
								input= new BufferedInputStream(history.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
								final Document document= getCachedDocument(path, input);
								try {
									input.close();
									input= null;
								} catch (IOException exception) {
									// Do nothing
								}
								monitor.worked(1);
								final Object result= transformDescriptor(descriptor, false);
								if (result instanceof Document) {
									boolean found= false;
									final NodeList list= ((Document) result).getElementsByTagName(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
									final Element root= document.getDocumentElement();
									if (sort) {
										final String string= Long.toString(stamp);
										for (int offset= 0; offset < list.getLength(); offset++) {
											final Element element= (Element) list.item(offset);
											final String attribute= element.getAttribute(IRefactoringSerializationConstants.ATTRIBUTE_STAMP);
											if (attribute != null) {
												if (string.compareTo(attribute) > 0) {
													root.insertBefore(document.importNode(element, true), element);
													found= true;
													break;
												}
											}
										}
									}
									if (!found)
										root.appendChild(document.importNode(list.item(0), true));
									writeHistoryEntry(history, document, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
									addIndexEntry(index, descriptor, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
								}
							} catch (ParserConfigurationException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							} catch (IOException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							} catch (SAXException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							} finally {
								if (input != null) {
									try {
										input.close();
									} catch (IOException exception) {
										// Do nothing
									}
								}
							}
						} else {
							try {
								final Object result= transformDescriptor(descriptor, false);
								if (result instanceof Node) {
									writeHistoryEntry(history, (Node) result, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
									appendIndexEntry(index, descriptor, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
								}
							} catch (IOException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							}
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the cached refactoring history document.
	 * 
	 * @param path
	 *            the path of the document
	 * @param input
	 *            the input stream where to read the document
	 * @return the cached refactoring history document
	 * @throws SAXException
	 *             if an error occurs while parsing the history entry
	 * @throws IOException
	 *             if an input/output error occurs
	 * @throws ParserConfigurationException
	 *             if an error occurs in the parser configuration
	 */
	private Document getCachedDocument(final IPath path, final InputStream input) throws SAXException, IOException, ParserConfigurationException {
		if (path.equals(fCachedPath) && fCachedDocument != null)
			return fCachedDocument;
		final Document document= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(input));
		fCachedDocument= document;
		fCachedPath= path;
		return document;
	}

	/**
	 * Returns the cached refactoring session descriptor.
	 * 
	 * @param store
	 *            the file store of the descriptor
	 * @param input
	 *            the input stream where to read the descriptor
	 * @return the cached refactoring session descriptor
	 * @throws CoreException
	 *             if an error occurs while reading the session
	 */
	private RefactoringSessionDescriptor getCachedSession(final IFileStore store, final InputStream input) throws CoreException {
		if (store.equals(fCachedStore) && fCachedDescriptor != null)
			return fCachedDescriptor;
		final RefactoringSessionDescriptor descriptor= new RefactoringSessionReader(true).readSession(new InputSource(input));
		fCachedDescriptor= descriptor;
		fCachedStore= store;
		return descriptor;
	}

	/**
	 * Reads the refactoring history from disk.
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param flags
	 *            the flags which must be present
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring history
	 */
	RefactoringHistory readRefactoringHistory(final long start, final long end, final int flags, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 200);
			final Set set= new HashSet();
			try {
				if (fHistoryStore.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists())
					readRefactoringDescriptorProxies(fHistoryStore, fProjectName, set, start, end, flags, new SubProgressMonitor(monitor, 80));
				final IFileStore store= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER).getChild(RefactoringHistoryService.NAME_WORKSPACE_PROJECT);
				if (store.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists())
					readRefactoringDescriptorProxies(store, null, set, start, end, flags, new SubProgressMonitor(monitor, 80));
			} catch (CoreException exception) {
				RefactoringCorePlugin.log(exception);
			}
			final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[set.size()];
			set.toArray(proxies);
			return new RefactoringHistoryImplementation(proxies);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Removes a refactoring descriptor from the managed history.
	 * 
	 * @param stamp
	 *            the time stamp of the refactoring descriptor
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	void removeRefactoringDescriptor(final long stamp, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 6);
			final IPath path= stampToPath(stamp);
			final IFileStore folder= fHistoryStore.getChild(path);
			if (folder != null) {
				final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
				final IFileStore index= folder.getChild(RefactoringHistoryService.NAME_INDEX_FILE);
				if (history != null && index != null && history.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists() && index.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
					InputStream input= null;
					try {
						input= new BufferedInputStream(history.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
						final Document document= getCachedDocument(path, input);
						final NodeList list= document.getElementsByTagName(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
						final int length= list.getLength();
						for (int offset= 0; offset < length; offset++) {
							final Node node= list.item(offset);
							final NamedNodeMap attributes= node.getAttributes();
							if (attributes != null) {
								final Node item= attributes.getNamedItem(IRefactoringSerializationConstants.ATTRIBUTE_STAMP);
								if (item != null) {
									final String value= item.getNodeValue();
									if (String.valueOf(stamp).equals(value)) {
										try {
											input.close();
											input= null;
										} catch (IOException exception) {
											// Do nothing
										}
										if (length == 1)
											removeIndexTree(folder, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
										else {
											node.getParentNode().removeChild(node);
											writeHistoryEntry(history, document, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
											removeIndexEntry(index, stamp, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
										}
										break;
									}
								}
							}
						}
					} catch (ParserConfigurationException exception) {
						throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
					} catch (IOException exception) {
						throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
					} catch (SAXException exception) {
						throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
					} finally {
						if (input != null) {
							try {
								input.close();
							} catch (IOException exception) {
								// Do nothing
							}
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the descriptor the specified proxy points to.
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @param monitor
	 *            the progress monitor to use
	 * @return the associated refactoring descriptor, or <code>null</code>
	 */
	RefactoringDescriptor requestDescriptor(final RefactoringDescriptorProxy proxy, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_resolving_information, 2);
			final long stamp= proxy.getTimeStamp();
			if (stamp >= 0) {
				InputStream input= null;
				try {
					final IFileStore folder= fHistoryStore.getChild(stampToPath(stamp));
					if (folder != null) {
						final IFileStore file= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
						if (file != null && file.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
							input= new BufferedInputStream(file.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
							final RefactoringSessionDescriptor descriptor= getCachedSession(file, input);
							if (descriptor != null) {
								final RefactoringDescriptor[] descriptors= descriptor.getRefactorings();
								for (int index= 0; index < descriptors.length; index++) {
									if (descriptors[index].getTimeStamp() == stamp) {
										final RefactoringDescriptor result= descriptors[index];
										result.setProject(fProjectName);
										return result;
									}
								}
							}
						}
					}
				} catch (CoreException exception) {
					// Do nothing
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException exception) {
						// Do nothing
					}
				}
			}
		} finally {
			monitor.done();
		}
		return null;
	}

	/**
	 * Sets the comment of the specified refactoring.
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @param comment
	 *            the comment
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while setting the comment
	 */
	void setComment(final RefactoringDescriptorProxy proxy, final String comment, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 100);
			final long stamp= proxy.getTimeStamp();
			if (stamp >= 0) {
				final IPath path= stampToPath(stamp);
				final IFileStore folder= fHistoryStore.getChild(path);
				if (folder != null) {
					final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
					if (history != null) {
						if (history.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
							InputStream input= null;
							try {
								input= new BufferedInputStream(history.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 40, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
								final Document document= getCachedDocument(path, input);
								try {
									input.close();
									input= null;
								} catch (IOException exception) {
									// Do nothing
								}
								final String time= String.valueOf(stamp);
								final NodeList list= document.getElementsByTagName(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
								for (int index= 0; index < list.getLength(); index++) {
									final Element element= (Element) list.item(index);
									if (time.equals(element.getAttribute(IRefactoringSerializationConstants.ATTRIBUTE_STAMP)))
										element.setAttribute(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT, comment);
								}
								writeHistoryEntry(history, document, new SubProgressMonitor(monitor, 40, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							} catch (ParserConfigurationException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							} catch (IOException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							} catch (SAXException exception) {
								throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
							} finally {
								if (input != null) {
									try {
										input.close();
									} catch (IOException exception) {
										// Do nothing
									}
								}
							}
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Writes the specified document node into the refactoring history.
	 * 
	 * @param file
	 *            the refactoring history file
	 * @param node
	 *            the node representing the history entry
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while adding the history entry
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private void writeHistoryEntry(final IFileStore file, final Node node, final IProgressMonitor monitor) throws CoreException, IOException {
		OutputStream output= null;
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 2);
			try {
				file.getParent().mkdir(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
				output= new BufferedOutputStream(file.openOutputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
				final Transformer transformer= TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, IRefactoringSerializationConstants.OUTPUT_INDENT);
				transformer.setOutputProperty(OutputKeys.METHOD, IRefactoringSerializationConstants.OUTPUT_METHOD);
				transformer.setOutputProperty(OutputKeys.ENCODING, IRefactoringSerializationConstants.OUTPUT_ENCODING);
				try {
					transformer.transform(new DOMSource(node), new StreamResult(output));
				} finally {
					fCachedDocument= null;
					fCachedPath= null;
					fCachedDescriptor= null;
					fCachedStore= null;
				}
			} catch (TransformerConfigurationException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
			} catch (TransformerFactoryConfigurationError exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
			} catch (TransformerException exception) {
				final Throwable throwable= exception.getException();
				if (throwable instanceof IOException)
					throw (IOException) throwable;
				RefactoringCorePlugin.log(exception);
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException exception) {
						// Do nothing
					}
				}
			}
		} finally {
			monitor.done();
		}
	}
}
