package org.eclipse.team.internal.core.target;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.core.Policy;

public interface ITeamStatusConstants {

	public static final IStatus OK_STATUS =
		new Status(IStatus.OK, TeamPlugin.ID, TeamException.OK, "OK", null);

	public static final IStatus NOT_CHECKED_OUT_STATUS =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			TeamException.NOT_CHECKED_OUT,
			Policy.bind("teamStatus.notCheckedOut"),
			null);

	public static final IStatus NOT_CHECKED_IN_STATUS =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			TeamException.NOT_CHECKED_IN,
			Policy.bind("teamStatus.notCheckedIn"),
			null);

	public static final IStatus NO_REMOTE_RESOURCE_STATUS =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			TeamException.NO_REMOTE_RESOURCE,
			Policy.bind("teamStatus.noRemoteResource"),
			null);

	public static final IStatus IO_FAILED_STATUS =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			TeamException.IO_FAILED,
			Policy.bind("teamStatus.ioFailed"),
			null);

	public static final IStatus CONFLICT_STATUS =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			TeamException.CONFLICT,
			Policy.bind("teamStatus.conflict"),
			null);

	public static final IStatus REQUIRED_CONFIGURATION_MISSING =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			-100,
			Policy.bind("provider.configuration.missing"),
			null);
			
	public static final IStatus INVALID_CONFIGURATION =
		new Status(
			IStatus.ERROR,
			TeamPlugin.ID,
			-101,
			Policy.bind("provider.configuration.invalid"),
			null);
}