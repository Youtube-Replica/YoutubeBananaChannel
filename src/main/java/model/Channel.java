package model;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;

import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class Channel {
    static String collectionName = "channel";

    public static String getChannelByID(int id) {
        System.out.println("in get channel by id");
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        JSONObject channelObject = new JSONObject();
        try {
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);

            channelObject.put("channel_id",id);
            channelObject.put("info",myDocument.getAttribute("info"));
            channelObject.put("subscriptions",myDocument.getAttribute("subscriptions"));
            channelObject.put("watched_videos",myDocument.getAttribute("watched_videos"));
            channelObject.put("blocked_channels",myDocument.getAttribute("blocked_channels"));
            channelObject.put("notifications",myDocument.getAttribute("notifications"));

        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        return channelObject.toString();
    }

    public static String getChannelsContaining(String channel_containing, int offset, int limit) {
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        JSONObject allChannelsReturned = new JSONObject();

        try {
            String query = "FOR doc IN channel\n" +
                    "        FILTER doc.info.name == @value\n" +
                    "        RETURN doc";
            Map<String, Object> bindVars = new MapBuilder().put("value", channel_containing).get();
            System.out.println("Bind vars:" + bindVars.toString());

            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null, BaseDocument.class);

            if(cursor.hasNext()) {
                BaseDocument cursor2 = null;
                JSONArray searchArray = new JSONArray();
//                int new_id=0;
                for (; cursor.hasNext(); ) {
                    JSONObject searchObjectM = new JSONObject();
                    cursor2 = cursor.next();

                    BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument(cursor2.getKey(),
                            BaseDocument.class);
                    System.out.println("myDocument: " + myDocument.toString());
//                    new_id = Integer.parseInt(cursor2.getKey());

                    searchObjectM.put("id",myDocument.getAttribute("id"));
                    searchObjectM.put("info",myDocument.getAttribute("info"));
                    searchObjectM.put("subscriptions",myDocument.getAttribute("subscriptions"));
                    searchObjectM.put("watched_videos",myDocument.getAttribute("watched_videos"));
                    searchObjectM.put("blocked_channels",myDocument.getAttribute("blocked_channels"));
                    searchObjectM.put("notifications",myDocument.getAttribute("notifications"));
                    System.out.println("searchOBJECT: " + searchObjectM.toString());

                    searchArray.add(searchObjectM);
                }
                allChannelsReturned.put("channels", searchArray);
            }

        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        return allChannelsReturned.toString();
    }

    public static String createChannel(JSONObject info){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        BaseDocument myObject = new BaseDocument();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(c.getTime());

        info.put("subscriptions", 0);
        info.put("date_created",formattedDate.toString());
        myObject.addAttribute("info",info);
        myObject.addAttribute("subscriptions",new JSONArray());
        myObject.addAttribute("watched_videos",new JSONArray());
        myObject.addAttribute("blocked_channels",new JSONArray());
        myObject.addAttribute("notifications",new JSONArray());

        try {
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (ArangoDBException e) {
            System.err.println("Failed to create document. " + e.getMessage());
        }
        return "Document created";
    }

    public static String deleteChannel(int id){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        try {
        arangoDB.db(dbName).collection(collectionName).deleteDocument(""+id);
        }catch (ArangoDBException e){
            System.err.println("Failed to delete document. " + e.getMessage());
        }
        return "Channel Deleted";
    }

    public static String updateChannel(int channel_id ,JSONObject info, JSONArray subscriptions, JSONArray watched_videos,
                                       JSONArray blocked_channels, JSONArray notifications){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        BaseDocument myObject = arangoDB.db(dbName).collection(collectionName).getDocument("" + channel_id,
                BaseDocument.class);

        myObject.updateAttribute("info",info);
        myObject.updateAttribute("subscriptions",subscriptions);
        myObject.updateAttribute("watched_videos",watched_videos);
        myObject.updateAttribute("blocked_channels",blocked_channels);
        myObject.updateAttribute("notifications",notifications);

        try {
            arangoDB.db(dbName).collection(collectionName).deleteDocument(""+channel_id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (ArangoDBException e) {
            System.err.println("Failed to update document. " + e.getMessage());
        }
        return "Document updated";
    }

}
