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
        System.out.println("in get channels containing: " + channel_containing);

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

                    searchObjectM.put("channel_id",myDocument.getAttribute("id"));
                    searchObjectM.put("info",myDocument.getAttribute("info"));
                    searchObjectM.put("subscriptions",myDocument.getAttribute("subscriptions"));
                    searchObjectM.put("watched_videos",myDocument.getAttribute("watched_videos"));
                    searchObjectM.put("blocked_channels",myDocument.getAttribute("blocked_channels"));
                    searchObjectM.put("notifications",myDocument.getAttribute("notifications"));
                    System.out.println("searchOBJECT: " + searchObjectM.toString());

                    searchArray.add(searchObjectM);
                }
                allChannelsReturned.put("channels containing: " + channel_containing, searchArray);
            }

        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        return allChannelsReturned.toString();
    }

    // Request: http://localhost:12345/comment?video_id=1&offset=5&limit=10
    // Response:
    public static String getCommentsByVideoIDPaginated(int id, int offset, int limit) {
        System.out.println("ID: " + id);
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        String collectionName = "comments";
        JSONObject allCommentsReturned = new JSONObject();

        try {
            String query = "FOR doc IN comments\n" +
                    "        FILTER doc.`video_id` == @value\n" +
                    //sort by time
                    "        LIMIT " + offset + ", " + limit +
                    "        RETURN doc";
            Map<String, Object> bindVars = new MapBuilder().put("value", id).get();
            System.out.println("Bind vars:");
            System.out.println(bindVars.toString());
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);

            if(cursor.hasNext()) {
                BaseDocument cursor2 = null;
                JSONArray searchArray = new JSONArray();
                int new_id=0;
                for (; cursor.hasNext(); ) {
                    JSONObject searchObjectM = new JSONObject();
                    cursor2 = cursor.next();
                    BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument(cursor2.getKey(),
                            BaseDocument.class);
                    new_id = Integer.parseInt(cursor2.getKey());

//                    searchObjectM.put("Video ID",myDocument2.getAttribute("video_id"));
                    searchObjectM.put("video_id",new_id);
                    searchObjectM.put("text",myDocument2.getAttribute("text"));
                    searchObjectM.put("likes",myDocument2.getAttribute("likes"));
                    searchObjectM.put("dislikes",myDocument2.getAttribute("dislikes"));
                    searchObjectM.put("channel_id",myDocument2.getAttribute("user"));
                    searchObjectM.put("mentions_ids",myDocument2.getAttribute("mentions"));
                    searchObjectM.put("reply_ids",myDocument2.getAttribute("replies"));

                    searchArray.add(searchObjectM);
                }
                allCommentsReturned.put("comments_on_video_: "+id,searchArray);
            }

        } catch (ArangoDBException e) {






            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        return allCommentsReturned.toString();
    }

    public static String createChannel(JSONObject info){
        System.out.println("In create channel");
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

    public static String updateComment(int comment_id ,int video_id, String text, JSONArray likes, JSONArray dislikes, int user_id, JSONArray mentions, JSONArray replies){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "scalable";
        String collectionName = "comments";
        BaseDocument myObject = arangoDB.db(dbName).collection(collectionName).getDocument("" + comment_id,
                BaseDocument.class);

        myObject.updateAttribute("video_id",video_id);
        myObject.updateAttribute("text",text);
        myObject.updateAttribute("likes",likes);
        myObject.updateAttribute("dislikes",dislikes);
        myObject.updateAttribute("user",user_id);
        myObject.updateAttribute("mentions",mentions);
        myObject.updateAttribute("replies",replies);
        try {
            arangoDB.db(dbName).collection(collectionName).deleteDocument(""+comment_id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (ArangoDBException e) {
            System.err.println("Failed to create document. " + e.getMessage());
        }
        return "Document updated";
    }

    }
