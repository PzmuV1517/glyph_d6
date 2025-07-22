package com.example.glyph_d6;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import java.util.Random;

// Correct Glyph Matrix SDK imports (from com.nothing.ketchum, not com.nothing.glyph.matrix)
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphMatrixFrame;
import com.nothing.ketchum.GlyphMatrixObject;
import com.nothing.ketchum.GlyphToy;
import com.nothing.ketchum.Glyph;

/**
 * D6 Dice Glyph Toy Service
 *
 * This service implements a virtual 6-sided dice for the Nothing Phone(3) Glyph Matrix.
 * Features:
 * - Shows dice patterns (1-6) on the 25x25 Glyph Matrix
 * - Responds to Glyph Button long press to roll dice
 * - Supports Always-On Display (AOD)
 * - Proper service lifecycle management
 */
public class GlyphToyService extends Service {

    private static final String TAG = "GlyphToyService";

    private GlyphMatrixManager mGM;
    private GlyphMatrixManager.Callback mCallback;

    private Random random = new Random();
    private int currentDiceValue = 1;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound - initializing D6 dice toy");
        init();
        return serviceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service unbound - cleaning up D6 dice toy");
        cleanup();
        return false;
    }

    private void init() {
        Log.d(TAG, "Initializing Glyph Matrix connection...");

        mCallback = new GlyphMatrixManager.Callback() {
            @Override
            public void onServiceConnected(ComponentName name) {
                Log.d(TAG, "Glyph Matrix service connected");
                mGM.register(Glyph.DEVICE_23112);
                displayDice(currentDiceValue);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "Glyph Matrix service disconnected");
            }
        };

        try {
            // Try different initialization approaches
            mGM = GlyphMatrixManager.getInstance(this);
        } catch (Exception e) {
            Log.e(TAG, "Could not initialize GlyphMatrixManager", e);
            return;
        }

        if (mGM != null) {
            mGM.init(mCallback);
        }
    }

    private void cleanup() {
        Log.d(TAG, "Cleaning up Glyph Matrix resources");

        if (mGM != null) {
            mGM.unInit();
            mGM = null;
        }
        mCallback = null;
    }

    private final Handler serviceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Received message: " + msg.what);

            switch (msg.what) {
                case GlyphToy.MSG_GLYPH_TOY: {
                    Bundle bundle = msg.getData();
                    String event = bundle.getString(GlyphToy.MSG_GLYPH_TOY_DATA);

                    if (GlyphToy.EVENT_CHANGE.equals(event)) {
                        Log.d(TAG, "Glyph Button long press - rolling dice");
                        rollDice();
                    } else if (GlyphToy.EVENT_AOD.equals(event)) {
                        Log.d(TAG, "AOD update - refreshing display");
                        displayDice(currentDiceValue);
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private final Messenger serviceMessenger = new Messenger(serviceHandler);

    private void rollDice() {
        // Generate random number between 1 and 6
        int newValue = random.nextInt(6) + 1;
        currentDiceValue = newValue;

        Log.d(TAG, "Dice rolled: " + currentDiceValue);
        displayDice(currentDiceValue);
    }

    private void displayDice(int value) {
        Log.d(TAG, "Displaying dice value: " + value);

        if (mGM == null) {
            Log.w(TAG, "GlyphMatrixManager is null, cannot display dice");
            return;
        }

        // Create the dice bitmap
        Bitmap diceBitmap = createDiceBitmap(value);

        try {
            // Create Glyph Matrix object - your sprites are 25x25, so no positioning needed
            GlyphMatrixObject.Builder diceBuilder = new GlyphMatrixObject.Builder();
            GlyphMatrixObject diceObject = diceBuilder
                    .setImageSource(diceBitmap)
                    .setScale(100)
                    .setOrientation(0)
                    .setPosition(0, 0) // No offset needed - sprites are full 25x25 size
                    .setBrightness(255)
                    .build();

            // Create frame and display
            GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder();
            GlyphMatrixFrame frame = frameBuilder.addTop(diceObject).build(this);

            mGM.setMatrixFrame(frame.render());
            Log.d(TAG, "Dice pattern sent to Glyph Matrix");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying dice on Glyph Matrix", e);
        }
    }

    /**
     * Creates a bitmap from your custom 25x25 sprite data for the given dice value
     */
    private Bitmap createDiceBitmap(int value) {
        // Get your custom sprite data (25x25 = 625 elements)
        int[] spriteData = DiceSprites.getDiceSprite(value);

        int size = 25; // 25x25 sprites
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        // Convert your sprite array to bitmap pixels
        int[] pixels = new int[size * size];
        for (int i = 0; i < spriteData.length; i++) {
            if (spriteData[i] == 255) {
                pixels[i] = Color.WHITE; // White pixel for 255 values
            } else {
                pixels[i] = Color.TRANSPARENT; // Transparent for 0 values
            }
        }

        bitmap.setPixels(pixels, 0, size, 0, 0, size, size);

        Log.d(TAG, "Created custom dice sprite for value: " + value + " (25x25)");
        return bitmap;
    }
}
