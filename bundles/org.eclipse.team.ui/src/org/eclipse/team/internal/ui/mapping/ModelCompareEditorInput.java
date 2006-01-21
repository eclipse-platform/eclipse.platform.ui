package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.compare.IPrepareCompareInputAdapter;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;

public class ModelCompareEditorInput extends CompareEditorInput {

	private final ResourceMappingSynchronizeParticipant participant;
	private final ICompareInput input;

	public ModelCompareEditorInput(ResourceMappingSynchronizeParticipant participant, ICompareInput input) {
		super(new CompareConfiguration());
		Assert.isNotNull(participant);
		Assert.isNotNull(input);
		this.participant = participant;
		this.input = input;
	}

	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched
		// from the server
		setTitle(getTitle());
        monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			IPrepareCompareInputAdapter adapter = getPrepareAdapter(input);
			if (adapter != null) {
				adapter.prepareInput(input, getCompareConfiguration(), Policy.subMonitorFor(monitor, 100));
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
            monitor.done();
        }
		return input;
	}

	private IPrepareCompareInputAdapter getPrepareAdapter(ICompareInput input) {
		return (IPrepareCompareInputAdapter)Utils.getAdapter(input, IPrepareCompareInputAdapter.class);
	}

	/**
	 * Return whether the compare input of this editor input matches the
	 * given object.
	 * @param object the object
	 * @return whether the compare input of this editor input matches the
	 * given object
	 */
	public boolean matches(Object object) {
		// TODO it would be faster to ask the input it it matched the given object
		// but this would require additional API
		ICompareInput input = participant.asCompareInput(object);
		if (input != null)
			return input.equals(this.input);
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other instanceof ModelCompareEditorInput) {
			ModelCompareEditorInput otherInput = (ModelCompareEditorInput) other;
			return input.equals(otherInput.input);
		}
		return false;
	}

}
