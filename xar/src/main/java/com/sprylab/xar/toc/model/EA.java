package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class EA extends Data {

	@Attribute
	private String id;

	@Element
	private String name;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}
}
