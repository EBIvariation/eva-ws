package uk.ac.ebi.eva.server.controllers;

import com.google.common.collect.Lists;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.server.models.ProgressReport;
import uk.ac.ebi.eva.server.repositories.ProgressReportRepository;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/import-status", produces = "application/json")
public class DbsnpRestController {

    private ProgressReportRepository repository;

    public DbsnpRestController(ProgressReportRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ProgressReport> getReport() {
        List<ProgressReport> progressReports = Lists.newArrayList(repository.findAll());
        return progressReports;
    }
}
