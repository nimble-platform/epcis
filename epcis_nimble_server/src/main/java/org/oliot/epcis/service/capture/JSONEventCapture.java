package org.oliot.epcis.service.capture;

import java.util.Iterator;

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
 * Copyright (C) 2017 Jaewook Jack Byun, Sungpil Woo
 *
 * This project is part of Oliot (oliot.org), pursuing the implementation of
 * Electronic Product Code Information Service(EPCIS) v1.2 specification in
 * EPCglobal.
 * 
 *
 * @author Jaewook Jack Byun, Ph.D student
 * 
 *         Korea Advanced Institute of Science and Technology (KAIST)
 * 
 *         Real-time Embedded System Laboratory(RESL)
 * 
 *         bjw0829@kaist.ac.kr, bjw0829@gmail.com
 * 
 * @author Sungpil Woo, Ph.D student
 * 
 *         Korea Advanced Institute of Science and Technology (KAIST)
 * 
 *         Real-time Embedded System Laboratory(RESL)
 * 
 *         woosungpil@kaist.ac.kr, woosungpil7@gmail.com
 */

/**
* Modifications copyright (C) 2019 Quan Deng
*/


@CrossOrigin()
@RestController
@RequestMapping("/JSONEventCapture")
public class JSONEventCapture {
    private static Logger log = LoggerFactory.getLogger(JSONEventCapture.class);

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
		
		log.info(" EPCIS Json Document Capture Started.... ");

