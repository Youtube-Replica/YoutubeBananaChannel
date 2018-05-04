package commands;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import model.Channel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class UpdateChannel extends Command {
    public void execute(){
        HashMap<String, Object> props = parameters;
        com.rabbitmq.client.Channel channel = (com.rabbitmq.client.Channel) props.get("channel");
        JSONParser parser = new JSONParser();

        int channel_id  = 0;
        JSONObject info = null;
        JSONArray subscriptions = null;
        JSONArray watched_videos = null;
        JSONArray blocked_channels = null;
        JSONArray notifications = null;

        try {
            JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
            JSONObject params = (JSONObject) parser.parse(body.get("body").toString());

            channel_id = Integer.parseInt(params.get("id").toString());
            info = (JSONObject) parser.parse(params.get("info").toString());
            subscriptions = (JSONArray) params.get("subscriptions");
            watched_videos = (JSONArray) params.get("watched_videos");
            blocked_channels = (JSONArray) params.get("blocked_channels");
            notifications = (JSONArray) params.get("notifications");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");

        String response = Channel.updateChannel(channel_id, info, subscriptions, watched_videos, blocked_channels,
                notifications);
//        String response = (String)props.get("body");
        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
            channel.basicAck(envelope.getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
