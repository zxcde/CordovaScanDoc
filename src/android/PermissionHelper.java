package cordova.plugin.scandoc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public static void requestPermissions(Context context, Activity activity, String... permissions) {
        List<String> notGrantedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermissions.add(permission);
            }
        }

        if (notGrantedPermissions.size() > 0) ActivityCompat
                .requestPermissions(activity, notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]), REQUEST_CODE_ASK_PERMISSIONS);
    }
}
