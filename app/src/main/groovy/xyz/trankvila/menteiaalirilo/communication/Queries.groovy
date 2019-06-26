package xyz.trankvila.menteiaalirilo.communication

import android.support.annotation.NonNull
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import groovy.transform.CompileStatic
import org.json.JSONObject
import xyz.trankvila.menteiaalirilo.utilities.Constants
import xyz.trankvila.menteiaalirilo.utilities.Session

@CompileStatic
final class Queries {
    private Queries() {}

    static JsonRequest currentIndoorCondition(Response.Listener<JSONObject> listener) {
        final query = "doni ko des frodeni testos"
        final url = "${Constants.baseURL}/respondi?token=${Session.userToken}"
        final request = new JsonObjectRequest(Request.Method.POST, url, null, listener, null) {
            @Override
            byte[] getBody() {
                return query.getBytes()
            }
        }
        request.retryPolicy = new DefaultRetryPolicy(10_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        return request
    }

    static VoiceRequest voiceRequest(String id, Response.Listener<byte[]> listener) {
        final url = "${Constants.baseURL}/paroli?id=$id&token=${Session.userToken}"
        return new VoiceRequest(Request.Method.GET, url, listener, null)
    }

    static VoiceRequest currentTimeReadout(Response.Listener<byte[]> listener) {
        final url = "${Constants.baseURL}/alirilo/now?token=${Session.userToken}"
        return new VoiceRequest(Request.Method.GET, url, listener, null)
    }

    static VoiceRequest currentDateReadout(Response.Listener<byte[]> listener) {
        final url = "${Constants.baseURL}/alirilo/today?token=${Session.userToken}"
        return new VoiceRequest(Request.Method.GET, url, listener, null)
    }
}
