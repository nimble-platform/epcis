package eu.nimble.service.epcis.services;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlockchainService {

    /**
     * Prepare the information that need to be kept in Blockchain for a given JSON
     * event object
     *
     * @param jsonEventObj
     * @param senderPartyID
     * @return the prepared information as JSONObject
     */
    public JSONObject buildJSONEventForBC(JSONObject jsonEventObj)
    {
        JSONObject jsonEventForBlockchain = new JSONObject();
        String unifiedJSONStr = this.unifyJsonString(jsonEventObj);
        String completeHash = DigestUtils.sha256Hex(unifiedJSONStr);
        jsonEventForBlockchain.put("completeHash", completeHash);

        List<String> productIDs = this.extractProductIDsFromEvent(jsonEventObj);
        //JSONArray epcArray = new JSONArray(productIDs);
        JSONArray epcArray = new JSONArray();
        for (String productID : productIDs) {
            JSONObject epcJson = new JSONObject();
            epcJson.put("epc", productID);
            epcArray.put(epcJson);
        }
        jsonEventForBlockchain.put("epcList", epcArray);

        jsonEventForBlockchain.put("EventData", jsonEventObj);

        return jsonEventForBlockchain;
    }

    /**
     * Get a list of basic JSON events. Each product id in the given JSON event will has one respective basic JSON event.
     * Each basic Json Event has four dimensions:
     * WHAT fields: the product id
     * WHEN fields:	 eventTimeZoneOffset, eventTime
     * WHERE fields: bizLocation, readPoint
     * WHY fields: bizStep, disposition
     * @param jsonEventObj original JSON event
     * @return a list of basic JSON events
     */
    public List<JSONObject> getBasicEventDataForAllProductIDs(JSONObject jsonEventObj)
    {
        List<JSONObject> basicEvents = new ArrayList<JSONObject>();

        List<String> productIDs = this.extractProductIDsFromEvent(jsonEventObj);
        JSONObject dimensionData = this.getEventBasicData(jsonEventObj);
        for (String productID : productIDs) {
            JSONObject basicEventData = dimensionData;
            basicEventData.put("productID", productID);
            basicEvents.add(basicEventData);
        }

        return basicEvents;
    }

    /**
     * Get all product IDs from the event data
     *
     * @param jsonObj
     * @return any product ID in the form of EPC and EPC Class
     */
    public List<String> extractProductIDsFromEvent(JSONObject jsonObj) {
        List<String> productIDs = new ArrayList<String>();

        List<String> MATCH_anyEPCAndEPCClassPath = Arrays
                .asList(new String[] { "$..parentID", "$..epc", "$..epcClass" });

        String jsonStr = jsonObj.toString();
        ReadContext ctx = JsonPath.parse(jsonStr);

        for (String path : MATCH_anyEPCAndEPCClassPath) {

            List<String> ids = ctx.read(path);

            productIDs.addAll(ids);
        }

        return productIDs;
    }

    /**
     * Unify string representation of a JSON Event Object, in order to have same hash code for identical JSON objects.
     *
     * It can often happen, that identical JSON objects are presented as different strings. It will lead to different hash codes.
     * Because, for example, 1) key locations is different 2) value is different sorted in array
     *
     * In order to avoid this problem, this method will do following to have a unified string representation:
     * 1) Flatten key value pairs
     * 2) Remove the generated list numbers in the flattened key
     * 3) Each key value pair is connected as a string
     * 4) Sort the key value string
     * @param jsonObj JSON Event Object.
     * @return unified string representation.
     */
    public String unifyJsonString(JSONObject jsonObj)
    {
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonObj.toString());

        // Remove numbers from key e.g. "[0]" from "a.d[0]"
        String regexListNr = "\\[\\d+\\]";
        List<String> flattenKeyValPairList = flattenJson.entrySet().stream().
                map(entry -> entry.getKey().replaceAll(regexListNr, "") + "=" + entry.getValue()).
                collect(Collectors.toList());

        List<String> sortedFlattenKeyValPairList = flattenKeyValPairList.stream().sorted().collect(Collectors.toList());

        return String.join(",", sortedFlattenKeyValPairList);
    }


    /**
     * Get a JSON event object with the following dimensions:
     * WHEN fields:	 eventTimeZoneOffset, eventTime
     * WHERE fields: bizLocation, readPoint
     * WHY fields: bizStep
     *
     * The field "disposition" in the WHY dimension is possibly not exist, and will be included when it is necessary.
     * @param jsonObj
     * @return
     */
    public JSONObject getEventBasicData(JSONObject jsonObj) {
        JSONObject basicJson = new JSONObject();

        List<String> basicFields = Arrays.asList(new String[] { "eventTimeZoneOffset", "eventTime", "bizLocation",
                "readPoint", "bizStep" });

        for (String field : basicFields) {

            if (jsonObj.has(field)) {
                basicJson.put(field, jsonObj.get(field));
            }
        }

        return basicJson;
    }

    private String getFileWithUtil(String fileName) throws IOException {
        String result = "";

        ClassLoader classLoader = getClass().getClassLoader();
        result = IOUtils.toString(classLoader.getResourceAsStream(fileName));

        return result;
    }

    public static void main(String[] args) throws Exception {
        String filename = "testFiles/eventJson_single_store.json";
        String filename2 = "testFiles/eventJson_single_store2.json";

        BlockchainService bcSrv = new BlockchainService();
        String content = bcSrv.getFileWithUtil(filename);
        JSONObject jsonObj1 = new JSONObject(content);

        String content2 = bcSrv.getFileWithUtil(filename2);
        JSONObject jsonObj2 = new JSONObject(content2);

        String unifiedStr1 = bcSrv.unifyJsonString(jsonObj1);
        String unifiedStr2 = bcSrv.unifyJsonString(jsonObj2);

        System.out.print("equal: " + unifiedStr1.equals(unifiedStr2));
    }
}
