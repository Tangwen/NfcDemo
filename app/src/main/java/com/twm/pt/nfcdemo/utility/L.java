package com.twm.pt.nfcdemo.utility;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * 用來記錄TRACE ALL STACK
 * 
 * 	定義:
	預設logList為空

	當使用traceNow時會看IS_DEBUG與logList決定要不要印
	但evenLog都會append

	但當發生error或用L.check()時只會看IS_DEBUG決定要不要印
	不會看logList有沒有目前的class

	用法:

	若目前是在Main.java
	在onCreate裡加一段
	L.logList.put(Main.class.getName(), true);
	就會印此java的log
	若
	L.logList.put(Main.class.getName(), false);
	就不會印此java的log
 * 
 * 
 * */
public final class L {
	public final static String TAG = "myPlay1";
	/**用來記錄所有EVENT，呼叫traceNow()就記一筆
	 * 當呼叫L.printEventLog()就會印出來*/
	public static StringBuilder evenLog=new StringBuilder();
	
	/**判斷現在要不要印log*/
	public final static boolean IS_DEBUG=true;		//上線版本 false : log都不要印出
	/**判斷現在要不要寫logfile*/
	public static boolean IS_WRITE_LOGFILE=true;
	private static String packageName = "com.twm.pt.myplay1";
	private static String appName = "myPlay1";
	private static String logFileName = "myPlay1.txt";
	/**  DB_PATH = /data/data/com.com.twm.ebgvoip.nicall */
	private static String dbPath = File.separator + "data"+File.separator+"data"+File.separator+packageName;
	
	/** 要印出LOG的Class清單,key是className,value是boolean(是否要印) */
	public static ConcurrentHashMap<String, Boolean> logList = new ConcurrentHashMap<String, Boolean>();
	
	/**判斷現在要不要印全部的log，層級在IS_DEBUG之下*/
	public final static boolean IS_PRINT_ALL=true;
	
	/**預設要印LOG的清單*/
	static{
		//L.logList.put(Main.class.getName(), true);
		//L.logList.put(EzpeerAudioPlayer.class.getName(), true);
		//L.logList.put(EzpeerAudioPlayerService.class.getName(), true);
		//L.logList.put(hot_list.class.getName(), true);
		//L.logList.put(Common_APIServer.class.getName(), true);
		//L.logList.put(MyMusicDBBehavior.class.getName(), true);
		//L.logList.put(UserMusicListDAO.class.getName(), true);
		
	}
	

	/**
	 * 用來trace目前跑了幾個class和method
	 * */
	public final static void traceAll() {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		StackTraceElement s=null;
		try {
			Log.d(TAG, "******************  Trace Log Start  ******************" );
			final StackTraceElement[] st = Thread.currentThread()
					.getStackTrace();
			for (int i=0;i<st.length;i++) {
				s=st[i];
				nowClass = s.getClassName();
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = s.getMethodName();
				nowLineNumber = s.getLineNumber();
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" );
			} 
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		Log.d(TAG, "******************  Trace Log End  ******************" );
	}
	
	/**
	 * 印出所有EVENT
	 * 可用L.traceNow()來append eventLog
	 * */
	public final static void printEventLog(){
		if(IS_DEBUG){
			Log.d(TAG, "******************  Even Log Start  ******************" );
			Log.d(TAG, evenLog.toString());
			Log.d(TAG, "******************  Even Log End  ******************" );
		}
	}
	
	/**
	 * 用來記錄Exception
	 * 會印出eventLog
	 * eventLog可用L.traceNow()來append
	 * */
	public final static void e(String TAG, String msg,Exception e) {
		if(IS_DEBUG){
			Log.e(TAG, msg, e);
			if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, msg, e);
			printEventLog();
		}
		
	}
	
