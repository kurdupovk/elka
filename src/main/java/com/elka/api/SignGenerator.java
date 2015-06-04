package com.elka.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kostya
 */
public class SignGenerator {

    private static final String[] params = new String[]{"suid", "uid", "aid", "authKey", "sessionKey"};

    public static JSONObject getSignRequest(JSONObject _arg1, String url)  {
        try {
            JSONObject _local3 = _arg1.getJSONObject("params");

            for (int i = 0; i < params.length; i++) {
                _local3.put(params[i], _arg1.get(params[i]));
            };
            String[] split = url.split("/");
            _local3.put("action", split[split.length - 1].toLowerCase());
            _local3.put("controller", split[split.length - 2].toLowerCase());
            _arg1.put("sign", calcParamsArraySign(_local3));
            _local3.remove("action");
            _local3.remove("controller");
            for (int i = 0; i < params.length; i++) {
                _local3.remove(params[i]);
            };
            return _arg1;
        } catch (JSONException ex) {
            return new JSONObject();
        }
    }

    public static String calcParamsArraySign(Object _arg1) throws JSONException {
        int _local4;
        JSONObject _local5;
        String _local2 = "";
        JSONArray _local3;
        if (_arg1 instanceof JSONArray) {
            _local3 = sortArray((JSONArray) _arg1);
        } else {
            _local3 = sortObject((JSONObject) _arg1);
        }
        _local4 = 0;
        while (_local4 < _local3.length()) {
            _local5 = _local3.getJSONObject(_local4);
            if (_local5.get("value") instanceof JSONArray
                    || _local5.get("value") instanceof JSONObject) {
                _local5.put("value", calcParamsArraySign(_local5.get("value")));
            }
            _local4++;
        };
        _local4 = 0;
        while (_local4 < _local3.length()) {
            _local5 = _local3.getJSONObject(_local4);
            _local2 = _local2 + ((_local5.getString("key") + "=") + _local5.getString("value"));
            if (_local4 < (_local3.length() - 1)) {
                _local2 = (_local2 + "&");
            };
            _local4++;
        };
        return DigestUtils.md5Hex(_local2);
    }

    public static JSONArray sortArray(JSONArray _arg1) throws JSONException {
        JSONArray _local2 = new JSONArray();
        int _local3 = 0;
        while (_local3 < _arg1.length()) {
            _local2.put(new JSONObject().put("key", _local3).put("value", _arg1.get(_local3)));
            _local3++;
        };
        return _local2;
    }

    public static JSONArray sortObject(JSONObject _arg1) throws JSONException {
        Iterator keys = _arg1.keys();
        ArrayList<JSONObject> list = new ArrayList<>();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            list.add(new JSONObject().put("key", key).put("value", _arg1.get(key)));
        }
        Collections.sort(list, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject t, JSONObject t1) {
                return ((Comparable) t.opt("key")).compareTo(t1.opt("key"));
            }
        });
        return new JSONArray(list);
    }

}
