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