	/**
	 * 用來記錄Exception
	 * 會印出eventLog
	 * eventLog可用L.traceNow()來append
	 * */
	public final static void e(String msg,Exception e) {
		if(IS_DEBUG){
			Log.e(TAG, msg,  e);
			if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, msg, e);
			printEventLog();
		}
	}
	
	/**
	 * 用來記錄Exception
	 * 會印出eventLog
	 * eventLog可用L.traceNow()來append
	 * */
	public final static void e(Exception e) {
		if(IS_DEBUG){
			Log.e(TAG, "",  e);
			if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "", e);
			printEventLog();
		}
	}
	
	public final static void e(OutOfMemoryError e) {
		//if (!showDebug) return;
		if(IS_DEBUG){
			try {
				Log.e(TAG, "",  e);
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "", e);
			} catch (RuntimeException e2) {
			} catch (Exception e2) {
			}
		}
		try {
			//==========================
		} catch (RuntimeException e2) {
			L.e(e);
		}catch (Exception e2) {
			L.e(e);
		}
		System.gc();
        Runtime.getRuntime().gc();
	}
	
	/**
	 * 用來記錄Exception classname, method, line number
	 * 但不會印出eventLog
	 * */
	@SuppressWarnings("unused")
	public final static void w(String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3]
						.getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3]
						.getLineNumber();
				Log.w(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]["+msg+"]");
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]["+msg+"]");
			}
		} catch (Exception e) {
			Log.e(TAG, nowMethod, e);
			Log.e(TAG, "");
		}
	}
	
	
	/**
	 * 用來記錄Exception classname, method, line number
	 * 但不會印出eventLog
	 * */
	@SuppressWarnings("unused")
	public final static void w(String tag, String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3]
						.getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3]
						.getLineNumber();
				Log.w(tag, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]["+msg+"]");
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]["+msg+"]");
			}
		} catch (Exception e) {
			Log.e(tag, nowMethod, e);
			Log.e(tag, "");
		}
	}
	
	/**
	 * 用來記錄Exception classname, method, line number
	 * 但不會印出eventLog
	 * */
	public final static void e(String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(IS_DEBUG){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3]
						.getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3]
						.getLineNumber();
				Log.e(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]["+msg+"]");
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]["+msg+"]");
			}
		} catch (Exception e) {
			Log.e(TAG, nowMethod, e);
			Log.e(TAG, "");
		}
	}

	/**
	 * 用來記錄目前跑到哪一個CLASS、METHOD、行數和訊息
	 * */
	@SuppressWarnings("unused")
	public final static void d(String TAG, String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
			}
		} catch (Exception e) {
			Log.e(TAG, nowMethod, e);
			Log.d(TAG, msg);
		}
	}

    /**
     * 用來記錄目前跑到哪一個CLASS、METHOD、行數和訊息
     * */
    @SuppressWarnings("unused")
    public final static void i(String TAG, String msg) {
        String nowClass = "";
        String nowMethod = "";
        int nowLineNumber = 0;
        try {
            nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
            if(nowClass.indexOf("$")!=-1)
                nowClass=nowClass.substring(0,nowClass.indexOf("$"));
            if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
                nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
                nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
                nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
                Log.i(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
                if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
            }
        } catch (Exception e) {
            Log.e(TAG, nowMethod, e);
            Log.d(TAG, msg);
        }
    }
	
	@SuppressWarnings("unused")
	public final static void e(String TAG, String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
				Log.e(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
			}
		} catch (Exception e) {
			Log.e(TAG, nowMethod, e);
			Log.d(TAG, msg);
		}
	}
	
	/**
	 * 用來記錄哪邊還要繼續寫完
	 * */
	public final static void check() {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(IS_DEBUG){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3]
						.getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3]
						.getLineNumber();
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]******************************************************");
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]    TODO 未完成");
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]******************************************************");
			}
		} catch (Exception e) {
			Log.e(TAG, nowMethod, e);
		}
	}
	
	/**
	 * 用來記錄哪邊還要繼續寫完
	 * */
	public final static void check(String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(IS_DEBUG){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3]
						.getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3]
						.getLineNumber();
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]******************************************************");
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]    TODO "+msg);
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]******************************************************");
			}
		} catch (Exception e) {
			Log.e(TAG, nowMethod, e);
		}
	}

	/**
	 * 用來記錄目前跑到哪一個CLASS、METHOD、行數和訊息
	 * */
	@SuppressWarnings("unused")
	public final static void d(String msg) {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]" + msg);
			}
		} catch (Exception e) {
			Log.e(TAG, nowClass, e);
			Log.d(TAG, nowClass + "[" + nowMethod + "][" + nowLineNumber + "]" + msg);
		}
	}
	
	/**
	 * 用來記錄目前跑到哪一個CLASS、METHOD、行數和訊息
	 * */
	@SuppressWarnings("unused")
	public final static void d() {
		String nowClass = "";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				nowClass=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
				nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
				nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
				Log.d(TAG, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]");
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, "["+nowClass + "][" + nowMethod + "][" + nowLineNumber + "]");
			}
		} catch (Exception e) {
			Log.e(TAG, nowClass, e);
			Log.d(TAG, nowClass + "[" + nowMethod + "][" + nowLineNumber + "]");
		}
	}
	
	/**
	 * 用來記錄目前跑到哪一個CLASS、METHOD、行數
	 * 同時append到evenLog
	 * 當呼叫L.printEventLog()可以印出所有eventLog
	 * */
	@SuppressWarnings("unused")
	public final static void traceNow() {
		String nowClass = "",nowClass2="";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			nowClass2=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
			nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
			nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			final String log="["+nowClass2 + "][" + nowMethod + "][" + nowLineNumber + "]";
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				Log.d(TAG, log );
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, log);
			}
			evenLog.append(log).append("\n");
		} catch(ArrayIndexOutOfBoundsException a){
			printEventLog();
			evenLog=new StringBuilder();
		}catch (Exception e) {
			Log.e(TAG, nowClass, e);
			Log.d(TAG, nowClass + "[" + nowMethod + "][" + nowLineNumber + "]" );
		}
	}
	
	/**
	 * 用來記錄目前跑到哪一個CLASS、METHOD、行數和訊息
	 * 同時append到evenLog
	 * 當呼叫L.printEventLog()可以印出所有eventLog
	 * */
	@SuppressWarnings("unused")
	public final static void traceNow(String msg) {
		String nowClass = "",nowClass2="";
		String nowMethod = "";
		int nowLineNumber = 0;
		try {
			nowClass = Thread.currentThread().getStackTrace()[3].getClassName();
			if(nowClass.indexOf("$")!=-1)
				nowClass=nowClass.substring(0,nowClass.indexOf("$"));
			nowClass2=(nowClass.indexOf(46)!=-1)?nowClass.substring(nowClass.lastIndexOf(46)+1):nowClass;
			nowMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
			nowLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			final String log="["+nowClass2 + "][" + nowMethod + "][" + nowLineNumber + "]"+msg;
			if(IS_DEBUG&&(IS_PRINT_ALL||(logList.containsKey(nowClass)&&logList.get(nowClass)))){
				Log.d(TAG, log );
				if(IS_WRITE_LOGFILE) getFileLogger().log(Level.ALL, log);
			}
			evenLog.append(log).append("\n");
		} catch(ArrayIndexOutOfBoundsException a){
			printEventLog();
			evenLog=new StringBuilder();
		}catch (Exception e) {
			Log.e(TAG, nowClass, e);
			Log.d(TAG, nowClass + "[" + nowMethod + "][" + nowLineNumber + "]"+msg );
		}
	}

	private static String getLogPath() {
		return Environment.getExternalStorageDirectory() + File.separator + appName;
	}
	@SuppressWarnings("unused")
	private static String getDBFilePaht() {
		return dbPath+File.separator+"databases"+File.separator+packageName+".db";
	}
	private static Logger fileLogger = null;
	private static Logger getFileLogger(){
		try {
			if(fileLogger==null){
				fileLogger = Logger.getLogger(packageName);
				String logPath = getLogPath();
				if(createDirectory(new File(logPath))) {
					String logFilePathName = logPath + File.separator + logFileName;
					FileHandler logHandler = new FileHandler(logFilePathName, 1024*1024*10, 8, true);
					Formatter formatter = getFormatter();
					logHandler.setFormatter(formatter);
					fileLogger.setLevel(Level.ALL);
					fileLogger.addHandler(logHandler);
				}
			}
		} catch (RuntimeException e) {
		} catch (Exception e) {
		}
		return fileLogger;
	}	
	
	private static boolean createDirectory(File createDir) {
		if(createDir.exists()) return true;
		return createDir.mkdir();
	}
	
	private static Formatter getFormatter(){
		Formatter formatter = null;
		try {
			formatter = new Formatter(){
				@Override
				public String format(LogRecord r) {
					return new java.text.SimpleDateFormat("MMdd-HH:mm ss,SSS", Locale.getDefault()).format(new Date(r.getMillis()))+"-"+r.getMessage()+"\n";
				}
			};
		} catch (RuntimeException e) {
		} catch (Exception e) {
		}
		return formatter;
	}	
	
	/**
	 * generate log zip file
	 * @param context
	 */
	public static String exportLog(Context context) {
		try {

			
			String zip = zipLog();
			
			return zip;
		} catch (Exception e) {
			L.e(e);
		}
		return null;
	}
	
	/**
	 * 把SDcard裡的log zip起來成一個file
	 * @return zipPath zip file abs path, otherwise null
	 * */
	private static String zipLog(){
//		ArrayList<File> zipFiles = new ArrayList<File>();
//		String zipPath = getLogPath() + File.separator + appName 
//				+ new java.text.SimpleDateFormat("_yyyy_MM_dd_HHmm", Locale.getDefault()).format(Calendar.getInstance().getTime())
//				+"(" + Env.SERVER_ENV+ ").zip";
//				;
//		File preZip=null;
//		try {
//			L.d("getLogPath()=" + getLogPath());
//			File f = new File(getLogPath());
//			if(!f.exists()){
//				if(L.IS_DEBUG) L.d("Log Path==null");
//				return null;
//			}
//			File[] ff = f.listFiles();
//			if(ff==null){
//				if(L.IS_DEBUG) L.d("Log Directory==null");
//				return null;
//			}
//			if(ff.length==0){
//				if(L.IS_DEBUG) L.d("Log Directory is empty");
//				return null;
//			}
//			for(File d : ff){
//				if(d.getName().contains(logFileName)){
//					zipFiles.add(d);
//				}else if(d.getName().contains(appName) && d.getName().contains(".zip")){
//					preZip = new File(d.getAbsolutePath());
//				}
//			}
//			//Add DB file
//			File dbFile = new File(getDBFilePaht());
//			L.d("getDBFilePaht()=" + getDBFilePaht());
//			if(dbFile.exists()) {
//				zipFiles.add(dbFile);
//				L.d();
//			} else {
//				L.d();
//			}
//			
//			ZipUtil.proccessMutipleFileToOneZip(zipPath,zipFiles);
//			return zipPath;
//		} catch (Exception e) {
//			L.e(e);
//		}finally{
//			if(preZip!=null){
//				boolean del = preZip.delete();
//				if(L.IS_DEBUG)L.d("砍之前的ZIP[" + preZip.getName() + "][" + del + "]");
//			}
//		}
		return null;
	}	
}
