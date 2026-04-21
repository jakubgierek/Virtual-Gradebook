package pl.dziennik.virtualgradebookfx.network.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private RequestType type;
    private Map<String, Object> data = new HashMap<>();

    public Request() {
    }

    public Request(RequestType type) {
        this.type = type;
    }

    public Request(RequestType type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Request add(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}