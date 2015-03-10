package hamsterServer;

import java.util.HashSet;

/*
 * Created by Fairy on 10.03.2015.
 */
public class RemoteIpInfo implements Cloneable {
    private long requestCount;
    private long lastQueryTime;
    private HashSet<String> uriRequested = new HashSet<>();

    public long getRequestCount() {
        return requestCount;
    }

    public void incRequestCount() {
        requestCount++;
    }

    public long getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(long lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public HashSet<String> getUriRequested() {
        return uriRequested;
    }

    @Override
    protected Object clone() {
        RemoteIpInfo res = new RemoteIpInfo();
        res.requestCount = this.requestCount;
        res.lastQueryTime = this.lastQueryTime;
        res.uriRequested = (HashSet<String>)this.uriRequested.clone();
        return res;
    }
}