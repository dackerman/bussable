package com.dacklabs.bustracker.activity;

import android.os.Bundle;

interface ActivityLifecycle {
    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc. This method also provides you with a
     * Bundle containing the activity's previously frozen state, if there was one. Always followed
     * by onStart().
     * <p>
     * next: onStart
     *
     * @param savedInstanceState
     */
    void onCreate(Bundle savedInstanceState);

    /**
     * Called when the activity is becoming visible to the user. Followed by onResume() if the
     * activity comes to the foreground, or onStop() if it becomes hidden.
     * <p>
     * next: onResume
     */
    void onStart();

    /**
     * Called when the activity will start interacting with the user. At this point your activity is
     * at the top of the activity stack, with user input going to it. Always followed by onPause().
     * <p>
     * next: onPause
     */
    void onResume();

    /**
     * Called when the system is about to start resuming a previous activity. This is typically used
     * to commit unsaved changes to persistent data, stop animations and other things that may be
     * consuming CPU, etc. Implementations of this method must be very quick because the next
     * activity will not be resumed until this method returns.
     * <p>
     * Followed by either onResume() if the activity returns back to the front, or onStop() if it
     * becomes invisible to the user.
     * <p>
     * next: onResume or onStop
     */
    void onPause();

    /**
     * Called when the activity is no longer visible to the user, because another activity has been
     * resumed and is covering this one. This may happen either because a new activity is being
     * started, an existing one is being brought in front of this one, or this one is being
     * destroyed.
     * <p>
     * Followed by either onRestart() if this activity is coming back to interact with the user, or
     * onDestroy() if this activity is going away.
     * <p>
     * <p>
     * next: KILL, onRestart, or onDestroy
     */
    void onStop();

    /**
     * Called after your activity has been stopped, prior to it being started again. Always followed
     * by onStart()
     * <p>
     * next: onStart
     */
    void onRestart();

    /**
     * The final call you receive before your activity is destroyed. This can happen either because
     * the activity is finishing (someone called finish() on it, or because the system is
     * temporarily destroying this instance of the activity to save space. You can distinguish
     * between these two scenarios with the isFinishing() method.
     *
     * <p>
     * next: KILL
     */
    void onDestroy();

    void onSaveInstanceState(Bundle outState);

    void onLowMemory();
}
