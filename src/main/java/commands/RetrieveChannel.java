package commands;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class RetrieveChannel extends Command {
   public void execute() {
       HashMap<String, Object> props = parameters;

       Channel channel = (Channel) props.get("channel");
       JSONParser parser = new JSONParser();
       int id = 0;
       int video_id = 0;
       int offset = 0;
       int limit = 0;
       String getType = "";
       try {
           JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
           System.out.println("******xxx******");
           JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
           System.out.println(params.toString());
           if(params.containsKey("video_id") && params.containsKey("offset")){
               video_id = Integer.parseInt(params.get("video_id").toString());
               offset = Integer.parseInt(params.get("offset").toString());
               limit = Integer.parseInt(params.get("limit").toString());
               getType = "videoIdPaginated";
           }
           else if(params.containsKey("video_id")){
               video_id = Integer.parseInt(params.get("video_id").toString());
               getType = "videoId";
           }
           else{
               id = Integer.parseInt(params.get("id").toString());
               getType = "allComments";
           }
       } catch (ParseException e) {
           e.printStackTrace();
       }
       System.out.println("Passed!");
       AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
       AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
       Envelope envelope = (Envelope) props.get("envelope");
       String response = "";

       if(getType.equals("videoIdPaginated")){
           System.out.println("Get comments by VIDEO ID paginated");
           response = model.Channel.getCommentsByVideoIDPaginated(video_id, offset, limit);
       }
       else if(getType.equals("videoId")){
           System.out.println("Get all comments on VIDEO ID");
           response = model.Channel.getCommentsByVideoID(id);
       }
       else{
           System.out.println("Get comment by its ID");
           response = model.Channel.getCommentByID(id);
       }
       try {
           channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
           channel.basicAck(envelope.getDeliveryTag(), false);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
