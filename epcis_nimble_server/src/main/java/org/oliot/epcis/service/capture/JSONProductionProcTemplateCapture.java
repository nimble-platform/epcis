package org.oliot.epcis.service.capture;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.nimble.service.epcis.services.AuthorizationSrv;

/**
* Created by Quan Deng, 2019
*/

@CrossOrigin()
@RestController
@RequestMapping("/JSONProductionProcTemplateCapture")
public class JSONProductionProcTemplateCapture {
    private static Logger log = LoggerFactory.getLogger(JSONProductionProcTemplateCapture.class);


	@Autowired
	AuthorizationSrv authorizationSrv;


	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> post(@RequestBody String inputString, 
			@RequestHeader(value="Authorization", required=true) String bearerToken, 
			@RequestParam(required = false) Integer gcpLength) {
		
		// Check NIMBLE authorization
		String userPartyID = authorizationSrv.checkToken(bearerToken);
		if (userPartyID == null) {
			return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
		}
		
		//TODO: Permission check for each event on the list. Return error, in case no permission on some events.
		//TODO: Save userPartyID into Event i.e. MongoDB. So that it is possible to know from whom the event is added, for the purpose of audit etc. 

		log.info(" Production Process Template Json Document Capture Started.... ");

		JSONObject jsonProductionProc = new JSONObject(inputString);
		
		if (Configuration.isCaptureVerfificationOn == true) {

			// JSONParser parser = new JSONParser();
			JsonSchemaLoader schemaLoader = new JsonSchemaLoader();

			try {
				JSONObject jsonProductionProcSchema = schemaLoader.getProductionProcTemplateSchema();

				if (!CaptureUtil.validate(jsonProductionProc, jsonProductionProcSchema)) {
					log.info("Json Document is invalid" + " about general_validcheck");

					return new ResponseEntity<>("Error: Json Document is not valid" + "general_validcheck",
							HttpStatus.BAD_REQUEST);

				}

			} catch (JSONException e) {
				log.info(" Json Document is not valid " + "second_validcheck");
			} catch (Exception e) {
				log.error(e.toString());
			}
		} 
		
		MongoCaptureUtil m = new MongoCaptureUtil();
		m.captureJSONProductionProcTemplate(jsonProductionProc, userPartyID);
		
		return new ResponseEntity<>("Production Process Template Document : Captured ", HttpStatus.OK);

	}

}
