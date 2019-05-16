package eu.nimble.service.epcis.controller.wrapper;

import eu.nimble.service.epcis.controller.BaseRestController;
import eu.nimble.service.epcis.services.NIMBLETokenService;
import org.json.JSONObject;
import org.oliot.epcis.service.capture.JSONMasterCaptureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class JSONMasterCaptureWrapper extends BaseRestController {

    private static Logger log = LoggerFactory.getLogger(JSONMasterCaptureWrapper.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    NIMBLETokenService nimbleTokenService;

    @Autowired
    JSONMasterCaptureService jsonMasterCaptureService;

    @Value("${data-replication.remote_nimble_epcis_server.url}")
    public String remoteNIMBLEEPCISURL;

    @Value("${data-replication.remote_nimble_epcis_server.enabled}")
    public boolean remoteNIMBLEEPCISEnabled;

    @PostMapping("/IntelligentJSONMasterCapture")
    public ResponseEntity<?> post(@RequestBody String inputString) {
        log.info(" EPCIS Json Document Capture Started.... ");

        List<JSONObject> validJsonMasterList = jsonMasterCaptureService.prepareJSONMasters(inputString);
        if (null == validJsonMasterList) {
            log.info("No Masters Captured!");
            return new ResponseEntity<>("Error: Json Document is not valid. Details can be found in the log files.",
                    HttpStatus.BAD_REQUEST);
        }

        // Local Capture
        log.info("Capture Masters into company local storage.... ");
        jsonMasterCaptureService.capturePreparedJSONMasters(validJsonMasterList);

        // Remote NIMBLE Server Capture
        boolean remoteCaptureSuccess = true;
        if (remoteNIMBLEEPCISEnabled) {
            remoteCaptureSuccess = this.replicateRemoteEPCIS(inputString);
        }

        String responseMsg = "EPCIS Json Master captured: " + validJsonMasterList.size();
        HttpStatus status = HttpStatus.OK;
        if (!remoteCaptureSuccess) {
            responseMsg = responseMsg + "; failed to replicate json masters to remote NIMBLE EPCIS server!";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(responseMsg, status);
    }

    /**
     * Replicate the json masters string into remote NIMBLE EPCIS server
     *
     * @param inputString
     * @return true, on success; false, otherwise
     */
    public boolean replicateRemoteEPCIS(String inputString) {
        boolean success = true;

        String jsonCaptureURL = remoteNIMBLEEPCISURL + "JSONMasterCapture";

        String bearerToken = nimbleTokenService.getBearerToken();
        if (null == bearerToken) {
            log.error(
                    "Fail to send EPCIS master to NIMBLE platform because failed to authorize the user with given name and password!");
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);
        HttpEntity<String> entity = new HttpEntity<String>(inputString, headers);

        try {
            restTemplate.exchange(jsonCaptureURL, HttpMethod.POST, entity, String.class);
            log.info("Captured Masters into NIMBLE Server. ");
        } catch (HttpStatusCodeException e) {
            log.error(
                    "Received error during replicate json masters into NIMBLE platform: " + e.getResponseBodyAsString());
            success = false;
        }

        return success;
    }
}
