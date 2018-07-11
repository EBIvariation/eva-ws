drop table if exists dgva_study_browser;

create table dgva_study_browser (
  STUDY_ACCESSION varchar(4000),
  TAXONOMY_IDS varchar(4000),
  COMMON_NAMES varchar(4000),
  SCIENTIFIC_NAMES varchar(4000),
  PUBMED_IDS varchar(4000),
  DISPLAY_NAME varchar(4000),
  STUDY_TYPE varchar(4000),
  STUDY_URL varchar(4000),
  STUDY_DESCRIPTION varchar(4000),
  ANALYSIS_TYPES varchar(4000),
  DETECTION_METHODS varchar(4000),
  METHOD_TYPES varchar(4000),
  PLATFORM_NAMES varchar(4000),
  ASSEMBLY_NAMES varchar(4000),
  ASSEMBLY_ACCESSIONS varchar(4000)
);