package com.DocSystem.common;

import java.io.File;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.DocSystem.entity.Doc;
import com.DocSystem.entity.Repos;
import com.DocSystem.entity.User;

import util.ReadProperties;
import util.Encrypt.MD5;

public class Path {	
	public static String getDocSysWebParentPath(String localPath) {
		int pos = localPath.indexOf("/DocSystem");
		return localPath.substring(0, pos+1);
	}
	
	//path必须是标准格式
	public static int getLevelByParentPath(String path) 
	{
		if(path == null || path.isEmpty())
		{
			return 0;
		}
		
		String [] paths = path.split("/");
		return paths.length;
	}

	public static int seperatePathAndName(String entryPath, String [] result) {
		if(entryPath.isEmpty())
		{
			//It it rootDoc
			return -1;
		}
		
		String [] paths = entryPath.split("/");
		
		int deepth = paths.length;
		//System.out.println("seperatePathAndName() deepth:" + deepth); 
		
		String  path = "";
		String name = "";
		
		//Get Name and pathEndPos
		int pathEndPos = 0;
		for(int i=deepth-1; i>=0; i--)
		{
			name = paths[i];
			if(name.isEmpty())
			{
				continue;
			}
			pathEndPos = i;
			break;
		}
		
		//Get Path
		for(int i=0; i<pathEndPos; i++)
		{
			String tempName = paths[i];
			if(tempName.isEmpty())
			{
				continue;
			}	
			
			path = path + tempName + "/";
		}
		
		result[0] = path;
		result[1] = name;

		int level = paths.length -1;
		return level;
	}
	
	//正确格式化仓库根路径
	public static String dirPathFormat(String path) {
		//如果传入的Path没有带/,给他加一个
		if(path.isEmpty())
		{
			return path;
		}
		
		String endChar = path.substring(path.length()-1, path.length());
		if(!endChar.equals("/"))	
		{
			path = path + "/";
		}
		return path;
	}

	//格式化本地路径
	public static String localDirPathFormat(String path, Integer OSType) {
		if(path == null || path.isEmpty())
		{
			return path;
		}

		path = path.replace('\\','/');
		
		String [] paths = path.split("/");
		
		char startChar = path.charAt(0);
		if(startChar == '/')	
		{
			if(OS.isWinOS(OSType))
			{
				path = "C:/" + buildPath(paths);
			}
			else
			{
				path = "/" + buildPath(paths);
			}
		}
		else
		{
			if(OS.isWinOS(OSType))
			{
				if(OS.isWinDiskStr(paths[0]))
				{
					paths[0] = paths[0].toUpperCase();
					path = buildPath(paths);					
				}
				else
				{
					path = "C:/" + buildPath(paths);
				}
			}
			else
			{
				path = "/" + buildPath(paths);
			}
		}	

		return path;
	}

	private static String buildPath(String[] paths) {
		String path = "";
		for(int i=0; i<paths.length; i++)
		{
			String subPath = paths[i];
			if(!subPath.isEmpty())
			{
				path = path + subPath + "/";
			}
		}
		return path;
	}
	
	//系统日志所在的目录
	protected String getSystemLogParentPath(Integer OSType) {
		String path = "";		
		path = ReadProperties.read("docSysConfig.properties", "SystemLogParentPath");
	    if(path == null || "".equals(path))
	    {
			if(OS.isWinOS(OSType)){  
				path = "C:/xampp/tomcat/logs/";
			}
			else
			{
				path = "/var/lib/tomcat7/logs/";	//Linux系统放在  /data	
			}
	    }    
		return path;
	}
	
	
	public static String getReposTmpPathForDoc(Repos repos, Doc doc) {
		String tmpDir = repos.getPath() + repos.getId() +  "/tmp/doc/" +  doc.getDocId() + "_" + doc.getName() + "/";
		FileUtil.createDir(tmpDir);
		return tmpDir;
	}
	
