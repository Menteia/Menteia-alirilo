package xyz.trankvila.menteiaalirilo.communication

import android.content.Intent
import android.util.Log
import com.amazon.device.messaging.ADMMessageHandlerBase
import com.amazon.device.messaging.ADMMessageReceiver

class ADMHandler extends ADMMessageHandlerBase {
    static class Receiver extends ADMMessageReceiver {
        Receiver() {
            super(ADMHandler.class)
        }
    }

    ADMHandler() {
        super(null)
    }

    @Override
    protected void onMessage(Intent intent) {
        final message = intent.getStringExtra("text")
        Log.d(this.class.simpleName, "Received: $message")
    }

    @Override
    protected void onRegistrationError(String s) {

    }

    @Override
    protected void onRegistered(String newRegistrationId) {
        Log.d(this.class.simpleName, "Registration ID: $newRegistrationId")
    }

    @Override
    protected void onUnregistered(String s) {

    }
}
