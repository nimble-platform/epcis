package eu.nimble.service.epcis.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.nimble.service.epcis.services.AuthorizationSrv;
import io.swagger.annotations.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.epcis.converter.mongodb.model.MasterData;
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

    @ApiOperation(value = "Get MasterData item for the given ObjectId.", notes = "Return one MasterData Item based on ObjectId, which is the unique id of MasterData Table",
            response = org.oliot.epcis.converter.mongodb.model.MasterData.class, responseContainer="List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @GetMapping("/GetMasterDataItem")
    public ResponseEntity<?> getMasterDataItem(@ApiParam(value = "MasterData ObjectId in NIMBLE Platform", required = true) @RequestParam("id") String id,
           @ApiParam(value = "The Bearer token provided by the identity service", required = true)
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

    @ApiOperation(value = "Delete MasterData item for the given ObjectId.",
            notes = "Delete one MasterData Item based on ObjectId, which is the unique id of MasterData Table", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "ObjectId is not valid?"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @PostMapping("/deleteMasterDataItem")
    public ResponseEntity<?> deleteMasterDataItem(@ApiParam(value = "MasterData ObjectId", required = true) @RequestParam("id") String id,
              @ApiParam(value = "The Bearer token provided by the identity service", required = true)
              @RequestHeader(value="Authorization", required=true) String bearerToken) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(bearerToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        MongoCollection<BsonDocument> collection = Configuration.mongoDatabase.getCollection("MasterData",
                BsonDocument.class);
        collection.deleteOne(new Document("_id", new ObjectId(id)));
        log.info("Delete Master Data Item Id: " + id);
        return new ResponseEntity<>("Delete Master Data Item Id: " + id, HttpStatus.OK);
    }
}
