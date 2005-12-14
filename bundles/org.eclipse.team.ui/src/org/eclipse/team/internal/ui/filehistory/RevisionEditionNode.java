package org.eclipse.team.internal.ui.filehistory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.history.IFileRevision;

/**
 * A class for comparing IFileRevision objects
 */
public class RevisionEditionNode implements IStructureComparator, ITypedElement, IEncodedStreamContentAccessor  {

	private IFileRevision revision;
	private RevisionEditionNode[] children;
	
	/**
	 * Creates a new RevisionEditionNode on the given resource edition.
	 */
	public RevisionEditionNode(IFileRevision revision) {
		this.revision = revision;
	}
	
	public Object[] getChildren() {
		if (children == null) {
			children = new RevisionEditionNode[0];
			/*if (revision != null) {
				try {
					TeamUIPlugin.runWithProgress(null, true cancelable, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								IFileRevision[] members = revision.members(monitor);
								children = new RevisionEditionNode[members.length];
								for (int i = 0; i < members.length; i++) {
									children[i] = new RevisionEditionNode(members[i]);
								}
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InterruptedException e) {
					// operation canceled
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof TeamException) {
						TeamUIPlugin.log(((TeamException) t));
					}
				}
			}*/
		}
		return children;
	}

	public String getName() {
		return revision == null ? "" : revision.getName(); //$NON-NLS-1$
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		if (revision == null) {
			return UNKNOWN_TYPE;
		}
	
		String name = revision.getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name.length() == 0 ? UNKNOWN_TYPE : name;
	}

	public String getCharset() throws CoreException {
		// See if the remote file has an encoding
		// TODO: need progress monitor
		IStorage storage = revision.getStorage(new NullProgressMonitor());
		if (storage instanceof IEncodedStorage) {
			String charset = ((IEncodedStorage)storage).getCharset();
			if (charset != null) {
				return charset;
			}
		}
		return null;
	}

	public InputStream getContents() throws CoreException {
		//TODO: monitor
		IStorage storage = revision.getStorage(new NullProgressMonitor());
		if (storage != null) {
			return storage.getContents();
		}
		return new ByteArrayInputStream(new byte[0]);
	}
	
	public IFileRevision getFileRevision() {
		return revision;
	}

}
