package commands;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import model.Channel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class CreateChannel extends Command {
   public  void execute(){
    HashMap<String, Object> props = parameters;

    com.rabbitmq.client.Channel channel = (com.rabbitmq.client.Channel) props.get("channel");
    JSONParser parser = new JSONParser();
    JSONObject info = new JSONObject();
    int user_id = 0;

        try {
        JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
        JSONObject params = (JSONObject) parser.parse(body.get("body").toString());
        info = (JSONObject) parser.parse(params.get("info").toString());
        user_id = Integer.parseInt(params.get("user_id").toString());


        System.out.println("BODY: " + body);
        System.out.println("PARAMS: " + params);
        System.out.println("INFO: " + info);

    } catch (ParseException e) {
        e.printStackTrace();
    }

    AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
    AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
    Envelope envelope = (Envelope) props.get("envelope");
    String response = Channel.createChannel(user_id, info);
//        String response = (String)props.get("body");
    try {
        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
    } catch (IOException e) {
        e.printStackTrace();
    }

}
}

