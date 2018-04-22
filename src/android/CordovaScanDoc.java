package cordova.plugin.scandoc;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaScanDoc extends CordovaPlugin {
    private static int RESULT_LOAD_IMAGE = 1;
    private CallbackContext callback = null;
    private OpenCvHelper openCvHelper;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callback = callbackContext;
        this.openCvHelper = new OpenCvHelper(this.cordova.getContext());

        //Permission
        PermissionHelper.requestPermissions(this.cordova.getContext(), this.cordova.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA);

        if ("coolMethod".equals(action)) {
            String message = args.getString(0);
            this.coolMethod(message, this.callback);
            return true;
        } else if ("scanDoc".equals(action)) {
            this.scanDoc(this.callback);
            // Send no result, to execute the callbacks later
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void scanDoc(CallbackContext callbackContext) {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        this.cordova.startActivityForResult((CordovaPlugin) this, i, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == this.cordova.getActivity().RESULT_OK && null != data) {
            //get bitmap
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = this.cordova.getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            Mat mat = openCvHelper.getMatFromBitmap(bitmap);
            MatOfPoint2f edge = openCvHelper.findEdge(mat);
            Rect quad = openCvHelper.findQuadEdge(edge);

            //return result
            //String content = "x: " + quad.x + " - y:" + quad.y + " - width:" + quad.width + " - height: " + quad.height;
            JSONObject jso = new JSONObject();
            try {
                jso.put("x", quad.x);
                jso.put("y", quad.y);
                jso.put("width", quad.width);
                jso.put("height", quad.height);
                jso.put("picturePath", picturePath);
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK, jso);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
            return;
        }

        // Handle other results if exists.
        super.onActivityResult(requestCode, resultCode, data);
    }
}
