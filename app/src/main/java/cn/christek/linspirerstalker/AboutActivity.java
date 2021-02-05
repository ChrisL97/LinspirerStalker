package cn.christek.linspirerstalker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

public class AboutActivity extends AlertActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertController.AlertParams p = this.mAlertParams;
        p.mTitle = getResources().getText(R.string.about);
        View view = View.inflate(this,R.layout.dialog_about,null);
        p.mView = view;
        ((TextView)view.findViewById(R.id.app_version)).setText(getAppVersionName());
        p.mPositiveButtonText = getResources().getText(R.string.confirm);
        setupAlert();
    }

    public String getAppVersionName() {
        String versionName ="";
        try {
            PackageManager pm = getPackageManager();
            PackageInfo p1 = pm.getPackageInfo(getPackageName(), 0);
            versionName = p1.versionName;
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
