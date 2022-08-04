package com.DocSystem.common.entity;

import java.util.concurrent.ConcurrentHashMap;

public class RemoteStorageConfig {

	public String protocol;
	public String rootPath;	//remote root path
	public Integer autoPull = 0;
	public Integer autoPullForce = 0;
	public Integer autoPush = 0;
	public Integer autoPushForce = 0;
	public LocalConfig FILE;
	public SftpConfig SFTP;
	public FtpConfig FTP;
	public SmbConfig SMB = null;
	public SvnConfig SVN = null;
	public GitConfig GIT = null;
	public boolean isVerRepos = false;
	public MxsDocConfig MXSDOC;
	public String remoteStorageIndexLib;
	
	public Long allowedMaxFile = null;
	public ConcurrentHashMap<String, Integer> notAllowedFileHashMap = null;
	public ConcurrentHashMap<String, Integer> allowedFileTypeHashMap = null;
	public ConcurrentHashMap<String, Integer> notAllowedFileTypeHashMap = null;
	
	public ConcurrentHashMap<String, Integer> ignoreHashMap = null;
	
}
