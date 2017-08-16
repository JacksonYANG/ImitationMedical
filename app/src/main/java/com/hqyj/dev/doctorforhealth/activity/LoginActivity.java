package com.hqyj.dev.doctorforhealth.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.CustomTools.WaitingDialog;
import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.BoardConfig;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * 医师登陆界面，授权的医师用户名及密码后，可登录到系统几界面
 */
public class LoginActivity extends Activity implements CallBack{
	private String TAG = "LoginActivity";
	private CheckBox rememberPass;
	private Button login;
	private EditText passwd,name;
	private boolean isremember = false;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private boolean isLogin = false;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Log.d("LoginActivity", "Device info : ");
		String devInfo = BoardConfig.readDeviceInfo();
		Log.d("LoginActivity", "Device info : "+ devInfo);
		if(devInfo == null || !devInfo.equals("www.farsight.com.cn")){
			Toast.makeText(LoginActivity.this, "www.farsight.com.cn", Toast.LENGTH_SHORT).show();
			return;
		}
		setContentView(R.layout.activity_login_1);
		ConnectivityManager connect = (ConnectivityManager) LoginActivity.this.getSystemService(CONNECTIVITY_SERVICE);
		final NetworkInfo info = connect.getActiveNetworkInfo();
		if(info == null){//检查是否有网络
			Toast.makeText(LoginActivity.this, "网络没有连接", Toast.LENGTH_SHORT).show();
		}
		
		name = (EditText) findViewById(R.id.doctorName);
		passwd = (EditText) findViewById(R.id.passwd);
		login = (Button) findViewById(R.id.login);
		rememberPass = (CheckBox) findViewById(R.id.rememberPasswd);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		isremember = pref.getBoolean("remember_pass", false);
		if(isremember){//机制账号和密码
			String account = pref.getString("account","");
			String password = pref.getString("passwd", "");
			name.setText(account);
			passwd.setText(password);
			rememberPass.setChecked(true);
		}
		// 登录按钮点击时，开启服务访问服务器进行验证
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				progressDialog = new ProgressDialog(LoginActivity.this);
				progressDialog.setTitle("正在登陆");
				progressDialog.setMessage("Loading...");
				progressDialog.setCancelable(true);
				progressDialog.show();

				String account = name.getText().toString();
				String password = passwd.getText().toString();
				Map<String, String> values = null;
				if (info != null) {
					if (!(account.equals("") || password.equals(""))) {
						editor = pref.edit();
						values = new HashMap<String, String>();
						values.put("name", account);
						values.put("pwd", password);
						if (rememberPass.isChecked()) {
							editor.putBoolean("remember_pass", true);
							editor.putString("account", account);
							editor.putString("passwd", password);
						} else {
							editor.clear();
						}
						editor.commit();
						Task loginTask = new Task(LoginActivity.this, Task.NET_WEB_SERVICE_GET_DATA, new Object[]{"checkPasswdUser", values});
						GetWebInfoService.newTask(loginTask);

					} else {
						Toast.makeText(LoginActivity.this, "账号或者密码输入不能为空", Toast.LENGTH_SHORT).show();
					}
				} else {

					Toast.makeText(LoginActivity.this, "没有联网，登录失败", Toast.LENGTH_SHORT).show();
				}
			}
		});

		
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d("LoginActivity", "Device info : 1");
		//开启服务
		Intent intent  = new Intent(this, GetWebInfoService.class);
		startService(intent);
		Log.d("LoginActivity", "Device info : 2");
	}
	//回调，通过服务处理后的数据在这里进行处理
	@Override
	public void DataHandler(Task task) {
		if(task == null){
			Log.d("LoginActivity", "task == null");
			return;
		}

		if(task.result == null && task.isTimeOut){
			handler.sendEmptyMessage(1);
		}
		JsonCommand.Doctor doctor;
		String json = (String) task.result;
		Log.d("LoginActivity", "json:" + json);
		if(json != null && (doctor = JsonCommand.getDoctorObject(json))!= null){
			if(doctor.code == 0){
				Log.d(TAG,"login");
				SharedPreferenceDB.setLoginUser(LoginActivity.this, doctor);
				stopService(new Intent(LoginActivity.this, GetWebInfoService.class));
				startActivity(new Intent(LoginActivity.this, BluetoothConnectActivity.class));
				isLogin = true;
				progressDialog.dismiss();
			}else{
				Message msg = new Message();
				msg.what = 2;
				msg.obj = doctor.errorStr;
				handler.sendMessage(msg);
			}
		}else{
			Message msg = new Message();
			msg.what = 2;
			msg.obj = "服务器访问失败";
			handler.sendMessage(msg);
		}

	}
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
				case 1:
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this,"连接超时",Toast.LENGTH_SHORT).show();

					break;
				case 2:
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();

					break;
			}
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(LoginActivity.this,GetWebInfoService.class));
	}
}























