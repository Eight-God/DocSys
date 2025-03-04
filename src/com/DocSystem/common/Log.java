package com.DocSystem.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Date;

import com.alibaba.fastjson.JSON;

import util.DateFormat;

public class Log {
	//logLevel
	public final static int debug = 0;
	public final static int info  = 1;
	public final static int warn  = 2;
	public final static int error = 3;

	//logMask
	public final static int allowGeneral = 1;
	public final static int allowOffice  = 2;
	public final static int allowAll  = allowGeneral | allowOffice;
	
	
	public static int logLevel = debug;
	public static int logMask = allowAll;
	public static String logFileConfig = null;	//logFileConfig	
	public static String logFile = null;	//run time
	
	public static boolean isLogEnable(int level, int mask)
	{
		if(level < logLevel)
		{
			return false;
		}
		return (mask & logMask) > 0;
	}
	
	public static void toFile(String content, String filePath) {
		if(filePath != null)
		{
			File file = new File(filePath);
			if(file.exists())
			{
				if(file.length() > 209715200)	//日志文件存在且大于200M则将文件按当前时间备份
				//if(file.length() > 20971520)	//日志文件存在且大于20M则将文件按当前时间备份
				{
					String timeStamp = DateFormat.dateTimeFormat2(new Date());
					File newfile = new File(filePath + "-" + timeStamp);
					file.renameTo(newfile);
				}
			}
			appendContentToFile(filePath, content, "UTF-8");	
		}
	}
	
	//向文件末尾追加内容
    public static boolean appendContentToFile(String filePath, String content, String encode) {
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(filePath, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
			
            byte[] buff;
			if(encode == null)
			{
				buff = content.getBytes();
			}
			else
			{
				buff = content.getBytes(encode); //将String转成指定charset的字节内容
			}
            randomFile.write(buff);
            
            randomFile.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public static void debug(String content) {
		if(isLogEnable(debug, allowGeneral))
		{
			System.out.println(content);

			if(logFile != null)
			{
				toFile(content + "\n", logFile);
			}
		}
	}
	
	public static void debug(Exception e) {
		if(isLogEnable(debug, allowGeneral))
		{
			e.printStackTrace(System.out);
			
			if(logFile != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				toFile(baos.toString(), logFile);
			}
		}
	}
	
	public static void info(String content) {
		if(isLogEnable(info, allowGeneral))
		{
			String timeStamp = DateFormat.dateTimeFormat(new Date());
			System.out.println(timeStamp + " " + content);

			if(logFile != null)
			{
				toFile(timeStamp + " " + content + "\n", logFile);
			}
		}
	}
	
	public static void infoHead(String content) {
		if(isLogEnable(info, allowGeneral))
		{
			String timeStamp = DateFormat.dateTimeFormat(new Date());
			System.out.println("\n" + timeStamp + " " + content);

			if(logFile != null)
			{
				toFile("\n" + timeStamp + " " + content + "\n", logFile);
			}
		}
	}
	
	public static void infoTail(String content) {
		if(isLogEnable(info, allowGeneral))
		{
			String timeStamp = DateFormat.dateTimeFormat(new Date());
			System.out.println(timeStamp + " " + content + "\n");

			if(logFile != null)
			{
				toFile(timeStamp + " " + content + "\n\n", logFile);
			}
		}
	}
	
	public static void info(Exception e) {
		if(isLogEnable(info, allowGeneral))
		{
			e.printStackTrace(System.out);

			if(logFile != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				toFile(baos.toString(), logFile);
			}
		}
	}
	
	public static void warn(String content) {
		if(isLogEnable(warn, allowAll))
		{
			String timeStamp = DateFormat.dateTimeFormat(new Date());
			System.out.println(timeStamp + " [warn] " + content);

			if(logFile != null)
			{
				toFile(timeStamp + " [warn] " + content  + "\n", logFile);
			}
		}
	}
	
	public static void warn(Exception e) {
		if(isLogEnable(warn, allowGeneral))
		{
			e.printStackTrace(System.out);

			if(logFile != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				toFile(baos.toString(), logFile);
			}
		}
	}
	
	public static void error(String content) {
		if(isLogEnable(error, allowAll))
		{
			String timeStamp = DateFormat.dateTimeFormat(new Date());
			System.out.println(timeStamp + " [error] " +content);

			if(logFile != null)
			{
				toFile(timeStamp + " [error] " + content  + "\n", logFile);
			}
		}
	}
	
	public static void error(Exception e) {
		if(isLogEnable(error, allowGeneral))
		{
			e.printStackTrace(System.out);

			if(logFile != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				toFile(baos.toString(), logFile);
			}
		}
	}
	
	public static void error(String content, String defaultLogFile) {
		if(isLogEnable(error, allowAll))
		{
			String timeStamp = DateFormat.dateTimeFormat(new Date());
			System.out.println(timeStamp + " [error] " +content);
			
			String logFilePath = logFile;
			if(logFilePath == null)
			{
				logFilePath = defaultLogFile;
			}
			
			if(logFilePath != null)
			{
				toFile(timeStamp + " [error] " + content  + "\n", logFilePath);
			}
		}
	}
	
	public static void error(Exception e, String defaultLogFile) {
		if(isLogEnable(error, allowGeneral))
		{
			e.printStackTrace(System.out);

			String logFilePath = logFile;
			if(logFilePath == null)
			{
				logFilePath = defaultLogFile;
			}
			
			if(logFilePath != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				toFile(baos.toString(), logFilePath);
			}
		}
	}
	
	public static void info(String Head, String msg) {
		info(Head + " " + msg);
	}
	
	public static void debugForOffice(String content) {
		debug("OFFICE: " + content);
	}

	public static void infoForOffice(String content) {
		info("OFFICE: " + content);
	}
	
	public static void infoForOfficeHead(String content) {
		infoHead("OFFICE: " + content);
	}

	public static void infoForOfficeTail(String content) {
		infoTail("OFFICE: " + content);
	}
	
	public static void printByte(byte data) {
		if(isLogEnable(debug, allowGeneral))
		{
			System.out.printf( "%02X ", data);

			if(logFile != null)
			{
				toFile(String.format("%02X ", data), logFile);
			}
		}
	}
	
	public static void printBytes(byte[] data) {
		if(isLogEnable(debug, allowGeneral))
		{
			for(int i=0; i<data.length; i++)
			{
				System.out.printf( "%02X ", data[i]);
			}

			if(logFile != null)
			{
				for(int i=0; i<data.length; i++)
				{
					toFile(String.format("%02X ", data[i]), logFile);
				}
			}
		}
	}
	
	public static void printBytesEx(String head, byte[] data) {
		if(isLogEnable(debug, allowGeneral))
		{
			System.out.printf(head);
			for(int i=0; i<data.length; i++)
			{
				System.out.printf( "%02X ", data[i]);
			}
			System.out.printf("\n");

			if(logFile != null)
			{
				toFile(head, logFile);
				for(int i=0; i<data.length; i++)
				{
					toFile(String.format("%02X ", data[i]), logFile);
				}
				toFile("\n", logFile);
			}
		}
	}
	
	//To print the obj by convert it to json format
	public static void printObject(String Head,Object obj)
	{
		if(isLogEnable(debug, allowGeneral))
		{
			if(obj == null)
			{
				debug(Head + "null");
				return;
			}
			String json = JSON.toJSONStringWithDateFormat(obj, "yyy-MM-dd HH:mm:ss");
			debug(Head + json);
		}
	}
}
