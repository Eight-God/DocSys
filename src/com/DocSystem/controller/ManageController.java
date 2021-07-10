package com.DocSystem.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import util.DateFormat;
import util.ReadProperties;
import util.ReturnAjax;
import com.DocSystem.entity.DocAuth;
import com.DocSystem.entity.GroupMember;
import com.DocSystem.entity.Repos;
import com.DocSystem.entity.Doc;
import com.DocSystem.entity.User;
import com.DocSystem.entity.ReposAuth;
import com.DocSystem.entity.ReposMember;
import com.DocSystem.entity.UserGroup;

import com.DocSystem.service.impl.ReposServiceImpl;
import com.DocSystem.service.impl.UserServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.DocSystem.common.FileUtil;
import com.DocSystem.common.Log;
import com.DocSystem.common.Path;
import com.DocSystem.common.entity.QueryResult;
import com.DocSystem.controller.BaseController;

@Controller
@RequestMapping("/Manage")
public class ManageController extends BaseController{
	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private ReposServiceImpl reposService;

	
	/********** 系统初始化 ***************/
	@RequestMapping("/docSysInit.do")
	public void docSysInit(String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** docSysInit.do ***********************");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}

		System.out.println("docSysInit.do docSysInit() Start docSysInitState:" + docSysIniState);
		if(docSysIniState == 1)
		{
			System.out.println("docSysInit.do 用户自定义数据库配置文件与系统数据库配置文件不一致，等待重启生效！");
			rt.setData("needRestart");
			writeJson(rt, response);
			return;
		}
				
		String ret = docSysInit(true);
		switch(ret)
		{
		case "ok":
			docSysIniState = 0;
			break;
		case "needRestart":
			docSysIniState = 1;
			break;
		default:
			docSysIniState = -2;
			Log.docSysErrorLog(ret, rt);
			break;
		}
		
		System.out.println("docSysInit.do docSysInit() End docSysInitState:" + docSysIniState);
		
		rt.setData(ret);
		writeJson(rt, response);
	}
	
	/********** 获取轮播图配置 ***************/
	@RequestMapping("/getBannerConfig.do")
	public void getBannerConfig(String serverIP, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getBannerConfig.do ***********************");

		System.out.println("getBannerConfig() serverIP:" + serverIP);
		collectDocSysInstallationInfo(serverIP, request);
		
		ReturnAjax rt = new ReturnAjax();

		//设置跨域访问允许
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", " GET,POST,OPTIONS,HEAD");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type,Accept,Authorization");
		response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
		
		String BannerConfigPath = docSysIniPath;
		if(FileUtil.isFileExist(BannerConfigPath + "bannerConfig.json") == false)
		{
			BannerConfigPath = docSysWebPath + "WEB-INF/classes/";
			if(FileUtil.isFileExist(BannerConfigPath + "bannerConfig.json") == false)
			{
				Log.docSysErrorLog("bannerConfig.json文件不存在",rt);
				writeJson(rt, response);
				return;	
			}
		}
		
		String s = FileUtil.readDocContentFromFile(BannerConfigPath, "bannerConfig.json");
		JSONObject jobj = JSON.parseObject(s);
		if(jobj == null)
		{
			Log.docSysErrorLog("解析bannerConfig.json文件失败",rt);
			writeJson(rt, response);
			return;	
		}
		
