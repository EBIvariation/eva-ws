DROP TABLE IF EXISTS browsable_file;
DROP TABLE IF EXISTS assembly;
DROP TABLE IF EXISTS dbsnp_assemblies;

CREATE TABLE browsable_file (
	file_id integer,
	ena_submission_file_id varchar(45),
	filename varchar(250),
	loaded boolean,
	eva_release varchar(50),
	deleted boolean,
	eva_release_deleted varchar(50),
	project_accession varchar(25),
	loaded_assembly varchar(500),
	assembly_set_id integer
);

CREATE TABLE assembly (
  assembly_accession varchar(25),
  assembly_chain varchar(25),
  assembly_version integer,
  assembly_set_id integer,
  assembly_name varchar(250),
  assembly_code varchar(25),
  taxonomy_id integer,
  assembly_location varchar(250),
  assembly_filename varchar(250),
  assembly_in_accessioning_store boolean
);

CREATE TABLE dbsnp_assemblies
(
    database_name varchar(50) NOT NULL,
    assembly_set_id integer NULL,
    assembly_accession varchar(25) NULL,
    loaded boolean DEFAULT false,
    UNIQUE (database_name, assembly_set_id, assembly_accession)
);