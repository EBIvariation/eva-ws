package uk.ac.ebi.eva.server.ws;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.lib.metadata.eva.SubmissionStatsEvaproDBAdaptor;

@RestController
@RequestMapping(value = "/v1/stats", produces = "text/plain")
@Api(tags = {"submission-stats"})
public class SubmissionStatsWSServer {

    @Autowired
    private SubmissionStatsEvaproDBAdaptor submissionStatsAdaptor;

    @GetMapping(value = "/submissions/count")
    public ResponseEntity<String> getSubmissionsCountPerMonth() {
        StringBuilder tsv = new StringBuilder("Month\tEntries\n");
        submissionStatsAdaptor.getCountByMonth()
                .forEach((month, count) -> tsv.append(month).append("\t").append(count).append("\n"));
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(tsv.toString());
    }

    @GetMapping(value = "/submissions/bytes")
    public ResponseEntity<String> getSubmissionsBytesPerMonth() {
        StringBuilder tsv = new StringBuilder("Month\tBytes\n");
        submissionStatsAdaptor.getBytesByMonth()
                .forEach((month, bytes) -> tsv.append(month).append("\t").append(bytes).append("\n"));
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(tsv.toString());
    }
}