		if (Configuration.isCaptureVerfificationOn == true) {

			// JSONParser parser = new JSONParser();
			JsonSchemaLoader schemaLoader = new JsonSchemaLoader();

			try {

				JSONObject jsonEvent = new JSONObject(inputString);
				JSONObject jsonEventSchema = schemaLoader.getEventSchema();

				if (!CaptureUtil.validate(jsonEvent, jsonEventSchema)) {
					log.info("Json Document is invalid" + " about general_validcheck");

					return new ResponseEntity<>("Error: Json Document is not valid" + "general_validcheck",
							HttpStatus.BAD_REQUEST);

				}

				/* Schema check for Capture */

				JSONArray jsonEventList = jsonEvent.getJSONObject("epcis").getJSONObject("EPCISBody")
						.getJSONArray("EventList");

				for (int i = 0; i < jsonEventList.length(); i++) {
					JSONObject jsonEventElement = jsonEventList.getJSONObject(i);

					if (jsonEventElement.has("ObjectEvent") == true) {

						/* startpoint of validation logic for ObjectEvent */
						JSONObject objectEventSchema = schemaLoader.getObjectEventSchema();
						JSONObject jsonObjectEvent = jsonEventElement.getJSONObject("ObjectEvent");

						if (!CaptureUtil.validate(jsonObjectEvent, objectEventSchema)) {
							log
									.info("Json Document is not valid" + " detail validation check for objectevent");
							return new ResponseEntity<>("Error: Json Document is not valid"
									+ " for detail validation check for objectevent", HttpStatus.BAD_REQUEST);

						}

						/* finish validation logic for ObjectEvent */
						if (!jsonObjectEvent.has("recordTime")) {
							jsonObjectEvent.put("recordTime", System.currentTimeMillis());
						}
						
						if (!jsonObjectEvent.has("eventType")) {
							jsonObjectEvent.put("eventType", "ObjectEvent");
						}

						if (jsonObjectEvent.has("any")) {
							/* start finding namespace in the any field. */
							JSONObject anyobject = jsonObjectEvent.getJSONObject("any");
							String namespace = "";
							boolean namespace_flag = false;

							Iterator<String> keyIter_ns = anyobject.keys();
							while (keyIter_ns.hasNext()) {
								String temp = keyIter_ns.next();
								if (temp.substring(0, 1).equals("@")) {
									namespace_flag = true;
									namespace = temp.substring(1, temp.length());
								}
							}

							if (!namespace_flag) {
								log.info("Json Document doesn't have namespace in any field");
								return new ResponseEntity<>(
										"Error: Json Document doesn't have namespace in any field"
												+ " for detail validation check for objectevent",
										HttpStatus.BAD_REQUEST);

							}
							/* finish finding namespace in the any field. */

							/*
							 * Start Validation whether each component use correct name space
							 */;

							Iterator<String> keyIter = anyobject.keys();
							while (keyIter.hasNext()) {
								String temp = keyIter.next();

								if (!temp.contains(namespace)) {
									log.info("Json Document use invalid namespace in anyfield");

									return new ResponseEntity<>(
											"Error: Json Document use invalid namespace in anyfield"
													+ " for detail validation check for objectevent",
											HttpStatus.BAD_REQUEST);

								}
							}
							/*
							 * Finish validation whether each component use correct name space
							 */

						}

						MongoCaptureUtil m = new MongoCaptureUtil();
						m.captureJSONEvent(jsonObjectEvent, userPartyID);

					} else if (jsonEventElement.has("AggregationEvent") == true) {

						/*
						 * startpoint of validation logic for AggregationEvent
						 */
						JSONObject aggregationEventSchema = schemaLoader.getAggregationEventSchema();
						JSONObject jsonAggregationEvent = jsonEventElement.getJSONObject("AggregationEvent");

						if (!CaptureUtil.validate(jsonAggregationEvent, aggregationEventSchema)) {

							log.info(
									"Json Document is not valid" + " detail validation check for aggregationevent");

							return new ResponseEntity<>(
									"Error: Json Document is not valid"
											+ " for detail validation check for aggregationevent",
									HttpStatus.BAD_REQUEST);

						}
						/* finish validation logic for AggregationEvent */

						if (!jsonAggregationEvent.has("recordTime")) {
							jsonAggregationEvent.put("recordTime", System.currentTimeMillis());
						}
						
						if (!jsonAggregationEvent.has("eventType")) {
							jsonAggregationEvent.put("eventType", "AggregationEvent");
						}

						if (jsonAggregationEvent.has("any")) {
							/* start finding namespace in the any field. */
							JSONObject anyobject = jsonAggregationEvent.getJSONObject("any");
							String namespace = "";
							boolean namespace_flag = false;

							Iterator<String> keyIter_ns = anyobject.keys();
							while (keyIter_ns.hasNext()) {
								String temp = keyIter_ns.next();
								if (temp.substring(0, 1).equals("@")) {
									namespace_flag = true;
									namespace = temp.substring(1, temp.length());
								}
							}

							if (!namespace_flag) {
								log.info("Json Document doesn't have namespace in any field");

								return new ResponseEntity<>(
										"Error: Json Document doesn't have namespace in any field"
												+ " for detail validation check for aggregationevent",
										HttpStatus.BAD_REQUEST);

							}
							/* finish finding namespace in the any field. */

							/*
							 * Start Validation whether each component use correct name space
							 */;

							Iterator<String> keyIter = anyobject.keys();
							while (keyIter.hasNext()) {
								String temp = keyIter.next();

								if (!temp.contains(namespace)) {
									log.info("Json Document use invalid namespace in anyfield");

									return new ResponseEntity<>(
											"Error: Json Document use invalid namespace in anyfield"
													+ " for detail validation check for aggregationevent",
											HttpStatus.BAD_REQUEST);

								}
							}

						}
						MongoCaptureUtil m = new MongoCaptureUtil();
						m.captureJSONEvent(jsonEventList.getJSONObject(i).getJSONObject("AggregationEvent"), userPartyID);

					} else if (jsonEventElement.has("TransformationEvent") == true) {

						/*
						 * startpoint of validation logic for TransFormationEvent
						 */
						JSONObject transformationEventSchema = schemaLoader.getTransformationEventSchema();
						JSONObject jsonTransformationEvent = jsonEventElement.getJSONObject("TransformationEvent");

						if (!CaptureUtil.validate(jsonTransformationEvent, transformationEventSchema)) {

							log.info(
									"Json Document is not valid" + " detail validation check for TransFormationEvent");

							return new ResponseEntity<>(
									"Error: Json Document is not valid"
											+ " for detail validation check for TransFormationEvent",
									HttpStatus.BAD_REQUEST);

						}
						/* finish validation logic for TransFormationEvent */

						if (!jsonTransformationEvent.has("recordTime")) {
							jsonTransformationEvent.put("recordTime", System.currentTimeMillis());
						}
						
						if (!jsonTransformationEvent.has("eventType")) {
							jsonTransformationEvent.put("eventType", "TransformationEvent");
						}

						if (jsonTransformationEvent.has("any")) {
							/* start finding namespace in the any field. */
							JSONObject anyobject = jsonTransformationEvent.getJSONObject("any");
							String namespace = "";
							boolean namespace_flag = false;

							Iterator<String> keyIter_ns = anyobject.keys();
							while (keyIter_ns.hasNext()) {
								String temp = keyIter_ns.next();
								if (temp.substring(0, 1).equals("@")) {
									namespace_flag = true;
									namespace = temp.substring(1, temp.length());
								}
							}

							if (!namespace_flag) {
								log.info("Json Document doesn't have namespace in any field");
								return new ResponseEntity<>(
										"Error: Json Document doesn't have namespace in any field"
												+ " for detail validation check for TransformationEvent",
										HttpStatus.BAD_REQUEST);

							}
							/* finish finding namespace in the any field. */

							/*
							 * Start Validation whether each component use correct name space
							 */;

							Iterator<String> keyIter = anyobject.keys();
							while (keyIter.hasNext()) {
								String temp = keyIter.next();

								if (!temp.contains(namespace)) {
									log.info("Json Document use invalid namespace in anyfield");
									return new ResponseEntity<>(
											"Error: Json Document use invalid namespace in anyfield"
													+ " for detail validation check for TransformationEvent",
											HttpStatus.BAD_REQUEST);
								}
							}
						}

						MongoCaptureUtil m = new MongoCaptureUtil();
						m.captureJSONEvent(jsonEventList.getJSONObject(i).getJSONObject("TransformationEvent"), userPartyID);

					} else if (jsonEventElement.has("TransactionEvent") == true) {

						/*
						 * startpoint of validation logic for TransFormationEvent
						 */
						JSONObject transactionEventSchema = schemaLoader.getTransactionEventSchema();
						JSONObject jsonTransactionEvent = jsonEventElement.getJSONObject("TransactionEvent");

						if (!CaptureUtil.validate(jsonTransactionEvent, transactionEventSchema)) {

							log.info(
									"Json Document is not valid." + " detail validation check for TransactionEvent");
							return new ResponseEntity<>(
									"Error: Json Document is not valid"
											+ " for detail validation check for TransactionEvent",
									HttpStatus.BAD_REQUEST);

						}
						/* finish validation logic for TransFormationEvent */

						if (!jsonTransactionEvent.has("recordTime")) {
							jsonTransactionEvent.put("recordTime", System.currentTimeMillis());
						}
						
						if (!jsonTransactionEvent.has("eventType")) {
							jsonTransactionEvent.put("eventType", "TransactionEvent");
						}

						if (jsonTransactionEvent.has("any")) {
							/* start finding namespace in the any field. */
							JSONObject anyobject = jsonTransactionEvent.getJSONObject("any");
							String namespace = "";
							boolean namespace_flag = false;

							Iterator<String> keyIter_ns = anyobject.keys();
							while (keyIter_ns.hasNext()) {
								String temp = keyIter_ns.next();
								if (temp.substring(0, 1).equals("@")) {
									namespace_flag = true;
									namespace = temp.substring(1, temp.length());
								}
							}

							if (!namespace_flag) {
								log.info("Json Document doesn't have namespace in any field");
								return new ResponseEntity<>(
										"Error: Json Document doesn't have namespace in any field"
												+ " for detail validation check for TransactionEvent",
										HttpStatus.BAD_REQUEST);

							}
							/* finish finding namespace in the any field. */

							/*
							 * Start Validation whether each component use correct name space
							 */;

							Iterator<String> keyIter = anyobject.keys();
							while (keyIter.hasNext()) {
								String temp = keyIter.next();

								if (!temp.contains(namespace)) {
									log.info("Json Document use invalid namespace in anyfield");
									return new ResponseEntity<>(
											"Error: Json Document use invalid namespace in anyfield"
													+ " for detail validation check for TransactionEvent",
											HttpStatus.BAD_REQUEST);
								}
							}

						}

						MongoCaptureUtil m = new MongoCaptureUtil();
						m.captureJSONEvent(jsonEventList.getJSONObject(i).getJSONObject("TransactionEvent"), userPartyID);
					} else {
						log
								.info("Json Document is not valid. " + " It doesn't have standard event_type");
						return new ResponseEntity<>(
								"Error: Json Document is not valid" + " It doesn't have standard event_type",
								HttpStatus.BAD_REQUEST);

					}

				}
				if (jsonEventList.length() != 0)
					log.info(" EPCIS Document : Captured ");

			} catch (JSONException e) {
				log.info(" Json Document is not valid " + "second_validcheck");
			} catch (Exception e) {
				log.error(e.toString());
			}

			return new ResponseEntity<>("EPCIS Document : Captured ", HttpStatus.OK);

		} else {
			JSONObject jsonEvent = new JSONObject(inputString);
			JSONArray jsonEventList = jsonEvent.getJSONObject("epcis").getJSONObject("EPCISBody")
					.getJSONArray("EventList");

			for (int i = 0; i < jsonEventList.length(); i++) {

				JSONObject jsonEventElement = jsonEventList.getJSONObject(i);

				if (jsonEventElement.has("ObjectEvent") == true) {
					MongoCaptureUtil m = new MongoCaptureUtil();
					m.captureJSONEvent(jsonEventElement.getJSONObject("ObjectEvent"), userPartyID);
				} else if (jsonEventElement.has("AggregationEvent") == true) {
					MongoCaptureUtil m = new MongoCaptureUtil();
					m.captureJSONEvent(jsonEventElement.getJSONObject("AggregationEvent"), userPartyID);
				} else if (jsonEventElement.has("TransformationEvent") == true) {
					MongoCaptureUtil m = new MongoCaptureUtil();
					m.captureJSONEvent(jsonEventElement.getJSONObject("TransformationEvent"), userPartyID);
				} else if (jsonEventElement.has("TransactionEvent") == true) {
					MongoCaptureUtil m = new MongoCaptureUtil();
					m.captureJSONEvent(jsonEventElement.getJSONObject("TransactionEvent"), userPartyID);
				}
			}
		}
		return new ResponseEntity<>("EPCIS Document : Captured ", HttpStatus.OK);

	}

}
