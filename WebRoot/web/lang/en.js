/*
 * English Support
 * */
var langType = "en";
var langExt = "_en";	//跳转网页扩展字符，用于区分跳转不同页面

function _Lang(str1, connectStr , str2)
{
	if(connectStr == undefined)
	{
		return lang(str1);
	}
	
	return lang(str1) + connectStr + lang(str2);
}

function _LangStats(totalNum, successNum)
{
	if(successNum == undefined)
	{
		return "Total: "+ totalNum;		
	}
	return "Total: "+ totalNum +", Failed: " + (totalNum - successNum);
}

function lang(str)
{
	var translateMap = 
	{
		//通用
		"提示" : "Message",
		"确认操作" : "Confirm",
		"错误" : "Error",
		"确定" : "Ok",
		"确认" : "Confirm",
		"取消" : "Cancel",
		"登录" : "Sign In",
		"注册" : "Sign Up",
		"退出登录" : "Log Out",
		"添加系统管理员" : "Add System Administrator",		
		"服务器异常" : "Server exception",
		"页面正在加载，请稍等" : "Loading",
		"页面正在加载，请稍侯" : "Loading",
		"页面正在加载，请稍等..." : "Loading...",
		"文件" : "File",
		"文件夹" : "Folder",		
		"用户未登录" : "User not Signed In",
		"任意用户" : "Everyone",
		"继续" : "Continue",
		"退出" : "Exit",
		"编辑" : "Edit",
		"新增用户" : "Add User",
		"编辑用户" : "Edit User",
		"删除用户" : "Delete User",
		"重置密码" : "Reset Password",
		"新增用户组" : "Add Group",
		"编辑用户组" : "Edit Group",
		"删除用户组" : "Delete Group",
		"新增仓库" : "Add Repository",
		"编辑仓库" : "Edit Repository",
		"删除仓库" : "Delete Repository",
		"删除成功" : "Delete Success",
		"删除失败" : "Delete Failed",
		"更新成功" : "Update Success",
		"更新失败" : "Update Failed",
		"导出" : "Export",
		"导入" : "Import",
		"备份" : "Backup",
		"成功" : "Success",
		"失败" : "Failed",
		"不限" : "NoLimit",
		"长期" : "LongTerm",
		"已过期" : "Expired",
		"证书已过期，请购买商业版证书！" : "License was expired, Please purchase business license !",
		"免费版禁止修改主页，请购买商业版证书！" : "Main page was changed, Please purchase business license !",
		"证书已失效，请重新购买商业版证书！" : "License is invalid, Please purchase business license !",
		"用户数量已达到上限，请购买商业版证书！" : "The number of users has reached the maximum limit, Please purchase business license !",
		"查询失败" : "Query Failed",
		"文件服务器" : "FileServer",	
		
		//注册
		"注册失败"	: "Sign Up Failed",			
		"获取验证码" : "Send",
		"验证码发送失败" : "Failed to send verification code",
		"验证码已发送，请注意查收！" : "MxsDoc will send a verification code to your mobile phone or email!",
		"两次密码输入不一致" : "Re-enter password error",
		"手机号不能为空" : "Mobile Number is empty",
		"用户名不能为空" : "Account is empty",
		"邮箱不能为空" : "Email is empty",
		"密码不能为空" : "Password is empty",
		"账号不能为空！" : "Account is empty!",
		"账号格式不正确！" : "Account format error!",
		"该用户已存在！" : "Account exists!",
		"该手机已被使用！" : "Mobile Phone have been registered",
		"该邮箱已被使用！" : "Email have been registered",
		"手机格式错误！" : "Incorrect Mobile Number",
		"邮箱格式错误！" : "Incorrect Email",
		"请填写正确的邮箱或手机" : "Please use correct email or mobile phone",
		"请使用正确的邮箱手机！" : "Please use correct email or mobile phone",
		"验证码错误！" : "Incorrect verification code!",
		"密码不能为空！" : "Password is empty!",
		"两次密码不一致，请重试！" : "Re-enter password error!",
		"保存成功" : "Save Ok",
		"保存失败" : "Save Failed",		
		"不能预览" : "Unable to preview",
		"文件必须小于" : "File is larger than",
		"请选择头像" : "Please select a profile picture",
		
		//登录
		"登录失败"	: "Sign In Failed",
    	"获取用户信息失败" : "Failed to get user information",
    	"用户名或密码错误！" : "Incorrect account or password!",
		
    	//退出登录
		"退出登录失败" : "Sign Out Failed",
		
		//添加系统管理员
		"添加系统管理员失败" : "Failed to add System Aministrator",

		//系统配置
		"系统初始化失败" : "System Init Failed",
		"重启服务" : "Restart Server",
		"未指定服务器路径！" : "Server not configured",
		"是否重启服务" : "Do you want to restart server",
		"重启成功" : "Restart Ok",
		"重启失败" : "Failed to restart",
		"获取数据库信息失败" : "Failed to get DataSource configuration",
		"数据库配置修改成功，请重启服务！" : "Update DataSource successfuly, Please restart server!",
		"数据库配置有变更，请先重启服务！"	: "DataSource changed, Please restart server!",
		"更新数据库配置信息失败" : "Failed to modify DataSource configuration",
		"数据库连接成功" : "Connect DataBase Successfuly",
		"数据库连接失败" : "Failed to connect DataBase",
		"连接数据库失败" : "Connect DataBase Failed",
		"新建数据库失败" : "Create DataBase Failed",
		"重置数据库" : "Reset DataBase",
		"是否重置数据库" : "Do you want to reset DataBase",
		"重置数据库成功" : "Reset DataBase Successfuly",
		"重置数据库失败" : "Failed to reset DataBase",
		"数据库导出失败" : "Failed to export data from DataBase",
		"导入数据" : "Import Data",
		"是否导入" : "Do you want to import",
		"导入成功" : "Import Successfuly",
		"导入失败" : "Failed to import",
		"上传异常" : "Upload exception",

		//仓库新增与设置
		"新建仓库" : "New Repository",
		"编辑仓库" : "Repository Settings",
		"用户未登录，请先登录！" : "Please login system firstly!",
		"您无权修改该仓库!" : "You have no right to modify this repository!",
		"仓库信息更新失败！" : "Failed to update the repository configuration",
		"仓库目录修改失败" : "Failed to change folder for repository",
		"修改仓库文件目录失败！" : "Failed to change the storage folder for repository",
		"普通用户无权修改仓库存储位置，请联系管理员！" : "You can not change the storage folder, please contact System Administrator",
		"版本仓库初始化失败！" : "Failed to init history storage repository",
		"仓库文件将被加密存储，密钥一旦丢失将导致文件无法恢复，是否加密？" : "Do you want to encrypt files in this repository?",
		"更新仓库配置失败" : "Failed to update repoistory configuration",
		"仓库类型不能修改" : "Repository Type can not be changed",
		"仓库信息未变化" : "Repository configuration was not changed",
		"仓库名不能为空" : "Name is empty",
		"仓库简介不能为空" : "Description is empty",
		"仓库存储路径不能为空" : "Path is empty",
		"新建仓库成功" : "Repoistory was created",
		"创建仓库失败" : "Failed to create new repoistory",
		"显示高级选项" : "Show Advanced Options",
		"隐藏高级选项" : "Hide Advanced Options",
		"测试成功" : "Test Success",
		"测试失败" : "Test Failed",
		"项目名" : "Repository Name",
		"磁盘空间" : "Disk Space",
		"系统错误" : "System Error",
		"更新目录失败" : "Failed to update repository menu",
		"备份密钥" : "Backup Encrypt-Key",
		"是否备份仓库密钥" : "Do you want to backup Encrypt-Key",
		"密钥备份成功" : "Encrypt-Key backup Success",
		"密钥备份失败" : "Failed to backup Encrypt-Key",
		"删除仓库" : "Delete Repository",
		"仓库删除后数据将无法恢复，是否删除" : "All data will be deleted, Please confirm you have backuped the data",
		"删除成功" : "Delete Ok",
		"删除失败" : "Failed to delete",
		"仓库设置" : "Repository Config",
		"版本管理" : "History",
		"关闭版本管理" : "Disable History",
		"开启版本管理" : "Enable History",
		"关闭历史版本管理" : "Disable History",
		"开启历史版本管理" : "Enable History",
		"忽略列表管理" : "Ignore List",
		"远程存储" : "RemoteStorage",
		"关闭远程存储" : "Disable RemoteStorage",
		"开启远程存储" : "Enable RemoteStorage",
		"全文搜索" : "TextSearch",
		"关闭全文搜索" : "Disable TextSearch",
		"开启全文搜索" : "Enable TextSearch",
		"自动备份" : "AutoBackup",
		"本地备份" : "LocalBackup",
		"关闭本地备份" : "Disable LocalBackup",
		"开启本地备份" : "Enable LocalBackup",
		"立即本地备份" : "Start LocalBackup",
		"异地备份" : "RemoteBackup",
		"立即异地备份" : "Start RemoteBackup",
		"关闭异地备份" : "Disable RemoteBackup",
		"开启异地备份" : "Enable RemoteBackup",
		"清除缓存" : "Clean Cache",
		"强制刷新" : "Force Refresh",
		"仓库不存在！" : "Repository not exists",
		"该仓库未设置远程存储，请联系管理员！" : "RemoteStorage was not configured, Please contact System Administrator",
		"远程存储忽略管理" : "RemoteStorage Ignore List",
		"关闭仓库所有文件的远程存储" : "Disable RemoteStorage for all documents",
		"开启仓库所有文件的远程存储" : "Enable RemoteStorage for all documents",
		"设置成功" : "Configure Ok",
		"设置失败" : "Configure Failed",
		"设置成功！" : "Configure Ok !",
		"获取版本忽略列表失败" : "Failed to get ignore list",
		"版本已忽略" : "History Disabled",
		"获取全文搜索忽略列表失败" : "Failed to get ignore list",
		"全文搜索已关闭" : "TextSearch Disabled",
		"获取远程存储忽略列表失败" : "Failed to get ignore list",
		"远程存储已关闭" : "RemoteStorage Disabled",
		"获取异地备份忽略列表失败" : "Failed to get ignore list",
		"异地备份已关闭" : "RemoteBackUp Disabled",
		"获取本地备份忽略列表失败" : "Failed to get ignore list",
		"本地备份已关闭" : "LocalBackUp Disabled",
		
		"该仓库未设置异地备份，请联系管理员！" : "RemoteBackup was not configured, Please contact System Administrator",
		"该仓库未设置异地自动备份，请联系管理员！" : "RemoteBackup was not configured, Please contact System Administrator",
		"异地备份忽略管理" : "RemoteBackup Ignore List",
		"关闭仓库所有文件的异地备份" : "Disable RemoteBackup for all documents",
		"开启仓库所有文件的异地备份" : "Enable RemoteBackup for all documents",
		
		"该仓库未设置本地备份，请联系管理员！" : "LocalBackup was not configured, Please contact System Administrator",
		"该仓库未设置本地自动备份，请联系管理员！" : "LocalBackup was not configured, Please contact System Administrator",
		"本地备份忽略管理" : "LocalBackup Ignore List",
		"关闭仓库所有文件的本地备份" : "Disable LocalBackup for all documents",
		"开启仓库所有文件的本地备份" : "Enable LocalBackup for all documents",
		
		"是否立即执行仓库自动备份？" : "Do you want to start AutoBackup for repository now?",
		"是否立即执行仓库本地自动备份？" : "Do you want to start LocalBackup for repository now?",
		"是否立即执行仓库异地自动备份？" : "Do you want to start RemoteBackup for repository now?",
		"开始" : "Start",
		"仓库备份中，可能需要花费较长时间，您可先关闭当前窗口！" : "Repository Backup may take long time, You can close this dialog now",
		"备份失败" : "Backup Failed",
		"仓库自动备份失败" : "Repository AutoBackup Failed",
		"本地自动备份成功" : "LocalBackup OK",
		"异地自动备份成功" : "RemoteBackup OK",		
		
		"该仓库未开启全文搜索，请联系管理员！" : "TextSearch was not configured, Please contact System Administrator",
		"全文搜索忽略管理" : "TextSearch Ignore List",
		"该仓库未设置全文搜索，请联系管理员！" : "TextSearch was not configured, Please contact System Administrator",
		"关闭仓库所有文件的全文搜索" : "Disable TextSearch for all documents",
		"开启仓库所有文件的全文搜索" : "Enable TextSearch for all documents",

		"版本忽略管理" : "History Ignore List",
		"历史版本忽略管理" : "History Ignore List",
		"该仓库未设置历史版本管理，请联系管理员！" : "History was not configured, Please contact System Administrator",
		"关闭仓库所有文件的历史版本管理" : "Disable History for all documents",
		"开启仓库所有文件的历史版本管理" : "Enable History for all documents",		
		"获取文件版本管理设置失败" : "Failed to get History ignore setting",
				
		"是否清除仓库缓存" : "Do you want to clean cache",
		"清除" : "Clean",
		"清除成功" : "Clean Ok",
		"清除失败" : "Clean Failed",
		
		"获取仓库用户列表失败" : "Failed to get user list",
		
		"暂无数据" : "No Data",
		"管理员" : "Admin",
		"读" : "Read",
		"写" : "Write",
		"增加" : "Add",
		"删除" : "Delete",
		"全部" : "All",
		"可继承" : "Inherit",
		"下载/分享" : "Download/Share",
		"不限" : "NoLimit",
		
		"是否删除该用户组的仓库权限" : "Do you want to remove Group on this repository",
		"删除用户组的仓库权限失败" : "Failed to remove Group on repository",
		"是否删除该用户的仓库权限" : "Do you want to remove User on this repository",
		"删除用户的仓库权限失败" : "Failed to remove User on repository",
		"是否删除该用户的目录权限设置" :  "Do you want to remove User on this folder",		
		"删除用户的目录权限设置失败" : "Failed to remove User on folder",

		"获取仓库列表失败" : "Failed to get repository list",
		"添加访问用户" : "Add Access User",
		"添加访问组" : "Add Access Group",
		
		"高级设置" : "Advanced Options",
		"密码设置" : "Set Password",
		"强制刷新整个仓库" : "Force Refresh whole repository",
		
		"权限设置失败" : "Access Configure Failed",
		"是否继续设置其他用户" : "Continue to configure other users",
		"后续错误是否执行此操作" : "Take the same action for this failure",
		"是" : "Yes",
		"否" : "No",
		"设置完成" : "Configure Completed",
		
		//文件操作与右键菜单选项
		"新建目录" : "New Folder",
		"新建文件" : "New File",	
		"刷新" : "Refresh",
		"分享" : "Share",
		"打开" : "Open",
		"新建" : "New",
		"删除" : "Delete",
		"重命名" : "Rename",
		"复制" : "Copy",
		"剪切" : "Cut",
		"粘贴" : "Paste",
		"移动" : "Move",
		"下载" : "Download",
		"上传" : "Upload",
		"预览" : "Preview",
		"拉取" : "Pull",
		"推送" : "Push",
		"锁定" : "Lock",
		"解锁" : "Unlock",
		"设置密码" : "Set Password",
		"文件上传" : "Upload Document",
		"文件推送" : "Push Document",
		"文件拉取" : "Pull Document",
		"文件分享" : "Share Document",
		"在新窗口打开" : "Open In New Page",
		"本地打开" : "Open In Office",
		"属性" : "Properties",
		"查看历史" : "View History",		
		"查看备份" : "View Backup",		
		"备注" : "Note",
		"下载备注" : "Download Note",
		"本地路径" : "Local File Path",
		"名字" : "Name",
		"路径" : "Path",		
		"链接" : "Link",
		"下载链接" : "Download Link",
		"全选" : "Select All",
		"回收站" : "RecycleBin",
		"更多" : "More",
		
		//通用
		"已存在" : "already exists",
		"请选择文件" : "No any file was selected",
		"请选择文件！" : "No any file was selected !",
		"请选择文件或目录" : "No any file or folder was selected",	
		"等" : "...",
		"个文件" : "files",
		"您没有新增或修改权限" : "You have no right to add or modify",
		"获取仓库信息失败" : "Failed to get repository information",
		"获取仓库目录失败" : "Failed to get repository's file list",
		"获取文件列表失败" : "Failed to get file list",
		"获取文件信息失败" : "Failed to get document information",
		"的备注" : "'s Note",
		"目标节点不存在" : "Target node not exists",
		"已存在，自动跳过" : "already exists, skip",
		"替换" : "Replace",
		"跳过" : "Skip",
		"继续" : "Continue",
		"结束" : "Stop",		
		"本地备份历史" : "LocalBackup History",
		"异地备份历史" : "RemoteBackup History",
		
		//访问密码验证
		"验证成功！" : "Verify Ok !",
		"验证失败" : "Verify Failed",
		
		//新建文件
		"文件名不能为空" : "FileName is empty",
		"新建完成" : "Create Ok",
		"新建失败" : "Create Failed",		
		
		//上传文件
		"正在上传" : "Uploading",
		"正在重传" : "Reuploading",
		"上传完成" : "Upload Completed",
		"请选择需要上传的文件" : "No any file or folder was selected to upload",
		"待上传" : "Upload Pennding...",
		"上传还未结束，是否终止上传" : "Upload not completed, do you want to cancel",
		"已存在,是否覆盖" : "already exists, do you want to replace",
		"后续已存在文件是否自动覆盖？" : "Do you want to replace existed files automatically?",
		"后续已存在文件是否自动跳过？" : "Do you want to skip existed files automatically?",
		"文件已存在，跳过且后续自动跳过" : "File already exist, Skip existed files automatically",
		"文件已存在，跳过但后续不自动跳过" : "File already exists and skip, do not skip existed files automatically",
		"文件已存在，跳过" : "File already exsits, skip",
		"是否继续上传其他文件？" : "Do you want to continue for other files ?",
		"继续" : "Continue",
		"结束上传" : "Stop Upload",		
		"后续错误是否执行此操作？" : "Do you want to take the same action ?",
		"警告" : "Warning",
		"校验码计算失败" : "File checksum caculate failed",
		"校验码计算失败:当前浏览器不支持读取文件" : "File checksum caculate failed : Current browser does not support to read file",
		"不是文件" : "is not a file",
		"文件上传超时" : "File upload timeout",
		"上传异常！" : "Upload Exception!",
		"文件读取失败！" : "Failed to read file",
		"用户取消了上传" : "Upload was canceled",
		
		//文件编辑
		"文件打开失败" : "Failed to open file",
			
		//刷新
		"刷新失败" : "Failed to refresh",		
		
		//删除文件
		"删除确认" : "Delete Confirm",
		"是否删除" : "Do you want to delete",
		"是否删除文件" : "Do you want to delete the file",
		"是否删除目录" : "Do you want to delete the folder",
		"清选择需要删除的文件或目录" : "No any file or folder was selected to delete",
		"待删除" : "Delete pendding...",
		"文件删除超时" : "Delete timeout",
		"删除失败,是否继续删除其他文件？" : "Delete failed, Continue to delete other files ?",
		"是否继续删除其他文件？" : "Continue to delete other files ?",
		
		//复制文件
		"请选择需要复制的文件" : "No any file or folder was selected to copy",
		"请选择需要复制的文件!" : "No any file or folder was selected to copy !",
		"复制成功" : "Copy Ok",
		"待复制" : "Copy Pendding...",
		"禁止将上级目录复制到子目录" : "Parent folder can not be copied to Sub folder",
		"文件复制超时" : "File Copy timeout",
		"文件已存在，用户放弃修改名字并取消了复制！" : "File already exists, User have gave up to change name and cancled the copy action",
		"复制失败,是否继续复制其他文件？" : "Copy failed, Continue to copy other files ?",
		"是否继续复制其他文件？" : "Continue to copy other files ?",
		"复制完成" : "Copy completed",
		"复制失败" : "Copy failed",
		
		//移动文件
		"是否移动文件" : "Do you want to move the file or folder",	
		"请选择需要移动的文件" : "No any file or folder was selected to move",
		"待移动" : "Move pendding...",
		"无法在同一个目录下移动" : "Unable to move under same folder",
		"文件移动超时" : "Move timeout",
		"移动失败,是否继续移动其他文件？" : "Move failed, Continue to move other files ?",
		"是否继续移动其他文件？" : "Continue to move other files ?",
		"移动完成" : "Move completed",
		"移动失败" : "Move failed",

		//重命名文件
		"重命名完成！" : "Rename completed",
		
		//下载文件
		"是否下载" : "Do you want to download",
		"请选择需要下载的文件" : "No any file or folder was selected to download",
		"请选择需要下载的文件!" : "No any file or folder was selected to download !",
		"下载还未结束，是否终止下载" : "Download not completed, do you want to cancel",
		"下载准备完成" : "Download is ready",
		"下载列表" : "Download List",
		"待下载..." : "Download Pennding...",
		"下载失败" : "Download failed",
		"目录压缩中" : "Compressing the folder",
		"下载失败,是否继续下载其他文件？" : "Download failed, Continue to download other files",
		"是否继续下载其他文件？" : "Continue to download other files",
		"已取消" : "Cancled",
		"下载准备中..." : "Download preparing...",
		
		
		//文件访问密码
		"密码验证" : "Verify Password",
		"访问密码设置" : "Set Access Password",
		"请选择需要设置访问密码的文件" : "No any file or folder was selected to set access password",
		
		//文件版本管理
		"该仓库未开通版本管理，请联系管理员" : "History was not configured for this repository, please contact System Administrator",
		"历史版本" : "History",
		"备注历史" : "Note's History",
		"历史详情" : "History Details",
		"下载仓库的历史版本" : "Download Repository's History",
		"的历史版本" : "'s History",
		"下载仓库的备注历史版本" : "Download Repository's Note History",
		"的备注历史版本" : "'s Note History",
		"历史版本下载准备中，可能需要花费较长时间，您可先关闭当前窗口！" : "History download preparing...",
		"历史版本恢复成功！" : "History recover success !",
		"历史版本恢复失败" : "Failed to recover history",
		"历史版本回退成功！" : "History revert success !",
		"历史版本回退失败" : "Failed to revert History",
		"获取历史信息失败" : "Failed to get history",
		"恢复成功" : "History recover success",
		"恢复失败" : "Failed to recover history",
		"回退成功" : "History revert success",
		"回退失败" : "Failed to revert History",
		"删除成功" : "History revert success",
		"删除失败" : "Failed to revert History",
		"查看" : "View",
		"详情" : "Detail",
		"恢复" : "Recover",
		"回退" : "Revert",
		
		
		//文件编辑
		"临时保存文件内容获取失败" : "Faied to get temp saved file content",
		"获取文件内容失败" : "Failed to get file content",
		
			
		//文件锁定与解锁
		"请选择需要锁定的文件" : "No any file or folder was selected to lock",
		"请选择需要解锁的文件" : "No any file or folder was selected to unlock",
		"锁定失败" : "Lock Failed",
		"解锁定败" : "Unlock Failed",
		
		//文件推送
		"推送失败" : "Push Failed",
		"远程文件可能被删除或覆盖，是否强制推送？" : "Remote files will be replaced forcely, please confirm",
		"远程文件改动将被强制覆盖，是否强制推送？" : "Remote files will be replaced forcely, please confirm",
		"该操作将推送目录下的所有文件，是否允许？" : "All files under this folder will be pushed, please confirm",
		
		//文件拉取
		"拉取失败" : "Pull Failed",
		"文件改动将被强制覆盖，是否强制拉取？" : "Local files will be replaced forcely, please confirm",
		"文件可能被删除或覆盖，是否强制拉取？" : "Local files will be replaced forcely, please confirm",
		"该操作将拉取目录下的所有文件，是否允许？" : "All files under this folder will be pulled, please confirm",
		
		//常用服务器管理
		"本地服务器" : "Local Server",
		"获取常用服务器列表失败" : "Failed to get server list",		
		"添加常用服务器" : "Add Prefer Server",
		"无法修改本地服务器！" : "Unable to configure Local Server",
		"设置常用服务器" : "Edit Prefer Server",
		"无法删除本地服务器！" : "Unable to remove Local Server",
		"网站地址不能为空" : "WebSite Link is empty",
		"服务器地址不能为空" : "Server URL is empty",
		"添加成功" : "Add Ok",
		"添加失败" : "Add Failed",		
		"服务器设置未改动" : "Server Config not changed",
		"修改成功" : "Modify Ok",
		"修改失败" : "Modify Failed",
		"设置未改动" : "Config not changed",
		"设置" : "Edit",
		"退出设置" : "Exit Edit",	
		"是否删除该用户的访问权限" : "Do you want to remove the user to access",
		"移除失败" : "Remove Failed",
		"添加" : "Add",
		"移除" : "Remove",
		
		//文件搜索
		"文件选择" : "Select File",
		"文件搜索失败" : "Failed to search",
		
		//仓库显示模式
		"标准模式" : "Standard Mode",
		"电子书模式" : "E-Book Mode",
		
		//文件Checkout/CheckIn
		"请选择需要检出文件" : "No any file was selected to check out",
		
		//文件分享
		"获取文件分享信息失败" : "Failed to get document share information",		
		"当前为分享链接，无法再次分享！" : "Shared Link can not be shared again",
		"创建文件分享失败" : "Failed to share",
		"是否删除该分享" : "Do you want to delete this sharing",
		"文件分享列表获取失败" : "Failed to get sharing list",
		"文件分享已过期" : "Document Share is out of date",
		"分享链接已复制" : "ShareLink was copied",
		"分享二维码" : "Document Share QR Code",
		"发送分享链接" : "Mail ShareLink",
		"来自MxsDoc的邮件" : "From MxsDoc",
		"请填写接收人信息" : "Please enter the Email",
		"验证失败" : "Verify Failed",
		"密码错误！" : "Password Error !",
		
		//远程存储
		"该仓库未设置远程存储" : "Remote Storage was not configured for this repository",		
		
		//管理后台
		"非管理员用户，请联系系统管理员" : "You are not Administrator, Please contact System Adminstrator",
		"载入中..." : "Loading...",
		"用户管理" : "User Management",
		"用户组管理" : "Group Management",
		"仓库管理" : "Repository Management",
		"系统日志管理" : "SystemLog Management",
		"日志管理" : "SystemLog Management",
		"系统管理" : "System Management",
		"证书管理" : "License Management",
		"订单管理" : "Order Management",
		"重启服务" : "Restart Server",
		"是否重启服务" : "Do you want to restart server",
		"系统重启中，请稍候..." : "System is restarting...",
		"重启失败" : "Restart Failed",
		"测试成功" : "Test Success",
		"测试失败" : "Test Failed",
		"重置" : "Reset",
		"是否重置" : "Do you want to reset",
		"重置成功" : "Reset Success",
		"重置失败" : "Reset Failed",
		"生成密钥成功" : "Key Generate Ok",
		"生成密钥失败" : "Key Generate Failed",
		"密钥导出失败" : "Export Key Failed",
		"删除密钥" : "Delete Key",
		"是否删除系统密钥" : "Do you want to delete System Key",
		"删除密钥成功" : "Delete Key Success",		
		"删除密钥失败" : "Failed to delete Key",	
		"请选择证书文件" : "Please select a license file",
		"清除锁定" : "Clean Locks",
		"是否清除系统所有锁定" : "Do you want to clean all locks",
		"安装成功" : "Install Success",
		"安装失败" : "Install Failed",
		"生成成功" : "Generate Success",
		"生成失败" : "Generate Failed",
		"重置成功" : "Reset Success",
		"重置失败" : "Reset Failed",
		"清除成功" : "Clean Success",
		"清除失败" : "Clean Failed",
		"禁用系统" : "Disable System",
		"是否禁用系统" : "Do you want to disable system access",
		"禁用系统失败" : "Failed to disable system access",
		"启用系统" : "Enable System",
		"是否启用系统" : "Do you want to enable system access",
		"启用系统失败" : "Failed to enable system access",
		
		//User
		"导出用户" : "Export Users",
		"是否导出用户？" : "Do you want to export users ?",
		"导出用户列表失败" : "Failed to export users",
		"是否删除用户？" : "Do you want to delete this user ?",
		
		//Group 
		"是否删除用户组？" : "Do you want to delete this group ?",
		"成员管理" : "Member Management",
		
		//Repository
		"是否删除仓库，仓库数据将无法恢复？" : "All data will be deleted, Please confirm you have backuped the data ?",
		
		//Log
		"日志下载失败" : "DebugLog Download Failed",
		"清除日志" : "Clean DebugLog",
		"是否清除调试日志？" : "Do you want to clean DebugLog ?",
		"清除日志成功" : "DebugLog was cleaned",
		"清除日志失败" : "Failed to clean DebugLog",
		
		//Email Service
		"获取邮件服务配置信息失败" : "Failed to get Email Service Settings",
		"更新邮件服务配置成功" : " Email Service Settings was updated",
		"更新邮件服务配置失败" : "Failed to update Email Service Settings",
		
		//SMS Service
		"获取短信服务配置信息失败" : "Failed to get SMS Service Settings",
		"更新短信服务配置成功" : "SMS Service Settings was updated",
		"更新短信服务配置失败" : "Failed to update SMS Service Settings",	
		
		//Migration
		"请选择需要迁移的仓库！" : "No any repository was selected to migrate",
		"系统迁移" : "System Migrate",
		
		//Data Source
		"获取数据库配置信息失败" : "Failed to get Data Source Setttings",
		"更新成功,重启服务后生效！" : "Configure Ok, Please restart MxsDoc !",
		"更新数据库配置信息失败" : "Failed to update Data Source Setttings",
		"数据库连接成功" : "DataBase Connect Ok",
		"数据库连接失败" : "DataBase Connect Failed",
		"删除数据库" : "Delete DataBase",
		"是否删除数据库？" : "Do you  want to delete DataBase ?",				
		"删除数据库成功" : "DataBase was deleted",
		"删除数据库失败" : "Failed to delete DataBase",
		"重置数据库" : "Reset DataBase",
		"是否重置数据库？" : "Do you want to reset DataBase",
		"重置数据库成功" : "DataBase reset Ok",
		"重置数据库失败" : "DataBase reset Failed",
		"数据库导出失败" : "Failed to export data from DataBase",		
		"导入数据" : "Import Data",
		"是否导入" : "Do you want to import",		
		"导入成功，建议重启服务！" : "Import Ok, Please restart MxsDoc !",
		"导入失败" : "Import Failed",
		
		//SystemInfo
		"更新成功！" : "Update Ok !",
		
		//Upgrade System
		"系统升级中，请稍后重试!" : "Upgrading MxsDoc, Please try later !",
		"正在安装Office，请稍后重试!" : "Installing OfficeEditor, Please try later !",
		"正在在线安装Office，请稍后重试!" : "Online Installing OfficeEditor, Please try later !",		
		"正在安装字体，请稍后重试!" : "Installing Office Fonts, Please try later !",		
		"正在重置字体库，请稍后重试!" : "Reseting Office Fonts, Please try later !",		
		"非法升级文件" : "Invalid Upgrade Package",
		"系统升级" : "System Upgrade",
		"是否升级系统？" : "Do you want to upgrade System ?",
		"正在上传" : "Uploading",
		"正在重传" : "Re-Uploading",
		"重传" : "Retry",
		"解压安装文件" : "Unzip upgrade package",
		"开始升级，请稍候..." : "Upgrading...",
		"升级任务已取消" : "Upgrade task was canceled",
		"升级任务取消失败" : "Failed to stop Upgrade task",
		"升级准备中" : "Upgrade prepare",
		"升级准备中" : "Upgrade prepare",
		"取消升级" : "Cancel Upgrade",
		"是否取消系统升级！" : "Do you want to cancel upgrade",
		"禁用Office" : "Disable OfficeEditor",
		"是否禁用Office？" : "Do you want to disable OfficeEditor",
		"禁用" : "Disable",
		"启用Office" : "Enable OfficeEditor",
		"是否启用Office？" : "Do you want to enable OfficeEditor ?",
		"启用" : "Enable",
		"禁用Office失败" : "Failed to disable OfficeEditor",
		"启用Office失败" : "Failed to enable OfficeEditor",		
		"非法Office安装文件!" : "Invalid OfficeEditor Package !",
		"安装Office" : "Install OfficeEditor",
		"安装字体" : "Install Office Fonts",
		"是否安装字体？" : "Do you want to install Office Fonts?",
		"开始安装..." : "Start to install...",
		"Office编辑器安装完成" : "OfficeEditor install Ok",
		"Office安装任务已取消" : "OfficeEditor install task was canceled",
		"Office安装任务取消失败" : "Failed to stop OfficeEditor install task",
		"字体安装完成" : "Office Fonts install Ok",
		"字体安装任务已取消" : "Office Fonts install task was canceled",
		"字体安装任务取消失败" : "Failed to stop Office Fonts install task",
		"安装准备中..." : "Install prepare...",
		"取消安装" : "Cancel Install",
		"是否取消Office安装！" : "Do you want to cancel install OfficeEditor ?",
		"是否取消字体安装！" : "Do you want to cancel install Office Fonts ?",
		"安装成功" : "Install Success",
		
		//System
		"新建仓库" : "Create Reposiotry",
		"删除仓库" : "Delete Reposiotry",
		"修改仓库" : "Modify Reposiotry",
		"设置仓库权限" : "Configure Reposiotry Permission",
		"删除仓库权限" : "Delete Reposiotry Permission",
		"文件推送" : "Push Document",
		"上传文件" : "Upload Document",
		"新增文件" : "Add Document",
		"删除文件" : "Delete Document",
		"下载文件" : "Donwload Document",
		"复制文件" : "Copy Document",
		"移动文件" : "Move Document",
		"重命名文件" : "Rename Document",
		"更新文件内容" : "Update Document Content",
		"上传备注图片" : "Upload Note's Images",
		"锁定文件" : "Lock Document",
		"解锁文件" : "Unlock Document",
		"设置文件访问密码" : "Set File Access Password",
		"下载历史文件" : "Download History Document",
		"恢复文件历史版本" : "Recover History",
		"新建文件分享" : "Create Document Sharing",
		"修改文件分享" : "Modify Document Sharing",
		"删除文件分享" : "Delete Document Sharing",
		"设置文件权限" : "Configure Document Permission",
		"删除文件权限" : "Delete Document Permission",
		"登录系统" : "Sign In",
		"退出登录" : "Log Out",
		"用户注册" : "Sign Up",
		
		//SystemLog
		"删除系统日志" : "Delete SystemLog",
		"是否删除系统日志？" : "Do you want to delete SystemLog ?",
		"删除成功" : "Delete Success",
		"删除失败" : "Delete Failed",		
			
		//Order
		"获取订单列表失败" : "Failed to get Order List",
		"付款" : "Pay",
		"退款" : "Refund",
		"未完成" : "Processing",
		"成功" : "Success",
		"失败" : "Failed",
		"已全额退款" : "Refunded",
		"是否退款？" : "Do you want to refund ?",
		"退款成功" : "Refund Success",
		"退款失败" : "Refund Failed",
		
		//License
		"获取证书列表失败" : "Failed to get License List",
		
		//Office License
		"新增Office证书" : "Add OfficeEidtor License",
		
		//仓库类型
		"文件管理系统" : "Storage",
		"文件服务器前置" : "Front-End",
		
		"是否清除所有仓库缓存？" : "Clean cache for all repositories ?",
		"清除成功！" : "Clean Success !",
		"转换仓库历史" : "Convert History Format",
		"是否将仓库历史转换成最新格式？" : "Do you want to convert history to latest format ?",
		"仓库历史转换成功" : "History Format Convert Success",
		"仓库历史转换失败" : "History Format Convert Failed",
		"禁用仓库" : "Disable Repository",
		"是否禁用仓库？" : "Do you want to enable Repository ?",
		"启用仓库" : "Enable Repository",
		"是否启用仓库？" : "Do you want to enable Repository ?",	
		"禁用仓库失败" : "Disable Repository Failed",
		"启用仓库失败" : "Enable Repository Failed",
		"备份仓库" : "Backup Repository",
		"备份仓库可能需要占用服务器较大的磁盘空间，是否备份仓库？" : "Repoistory Backup may need huge storage space, Do you want to continue ?",
		"仓库备份中，可能需要花费较长时间，您可先关闭当前窗口！" : "Repository Backup...",
		"备份仓库失败" : "Repository Backup Failed",
		"仓库备份成功" : "Repository Backup Success",
		"仓库备份成功，是否下载仓库备份文件？" : "Repository Backup Success, Do you want to download the backup ?",
		
		//分页显示
		"首页" : "Top",
		"尾页" : "End",
		"上一页" : "Prev",
		"下一页" : "Next",		
 	};
	
	var newStr = translateMap[str];
	if ( undefined == newStr )
	{
		return str;
	}
	
	return newStr;
}