package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.VersionedIdentifier;

/**
 * Defaurl implementation of IImport
 */
public class DefaultImport implements IImport {

	/**
	 * The identifer
	 */
	private VersionedIdentifier id;
	
	/**
	 * the matching rule
	 */
	private int rule;

	/**
	 * Constructor for DefaultImport.
	 */
	public DefaultImport(VersionedIdentifier identifier, int rule) {
		super();
		this.id = identifier;
		this.rule = rule;
	}

	/**
	 * Constructor for DefaultImport.
	 */
	public DefaultImport(String id, String ver, int rule) {
		this(new VersionedIdentifier(id,ver),rule);
	}
	/**
	 * @see IImport#getIdentifier()
	 */
	public VersionedIdentifier getIdentifier() {
		return id;
	}

	/**
	 * @see IImport#getRule()
	 */
	public int getRule() {
		return rule;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(VersionedIdentifier id) {
		this.id = id;
	}

	/**
	 * Sets the rule.
	 * @param rule The rule to set
	 */
	public void setRule(int rule) {
		this.rule = rule;
	}

}

