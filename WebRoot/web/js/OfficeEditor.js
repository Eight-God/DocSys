//OfficeEditor类
var OfficeEditor = (function () {	
	var docEditor;
    var docInfo;
    var fileType;
    var documentType;
    var title;
    var dockey;
    var historyList;

	//For ArtDialog
	function initForArtDialog() 
	{
	    var params = GetRequest();
	    var docid = params['docid'];
	    //获取artDialog父窗口传递过来的参数
	    var artDialog2 = window.top.artDialogList['ArtDialog'+docid];
	    if (artDialog2 == null) {
	        artDialog2 = window.parent.artDialogList['ArtDialog' + docid];
	    }
	    
	    // 获取对话框传递过来的数据
	    docInfo = artDialog2.config.data;
		console.log("docInfo:",docInfo);
		
		init();
	}
	
	//For NewPage
	function initForNewPage()
	{
	    docInfo = getDocInfoFromRequestParamStr();	    
	    
	    init();
	}
	
	//For BootstrapDialog
	function PageInit(Input_doc)
	{
		console.log("PageInit InputDoc:", Input_doc);
		docInfo = Input_doc;
		
		init();
	}	

	function init()
	{
	    fileType = getFileSuffix(docInfo.name);
	    fileType = convertWpsToOfficeType(fileType);
	    documentType = getDocumentType(fileType);
	    title = docInfo.name;
	    dockey = docInfo.docId + "";
	
	    getDocOfficeLink(docInfo, showOffice, showErrorMessage, "REST", 1);
	    document.title = docInfo.name;
	}    
	
    function GetRequest() {
        var url = location.search; //获取url中"?"符后的字串
        var theRequest = {};
        if (url.indexOf("?") !== -1) {
            var str = url.substr(1);
            var strs = str.split("&");
            for(var i = 0; i < strs.length; i ++) {
                theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
            }
        }
        return theRequest;
    }
    
    function showOffice(data)
   	{
		var fileLink = buildFullLink(data.fileLink);
		var saveFileLink = "";
		dockey = data.key + "";	//key是用来标志唯一性的，文件改动了key也必须改动
		
    	console.log("showOffice() title=" + title + " dockey=" + dockey + " fileType=" + fileType + " documentType=" + documentType + " fileLink="+fileLink + " saveFileLink:" + saveFileLink);
		var type = 'desktop';
    	if(gIsPC == false)
		{
			type = 'mobile';
		}
    	
		var editEn =  data.editEn == 1? true:false;
		var mode = "view";
		if(editEn == true)
		{
			saveFileLink = buildFullLink(data.saveFileLink);
			mode = "edit";
		}
		var downloadEn = data.downloadEn == 1? true:false;
		console.log("editEn:" + editEn + " downloadEn:" + downloadEn);
		var user = {
            "id": data.userId + "",
            "name": data.userName,
        };
		
        var innerAlert = function (message) {
            if (console && console.log)
                console.log(message);
        };
		
    	var onRequestHistory = function() {
    	    console.log("onRequestHistory()");
    	    getOfficeDocHistoryList(docInfo, initOfficeDocHistoryList);
    	};
    	
    	var onRequestHistoryData = function(event) {
    		console.log("onRequestHistoryData() event:", event);
    		var version = event.data;
    		setOfficeDocHistoryData(version);
    	};
    	
    	var onRequestHistoryClose = function (event){
    		innerAlert("onRequestHistoryClose() event:", event);
            document.location.reload();
        };
        
        var onError = function (event) {
            if (event)
                innerAlert(event.data);
        };

        var onOutdatedVersion = function (event) {
        	innerAlert("onOutdatedVersion() event:", event);
        	//reload will trigger state change loop
        	location.reload(true);
        };

        var replaceActionLink = function(href, linkParam) {
            var link;
            var actionIndex = href.indexOf("&action=");
            if (actionIndex != -1) {
                var endIndex = href.indexOf("&", actionIndex + "&action=".length);
                if (endIndex != -1) {
                    link = href.substring(0, actionIndex) + href.substring(endIndex) + "&action=" + encodeURIComponent(linkParam);
                } else {
                    link = href.substring(0, actionIndex) + "&action=" + encodeURIComponent(linkParam);
                }
            } else {
                link = href + "&action=" + encodeURIComponent(linkParam);
            }
            return link;
        }

        var onMakeActionLink = function (event) {
        	innerAlert("onMakeActionLink() event:", event);
            var actionData = event.data;
            var linkParam = JSON.stringify(actionData);
            docEditor.setActionLink(replaceActionLink(location.href, linkParam));
        };
        
        
		
    	var config = {
				"type": type,
    		    "document": {
    		        "fileType": fileType,
    		        "key": dockey,
    		        "title": title,
    		        "url": fileLink,
    		        "permissions": {
    		            "comment": editEn,
    		            "download": downloadEn,
    		            "edit": editEn,
    		            "fillForms": editEn,
    		            "modifyContentControl": editEn,
    		            "modifyFilter": editEn,
    		            "print": downloadEn,
    		            "review": true,
    		            "changeHistory": true
    		        },
    		    },
    		    "documentType": documentType,
    		    "editorConfig": {
    		    	"mode": mode,
                    "callbackUrl": saveFileLink,
                    "lang": "zh-CN",
                    "user": user,
                    "spellcheck": false,
                },
    		    "events": {
    		    	"onError": onError,
    		    	"onRequestHistory": onRequestHistory,
    		        "onRequestHistoryData": onRequestHistoryData,
    		        "onRequestHistoryClose": onRequestHistoryClose,
    		        //"onOutdatedVersion": onOutdatedVersion,
                    //"onMakeActionLink": onMakeActionLink,
    		    },
                "height": "100%",
                "width": "100%",
    	};
        docEditor = new DocsAPI.DocEditor("placeholder", config);
        
        function getOfficeDocHistoryList(docInfo, successCallback, errorCallback)
        {	
        	console.log("getOfficeDocHistoryList()  docInfo:", docInfo);
            if(!docInfo || docInfo == null || docInfo.id == 0)
            {
            	//未定义需要显示的文件
            	errorInfo = "请选择文件";
            	errorCallback && errorCallback(errorInfo);
            	return;
            }
          	
            var url = "/DocSystem/web/static/office-editor/getOfficeDocHistoryList.do"
            $.ajax({
                url : url,
                type : "post",
                dataType : "json",
                data : {
                	reposId: docInfo.vid,
                    path: docInfo.path,
                    name: docInfo.name,
                    shareId: docInfo.shareId,
                },
                success : function (ret) {
                	console.log("getOfficeDocHistoryList ret",ret);
                	if( "ok" == ret.status )
                	{
                		successCallback &&successCallback(ret.data, ret.dataEx);
                    }
                    else 
                    {
                    	console.log(ret.msgInfo);
                    	errorInfo = "获取文件信息失败：" + ret.msgInfo;
                    	errorCallback && errorCallback(errorInfo);
                    }
                },
                error : function () {
                	errorInfo = "获取文件信息失败：服务器异常";
                	errorCallback && errorCallback(errorInfo);
                }
            });
        }

        function initOfficeDocHistoryList(list, dataEx)
        {
        	if(list)
        	{
        		historyList = [];
        		for(var i=0; i<list.length; i++)
        		{
        			var data = list[i];
        			var history = {};
        			history.serverVersion = dataEx.serverVersion;
        			history.user = {};
        			history.user.id = data.useridoriginal;
        			history.user.name = data.userName !== undefined ? data.userName:data.user;
    				history.orgKey = data.docId;
        			history.created = formatTime(data.time);
        			history.version = i+1;
        			history.path = dataEx.path;
        			history.name = dataEx.name;
        			history.url = buildHistoryUrl(docInfo, history, data.orgChangeIndex);
        			if(data.orgChangeIndex === undefined)
        			{
    					//First history have no previous info
    					history.orgChangeIndex = -1;
        			}
    				else
    				{
        				history.orgChangeIndex = data.orgChangeIndex;
        				history.changesUrl = buildChangesUrl(docInfo, history);	        				
        				history.previous = buildPreviousHistory(docInfo, history);
        			}
    				//update history.key会触发auth，暂时不修改
        			history.key = dockey + "_" + history.orgKey + "_" + history.orgChangeIndex; //history dockey
        			
        			console.log("initOfficeDocHistoryList history[" + i + "]", history);
        			historyList.push(history);
        		}
        		
        		var currentVersion = list.length;
        		docEditor.refreshHistory({
        	        "currentVersion": currentVersion,
        	        "history": historyList
        	    });
        	}
        }
        
        function buildPreviousHistory(docInfo, history)
        {
        	var previous = {};
			previous.orgChangeIndex = history.orgChangeIndex - 1;
			previous.key = dockey + "_" + history.orgKey + "_" + previous.orgChangeIndex;
			previous.url = buildHistoryUrl(docInfo, history, previous.orgChangeIndex);
			return previous;
		}
        
        function buildHistoryUrl(docInfo, history, orgChangeIndex)
        {
        	var url = "/DocSystem/web/static/office-editor/downloadHistory/" 
        				+ docInfo.vid 
        				+ "/" + history.path 
        				+ "/" + history.name 
        				+ "/" + history.key;
        	
        	if(orgChangeIndex  !== undefined)
        	{
        		url += "/" + orgChangeIndex;
        	}
        	else
        	{
        		url += "/-1";
        	}
        	
        	if(docInfo.authCode !== undefined)
       		{
        		url += "/" + docInfo.authCode;
       		}
       		else
       		{
       			url += "/0";
       		}
        	
       		if(docInfo.shareId !== undefined)
       		{
       			url +=  "/"  + docInfo.shareId;
       		}
       		else
       		{
       			url += "/0";
       		}
        	
        	return buildFullLink(url);
        }
        
        function buildChangesUrl(docInfo, history)
        {
        	var url = "/DocSystem/web/static/office-editor/downloadHistoryDiff/" 
        				+ docInfo.vid + "/" + history.path + "/" + history.name 
        				+ "/" + history.key;
        	
        	if(history.orgChangeIndex !== undefined)
        	{
        		url += "/" + history.orgChangeIndex;
        	}
        	else
        	{
        		url += "/-1";;
        	}
       		
        	if(docInfo.authCode !== undefined)
       		{
        		url += "/" + docInfo.authCode;
       		}
       		else
       		{
       			url += "/0";
       		}
        	
       		if(docInfo.shareId !== undefined)
       		{
       			url +=  "/"  + docInfo.shareId;
       		}
       		else
       		{
       			url += "/0";
       		}
        	
        	return buildFullLink(url);
        }

        function setOfficeDocHistoryData(version)
        {
        	if(historyList)
        	{
        		var data = historyList[version-1];
        		console.log("setOfficeDocHistoryData() data:", data);
        		docEditor.setHistoryData(data);
        	}
        }	        
   	}
	
 	function formatTime(time){
 		var now = new Date(time);
 		var year=now.getFullYear();
 		var month=now.getMonth()+1;
 		var date=now.getDate();
 		var hh=now.getHours();
 		var mm=now.getMinutes();
 		var ss=now.getSeconds();

 		return year+"-"+month+"-"+date + " " + hh+":"+mm+":"+ss;
 	}
	
	//开放给外部的调用接口
	return {
		initForArtDialog: function(){
			initForArtDialog();
	    },
	    initForNewPage: function(){
	    	initForNewPage();
	    },
	    PageInit: function(docInfo){
        	PageInit(docInfo);
        },
	};
})();