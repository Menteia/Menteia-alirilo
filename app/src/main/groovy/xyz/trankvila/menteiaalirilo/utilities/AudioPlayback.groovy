package xyz.trankvila.menteiaalirilo.utilities

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.regions.Region
import com.amazonaws.services.polly.AmazonPollyClient
import com.amazonaws.services.polly.AmazonPollyPresigningClient
import com.amazonaws.services.polly.model.DescribeVoicesRequest
import com.amazonaws.services.polly.model.OutputFormat
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest
import com.amazonaws.services.polly.model.TextType
import groovy.transform.CompileStatic
import org.dom4j.DocumentHelper
import org.dom4j.QName

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

@CompileStatic
final class AudioPlayback {
    private static AmazonPollyPresigningClient pollyClient

    private AudioPlayback() {}

    static void play(byte [] data, Context context) {
        final dir = context.cacheDir
        final tempfile = File.createTempFile("temp", "ogg", dir)
        tempfile.setBytes(data)
        final player = MediaPlayer.create(context, Uri.fromFile(tempfile))
        player.setOnCompletionListener({
            it.stop()
            it.release()
            tempfile.delete()
        })
        player.start()
        Log.d(this.simpleName, "Started playing")
    }

    static void speak(String ssml, Context context, boolean question, Closure onFinishedListener) {
        if (pollyClient == null) {
            AWSMobileClient.instance.initialize(context, new Callback<UserStateDetails>() {
                @Override
                void onResult(UserStateDetails result) {
                    try {
                        pollyClient = new AmazonPollyPresigningClient(AWSMobileClient.getInstance())
                        pollyClient.setRegion(Region.getRegion("us-west-2"))
                    } catch (Exception e) {
                        Log.e(AudioPlayback.name, "Error fetching voices", e)
                    }

                    synthesizeSpeech(ssml, question, onFinishedListener)
                }

                @Override
                void onError(Exception e) {
                    Log.e(AudioPlayback.name, "AWS initialization error", e)
                }
            })
        } else {
            synthesizeSpeech(ssml, question, onFinishedListener)
        }
    }

    private static synthesizeSpeech(String ssml, boolean question, Closure onFinishedListener) {
        final url = pollyClient.getPresignedSynthesizeSpeechUrl(new SynthesizeSpeechPresignRequest()
                .withVoiceId(question ? "Salli" : "Ivy")
                .withTextType(TextType.Ssml)
                .withText(ssml)
                .withOutputFormat(OutputFormat.Ogg_vorbis)
        )

        final player = new MediaPlayer()
        player.setOnCompletionListener({
            it.release()
            onFinishedListener.call()
        })
        player.setOnPreparedListener({
            it.start()
        })
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        try {
            player.setDataSource(url.toString())
        } catch (IOException e) {
            Log.e(this.name, "Unable to set data source", e)
        }

        player.prepareAsync()
    }
}
