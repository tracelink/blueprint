package com.tracelink.prodsec.blueprint.app.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;

/**
 * Entity for a single configured statement in a policy clause.
 *
 * @author mcool
 */
@Entity
@Table(name = "configured_statement")
public class ConfiguredStatementEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "statement_id")
	private long statementId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "base_statement_id")
	private BaseStatementEntity baseStatement;

	@Column(name = "negated")
	private boolean negated;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "statement_id", nullable = false)
	private List<ArgumentEntity> arguments = new ArrayList<>();

	public BaseStatementEntity getBaseStatement() {
		return baseStatement;
	}

	public void setBaseStatement(BaseStatementEntity baseStatement) {
		this.baseStatement = baseStatement;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public List<String> getArgumentValues() {
		return arguments.stream().map(ArgumentEntity::getValue).collect(Collectors.toList());
	}

	public void setArgumentValues(List<String> arguments) {
		if (arguments != null) {
			this.arguments.clear();
			this.arguments = arguments.stream().map(ArgumentEntity::new)
					.collect(Collectors.toList());
		}
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public ConfiguredStatementDto toDto() {
		ConfiguredStatementDto dto = new ConfiguredStatementDto();
		dto.setBaseStatementName(baseStatement.getName());
		dto.setNegated(negated);
		dto.setArgumentValues(getArgumentValues());
		return dto;
	}
}
