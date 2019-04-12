package org.oliot.epcis.service.capture;

import eu.nimble.service.epcis.services.AuthorizationSrv;
import io.swagger.annotations.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.epcis.service.capture.mongodb.MongoCaptureUtil;
import org.oliot.model.jsonschema.JsonSchemaLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
* Created by hos, BIBA, 2019
*/

@Api(tags = {"EPCIS JSON Master Data Capture"})
@CrossOrigin()
@RestController
@RequestMapping("/JSONMasterCapture")
public class JSONMasterCapture {
    private static Logger log = LoggerFactory.getLogger(JSONMasterCapture.class);

    @Autowired
    AuthorizationSrv authorizationSrv;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> post(@RequestBody String inputString,
                                  @RequestHeader(value="Authorization", required= true) String accessToken,
                                  @RequestParam(required = false) Integer gcpLength) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(accessToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        // JSONObject retMsg = new JSONObject();
        log.info("EPCIS Master Json Document Capture Started.... ");
        if (Configuration.isCaptureVerfificationOn == true) {

            // JSONParser parser = new JSONParser();
            JsonSchemaLoader schemaLoader = new JsonSchemaLoader();

            try {

                JSONObject jsonMaster = new JSONObject(inputString);
                JSONObject jsonMasterSchema = schemaLoader.getMasterDataSchema();

                if (!CaptureUtil.validate(jsonMaster, jsonMasterSchema)) {
                    log.info("Json Document is invalid" + " about general_validcheck");

                    return new ResponseEntity<>("Error: Json Document is not valid" + "general_validcheck",
                            HttpStatus.BAD_REQUEST);

                }

                /* Schema check for Capture */

                JSONArray jsonEventList = jsonMaster.getJSONObject("epcismd").getJSONObject("EPCISBody")
                        .getJSONArray("VocabularyList");

                for (int i = 0; i < jsonEventList.length(); i++) {
                    JSONObject jsonEventElement = jsonEventList.getJSONObject(i);

                    if (jsonEventElement.has("Vocabulary") == true) {

                        /* startpoint of validation logic for ObjectMaster */
                        JSONObject objectMasterSchema = schemaLoader.getObjectMasterSchema();
                        JSONObject jsonObjectMaster = jsonEventElement.getJSONObject("Vocabulary");

                        if (!CaptureUtil.validate(jsonObjectMaster, objectMasterSchema)) {
                            log.info("Json Master Document is not valid" + " detail validation check for object Master");
                            return new ResponseEntity<>("Error: Json Master Document is not valid"
                                    + " for detail validation check for object Master", HttpStatus.BAD_REQUEST);
                        }

                        MongoCaptureUtil m = new MongoCaptureUtil();
                        m.captureJSONMaster(jsonObjectMaster);

                    }   else {
                        log.info("Json Master Document is not valid. " + " It doesn't have standard event_type");
                        return new ResponseEntity<>(
                                "Error: Json Master Document is not valid" + " It doesn't have standard event_type",
                                HttpStatus.BAD_REQUEST);
                    }

                }
                if (jsonEventList.length() != 0)
                    log.info(" EPCIS Master Document : Captured ");

            } catch (JSONException e) {
                log.info(" Json Document is not valid " + "second_validcheck");
            } catch (Exception e) {
                log.error(e.toString());
            }

            return new ResponseEntity<>("EPCIS Master Document : Captured ", HttpStatus.OK);

        } else {
            JSONObject jsonEvent = new JSONObject(inputString);
            JSONArray jsonEventList = jsonEvent.getJSONObject("epcismd").getJSONObject("EPCISBody")
                    .getJSONArray("VocabularyList");

            for (int i = 0; i < jsonEventList.length(); i++) {

                JSONObject jsonEventElement = jsonEventList.getJSONObject(i);

                if (jsonEventElement.has("Vocabulary") == true) {
                    MongoCaptureUtil m = new MongoCaptureUtil();
                    m.captureJSONMaster(jsonEventElement.getJSONObject("Vocabulary"));
                }
            }
        }
        return new ResponseEntity<>("EPCIS Master Document : Captured ", HttpStatus.OK);

    }

}
