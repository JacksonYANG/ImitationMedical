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
 * 医师登陆界面，授权的医师用户名及密码后，可登录到系统机界面
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);//设置界面显示无标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设定窗口全屏
		Log.d("LoginActivity", "Device info : ");
		String devInfo = BoardConfig.readDeviceInfo();
		Log.d("LoginActivity", "Device info : "+ devInfo);
		setContentView(R.layout.activity_login_1);//设置UI
		ConnectivityManager connect = (ConnectivityManager) LoginActivity.this.getSystemService(CONNECTIVITY_SERVICE);//主要管理与网络连接相关的操作
		final NetworkInfo info = connect.getActiveNetworkInfo();//获取当前网络的连接状态
		if(info == null){//检查是否有网络
			Toast.makeText(LoginActivity.this, "网络没有连接", Toast.LENGTH_SHORT).show();
		}
		
		name = (EditText) findViewById(R.id.doctorName);
		passwd = (EditText) findViewById(R.id.passwd);
		login = (Button) findViewById(R.id.login);
		rememberPass = (CheckBox) findViewById(R.id.rememberPasswd);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		isremember = pref.getBoolean("remember_pass", false);//设置默认没有记住密码
		if(isremember){//已经记住账号和密码
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
				Map<String, String> values = null;//设置匹配地图
				if (info != null) {
					if (!(account.equals("") || password.equals(""))) {
						editor = pref.edit();
						values = new HashMap<String, String>();
						values.put("name", account);
						values.put("pwd", password);
						if (rememberPass.isChecked()) {
							//选择记住用户名和密码加入到SharePreference
							editor.putBoolean("remember_pass", true);
							editor.putString("account", account);
							editor.putString("passwd", password);
						} else {
							editor.clear();
						}
						editor.commit();
						//在服务中提交用户名和密码并且进行MAP匹配
						Task loginTask = new Task(LoginActivity.this, Task.NET_WEB_SERVICE_GET_DATA, new Object[]{"checkPasswdUser", values});
						GetWebInfoService.newTask(loginTask);

					} else {
						Toast.makeText(LoginActivity.this, "账号或者密码输入不能为空", Toast.LENGTH_SHORT).show();
					}
				} else {

					Toast.makeText(LoginActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
				}
			}
		});

		
	}

	@Override
	protected void onStart() {
		//在生命周期中只要打开软件即启动Task服务
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
			handler.sendEmptyMessage(1);//返回1号错误的空信息
		}
		JsonCommand.Doctor doctor;
		String json = (String) task.result;//处理返回的JSON格式数据
		Log.d("LoginActivity", "json格式数据:" + json);
		if(json != null && (doctor = JsonCommand.getDoctorObject(json))!= null){
			if(doctor.code == 0){
				//若Json.code返回为0，则登陆成功
				Log.d(TAG,"login");
				SharedPreferenceDB.setLoginUser(LoginActivity.this, doctor);
				stopService(new Intent(LoginActivity.this, GetWebInfoService.class));
				startActivity(new Intent(LoginActivity.this, BluetoothConnectActivity.class));
				isLogin = true;
				progressDialog.dismiss();
			}else{
				Message msg = new Message();
				msg.what = 2;
				msg.obj = doctor.errorStr;//返回2号错误信息
				handler.sendMessage(msg);
			}
		}else{
			Message msg = new Message();
			msg.what = 2;
			msg.obj = "服务器访问失败";//返回服务器访问失败的错误信息
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























