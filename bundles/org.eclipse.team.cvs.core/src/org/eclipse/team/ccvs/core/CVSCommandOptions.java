package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Options for CVS commands.
 */
public final class CVSCommandOptions {
	
	public class CommandOption {
		private String option;
		private String argument;

		protected CommandOption(String option) {
			this.option = option;
			this.argument = null;
		}

		protected CommandOption(String option, String argument) {
			this.option = option;
			this.argument = argument;
		}

		public String getOption() {
			return option;
		}
		
		public String getArgument() {
			return argument;
		}
	}
	
	public static final class DiffOption extends CommandOption {
		private DiffOption(String option) {
			defaultOptions.super(option);
		}
		static public final DiffOption UNIFIED_FORMAT = new DiffOption("-u");
		static public final DiffOption CONTEXT_FORMAT = new DiffOption("-c");
		static public final DiffOption INCLUDE_NEWFILES = new DiffOption("-N");
		static public final DiffOption DONT_RECURSE = new DiffOption("-l");
	}
	
	public static final class QuietOption extends CommandOption {
		private QuietOption(String option) {
			defaultOptions.super(option);
		}
		static public final QuietOption PARTLY_QUIET = new QuietOption("-q");
		static public final QuietOption SILENT = new QuietOption("-Q");
	}
	
	public static final class UpdateOption extends CommandOption {
		private UpdateOption(String option) {
			defaultOptions.super(option);
		}
		private UpdateOption(String option, String argument) {
			defaultOptions.super(option, argument);
		}
		static public final UpdateOption CLEAR_STICKY = new UpdateOption("-A");
		static public final UpdateOption IGNORE_LOCAL_CHANGES = new UpdateOption("-C");
		
		public UpdateOption updateOptionForTag(CVSTag tag) {
			int type = tag.getType();
			if (type == CVSTag.BRANCH || type == CVSTag.VERSION || type == CVSTag.HEAD) {
				return new UpdateOption("-r", tag.getName());
			} else if (type == CVSTag.DATE) {
				return new UpdateOption("-D", tag.getName());
			} else {
				return null;
			}
		}
		public UpdateOption updateOptionForMerge(CVSTag tag1, CVSTag tag2) {
			// XXX Fill in once we add support for merging
			return null;
		}
	}

	static private final CVSCommandOptions defaultOptions = new CVSCommandOptions();
}