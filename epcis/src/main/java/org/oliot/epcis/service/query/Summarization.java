package org.oliot.epcis.service.query;

import java.util.TimerTask;

import org.oliot.epcis.configuration.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

public class Summarization extends TimerTask {

	@Override
	public void run() {
		ApplicationContext ctx = new GenericXmlApplicationContext(
				"classpath:MongoConfig.xml");
		MongoOperations mongoOperation = (MongoOperations) ctx
				.getBean("mongoTemplate");
		DBCollection collection = mongoOperation.getCollection("ObjectEvent");

		String mapFunc = "function(){ if( this.extension != null ) { emit( this.epcList[0].epc + \"|\"+Math.round(this.eventTime/86400000)*86400000, this.extension.extension.any ); }}";
		String reduceFunc = "function(key, values) { return values[0];}";
		
		MapReduceOutput out = collection.mapReduce(mapFunc, reduceFunc, "Summary", MapReduceCommand.OutputType.REPLACE, null);
		
		Configuration.logger
		.info("Summarization MapReduce completed  within " + out.getDuration() + "ms");
		((AbstractApplicationContext) ctx).close();
	}

}
