package eu.nimble.service.epcis.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.nimble.service.epcis.services.AuthorizationSrv;
import io.swagger.annotations.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.epcis.converter.mongodb.model.MasterData;
import org.oliot.epcis.service.query.RESTLikeQueryService;
import org.oliot.epcis.service.query.mongodb.MongoQueryService;
import org.oliot.model.epcis.PollParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = { "EPCIS MasterData Operation" })
@RestController
public class MasterDataController extends BaseRestController{

    @Autowired
    AuthorizationSrv authorizationSrv;

    private static Logger log = LoggerFactory.getLogger(MasterDataController.class);

    @ApiOperation( value = "", hidden = true)
    @GetMapping("/GetMasterDataItem")
    public ResponseEntity<?> getMasterDataItem(@RequestParam("id") String id,
           @RequestHeader(value="Authorization", required=true) String bearerToken) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(bearerToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");
        MongoCollection<BsonDocument> collection = Configuration.mongoDatabase.getCollection("MasterData",
                BsonDocument.class);
        BsonDocument masterDataItem = collection.find(
                Filters.eq("_id", new ObjectId(id))
        ).first();
        return new ResponseEntity<>(masterDataItem.toJson(), responseHeaders, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete MasterData items for the given Id.",
            notes = "Delete all MasterData Items match on given Id", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "ObjectId is not valid?"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @PostMapping("/deleteMasterDataItemById")
    public ResponseEntity<?> deleteMasterDataItemById(@ApiParam(value = "MasterData Item Id", required = true) @RequestParam("id") String id,
                                                  @ApiParam(value = "The Bearer token provided by the identity service", required = true)
                                                  @RequestHeader(value="Authorization", required=true) String bearerToken) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(bearerToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        MongoCollection<BsonDocument> collection = Configuration.mongoDatabase.getCollection("MasterData",
                BsonDocument.class);
        BasicDBObject document = new BasicDBObject();
        document.append("id", id);
        collection.deleteMany(document);

        log.info("Delete All Master Data Items for Id: " + id);
        return new ResponseEntity<>("Delete All Master Data Items for Id: " + id, HttpStatus.OK);
    }


    @ApiOperation(value = "Get MasterData item for the given id.", notes = "Return one MasterData Item based on id",
            response = org.oliot.epcis.converter.mongodb.model.MasterData.class, responseContainer="List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @GetMapping("/GetLatestMasterDataById")
    public ResponseEntity<?> getLatestMasterDataItemById(@ApiParam(value = "MasterData id in NIMBLE Platform", required = true) @RequestParam("id") String id,
                                               @ApiParam(value = "The Bearer token provided by the identity service", required = true)
                                               @RequestHeader(value="Authorization", required=true) String bearerToken) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(bearerToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        PollParameters p = new PollParameters();
        p.setMaxElementCount(1);
        p.setEQ_name(id);
        p.setMasterDataFormat("JSON");
        p.setQueryName("SimpleMasterDataQuery");
        String masterDataItem = null;
        MongoQueryService mongoQueryService = new MongoQueryService();
        try {
            masterDataItem = mongoQueryService.poll(p, userPartyID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(masterDataItem, HttpStatus.OK);
    }
}
