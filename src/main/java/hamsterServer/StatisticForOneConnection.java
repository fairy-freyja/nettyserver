package hamsterServer;

import java.util.Date;

/*
 * Created by Fairy on 07.03.2015.
 */
public class StatisticForOneConnection {
	private String ip = "";
	private String uri = "";
	private Date date = new Date();
	private long writeBytes = 0;
	private long readBytes = 0;
	private long speed = 0;

    public StatisticForOneConnection (){};

	
	public String toString() {
		return "" + ip
		   + " " + uri
		   + " " + date
		   + " " + writeBytes
		   + " " + readBytes
		   + " " + speed ;
	}
	
	public String getIP() {
		return ip + "";
	}
	public String getURI() {
		return uri + "";
	}
	public Date getDate() {
		return date;
	}
	public String getWriteBytes() {
		return writeBytes + "";
	}
	public String getReadBytes() {
		return readBytes + "";
	}
	public String getSpeed() {
		return speed + "";
	}

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setWriteBytes(long writeBytes) {
        this.writeBytes = writeBytes;
    }

    public void setReadBytes(long readBytes) {
        this.readBytes = readBytes;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }
}
