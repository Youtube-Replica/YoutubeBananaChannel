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
       System.out.println();
       HashMap<String, Object> props = parameters;

       Channel channel = (Channel) props.get("channel");
       JSONParser parser = new JSONParser();
       int channel_id = 0;
       String channel_containing = "";
       int offset = 0;
       int limit = 0;
       String getType = "";
       try {
           JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
           JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
           System.out.println(params.toString());
           if(params.containsKey("offset")){
               channel_containing = params.get("channel_containing").toString();
               offset = Integer.parseInt(params.get("offset").toString());
               limit = Integer.parseInt(params.get("limit").toString());
               getType = "channelsPaginated";
           }
           else{
               channel_id = Integer.parseInt(params.get("id").toString());
               getType = "channel";
           }
       } catch (ParseException e) {
           e.printStackTrace();
       }
       AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
       AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
       Envelope envelope = (Envelope) props.get("envelope");
       String response = "";

       if(getType.equals("channelsPaginated")){
           System.out.println("Get channels paginated");
           response = model.Channel.getChannelsContaining(channel_containing, offset, limit);
       }
       else{
           System.out.println("Get channel by its ID");
           response = model.Channel.getChannelByID(channel_id);
       }
       try {
           channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
           channel.basicAck(envelope.getDeliveryTag(), false);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
