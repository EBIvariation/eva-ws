CREATE MATERIALIZED VIEW evapro.study_browser AS
 SELECT project.project_accession,
    project.eva_study_accession AS study_id,
    project.title AS project_title,
    COALESCE(project.eva_description, project.description) AS description,
    COALESCE(project_children_taxonomy.taxonomy_ids, '-'::text) AS tax_id,
    COALESCE(project_children_taxonomy.taxonomy_common_names, '-'::text) AS common_name,
    COALESCE(project_children_taxonomy.taxonomy_scientific_names, '-'::text) AS scientific_name,
    COALESCE(project.source_type, '-'::character varying) AS source_type,
    COALESCE(project.study_type, '-'::character varying) AS study_type,
    COALESCE(project_counts.etl_count, project_counts.estimate_count) AS variant_count,
    project_samples_temp1.sample_count AS samples,
    COALESCE(project.eva_center_name, project.center_name) AS center,
    COALESCE(project.scope, '-'::character varying) AS scope,
    COALESCE(project.material, '-'::character varying) AS material,
    COALESCE(c.ids, '-'::text) AS publications,
    COALESCE(project_children_taxonomy.child_projects, '-'::text) AS associated_projects,
    COALESCE(initcap(project_experiment.experiment_type), '-'::text) AS experiment_type,
    COALESCE(project_experiment.experiment_type_abbreviation, '-'::text) AS experiment_type_abbreviation,
    COALESCE(a.v_ref, '-'::text) AS assembly_accession,
    COALESCE(b.v_ref, '-'::text) AS assembly_name,
    COALESCE(d.platform, '-'::text) AS platform,
    COALESCE(r.resource, '-'::text::character varying) AS resource,
    COALESCE(browsable_table.browsable, false) AS browsable
   FROM project
     LEFT JOIN project_counts USING (project_accession)
     LEFT JOIN project_children_taxonomy USING (project_accession)
     LEFT JOIN project_samples_temp1 USING (project_accession)
     LEFT JOIN project_eva_submission project_eva_submission(project_accession, eva_submission_id, eload_id, old_eva_submission_id) USING (project_accession)
     LEFT JOIN project_experiment USING (project_accession)
     LEFT JOIN ( SELECT project_publication.project_accession,
            string_agg(project_publication.id::text, ', '::text) AS ids
           FROM project_publication
          GROUP BY project_publication.project_accession) c(project_accession_1, ids) ON c.project_accession_1::text = project.project_accession::text
     LEFT JOIN ( SELECT project_reference.project_accession,
            string_agg(project_reference.reference_accession::text, ', '::text) AS v_ref
           FROM project_reference
          GROUP BY project_reference.project_accession) a(project_accession_1, v_ref) ON a.project_accession_1::text = project.project_accession::text
     LEFT JOIN ( SELECT project_reference.project_accession,
            string_agg(project_reference.reference_name::text, ', '::text) AS v_ref
           FROM project_reference
          GROUP BY project_reference.project_accession) b(project_accession_1, v_ref) ON b.project_accession_1::text = project.project_accession::text
     LEFT JOIN ( SELECT project_platform.project_accession,
            string_agg(project_platform.platform::text, ', '::text) AS platform
           FROM project_platform
          GROUP BY project_platform.project_accession) d(project_accession_1, platform) ON d.project_accession_1::text = project.project_accession::text
     JOIN eva_submission USING (eva_submission_id)
     LEFT JOIN project_resource r USING (project_accession)
     LEFT JOIN ( SELECT browsable_file.project_accession,
            true AS browsable
           FROM browsable_file
          GROUP BY browsable_file.project_accession) browsable_table(project_accession_browsable, browsable) ON browsable_table.project_accession_browsable::text = project.project_accession::text
  WHERE (project.hold_date <= now()::date OR project.hold_date IS NULL) AND eva_submission.eva_submission_status_id >= 6 AND project.ena_status = 4 AND project.eva_status = 1
  ORDER BY project_children_taxonomy.taxonomy_common_names;
