package com.DocSystem.common.CommitAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.DocSystem.common.DocUtil;
import com.DocSystem.entity.Doc;

public class CommitAction{
    private CommitType action; //1:add 2:delete 3:modify 4:move 5:copy
    
    private Doc doc;
    private Doc newDoc;

    private String localRootPath;
    private String localRefRootPath;
    
    //Sub Action List
    public boolean isSubAction = false;
    private List<CommitAction> subActionList = null;
	
    //result: execute result
    public boolean result = true;
    
	public void setAction(CommitType commitActionType) {
		this.action = commitActionType;
	}
	
	public CommitType getAction()
	{
		return action;
	}
	
	public void setDoc(Doc doc) {
		this.doc = doc;
	}
	
	public Doc getDoc()
	{
		return doc;
	}

	public void setNewDoc(Doc newDoc) {
		this.newDoc = newDoc;
	}
	
	public Doc getNewDoc()
	{
		return newDoc;
	}

	public void setLocalRefRootPath(String localRefRootPath) {
		this.localRefRootPath = localRefRootPath;
	}
	
	public String getLocalRefRootPath()
	{
		return localRefRootPath;
	}
	
	public void setLocalRootPath(String localRootPath) {
		this.localRootPath = localRootPath;
	}
	
	public String getLocalRootPath()
	{
		return localRootPath;
	}
	
	public void setSubActionList(List<CommitAction> subActionList) {
		this.subActionList = subActionList;
	}
	
	public List<CommitAction> getSubActionList()
	{
		return subActionList;
	}
	
	public void setResult(boolean result) {
		this.result = result;
	}
	public boolean getResult()
	{
		return result;
	}
	
	/******************************** Basic Interface for CommitAction *************************************/
	//版本仓库底层通用接口
	protected void insertAddFileAction(List<CommitAction> actionList, Doc doc, boolean isSubAction, boolean isGit) {
		if(isGit && doc.getName().equals(".git"))
		{
			return;
		}
		//printObject("insertAddFileAction:", doc);
		
    	CommitAction action = new CommitAction();
    	action.setAction(CommitType.ADD);
    	action.setDoc(doc);
    	action.isSubAction = isSubAction;
    	actionList.add(action);
	}
    
	protected void insertAddDirAction(List<CommitAction> actionList,Doc doc, boolean isSubAction, boolean isGit) 
	{
		if(isGit && doc.getName().equals(".git"))
		{
			return;
		}
		//printObject("insertAddDirAction:", doc);
		System.out.println("insertAddDirAction() " + doc.getPath() + doc.getName());

		String localParentPath = doc.getLocalRootPath() + doc.getPath();
		File dir = new File(localParentPath, doc.getName());
		File[] tmp = dir.listFiles();
		
		//there is not subNodes under dir
		if(tmp == null || tmp.length == 0)
		{
	    	CommitAction action = new CommitAction();
	    	action.setAction(CommitType.ADD);
	    	action.setDoc(doc);
	    	action.isSubAction = isSubAction;
	    	action.setSubActionList(null);
	    	actionList.add(action);
	    	return;
		}
		
		//Build subActionList
    	String subParentPath = doc.getPath() + doc.getName() + "/";
    	if(doc.getName().isEmpty())
    	{
    		subParentPath = doc.getPath();
    	}
    	int subDocLevel = doc.getLevel() + 1;
    	
		List<CommitAction> subActionList = new ArrayList<CommitAction>();
	    for(int i=0;i<tmp.length;i++)
	    {
	    	File localEntry = tmp[i];
	    	int subDocType = localEntry.isFile()? 1: 2;
	    	Doc subDoc = DocUtil.buildBasicDoc(doc.getVid(), null, doc.getDocId(),  doc.getReposPath(), subParentPath, localEntry.getName(), subDocLevel, subDocType, doc.getIsRealDoc(), doc.getLocalRootPath(), doc.getLocalVRootPath(), localEntry.length(), "");
	    	if(localEntry.isDirectory())
	    	{	
	    		insertAddDirAction(subActionList,subDoc,true, isGit);
	    	}
	    	else
	    	{
	    		insertAddFileAction(subActionList,subDoc,true, isGit);
	    	}
	 	}
		
    	CommitAction action = new CommitAction();
    	action.setAction(CommitType.ADD);
    	action.setDoc(doc);
    	action.isSubAction = isSubAction;
    	action.setSubActionList(subActionList);
    	actionList.add(action);    	
	}
	
	protected void insertDeleteAction(List<CommitAction> actionList, Doc doc, boolean isGit) {
		if(isGit && doc.getName().equals(".git"))
		{
			return;
		}

		//printObject("insertDeleteAction:", doc);
		
		System.out.println("insertDeleteAction() " + doc.getPath() + doc.getName());

    	CommitAction action = new CommitAction();
    	action.setAction(CommitType.DELETE);
    	action.setDoc(doc);
    	actionList.add(action);
	}
    
	protected void insertModifyAction(List<CommitAction> actionList, Doc doc, boolean isGit) {
		if(isGit && doc.getName().equals(".git"))
		{
			return;
		}

		//printObject("insertModifyAction:", doc);
		System.out.println("insertModifyAction() " + doc.getPath() + doc.getName());

		CommitAction action = new CommitAction();
    	action.setAction(CommitType.MODIFY);
    	action.setDoc(doc);
    	actionList.add(action);	
	}
	
	public static void insertAction(List<CommitAction> actionList, Doc doc, CommitType actionType, boolean isGit) {
		if(isGit && doc.getName().equals(".git"))
		{
			return;
		}

		//printObject("insertAction:", doc);
		System.out.println("insertAction() actionType:" + actionType + " doc:" + doc.getPath() + doc.getName());

		CommitAction action = new CommitAction();
    	action.setAction(actionType);
    	action.setDoc(doc);
    	actionList.add(action);	
	}
}