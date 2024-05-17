// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.android.globalactionbarservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.media.AudioManager;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayDeque;
import java.util.Deque;


import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class GlobalActionBarService extends AccessibilityService {
    FrameLayout mLayout;
    private View cursorView;

    private int lastX = 0, lastY=0, firstX=0, firstY=0;


    @Override
    protected void onServiceConnected() {
        // Create an overlay and display the action bar
        Log.d("DANX", "onServiceConnected");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);
        cursorView=(View) mLayout.findViewById(R.id.trackCursor);
        View trackPad = (View) mLayout.findViewById(R.id.trackpad);
        trackPad.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        firstX=lastX= (int) event.getX();
                        firstY=lastY= (int) event.getY();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        moveCursor((int)event.getX(), (int)event.getY());
                        break;
                    }
                    case MotionEvent.ACTION_UP:{
                        int dist = (int)(Math.pow(firstX-lastX,2)+Math.pow(firstY-lastY,2));
                        Log.d("danx", "click "+dist);
                        if(dist<20){
                            //click
                            createClick(cursorView.getX(), cursorView.getY());
                        }
                        break;
                    }
                }
                Log.d("DANX", "Touch trackpad coordinates : " +
                        String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()));
                return true;
            }
        });
        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("DANX", "Touch layout coordinates : " +
                        String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()));
                return false;
            }
        });
    }

    private void createClick(float x, float y) {

        GestureResultCallback callback = new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d("danx", "gesture completed");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d("danx", "gesture cancelled");
            }
        };

        Log.d("danx", "click "+x+", "+y);
        // for a single tap a duration of 1 ms is enough
        final int DURATION = 1;

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        this.dispatchGesture(clickBuilder.build(), callback, null);
    }

    private void moveCursor(int newX, int newY){
        try {
            int dx = lastX - newX, dy = lastY - newY;
            cursorView.setX(cursorView.getX()-dx);
            cursorView.setY(cursorView.getY()-dy);
            lastX = newX;
            lastY = newY;
        }
        catch(Exception e){
            Log.e("DANX", "moveCursor Exception" + e.getMessage());
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("DANX", "onAccessibilityEvent");

    }

    @Override
    public void onInterrupt() {
        Log.d("DANX", "onInterrupt");

    }


}
