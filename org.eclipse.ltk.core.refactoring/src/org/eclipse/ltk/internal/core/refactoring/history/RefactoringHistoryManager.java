/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

import org.w3c.dom.Document;
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

	/** The index entry delimiter */
	private static final char DELIMITER_ENTRY= '\n';

	/** The index stamp delimiter */
	private static final char DELIMITER_STAMP= '\t';

	/** The index file name */
	private static final String NAME_INDEX_FILE= "index.dat"; //$NON-NLS-1$

	/**
	 * Transforms the specified descriptor into a history object.
	 * 
	 * @param descriptor
	 *            the descriptor to transform
	 * @return the transformation result
	 * @throws CoreException
	 *             if an error occurs
	 */
	private static Object descriptorToNode(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final IRefactoringSessionTransformer transformer= new XmlRefactoringSessionTransformer();
		try {
			transformer.beginSession(null);
			try {
				transformer.beginRefactoring(descriptor.getID(), descriptor.getTimeStamp(), descriptor.getProject(), descriptor.getDescription(), descriptor.getComment());
				for (final Iterator iterator= descriptor.getArguments().entrySet().iterator(); iterator.hasNext();) {
					final Map.Entry entry= (Entry) iterator.next();
					transformer.createArgument((String) entry.getKey(), (String) entry.getValue());
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
	 * Reads the specified number of refactoring descriptors from the head of
	 * the history.
	 * 
	 * @param store
	 *            the file store
	 * @param descriptors
	 *            the list of read descriptors
	 * @param count
	 *            the total number of descriptors to be read
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	private static void readDescriptors(final IFileStore store, final List descriptors, final int count, final IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(descriptors);
		Assert.isNotNull(monitor);
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 16);
			if (count > 0) {
				final IFileInfo info= store.fetchInfo();
				if (store.getName().equalsIgnoreCase(RefactoringHistoryService.NAME_REFACTORING_HISTORY) && !info.isDirectory()) {
					final RefactoringDescriptor[] results= new XmlRefactoringSessionReader().readSession(new InputSource(new BufferedInputStream(store.openInputStream(EFS.NONE, null)))).getRefactorings();
					monitor.worked(1);
					Arrays.sort(results, new Comparator() {

						public final int compare(final Object first, final Object second) {
							return (int) (((RefactoringDescriptor) first).getTimeStamp() - ((RefactoringDescriptor) second).getTimeStamp());
						}
					});
					monitor.worked(1);
					final int size= count - descriptors.size();
					for (int index= 0; index < results.length && index < size; index++)
						descriptors.add(results[index]);
					monitor.worked(1);
				} else
					monitor.worked(3);
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				final IFileStore[] stores= store.childStores(EFS.NONE, null);
				Arrays.sort(stores, new Comparator() {

					public final int compare(final Object first, final Object second) {
						return ((IFileStore) first).getName().compareTo(((IFileStore) second).getName());
					}
				});
				monitor.worked(1);
				final IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 12);
				try {
					subMonitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, stores.length);
					for (int index= 0; index < stores.length; index++)
						readDescriptors(stores[index], descriptors, count - descriptors.size(), new SubProgressMonitor(subMonitor, 1));
				} finally {
					subMonitor.done();
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Recursively deletes entries of the refactoring history file tree.
	 * 
	 * @param store
	 *            the file store
	 * @throws CoreException
	 *             if an error occurs
	 */
	public static void recursiveDelete(final IFileStore store) throws CoreException {
		Assert.isNotNull(store);
		final IFileInfo info= store.fetchInfo();
		if (info.isDirectory()) {
			if (info.getName().equalsIgnoreCase(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER))
				return;
			final IFileStore[] stores= store.childStores(EFS.NONE, null);
			for (int index= 0; index < stores.length; index++) {
				final IFileInfo current= stores[index].fetchInfo();
				if (current.isDirectory())
					return;
			}
		}
		final IFileStore parent= store.getParent();
		store.delete(0, null);
		recursiveDelete(parent);
	}

	/**
	 * Removes the specified index entry.
	 * 
	 * @param file
	 *            the history index file
	 * @param stamp
	 *            the time stamp
	 * @throws CoreException
	 *             if an error occurs
	 * @throws IOException
	 *             if an error occurs
	 */
	private static void removeIndexEntry(final IFileStore file, final long stamp) throws CoreException, IOException {
		InputStream input= null;
		try {
			if (file.fetchInfo().exists()) {
				input= new DataInputStream(new BufferedInputStream(file.openInputStream(EFS.NONE, null)));
				if (input != null) {
					final String value= new Long(stamp).toString();
					final BufferedReader reader= new BufferedReader(new InputStreamReader(input));
					final StringBuffer buffer= new StringBuffer();
					while (reader.ready()) {
						final String line= reader.readLine();
						if (!line.startsWith(value)) {
							buffer.append(line);
							buffer.append('\n');
						}
					}
					try {
						input.close();
						input= null;
					} catch (IOException exception) {
						// Do nothing
					}
					OutputStream output= null;
					try {
						file.getParent().mkdir(EFS.NONE, null);
						output= new BufferedOutputStream(file.openOutputStream(EFS.NONE, null));
						output.write(buffer.toString().getBytes());
					} finally {
						if (output != null) {
							try {
								output.close();
							} catch (IOException exception) {
								// Do nothing
							}
						}
					}
				}
			}
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException exception) {
				// Do nothing
			}
		}
	}

	/**
	 * Writes a refactoring history entry.
	 * 
	 * @param file
	 *            the refactoring history file
	 * @param node
	 *            the DOM node
	 * @throws CoreException
	 *             if an error occurs
	 * @throws IOException
	 *             if an error occurs
	 */
	private static void writeHistoryEntry(final IFileStore file, final Node node) throws CoreException, IOException {
		Assert.isNotNull(file);
		Assert.isNotNull(node);
		OutputStream output= null;
		try {
			file.getParent().mkdir(EFS.NONE, null);
			output= new BufferedOutputStream(file.openOutputStream(EFS.NONE, null));
			if (output != null) {
				final Transformer transformer= TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, IRefactoringSerializationConstants.OUTPUT_METHOD);
				transformer.setOutputProperty(OutputKeys.ENCODING, IRefactoringSerializationConstants.OUTPUT_ENCODING);
				transformer.transform(new DOMSource(node), new StreamResult(output));
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
	}

	/**
	 * Writes a refactoring history index.
	 * 
	 * @param file
	 *            the history index file
	 * @param stamp
	 *            the time stamp
	 * @param description
	 *            the description of the refactoring
	 * @throws CoreException
	 *             if an error occurs
	 * @throws IOException
	 *             if an error occurs
	 */
	private static void writeIndexEntry(final IFileStore file, final long stamp, final String description) throws CoreException, IOException {
		Assert.isNotNull(file);
		Assert.isNotNull(description);
		OutputStream output= null;
		try {
			file.getParent().mkdir(EFS.NONE, null);
			output= new BufferedOutputStream(file.openOutputStream(EFS.APPEND, null));
			final StringBuffer buffer= new StringBuffer(64);
			buffer.append(stamp);
			buffer.append(DELIMITER_STAMP);
			buffer.append(description);
			buffer.append(DELIMITER_ENTRY);
			output.write(buffer.toString().getBytes());
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException exception) {
					// Do nothing
				}
			}
		}
	}

	/** The history file store */
	private final IFileStore fHistoryStore;

	/** The name of the project whose history is managed */
	private final String fProjectName;

	/**
	 * Creates a new refactoring history manager.
	 * 
	 * @param store
	 *            the history file store
	 * @param name
	 *            the non-empty name of the managed project
	 */
	public RefactoringHistoryManager(final IFileStore store, final String name) {
		Assert.isNotNull(store);
		Assert.isTrue(name == null || !"".equals(name)); //$NON-NLS-1$
		fHistoryStore= store;
		fProjectName= name;
	}

	/**
	 * Adds a refactoring descriptor to the managed history.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to add
	 * @throws CoreException
	 *             if an error occurs
	 */
	void addDescriptor(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final long stamp= descriptor.getTimeStamp();
		if (stamp >= 0) {
			final IFileStore folder= stampToStore(stamp);
			if (folder != null) {
				final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_REFACTORING_HISTORY);
				final IFileStore index= folder.getChild(NAME_INDEX_FILE);
				if (history != null && index != null) {
					if (history.fetchInfo().exists()) {
						InputStream input= null;
						try {
							input= new BufferedInputStream(history.openInputStream(EFS.NONE, null));
							if (input != null) {
								final Document document= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(input));
								if (input != null) {
									try {
										input.close();
										input= null;
									} catch (IOException exception) {
										// Do nothing
									}
								}
								final Object result= descriptorToNode(descriptor);
								if (result instanceof Document) {
									final NodeList list= ((Document) result).getElementsByTagName(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
									Assert.isTrue(list.getLength() == 1);
									document.getDocumentElement().appendChild(document.importNode(list.item(0), true));
									writeHistoryEntry(history, document);
									writeIndexEntry(index, descriptor.getTimeStamp(), descriptor.getDescription());
								}
							}
						} catch (ParserConfigurationException exception) {
							new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
						} catch (IOException exception) {
							new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
						} catch (SAXException exception) {
							new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
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
							final Object result= descriptorToNode(descriptor);
							if (result instanceof Node) {
								writeHistoryEntry(history, (Node) result);
								writeIndexEntry(index, descriptor.getTimeStamp(), descriptor.getDescription());
							}
						} catch (IOException exception) {
							throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
						}
					}
				}
			}
		}
	}

	/**
	 * Is the refactoring history empty?
	 * 
	 * @return <code>true</code> if it is empty, <code>false</code>
	 *         otherwise
	 * @throws CoreException
	 *             if an error occurs
	 */
	boolean isEmpty() throws CoreException {
		if (fHistoryStore.fetchInfo().exists())
			return fHistoryStore.childStores(0, null).length == 0;
		return true;
	}

	/**
	 * Reads the specified number of refactoring descriptors from the head of
	 * the history.
	 * 
	 * @param count
	 *            the number of descriptors
	 * @param monitor
	 *            the progress monitor to use
	 * @return The refactoring descriptors, or an empty array
	 */
	RefactoringDescriptor[] readDescriptors(final int count, final IProgressMonitor monitor) {
		Assert.isTrue(count >= 0);
		Assert.isNotNull(monitor);
		final List list= new ArrayList(count);
		try {
			if (fHistoryStore.fetchInfo().exists())
				readDescriptors(fHistoryStore, list, count, monitor);
		} catch (CoreException exception) {
			RefactoringCorePlugin.log(exception);
		}
		final RefactoringDescriptor[] descriptors= new RefactoringDescriptor[list.size()];
		list.toArray(descriptors);
		return descriptors;
	}

	/**
	 * Reads the refactoring history from disk.
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring history
	 */
	RefactoringHistory readHistory(final long start, final long end, final IProgressMonitor monitor) {
		Assert.isTrue(end >= start);
		Assert.isNotNull(monitor);
		final List list= new ArrayList();
		try {
			if (fHistoryStore.fetchInfo().exists())
				readProxies(fHistoryStore, list, start, end, monitor);
		} catch (CoreException exception) {
			RefactoringCorePlugin.log(exception);
		}
		final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[list.size()];
		list.toArray(proxies);
		return new RefactoringHistoryImplementation(proxies);
	}

	/**
	 * Reads refactoring descriptor proxies.
	 * 
	 * @param store
	 *            the file store to read
	 * @param list
	 *            the list of proxies to fill in
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param monitor
	 *            the progress monitor to use
	 * @throws CoreException
	 *             if an error occurs
	 */
	private void readProxies(final IFileStore store, final List list, final long start, final long end, final IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(store);
		Assert.isNotNull(list);
		Assert.isNotNull(store);
		Assert.isNotNull(monitor);
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 16);
			final IFileInfo info= store.fetchInfo();
			if (store.getName().equalsIgnoreCase(NAME_INDEX_FILE) && !info.isDirectory() && info.exists()) {
				InputStream stream= null;
				try {
					stream= store.openInputStream(0, null);
					monitor.worked(1);
					final BufferedReader reader= new BufferedReader(new InputStreamReader(stream));
					while (reader.ready()) {
						if (monitor.isCanceled())
							throw new OperationCanceledException();
						final String line= reader.readLine();
						if (line != null) {
							final int index= line.indexOf(DELIMITER_STAMP);
							if (index > 0) {
								try {
									final long stamp= new Long(line.substring(0, index)).longValue();
									if (stamp >= start && stamp <= end)
										list.add(new RefactoringDescriptorProxy(line.substring(index + 1), fProjectName, stamp));
								} catch (NumberFormatException exception) {
									// Just skip
								}
							}
						}
					}
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
			final IFileStore[] stores= store.childStores(EFS.NONE, null);
			final IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 12);
			try {
				subMonitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, stores.length);
				for (int index= 0; index < stores.length; index++)
					readProxies(stores[index], list, start, end, new SubProgressMonitor(subMonitor, 1));
			} finally {
				subMonitor.done();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Removes a refactoring descriptor from the managed history.
	 * 
	 * @param stamp
	 *            the time stamp of the refactoring descriptor
	 * @throws CoreException
	 *             if an error occurs
	 */
	void removeDescriptor(long stamp) throws CoreException {
		Assert.isTrue(stamp >= 0);
		final IFileStore folder= stampToStore(stamp);
		if (folder != null) {
			final IFileStore history= folder.getChild(RefactoringHistoryService.NAME_REFACTORING_HISTORY);
			final IFileStore index= folder.getChild(NAME_INDEX_FILE);
			if (history != null && index != null && history.fetchInfo().exists() && index.fetchInfo().exists()) {
				InputStream input= null;
				try {
					input= new BufferedInputStream(history.openInputStream(EFS.NONE, null));
					if (input != null) {
						final Document document= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(input));
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
										node.getParentNode().removeChild(node);
										if (input != null) {
											try {
												input.close();
												input= null;
											} catch (IOException exception) {
												// Do nothing
											}
										}
										if (length == 1)
											recursiveDelete(folder);
										else {
											writeHistoryEntry(history, document);
											removeIndexEntry(index, stamp);
										}
										break;
									}
								}
							}
						}
					}
				} catch (ParserConfigurationException exception) {
					new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
				} catch (IOException exception) {
					new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
				} catch (SAXException exception) {
					new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
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

	/**
	 * Returns the descriptor the specified proxy points to.
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @return the associated refactoring descriptor, or <code>null</code>
	 */
	RefactoringDescriptor requestDescriptor(final RefactoringDescriptorProxy proxy) {
		Assert.isNotNull(proxy);
		final long stamp= proxy.getTimeStamp();
		if (stamp >= 0) {
			InputStream input= null;
			try {
				final IFileStore folder= stampToStore(stamp);
				if (folder != null) {
					final IFileStore file= folder.getChild(RefactoringHistoryService.NAME_REFACTORING_HISTORY);
					if (file != null && file.fetchInfo().exists()) {
						input= new BufferedInputStream(file.openInputStream(EFS.NONE, null));
						if (input != null)
							return new XmlRefactoringSessionReader().readDescriptor(new InputSource(input), stamp);
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
		return null;
	}

	/**
	 * Returns a file store representing the history part for the specified time
	 * stamp.
	 * 
	 * @param stamp
	 *            the time stamp
	 * @return A file store which may not exist
	 * @throws CoreException
	 *             if an error occurs
	 */
	private IFileStore stampToStore(final long stamp) throws CoreException {
		Assert.isTrue(stamp >= 0);
		final Calendar calendar= Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00")); //$NON-NLS-1$
		calendar.setTimeInMillis(stamp);
		IFileStore store= fHistoryStore;
		store= store.getChild(String.valueOf(calendar.get(Calendar.YEAR)));
		store= store.getChild(String.valueOf(calendar.get(Calendar.MONTH) + 1));
		store= store.getChild(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
		return store;
	}
}