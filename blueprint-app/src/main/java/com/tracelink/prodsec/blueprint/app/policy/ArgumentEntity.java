package com.tracelink.prodsec.blueprint.app.policy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Model for a configured argument.
 *
 * @author mcool
 */
@Entity
@Table(name = "arguments")
public class ArgumentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "argument_id")
	private long argumentId;

	// TBD, might use this for templating
//	@Column(name = "key")
//	private String key;

	@Column(name = "arg_value")
	private String value;

	public ArgumentEntity() {

	}

	public ArgumentEntity(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
