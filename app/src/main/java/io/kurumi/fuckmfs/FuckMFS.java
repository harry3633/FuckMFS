package io.kurumi.fuckmfs;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import com.sollyu.android.appenv.helper.PhoneHelper;
import com.sollyu.android.appenv.helper.RandomHelper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONObject;
import android.telephony.TelephonyManager;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import java.net.NetworkInterface;
import android.net.wifi.WifiManager;
import java.io.IOException;

public class FuckMFS implements IXposedHookLoadPackage {

    public static ClassLoader loader;

	XC_MethodReplacement METHOD_EMPTY = new XC_MethodReplacement() {

		@Override
		protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

			return null;

		}

	};

	final String[] whitePrefix = new String[] {

		"com.android",
		"com.xiaomi",
		"com.tencent",
		"com.sina",
		"com.baidu",
		"com.zhihu",
		"com.alibaba",
		"com.xunlei",
		"com.uc",
		"com.douban",
		"com.renren",
		"com.coolapk",
		"com.microsoft",
		"cn.wps",
		"tv.danmaku",
		"com.sohu",
		"com.cainiao",
		"com.qualcomm"

	};

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam pkg) throws Throwable {

		loader = pkg.classLoader;

		if ("com.Android.MFSocket".equals(pkg.packageName)) {

			try {

				Class.forName("com.Android.MFSocket.SystemMsg",false,pkg.classLoader);

				// 阻止SHELL执行

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.ShellUtils",false,pkg.classLoader),
						"execCommand",
						String[].class,boolean.class,boolean.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

								return Class
									.forName("com.Android.MFSocket.ShellUtils$CommandResult")
									.getConstructor(int.class,String.class,String.class)
									.newInstance(-1,null,null);

							}

						});

				} catch (ClassNotFoundException e) {}



				// 阻止读取联系人

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.ContactsMsgOS20",false,pkg.classLoader),
						"getContactOS20",
						BufferedOutputStream.class,StringBuffer.class,int.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								BufferedOutputStream out = (BufferedOutputStream) params.args[0];
								StringBuffer contatMsg = (StringBuffer) params.args[1];
								int oneTimeCount = (int)params.args[2];

								int contactConts = RandomHelper.getInstance().randomInt(1,11);

								StringBuffer count = new StringBuffer();

								count.append(contactConts);

								sendData(out,count.toString());

								int nSendCount = 0;

								for (int index = 1;index < contactConts;index ++) {

									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(2,false,false,true),contatMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomTelephonyGetLine1Number(),contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendItem("",contatMsg);
									RecordPackage.AppendRecord(contatMsg);


									if (nSendCount % oneTimeCount == 0) {

										sendData(out,contatMsg.toString());
										contatMsg.setLength(0);

									}

								}

								sendData(out,contatMsg.toString());
								contatMsg.setLength(0);


								return null;
							}

						});

				} catch (ClassNotFoundException e) {}

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.ContactsSIM",false,pkg.classLoader),
						"getContact",
						BufferedOutputStream.class,StringBuffer.class,int.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								BufferedOutputStream out = (BufferedOutputStream) params.args[0];
								StringBuffer contatMsg = (StringBuffer) params.args[1];
								int oneTimeCount = (int)params.args[2];

								int simCount = RandomHelper.getInstance().randomInt(1,3);

								int nSendCount = 0;

								StringBuffer count = new StringBuffer();

								count.append(simCount);

								sendData(out,count.toString());


								String phonename = RandomHelper.getInstance().randomString(2,true,true,true);
								String phoneNo = RandomHelper.getInstance().randomTelephonyGetLine1Number();

								Log.i("PhoneContact","name: " + phonename + " phone: " + phoneNo);

								RecordPackage.AppendItem(phonename,contatMsg);
								RecordPackage.AppendItem(phoneNo,contatMsg);
								RecordPackage.AppendRecord(contatMsg);

								nSendCount++;

								if (nSendCount % oneTimeCount == 0) {

									sendData(out,contatMsg.toString());

									contatMsg.setLength(0);

								}


								sendData(out,contatMsg.toString());
								contatMsg.setLength(0);

								return null;

							}
						});

				} catch (ClassNotFoundException e) {}

				// 应用信息

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.AppMsg",false,pkg.classLoader),
						"GetAppMsg",
						StringBuffer.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								StringBuffer appMsg = (StringBuffer) params.args[0];

								Context mContext = (Context) XposedHelpers.getObjectField(params.thisObject,"mContext");

								PackageManager pm = mContext.getPackageManager();
								List<PackageInfo> packages = pm.getInstalledPackages(12288);
								int packageSize = packages.size();

								next : for (int i = 0; i < packageSize; i++) {

									PackageInfo packageInfo = (PackageInfo) packages.get(i);

									String appType = "system";

									if ((packageInfo.applicationInfo.flags & 1) == 0) {

										appType = "user";


									}

									String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
									String packageName = packageInfo.packageName;

									loop : while (true) {

										for (String prefix : whitePrefix) {

											if (packageName.startsWith(prefix)) {

												break loop;

											}

										}

										continue next;

									}

									String versionName = packageInfo.versionName;
									String appDir = packageInfo.applicationInfo.publicSourceDir;
									String strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() - ((long)RandomHelper.getInstance().randomInt(30,2000) * 24 * 60 * 1000)));
									String sig = "";
									String md5 = "";

									StringBuffer strPermissionsBuffer = new StringBuffer();
									try {
										String[] sharedPkgList = pm.getPackageInfo(packageName,4096).requestedPermissions;
										if (sharedPkgList != null) {
											for (String append : sharedPkgList) {
												strPermissionsBuffer.append(append);
												strPermissionsBuffer.append(",");
											}
										}
									} catch (Exception e2) {
									}


									RecordPackage.AppendItem(appName,appMsg);
									RecordPackage.AppendItem(packageName,appMsg);
									RecordPackage.AppendItem(versionName,appMsg);
									RecordPackage.AppendItem(appType,appMsg);
									RecordPackage.AppendItem(0,appMsg);
									RecordPackage.AppendItem(strDate,appMsg);
									RecordPackage.AppendItem(appDir,appMsg);
									RecordPackage.AppendItem(strPermissionsBuffer.toString(),appMsg);
									RecordPackage.AppendItem(sig,appMsg);
									RecordPackage.AppendItem(md5,appMsg);
									RecordPackage.AppendRecord(appMsg);

								}

								return null;
							}

						});

				} catch (ClassNotFoundException e) {}

				// 音频 / 视频

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.AudioMsg",false,pkg.classLoader),
						"getAudio",
						BufferedOutputStream.class,StringBuffer.class,int.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								BufferedOutputStream out = (BufferedOutputStream) params.args[0];
								StringBuffer audioMsg = (StringBuffer) params.args[1];
								int nConfig = (int)params.args[2];

								boolean bNeedMD5 = false;

								switch (nConfig) {
									case 0:
										bNeedMD5 = false;
										break;
									case 1:
										bNeedMD5 = true;
										break;
									case 2:
										bNeedMD5 = false;
										break;
									case 3:
										bNeedMD5 = true;
										break;
								}

								int count = RandomHelper.getInstance().randomInt(9,114);

								sendData(out,((Integer)count).toString());


								for (int index = 0;index < count;index ++) {

									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(11,45),true,true,true) + ".mp3",audioMsg);
									RecordPackage.AppendItem("",audioMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(2,5),true,true,false),audioMsg);
									RecordPackage.AppendItem("file:///dev/null",audioMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomInt(1 * 100,30 * 60 * 1000),audioMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomInt(1 * 1024,30 * 1024),audioMsg);
									RecordPackage.AppendItem("audio/mp3",audioMsg);
									RecordPackage.AppendItem(((Long)System.currentTimeMillis() - ((long)RandomHelper.getInstance().randomInt(10,30 * 24) * 24 * 60 * 1000)),audioMsg);
									RecordPackage.AppendItem(bNeedMD5 ? RandomHelper.getInstance().randomString(16,true,false,true) : "",audioMsg);
									RecordPackage.AppendRecord(audioMsg);

									sendData(out,audioMsg.toString());
									audioMsg.setLength(0);

								}

								sendData(out,audioMsg.toString());
								audioMsg.setLength(0);

								return null;
							}

						});

				} catch (ClassNotFoundException e) {}

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.VideoMsg",false,pkg.classLoader),
						"getVideo",
						BufferedOutputStream.class,StringBuffer.class,int.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								BufferedOutputStream out = (BufferedOutputStream) params.args[0];
								StringBuffer videoMsg = (StringBuffer) params.args[1];
								int nConfig = (int)params.args[2];

								boolean bNeedMD5 = false;

								switch (nConfig) {
									case 0:
										bNeedMD5 = false;
										break;
									case 1:
										bNeedMD5 = true;
										break;
									case 2:
										bNeedMD5 = false;
										break;
									case 3:
										bNeedMD5 = true;
										break;
								}

								int count = RandomHelper.getInstance().randomInt(9,114);

								sendData(out,((Integer)count).toString());


								for (int index = 0;index < count;index ++) {

									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(11,45),true,true,true) + ".mp4",videoMsg);
									RecordPackage.AppendItem("",videoMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(2,5),true,true,false),videoMsg);
									RecordPackage.AppendItem("file:///dev/null",videoMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomInt(1 * 100,30 * 60 * 1000),videoMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomInt(1 * 1024,30 * 1024),videoMsg);
									RecordPackage.AppendItem("video/mp4",videoMsg);
									RecordPackage.AppendItem(((Long)System.currentTimeMillis() - ((long)RandomHelper.getInstance().randomInt(10,30 * 24) * 24 * 60 * 1000)),videoMsg);
									RecordPackage.AppendItem(bNeedMD5 ? RandomHelper.getInstance().randomString(16,true,false,true) : "",videoMsg);
									RecordPackage.AppendRecord(videoMsg);

									sendData(out,videoMsg.toString());
									videoMsg.setLength(0);

								}

								sendData(out,videoMsg.toString());
								videoMsg.setLength(0);

								return null;
							}

						});

				} catch (ClassNotFoundException e) {}


				// 蓝牙

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.BtMsg",false,pkg.classLoader),
						"getBtInfo",
						StringBuffer.class,
						METHOD_EMPTY);

				} catch (ClassNotFoundException e) {}

				// 此处是取得已配对设备 MAC等在下方systeminfo 可忽略

				// 定位

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.GpsMsg",false,pkg.classLoader),
						"getGpsInfo",
						StringBuffer.class,
						METHOD_EMPTY);

				} catch (ClassNotFoundException e) {}

				// 位置信息 可忽略

				// 相册

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.ImageMsg",false,pkg.classLoader),
						"getImage",
						BufferedOutputStream.class,StringBuffer.class,int.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								BufferedOutputStream out = (BufferedOutputStream) params.args[0];
								StringBuffer imageMsg = (StringBuffer) params.args[1];
								int nConfig = (int)params.args[2];

								boolean bNeedMD5 = false;

								switch (nConfig) {
									case 0:
										bNeedMD5 = false;
										break;
									case 1:
										bNeedMD5 = true;
										break;
									case 2:
										bNeedMD5 = false;
										break;
									case 3:
										bNeedMD5 = true;
										break;
								}

								int count = RandomHelper.getInstance().randomInt(9,114);

								sendData(out,((Integer)count).toString());

								for (int index = 0;index < count;index ++) {

									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(11,45),true,true,true) + ".png",imageMsg);
									RecordPackage.AppendItem("",imageMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(2,5),true,true,false),imageMsg);
									RecordPackage.AppendItem("file:///dev/null",imageMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomInt(1 * 100,30 * 60 * 1000),imageMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomInt(1 * 1024,30 * 1024),imageMsg);
									RecordPackage.AppendItem("image/png",imageMsg);
									RecordPackage.AppendItem(((Long)System.currentTimeMillis() - ((long)RandomHelper.getInstance().randomInt(10,30 * 24) * 24 * 60 * 1000)),imageMsg);
									RecordPackage.AppendItem(bNeedMD5 ? RandomHelper.getInstance().randomString(16,true,false,true) : "",imageMsg);
									RecordPackage.AppendRecord(imageMsg);

									sendData(out,imageMsg.toString());
									imageMsg.setLength(0);

								}

								sendData(out,imageMsg.toString());
								imageMsg.setLength(0);

								return null;
							}

						});

				} catch (ClassNotFoundException e) {}


				// 短信

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.SmsMsg",false,pkg.classLoader),
						"getSms",
						BufferedOutputStream.class,StringBuffer.class,int.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								BufferedOutputStream out = (BufferedOutputStream) params.args[0];
								StringBuffer smsMsg = (StringBuffer) params.args[1];
								int oneTimeCount = (int)params.args[2];

								ContentResolver mResolver = (ContentResolver) XposedHelpers.getObjectField(params.thisObject,"mResolver");

								if (mResolver != null) {
									Cursor cur;
									String[] smsBox = new String[]{"content://sms/inbox", "content://sms/sent", "content://sms/draft", "content://sms/outbox", "content://sms/failed", "content://sms/queued"};
									String[] projection = new String[]{"address", "person", "date", "protocol", "read", "type", "subject", "body"};
									int smsCount = 0;
									for (int i = 0; i < 6; i++) {
										cur = mResolver.query(Uri.parse(smsBox[i]),projection,null,null,"date desc");
										smsCount += cur.getCount();
										cur.close();
									}
									StringBuffer count = new StringBuffer();


									count.append(smsCount);

									sendData(out,count.toString());

									for (int j = 0; j < 6; j++) {
										Uri uri = Uri.parse(smsBox[j]);
										cur = mResolver.query(uri,projection,null,null,"date desc");

										if (cur.moveToFirst()) {
											int nameIdx = cur.getColumnIndexOrThrow("person");
											int addressIdx = cur.getColumnIndexOrThrow("address");
											int dateIdx = cur.getColumnIndexOrThrow("date");
											int prtclIdx = cur.getColumnIndexOrThrow("protocol");
											int readIdx = cur.getColumnIndexOrThrow("read");
											int subjectIdx = cur.getColumnIndexOrThrow("subject");
											int bodyIdx = cur.getColumnIndexOrThrow("body");
											int typeIdx = cur.getColumnIndexOrThrow("type");
											do {
												String person = cur.getString(nameIdx);
												String address = cur.getString(addressIdx);
												long date = cur.getLong(dateIdx);
												String subject = cur.getString(subjectIdx);
												String body = cur.getString(bodyIdx);
												int type = cur.getInt(typeIdx);
												int read = cur.getInt(readIdx);
												int protocol = cur.getInt(prtclIdx);

												if (!address.startsWith("1069")) {

													address = RandomHelper.getInstance().randomTelephonyGetLine1Number();

													body = RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(9,114),true,true,true);

												}

												RecordPackage.AppendItem(person,smsMsg);
												RecordPackage.AppendItem(address,smsMsg);
												RecordPackage.AppendItem(date,smsMsg);
												RecordPackage.AppendItem(subject,smsMsg);
												RecordPackage.AppendItem(body,smsMsg);
												RecordPackage.AppendItem(type,smsMsg);
												RecordPackage.AppendItem(read,smsMsg);
												RecordPackage.AppendItem(protocol,smsMsg);
												RecordPackage.AppendRecord(smsMsg);
												if (0 % oneTimeCount == 0) {
													sendData(out,smsMsg.toString());
													smsMsg.setLength(0);
												}
											} while (cur.moveToNext());
										}
										sendData(out,smsMsg.toString());
										smsMsg.setLength(0);
										cur.close();


										cur.close();
									}

								}

								return null;
							}
						});

				} catch (ClassNotFoundException e) {}

				// 文件Hash

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.MD5Msg",false,pkg.classLoader),
						"fileMD5",
						String.class,
						new XC_MethodReplacement() {

							@Override
							protected String replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

								return "1145141919810FDB1471EF51EC3A32CD";

							}
						});

				} catch (ClassNotFoundException e) {}



				String[] networkOperator = new String[] {

					"46000","46002", // 移动
					"46001", // 联通
					"46003" , // 电信

				};

				final String no = networkOperator[RandomHelper.getInstance().randomInt(0,4)];

				try {

					StringBuilder json = new StringBuilder();

					BufferedReader reader = new BufferedReader(new InputStreamReader(pkg.classLoader.getResourceAsStream("assets/phone.json")));

					String line;

					while ((line = reader.readLine()) != null) {

						json.append(line + "\n");

					}

					PhoneHelper.getInstance().setPhoneJsonObject(new JSONObject(json.toString()));

				} catch (Exception ex) {}

				try {

					XposedHelpers.findAndHookMethod(
						Class.forName("com.Android.MFSocket.SystemMsg",false,pkg.classLoader),
						"getSystemMsg",
						StringBuffer.class,
						new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {

								StringBuffer sysMsg =  (StringBuffer) params.args[0];

								RecordPackage.AppendItem("STAT",sysMsg);
								RecordPackage.AppendItem(114514,sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("IMEI",sysMsg);
								RecordPackage.AppendItem(RandomHelper.getInstance().randomTelephonyGetDeviceId(),sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("IMSI",sysMsg);
								RecordPackage.AppendItem(RandomHelper.getInstance().randomSimSubscriberId(),sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("IMOS",sysMsg);
								RecordPackage.AppendItem(RandomHelper.getInstance().randomBuildVersionName(),sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("INET",sysMsg);
								RecordPackage.AppendItem(no,sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("ISIM",sysMsg);
								RecordPackage.AppendItem(RandomHelper.getInstance().randomTelephonySimSerialNumber(),sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								try {
									RecordPackage.AppendItem("WIFIMAC",sysMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomWifiInfoMacAddress(),sysMsg);
									RecordPackage.AppendRecord(sysMsg);
									RecordPackage.AppendItem("BTMAC",sysMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomWifiInfoMacAddress(),sysMsg);
									RecordPackage.AppendRecord(sysMsg);
									RecordPackage.AppendItem("OWNER",sysMsg);
									RecordPackage.AppendItem(RandomHelper.getInstance().randomString(RandomHelper.getInstance().randomInt(1,11),true,true,true),sysMsg);
									RecordPackage.AppendRecord(sysMsg);
								} catch (Exception e) {
									e.printStackTrace();
								}

								String mf = "HUAWEI";
								String name = "P10";
								String model = "VTR-AL00";


								try {


									ArrayList<String> mfList = PhoneHelper.getInstance().getManufacturerList();

									mf = mfList.get(RandomHelper.getInstance().randomInt(0,mfList.size()));

									HashMap<String, String> modelList = PhoneHelper.getInstance().getModelList(mf);


									name = (String) modelList.keySet().toArray()[RandomHelper.getInstance().randomInt(0,modelList.size())];
									model = modelList.get(name);

								} catch (Exception ex) {}

								RecordPackage.AppendItem("MODEL",sysMsg);
								RecordPackage.AppendItem(model,sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("MANUFACTURE",sysMsg);
								RecordPackage.AppendItem(mf,sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("RELEASE",sysMsg);
								RecordPackage.AppendItem(RandomHelper.getInstance().randomBuildVersionName(),sysMsg);
								RecordPackage.AppendRecord(sysMsg);
								RecordPackage.AppendItem("NAME",sysMsg);
								RecordPackage.AppendItem(name,sysMsg);
								RecordPackage.AppendRecord(sysMsg);

								return null;

							}
						});

					return;

				} catch (ClassNotFoundException e) {}

			} catch (ClassNotFoundException ex) {}

		}

		if (!"com.android.mfsocket".equals(pkg.packageName))	{

			return;

		} else {

			// 应用信息

			XposedHelpers.findAndHookMethod(
				PackageManager.class,
				"getInstalledPackages",
				int.class,
				new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws java.lang.Throwable {

						List<PackageInfo> installedPackages = (List<PackageInfo>) param.getResult();

						List<PackageInfo> result = new LinkedList<>();

						for (PackageInfo app : installedPackages) {

                            for (String prefix : whitePrefix) {

                                if (app.packageName.startsWith(prefix)) {

                                    result.add(app);

                                }

                            }

                        }

						param.setResult(result);


					}


				});

			// SHELL

			XposedBridge.hookAllMethods(Runtime.class,"exec",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {

						param.setThrowable(new IOException());

						return null;
					}
				});

			// 蓝牙Mac与设备

			XposedHelpers.findAndHookMethod(BluetoothAdapter.class,
				"getAddress",
				new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomWifiInfoMacAddress();

					}


				});

			// WIFI MAC 与 其他网络信息

			XposedHelpers.findAndHookMethod(WifiManager.class,"getConnectionInfo",METHOD_EMPTY);

			XposedHelpers.findAndHookMethod(NetworkInterface.class,"getNetworkInterfaces",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {
						return new LinkedList<>();
					}

				});

			XposedHelpers.findAndHookMethod(WifiManager.class,"getConfiguredNetworks",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {
						return new LinkedList();
					}
				});

			XposedHelpers.findAndHookMethod(BluetoothAdapter.class,
				"getBondedDevices",
				new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return new LinkedHashSet();

					}


				});

			// 因为代码混淆以及内容复杂 且咱没什么时间 这里直接打掉好了 (短信、媒体文件、日历、通话记录等等等...)

			XposedBridge.hookAllMethods(ContentResolver.class,
				"query",
				new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {

						param.args[0] = Uri.parse("content://null");

					}

				});

			// 定位

			XposedHelpers.findAndHookMethod(LocationManager.class,"getLastKnownLocation",METHOD_EMPTY);

			// 设备信息

			XposedBridge.hookAllMethods(TelephonyManager.class,"getDeviceId",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomTelephonyGetDeviceId();

					}
				});

			XposedBridge.hookAllMethods(TelephonyManager.class,"getImei",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomTelephonyGetDeviceId();

					}
				});

			XposedBridge.hookAllMethods(TelephonyManager.class,"getMeid",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomTelephonyGetDeviceId();

					}
				});

			XposedBridge.hookAllMethods(TelephonyManager.class,"getSubscriberId",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomTelephonyGetDeviceId();

					}
				});

			XposedBridge.hookAllMethods(TelephonyManager.class,"getLine1Number",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomTelephonyGetLine1Number();

					}
				});


			XposedBridge.hookAllMethods(TelephonyManager.class,"getSimSerialNumber",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {

						return RandomHelper.getInstance().randomTelephonySimSerialNumber();

					}
				});


		    // 设备信息

			XposedBridge.hookAllMethods(Class.forName("com.android.mfsocket.b",false,pkg.classLoader),"a",new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam p1) throws Throwable {
						return true;
					}
				});

		}



    }

    public static void sendData(BufferedOutputStream out,String data) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {

        Class.forName("com.Android.MFSocket.SocketTrans",true,loader)
            .getMethod("sendData",BufferedOutputStream.class,String.class)
            .invoke(null,out,data);

    }

    public static class RecordPackage {

        public static void AppendItem(double paramDouble,StringBuffer paramStringBuffer) {
            paramStringBuffer.append(paramDouble);
            paramStringBuffer.append("#/}.[*#");
        }

        public static void AppendItem(int paramInt,StringBuffer paramStringBuffer) {
            paramStringBuffer.append(paramInt);
            paramStringBuffer.append("#/}.[*#");
        }

        public static void AppendItem(long paramLong,StringBuffer paramStringBuffer) {
            paramStringBuffer.append(paramLong);
            paramStringBuffer.append("#/}.[*#");
        }

        public static void AppendItem(String paramString,StringBuffer paramStringBuffer) {
            paramStringBuffer.append(paramString);
            paramStringBuffer.append("#/}.[*#");
        }

        public static void AppendRecord(StringBuffer paramStringBuffer) {
            paramStringBuffer.append("$/}.[*$");
        }

    }


}
