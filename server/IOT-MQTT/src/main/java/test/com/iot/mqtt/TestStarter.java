package test.com.iot.mqtt;

import com.risky.server.MQTT.MQTTServerStarter;
import com.risky.server.MQTT.common.cache.redis.connection.MqttConnectionClientCache;
import com.risky.server.MQTT.config.ConnectClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MQTTServerStarter.class)
public class TestStarter {


    @Autowired
    private MqttConnectionClientCache mqttConnectionClientCache;

    @Test
    public void test(){
        mqttConnectionClientCache.add("1",new ConnectClient());

        mqttConnectionClientCache.get("1");
    }

}
