package org.oliot.epcis.service.query;

import static org.oliot.epcis.service.query.mongodb.MongoQueryUtil.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.epcis.service.query.mongodb.MongoQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Copyright (C) 2014 KAIST RESL
 *
 * This project is part of Oliot (oliot.org), pursuing the implementation of
 * Electronic Product Code Information Service(EPCIS) v1.1 specification in
 * EPCglobal.
 * [http://www.gs1.org/gsmp/kc/epcglobal/epcis/epcis_1_1-standard-20140520.pdf]
 * 
 *
 * @author Jack Jaewook Byun, Ph.D student
 * 
 *         Korea Advanced Institute of Science and Technology (KAIST)
 * 
 *         Real-time Embedded System Laboratory(RESL)
 * 
 *         bjw0829@kaist.ac.kr
 */
@Controller
public class RESTLikeQueryService implements ServletContextAware {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private HttpServletRequest request;

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Registers a subscriber for a previously defined query having the
	 * specified name. The params argument provides the values to be used for
	 * any named parameters defined by the query. The dest parameter specifies a
	 * destination where results from the query are to be delivered, via the
	 * Query Callback Interface. The dest parameter is a URI that both
	 * identifies a specific binding of the Query Callback Interface to use and
	 * specifies addressing information. The controls parameter controls how the
	 * subscription is to be processed; in particular, it specifies the
	 * conditions under which the query is to be invoked (e.g., specifying a
	 * periodic schedule). The subscriptionID is an arbitrary string that is
	 * copied into every response delivered to the specified destination, and
	 * otherwise not interpreted by the EPCIS service. The client may use the
	 * subscriptionID to identify from which subscription a given result was
	 * generated, especially when several subscriptions are made to the same
	 * destination. The dest argument MAY be null or empty, in which case
	 * results are delivered to a pre-arranged destination based on the
	 * authenticated identity of the caller. If the EPCIS implementation does
	 * not have a destination pre-arranged for the caller, or does not permit
	 * this usage, it SHALL raise an InvalidURIException.
	 */
	@RequestMapping(value = "/Subscribe/{queryName}/{subscriptionID}", method = RequestMethod.GET)
	@ResponseBody
	public String subscribe(@PathVariable String queryName,
			@PathVariable String subscriptionID, @RequestParam String dest,
			@RequestParam String cronExpression,
			@RequestParam(required = false) boolean reportIfEmpty,
			@RequestParam(required = false) String eventType,
			@RequestParam(required = false) String GE_eventTime,
			@RequestParam(required = false) String LT_eventTime,
			@RequestParam(required = false) String GE_recordTime,
			@RequestParam(required = false) String LT_recordTime,
			@RequestParam(required = false) String EQ_action,
			@RequestParam(required = false) String EQ_bizStep,
			@RequestParam(required = false) String EQ_disposition,
			@RequestParam(required = false) String EQ_readPoint,
			@RequestParam(required = false) String WD_readPoint,
			@RequestParam(required = false) String EQ_bizLocation,
			@RequestParam(required = false) String WD_bizLocation,
			@RequestParam(required = false) String EQ_transformationID,
			@RequestParam(required = false) String MATCH_epc,
			@RequestParam(required = false) String MATCH_parentID,
			@RequestParam(required = false) String MATCH_inputEPC,
			@RequestParam(required = false) String MATCH_outputEPC,
			@RequestParam(required = false) String MATCH_anyEPC,
			@RequestParam(required = false) String MATCH_epcClass,
			@RequestParam(required = false) String MATCH_inputEPCClass,
			@RequestParam(required = false) String MATCH_outputEPCClass,
			@RequestParam(required = false) String MATCH_anyEPCClass,
			@RequestParam(required = false) String EQ_quantity,
			@RequestParam(required = false) String GT_quantity,
			@RequestParam(required = false) String GE_quantity,
			@RequestParam(required = false) String LT_quantity,
			@RequestParam(required = false) String LE_quantity,
			@RequestParam(required = false) String orderBy,
			@RequestParam(required = false) String orderDirection,
			@RequestParam(required = false) String eventCountLimit,
			@RequestParam(required = false) String maxEventCount,
			Map<String, String> params) {

		if (Configuration.backend.equals("MongoDB")) {
			MongoQueryService mongoQueryService = new MongoQueryService();
			return mongoQueryService.subscribe(queryName, subscriptionID, dest,
					cronExpression, reportIfEmpty, eventType, GE_eventTime,
					LT_eventTime, GE_recordTime, LT_recordTime, EQ_action,
					EQ_bizStep, EQ_disposition, EQ_readPoint, WD_readPoint,
					EQ_bizLocation, WD_bizLocation, EQ_transformationID,
					MATCH_epc, MATCH_parentID, MATCH_inputEPC, MATCH_outputEPC,
					MATCH_anyEPC, MATCH_epcClass, MATCH_inputEPCClass,
					MATCH_outputEPCClass, MATCH_anyEPCClass, EQ_quantity,
					GT_quantity, GE_quantity, LT_quantity, LE_quantity,
					orderBy, orderDirection, eventCountLimit, maxEventCount,
					params);
		} else if (Configuration.backend.equals("Cassandra")) {
			return null;
		} else if (Configuration.backend.equals("MySQL")) {
			return null;
		}

		return null;
	}

	/**
	 * Removes a previously registered subscription having the specified
	 * subscriptionID.
	 */
	@RequestMapping(value = "/Unsubscribe/{subscriptionID}", method = RequestMethod.GET)
	public void unsubscribe(@PathVariable String subscriptionID) {

		if (Configuration.backend.equals("MongoDB")) {
			MongoQueryService mongoQueryService = new MongoQueryService();
			mongoQueryService.unsubscribe(subscriptionID);
		} else if (Configuration.backend.equals("Cassandra")) {

		} else if (Configuration.backend.equals("MySQL")) {

		}

	}

	/**
	 * Returns a list of all subscriptionIDs currently subscribed to the
	 * specified named query.
	 */
	@RequestMapping(value = "/GetSubscriptionIDs/{queryName}", method = RequestMethod.GET)
	@ResponseBody
	public String getSubscriptionIDsREST(@PathVariable String queryName) {

		if (Configuration.backend.equals("MongoDB")) {
			MongoQueryService mongoQueryService = new MongoQueryService();
			return mongoQueryService.getSubscriptionIDsREST(queryName);
		} else if (Configuration.backend.equals("Cassandra")) {
			return null;
		} else if (Configuration.backend.equals("MySQL")) {
			return null;
		}

		return null;
	}

	@RequestMapping(value = "/Poll/{queryName}", method = RequestMethod.GET)
	@ResponseBody
	public String poll(@PathVariable String queryName,
			@RequestParam(required = false) String eventType,
			@RequestParam(required = false) String GE_eventTime,
			@RequestParam(required = false) String LT_eventTime,
			@RequestParam(required = false) String GE_recordTime,
			@RequestParam(required = false) String LT_recordTime,
			@RequestParam(required = false) String EQ_action,
			@RequestParam(required = false) String EQ_bizStep,
			@RequestParam(required = false) String EQ_disposition,
			@RequestParam(required = false) String EQ_readPoint,
			@RequestParam(required = false) String WD_readPoint,
			@RequestParam(required = false) String EQ_bizLocation,
			@RequestParam(required = false) String WD_bizLocation,
			@RequestParam(required = false) String EQ_transformationID,
			@RequestParam(required = false) String MATCH_epc,
			@RequestParam(required = false) String MATCH_parentID,
			@RequestParam(required = false) String MATCH_inputEPC,
			@RequestParam(required = false) String MATCH_outputEPC,
			@RequestParam(required = false) String MATCH_anyEPC,
			@RequestParam(required = false) String MATCH_epcClass,
			@RequestParam(required = false) String MATCH_inputEPCClass,
			@RequestParam(required = false) String MATCH_outputEPCClass,
			@RequestParam(required = false) String MATCH_anyEPCClass,
			@RequestParam(required = false) String EQ_quantity,
			@RequestParam(required = false) String GT_quantity,
			@RequestParam(required = false) String GE_quantity,
			@RequestParam(required = false) String LT_quantity,
			@RequestParam(required = false) String LE_quantity,
			@RequestParam(required = false) String orderBy,
			@RequestParam(required = false) String orderDirection,
			@RequestParam(required = false) String eventCountLimit,
			@RequestParam(required = false) String maxEventCount,

			@RequestParam(required = false) String vocabularyName,
			@RequestParam(required = false) boolean includeAttributes,
			@RequestParam(required = false) boolean includeChildren,
			@RequestParam(required = false) String attributeNames,
			@RequestParam(required = false) String EQ_name,
			@RequestParam(required = false) String WD_name,
			@RequestParam(required = false) String HASATTR,
			@RequestParam(required = false) String maxElementCount,
			@RequestParam Map<String, String> params) {

		if (Configuration.backend.equals("MongoDB")) {
			MongoQueryService mongoQueryService = new MongoQueryService();
			return mongoQueryService.poll(queryName, eventType, GE_eventTime,
					LT_eventTime, GE_recordTime, LT_recordTime, EQ_action,
					EQ_bizStep, EQ_disposition, EQ_readPoint, WD_readPoint,
					EQ_bizLocation, WD_bizLocation, EQ_transformationID,
					MATCH_epc, MATCH_parentID, MATCH_inputEPC, MATCH_outputEPC,
					MATCH_anyEPC, MATCH_epcClass, MATCH_inputEPCClass,
					MATCH_outputEPCClass, MATCH_anyEPCClass, EQ_quantity,
					GT_quantity, GE_quantity, LT_quantity, LE_quantity,
					orderBy, orderDirection, eventCountLimit, maxEventCount,
					vocabularyName, includeAttributes, includeChildren,
					attributeNames, EQ_name, WD_name, HASATTR, maxElementCount,
					params);
		} else if (Configuration.backend.equals("Cassandra")) {
			return null;
		} else if (Configuration.backend.equals("MySQL")) {
			return null;
		}

		return null;
	}

	/**
	 * [REST Version of getQueryNames] Returns a list of all query names
	 * available for use with the subscribe and poll methods. This includes all
	 * pre- defined queries provided by the implementation, including those
	 * specified in Section 8.2.7.
	 * 
	 * No Dependency with Backend
	 * 
	 * @return JSONArray of query names ( String )
	 */
	@RequestMapping(value = "/GetQueryNames", method = RequestMethod.GET)
	@ResponseBody
	public String getQueryNamesREST() {
		JSONArray jsonArray = new JSONArray();
		List<String> queryNames = getQueryNames();
		for (int i = 0; i < queryNames.size(); i++) {
			jsonArray.put(queryNames.get(i));
		}
		return jsonArray.toString(1);
	}

	/**
	 * Returns a list of all query names available for use with the subscribe
	 * and poll methods. This includes all pre- defined queries provided by the
	 * implementation, including those specified in Section 8.2.7.
	 * 
	 * No Dependency with Backend
	 */
	public List<String> getQueryNames() {
		List<String> queryNames = new ArrayList<String>();
		queryNames.add("SimpleEventQuery");
		queryNames.add("SimpleMasterDataQuery");
		return queryNames;
	}

	/**
	 * Returns a string that identifies what version of the specification this
	 * implementation complies with. The possible values for this string are
	 * defined by GS1. An implementation SHALL return a string corresponding to
	 * a version of this specification to which the implementation fully
	 * complies, and SHOULD return the string corresponding to the latest
	 * version to which it complies. To indicate compliance with this Version
	 * 1.1 of the EPCIS specification, the implementation SHALL return the
	 * string 1.1.
	 * 
	 * No Dependency with Backend
	 */
	@RequestMapping(value = "/GetStandardVersion", method = RequestMethod.GET)
	@ResponseBody
	public String getStandardVersion() {
		return "1.1";
	}

	/**
	 * Returns a string that identifies what vendor extensions this
	 * implementation provides. The possible values of this string and their
	 * meanings are vendor-defined, except that the empty string SHALL indicate
	 * that the implementation implements only standard functionality with no
	 * vendor extensions. When an implementation chooses to return a non-empty
	 * string, the value returned SHALL be a URI where the vendor is the owning
	 * authority. For example, this may be an HTTP URL whose authority portion
	 * is a domain name owned by the vendor, a URN having a URN namespace
	 * identifier issued to the vendor by IANA, an OID URN whose initial path is
	 * a Private Enterprise Number assigned to the vendor, etc.
	 * 
	 * No Dependency with Backend
	 */
	@RequestMapping(value = "/GetVendorVersion", method = RequestMethod.GET)
	@ResponseBody
	public String getVendorVersion() {
		// It is not a version of Vendor
		return null;
	}

	// Misc APIs for DrM
	@RequestMapping(value = "/events", method = RequestMethod.GET)
	@ResponseBody
	public String getSampleEvents(@RequestParam String epc,
			@RequestParam(required = false) String fromTime,
			@RequestParam(required = false) String toTime,
			@RequestParam(required = false) String rate,
			@RequestParam(required = false) String isCompressed,
			@RequestParam(required = false) int order) {

		// No validation on query, do your best effort

		// Make Query Objects
		List<DBObject> queryList = new ArrayList<DBObject>();
		try {
			if (fromTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				GregorianCalendar geEventTimeCalendar = new GregorianCalendar();
				geEventTimeCalendar.setTime(sdf.parse(fromTime));
				long geEventTimeMillis = geEventTimeCalendar.getTimeInMillis();
				DBObject query = new BasicDBObject();
				query.put("eventTime", new BasicDBObject("$gte",
						geEventTimeMillis));
				queryList.add(query);
			}
			if (toTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				GregorianCalendar ltEventTimeCalendar = new GregorianCalendar();
				Date date = sdf.parse(toTime);
				ltEventTimeCalendar.setTime(date);
				long ltEventTimeMillis = ltEventTimeCalendar.getTimeInMillis();
				DBObject query = new BasicDBObject();
				query.put("eventTime", new BasicDBObject("$lte",
						ltEventTimeMillis));
				queryList.add(query);
			}

			if (epc != null) {
				DBObject query = getINQueryObject(
						new String[] { "epcList.epc" }, epc);
				if (query != null)
					queryList.add(query);
			}
		} catch (ParseException e) {
			return e.toString();
		}

		// Field Projection
		DBObject fields = new BasicDBObject();
		fields.put("eventTime", 1);
		fields.put("epcList", 1);
		fields.put("extension", 1);
		fields.put("_id", 0);

		// Query
		ApplicationContext ctx = new GenericXmlApplicationContext(
				"classpath:MongoConfig.xml");
		MongoOperations mongoOperation = (MongoOperations) ctx
				.getBean("mongoTemplate");
		DBCollection collection = mongoOperation.getCollection("ObjectEvent");

		// Merge All the queries with and
		DBObject baseQuery = new BasicDBObject();
		DBCursor cursor;
		if (queryList.isEmpty() == false) {
			BasicDBList aggreQueryList = new BasicDBList();
			for (int i = 0; i < queryList.size(); i++) {
				aggreQueryList.add(queryList.get(i));
			}
			baseQuery.put("$and", aggreQueryList);
			// Query
			cursor = collection.find(baseQuery, fields);
		} else {
			cursor = collection.find();
		}
		
		DBObject orderBy = new BasicDBObject();
		orderBy.put("eventTime", order);

		cursor.sort(orderBy);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		// Rate should be integer
		int rateInt = 1;
		try {
			rateInt = Integer.parseInt(rate);
		} catch (NumberFormatException e) {
			((AbstractApplicationContext) ctx).close();
			return e.toString();
		}

		JSONArray retArr = new JSONArray();

		int count = 1;

		while (cursor.hasNext()) {
			DBObject dbObject = cursor.next();
			if (count % rateInt == 0) {
				if (dbObject.containsField("extension") == true) {
					long eventTime = (long) dbObject.get("eventTime");
					GregorianCalendar eventCalendar = new GregorianCalendar();
					eventCalendar.setTimeInMillis(eventTime);
					DBObject ext1 = (DBObject) dbObject.get("extension");
					DBObject ext2 = (DBObject) ext1.get("extension");
					DBObject any = (DBObject) ext2.get("any");
					JSONObject extObj = new JSONObject(any.toMap());
					if (isCompressed != null && isCompressed.equals("true")) {
						// Compression of ECG, EMG
						if (extObj.isNull("ehealth_sensors_ECG") == false) {
							String ecg = (String) extObj
									.get("ehealth_sensors_ECG");
							String[] sp = ecg.split(",");
							if (sp.length > 1) {
								extObj.put("ehealth_sensors_ECG", sp[1]);
							}
						}
						if (extObj.isNull("ehealth_sensors_EMG") == false) {
							String emg = (String) extObj
									.get("ehealth_sensors_EMG");
							String[] sp = emg.split(",");
							if (sp.length > 1) {
								extObj.put("ehealth_sensors_EMG", sp[1]);
							}
						}
					}
					String time = sdf.format(eventCalendar.getTime());
					JSONObject json = new JSONObject();
					json.put(time, extObj);
					retArr.put(json);
				}
			}
			count++;
		}

		((AbstractApplicationContext) ctx).close();
		return retArr.toString(1);
	}

	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	@ResponseBody
	public String getSummary(@RequestParam String epc) {

		ApplicationContext ctx = new GenericXmlApplicationContext(
				"classpath:MongoConfig.xml");
		MongoOperations mongoOperation = (MongoOperations) ctx
				.getBean("mongoTemplate");
		DBCollection collection = mongoOperation.getCollection("Summary");
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		DBCursor cursor = collection.find();
		JSONArray retArr = new JSONArray();

		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			String id = (String) obj.get("_id");
			if (id.contains(epc)) {
				String[] ids = id.split("\\|");
				if (ids.length < 2)
					continue;
				String timeString = ids[1];
				Long timeLong = Long.parseLong(timeString);
				Date date = new Date(timeLong);
				String time = sdf.format(date);
				JSONObject jObj = new JSONObject();
				jObj.put(time, obj.get("value"));
				jsonList.add(jObj);
			}
		}

		Collections.sort(jsonList, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return JSONObject.getNames(o1)[0].compareTo(JSONObject
						.getNames(o2)[0]);
			}

		});

		for (int i = 0; i < jsonList.size(); i++) {
			retArr.put(jsonList.get(i));
		}

		((AbstractApplicationContext) ctx).close();
		return retArr.toString(1);
	}

	@RequestMapping(value = "/event", method = RequestMethod.GET)
	@ResponseBody
	public String getRecentEvent(@RequestParam String epcList) {

		// No validation on query, do your best effort

		ApplicationContext ctx = new GenericXmlApplicationContext(
				"classpath:MongoConfig.xml");
		MongoOperations mongoOperation = (MongoOperations) ctx
				.getBean("mongoTemplate");
		DBCollection collection = mongoOperation.getCollection("ObjectEvent");
		//TODO:
		// Field Projection
		DBObject fields = new BasicDBObject();
		fields.put("eventTime", 1);
		fields.put("epcList", 1);
		fields.put("extension", 1);
		fields.put("_id", 0);

		long curTime = System.currentTimeMillis();
		
		JSONArray retArr = new JSONArray();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// For each comma separated EPCs
		String[] epcArr = epcList.split(",");
		for (int i = 0; i < epcArr.length; i++) {
			JSONObject retObj = new JSONObject();
			String epc = epcArr[i].trim();
			retObj.put("epc", epc);
			DBObject query = getINQueryObject(new String[] { "epcList.epc" },
					epc);
			DBObject order = new BasicDBObject("eventTime", -1);
			DBCursor dbCursor = collection.find(query, fields);
			dbCursor.sort(order);
			dbCursor.limit(10);
			JSONArray dataArr = new JSONArray();
			while( dbCursor.hasNext() )
			{
				DBObject dbObject = dbCursor.next();
				if (dbObject != null) {
					long eventTime = (long) dbObject.get("eventTime");
					if( eventTime < ( curTime - 10000 ) )
					{
						continue;
					}
					GregorianCalendar eventCalendar = new GregorianCalendar();
					eventCalendar.setTimeInMillis(eventTime);
					String time = sdf.format(eventCalendar.getTime());
					DBObject ext1 = (DBObject) dbObject.get("extension");
					if (ext1 != null) {
						DBObject ext2 = (DBObject) ext1.get("extension");
						DBObject any = (DBObject) ext2.get("any");
						JSONObject extObj = new JSONObject();
						extObj.put(time, any);
						dataArr.put(extObj);
					}
				}
			}	
			retObj.put("data", dataArr);
			retArr.put(retObj);
		}
		((AbstractApplicationContext) ctx).close();
		return retArr.toString(1);
	}

	@Deprecated
	@RequestMapping(value = "/events/unit", method = RequestMethod.GET)
	@ResponseBody
	public String getEvents(@RequestParam String epc,
			@RequestParam(required = false) String fromTime,
			@RequestParam(required = false) String toTime,
			@RequestParam String samplingUnit) {

		// No validation on query, do your best effort

		// Make Query Objects
		List<DBObject> queryList = new ArrayList<DBObject>();
		try {
			if (fromTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				GregorianCalendar geEventTimeCalendar = new GregorianCalendar();
				geEventTimeCalendar.setTime(sdf.parse(fromTime));
				long geEventTimeMillis = geEventTimeCalendar.getTimeInMillis();
				DBObject query = new BasicDBObject();
				query.put("eventTime", new BasicDBObject("$gte",
						geEventTimeMillis));
				queryList.add(query);
			}
			if (toTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				GregorianCalendar ltEventTimeCalendar = new GregorianCalendar();
				Date date = sdf.parse(toTime);
				ltEventTimeCalendar.setTime(date);
				long ltEventTimeMillis = ltEventTimeCalendar.getTimeInMillis();
				DBObject query = new BasicDBObject();
				query.put("eventTime", new BasicDBObject("$lt",
						ltEventTimeMillis));
				queryList.add(query);
			}

			if (epc != null) {
				DBObject query = getINQueryObject(
						new String[] { "epcList.epc" }, epc);
				if (query != null)
					queryList.add(query);
			}
		} catch (ParseException e) {
			return e.toString();
		}

		// Field Projection
		DBObject fields = new BasicDBObject();
		fields.put("eventTime", 1);
		fields.put("epcList", 1);
		fields.put("extension", 1);
		fields.put("_id", 0);

		// Query
		ApplicationContext ctx = new GenericXmlApplicationContext(
				"classpath:MongoConfig.xml");
		MongoOperations mongoOperation = (MongoOperations) ctx
				.getBean("mongoTemplate");
		DBCollection collection = mongoOperation.getCollection("ObjectEvent");

		// Merge All the queries with $and
		DBObject baseQuery = new BasicDBObject();
		DBCursor cursor;
		if (queryList.isEmpty() == false) {
			BasicDBList aggreQueryList = new BasicDBList();
			for (int i = 0; i < queryList.size(); i++) {
				aggreQueryList.add(queryList.get(i));
			}
			baseQuery.put("$and", aggreQueryList);
			// Query
			cursor = collection.find(baseQuery, fields);
		} else {
			cursor = collection.find();
		}

		Map<String, JSONObject> retMap = new HashMap<String, JSONObject>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		while (cursor.hasNext()) {
			DBObject dbObject = cursor.next();
			if (dbObject.containsField("extension") == true) {
				long eventTime = (long) dbObject.get("eventTime");
				GregorianCalendar eventCalendar = new GregorianCalendar();
				eventCalendar.setTimeInMillis(eventTime);
				// Filtering
				if (samplingUnit.equals("sec")) {

				} else if (samplingUnit.equals("min")) {
					eventCalendar.set(Calendar.SECOND, 0);
				} else if (samplingUnit.equals("hour")) {
					eventCalendar.set(Calendar.MINUTE, 0);
					eventCalendar.set(Calendar.SECOND, 0);
				} else if (samplingUnit.equals("day")) {
					eventCalendar.set(Calendar.HOUR_OF_DAY, 1);
					eventCalendar.set(Calendar.MINUTE, 0);
					eventCalendar.set(Calendar.SECOND, 0);
				}
				DBObject ext1 = (DBObject) dbObject.get("extension");
				DBObject ext2 = (DBObject) ext1.get("extension");
				DBObject any = (DBObject) ext2.get("any");
				JSONObject extObj = new JSONObject(any.toMap());
				String time = sdf.format(eventCalendar.getTime());
				retMap.put(time, extObj);
			}
		}

		Iterator<String> iter = retMap.keySet().iterator();
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		while (iter.hasNext()) {
			JSONObject json = new JSONObject();
			String time = iter.next();
			json.put(time, retMap.get(time));
			jsonList.add(json);
		}
		Collections.sort(jsonList, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return JSONObject.getNames(o1)[0].compareTo(JSONObject
						.getNames(o2)[0]);
			}

		});

		JSONArray retArr = new JSONArray();

		for (int i = 0; i < jsonList.size(); i++) {
			retArr.put(jsonList.get(i));
		}

		((AbstractApplicationContext) ctx).close();
		return retArr.toString(1);
	}

	// Misc APIs for DrM
	@RequestMapping(value = "/location", method = RequestMethod.GET)
	@ResponseBody
	public String getLocation(@RequestParam String epcList) {

		// No validation on query, do your best effort

		ApplicationContext ctx = new GenericXmlApplicationContext(
				"classpath:MongoConfig.xml");
		MongoOperations mongoOperation = (MongoOperations) ctx
				.getBean("mongoTemplate");
		DBCollection collection = mongoOperation.getCollection("ObjectEvent");

		// Field Projection
		DBObject fields = new BasicDBObject();
		fields.put("eventTime", 1);
		fields.put("epcList", 1);
		fields.put("extension", 1);
		fields.put("_id", 0);

		JSONArray retArr = new JSONArray();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// For each comma separated EPCs
		String[] epcArr = epcList.split(",");
		for (int i = 0; i < epcArr.length; i++) {
			JSONObject retObj = new JSONObject();
			String epc = epcArr[i].trim();
			retObj.put("epc", epc);
			DBObject query = getINQueryObject(new String[] { "epcList.epc" },
					epc);
			DBObject order = new BasicDBObject("eventTime", -1);
			DBObject dbObject = collection.findOne(query, fields, order);
			if (dbObject != null) {
				long eventTime = (long) dbObject.get("eventTime");
				GregorianCalendar eventCalendar = new GregorianCalendar();
				eventCalendar.setTimeInMillis(eventTime);
				String time = sdf.format(eventCalendar.getTime());
				retObj.put("eventTime", time);
				DBObject ext1 = (DBObject) dbObject.get("extension");
				if (ext1 != null) {
					DBObject ext2 = (DBObject) ext1.get("extension");
					DBObject any = (DBObject) ext2.get("any");
					if (any.containsField("smartphone_sensors_gps_lon") == true
							&& any.containsField("smartphone_sensors_gps_lat") == true) {
						retObj.put("lon", any.get("smartphone_sensors_gps_lon")
								.toString());
						retObj.put("lat", any.get("smartphone_sensors_gps_lat")
								.toString());
						retArr.put(retObj);
					}
				}
			}
		}

		((AbstractApplicationContext) ctx).close();
		return retArr.toString(1);
	}
}
