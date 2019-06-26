package xyz.trankvila.menteiaalirilo.utilities

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import groovy.transform.CompileStatic

@CompileStatic
final class AudioPlayback {
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
}
