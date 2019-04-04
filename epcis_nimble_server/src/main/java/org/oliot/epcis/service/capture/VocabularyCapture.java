package org.oliot.epcis.service.capture;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.json.JSONObject;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.model.epcis.EPCISMasterDataDocumentType;
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
@RequestMapping("/VocabularyCapture")
public class VocabularyCapture {
    private static Logger log = LoggerFactory.getLogger(VocabularyCapture.class);


	@Autowired
	AuthorizationSrv authorizationSrv;
	@Autowired
	CaptureService captureService;


	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> post(@RequestBody String inputString, @RequestHeader(value="Authorization", required=true) String bearerToken, 
			@RequestParam(required = false) Integer gcpLength) {

		// Check NIMBLE authorization
		String userPartyID = authorizationSrv.checkToken(bearerToken);
		if (userPartyID == null) {
			return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
		}
		
		//TODO: Permission check for each event on the list. Return error, in case no permission on some events.
		//TODO: Save userPartyID into Event i.e. MongoDB. So that it is possible to know from whom the Vocabulary is added, for the purpose of audit etc. 
		
		log.info(" EPCIS Masterdata Document Capture Started.... ");

		JSONObject retMsg = new JSONObject();
		if (Configuration.isCaptureVerfificationOn == true) {
			InputStream validateStream = CaptureUtil.getXMLDocumentInputStream(inputString);
			// Parsing and Validating data
			boolean isValidated = CaptureUtil.validate(validateStream,
					 "/EPCglobal-epcis-masterdata-1_2.xsd");
			if (isValidated == false) {
				return new ResponseEntity<>(new String("Error: EPCIS Masterdata Document is not validated"),
						HttpStatus.BAD_REQUEST);
			}
		}

		InputStream epcisStream = CaptureUtil.getXMLDocumentInputStream(inputString);
		EPCISMasterDataDocumentType epcisMasterDataDocument = JAXB.unmarshal(epcisStream,
				EPCISMasterDataDocumentType.class);
		retMsg = captureService.capture(epcisMasterDataDocument, userPartyID,  gcpLength);
		log.info(" EPCIS Masterdata Document : Captured ");

		if (retMsg.isNull("error") == true)
			return new ResponseEntity<>(retMsg.toString(), HttpStatus.OK);
		else
			return new ResponseEntity<>(retMsg.toString(), HttpStatus.BAD_REQUEST);
	}
}
