package plugin.google.maps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaResourceApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PluginFootsteps extends MyPlugin {

  private enum Animation {
    DROP,
    BOUNCE
  }

  /**
   * Create a marker
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  @SuppressWarnings("unused")
  private void createFootsteps(final JSONArray args, final CallbackContext callbackContext) throws JSONException {

    // Create an instance of Marker class
    final MarkerOptions markerOptions = new MarkerOptions();
    final JSONObject opts = args.getJSONObject(1);
    JSONArray positions = opts.getJSONArray("positions");
    final JSONArray allMarkers = new JSONArray();

    List<Marker> markers = new ArrayList<Marker>();

    for (int i = 0; i < positions.length(); i++) {

      JSONObject position = positions.getJSONObject(i);

      markerOptions.position(new LatLng(position.getDouble("lat"), position.getDouble("lng")));
      markerOptions.rotation((float)position.getDouble("rotation"));

      if (opts.has("title")) {
          markerOptions.title(opts.getString("title"));
      }

      if (opts.has("visible")) {
        if (opts.has("icon") && "".equals(opts.getString("icon")) == false) {
          markerOptions.visible(false);
        } else {
          markerOptions.visible(opts.getBoolean("visible"));
        }
      }


      if (opts.has("flat")) {
        markerOptions.flat(opts.getBoolean("flat"));
      }
      if (opts.has("opacity")) {
        markerOptions.alpha((float) opts.getDouble("opacity"));
      }
      if (opts.has("zIndex")) {
        // do nothing, API v2 has no zIndex :(
      }
      Marker marker = map.addMarker(markerOptions);

      // Store the marker
      String id = "marker_" + marker.getId();
      this.objects.put(id, marker);

      JSONObject properties = new JSONObject();
      this.objects.put("marker_property_" + marker.getId(), properties);

      // Prepare the result
      final JSONObject result = new JSONObject();
      result.put("hashCode", marker.hashCode());
      result.put("id", id);

      allMarkers.put(result);

      markers.add(marker);

    }


    // Load icon
    if (opts.has("icon")) {
      Bundle bundle = null;
      Object value = opts.get("icon");
      bundle = new Bundle();
      bundle.putString("url", (String)value);

      this.setIcon_(markers, bundle, new PluginAsyncInterface() {

        @Override
        public void onPostExecute(Object object) {
          Marker marker = (Marker)object;
          if (opts.has("visible")) {
            try {
              marker.setVisible(opts.getBoolean("visible"));
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            marker.setVisible(true);
          }

          // Animation

          callbackContext.success(allMarkers);
        }

        @Override
        public void onError(String errorMsg) {
          callbackContext.error(errorMsg);
        }

      });
    }

  }


  /**
   * Set rotation for the marker
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  @SuppressWarnings("unused")
  private void setRotation(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    float rotation = (float)args.getDouble(2);
    String id = args.getString(1);
    this.setFloat("setRotation", id, rotation, callbackContext);
  }

  /**
   * Set opacity for the marker
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  @SuppressWarnings("unused")
  private void setOpacity(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    float alpha = (float)args.getDouble(2);
    String id = args.getString(1);
    this.setFloat("setAlpha", id, alpha, callbackContext);
  }

    /**
     * Set zIndex for the marker (dummy code, not available on Android V2)
     * @param args
     * @param callbackContext
     * @throws JSONException
     */
    @SuppressWarnings("unused")
    private void setZIndex(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        // nothing to do :(
        // it's a shame google...
    }



  /**
   * Set flat for the marker
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  @SuppressWarnings("unused")
  private void setFlat(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    boolean isFlat = args.getBoolean(2);
    String id = args.getString(1);
    this.setBoolean("setFlat", id, isFlat, callbackContext);
  }

  /**
   * Set visibility for the object
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  protected void setVisible(JSONArray args, CallbackContext callbackContext) throws JSONException {
    boolean visible = args.getBoolean(2);
    String id = args.getString(1);
    this.setBoolean("setVisible", id, visible, callbackContext);
  }
  /**
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  protected void setDisableAutoPan(JSONArray args, CallbackContext callbackContext) throws JSONException {
    boolean disableAutoPan = args.getBoolean(2);
    String id = args.getString(1);
    Marker marker = this.getMarker(id);
    String propertyId = "marker_property_" + marker.getId();
    JSONObject properties = null;
    if (this.objects.containsKey(propertyId)) {
      properties = (JSONObject)this.objects.get(propertyId);
    } else {
      properties = new JSONObject();
    }
    properties.put("disableAutoPan", disableAutoPan);
    this.objects.put(propertyId, properties);
    this.sendNoResult(callbackContext);
  }

  /**
   * Return the position of the marker
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  @SuppressWarnings("unused")
  private void getPosition(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    String id = args.getString(1);
    Marker marker = this.getMarker(id);
    LatLng position = marker.getPosition();

    JSONObject result = new JSONObject();
    result.put("lat", position.latitude);
    result.put("lng", position.longitude);
    callbackContext.success(result);
  }


  /**
   * Remove the marker
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  @SuppressWarnings("unused")
  private void remove(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    String id = args.getString(1);
    Marker marker = this.getMarker(id);
    if (marker == null) {
      callbackContext.success();
      return;
    }
    marker.remove();
    this.objects.remove(id);

    String propertyId = "marker_property_" + id;
    this.objects.remove(propertyId);
    this.sendNoResult(callbackContext);
  }


  private void setIcon_(final List<Marker> markers, final Bundle iconProperty, final PluginAsyncInterface callback) {

    String iconUrl = iconProperty.getString("url");
    if (iconUrl.indexOf("://") == -1 &&
      iconUrl.startsWith("/") == false &&
      iconUrl.startsWith("www/") == false &&
      iconUrl.startsWith("data:image") == false) {
      iconUrl = "./" + iconUrl;
    }
    if (iconUrl.indexOf("./") == 0) {
      String currentPage = this.webView.getUrl();
      currentPage = currentPage.replaceAll("[^\\/]*$", "");
      iconUrl = iconUrl.replace("./", currentPage);
    }

    if (iconUrl == null) {
      callback.onPostExecute(markers);
      return;
    }


    if (iconUrl.indexOf("http") != 0) {
      //----------------------------------
      // Load icon from local file
      //----------------------------------
      AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

        @Override
        protected Bitmap doInBackground(Void... params) {
          String iconUrl = iconProperty.getString("url");

          Bitmap image = null;
          if (iconUrl.indexOf("cdvfile://") == 0) {
            CordovaResourceApi resourceApi = webView.getResourceApi();
            iconUrl = PluginUtil.getAbsolutePathFromCDVFilePath(resourceApi, iconUrl);
          }

          if (iconUrl.indexOf("data:image/") == 0 && iconUrl.indexOf(";base64,") > -1) {
            String[] tmp = iconUrl.split(",");
            image = PluginUtil.getBitmapFromBase64encodedImage(tmp[1]);
          } else if (iconUrl.indexOf("file://") == 0 &&
            iconUrl.indexOf("file:///android_asset/") == -1) {
            iconUrl = iconUrl.replace("file://", "");
            File tmp = new File(iconUrl);
            if (tmp.exists()) {
              image = BitmapFactory.decodeFile(iconUrl);
            } else {
              if (PluginFootsteps.this.mapCtrl.isDebug) {
                Log.w("GoogleMaps", "icon is not found (" + iconUrl + ")");
              }
            }
          } else {
            if (iconUrl.indexOf("file:///android_asset/") == 0) {
              iconUrl = iconUrl.replace("file:///android_asset/", "");
            }
            if (iconUrl.indexOf("./") == 0) {
              iconUrl = iconUrl.replace("./", "www/");
            }
            AssetManager assetManager = PluginFootsteps.this.cordova.getActivity().getAssets();
            InputStream inputStream;
            try {
              inputStream = assetManager.open(iconUrl);
              image = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
              Log.w("GoogleMaps", "Unable to decode bitmap stream for icon " + iconUrl + ")");
              e.printStackTrace();
              return null;
            }
          }
          if (image == null) {
            return null;
          }

          int width = 18;
          int height = 37;
          image = PluginUtil.resizeBitmap(image, width, height);
          image = PluginUtil.scaleBitmapForDevice(image);


          return image;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
          if (image == null) {
            callback.onPostExecute(markers);
            return;
          }

          try {
            //TODO: check image is valid?

            List<Marker> newMarkers = new ArrayList<Marker>();

            for(int i=0; i<markers.size(); i++) {

              Marker marker = markers.get(i);

              BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(image);
              marker.setIcon(bitmapDescriptor);

              // Save the information for the anchor property
              Bundle imageSize = new Bundle();
              imageSize.putInt("width", image.getWidth());
              imageSize.putInt("height", image.getHeight());
              PluginFootsteps.this.objects.put("imageSize", imageSize);

              newMarkers.add(marker);

            }

            callback.onPostExecute(newMarkers);

          } catch (java.lang.IllegalArgumentException e) {
            Log.e("GoogleMapsPlugin","PluginFootsteps: Warning - marker method called when marker has been disposed, wait for addMarker callback before calling more methods on the marker (setIcon etc).");
            //e.printStackTrace();

          }
        }
      };
      task.execute();


      return;
    }
  }

  private void _setIconAnchor(Marker marker, double anchorX, double anchorY, int imageWidth, int imageHeight) {
    // The `anchor` of the `icon` property
    anchorX = anchorX * this.density;
    anchorY = anchorY * this.density;
    marker.setAnchor((float)(anchorX / imageWidth), (float)(anchorY / imageHeight));
  }
  private void _setInfoWindowAnchor(Marker marker, double anchorX, double anchorY, int imageWidth, int imageHeight) {
    // The `anchor` of the `icon` property
    anchorX = anchorX * this.density;
    anchorY = anchorY * this.density;
    marker.setInfoWindowAnchor((float)(anchorX / imageWidth), (float)(anchorY / imageHeight));
  }
}
