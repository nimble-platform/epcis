package eu.nimble.service.epcis.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.oliot.epcis.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MasterDataController extends BaseRestController{
    private static Logger log = LoggerFactory.getLogger(MasterDataController.class);

    @GetMapping("/GetMasterDataItem")
    public ResponseEntity<?> getMasterDataItem(@RequestParam("id") String id) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");
        MongoCollection<BsonDocument> collection = Configuration.mongoDatabase.getCollection("MasterData",
                BsonDocument.class);
        BsonDocument masterDataItem = collection.find(
                Filters.eq("_id", new ObjectId(id))
        ).first();
        return new ResponseEntity<>(masterDataItem.toJson(), responseHeaders, HttpStatus.OK);
    }
}
