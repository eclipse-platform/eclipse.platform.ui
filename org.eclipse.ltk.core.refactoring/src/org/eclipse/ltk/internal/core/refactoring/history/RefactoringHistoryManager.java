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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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

	/** The calendar instance */
	private static final Calendar fgCalendar= Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00")); //$NON-NLS-1$

	/**
	 * Checks whether the argument is well-formed.
	 * 
	 * @param argument
	 *            the argument
	 * @param whitespace
	 *            <code>true</code> if the arguments should be checked for
	 *            whitespace characters, <code>false</code> otherwise
	 * @throws CoreException
	 *             if the argument violates any of the constraints
	 */
	private static void checkArgument(final Object argument, final boolean whitespace) throws CoreException {
		if (argument instanceof String) {
			final String string= (String) argument;
			final char[] characters= string.toCharArray();
			if (characters.length == 0)
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, RefactoringCoreMessages.RefactoringHistoryManager_empty_argument, null));
			if (whitespace) {
				for (int index= 0; index < characters.length; index++) {
					if (Character.isWhitespace(characters[index]))
						throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, RefactoringCoreMessages.RefactoringHistoryManager_whitespace_argument_key, null));
				}
			}
		} else
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, RefactoringCoreMessages.RefactoringHistoryManager_non_string_argument, null));
	}

	/**
	 * Checks whether the argument map is well-formed.
	 * <p>
	 * All arguments contained in the map are checked according to the rules of
	 * {@link RefactoringDescriptor}.
	 * </p>
	 * 
	 * @param arguments
	 *            the argument map
	 * @throws CoreException
	 *             if the argument violates any of the constraints
	 */
	public static void checkArgumentMap(final Map arguments) throws CoreException {
		Assert.isNotNull(arguments);
		for (final Iterator iterator= arguments.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry entry= (Map.Entry) iterator.next();
			checkArgument(entry.getKey(), true);
			checkArgument(entry.getValue(), false);
		}
	}

	/**
	 * Creates a new core exception representing an I/O error.
	 * 
	 * @param exception
	 *            the throwable to wrap
	 * @return the core exception
	 */
	private static CoreException createCoreException(final Throwable exception) {
		return new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getLocalizedMessage(), exception));
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
			final StringBuffer buffer= new StringBuffer(length + 16);
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
	 * @param task
	 *            the task label to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	private static void readRefactoringDescriptorProxies(final IFileStore store, final String project, final Collection collection, final long start, final long end, final int flags, final IProgressMonitor monitor, final String task) throws CoreException {
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
					throw createCoreException(exception);
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
				subMonitor.beginTask(task, stores.length);
				for (int index= 0; index < stores.length; index++)
					readRefactoringDescriptorProxies(stores[index], project, collection, start, end, flags, new SubProgressMonitor(subMonitor, 1), task);
			} finally {
				subMonitor.done();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Reads refactoring descriptor proxies from the specified input stream.
	 * <p>
	 * The refactoring descriptor proxies are returned in no particular order.
	 * </p>
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
	 * Reads refactoring descriptors from the specified input stream.
	 * <p>
	 * The refactoring descriptors are returned in no particular order.
	 * </p>
	 * 
	 * @param stream
	 *            the input stream where to read from
	 * @return An array of refactoring descriptor proxies
	 * @throws CoreException
	 *             if an error occurs while reading the descriptors
	 */
	public static RefactoringDescriptor[] readRefactoringDescriptors(final InputStream stream) throws CoreException {
		final List list= new ArrayList(64);
		readRefactoringDescriptors(stream, list, new NullProgressMonitor());
		return (RefactoringDescriptor[]) list.toArray(new RefactoringDescriptor[list.size()]);
	}

	/**
	 * Reads refactoring descriptors from the specified input stream.
	 * 
	 * @param stream
	 *            the input stream where to read from
	 * @param collection
	 *            the list of descriptors read from the history
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs while reading the descriptors
	 */
	private static void readRefactoringDescriptors(final InputStream stream, final Collection collection, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 1);
			final RefactoringDescriptor[] results= new RefactoringSessionReader(true).readSession(new InputSource(new BufferedInputStream(stream))).getRefactorings();
			for (int index= 0; index < results.length; index++)
				collection.add(results[index]);
		} finally {
			monitor.done();
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
	 * @param task
	 *            the task label to use
	 * @throws CoreException
	 *             if an error occurs while removing the index tree
	 */
	private static void removeIndexTree(final IFileStore store, final IProgressMonitor monitor, final String task) throws CoreException {
		try {
			monitor.beginTask(task, 16);
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
			removeIndexTree(parent, new SubProgressMonitor(monitor, 12, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), task);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Sorts the refactoring descriptor proxies in ascending order of their time
	 * stamps.
	 * 
	 * @param descriptors
	 *            the refactoring descriptors
	 */
	public static void sortRefactoringDescriptorsAscending(final RefactoringDescriptor[] descriptors) {
		Arrays.sort(descriptors, new Comparator() {

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
	}

	/**
	 * Sorts the refactoring descriptor proxies in ascending order of their time
	 * stamps.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public static void sortRefactoringDescriptorsAscending(final RefactoringDescriptorProxy[] proxies) {
		Arrays.sort(proxies, new Comparator() {

			public final int compare(final Object first, final Object second) {
				final RefactoringDescriptorProxy predecessor= (RefactoringDescriptorProxy) first;
				final RefactoringDescriptorProxy successor= (RefactoringDescriptorProxy) second;
				final long delta= predecessor.getTimeStamp() - successor.getTimeStamp();
				if (delta > 0)
					return 1;
				else if (delta < 0)
					return -1;
				return 0;
			}
		});
	}

	/**
	 * Sorts the refactoring descriptor proxies in descending order of their
	 * time stamps.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public static void sortRefactoringDescriptorsDescending(final RefactoringDescriptorProxy[] proxies) {
		Arrays.sort(proxies, new Comparator() {

			public final int compare(final Object first, final Object second) {
				final RefactoringDescriptorProxy predecessor= (RefactoringDescriptorProxy) first;
				final RefactoringDescriptorProxy successor= (RefactoringDescriptorProxy) second;
				final long delta= successor.getTimeStamp() - predecessor.getTimeStamp();
				if (delta > 0)
					return 1;
				else if (delta < 0)
					return -1;
				return 0;
			}
		});
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
		fgCalendar.setTimeInMillis(stamp);
		final StringBuffer buffer= new StringBuffer(256);
		buffer.append(fgCalendar.get(Calendar.YEAR));
		buffer.append(IPath.SEPARATOR);
		buffer.append(fgCalendar.get(Calendar.MONTH) + 1);
		buffer.append(IPath.SEPARATOR);
		buffer.append(fgCalendar.get(Calendar.WEEK_OF_YEAR));
		return new Path(buffer.toString());
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
			transformer.beginSession(null, IRefactoringSerializationConstants.CURRENT_VERSION);
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
	 * Writes the specified index entry to the refactoring history.
	 * 
	 * @param file
	 *            the history index file
	 * @param proxies
	 *            the refactoring descriptors
	 * @param flags
	 *            the flags to use (either {@link EFS#NONE} or
	 *            {@link EFS#APPEND})
	 * @param monitor
	 *            the progress monitor to use
	 * @param task
	 *            the task label
	 * @throws CoreException
	 *             if an error occurs while writing the index entry
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private static void writeIndexEntry(final IFileStore file, final RefactoringDescriptorProxy[] proxies, final int flags, final IProgressMonitor monitor, final String task) throws CoreException, IOException {
		OutputStream output= null;
		try {
			monitor.beginTask(task, 2);
			file.getParent().mkdir(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			output= new BufferedOutputStream(file.openOutputStream(flags, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
			writeRefactoringDescriptorProxies(output, proxies);
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
	 * Writes refactoring descriptor proxies to the specified output stream.
	 * 
	 * @param stream
	 *            the output stream where to write to
	 * @param proxies
	 *            the refactoring descriptor proxies to write
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	public static void writeRefactoringDescriptorProxies(final OutputStream stream, final RefactoringDescriptorProxy[] proxies) throws IOException {
		final StringBuffer buffer= new StringBuffer(proxies.length * 64);
		sortRefactoringDescriptorsAscending(proxies);
		for (int index= 0; index < proxies.length; index++) {
			buffer.append(proxies[index].getTimeStamp());
			buffer.append(DELIMITER_COMPONENT);
			buffer.append(escapeString(proxies[index].getDescription()));
			buffer.append(DELIMITER_ENTRY);
		}
		stream.write(buffer.toString().getBytes(IRefactoringSerializationConstants.OUTPUT_ENCODING));
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
			transformer.beginSession(descriptor.getComment(), descriptor.getVersion());
			for (int index= 0; index < descriptors.length; index++) {
				final RefactoringDescriptor current= descriptors[index];
				if (current != null) {
					try {
						long stamp= stamps ? current.getTimeStamp() : -1;
						transformer.beginRefactoring(current.getID(), stamp, current.getProject(), current.getDescription(), current.getComment(), current.getFlags());
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
				throw createCoreException(exception);
			} catch (TransformerFactoryConfigurationError exception) {
				throw createCoreException(exception);
			} catch (TransformerException exception) {
				final Throwable throwable= exception.getException();
				if (throwable instanceof IOException)
					throw createCoreException(exception);
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
	 * The non-empty name of the managed project, or <code>null</code> for the
	 * workspace
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
	void addRefactoringDescriptor(final RefactoringDescriptor descriptor, final boolean sort, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 18);
			final long stamp= descriptor.getTimeStamp();
			if (stamp >= 0) {
				final IPath path= stampToPath(stamp);
				final IFileStore folder= fHistoryStore.getChild(path);
				final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
				final IFileStore index= folder.getChild(RefactoringHistoryService.NAME_INDEX_FILE);
				final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[] { new DefaultRefactoringDescriptorProxy(descriptor.getDescription(), descriptor.getProject(), descriptor.getTimeStamp())};
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
							writeHistoryEntry(history, document, new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
							if (sort) {
								final Set set= new HashSet(64);
								readRefactoringDescriptorProxies(index, null, set, 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, new SubProgressMonitor(monitor, 2), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
								writeIndexEntry(index, (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]), EFS.NONE, new SubProgressMonitor(monitor, 3, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
							} else
								writeIndexEntry(index, proxies, EFS.APPEND, new SubProgressMonitor(monitor, 5, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
						}
					} catch (ParserConfigurationException exception) {
						throw createCoreException(exception);
					} catch (IOException exception) {
						throw createCoreException(exception);
					} catch (SAXException exception) {
						throw createCoreException(exception);
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
							writeHistoryEntry(history, (Node) result, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
							writeIndexEntry(index, proxies, EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
						}
					} catch (IOException exception) {
						throw createCoreException(exception);
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
					readRefactoringDescriptorProxies(fHistoryStore, fProjectName, set, start, end, flags, new SubProgressMonitor(monitor, 80), RefactoringCoreMessages.RefactoringHistoryService_retrieving_history);
				final IFileStore store= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER).getChild(RefactoringHistoryService.NAME_WORKSPACE_PROJECT);
				if (store.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists())
					readRefactoringDescriptorProxies(store, null, set, start, end, flags, new SubProgressMonitor(monitor, 80), RefactoringCoreMessages.RefactoringHistoryService_retrieving_history);
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
	 * Removes refactoring descriptors from the managed history.
	 * <p>
	 * All refactoring descriptors must be from the history entry denoted by the
	 * specified path.
	 * </p>
	 * 
	 * @param proxies
	 *            the refactoring descriptors
	 * @param path
	 *            the path of the history entry
	 * @param monitor
	 *            the progress monitor to use
	 * @param task
	 *            the task label to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	private void removeRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, final IPath path, final IProgressMonitor monitor, final String task) throws CoreException {
		try {
			monitor.beginTask(task, 5);
			final IFileStore folder= fHistoryStore.getChild(path);
			final IFileStore index= folder.getChild(RefactoringHistoryService.NAME_INDEX_FILE);
			if (index.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
				final Set resultingProxies= new HashSet(64);
				readRefactoringDescriptorProxies(index, null, resultingProxies, 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, new SubProgressMonitor(monitor, 1), task);
				if (resultingProxies.size() == proxies.length)
					removeIndexTree(folder, new SubProgressMonitor(monitor, 1), task);
				else {
					final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
					if (history.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
						InputStream input= null;
						Document document= null;
						try {
							input= new BufferedInputStream(history.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
							document= getCachedDocument(path, input);
						} catch (ParserConfigurationException exception) {
							throw createCoreException(exception);
						} catch (IOException exception) {
							throw createCoreException(exception);
						} catch (SAXException exception) {
							throw createCoreException(exception);
						} finally {
							if (input != null) {
								try {
									input.close();
								} catch (IOException exception) {
									// Do nothing
								}
							}
						}
						final Set removedNodes= new HashSet(proxies.length);
						final NodeList list= document.getElementsByTagName(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
						final int length= list.getLength();
						for (int offset= 0; offset < length; offset++) {
							final Node node= list.item(offset);
							final NamedNodeMap attributes= node.getAttributes();
							if (attributes != null) {
								final Node item= attributes.getNamedItem(IRefactoringSerializationConstants.ATTRIBUTE_STAMP);
								if (item != null) {
									final String value= item.getNodeValue();
									if (value != null) {
										for (int current= 0; current < proxies.length; current++) {
											final RefactoringDescriptorProxy proxy= proxies[current];
											final long stamp= proxy.getTimeStamp();
											if (value.equals(String.valueOf(stamp))) {
												resultingProxies.remove(new DefaultRefactoringDescriptorProxy(proxy.getDescription(), proxy.getProject(), stamp));
												removedNodes.add(node);
											}
										}
									}
								}
							}
						}
						for (final Iterator iterator= removedNodes.iterator(); iterator.hasNext();) {
							final Node node= (Node) iterator.next();
							node.getParentNode().removeChild(node);
						}
						try {
							writeIndexEntry(index, (RefactoringDescriptorProxy[]) resultingProxies.toArray(new RefactoringDescriptorProxy[resultingProxies.size()]), EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), task);
							writeHistoryEntry(history, document, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), task);
						} catch (IOException exception) {
							throw createCoreException(exception);
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Removes refactoring descriptors from the managed history.
	 * 
	 * @param proxies
	 *            the refactoring descriptors
	 * @param monitor
	 *            the progress monitor to use
	 * @param task
	 *            the task label to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	void removeRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, final IProgressMonitor monitor, final String task) throws CoreException {
		try {
			final Map paths= new HashMap();
			monitor.beginTask(task, proxies.length + 300);
			for (int index= 0; index < proxies.length; index++) {
				final IPath path= stampToPath(proxies[index].getTimeStamp());
				Collection collection= (Collection) paths.get(path);
				if (collection == null) {
					collection= new ArrayList(64);
					paths.put(path, collection);
				}
				collection.add(proxies[index]);
			}
			final IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 300);
			try {
				final Set entries= paths.entrySet();
				subMonitor.beginTask(task, entries.size());
				for (final Iterator iterator= entries.iterator(); iterator.hasNext();) {
					final Map.Entry entry= (Map.Entry) iterator.next();
					final Collection collection= (Collection) entry.getValue();
					removeRefactoringDescriptors((RefactoringDescriptorProxy[]) collection.toArray(new RefactoringDescriptorProxy[collection.size()]), (IPath) entry.getKey(), new SubProgressMonitor(subMonitor, 1), task);
				}
			} finally {
				subMonitor.done();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Requests the resolved refactoring descriptor associated with the given
	 * proxy.
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
					final IFileStore file= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
					if (file.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
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
				final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_HISTORY_FILE);
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
							if (time.equals(element.getAttribute(IRefactoringSerializationConstants.ATTRIBUTE_STAMP))) {
								element.setAttribute(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT, comment);
								break;
							}
						}
						writeHistoryEntry(history, document, new SubProgressMonitor(monitor, 40, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), RefactoringCoreMessages.RefactoringHistoryService_updating_history);
					} catch (ParserConfigurationException exception) {
						throw createCoreException(exception);
					} catch (IOException exception) {
						throw createCoreException(exception);
					} catch (SAXException exception) {
						throw createCoreException(exception);
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
	 * Writes the specified document node into the refactoring history.
	 * 
	 * @param file
	 *            the refactoring history file
	 * @param node
	 *            the node representing the history entry
	 * @param monitor
	 *            the progress monitor to use
	 * @param task
	 *            the task label
	 * @throws CoreException
	 *             if an error occurs while adding the history entry
	 */
	private void writeHistoryEntry(final IFileStore file, final Node node, final IProgressMonitor monitor, final String task) throws CoreException {
		OutputStream output= null;
		try {
			monitor.beginTask(task, 2);
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
				throw createCoreException(exception);
			} catch (TransformerFactoryConfigurationError exception) {
				throw createCoreException(exception);
			} catch (TransformerException exception) {
				final Throwable throwable= exception.getException();
				if (throwable instanceof IOException)
					throw createCoreException(exception);
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
