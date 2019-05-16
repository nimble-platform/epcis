package eu.nimble.service.epcis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String main() {
        return "index"; //view
    }

    @GetMapping("/json-event-capture")
    public String jsonEventCapture() {
        return "json-event-capture";
    }

    @GetMapping("/json-event-query")
    public String jsonEventQuery() {
        return "json-event-query";
    }

    @GetMapping("/json-master-single-capture")
    public String jsonMasterSingleCapture() {
        return "json-master-single-capture";
    }

    @GetMapping("/json-master-multiple-capture")
    public String jsonMasterMultipleCapture() {
        return "json-master-multiple-capture";
    }

    @GetMapping("json-master-query")
    public String jsonMasterQuery() {
        return "json-master-query";
    }

    @GetMapping("/xml-event-capture")
    public String xmlEventCapture() {
        return "xml-event-capture";
    }

    @GetMapping("/xml-event-query")
    public String xmlEventQuery() {
        return "xml-event-query";
    }

    @GetMapping("/xml-master-capture")
    public String xmlMasterCapture() {
        return "xml-master-capture";
    }

    @GetMapping("xml-master-query")
    public String xmlMasterQuery() {
        return "xml-master-query";
    }

    @GetMapping("/json-production-capture")
    public String jsonProductionCapture() {
        return "json-production-capture";
    }

    @GetMapping("/json-production-query")
    public String jsonProductionQuery() {
        return "json-production-query";
    }
}