	public static String getReposTmpPathForPreview(Repos repos, Doc doc) {
		String docLocalPath = doc.getLocalRootPath() + doc.getPath() + doc.getName();
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/preview/" +  docLocalPath.hashCode() + "_" + doc.getName() + "/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	public static String getReposTmpPathForTextEdit(Repos repos, User login_user, boolean isRealDoc) {
		if(isRealDoc)
		{
			String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/TextEdit/" + login_user.getId() + "/RDOC/";
			FileUtil.createDir(userTmpDir);
		}
		
		//VDoc
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/TextEdit/" + login_user.getId() + "/VDOC/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	public static String getReposTmpPathForDownload(Repos repos, User login_user) {
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/download/" + login_user.getId() + "/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	public static String getReposTmpPathForUpload(Repos repos, User login_user) {
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/upload/" + login_user.getId() + "/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	//根据路径来获取上层路径 
	protected static String getParentPath(String path) {
		if(path == null || path.length() < 2)
		{
			System.out.println("getParentPath() failed to get parentPath for path:" + path);
			return null;
		}
		
		//反向查找 "/"
		int pos = path.lastIndexOf("/", path.length() - 2);
		if(pos == -1)
		{
			System.out.println("getParentPath() failed to get parentPath for path:" + path);
			return null;
		}
		String parentPath = path.substring(0, pos+1);
        System.out.println("getParentPath() parentPath:" + parentPath);
		return parentPath;
	}
	
	public static String getParentPath(String path, int n, Integer OSType) {
		path = localDirPathFormat(path, OSType); //首先对路径进行统一格式化
		
		for(int i=0; i<n; i++)
		{
			path = getParentPath(path);
		}
		
		return path;
	}
	
	//WebPath was 
	public static String getWebPath(Integer OSType) {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        String webPath =  wac.getServletContext().getRealPath("/");
        webPath = localDirPathFormat(webPath, OSType);
        System.out.println("getWebPath() webPath:" + webPath);
		return webPath;
	}
	
	//获取本地仓库默认存储位置（相对于仓库的存储路径）
	public static String getDefaultLocalVerReposPath(String path) {
		String localSvnPath = path + "DocSysVerReposes/";
		return localSvnPath;
	}
	
	protected String getDocPath(Doc doc) 
	{
		String path = doc.getPath();
		if(path == null)
		{
			return doc.getName();
		}

		return path + doc.getName();
	}
	
	//获取默认的仓库根路径
	public static String getDefaultReposRootPath(Integer OSType) {
		String path = ReadProperties.read("docSysConfig.properties", "defaultReposStorePath");
		if(OS.isWinOS(OSType))
		{
			if(path == null || path.isEmpty())
			{
				path = "C:/DocSysReposes/";
			}
			else
			{
				path = localDirPathFormat(path, OSType);
			}
	    }	
		else
		{
			if(path == null || path.isEmpty())
			{
				path = "/DocSysReposes/";
			}
			else
			{
				path = localDirPathFormat(path, OSType);
			}
		}	    
		return path;
	}

	
	public static String getReposPath(Repos repos) {
		String path = repos.getPath();
		return path + repos.getId() + "/";
	}

	//获取仓库的实文件的本地存储根路径
	public static String getReposRealPath(Repos repos)
	{
		String reposRPath =  repos.getRealDocPath();
		if(reposRPath == null || reposRPath.isEmpty())
		{
			reposRPath = getReposPath(repos) + "data/rdata/";	//实文件系统的存储数据放在data目录下 
		}
		//System.out.println("getReposRealPath() " + reposRPath);
		return reposRPath;
	}
	
	//获取仓库的虚拟文件的本地存储根路径
	public static String getReposVirtualPath(Repos repos)
	{
		String reposVPath = getReposPath(repos) + "data/vdata/";	//实文件系统的存储数据放在data目录下 
		//System.out.println("getReposVirtualPath() " + reposVPath);
		return reposVPath;
	}
	
	//获取仓库的密码文件的存储路径
	public static String getReposPwdPath(Repos repos)
	{
		String reposPwdPath = getReposPath(repos) + "data/pwd/";	//实文件系统的存储数据放在data目录下 
		//System.out.println("getReposPwdPath() " + reposPwdPath);
		return reposPwdPath;
	}
	
	//仓库文件缓存根目录
	protected String getReposTmpPath(Repos repos) {
		String tmpDir = repos.getPath() + repos.getId() +  "/tmp/";
		return tmpDir;
	}
	
	
	//历史文件缓存目录，需要区分RDoc和VDoc
	public static String getReposTmpPathForHistory(Repos repos, String commitId, boolean isRealDoc) {
		if(isRealDoc)
		{	
			String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/History/rdata/" + commitId + "/";
			FileUtil.createDir(userTmpDir);
			return userTmpDir;
		}
		
		//is VDoc
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/History/vdata/" + commitId + "/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	//压缩文件解压缓存目录
	public static String getReposTmpPathForUnzip(Repos repos, User login_user) {
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/Unzip/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	//用户的仓库临时目录
	public static String getReposTmpPathForUser(Repos repos, User login_user) {
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/User/" + login_user.getId() + "/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}

	public static String getReposTmpPathForOfficeText(Repos repos, Doc doc) {
		String docLocalPath = doc.getLocalRootPath() + doc.getPath() + doc.getName();
		String userTmpDir = repos.getPath() + repos.getId() +  "/tmp/OfficeText/" + docLocalPath.hashCode() + "_" + doc.getName() + "/";
		FileUtil.createDir(userTmpDir);
		return userTmpDir;
	}
	
	
	//获取OfficeEditorApi的配置
	public static String getOfficeEditorApi() {
		String officeEditorApi = ReadProperties.read("docSysConfig.properties", "officeEditorApi");
		if(officeEditorApi != null && !officeEditorApi.isEmpty())
		{
			return officeEditorApi.replace("\\", "/");
		}
		return officeEditorApi;
	}
	
	//系统日志的名字，可以是目录或文件
	protected String getSystemLogFileName() {
		String name = "";
		
		name = ReadProperties.read("docSysConfig.properties", "SystemLogFileName");
	    if(name == null || "".equals(name))
	    {
			name = "catalina.log";
	    }	    
		return name;
	}
	
	public static String getVDocName(Doc doc) 
	{
		return doc.getDocId() + "_" + doc.getName();
	}
	
	protected static String getHashId(String path) 
	{
		String hashId = MD5.md5(path);
		System.out.println("getHashId() " + hashId + " for " + path);
		return hashId;
	}

	public static String getOfficeTextFileName(Doc doc) {
		File file = new File(doc.getLocalRootPath() + doc.getPath() + doc.getName());
		return "officeText_" + file.length() + "_" + file.lastModified() + ".txt";
	}
	
	public static String getPreviewFileName(Doc doc) {
		File file = new File(doc.getLocalRootPath() + doc.getPath() + doc.getName());
		return "preview_" + file.length() + "_" + file.lastModified() + ".pdf";
	}
	
	protected String getLocalVerReposURI(Repos repos, boolean isRealDoc) {
		String localVerReposURI = null;

		Integer verCtrl = null;
		String localSvnPath = null;

		if(isRealDoc)
		{
			verCtrl = repos.getVerCtrl();
			localSvnPath = repos.getLocalSvnPath();
		}
		else
		{
			verCtrl = repos.getVerCtrl1();
			localSvnPath = repos.getLocalSvnPath1();
		}	

		String reposName = getVerReposName(repos,isRealDoc);
		
		if(verCtrl == 1)
		{
			localVerReposURI = "file:///" + localSvnPath + reposName;
		}
		else
		{
			localVerReposURI = null;
			
		}
		return localVerReposURI;
	}
	
	public static String getLocalVerReposPath(Repos repos, boolean isRealDoc) {
		String localVerReposPath = null;
		
		String localSvnPath = null;
		if(isRealDoc)
		{
			localSvnPath = repos.getLocalSvnPath();
		}
		else
		{
			localSvnPath = repos.getLocalSvnPath1();
		}	
		
		localSvnPath = dirPathFormat(localSvnPath);

		String reposName = getVerReposName(repos,isRealDoc);
		
		localVerReposPath = localSvnPath + reposName + "/";
		return localVerReposPath;
	}

	public static String getVerReposName(Repos repos,boolean isRealDoc) {
		String reposName = null;
		
		Integer id = repos.getId();
		if(isRealDoc)
		{
			Integer verCtrl = repos.getVerCtrl();
			if(verCtrl == 1)
			{
				reposName = id + "_SVN_RRepos";
			}
			else if(verCtrl == 2)
			{ 
				if(repos.getIsRemote() == 1)
				{
					reposName = id + "_GIT_RRepos_Remote";					
				}
				else
				{

					reposName = id + "_GIT_RRepos";
				}
			}
		}
		else
		{
			Integer verCtrl = repos.getVerCtrl1();			
			if(verCtrl == 1)
			{
				reposName = id + "_SVN_VRepos";
			}
			else if(verCtrl == 2)
			{
				if(repos.getIsRemote1() == 1)
				{

					reposName = id + "_GIT_VRepos_Remote";					
				}
				else
				{
					reposName = id + "_GIT_VRepos";
				}
			}
		}
		return reposName;
	}
	
	//Build DocId by DocName
	public static Long buildDocIdByName(Integer level, String parentPath, String docName) 
	{
		String docPath = parentPath + docName;
		if(docName.isEmpty())
		{
			if(parentPath.isEmpty())
			{
				return 0L;
			}
			
			docPath = parentPath.substring(0, parentPath.length()-1);	//remove the last char '/'
		}
		
		Long docId = level*100000000000L + docPath.hashCode() + 102147483647L;	//为了避免文件重复使用level*100000000 + docName的hashCode
		return docId;
	}		
	
	protected Long buildPidByPath(int level, String path) 
	{
		if(path == null || path.isEmpty())
		{
			return 0L;
		}
		
		char lastChar = path.charAt(path.length()-1);
		if(lastChar == '/')
		{
			path = path.substring(0,path.length()-1);
		}
		
		Long pid = buildDocIdByName(level-1, path, "");
		return pid;
	}
	
}
