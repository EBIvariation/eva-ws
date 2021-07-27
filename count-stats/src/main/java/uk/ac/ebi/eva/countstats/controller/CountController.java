package uk.ac.ebi.eva.countstats.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.service.CountService;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class CountController {
    private CountService countService;

    public CountController(CountService countService) {
        this.countService = countService;
    }

    @PostMapping("count")
    public Count saveCount(@RequestBody Count count) {
        return countService.saveCount(count);
    }

    @PostMapping("/bulk/count")
    public Iterable<Count> saveAllCount(@RequestBody List<Count> countList) {
        return countService.saveAllCount(countList);
    }

    @GetMapping("count")
    public Iterable<Count> getCount() {
        return countService.getAllCounts();
    }

    @GetMapping("count/{process}")
    public Long getCountForProcess(@PathVariable("process") String process, @RequestParam("study") String study) {
        return countService.getCountForProcess(process, study);
    }
}