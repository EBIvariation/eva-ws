INSERT INTO browsable_file
(file_id, ena_submission_file_id, filename, loaded, eva_release, deleted, eva_release_deleted, project_accession, loaded_assembly, assembly_set_id)
VALUES(1, 'ERF1', 'file1.vcf.gz', true, 'Unreleased', false, 'None', 'PRJEB11', NULL, 11);
INSERT INTO browsable_file
(file_id, ena_submission_file_id, filename, loaded, eva_release, deleted, eva_release_deleted, project_accession, loaded_assembly, assembly_set_id)
VALUES(2, 'ERF2', 'file2.vcf.gz', true, 'Unreleased', false, 'None', 'PRJEB22', NULL, 22);
INSERT INTO browsable_file
(file_id, ena_submission_file_id, filename, loaded, eva_release, deleted, eva_release_deleted, project_accession, loaded_assembly, assembly_set_id)
VALUES(3, 'ERF3', 'file.noFTP.vcf.gz', true, 'Unreleased', false, 'None', 'PRJEB33', NULL, 33);
INSERT INTO browsable_file
(file_id, ena_submission_file_id, filename, loaded, eva_release, deleted, eva_release_deleted, project_accession, loaded_assembly, assembly_set_id)
VALUES(4, 'H1', 'HumanBrowsableFile.vcf.gz', true, 'Unreleased', false, 'None', 'PRJEBH1', NULL, 75);
INSERT INTO browsable_file
(file_id, ena_submission_file_id, filename, loaded, eva_release, deleted, eva_release_deleted, project_accession, loaded_assembly, assembly_set_id)
VALUES(5, 'C1', 'CowNoBrowsableFile.vcf.gz', false, 'Unreleased', false, 'None', 'PRJEBC1', NULL, 5);
INSERT INTO browsable_file
(file_id, ena_submission_file_id, filename, loaded, eva_release, deleted, eva_release_deleted, project_accession, loaded_assembly, assembly_set_id)
VALUES(6, 'C2', 'CowDeletedFile.vcf.gz', true, 'Unreleased', true, 'None', 'PRJEBC2', NULL, 5);

INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000002315.3', 'GCA_000002315', 3, 96, 'Gallus_gallus-5.0', 'galgal5', 9031, NULL, NULL, true);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000002315.2', 'GCA_000002315', 2, 19, 'Gallus_gallus-4.0', 'galgal4', 9031, NULL, NULL, false);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000298735.1', 'GCA_000298735', 1, 11, 'Oar_v3.1', 'oarv31', 9940, NULL, NULL, false);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000298735.2', 'GCA_000298735', 2, 95, 'Oar_v4.0', 'oarv40', 9940, NULL, NULL, true);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000002035.3', 'GCA_000002035', 3, 97, 'GRCz10', 'grcz10', 7955, NULL, NULL, true);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000001405.18', 'GCA_000001405', 18, 75, 'GRCh38.p3', 'grch38', 9606, NULL, NULL, true);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000003055.3', 'GCA_000003055', 3, 5, 'Bos_taurus_UMD_3.1', 'umd31', 9913, NULL, NULL, true);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000003025.6', 'GCA_000003025', 6, 89, 'Sscrofa11.1', NULL, 9823, NULL, NULL, true);
INSERT INTO assembly
(assembly_accession, assembly_chain, assembly_version, assembly_set_id, assembly_name, assembly_code, taxonomy_id, assembly_location, assembly_filename, assembly_in_accessioning_store)
VALUES('GCA_000002035.2', 'GCA_000002035', 2, 97, 'Zv9', 'zv9', 7955, NULL, NULL, true);


INSERT INTO dbsnp_assemblies
(database_name, assembly_set_id, assembly_accession, loaded)
VALUES('dbsnp_chicken_9031', 96, 'GCA_000002315.3', true);
INSERT INTO dbsnp_assemblies
(database_name, assembly_set_id, assembly_accession, loaded)
VALUES('dbsnp_sheep_9940', 11, 'GCA_000298735.1', true);
INSERT INTO dbsnp_assemblies
(database_name, assembly_set_id, assembly_accession, loaded)
VALUES('dbsnp_sheep_9940', 95, 'GCA_000298735.2', false);
INSERT INTO dbsnp_assemblies
(database_name, assembly_set_id, assembly_accession, loaded)
VALUES('dbsnp_pig_9823', 89, 'GCA_000003025.6', true);
INSERT INTO dbsnp_assemblies
(database_name, assembly_set_id, assembly_accession, loaded)
VALUES('dbsnp_zebrafish_7955', 97, 'GCA_000002035.3', true);
