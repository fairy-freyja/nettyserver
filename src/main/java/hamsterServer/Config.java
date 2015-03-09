package hamsterServer;

import java.io.IOException;
import java.util.Properties;

/*
 * Created by Fairy on 06.03.2015.
 */
public class Config {

    public static final int PORT;

    static {
        Properties prop = new Properties();
        try {
            prop.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            System.out.printf("File not found");
            e.printStackTrace();
        }

        PORT = Integer.parseInt(prop.getProperty("port"));
    }
}