		JSONArray list = jobj.getJSONArray("BannerConfigList");
		rt.setData(list);
		writeJson(rt, response);
	}

	/********** 获取系统邮件配置 ***************/
	@RequestMapping("/getSystemEmailConfig.do")
	public void getSystemEmailConfig(String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getSystemEmailConfig.do ***********************");

		System.out.println("getSystemEmailConfig()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		
		String host = ReadProperties.read("docSysConfig.properties", "mail.smtp.host");
		if(host == null)
		{
			host = "";
		}
		String email = ReadProperties.read("docSysConfig.properties", "fromuser");
		if(email == null)
		{
			email = "";
		}
		String pwd = ReadProperties.read("docSysConfig.properties", "frompwd");
		if(pwd == null)
		{
			pwd = "";
		}
		
		JSONObject config = new JSONObject();
		config.put("host", host);
		config.put("email", email);
		config.put("pwd", pwd);
		rt.setData(config);
		writeJson(rt, response);
	}
	
	/********** 设置系统邮件配置 ***************/
	@RequestMapping("/setSystemEmailConfig.do")
	public void setSystemEmailConfig(String authCode,String host, String email, String pwd, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** setSystemEmailConfig.do ***********************");
		
		System.out.println("setSystemEmailConfig() host:" + host + " email:" + email + " pwd:" + pwd);
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		if(email == null && pwd == null)
		{
			Log.docSysErrorLog("没有参数改动，请重新设置！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//checkAndAdd docSys.ini Dir
		if(checkAndAddDocSysIniDir())
		{
			Log.docSysErrorLog("系统初始化目录创建失败！", rt);
			writeJson(rt, response);			
			return;
		}
		
		String docSystemConfigPath = docSysWebPath + "WEB-INF/classes/";
		String tmpDocSystemConfigPath = docSysIniPath;
		String configFileName = "docSysConfig.properties";
		if(FileUtil.copyFile(docSystemConfigPath + configFileName, tmpDocSystemConfigPath + configFileName, true) == false)
		{
			//Failed to copy 
			System.out.println("setSystemEmailConfig() Failed to copy " + docSystemConfigPath + configFileName + " to " + tmpDocSystemConfigPath + configFileName);
			Log.docSysErrorLog("创建临时配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}

		if(host != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "mail.smtp.host", host);
		}
		if(email != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "fromuser", email);
		}
		if(pwd != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "frompwd", pwd);
		}
		
		if(FileUtil.copyFile(tmpDocSystemConfigPath + configFileName, docSystemConfigPath + configFileName, true) == false)
		{
			System.out.println("setSystemEmailConfig() Failed to copy " + tmpDocSystemConfigPath + configFileName + " to " + docSystemConfigPath + configFileName);
			Log.docSysErrorLog("写入配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
	}

	
	private boolean checkAndAddDocSysIniDir() {
		File docSysIniDir = new File(docSysIniPath);
		if(docSysIniDir.exists() == true)
		{
			return false;
		}
		
		return docSysIniDir.mkdirs();
	}
	
	@RequestMapping("/getSystemSmsConfig.do")
	public void getSystemSmsConfig(String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getSystemSmsConfig.do ***********************");

		System.out.println("getSystemSmsConfig()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		String server = ReadProperties.read("docSysConfig.properties", "smsServer");
		if(server == null)
		{
			server = "";
		}
		
		String apikey = ReadProperties.read("docSysConfig.properties", "smsApikey");
		if(apikey == null)
		{
			apikey = "";
		}
		
		String tplid = ReadProperties.read("docSysConfig.properties", "smsTplid");
		if(tplid == null)
		{
			tplid = "";
		}
		
		JSONObject config = new JSONObject();
		config.put("server", server);
		config.put("apikey", apikey);
		config.put("tplid", tplid);
		
		rt.setData(config);
		writeJson(rt, response);
	}
	
	/********** 设置系统短信配置 ***************/
	@RequestMapping("/setSystemSmsConfig.do")
	public void setSystemSmsConfig(String authCode,String server, String apikey, String tplid, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** setSystemSmsConfig.do ***********************");
		
		System.out.println("setSystemSmsConfig() server:" + server + " apikey:" + apikey + " tplid:" + tplid);
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		if(server == null && apikey == null && tplid == null)
		{
			Log.docSysErrorLog("没有参数改动，请重新设置！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//checkAndAdd docSys.ini Dir
		if(checkAndAddDocSysIniDir())
		{
			Log.docSysErrorLog("系统初始化目录创建失败！", rt);
			writeJson(rt, response);			
			return;
		}
		
		String docSystemConfigPath = docSysWebPath + "WEB-INF/classes/";
		String tmpDocSystemConfigPath = docSysIniPath;
		String configFileName = "docSysConfig.properties";
		if(FileUtil.copyFile(docSystemConfigPath + configFileName, tmpDocSystemConfigPath + configFileName, true) == false)
		{
			//Failed to copy 
			System.out.println("setSystemSmsConfig() Failed to copy " + docSystemConfigPath + configFileName + " to " + tmpDocSystemConfigPath + configFileName);
			Log.docSysErrorLog("创建临时配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}

		if(server != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "smsServer", server);
		}
		if(apikey != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "smsApikey", apikey);
		}
		if(tplid != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "smsTplid", tplid);
		}
		
		if(FileUtil.copyFile(tmpDocSystemConfigPath + configFileName, docSystemConfigPath + configFileName, true) == false)
		{
			System.out.println("setSystemSmsConfig() Failed to copy " + tmpDocSystemConfigPath + configFileName + " to " + docSystemConfigPath + configFileName);
			Log.docSysErrorLog("写入配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
	}
	
	@RequestMapping("/getSystemDbConfig.do")
	public void getSystemDbConfig(String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getSystemDbConfig.do ***********************");

		System.out.println("getSystemDbConfig()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			rt.setData("invalidAuthCode");
			writeJson(rt, response);			
			return;
		}
		
		String type = ReadProperties.read("jdbc.properties", "db.type");
		if(type == null)
		{
			type = "";
		}
		String url = ReadProperties.read("jdbc.properties", "db.url");
		if(url == null)
		{
			url = "";
		}
		String user = ReadProperties.read("jdbc.properties", "db.username");
		if(user == null)
		{
			user = "";
		}
		
		String pwd = ReadProperties.read("jdbc.properties", "db.password");
		if(pwd == null)
		{
			pwd = "";
		}
		
		JSONObject config = new JSONObject();
		config.put("type", type);
		config.put("url", url);
		config.put("user", user);
		config.put("pwd", pwd);

		rt.setData(config);
		writeJson(rt, response);
	}
	
	/********** 设置系统数据库配置 ***************/
	@RequestMapping("/setSystemDBConfig.do")
	public void setSystemDBConfig(String authCode, String type, String url, String user, String pwd, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** setSystemDBConfig.do ***********************");

		System.out.println("setSystemDBConfig() type:"  + type + " url:" + url + " user:" + user  + " pwd:" + pwd);
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		if(url == null && user == null && pwd == null)
		{
			Log.docSysErrorLog("没有参数改动，请重新设置！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//checkAndAdd docSys.ini Dir
		if(checkAndAddDocSysIniDir())
		{
			Log.docSysErrorLog("系统初始化目录创建失败！", rt);
			writeJson(rt, response);			
			return;
		}
		
		String docSystemConfigPath = docSysWebPath + "WEB-INF/classes/";
		String tmpDocSystemConfigPath = docSysIniPath;
		String configFileName = "jdbc.properties";
		if(FileUtil.copyFile(docSystemConfigPath + configFileName, tmpDocSystemConfigPath + configFileName, true) == false)
		{
			//Failed to copy 
			System.out.println("setSystemDBConfig() Failed to copy " + docSystemConfigPath + configFileName + " to " + tmpDocSystemConfigPath + configFileName);
			Log.docSysErrorLog("创建临时配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}
		
		if(type != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "db.type", type);
		}
		if(url != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "db.url", url);
		}
		if(user != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "db.username", user);
		}
		if(pwd != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "db.password", pwd);
		}
		
		if(FileUtil.copyFile(tmpDocSystemConfigPath + configFileName, docSystemConfigPath + configFileName, true) == false)
		{
			System.out.println("setSystemDBConfig() Failed to copy " + tmpDocSystemConfigPath + configFileName + " to " + docSystemConfigPath + configFileName);
			Log.docSysErrorLog("写入配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}
		
		docSysIniState = 1; //needRestart
		addDocSysInitAuthCode();
		
		writeJson(rt, response);
	}
	
	@RequestMapping("/testDatabase.do")
	public void testDatabase(String type, String url, String user, String pwd, String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** testDatabase.do ***********************");

		System.out.println("testDatabase() type:" + type + " url:" + url + " user:" + user + " pwd:" + pwd);
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}

		if(testDB(type, url, user, pwd) == false)	//数据库不存在
		{
			System.out.println("testDatabase() 连接数据库:" + url + " 失败");
			Log.docSysErrorLog("连接数据库失败", rt);
		}
		writeJson(rt, response);
	}
	
	//强制复位数据库
	@RequestMapping("/deleteDatabase.do")
	public void deleteDatabase(String type, String url, String user, String pwd, String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		System.out.println("\n****************** deleteDatabase.do ***********************");

		System.out.println("deleteDatabase()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}

		if(testDB(type, url, user, pwd) == false)	//数据库不存在
		{
			System.out.println("deleteDatabase() 连接数据库:" + url + " 失败");
			Log.docSysErrorLog("连接数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		
		//backUpDB
		Date date = new Date();
		String backUpTime = DateFormat.dateTimeFormat2(date);
		String backUpPath = docSysIniPath + "backup/" + backUpTime + "/";
		if(backupDatabase(backUpPath, type, url, user, pwd, true) == false)
		{
			System.out.println("deleteDatabase() 数据库备份失败!");
			Log.docSysErrorLog("备份数据库失败", rt);
			writeJson(rt, response);
			return;
		}		
		
		String dbName = getDBNameFromUrl(type, url);
		String tmpDbName = dbName.toLowerCase();
		System.out.println("deleteDatabase() dbName:" + dbName + " tmpDbName:" + tmpDbName);
		if(!tmpDbName.contains("docsystem")) //只能删除docsystem相关的数据库
		{
			System.out.println("deleteDatabase() 非法删除操作");
			Log.docSysErrorLog("非法删除操作：" + dbName, rt);
			writeJson(rt, response);			
			return;			
		}
		
		if(deleteDB(type, dbName, url, user, pwd) == false)
		{
			System.out.println("deleteDatabase() 删除数据库失败");
			Log.docSysErrorLog("数据库初始化失败", rt);
			writeJson(rt, response);			
			return;
		}
		writeJson(rt, response);
	}
	
	//强制复位数据库
	@RequestMapping("/resetDatabase.do")
	public void resetDatabase(String type, String url, String user, String pwd, String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		System.out.println("\n****************** resetDatabase.do ***********************");

		System.out.println("resetDatabase() type:" + type + " url:" + url + " user:" + user + " pwd:" + pwd);

		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}

		if(testDB(type, url, user, pwd) == false)	//数据库不存在
		{
			System.out.println("resetDatabase() 连接数据库:" + url + " 失败");
			String dbName = getDBNameFromUrl(type, url);

			createDB(type, dbName, url, user, pwd);
			if(initDB(type, url, user, pwd) == false)
			{
				System.out.println("resetDatabase() 新建数据库失败");
				Log.docSysErrorLog("新建数据库失败", rt);
				writeJson(rt, response);
				return;
			}
			writeJson(rt, response);
			return;
		}
		
		//backUpDB
		Date date = new Date();
		String backUpTime = DateFormat.dateTimeFormat2(date);
		String backUpPath = docSysIniPath + "backup/" + backUpTime + "/";
		if(backupDatabase(backUpPath, type, url, user, pwd, true) == false)
		{
			System.out.println("resetDatabase() 数据库备份失败!");
			Log.docSysErrorLog("备份数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		deleteDBTabsEx(type, url, user, pwd);
		if(initDB(type, url, user, pwd) == false)
		{
			System.out.println("resetDatabase() reset database failed: initDB error");
			Log.docSysErrorLog("数据库初始化失败", rt);
			writeJson(rt, response);			
			return;
		}
		writeJson(rt, response);
	}
	
	@RequestMapping("/exportDBData.do")
	public void exportDBData(String type, String url, String user, String pwd, String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		System.out.println("\n****************** exportDBData.do ***********************");

		System.out.println("exportDBData()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}

		if(testDB(type, url, user, pwd) == false)	//数据库不存在
		{
			System.out.println("exportDBData() 连接数据库:" + url + " 失败");
			Log.docSysErrorLog("连接数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		
		//backUpDB
		Date date = new Date();
		String backUpTime = DateFormat.dateTimeFormat2(date);
		String backUpPath = docSysIniPath + "backup/" + backUpTime + "/";
		if(backupDatabase(backUpPath, type, url, user, pwd, true) == false)
		{
			System.out.println("exportDBData() 数据库备份失败!");
			Log.docSysErrorLog("备份数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		
		String targetPath = docSysIniPath + "backup/";
		String targetName = "docsystem_"+backUpTime+".zip";
		if(doCompressDir(docSysIniPath + "backup/", backUpTime, docSysIniPath + "backup/", targetName, rt) == false)
		{
			Log.docSysErrorLog("压缩本地目录失败！", rt);
			writeJson(rt, response);
			return;
		}
		
		Doc downloadDoc = buildDownloadDocInfo(0, "","", targetPath, targetName);
		String downloadLink = "/DocSystem/Doc/downloadDoc.do?vid=" + downloadDoc.getVid() + "&path="+ downloadDoc.getPath() + "&name="+ downloadDoc.getName() + "&targetPath=" + downloadDoc.targetPath + "&targetName="+downloadDoc.targetName;	
		rt.setData(downloadLink);
		writeJson(rt, response);
	}
	
	@RequestMapping("/importDBData.do")
	public void importDBData(MultipartFile uploadFile, String type, String url, String user, String pwd, String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		System.out.println("\n****************** importDBData.do ***********************");

		System.out.println("importDBData()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		//FileUtil.saveFile to tmpPath
		if(uploadFile == null)
		{
			System.out.println("importDBData() uploadFile is null");
			Log.docSysErrorLog("上传文件为空", rt);
			writeJson(rt, response);
			return;
		}
		String fileName = uploadFile.getOriginalFilename();
		String suffix = FileUtil.getFileSuffix(fileName);
		String webTmpPathForImportDBData = getWebTmpPath() + "importDBData/";
		FileUtil.saveFile(uploadFile, webTmpPathForImportDBData, fileName);
		
		if(testDB(type, url, user, pwd) == false)	//数据库不存在
		{
			System.out.println("importDBData() 连接数据库:" + url + " 失败");
			Log.docSysErrorLog("连接数据库失败", rt);
			writeJson(rt, response);
			return;
		}

		if(importDatabase(null, suffix, webTmpPathForImportDBData, fileName, type, url, user, pwd) == false)
		{
			System.out.println("importDBData() 数据库导入失败");
			Log.docSysErrorLog("数据库导入失败", rt);
			writeJson(rt, response);
			return;			
		}
		writeJson(rt, response);
	}

	@RequestMapping("/getSystemInfo.do")
	public void getSystemInfo(String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getSystemInfo.do ***********************");

		System.out.println("getSystemInfo()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		String version = FileUtil.readDocContentFromFile(docSysWebPath, "version");
		if(version == null)
		{
			version = "";
		}
		
		String tomcatPath = getTomcatPath();
		String javaHome = getJavaHome();
		String officeEditorApi = Path.getOfficeEditorApi();
		String defaultReposStorePath = Path.getDefaultReposRootPath(OSType);
		String ldapConfig = getLdapConfig();

		JSONObject config = new JSONObject();
		config.put("docSysType", docSysType);
		config.put("isSalesServer", isSalesServer);
		
		config.put("version", version);
		config.put("tomcatPath", tomcatPath);
		config.put("javaHome", javaHome);
		config.put("ldapConfig", ldapConfig);
		
		if(docSysType < 1)
		{
			String openOfficePath = getOpenOfficePath();
			config.put("openOfficePath", openOfficePath);
		}
		config.put("officeEditorApi", officeEditorApi);
		config.put("defaultReposStorePath", defaultReposStorePath);
		rt.setData(config);
		writeJson(rt, response);
	}

	@RequestMapping("/getSystemLicenses.do")
	public void getSystemLicenses(String authCode, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getSystemLicenses.do ***********************");
		
		System.out.println("getSystemLicenses()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		JSONObject systemLicense = new JSONObject();
		systemLicense.put("type", systemLicenseInfo.type);
		systemLicense.put("usersCount", systemLicenseInfo.usersCount);
		systemLicense.put("expireTime", systemLicenseInfo.expireTime);
		systemLicense.put("hasLicense", systemLicenseInfo.hasLicense);
		systemLicense.put("customer", systemLicenseInfo.customer);
		systemLicense.put("createTime", systemLicenseInfo.createTime);
		
		JSONObject officeLicense = new JSONObject();
		officeLicense.put("mode", officeLicenseInfo.mode);
		officeLicense.put("usersCount", officeLicenseInfo.usersCount);
		officeLicense.put("connections", officeLicenseInfo.connections);
		officeLicense.put("hasLicense", officeLicenseInfo.hasLicense);
		
		
		JSONObject licenses = new JSONObject();
		licenses.put("docSysType", docSysType);
		licenses.put("systemLicense", systemLicense);
		licenses.put("officeLicense", officeLicense);
		rt.setData(licenses);
		writeJson(rt, response);
	}
	
	/********** 设置系统数据库配置 ***************/
	@RequestMapping("/setSystemInfo.do")
	public void setSystemInfo(String authCode, 
			String tomcatPath, 
			String javaHome,
			String openOfficePath, 
			String officeEditorApi,
			String defaultReposStorePath,
			String ldapConfig,
			HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** setSystemInfo.do ***********************");

		System.out.println("setSystemInfo() tomcatPath:" + tomcatPath + " javaHome:" + javaHome 
				+ " openOfficePath:" + openOfficePath + " officeEditorApi:" + officeEditorApi 
				+ " defaultReposStorePath:" + defaultReposStorePath 
				+ " ldapConfig:" + ldapConfig);
		
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		if(tomcatPath == null && openOfficePath == null && javaHome == null && ldapConfig == null)
		{
			Log.docSysErrorLog("没有参数改动，请重新设置！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//checkAndAdd docSys.ini Dir
		if(checkAndAddDocSysIniDir())
		{
			Log.docSysErrorLog("系统初始化目录创建失败！", rt);
			writeJson(rt, response);			
			return;
		}
		
		String docSystemConfigPath = docSysWebPath + "WEB-INF/classes/";
		String tmpDocSystemConfigPath = docSysIniPath;
		String configFileName = "docSysConfig.properties";
		if(FileUtil.copyFile(docSystemConfigPath + configFileName, tmpDocSystemConfigPath + configFileName, true) == false)
		{
			//Failed to copy 
			System.out.println("setSystemDBConfig() Failed to copy " + docSystemConfigPath + configFileName + " to " + tmpDocSystemConfigPath + configFileName);
			Log.docSysErrorLog("创建临时配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}
		
		if(tomcatPath != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "tomcatPath", tomcatPath);
		}
		if(javaHome != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "javaHome", javaHome);
		}
		if(openOfficePath != null)
		{
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "openOfficePath", openOfficePath);
		}
		
		if(officeEditorApi != null)
		{
			officeEditorApi.replace("\\", "/");
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "officeEditorApi", officeEditorApi);
			setOfficeEditor(officeEditorApi);
		}
		
		if(defaultReposStorePath != null)
		{
			defaultReposStorePath = Path.localDirPathFormat(defaultReposStorePath, OSType);
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "defaultReposStorePath", defaultReposStorePath);
		}
		
		if(ldapConfig != null)
		{
			if(docSysType != 1)
			{
				Log.docSysErrorLog("系统不支持LDAP登录认证，请购买企业版证书！", rt);
				writeJson(rt, response);
				return;
			}
			ReadProperties.setValue(tmpDocSystemConfigPath + configFileName, "ldapConfig", ldapConfig);
		}
				
		if(FileUtil.copyFile(tmpDocSystemConfigPath + configFileName, docSystemConfigPath + configFileName, true) == false)
		{
			System.out.println("setSystemDBConfig() Failed to copy " + tmpDocSystemConfigPath + configFileName + " to " + docSystemConfigPath + configFileName);
			Log.docSysErrorLog("写入配置文件失败！", rt);
			writeJson(rt, response);
			return;
		}
				
		writeJson(rt, response);
		
		if(ldapConfig != null)
		{
			applySystemLdapConfig(ldapConfig);			
		}
	}

	@RequestMapping("/upgradeSystem.do")
	public void upgradeSystem(MultipartFile uploadFile, String authCode, 
			HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		System.out.println("\n****************** upgradeSystem.do ***********************");

		System.out.println("upgradeSystem()");
		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		//FileUtil.saveFile to tmpPath
		if(uploadFile == null)
		{
			System.out.println("upgradeSystem() uploadFile is null");
			Log.docSysErrorLog("上传文件为空", rt);
			writeJson(rt, response);
			return;
		}
		String fileName = uploadFile.getOriginalFilename();
		if(!fileName.equals("DocSystem.war"))
		{
			System.out.println("upgradeSystem() 非法升级文件");
			Log.docSysErrorLog("非法升级文件:" + fileName, rt);
			writeJson(rt, response);
			return;
		}
		
		if(FileUtil.saveFile(uploadFile, docSysIniPath, fileName) == null)
		{
			System.out.println("upgradeSystem() 保存升级文件失败");
			Log.docSysErrorLog("保存升级文件失败", rt);
			writeJson(rt, response);
			return;
		}
		
		//开始解压
		if(unZip(docSysIniPath + fileName, docSysIniPath + "DocSystem/") == false)
		{
			System.out.println("upgradeSystem() 解压失败");
			Log.docSysErrorLog("升级包解压失败", rt);
			writeJson(rt, response);
			return;
		}
		
		//开始升级
		if(upgradeServer(rt) == false)
		{
			System.out.println("upgradeSystem() 升级系统失败");
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
	}
	
	@RequestMapping("/restartServer.do")
	public void restartServer(String authCode, String tomcatPath, String javaHome,
			HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		System.out.println("\n****************** restartServer.do ***********************");

		ReturnAjax rt = new ReturnAjax();
		if(superAdminAccessCheck(authCode, "docSysInit", session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}		
		
		//开始重启
		if(restartServer(rt) == false)
		{
			System.out.println("restartServer() 重启服务失败");
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
	}
	/********** 获取用户列表 ***************/
	@RequestMapping("/getUserList.do")
	public void getUserList(String userName, Integer pageIndex, Integer pageSize, HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getUserList.do ***********************");

		System.out.println("getUserList() searchWord:" + userName + " pageIndex:" + pageIndex + " pageSize:" + pageSize);
		ReturnAjax rt = new ReturnAjax();
		if(adminAccessCheck(null, null, session, rt) == false)
		{
			writeJson(rt, response);			
			return;
		}
		
		User user = null;
		if(userName != null && !userName.isEmpty())
		{
			user = new User();
			user.setName(userName);
			user.setRealName(userName);
			user.setNickName(userName);
			user.setEmail(userName);
			user.setTel(userName);
		}
		
		//List <User> UserList = userService.geAllUsers();
		QueryResult queryResult = new QueryResult();
		List <User> UserList = getUserListOnPage(user, pageIndex, pageSize, queryResult);
		
		rt.setData(UserList);
		rt.setDataEx(queryResult.total);
		writeJson(rt, response);
	}
	
	@RequestMapping(value="addFirstAdminUser")
	public void addFirstAdminUser(User user, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** addFirstAdminUser.do ***********************");

		System.out.println("addFirstAdminUser");
		ReturnAjax rt = new ReturnAjax();
		
		//查询系统中是否存在超级管理员
		if(isFirstAdminUserExists() == true)
		{
			Log.docSysErrorLog("系统管理员已存在!", rt);
			writeJson(rt, response);	
			return;
		}
		
		String name = user.getName();
		if(name == null || "".equals(name))
		{
			Log.docSysErrorLog("用户名不能为空！", rt);
			writeJson(rt, response);	
			return;
		}
		
		String pwd = user.getPwd();
		if(pwd == null || "".equals(pwd))
		{
			Log.docSysErrorLog("密码不能为空！", rt);
			writeJson(rt, response);	
			return;
		}
		
		if(userCheck(user, true, true, rt) == false)
		{
			System.out.println("用户检查失败!");			
			writeJson(rt, response);
			return;			
		}
		
		user.setCreateType(0);	//用户为首次添加
		user.setType(2); //系统管理员
		//set createTime
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String createTime = df.format(new Date());// new Date()为获取当前系统时间
		user.setCreateTime(createTime);	//设置创建时间

		if(userService.addUser(user) == 0)
		{
			Log.docSysErrorLog("Failed to add new User in DB", rt);
		}
		
		writeJson(rt, response);
		return;
	}

	@RequestMapping(value="addUser")
	public void addUser(User user, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** addUser.do ***********************");

		System.out.println("addUser");
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}

		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		String userName = user.getName();
		String pwd = user.getPwd();
		Integer type = user.getType();

		System.out.println("userName:"+userName + " pwd:"+pwd + "type:" + type);
		
		//检查是否越权设置
		if(type > login_user.getType())
		{
			Log.docSysErrorLog("danger#越权操作！", rt);
			writeJson(rt, response);
			return;
		}
		
		//检查用户名是否为空
		if(userName ==null||"".equals(userName))
		{
			Log.docSysErrorLog("danger#账号不能为空！", rt);
			writeJson(rt, response);
			return;
		}
		
		if(pwd == null || "".equals(pwd))
		{
			Log.docSysErrorLog("密码不能为空！", rt);
			writeJson(rt, response);	
			return;
		}
		
		if(checkSystemUsersCount(rt) == false)
		{
			writeJson(rt, response);	
			return;			
		}
		
		if(userCheck(user, true, true, rt) == false)
		{
			System.out.println("用户检查失败!");			
			writeJson(rt, response);
			return;			
		}
				
		user.setCreateType(2);	//用户为管理员添加
		//set createTime
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String createTime = df.format(new Date());// new Date()为获取当前系统时间
		user.setCreateTime(createTime);	//设置川剧时间

		if(userService.addUser(user) == 0)
		{
			Log.docSysErrorLog("Failed to add new User in DB", rt);
		}
		
		writeJson(rt, response);
		return;
	}

	@RequestMapping(value="editUser")
	public void editUser(User user, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** editUser.do ***********************");

		Log.printObject("editUser inputUser:", user);
		
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		Log.printObject("editUser login_user:", login_user);
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		Integer userId = user.getId();
		if(userId == null)
		{
			Log.docSysErrorLog("用户ID不能为空", rt);
			writeJson(rt, response);
			return;
		}
		
		String userName = user.getName();
		Integer type = user.getType();
		String pwd = user.getPwd();
		
		System.out.println("userId:" + userId + " userName:"+userName + "type:" + type  + " pwd:" + pwd);
		
		//检查是否越权设置
		if(login_user.getType() < type)
		{
			Log.docSysErrorLog("越权操作：您无权设置该用户等级！", rt);
			writeJson(rt, response);
			return;
		}
		
		//注意：两个Integer类型==是地址比较
		//但Integer把-128到127（可调）的整数都提前实例化了， 所以你不管创建多少个这个范围内的Integer都是同一个对象。
		//所以在数字大于127时用==会出现逻辑错误问题
		if(!userId.equals(login_user.getId()))
		{
			//不得修改同级别或高级别用户的信息
			User tempUser  = userService.getUser(userId);
			if(tempUser == null)
			{
				Log.docSysErrorLog("用户不存在！", rt);
				writeJson(rt, response);
				return;	
			}

			if(login_user.getType() < tempUser.getType())
			{
				Log.docSysErrorLog("越权操作：您无权修改高级别用户的设置！", rt);
				writeJson(rt, response);
				return;			
			}

			if(login_user.getType().equals(tempUser.getType()))
			{
				Log.docSysErrorLog("越权操作：您无权修改同级别用户的设置！", rt);
				writeJson(rt, response);
				return;			
			}
			
			//检查用户名是否有改动
			if(user.getName() != null)
			{
				if(tempUser.getName() == null || user.getName().equals(tempUser.getName()))
				{
					user.setName(null);
				}
			}
			
			//检查Tel是否改动
			if(user.getTel() != null)
			{
				if(tempUser.getTel() == null || user.getTel().equals(tempUser.getTel()))
				{
					user.setTel(null);
				}
			}

			//检查Email是否改动
			if(user.getEmail() != null)
			{
				if(tempUser.getEmail() == null || user.getEmail().equals(tempUser.getEmail()))
				{
					user.setEmail(null);
				}
			}
		}
		
		if(userEditCheck(user, rt) == false)
		{
			System.out.println("用户检查失败!");			
			writeJson(rt, response);
			return;			
		}
		
		if(userService.editUser(user) == 0)
		{
			Log.docSysErrorLog("更新数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
		return;
	}
	
	@RequestMapping(value="resetPwd")
	public void resetPwd(User user, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** resetPwd.do ***********************");

		System.out.println("resetPwd");
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 2)
		{
			Log.docSysErrorLog("您无权进行此操作！", rt);
			writeJson(rt, response);
			return;			
		}
		
		Integer userId = user.getId();
		String pwd = user.getPwd();
		
		System.out.println("userId:" +userId + " pwd:" + pwd);
	
		if(userId == null)
		{
			Log.docSysErrorLog("用户ID不能为空", rt);
			writeJson(rt, response);
			return;
		}
		
		//不得修改同级别或高级别用户的密码
		if(!userId.equals(login_user.getId()))
		{
			User tempUser  = userService.getUser(userId);
			if(tempUser.getType() > login_user.getType())
			{
				Log.docSysErrorLog("越权操作：您无权修改高级别用户的密码！", rt);
				writeJson(rt, response);
				return;			
			}
			
			if(tempUser.getType().equals(login_user.getType()))
			{
				Log.docSysErrorLog("越权操作：您无权修改同级别用户的密码！", rt);
				writeJson(rt, response);
				return;			
			}
		}	
				
		
		if(userService.editUser(user) == 0)
		{
			Log.docSysErrorLog("更新数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
		return;
	}

	
	@RequestMapping(value="delUser")
	public void delUser(Integer userId, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** delUser.do ***********************");

		System.out.println("delUser " + userId);
		
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 2)
		{
			Log.docSysErrorLog("您无权进行此操作！", rt);
			writeJson(rt, response);
			return;			
		}
		
		if(userId == null)
		{
			Log.docSysErrorLog("用户ID不能为空", rt);
			writeJson(rt, response);
			return;
		}
		
		if(!userId.equals(login_user.getId()))
		{
			//不得删除同级别或高级别用户
			User tempUser  = userService.getUser(userId);
			if(tempUser == null)
			{
				Log.docSysErrorLog("用户不存在！", rt);
				writeJson(rt, response);
				return;	
			}

			if(tempUser.getType() > login_user.getType())
			{
				Log.docSysErrorLog("越权操作：您无权删除高级别用户！", rt);
				writeJson(rt, response);
				return;			
			}
	
			if(tempUser.getType().equals(login_user.getType()))
			{
				Log.docSysErrorLog("越权操作：您无权删除同级别用户！", rt);
				writeJson(rt, response);
				return;			
			}		
		}
		
		if(userService.delUser(userId) == 0)
		{
			Log.docSysErrorLog("更新数据库失败", rt);
			writeJson(rt, response);
			return;		
		}
		else
		{
			//Delete all related ReposAuth \ DocAuth \GroupMember Infos
			DocAuth docAuth = new DocAuth();
			docAuth.setUserId(userId);
			reposService.deleteDocAuthSelective(docAuth);

			ReposAuth reposAuth = new ReposAuth();
			reposAuth.setUserId(userId);
			reposService.deleteReposAuthSelective(reposAuth);
			
			GroupMember groupMember = new GroupMember();
			groupMember.setUserId(userId);
			userService.deleteGroupMemberSelective(groupMember);
		}
		
		writeJson(rt, response);
		return;		
	}
	
	/********** 获取仓库列表 ***************/
	@RequestMapping("/getReposList.do")
	public void getReposList(HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getReposList.do ***********************");

		System.out.println("getReposList()");
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		List <Repos> list = getAllReposes();
		
		rt.setData(list);
		writeJson(rt, response);
	}

	private List<Repos> getAllReposes() {
		List <Repos> list = userService.geAllReposes();
		return list;
	}
	
	/********** 获取系统所有用户 ：前台用于给repos添加访问用户，返回的结果实际上是reposMember列表***************/
	@RequestMapping("/getReposAllUsers.do")
	public void getReposAllUsers(String searchWord, Integer reposId,HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getReposAllUsers.do ***********************");
		
		System.out.println("getReposAllUsers reposId: " + reposId);
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		
		User user = null;
		if(searchWord != null && !searchWord.isEmpty())
		{
			user = new User();
			user.setName(searchWord);
			user.setRealName(searchWord);
			user.setNickName(searchWord);
			user.setEmail(searchWord);
			user.setTel(searchWord);
		}
		
		List<ReposMember> UserList = null;
		if(user == null)
		{
			UserList = userService.getReposAllUsers(reposId);
		}
		else
		{
			HashMap<String, String> param = buildQueryParamForObj(user, null, null);
			param.put("reposId", reposId+"");
			UserList = userService.queryReposMemberWithParamLike(param);				
		}
		Log.printObject("UserList:",UserList);
				
		rt.setData(UserList);
		writeJson(rt, response);
	}
	
	/********** 获取用户组列表 ***************/
	@RequestMapping("/getGroupList.do")
	public void getGroupList(HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getGroupList.do ***********************");
		
		System.out.println("getGroupList()");
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//获取All UserList
		List <UserGroup> GroupList = getAllGroups();
		
		rt.setData(GroupList);
		writeJson(rt, response);
	}

	private List<UserGroup> getAllGroups() {
		List <UserGroup> GroupList = userService.geAllGroups();
		return GroupList;
	}
	
	@RequestMapping(value="addGroup")
	public void addGroup(UserGroup group, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** addGroup.do ***********************");

		System.out.println("addGroup");
		String name = group.getName();
		String info = group.getInfo();
		
		System.out.println("name:"+name + " info:"+info);
		ReturnAjax rt = new ReturnAjax();
		
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//检查用户名是否为空
		if(name ==null||"".equals(name))
		{
			Log.docSysErrorLog("组名不能为空！", rt);
			writeJson(rt, response);
			return;
		}
		
		if(isGroupExist(name) == true)
		{
			Log.docSysErrorLog("用户组 " + name + " 已存在！", rt);
			writeJson(rt, response);
			return;
		}
	
		//set createTime
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String createTime = df.format(new Date());// new Date()为获取当前系统时间
		group.setCreateTime(createTime);	//设置川剧时间

		if(userService.addGroup(group) == 0)
		{
			Log.docSysErrorLog("Failed to add new Group in DB", rt);
		}
		
		writeJson(rt, response);
		return;
	}

	private boolean isGroupExist(String name) {
		UserGroup qGroup = new UserGroup();
		//检查用户名是否为空
		if(name==null||"".equals(name))
		{
			return true;
		}
			
		qGroup.setName(name);
		List<UserGroup> list = userService.getGroupListByGroupInfo(qGroup);
		if(list == null || list.size() == 0)
		{
			return false;
		}
		return true;
	}
	
	@RequestMapping(value="delGroup")
	public void delGroup(Integer id, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** delGroup.do ***********************");

		System.out.println("delGroup " + id);
		
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(userService.delGroup(id) == 0)
		{
			Log.docSysErrorLog("Failed to delete Group from DB", rt);
			writeJson(rt, response);
			return;		
		}
		else
		{
			//Delete all related ReposAuth \ DocAuth \GroupMember Infos
			DocAuth docAuth = new DocAuth();
			docAuth.setGroupId(id);
			reposService.deleteDocAuthSelective(docAuth);
	
			ReposAuth reposAuth = new ReposAuth();
			reposAuth.setGroupId(id);
			reposService.deleteReposAuthSelective(reposAuth);
			
			GroupMember groupMember = new GroupMember();
			groupMember.setGroupId(id);
			userService.deleteGroupMemberSelective(groupMember);
		}
		
		writeJson(rt, response);
		return;		
	}
	
	@RequestMapping(value="editGroup")
	public void editGroup(UserGroup group, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** editGroup.do ***********************");

		System.out.println("editGroup");

		Integer groupId = group.getId();
		String name = group.getName();
		String info = group.getInfo();

		System.out.println("name:"+name + " info:"+info);
	
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(groupId == null)
		{
			Log.docSysErrorLog("用户组ID不能为空", rt);
			writeJson(rt, response);
			return;
		}
		
		if(userService.editGroup(group) == 0)
		{
			Log.docSysErrorLog("更新数据库失败", rt);
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
		return;
	}
	
	/********** 获取系统所有用户 ：前台用于给group添加访问用户，返回的结果实际上是groupMember列表***************/
	@RequestMapping("/getGroupAllUsers.do")
	public void getGroupAllUsers(String searchWord, Integer groupId,HttpSession session,HttpServletRequest request,HttpServletResponse response)
	{
		System.out.println("\n****************** getGroupAllUsers.do ***********************");

		System.out.println("getGroupAllUsers searchWord:" + searchWord + " groupId: " + groupId);
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		User user = null;
		if(searchWord != null && !searchWord.isEmpty())
		{
			user = new User();
			user.setName(searchWord);
			user.setRealName(searchWord);
			user.setNickName(searchWord);
			user.setEmail(searchWord);
			user.setTel(searchWord);
		}
		
		List <GroupMember> UserList = null;
		if(user == null)
		{
			UserList = userService.getGroupAllUsers(groupId);	
		}
		else
		{
			HashMap<String, String> param = buildQueryParamForObj(user, null, null);
			param.put("groupId",groupId+"");
			UserList = userService.queryGroupMemberWithParamLike(param);				
		}
		Log.printObject("UserList:",UserList);
		
		rt.setData(UserList);
		writeJson(rt, response);
	}
	
	@RequestMapping(value="addGroupMember")
	public void addGroupMember(Integer groupId,Integer userId, HttpSession session,HttpServletResponse response)
	{
		System.out.println("\n****************** addGroupMember.do ***********************");

		System.out.println("addGroupMember groupId:" + groupId + " userId:" + userId);
		
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		//检查GroupId是否为空
		if(groupId == null)
		{
			Log.docSysErrorLog("组ID不能为空！", rt);
			writeJson(rt, response);
			return;
		}
		
		//检查用户ID是否为空
		if(userId == null)
		{
			Log.docSysErrorLog("用户ID不能为空！", rt);
			writeJson(rt, response);
			return;
		}
		GroupMember groupMember = new GroupMember();
		groupMember.setGroupId(groupId);
		groupMember.setUserId(userId);
		
		if(isGroupMemberExist(groupMember) == true)
		{
			System.out.println("addGroupMember() 用户 " + userId + " 已是该组成员！");
			Log.docSysErrorLog("用户 " + userId + " 已是该组成员！", rt);
			writeJson(rt, response);
			return;
		}
	
		if(userService.addGroupMember(groupMember) == 0)
		{
			System.out.println("addGroupMember() Failed to add groupMember");
			Log.docSysErrorLog("Failed to add new GroupMember in DB", rt);
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
		return;
	}

	private boolean isGroupMemberExist(GroupMember groupMember) {
		List<UserGroup> list = userService.getGroupMemberListByGroupMemberInfo(groupMember);
		if(list == null || list.size() == 0)
		{
			return false;
		}
		return true;
	}
	
	@RequestMapping(value="delGroupMember")
	public void delGroupMember(Integer id, HttpSession session,HttpServletResponse response)
	{
		System.out.println("delGroupMember " + id);
		
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(userService.delGroupMember(id) == 0)
		{
			Log.docSysErrorLog("Failed to delete GroupMember from DB", rt);
		}
		
		writeJson(rt, response);
		return;		
	}
	
	@RequestMapping(value="getSystemLog")
	public void getSystemLog(String fileName,HttpSession session,HttpServletRequest request,HttpServletResponse response) throws Exception{

		System.out.println("\n****************** getSystemLog.do ***********************");

		System.out.println("getSystemLog: " + fileName);
		ReturnAjax rt = new ReturnAjax();
		User login_user = (User) session.getAttribute("login_user");
		if(login_user == null)
		{
			Log.docSysErrorLog("用户未登录，请先登录！", rt);
			writeJson(rt, response);			
			return;
		}
		
		if(login_user.getType() < 1)
		{
			Log.docSysErrorLog("非管理员用户，请联系统管理员！", rt);
			writeJson(rt, response);			
			return;
		}
		
		String localParentPath = Path.getSystemLogParentPath(OSType);
		if(fileName == null || "".equals(fileName))
		{
			fileName = Path.getSystemLogFileName();
		}
		
		sendTargetToWebPage(localParentPath,fileName, localParentPath, rt, response, request, false, null);
		return;		
	}
	

}
