DROP TABLE IF EXISTS browsable_file;
DROP TABLE IF EXISTS assembly;
DROP TABLE IF EXISTS dbsnp_assemblies;

CREATE TABLE browsable_file (
	file_id int4,
	ena_submission_file_id varchar(45),
	filename varchar(250),
	loaded bool,
	eva_release varchar(50),
	deleted bool,
	eva_release_deleted varchar(50),
	project_accession varchar(25),
	loaded_assembly varchar(500),
	assembly_set_id int4
);

CREATE TABLE assembly (
  assembly_accession varchar(25),
  assembly_chain varchar(25),
  assembly_version int4,
  assembly_set_id int4,
  assembly_name varchar(250),
  assembly_code varchar(25),
  taxonomy_id int4,
  assembly_location varchar(250),
  assembly_filename varchar(250)
);

CREATE TABLE dbsnp_assemblies
(
 database_name varchar(50),
 assembly_set_id int4,
 loaded boolean,
 unique (database_name, assembly_set_id)
)