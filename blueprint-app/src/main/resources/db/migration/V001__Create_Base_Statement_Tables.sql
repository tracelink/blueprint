CREATE TABLE base_statement_functions (
  function_id BIGSERIAL NOT NULL,
  name varchar(255) NOT NULL,
  author varchar(255) NOT NULL,
  version INTEGER NOT NULL,
  state varchar(63) NOT NULL,
  description TEXT NOT NULL,
  parameters TEXT NOT NULL,
  expression TEXT NOT NULL,
  PRIMARY KEY (function_id)
);

CREATE TABLE function_dependency (
  function_id BIGSERIAL NOT NULL,
  dependency_id BIGSERIAL NOT NULL,
  PRIMARY KEY (function_id,dependency_id),
  CONSTRAINT fk_function_dependency_function_id FOREIGN KEY (function_id) REFERENCES base_statement_functions (function_id),
  CONSTRAINT fk_function_dependency_dependency_id FOREIGN KEY (dependency_id) REFERENCES base_statement_functions (function_id)
);

CREATE TABLE base_statements (
  base_statement_id BIGSERIAL NOT NULL,
  name varchar(255) NOT NULL,
  author varchar(255) NOT NULL,
  version INTEGER NOT NULL,
  state varchar(63) NOT NULL,
  description TEXT NOT NULL,
  negation_allowed boolean NOT NULL,
  function_id BIGSERIAL NOT NULL,
  PRIMARY KEY (base_statement_id),
  CONSTRAINT fk_function_id FOREIGN KEY (function_id) REFERENCES base_statement_functions (function_id)
);

CREATE TABLE policy_types (
	policy_type_id BIGSERIAL NOT NULL,
	name varchar(63) NOT NULL,
	PRIMARY KEY (policy_type_id)
);

CREATE TABLE policy_type_statement (
	base_statement_id BIGSERIAL NOT NULL,
	policy_type_id BIGSERIAL NOT NULL,
	PRIMARY KEY (base_statement_id,policy_type_id),
	CONSTRAINT fk_pol_type_stmt_statement_id FOREIGN KEY (base_statement_id) REFERENCES base_statements (base_statement_id),
	CONSTRAINT fk_pol_type_stmt_policy_type_id FOREIGN KEY (policy_type_id) REFERENCES policy_types (policy_type_id)
);

CREATE TABLE policy_type_function (
	function_id BIGSERIAL NOT NULL,
	policy_type_id BIGSERIAL NOT NULL,
	PRIMARY KEY (function_id,policy_type_id),
	CONSTRAINT fk_pol_type_fun_function_id FOREIGN KEY (function_id) REFERENCES base_statement_functions (function_id),
	CONSTRAINT fk_pol_type_fun_policy_type_id FOREIGN KEY (policy_type_id) REFERENCES policy_types (policy_type_id)
);

CREATE TABLE base_statement_arguments (
  argument_id BIGSERIAL NOT NULL,
  parameter varchar(255) NOT NULL,
  description TEXT DEFAULT NULL,
  arg_type varchar(255) DEFAULT NULL,
  enum_values TEXT DEFAULT NULL,
  array_unordered boolean DEFAULT NULL,
  array_unique boolean DEFAULT NULL,
  base_statement_id BIGSERIAL NOT NULL,
  PRIMARY KEY (argument_id),
  CONSTRAINT fk_base_statement_id FOREIGN KEY (base_statement_id) REFERENCES base_statements (base_statement_id)
);
