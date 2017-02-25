package com.henrysnoek.spotifyandroidsdkexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.player.Config
import com.spotify.sdk.android.player.ConnectionStateCallback
import com.spotify.sdk.android.player.Error
import com.spotify.sdk.android.player.Player
import com.spotify.sdk.android.player.PlayerEvent
import com.spotify.sdk.android.player.Spotify
import com.spotify.sdk.android.player.SpotifyPlayer

class MainActivity : Activity(), Player.NotificationCallback, ConnectionStateCallback {

    private var mPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val builder = AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI)
        builder.setScopes(arrayOf("user-read-private", "streaming"))
        val request = builder.build()

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, intent)
            if (response.type == AuthenticationResponse.Type.TOKEN) {
                val playerConfig = Config(this, response.accessToken, CLIENT_ID)
                Spotify.getPlayer(playerConfig, this, object : SpotifyPlayer.InitializationObserver {
                    override fun onInitialized(spotifyPlayer: SpotifyPlayer) {
                        mPlayer = spotifyPlayer
                        mPlayer!!.addConnectionStateCallback(this@MainActivity)
                        mPlayer!!.addNotificationCallback(this@MainActivity)
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.message)
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        Spotify.destroyPlayer(this)
        super.onDestroy()
    }

    override fun onPlaybackEvent(playerEvent: PlayerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name)
    }

    override fun onPlaybackError(error: Error) {
        Log.d("MainActivity", "Playback error received: " + error.name)
    }

    override fun onLoggedIn() {
        Log.d("MainActivity", "User logged in")

        mPlayer!!.playUri(null, "spotify:track:6JEK0CvvjDjjMUBFoXShNZ", 0, 0)
    }

    override fun onLoggedOut() {
        Log.d("MainActivity", "User logged out")
    }

    override fun onLoginFailed(error: Error) {
        Log.d("MainActivity", "Login failed")
    }

    override fun onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred")
    }

    override fun onConnectionMessage(message: String) {
        Log.d("MainActivity", "Received connection message: " + message)
    }

    companion object {
        private val CLIENT_ID = BuildConfig.CLIENT_ID
        private val REDIRECT_URI = BuildConfig.REDIRECT_URI

        // Request code that will be used to verify if the result comes from correct activity
        // Can be any integer
        private val REQUEST_CODE = 1337
    }
}
