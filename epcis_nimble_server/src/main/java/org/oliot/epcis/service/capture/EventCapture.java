package org.oliot.epcis.service.capture;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.json.JSONObject;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.model.epcis.EPCISDocumentType;
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
 * Copyright (C) 2014-2017 Jaewook Byun
 *
 * This project is part of Oliot open source (http://oliot.org). Oliot EPCIS
 * v1.2.x is Java Web Service complying with Electronic Product Code Information
 * Service (EPCIS) v1.2.
 *
 * @author Jaewook Byun, Ph.D student
 * 
 *         Korea Advanced Institute of Science and Technology (KAIST)
 * 
 *         Real-time Embedded System Laboratory(RESL)
 * 
 *         bjw0829@kaist.ac.kr, bjw0829@gmail.com
 */

/**
* Modifications copyright (C) 2019 Quan Deng
*/

@CrossOrigin()
@RestController
@RequestMapping("/EventCapture")
public class EventCapture {
    private static Logger log = LoggerFactory.getLogger(EventCapture.class);

	@Autowired
	AuthorizationSrv authorizationSrv;
	
	@Autowired
	CaptureService 	captureService;

	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	/**
	 * 
	 * @param inputString
	 * @param accessToken 
	 * @param gcpLength Global Company Prefix(GCP) length. 
	 * @return
	 */
	public ResponseEntity<?> post(@RequestBody String inputString, 
			@RequestHeader(value="Authorization", required=true) String bearerToken, 
			@RequestParam(required = false) Integer gcpLength) {
		JSONObject retMsg = new JSONObject();

		// Check NIMBLE authorization
		String userPartyID = authorizationSrv.checkToken(bearerToken);
		if (userPartyID == null) {
			return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
		}

		log.info(" EPCIS Document Capture Started.... ");

		// XSD based Validation
		if (Configuration.isCaptureVerfificationOn == true) {
			InputStream validateStream = CaptureUtil.getXMLDocumentInputStream(inputString);
			boolean isValidated = CaptureUtil.validate(validateStream,
					 "/EPCglobal-epcis-1_2.xsd");
			if (isValidated == false) {
				return new ResponseEntity<>(
						new String("[Error] Input EPCIS Document does not comply the standard schema"),
						HttpStatus.BAD_REQUEST);
			}
			log.info(" EPCIS Document : Validated ");

		}

		InputStream epcisStream = CaptureUtil.getXMLDocumentInputStream(inputString);
		EPCISDocumentType epcisDocument = JAXB.unmarshal(epcisStream, EPCISDocumentType.class);

		if (Configuration.isCaptureVerfificationOn == true) {
			ResponseEntity<?> error = CaptureUtil.checkDocumentHeader(epcisDocument);
			if (error != null)
				return error;
		}

		retMsg = captureService.capture(epcisDocument, userPartyID, gcpLength);
		

		if (retMsg.isNull("error") == true)
		{
			log.info(" EPCIS Document : Captured successfully!");
			return new ResponseEntity<>(retMsg.toString(), HttpStatus.OK);
		}
		else
		{
			log.info(" EPCIS Document : Capture Failed!");
			return new ResponseEntity<>(retMsg.toString(), HttpStatus.BAD_REQUEST);
		}
	}

}
