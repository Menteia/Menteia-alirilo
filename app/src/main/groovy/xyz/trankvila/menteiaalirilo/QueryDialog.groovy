package xyz.trankvila.menteiaalirilo

import android.app.AlertDialog
import android.app.Dialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.transition.Visibility
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.RequestQueue
import groovy.transform.CompileStatic
import xyz.trankvila.menteiaalirilo.communication.Queries
import xyz.trankvila.menteiaalirilo.communication.VoiceRequest
import xyz.trankvila.menteiaalirilo.utilities.AudioPlayback
import xyz.trankvila.menteiaalirilo.utilities.Session

@CompileStatic
class QueryDialog extends DialogFragment {
    @Override
    Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final builder = new AlertDialog.Builder(getActivity())
        final arguments = getArguments()
        final requestQueue = Session.requestQueue
        final title = arguments.getString("title")
        final layout = getActivity().layoutInflater.inflate(R.layout.dialog_query, null)
        builder.setView(layout)
        final progressIndicator = layout.findViewById(R.id.dialog_query_progress_bar) as ProgressBar
        final responseText = layout.findViewById(R.id.dialog_query_response_text) as TextView
        responseText.setVisibility(View.GONE)
        final request = Queries.currentIndoorCondition({
            final response = it.getString("teksto")
            progressIndicator.setVisibility(View.GONE)
            responseText.setVisibility(View.VISIBLE)
            responseText.setText(response)
            final voiceRequest = Queries.voiceRequest(it.getString("UUID"), {
                AudioPlayback.play(it, getActivity())
            })
            requestQueue.add(voiceRequest)
        })
        requestQueue.add(request)
        builder.setTitle(title)
        builder.setPositiveButton(android.R.string.ok, null)
        return builder.create()
    }
}
