package com.DocSystem.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import util.RegularUtil;
import util.ReturnAjax;
import util.Encrypt.MD5;
import util.WebUploader.MultipartFileParam;

import com.DocSystem.entity.User;
import com.DocSystem.service.impl.UserServiceImpl;
import com.DocSystem.controller.BaseController;
import com.DocSystem.common.FileUtil;
import com.DocSystem.common.Log;
import com.DocSystem.commonService.EmailService;
import com.DocSystem.commonService.SmsService;

@Controller
@RequestMapping("/User")
public class UserController extends BaseController {
	@Autowired
	private UserServiceImpl userService;
	
	@Autowired
	private SmsService smsService;
	
	@Autowired
	private EmailService emailService;
	
	
	
	//用户登录接口
	@RequestMapping("/login.do")
	public void login(String userName,String pwd,String rememberMe,HttpServletRequest request,HttpSession session,HttpServletResponse response){
		Log.infoHead("************** login ****************");
		Log.debug("login userName:"+userName + " pwd:" + pwd + " rememberMe:" + rememberMe);
		
		ReturnAjax rt = new ReturnAjax();
		
		User loginUser = null;
		try {
			loginUser = loginCheck(userName, pwd, request, session, response, rt);
			if(loginUser == null)
			{
				writeJson(rt, response);
				User tmp_user = new User();
				tmp_user.setName(userName);
				addSystemLog(request, tmp_user, "login", "login", "登录","失败", null, null, null, buildSystemLogDetailContent(rt));
				return;
			}
		} catch (Exception e) {
			errorLog("login 异常！");
			errorLog(e);
			rt.setError("用户登录异常，请检查数据库配置是否正常!");
			rt.setData("needCheckDBSetting");
			docSysIniState = -1;
			addDocSysInitAuthCode(systemUser);
			writeJson(rt, response);	
			return;
		}
		
		
		//Set session
		Log.debug("登录成功");
		session.setAttribute("login_user", loginUser);
		session.setMaxInactiveInterval(24*60*60);	//24 hours
		
		Log.debug("SESSION ID:" + session.getId());

		//如果用户点击了保存密码则保存cookies
		if(rememberMe!=null&&rememberMe.equals("1")){
			String encUserName = URLEncode(userName);
			addCookie(response, "dsuser", encUserName, 7*24*60*60);//一周内免登录
			addCookie(response, "dstoken", pwd, 7*24*60*60);
			Log.debug("用户cookie保存成功");
		}

		//Feeback to page
		addSystemLog(request, loginUser, "login", "login", "登录","成功", null, null, null, buildSystemLogDetailContent(rt));
		
		rt.setMsgInfo("登录成功！");
		rt.setData(loginUser);	//将数据库取出的用户信息返回至前台
		writeJson(rt, response);	

		if(redisEn)
		{
			if(globalClusterDeployCheckState == 1)
			{				
				//TODO: 如果集群检测未结束，需要在这里完成后续的集群检测
				completeClusterDeployCheck();
			}
		}
		return;
	}

	//获取当前登录用户信息
	@RequestMapping(value="getLoginUser")
	public void getLoginUser(HttpServletRequest request,HttpSession session,HttpServletResponse response){
		Log.infoHead("************** getLoginUser ****************");
		Log.debug("getLoginUser SESSION ID:" + session.getId());
		
		ReturnAjax rt = new ReturnAjax();
		
		//查询系统中是否存在超级管理员
		User qUser = new User();
		qUser.setType(2); //超级管理员
		List<User> uList = userService.getUserListByUserInfo(qUser);
		if(uList == null || uList.size() == 0)
		{
			Log.warn("系统管理员不存在!");
			rt.setError("系统管理员不存在!");
			rt.setData("needAddFirstAdmin");
			writeJson(rt, response);	
			return;
		}
		
		User user = getLoginUser(session, request, response, rt);
		if(user == null)
		{	
			//用户未登录
			writeJson(rt, response);
			return;
		}
		
		//I not sure if the info in loginUser is lastest, so I need to get the usrInfo from database 
		user = userService.getUser(user.getId());
		user.setPwd("");
		user.docSysType = docSysType;
		user.isSalesServer = isSalesServer;
		rt.setData(user);	//返回用户信息
		writeJson(rt, response);
		
		if(redisEn)
		{
			if(globalClusterDeployCheckState == 1)
			{				
				//TODO: 如果集群检测未结束，需要在这里完成后续的集群检测
				completeClusterDeployCheck();
			}
		}
	}
	
