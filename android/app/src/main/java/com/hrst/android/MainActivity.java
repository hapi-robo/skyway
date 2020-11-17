package com.hrst.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import io.skyway.Peer.Browser.Canvas;
import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.DataConnection;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Peer mPeer;
    private MediaStream mLocalStream;
    private MediaStream	mRemoteStream;
    private MediaConnection mMediaConnection;
    private DataConnection mDataConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocalStream();
            } else {
                Toast.makeText(this, "Failed to access the camera and microphone.\nclick allow when asked for permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Canvas canvas = (Canvas) findViewById(R.id.remoteView);
        initWebRtc("hrst-1234567890", canvas);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Set default volume control stream type.
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Canvas canvas = (Canvas) findViewById(R.id.remoteView);
        destroyPeer(canvas);
    }

    void initWebRtc(String id, Canvas canvas) {
        if (mPeer != null) {
            return;
        }

        // initialize WebRTC connection
        PeerOption option = new PeerOption();
        option.key = BuildConfig.SKYWAY_API_KEY;
        option.domain = BuildConfig.SKYWAY_DOMAIN;
        mPeer = new Peer(this, id, option);
        Log.i(TAG, "Local ID: " + id);

        // set event handlers
        mPeer.on(Peer.PeerEventEnum.OPEN, (OnCallback) object -> {
            // request permissions
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},0);
            }
            else {
                startLocalStream();
            }

        });
        mPeer.on(Peer.PeerEventEnum.CALL, object -> {
            if (!(object instanceof MediaConnection)) {
                return;
            }
            mMediaConnection = (MediaConnection) object;
            setMediaCallbacks(canvas);
            mMediaConnection.answer(mLocalStream);
        });
        mPeer.on(Peer.PeerEventEnum.CONNECTION, object -> {
            Log.i(TAG, "Received data connection from remote peer");
            if (!(object instanceof DataConnection)) {
                return;
            }
            mDataConnection = (DataConnection) object;
            mDataConnection.on(DataConnection.DataEventEnum.OPEN, (obj) -> {
                Log.d(TAG, "Data connection: open");
            });
            mDataConnection.on(DataConnection.DataEventEnum.CLOSE, (obj) -> {
                Log.d(TAG, "Data connection: close");
            });
            mDataConnection.on(DataConnection.DataEventEnum.DATA, obj -> {
                Log.i(TAG, "Message received data type: " + obj.getClass().getSimpleName());
            });
        });
        mPeer.on(Peer.PeerEventEnum.DISCONNECTED, object -> Log.d(TAG, "Disconnected from signaling server"));
        mPeer.on(Peer.PeerEventEnum.CLOSE, object -> Log.d(TAG, "Disconnected from signaling server, media connection, and data connection"));
        mPeer.on(Peer.PeerEventEnum.ERROR, object -> {
            PeerError error = (PeerError) object;
            Log.d(TAG, error.typeString + ": " + error.getMessage());
        });
    }

    void startLocalStream() {
        Navigator.initialize(mPeer);
        MediaConstraints constraints = new MediaConstraints();
        mLocalStream = Navigator.getUserMedia(constraints);

        Canvas canvas = (Canvas) findViewById(R.id.localView);
        mLocalStream.addVideoRenderer(canvas,0);
    }

    void setMediaCallbacks(Canvas canvas) {
        mMediaConnection.on(MediaConnection.MediaEventEnum.STREAM, (OnCallback) object -> {
            mRemoteStream = (MediaStream) object;
            mRemoteStream.addVideoRenderer(canvas,0);
        });
        mMediaConnection.on(MediaConnection.MediaEventEnum.CLOSE, object -> closeRemoteStream(canvas));
        mMediaConnection.on(MediaConnection.MediaEventEnum.ERROR, object -> {
            PeerError error = (PeerError) object;
            Log.d(TAG, error.typeString + ": " + error.getMessage());
        });
    }

    void closeRemoteStream(Canvas canvas){
        if (null == mRemoteStream) {
            return;
        }

        mRemoteStream.removeVideoRenderer(canvas,0);
        mRemoteStream.close();
    }

    private void destroyPeer(Canvas canvas) {
        closeRemoteStream(canvas);

        if (null != mLocalStream) {
            mLocalStream.removeVideoRenderer(canvas,0);
            mLocalStream.close();
        }

        if (null != mMediaConnection)	{
            mMediaConnection.on(MediaConnection.MediaEventEnum.STREAM, null);
            mMediaConnection.on(MediaConnection.MediaEventEnum.CLOSE, null);
            mMediaConnection.on(MediaConnection.MediaEventEnum.ERROR, null);

            if (mMediaConnection.isOpen()) {
                mMediaConnection.close();
            }
        }

        Navigator.terminate();

        if (null != mPeer) {
            mPeer.on(Peer.PeerEventEnum.OPEN, null);
            mPeer.on(Peer.PeerEventEnum.CONNECTION, null);
            mPeer.on(Peer.PeerEventEnum.CALL, null);
            mPeer.on(Peer.PeerEventEnum.CLOSE, null);
            mPeer.on(Peer.PeerEventEnum.DISCONNECTED, null);
            mPeer.on(Peer.PeerEventEnum.ERROR, null);

            if (!mPeer.isDisconnected()) {
                mPeer.disconnect();
            }

            if (!mPeer.isDestroyed()) {
                mPeer.destroy();
            }

            mPeer = null;
        }
    }
}