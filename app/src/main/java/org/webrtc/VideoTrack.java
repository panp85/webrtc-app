/*
 *  Copyright 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import java.util.IdentityHashMap;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

/** Java version of VideoTrackInterface. */
public class VideoTrack extends MediaStreamTrack {
  private static final String TAG = "VideoTrack";

  private final List<VideoRenderer> renderers = new ArrayList<>();
  private final IdentityHashMap<VideoSink, Long> sinks = new IdentityHashMap<VideoSink, Long>();

  public VideoTrack(long nativeTrack) {
    super(nativeTrack);
  }

  /**
   * Adds a VideoSink to the track.
   *
   * A track can have any number of VideoSinks. VideoSinks will replace
   * renderers. However, converting old style texture frames will involve costly
   * conversion to I420 so it is not recommended to upgrade before all your
   * sources produce VideoFrames.
   */
  public void addSink(VideoSink sink) {
    final long nativeSink = nativeWrapSink(sink);
    sinks.put(sink, nativeSink);
   	Log.i(TAG, "ppt, in addSink, go to nativeAddSink");
    nativeAddSink(nativeTrack, nativeSink);
  }

  /**
   * Removes a VideoSink from the track.
   *
   * If the VideoSink was not attached to the track, this is a no-op.
   */
  public void removeSink(VideoSink sink) {
    final long nativeSink = sinks.remove(sink);
    if (nativeSink != 0) {
      nativeRemoveSink(nativeTrack, nativeSink);
      nativeFreeSink(nativeSink);
    }
  }

  public void addRenderer(VideoRenderer renderer) {
    renderers.add(renderer);
	Log.i(TAG, "ppt, in addRenderer, go to nativeAddSink");
    nativeAddSink(nativeTrack, renderer.nativeVideoRenderer);
  }

  public void removeRenderer(VideoRenderer renderer) {
    if (!renderers.remove(renderer)) {
      return;
    }
    nativeRemoveSink(nativeTrack, renderer.nativeVideoRenderer);
    renderer.dispose();
  }

  @Override
  public void dispose() {
    for (VideoRenderer renderer : renderers) {
      nativeRemoveSink(nativeTrack, renderer.nativeVideoRenderer);
      renderer.dispose();
    }
    renderers.clear();
    for (long nativeSink : sinks.values()) {
      nativeRemoveSink(nativeTrack, nativeSink);
      nativeFreeSink(nativeSink);
    }
    sinks.clear();
    super.dispose();
  }

  private static native void nativeAddSink(long track, long nativeSink);
  private static native void nativeRemoveSink(long track, long nativeSink);
  private static native long nativeWrapSink(VideoSink sink);
  private static native void nativeFreeSink(long sink);
}