	//登出接口
	@RequestMapping(value="logout")
	public void logOut(HttpServletRequest request, HttpSession session,HttpServletResponse response,ModelMap model,String type){
		Log.infoHead("************** logout ****************");
		Log.debug("Logout SESSION ID:" + session.getId());
		
		ReturnAjax rt = new ReturnAjax();
		User loginUser = (User) session.getAttribute("login_user");
		
		//删除cookie即将cookie的maxAge设置为0
		addCookie(response, "dsuser", null, 0);
		addCookie(response, "dstoken", null, 0);
		
		//清除session中的登录信息
		session.removeAttribute("login_user");
		
		//清除session中文件密码信息  
		List<String> docPwdList = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> attrs = session.getAttributeNames();
		if(attrs != null)
		{
			while(attrs.hasMoreElements())
			{
				// 获取session键值  
				String name = attrs.nextElement().toString();
				if(name.startsWith("docPwd_"))
				{
					docPwdList.add(name);
					// 根据键值取session中的值  
					//Object vakue = session.getAttribute(name);
					//Log.debug("------" + name + ":" + vakue +"--------\n");
				}	
			}
			for(int i=0; i<docPwdList.size(); i++)
			{
				session.removeAttribute(docPwdList.get(i));
			}
		}
		
		rt.setMsgInfo("您已成功退出登陆。");

		addSystemLog(request, loginUser, "logout", "logout", "退出登录","成功", null, null, null, buildSystemLogDetailContent(rt));

		writeJson(rt, response);	
	}
	
	
	//用户是否已注册检查接口
	@RequestMapping(value="checkUserRegistered")
	public void checkUserRegistered(String userName, HttpServletResponse response)
	{
		Log.infoHead("************** checkUserRegistered ****************");
		Log.debug("checkUserRegistered userName:"+userName);
		
		ReturnAjax rt = new ReturnAjax();
		
		//检查用户名是否为空
		if(userName==null||"".equals(userName))
		{
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		
		User user = new User();
		user.setName(userName);
		if(RegularUtil.isEmail(userName))	//邮箱注册
		{
			user.setEmail(userName);			
		}
		else if(RegularUtil.IsMobliePhone(userName))
		{
			user.setTel(userName);
		}
		else
		{
			rt.setError("账号格式不正确！");
			writeJson(rt, response);
			return;
		}
		
		userCheck(user, true, true, rt);
		writeJson(rt, response);
	}
	
	//注册接口
	@RequestMapping(value="register")
	public void register(HttpServletRequest request, HttpSession session,String userName,String pwd,String pwd2,String verifyCode,HttpServletResponse response,ModelMap model)
	{
		Log.infoHead("************** register ****************");		
		Log.debug("register userName:"+userName + " pwd:"+pwd + " pwd2:"+pwd2 + " verifyCode:"+verifyCode);
		
		ReturnAjax rt = new ReturnAjax();
		
		//检查用户名是否为空
		if(userName==null||"".equals(userName))
		{
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		
		if(checkSystemUsersCount(rt) == false)
		{
			writeJson(rt, response);
			return;
		}

		User user = new User();
		user.setName(userName);
		if(RegularUtil.isEmail(userName))	//邮箱注册
		{
			user.setEmail(userName);
		}
		else if(RegularUtil.IsMobliePhone(userName))
		{
			user.setTel(userName);
		}
		else
		{
			rt.setError("账号格式不正确！");
			writeJson(rt, response);
			return;
		}
		
		if(userCheck(user, true, true, rt) == false)
		{
			Log.debug("用户检查失败!");			
			writeJson(rt, response);
			return;			
		}
		
		//检查验证码是否正确
		if(checkVerifyCode(session,"docsys_vcode", userName, verifyCode,1) == false)
		{
			rt.setError("验证码错误！");
			writeJson(rt, response);
			return;
		}
		
		//检查密码是否为空
		if(pwd==null||"".equals(pwd))
		{
			rt.setError("密码不能为空！");
			writeJson(rt, response);
			return;
		}
		
		if(!pwd.equals(pwd2))	//要不要在后台检查两次密码不一致问题呢
		{
			Log.debug("注册密码："+pwd);
			Log.debug("确认注册密码："+pwd2);
			rt.setError("两次密码不一致，请重试！");
			writeJson(rt, response);
			return;
		}
		user.setPwd(pwd);
		user.setCreateType(1);	//用户为自主注册
		//set createTime
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String createTime = df.format(new Date());// new Date()为获取当前系统时间
		user.setCreateTime(createTime);	//设置川剧时间
		user.setType(0);
		if(isFirstAdminUserExists() == false)
		{
			user.setType(2);
		}
		userService.addUser(user);
		
		addSystemLog(request, user, "register", "register", "用户注册","成功", null, null, null, buildSystemLogDetailContent(rt));

		user.setPwd("");	//密码不要返回回去
		rt.setData(user);
		writeJson(rt, response);
		return;
	}
	
	//注册接口(无校验码)
	@RequestMapping(value="registerEx")
	public void registerEx(
			String userName, 
			String pwd, 
			String pwd2, 
		    String realName,
		    String nickName,
		    String tel,
		    String email,
		    String intro,
		    HttpServletRequest request, HttpSession session, HttpServletResponse response,ModelMap model)
	{
		Log.infoHead("************** registerEx ****************");		
		Log.debug("registerEx userName:"+userName + " pwd:"+pwd + " pwd2:"+pwd2 
				+ " realName:"+realName + " nickName:"+nickName + " tel:"+tel + " email:"+email + " intro:"+intro);
		
		ReturnAjax rt = new ReturnAjax();
		
		//检查用户名是否为空
		if(userName==null||"".equals(userName))
		{
			Log.info("registerEx 账号不能为空");		
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		
		//检查密码是否为空
		if(pwd==null||"".equals(pwd))
		{
			rt.setError("密码不能为空！");
			writeJson(rt, response);
			return;
		}
		
		if(!pwd.equals(pwd2))	//要不要在后台检查两次密码不一致问题呢
		{
			Log.debug("注册密码："+pwd);
			Log.debug("确认注册密码："+pwd2);
			rt.setError("两次密码不一致，请重试！");
			writeJson(rt, response);
			return;
		}
						
		if(checkSystemUsersCount(rt) == false)
		{
			writeJson(rt, response);
			return;
		}

		User user = new User();
		user.setName(userName);
		if(RegularUtil.isEmail(userName))	//邮箱注册
		{
			user.setEmail(userName);
		}
		else if(RegularUtil.IsMobliePhone(userName))
		{
			user.setTel(userName);
		}		
		if(tel != null)
		{
			user.setTel(tel);
		}
		if(email != null)
		{
			user.setEmail(email);
		}
		
		if(userCheck(user, true, true, rt) == false)
		{
			Log.info("用户检查失败!");			
			writeJson(rt, response);
			return;			
		}
		
		user.setPwd(pwd);
		user.setCreateType(1);	//用户为自主注册
		user.setRealName(realName);
		user.setNickName(nickName);
		user.setIntro(intro);
		
		//set createTime
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String createTime = df.format(new Date());// new Date()为获取当前系统时间
		user.setCreateTime(createTime);	//设置川剧时间
		user.setType(0);
		if(isFirstAdminUserExists() == false)
		{
			user.setType(2);
		}
		userService.addUser(user);
		
		addSystemLog(request, user, "registerEx", "registerEx", "用户注册","成功", null, null, null, buildSystemLogDetailContent(rt));

		user.setPwd("");	//密码不要返回回去
		rt.setData(user);
		writeJson(rt, response);
		return;
	}
	
	/**
	 * 发送邮箱验证信息
	 * @param response
	 * @param userName type
	 */
	@RequestMapping("/sendVerifyCode.do")
	public void sendVerifyCode(String userName,Integer type,
			String mailSubject,
			HttpSession session,HttpServletResponse response)
	{
		Log.infoHead("************** sendVerifyCode ****************");		
		Log.debug("sendVerifyCode userName:"+userName + " type:" + type);
		
		ReturnAjax rt = new ReturnAjax();
		if(userName == null || "".equals(userName))	//从session中取出用户名??
		{
			Log.debug("userName不能为空");
			rt.setError("请填写正确的邮箱或手机");
			writeJson(rt, response);
			return;
		}

		//根据注册类型不同，验证码需要放置在不同的session里面
		String sessionName = "";	//0 注册，1忘记密码
		if(type == null)	//默认用于注册
		{
			type = 0;	//默认验证码为用户注册
		}
		if(type == 0)
		{
			sessionName = "docsys_vcode";
		}
		else	
		{
			sessionName = "docsys_vcode" + type;			
		}
		
		//如果是邮箱则发送到邮箱，否则发送到手机
		if(RegularUtil.isEmail(userName))	//邮箱注册
		{	
			String code = generateVerifyCode(session,sessionName,userName);
			
			String content = 		
					"<br>"
					+ "您收到了来自MxsDoc的验证码：" + code + ",15分钟内有效，请及时验证。"
					+ "<br>"
					+ "<br>";

			String mailContent =  channel.buildMailContent(content);
			if(mailContent == null)
			{
				mailContent = content;
			}
			
			emailService.sendEmail(rt, userName, mailContent, mailSubject);
			writeJson(rt, response);
			return;	
		}
		else if(RegularUtil.IsMobliePhone(userName))
		{
			String code = generateVerifyCode(session,sessionName,userName);
			sendVerifyCodeSMS(rt,userName,type,code);
			writeJson(rt, response);
			return;
		}
		else
		{
			Log.debug("userName不是邮箱或手机");
			rt.setError("请使用正确的邮箱手机！");
			writeJson(rt, response);
			return;
		}		
	}

	private void sendVerifyCodeSMS(ReturnAjax rt, String phone, Integer type, String code) {
		String smsSendUri = getSmsSendUri();
		String smsApikey = getSmsApikey();
		String smsTplid = getSmsTplid();
		
		switch(type.intValue())
		{
		case 0:
			smsService.sendSms(rt,phone, smsSendUri, smsApikey, smsTplid, code, null, null); //注册短信模板id
			break;
		case 1:
			smsService.sendSms(rt,phone, smsSendUri, smsApikey, smsTplid, code, null, null); //忘记密码短信模板id
			break;
		default:
			smsService.sendSms(rt,phone, smsSendUri, smsApikey, smsTplid, code, null, null); //注册短信模板id
		}

	}

	//生成验证码: sessionVarName 保存验证码的session变量名
	public String generateVerifyCode(HttpSession session,String sessionVarName,String userName)
	{
		String code = Math.round(Math.random() * 1000000) + "";
		while(code.length()<6){
				code = "0" + code;
		}
		//将验证码保存进session中，同时将session有效期改为15分钟，有点风险
		session.setAttribute(sessionVarName, userName+code);
		session.setMaxInactiveInterval(15*60);
		return code;
	}
	
	//检查验证码：successClear设置的话，则验证通过会清除
	public boolean checkVerifyCode(HttpSession session, String sessionVarName, String userName, String code,int successClear)
	{
		code = userName+code;
		String code1 = (String) session.getAttribute(sessionVarName);
		if(code1!=null&&!"".equals(code1)&&code!=null&&!"".equals(code1)){
			if(code.equals(code1)){
				if(successClear == 1)
				{
					//验证码用过一次后将不能再使用，将session改回24小时有效，session不需要一直有效，因为网页可能一直在线
					session.removeAttribute(sessionVarName);
					session.setMaxInactiveInterval(24*60*60);	
				}
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@RequestMapping(value="checkVerifyCode")
	public void checkVerifyCode(HttpSession session,String userName,Integer type, String verifyCode,HttpServletResponse response,ModelMap model)
	{
		Log.infoHead("************** checkVerifyCode ****************");		
		Log.debug("checkVerifyCode userName:"+userName + " type:"+type + " verifyCode:"+verifyCode);
		
		ReturnAjax rt = new ReturnAjax();
		
		//检查用户名是否为空
		if(userName==null||"".equals(userName))
		{
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		
		//检查验证码是否正确
		//根据注册类型不同，验证码需要放置在不同的session里面
		String sessionName = "";	//0 注册，1忘记密码
		if(type == null)	//默认用于注册
		{
			type = 0;	//默认验证码为用户注册
		}
		if(type == 0)
		{
			sessionName = "docsys_vcode";
		}
		else	
		{
			sessionName = "docsys_vcode" + type;			
		}
		if(checkVerifyCode(session,sessionName, userName, verifyCode,0) == false)
		{
			rt.setError("验证码错误！");
			writeJson(rt, response);
			return;
		}
		
		//返回成功信息
		writeJson(rt, response);
		return;
	}	
	
	//This function is for forget password
	@RequestMapping(value="changePwd")
	public void changePwd(HttpSession session,String userName,String pwd,String pwd2,String verifyCode,HttpServletResponse response,ModelMap model)
	{
		Log.infoHead("************** changePwd ****************");		
		Log.debug("changePwd userName:"+userName + " pwd:"+pwd + " pwd2:"+pwd2 + " verifyCode:"+verifyCode);
		
		ReturnAjax rt = new ReturnAjax();
		
		User qUser = new User();
		//检查用户名是否为空
		if(userName==null||"".equals(userName))
		{
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		else if(RegularUtil.isEmail(userName))	//邮箱注册
		{
			qUser.setEmail(userName);
		}
		else if(RegularUtil.IsMobliePhone(userName))
		{
			qUser.setTel(userName);
		}
		else
		{
			rt.setError("账号格式不正确！");
			writeJson(rt, response);
			return;
		}
		List<User> uList = getUserList(userName,null);
		if(uList == null || uList.size() == 0)
		{
			rt.setError("用户不存在！");
			writeJson(rt, response);
			return;
		}
		
		//检查验证码是否正确
		if(checkVerifyCode(session,"docsys_vcode1", userName, verifyCode,1) == false)
		{
			rt.setError("验证码错误！");
			writeJson(rt, response);
			return;
		}
		
		//检查密码是否为空
		if(pwd==null||"".equals(pwd))
		{
			rt.setError("密码不能为空！");
			writeJson(rt, response);
			return;
		}
		
		if(!pwd.equals(pwd2))	//要不要在后台检查两次密码不一致问题呢
		{
			Log.debug("密码："+pwd);
			Log.debug("确认密码："+pwd2);
			rt.setError("两次密码不一致，请重试！");
			writeJson(rt, response);
			return;
		}
		
		//更新密码
		User user = new User();
		user.setId(uList.get(0).getId());	//设置UserId
		user.setPwd(pwd);
		if(userService.updateUserInfo(user) == 0)
		{
			Log.debug("设置密码失败!");
			rt.setError("设置密码失败！");
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
		return;
	}
	
	@RequestMapping(value="modifyPwd")
	public void modifyPwd(HttpSession session,String userName,String pwd,String pwd2,String oldPwd,HttpServletResponse response,ModelMap model)
	{
		Log.infoHead("************** modifyPwd ****************");		
		Log.debug("changePwd userName:"+userName + " pwd:"+pwd + " pwd2:"+pwd2 + " oldPwd:"+oldPwd);
		
		ReturnAjax rt = new ReturnAjax();
		
		//检查用户名是否为空
		if(userName==null||"".equals(userName))
		{
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		
		//Check the user oldPwd
		User qUser = new User();
		qUser.setName(userName);
		qUser.setPwd(oldPwd);
		List<User> uList = userService.getUserListByUserInfo(qUser);
		if(uList == null || uList.size() == 0)
		{
			rt.setError("用户名或密码错误！");
			writeJson(rt, response);
			return;
		}
		
		//检查密码是否为空
		if(pwd==null||"".equals(pwd))
		{
			rt.setError("密码不能为空！");
			writeJson(rt, response);
			return;
		}
		
		if(!pwd.equals(pwd2))	//要不要在后台检查两次密码不一致问题呢
		{
			Log.debug("密码："+pwd);
			Log.debug("确认密码："+pwd2);
			rt.setError("两次密码不一致，请重试！");
			writeJson(rt, response);
			return;
		}
		
		//更新密码
		User user = new User();
		user.setId(uList.get(0).getId());	//设置UserId
		user.setPwd(pwd);
		if(userService.updateUserInfo(user) == 0)
		{
			Log.debug("设置密码失败!");
			rt.setError("设置密码失败！");
			writeJson(rt, response);
			return;
		}
		
		writeJson(rt, response);
		return;
	}
	
	@RequestMapping(value="updateLoginUserInfo")
	public void updateLoginUserInfo(HttpSession session,String userName,String nickName,String realName,String intro,HttpServletResponse response,ModelMap model)
	{
		Log.infoHead("************** updateLoginUserInfo ****************");		
		Log.debug("updateUserInfo userName:"+userName + " nickName:"+nickName + " realName:"+realName + " intro:"+intro);

		ReturnAjax rt = new ReturnAjax();
		
		//检查用户名是否为空，注意用户名真的是用户名，不是指绑定的手机和邮箱
		if(userName==null||"".equals(userName))
		{
			Log.debug("updateUserInfo() userName is empty！");
			rt.setError("账号不能为空！");
			writeJson(rt, response);
			return;
		}
		
		//Check if user is login
		User loginUser = (User) session.getAttribute("login_user");
		if(loginUser == null)
		{
			Log.debug("updateUserInfo() 用户未登陆！");
			rt.setError("用户未登陆！");
			writeJson(rt, response);
			return;
		}
		
		if(!userName.equals(loginUser.getName()))
		{
			Log.debug("updateUserInfo() 不能修改其他用户的信息！");
			rt.setError("修改用户信息失败！");
			writeJson(rt, response);
			return;
		}
		
		//Try to find the User
		User user = getUserByName(userName);
		if(user == null)
		{
			rt.setError("用户不存在！");
			writeJson(rt, response);
			return;			
		}
		
		//检查用户名是否为空
		if(realName!=null&&"".equals(realName))
		{
			rt.setError("真实姓名不能为空！");
			writeJson(rt, response);
			return;
		}

		if(nickName!=null&&"".equals(nickName))
		{
			rt.setError("昵称不能为空！");
			writeJson(rt, response);
			return;
		}
		
		User newUserInfo = new User();
		newUserInfo.setId(user.getId());
		newUserInfo.setNickName(nickName);
		newUserInfo.setRealName(realName);	
		newUserInfo.setIntro(intro);	
		if(userService.updateUserInfo(newUserInfo) == 0)
		{
			rt.setError("用户信息更新失败！");
			writeJson(rt, response);
			return;			
		}
		syncUpLoginUserInfo(newUserInfo,loginUser);
		
		writeJson(rt, response);
		return;
	}
	
	private void syncUpLoginUserInfo(User user,User loginUser)
	{
		if(user.getNickName() != null)
		{
			loginUser.setNickName(user.getNickName());
		}
		if(user.getRealName() != null)
		{
			loginUser.setRealName(user.getRealName());
		}
		if(user.getIntro() != null)
		{
			loginUser.setNickName(user.getIntro());
		}
		
	}
	
	@RequestMapping(value="uploadUserImg")
    public  void uploadUserImg(MultipartFileParam param, HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception 
    {	
		Log.infoHead("************** uploadUserImg ****************");		
		Log.debug("uploadUserImg() filename:"+param.getName() + " size:" + param.getSize() + " Uid:" +param.getUid());
		
		ReturnAjax rt = new ReturnAjax();
		
		//Check if user is login
		User loginUser = (User) session.getAttribute("login_user");
		if(loginUser == null)
		{
			Log.debug("uploadUserImg() 用户未登陆！");
			rt.setError("用户未登陆！");
			writeJson(rt, response);
			return;
		}
		
		//Save the file
		MultipartFile uploadFile = param.getFile();
		if (uploadFile == null) 
		{
			Log.debug("uploadUserImg() uploadFile is null！");
			rt.setError("文件上传失败！");
			writeJson(rt, response);
			return;
		}
		
		if(isPictureFile(uploadFile.getOriginalFilename()) == false)
		{
			Log.debug("uploadUserImg() file format error！");
			rt.setMsgData("uploadUserImg() file format error！");
			rt.setError("文件格式错误！");
			writeJson(rt, response);
			return;
		}
		
		/*保存文件*/
		Log.debug("uploadFile size is :" + uploadFile.getSize());		
		
		String userImgName = saveUserImg(uploadFile,loginUser);
		if(userImgName == null)
		{
			Log.debug("uploadUserImg() saveFile Failed！");
			rt.setMsgData("uploadUserImg() saveFile Failed！");
			rt.setError("文件上传失败！");
			writeJson(rt, response);
			return;
		}
			
		//Set the user img info
		String userImgUrl = userImgName;
		User user = new User();
		user.setId(loginUser.getId());
		user.setImg(userImgUrl);
		if(userService.updateUserInfo(user) == 0)
		{
			Log.debug("uploadUserImg() updateUserInfo Failed！");
			rt.setMsgData("uploadUserImg() updateUserInfo Failed！");
			rt.setError("用户头像更新失败！");
			writeJson(rt, response);
			return;				
		}
		loginUser.setImg(userImgUrl);
		rt.setData(loginUser);
		writeJson(rt, response);
    }

	private boolean isPictureFile(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(suffix == null || suffix.isEmpty())
        {
        	return false;
        }
        return FileUtil.isPicture(suffix.toLowerCase());
	}

	private String saveUserImg(MultipartFile uploadFile,User user) 
	{
		String fileName = uploadFile.getOriginalFilename();
        
        String imgDirPath = getUserImgPath(); 
        Log.debug("imgDirPath:" + imgDirPath);
        File dir = new File(imgDirPath);
        if (!dir.exists()) {
        	if(dir.mkdirs() == false)
        	{
        		return null;
        	}
        }
        
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String usrImgName =  user.getId()+"_"+ MD5.md5(fileName) + "."  + suffix; 
        String retName = null;
		try {
			retName = FileUtil.saveFile(uploadFile, imgDirPath,usrImgName);
		} catch (Exception e) {
			errorLog("saveUserImg() saveFile " + usrImgName +" 异常！");
			errorLog(e);
			return null;
		}
		
		Log.debug("saveUserImg() saveFile return: " + retName);
		if(retName == null  || !retName.equals(usrImgName))
		{
			Log.debug("updateRealDoc() saveFile " + usrImgName +" Failed！");
			return null;
		}
		
		return retName;
	}
	
	private String getUserImgPath()
	{	
		String imgDirPath = docSysDataPath + "userImg/";
        return imgDirPath;
	}
		
	//This interface is for getUserImg if useImgs not under tomcat
	@RequestMapping(value="getUserImg")
    public  void getUserImg(String fileName, HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception 
    {	
		Log.info("************** getUserImg ****************");				
		Log.debug("getUserImg() fileName:" + fileName);
		
		//解决中文编码问题
		if(request.getHeader("User-Agent").toUpperCase().indexOf("MSIE")>0){  
			fileName = URLEncoder.encode(fileName, "UTF-8");  
		}else{  
			fileName = new String(fileName.getBytes("UTF-8"),"ISO8859-1");  
		}  
		Log.debug("getUserImg fileName:" + fileName);
		
		if(fileName.contains(".."))
		{
			Log.info("getUserImg fileName contains relative path [" + fileName + "]");
			return;
		}
		 
		//解决空格问题
		response.setHeader("content-disposition", "attachment;filename=\"" + fileName +"\"");
		response.setHeader("Content-Type","image/jped");
		
		//读取要下载的文件，保存到文件输入流
		String dstPath = getUserImgPath() + fileName;
		FileInputStream in = new FileInputStream(dstPath);
		//创建输出流
		OutputStream out = response.getOutputStream();
		//创建缓冲区
		byte buffer[] = new byte[1024];
		int len = 0;
		//循环将输入流中的内容读取到缓冲区当中
		while((len=in.read(buffer))>0){
			//输出缓冲区的内容到浏览器，实现文件下载
			out.write(buffer, 0, len);
		}
		//关闭文件输入流
		in.close();
		//关闭输出流
		out.close();
    }
	
}
