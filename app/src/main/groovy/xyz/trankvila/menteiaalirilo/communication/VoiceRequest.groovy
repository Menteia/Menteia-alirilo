package xyz.trankvila.menteiaalirilo.communication

import android.net.http.HttpResponseCache
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import groovy.transform.CompileStatic

@CompileStatic
class VoiceRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> resultListener

    VoiceRequest(int method, String url, @NonNull Response.Listener<byte[]> resultListener, @Nullable Response.ErrorListener listener) {
        super(method, url, listener)
        this.resultListener = resultListener
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response))
    }

    @Override
    protected void deliverResponse(byte[] response) {
        resultListener.onResponse(response)
    }
}
