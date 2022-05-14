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
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity for a single policy clause in a policy.
 *
 * @author mcool
 */
@Entity
@Table(name = "policy_clause")
public class PolicyClauseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clause_id")
	private long clauseId;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "clause_id", nullable = false)
	private List<ConfiguredStatementEntity> statements = new ArrayList<>();

	public List<ConfiguredStatementEntity> getStatements() {
		return statements;
	}

	public void setStatements(List<ConfiguredStatementEntity> statements) {
		if (statements != null) {
			this.statements.clear();
			this.statements.addAll(statements);
		}
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public PolicyClauseDto toDto() {
		PolicyClauseDto dto = new PolicyClauseDto();
		dto.setStatements(statements.stream().map(ConfiguredStatementEntity::toDto).collect(
				Collectors.toUnmodifiableList()));
		return dto;
	}
}
