package eu.nimble.service.epcis.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.nimble.service.epcis.services.AuthorizationSrv;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.oliot.epcis.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MasterDataController extends BaseRestController{

    @Autowired
    AuthorizationSrv authorizationSrv;

    private static Logger log = LoggerFactory.getLogger(MasterDataController.class);

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

    @PostMapping("/deleteMasterData")
    public ResponseEntity<?> deleteMasterDataItem(@RequestParam("id") String id,
              @RequestHeader(value="Authorization", required=true) String bearerToken) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(bearerToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        MongoCollection<BsonDocument> collection = Configuration.mongoDatabase.getCollection("MasterData",
                BsonDocument.class);
        collection.deleteOne(new Document("_id", new ObjectId(id)));
        return new ResponseEntity<>(collection.deleteOne(new Document("_id", new ObjectId(id))), HttpStatus.OK);
    }
}
