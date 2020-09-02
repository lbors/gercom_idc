/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.test;


import java.io.IOException;
import java.util.Date;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONException;
import org.bson.Document;


public class MongoDBInteracter {
	
	private MongoDatabase db;
	private MongoClient mongoClient;
	private MongoCollection<Document> collection;
	int port;
	
	/**
	 * addr: IP address of the node where MongoDB is running
	 * port: The port number (use default port number: 27017 )
	 * collName: collection name 
	 **/
        
	public MongoDBInteracter(String addr, int port, String collName){
		this.connectionManager(addr, port, collName);
	}
	
	private synchronized void connectionManager(String addr, int port, String collName){
		mongoClient = new MongoClient(addr, port);
		System.out.println("Establishing a connection to the Mongo DB...");
		db = mongoClient.getDatabase("test");
		collection = db.getCollection(collName);
	}
	
	public MongoDatabase getMongoDB(){
		return this.db;
	}
	
	public MongoClient getMongoClient(){
		return this.mongoClient;
	}
	
	public void setCollection(String collName){
		this.collection = this.db.getCollection(collName);
		
	}
	
	public MongoCollection<Document> getCollection(){
		return this.collection;
	}
	
	/* removing a document (service descriptor) from DB */
	public void removeDocument(MongoCollection<Document> coll, String id){
		coll.deleteMany(new Document("_id", id));
	}
	
	/* removing all documents (service descriptors) from DB */
	public void removeAllDocuments(MongoCollection<Document> coll){
		coll.deleteMany(new Document());
	}
	
	/* drop a collection from DB*/
	public void dropCollection(MongoCollection<Document> coll){
		coll.drop();
	}
	
	/* drop collection */
	public void dropCollection(){
		this.collection.drop();
	}
	
        
	public MongoCollection<Document> createMongoDBEntry(JSONObject slaKPI) throws java.text.ParseException, JSONException, IOException {
		JSONArray kpiList = (JSONArray) slaKPI.get("kpiList");
		int size = kpiList.length();
		Date endDate= new Date();
		
		Document doc = new Document()
			.append("_id", slaKPI.get("agreementId"))
			.append("instance_type", slaKPI.get("name"))
			.append("instanceId", slaKPI.get("agreementId"))
			.append("start_time",  new java.util.Date())
			.append("end_time",  endDate)
			.append("maxResult",  slaKPI.get("maxResult"));
                
		for (int i=0; i<size; i++) {
                    doc.append((String) kpiList.get(i), new Document());
		}
                
		collection.insertOne(doc);
		return collection;
	}
        
        
        public Document getMongoDBEntry(String serviceID, String probeName) {
            Document doc = collection.findOneAndDelete(new Document("_id", serviceID));
            //doc.get
            return doc;
        }	
}
