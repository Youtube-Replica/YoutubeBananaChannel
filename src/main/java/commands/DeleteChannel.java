package commands;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import model.Channel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class DeleteChannel extends Command {
    public void execute(){
        HashMap<String, Object> props = parameters;
        com.rabbitmq.client.Channel channel = (com.rabbitmq.client.Channel) props.get("channel");
        JSONParser parser = new JSONParser();
        int id = 0;
        try {
            JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
            System.out.println(body.toString());
            JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
            id = Integer.parseInt(params.get("id").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");
        String response ="";
        response = Channel.deleteChannel(id);

        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
