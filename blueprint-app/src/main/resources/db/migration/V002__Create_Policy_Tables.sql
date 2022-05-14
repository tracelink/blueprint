CREATE TABLE policy (
  policy_id BIGSERIAL NOT NULL,
  name varchar(255) NOT NULL,
  policy_type_id BIGSERIAL NOT NULL,
  author varchar(255) NOT NULL,
  PRIMARY KEY (policy_id),
  CONSTRAINT fk_pol_type_policy FOREIGN KEY (policy_type_id) REFERENCES policy_types (policy_type_id)
);

CREATE TABLE policy_clause (
  clause_id BIGSERIAL NOT NULL,
  policy_id BIGSERIAL NOT NULL,
  PRIMARY KEY (clause_id),
  CONSTRAINT fk_policy_rel FOREIGN KEY (policy_id) REFERENCES policy (policy_id)
);

CREATE TABLE configured_statement (
  statement_id BIGSERIAL NOT NULL,
  clause_id BIGSERIAL NOT NULL,
  base_statement_id BIGSERIAL NOT NULL,
  negated boolean NOT NULL,
  PRIMARY KEY (statement_id),
  CONSTRAINT fk_clause_rel FOREIGN KEY (clause_id) REFERENCES policy_clause (clause_id),
  CONSTRAINT fk_base_rel FOREIGN KEY (base_statement_id) REFERENCES base_statements (base_statement_id)
);

CREATE TABLE arguments (
	argument_id BIGSERIAL NOT NULL,
	statement_id BIGSERIAL NOT NULL,
	/*arg_key varchar(255) NOT NULL,*/
	arg_value varchar(255) NOT NULL,
	PRIMARY KEY (argument_id),
	CONSTRAINT fk_statement_rel FOREIGN KEY (statement_id) REFERENCES configured_statement (statement_id)
);
